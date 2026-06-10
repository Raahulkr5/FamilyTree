package com.example.data.utils

import com.example.data.model.MemberEntity
import com.example.data.model.RelationshipEntity
import kotlin.math.min

data class KinshipResult(
    val term: String,
    val degreeOfConsanguinity: Int,
    val description: String
)

class KinshipCalculator(
    private val members: List<MemberEntity>,
    private val relationships: List<RelationshipEntity>
) {
    private val parentsMap = mutableMapOf<Int, MutableList<Int>>()
    private val childrenMap = mutableMapOf<Int, MutableList<Int>>()
    private val spousesMap = mutableMapOf<Int, Int>()

    init {
        for (rel in relationships) {
            val fromId = rel.memberId
            val toId = rel.relatedMemberId
            if (rel.relationshipType == "PARENT_OF") {
                // fromId is PARENT of toId (toId is child of fromId)
                parentsMap.getOrPut(toId) { mutableListOf() }.add(fromId)
                childrenMap.getOrPut(fromId) { mutableListOf() }.add(toId)
            } else if (rel.relationshipType == "SPOUSE_OF") {
                spousesMap[fromId] = toId
                spousesMap[toId] = fromId
            }
        }
    }

    fun calculateRelationship(fromMemberId: Int, toMemberId: Int): KinshipResult {
        if (fromMemberId == toMemberId) {
            return KinshipResult(
                term = "Self",
                degreeOfConsanguinity = 0,
                description = "You are looking at the same family member profile."
            )
        }

        // 1. Direct Spouse Check
        if (spousesMap[fromMemberId] == toMemberId) {
            val toMember = getMember(toMemberId)
            val term = when (toMember?.gender) {
                "Male" -> "Husband"
                "Female" -> "Wife"
                else -> "Spouse"
            }
            return KinshipResult(
                term = term,
                degreeOfConsanguinity = 1,
                description = "Partner joined in matrimonial union."
            )
        }

        // 2. Build Ancestor Path maps via BFS to retrieve distance to all ancestors
        val pathFromA = getAncestorDistances(fromMemberId)
        val pathFromB = getAncestorDistances(toMemberId)

        // Find all common ancestors
        val commonAncestors = pathFromA.keys.intersect(pathFromB.keys)

        val toMember = getMember(toMemberId)
        val toGender = toMember?.gender ?: "Other"

        if (commonAncestors.isNotEmpty()) {
            // Find the Closest Common Ancestor (CCA) that minimizes pathFromA[ancestor] + pathFromB[ancestor]
            var bestCCA: Int? = null
            var minCombinedDistance = Int.MAX_VALUE

            for (cca in commonAncestors) {
                val distA = pathFromA[cca] ?: continue
                val distB = pathFromB[cca] ?: continue
                val combined = distA + distB
                if (combined < minCombinedDistance) {
                    minCombinedDistance = combined
                    bestCCA = cca
                }
            }

            if (bestCCA != null) {
                val dA = pathFromA[bestCCA]!!
                val dB = pathFromB[bestCCA]!!
                return getTermFromDistances(dA, dB, toGender, minCombinedDistance)
            }
        }

        // 3. Check for Affinal Relations (through spouses: e.g. Mother-in-law, Sibling-in-law)
        val spouseOfA = spousesMap[fromMemberId]
        if (spouseOfA != null) {
            val spouseRelation = calculateRelationship(spouseOfA, toMemberId)
            if (spouseRelation.term != "Distant Relative" && spouseRelation.term != "Self") {
                return KinshipResult(
                    term = "${spouseRelation.term}-in-law",
                    degreeOfConsanguinity = spouseRelation.degreeOfConsanguinity + 1,
                    description = "Affinal relative linked through marriage with your spouse."
                )
            }
        }

        val spouseOfB = spousesMap[toMemberId]
        if (spouseOfB != null) {
            val ownRelationToSpouseOfB = calculateRelationship(fromMemberId, spouseOfB)
            if (ownRelationToSpouseOfB.term != "Distant Relative" && ownRelationToSpouseOfB.term != "Self") {
                return KinshipResult(
                    term = "${ownRelationToSpouseOfB.term}'s Spouse",
                    degreeOfConsanguinity = ownRelationToSpouseOfB.degreeOfConsanguinity + 1,
                    description = "Matrimonial partner of your core blood relative."
                )
            }
        }

        return KinshipResult(
            term = "Distant Relative",
            degreeOfConsanguinity = 9,
            description = "No direct lineage overlap or immediate marriage connection found in this repository."
        )
    }

    private fun getAncestorDistances(startId: Int): Map<Int, Int> {
        val distances = mutableMapOf<Int, Int>()
        distances[startId] = 0
        val queue = mutableListOf(startId)

        while (queue.isNotEmpty()) {
            val current = queue.removeAt(0)
            val currentDist = distances[current] ?: 0

            val parents = parentsMap[current] ?: emptyList()
            for (p in parents) {
                if (p !in distances) {
                    distances[p] = currentDist + 1
                    queue.add(p)
                }
            }
        }
        return distances
    }

    private fun getTermFromDistances(dA: Int, dB: Int, gender: String, degree: Int): KinshipResult {
        if (dA == 0) {
            // B is direct ancestor of A
            return when (dB) {
                1 -> KinshipResult(
                    term = if (gender == "Male") "Father" else if (gender == "Female") "Mother" else "Parent",
                    degreeOfConsanguinity = degree,
                    description = "Immediate direct generation antecedent."
                )
                2 -> KinshipResult(
                    term = if (gender == "Male") "Grandfather" else if (gender == "Female") "Grandmother" else "Grandparent",
                    degreeOfConsanguinity = degree,
                    description = "Generational antecedent twice removed."
                )
                else -> {
                    val prefix = "Great-".repeat(dB - 2)
                    KinshipResult(
                        term = "$prefix${if (gender == "Male") "Grandfather" else if (gender == "Female") "Grandmother" else "Grandparent"}",
                        degreeOfConsanguinity = degree,
                        description = "Historical direct lineage ancestor."
                    )
                }
            }
        }

        if (dB == 0) {
            // B is direct descendant of A
            return when (dA) {
                1 -> KinshipResult(
                    term = if (gender == "Male") "Son" else if (gender == "Female") "Daughter" else "Child",
                    degreeOfConsanguinity = degree,
                    description = "Direct generation successor."
                )
                2 -> KinshipResult(
                    term = if (gender == "Male") "Grandson" else if (gender == "Female") "Granddaughter" else "Grandchild",
                    degreeOfConsanguinity = degree,
                    description = "Generational successor twice removed."
                )
                else -> {
                    val prefix = "Great-".repeat(dA - 2)
                    KinshipResult(
                        term = "$prefix${if (gender == "Male") "Grandson" else if (gender == "Female") "Granddaughter" else "Grandchild"}",
                        degreeOfConsanguinity = degree,
                        description = "Historical direct descendancy partner."
                    )
                }
            }
        }

        if (dA == 1) {
            if (dB == 1) {
                return KinshipResult(
                    term = if (gender == "Male") "Brother" else if (gender == "Female") "Sister" else "Sibling",
                    degreeOfConsanguinity = degree,
                    description = "Immediate sibling sharing common parentage."
                )
            } else if (dB == 2) {
                return KinshipResult(
                    term = if (gender == "Male") "Uncle" else if (gender == "Female") "Aunt" else "Aunt/Uncle",
                    degreeOfConsanguinity = degree,
                    description = "Sibling of your direct parent."
                )
            } else {
                val prefix = "Great-".repeat(dB - 2)
                return KinshipResult(
                    term = "$prefix${if (gender == "Male") "Uncle" else if (gender == "Female") "Aunt" else "Aunt/Uncle"}",
                    degreeOfConsanguinity = degree,
                    description = "Generational sibling of your historical grandparents."
                )
            }
        }

        if (dB == 1) {
            if (dA == 2) {
                return KinshipResult(
                    term = if (gender == "Male") "Nephew" else if (gender == "Female") "Niece" else "Niece/Nephew",
                    degreeOfConsanguinity = degree,
                    description = "Offspring of your direct sibling."
                )
            } else {
                val prefix = "Great-".repeat(dA - 2)
                return KinshipResult(
                    term = "$prefix${if (gender == "Male") "Nephew" else if (gender == "Female") "Niece" else "Niece/Nephew"}",
                    degreeOfConsanguinity = degree,
                    description = "Offspring of your historical nephew/niece lineage."
                )
            }
        }

        // Cousins
        val minD = min(dA, dB)
        val maxD = maxOf(dA, dB)
        val cousinDegree = minD - 1
        val removal = maxD - minD

        val ordinal = when (cousinDegree) {
            1 -> "First"
            2 -> "Second"
            3 -> "Third"
            else -> "${cousinDegree}th"
        }

        var removeStr = ""
        if (removal > 0) {
            removeStr = " " + when (removal) {
                1 -> "once"
                2 -> "twice"
                else -> "${removal} times"
            } + " removed"
        }

        return KinshipResult(
            term = "$ordinal Cousin$removeStr",
            degreeOfConsanguinity = degree,
            description = "Collateral relation with common lineage at generation degree $cousinDegree."
        )
    }

    private fun getMember(id: Int): MemberEntity? {
        return members.find { it.id == id }
    }
}
