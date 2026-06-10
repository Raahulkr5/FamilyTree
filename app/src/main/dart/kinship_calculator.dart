/// Kinship Relationship Calculator for Family Tree Members.
/// Solves exact kinship terms (Parents, Siblings, Cousins, Uncles, Nieces, etc.)
/// based on closest common ancestors (CCA) and spouse linkages.
class Member {
  final int id;
  final String firstName;
  final String lastName;
  final String gender; // "Male", "Female", "Other"

  Member({
    required this.id,
    required this.firstName,
    required this.lastName,
    required this.gender,
  });

  String get fullName => '$firstName $lastName';
}

enum RelationshipType {
  parentOf,
  spouseOf,
}

class Relationship {
  final int memberId;
  final int relatedMemberId;
  final RelationshipType type;

  Relationship({
    required this.memberId,
    required this.relatedMemberId,
    required this.type,
  });
}

class KinshipResult {
  final String term;
  final int degreeOfConsanguinity;
  final String description;

  KinshipResult({
    required this.term,
    required this.degreeOfConsanguinity,
    required this.description,
  });

  @override
  String toString() => '$term (Degree: $degreeOfConsanguinity) - $description';
}

class KinshipCalculator {
  final List<Member> members;
  final List<Relationship> relationships;

  // Cache parent lists to speed up BFS traverses
  final Map<int, List<int>> _parentsMap = {};
  final Map<int, List<int>> _childrenMap = {};
  final Map<int, int> _spousesMap = {};

  KinshipCalculator({required this.members, required this.relationships}) {
    _initializeAdjacency();
  }

  void _initializeAdjacency() {
    for (var rel in relationships) {
      if (rel.type == RelationshipType.parentOf) {
        // rel.memberId is PARENT OF rel.relatedMemberId (child)
        _parentsMap.putIfAbsent(rel.relatedMemberId, () => []).add(rel.memberId);
        _childrenMap.putIfAbsent(rel.memberId, () => []).add(rel.relatedMemberId);
      } else if (rel.type == RelationshipType.spouseOf) {
        _spousesMap[rel.memberId] = rel.relatedMemberId;
        _spousesMap[rel.relatedMemberId] = rel.memberId;
      }
    }
  }

  /// Calculates the kinship relation from [fromMemberId] to [toMemberId].
  KinshipResult calculateRelationship(int fromMemberId, int toMemberId) {
    if (fromMemberId == toMemberId) {
      return KinshipResult(
        term: "Self",
        degreeOfConsanguinity: 0,
        description: "You are looking at the same family member profile.",
      );
    }

    // 1. Direct Spouse Check
    if (_spousesMap[fromMemberId] == toMemberId) {
      final toMember = _getMember(toMemberId);
      String term = "Spouse";
      if (toMember?.gender == "Male") term = "Husband";
      if (toMember?.gender == "Female") term = "Wife";
      return KinshipResult(
        term: term,
        degreeOfConsanguinity: 1,
        description: "Partner joined in matrimonial union.",
      );
    }

    // 2. Build Ancestor Path maps via BFS / DFS to retrieve distance to all ancestors
    final Map<int, int> pathFromA = _getAncestorDistances(fromMemberId);
    final Map<int, int> pathFromB = _getAncestorDistances(toMemberId);

    // Find all common ancestors
    final List<int> commonAncestors = [];
    for (var ancestorId in pathFromA.keys) {
      if (pathFromB.containsKey(ancestorId)) {
        commonAncestors.add(ancestorId);
      }
    }

    final toMember = _getMember(toMemberId);
    final toGender = toMember?.gender ?? "Other";

    if (commonAncestors.isNotEmpty) {
      // Find the Closest Common Ancestor (CCA) that minimizes pathFromA[ancestor] + pathFromB[ancestor]
      int? bestCCA;
      int minCombinedDistance = 999999;

      for (var cca in commonAncestors) {
        int distA = pathFromA[cca]!;
        int distB = pathFromB[cca]!;
        int combined = distA + distB;
        if (combined < minCombinedDistance) {
          minCombinedDistance = combined;
          bestCCA = cca;
        }
      }

      if (bestCCA != null) {
        int dA = pathFromA[bestCCA]!;
        int dB = pathFromB[bestCCA]!;

        return _getTermFromDistances(dA, dB, toGender, minCombinedDistance);
      }
    }

    // 3. Check for Affinal Relations (through spouses: e.g. Mother-in-law, Sibling-in-law)
    final spouseOfA = _spousesMap[fromMemberId];
    if (spouseOfA != null) {
      final spouseRelation = calculateRelationship(spouseOfA, toMemberId);
      if (spouseRelation.term != "Unknown" && spouseRelation.term != "Self") {
        return KinshipResult(
          term: "${spouseRelation.term}-in-law",
          degreeOfConsanguinity: spouseRelation.degreeOfConsanguinity + 1,
          description: "Affinal relative linked through marriage with your spouse.",
        );
      }
    }

    final spouseOfB = _spousesMap[toMemberId];
    if (spouseOfB != null) {
      final ownRelationToSpouseOfB = calculateRelationship(fromMemberId, spouseOfB);
      if (ownRelationToSpouseOfB.term != "Unknown" && ownRelationToSpouseOfB.term != "Self") {
        return KinshipResult(
          term: "${ownRelationToSpouseOfB.term}'s Spouse",
          degreeOfConsanguinity: ownRelationToSpouseOfB.degreeOfConsanguinity + 1,
          description: "Matrimonial partner of your core blood relative.",
        );
      }
    }

    return KinshipResult(
      term: "Distant Relative",
      degreeOfConsanguinity: 9,
      description: "No direct lineage overlap or immediate marriage connection found in this repository.",
    );
  }

  Map<int, int> _getAncestorDistances(int startId) {
    final Map<int, int> distances = {startId: 0};
    final List<int> queue = [startId];

    while (queue.isNotEmpty) {
      final current = queue.removeAt(0);
      final currentDist = distances[current]!;

      final parents = _parentsMap[current] ?? [];
      for (var p in parents) {
        if (!distances.containsKey(p)) {
          distances[p] = currentDist + 1;
          queue.add(p);
        }
      }
    }
    return distances;
  }

  KinshipResult _getTermFromDistances(int dA, int dB, String gender, int degree) {
    if (dA == 0) {
      // B is direct ancestor of A
      if (dB == 1) {
        return KinshipResult(
          term: gender == "Male" ? "Father" : (gender == "Female" ? "Mother" : "Parent"),
          degreeOfConsanguinity: degree,
          description: "Immediate direct generation antecedent.",
        );
      } else if (dB == 2) {
        return KinshipResult(
          term: gender == "Male" ? "Grandfather" : (gender == "Female" ? "Grandmother" : "Grandparent"),
          degreeOfConsanguinity: degree,
          description: "Generational antecedent twice removed.",
        );
      } else {
        String prefix = "Great-" * (dB - 2);
        return KinshipResult(
          term: "$prefix${gender == "Male" ? "Grandfather" : (gender == "Female" ? "Grandmother" : "Grandparent")}",
          degreeOfConsanguinity: degree,
          description: "Historical direct lineage ancestor.",
        );
      }
    }

    if (dB == 0) {
      // B is direct descendant of A
      if (dA == 1) {
        return KinshipResult(
          term: gender == "Male" ? "Son" : (gender == "Female" ? "Daughter" : "Child"),
          degreeOfConsanguinity: degree,
          description: "Direct generation successor.",
        );
      } else if (dA == 2) {
        return KinshipResult(
          term: gender == "Male" ? "Grandson" : (gender == "Female" ? "Granddaughter" : "Grandchild"),
          degreeOfConsanguinity: degree,
          description: "Generational successor twice removed.",
        );
      } else {
        String prefix = "Great-" * (dA - 2);
        return KinshipResult(
          term: "$prefix${gender == "Male" ? "Grandson" : (gender == "Female" ? "Granddaughter" : "Grandchild")}",
          degreeOfConsanguinity: degree,
          description: "Historical direct descendancy partner.",
        );
      }
    }

    if (dA == 1) {
      if (dB == 1) {
        return KinshipResult(
          term: gender == "Male" ? "Brother" : (gender == "Female" ? "Sister" : "Sibling"),
          degreeOfConsanguinity: degree,
          description: "Immediate sibling sharing common parentage.",
        );
      } else if (dB == 2) {
        return KinshipResult(
          term: gender == "Male" ? "Uncle" : (gender == "Female" ? "Aunt" : "Aunt/Uncle"),
          degreeOfConsanguinity: degree,
          description: "Sibling of your direct parent.",
        );
      } else {
        String prefix = "Great-" * (dB - 2);
        return KinshipResult(
          term: "$prefix${gender == "Male" ? "Uncle" : (gender == "Female" ? "Aunt" : "Aunt/Uncle")}",
          degreeOfConsanguinity: degree,
          description: "Generational sibling of your historical grandparents.",
        );
      }
    }

    if (dB == 1) {
      if (dA == 2) {
        return KinshipResult(
          term: gender == "Male" ? "Nephew" : (gender == "Female" ? "Niece" : "Nibblet"),
          degreeOfConsanguinity: degree,
          description: "Offspring of your direct sibling.",
        );
      } else {
        String prefix = "Great-" * (dA - 2);
        return KinshipResult(
          term: "$prefix${gender == "Male" ? "Nephew" : (gender == "Female" ? "Niece" : "Nibblet")}",
          degreeOfConsanguinity: degree,
          description: "Offspring of your historical nephew/niece pipeline.",
        );
      }
    }

    // Cousins
    int minD = dA < dB ? dA : dB;
    int maxD = dA > dB ? dA : dB;
    int cousinDegree = minD - 1;
    int removal = maxD - minD;

    String ordinal;
    if (cousinDegree == 1) {
      ordinal = "First";
    } else if (cousinDegree == 2) {
      ordinal = "Second";
    } else if (cousinDegree == 3) {
      ordinal = "Third";
    } else {
      ordinal = "${cousinDegree}th";
    }

    String removeStr = "";
    if (removal > 0) {
      removeStr = " " + (removal == 1 ? "once" : (removal == 2 ? "twice" : "${removal} times")) + " removed";
    }

    return KinshipResult(
      term: "$ordinal Cousin$removeStr",
      degreeOfConsanguinity: degree,
      description: "Collateral relation with common lineage at generation degree $cousinDegree.",
    );
  }

  Member? _getMember(int id) {
    for (var m in members) {
      if (m.id == id) return m;
    }
    return null;
  }
}
