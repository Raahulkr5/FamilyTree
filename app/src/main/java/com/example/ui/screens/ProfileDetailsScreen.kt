@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.ui.screens

import androidx.compose.material3.ExperimentalMaterial3Api

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.MemberEntity
import com.example.data.utils.KinshipCalculator
import com.example.data.utils.KinshipResult
import com.example.ui.MainViewModel
import com.example.ui.Screen
import com.example.ui.theme.ForestGreen

data class TimelineEvent(
    val year: Int,
    val dateDisplay: String,
    val title: String,
    val content: String,
    val category: String, // "Birth", "Milestone", "Story", "Achievement", "Passing"
    val location: String? = null
)

fun extractYear(dateStr: String?): Int {
    if (dateStr.isNullOrBlank()) return 1900
    val match = Regex("\\b\\d{4}\\b").find(dateStr)
    if (match != null) {
        return match.value.toIntOrNull() ?: 1900
    }
    val tokens = dateStr.split(' ', '-', '/')
    for (t in tokens) {
        val parsed = t.toIntOrNull()
        if (parsed != null && parsed in 1000..3000) {
            return parsed
        }
    }
    return 1900
}

@Composable
fun VerticalTimelineComponent(
    events: List<TimelineEvent>,
    birthDate: String
) {
    if (events.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No timeline events recorded yet. Create some stories or achievements to build the path!",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        events.forEachIndexed { index, event ->
            val nodeColor = when (event.category) {
                "Birth" -> Color(0xFF00AA90) // Teal
                "Passing" -> Color(0xFFE05252) // Red/Caring
                "Achievement" -> Color(0xFFD4AF37) // Golden Orchid
                "Milestone" -> Color(0xFFBD00FF) // Cyber Neon Orchid / Violet
                else -> MaterialTheme.colorScheme.primary // Legacy Indigo
            }

            val iconVec = when (event.category) {
                "Birth" -> Icons.Default.Cake
                "Passing" -> Icons.Default.Favorite
                "Achievement" -> Icons.Default.EmojiEvents
                "Milestone" -> Icons.Default.Star
                else -> Icons.Default.Book
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Time / Year Display column (Left Side)
                Column(
                    modifier = Modifier
                        .width(72.dp)
                        .padding(top = 8.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = event.year.toString(),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    val birthYr = extractYear(birthDate)
                    val age = event.year - birthYr
                    if (age >= 0 && birthYr > 1900) {
                        Text(
                            text = if (age == 0) "Newborn" else "Age $age",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.secondary
                        )
                    } else {
                        Text(
                            text = event.dateDisplay,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Timeline node circle with connecting vertical connector line
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(36.dp)
                ) {

                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(nodeColor.copy(alpha = 0.15f))
                            .border(1.5.dp, nodeColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = iconVec,
                            contentDescription = event.category,
                            tint = nodeColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    if (index < events.size - 1) {
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .height(90.dp)
                                .background(
                                    brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                        colors = listOf(nodeColor, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                    )
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Info / Event card (Right Side)
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .padding(bottom = 12.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                    ),
                    border = BorderStroke(
                        width = 0.8.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = event.title,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            
                            Surface(
                                color = nodeColor.copy(alpha = 0.15f),
                                contentColor = nodeColor,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = event.category,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = event.content,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp
                        )

                        if (!event.location.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Start
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = "Location",
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = event.location,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileDetailsScreen(viewModel: MainViewModel, modifier: Modifier = Modifier) {
    val member by viewModel.activeMember.collectAsState()
    val allMembers by viewModel.members.collectAsState()
    val relationships by viewModel.relationships.collectAsState()

    val stories by viewModel.activeMemberStories.collectAsState()
    val documents by viewModel.activeMemberDocuments.collectAsState()

    val bioDraft by viewModel.bioDraft.collectAsState()
    val aiSuggestions by viewModel.aiSuggestions.collectAsState()
    val isAiRunning by viewModel.isAiRunning.collectAsState()

    var activeTab by remember { mutableStateOf(0) } // 0 = BIO, 1 = TIMELINE, 2 = STORIES, 3 = DOCUMENTS

    var showAddStoryDialog by remember { mutableStateOf(false) }
    var storyTitle by remember { mutableStateOf("") }
    var storyCategory by remember { mutableStateOf("Story") }
    var storyContent by remember { mutableStateOf("") }
    var storyDate by remember { mutableStateOf("") }

    var showAddDocDialog by remember { mutableStateOf(false) }
    var docTitle by remember { mutableStateOf("") }
    var docType by remember { mutableStateOf("Certificate") }
    var docDesc by remember { mutableStateOf("") }
    var docUri by remember { mutableStateOf("") }

    var showRelDialog by remember { mutableStateOf(false) }
    var relPartnerId by remember { mutableStateOf<Int?>(null) }
    var relType by remember { mutableStateOf("PARENT_OF") }

    var selectKinshipMemberId by remember { mutableStateOf<Int?>(null) }
    var kinshipDropdownExpanded by remember { mutableStateOf(false) }

    if (member == null) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator()
        }
        return
    }

    val currentMember = member!!

    // Find custom calculated relationships
    val mappedRels = remember(currentMember, relationships, allMembers) {
        val relList = mutableListOf<Pair<String, MemberEntity>>()
        relationships.forEach { r ->
            if (r.memberId == currentMember.id) {
                allMembers.find { it.id == r.relatedMemberId }?.let { other ->
                    val relLabel = if (r.relationshipType == "PARENT_OF") "Child" else "Partner / Spouse"
                    relList.add(relLabel to other)
                }
            } else if (r.relatedMemberId == currentMember.id) {
                allMembers.find { it.id == r.memberId }?.let { other ->
                    val relLabel = if (r.relationshipType == "PARENT_OF") "Mother / Father" else "Partner / Spouse"
                    relList.add(relLabel to other)
                }
            }
        }
        relList
    }

    // Dynamic, sorted chronological vertical legacy timeline events
    val timelineEvents = remember(currentMember, stories) {
        val list = mutableListOf<TimelineEvent>()
        
        // 1. Birth Event
        if (currentMember.birthDate.isNotBlank()) {
            val birthYear = extractYear(currentMember.birthDate)
            list.add(
                TimelineEvent(
                    year = birthYear,
                    dateDisplay = currentMember.birthDate,
                    title = "Birth of ${currentMember.firstName}",
                    content = "Welcomed into the lineage at ${currentMember.birthLocation ?: "Unknown location"}.",
                    category = "Birth",
                    location = currentMember.birthLocation
                )
            )
        }
        
        // 2. Custom Milestones, Stories, Achievements
        stories.forEach { story ->
            val eventDate = story.dateOccurred ?: ""
            val birthYear = extractYear(currentMember.birthDate)
            val eventYear = if (eventDate.isNotBlank()) extractYear(eventDate) else (birthYear + 18)
            list.add(
                TimelineEvent(
                    year = eventYear,
                    dateDisplay = if (eventDate.isNotBlank()) eventDate else "Milestone Year",
                    title = story.title,
                    content = story.content,
                    category = story.category,
                    location = null
                )
            )
        }
        
        // 3. Passing / Resting Event (if deceased)
        if (currentMember.isDeceased) {
            val deathDateStr = currentMember.deathDate ?: ""
            val birthYear = extractYear(currentMember.birthDate)
            val deathYear = if (deathDateStr.isNotBlank()) extractYear(deathDateStr) else (birthYear + 75)
            list.add(
                TimelineEvent(
                    year = deathYear,
                    dateDisplay = if (deathDateStr.isNotBlank()) deathDateStr else "Peaceful Passing",
                    title = "Silent Legacy Transition",
                    content = "Passed away, resting at ${currentMember.deathLocation ?: "Unknown Location"}.",
                    category = "Passing",
                    location = currentMember.deathLocation
                )
            )
        }
        
        list.sortedWith(compareBy({ it.year }, { it.title }))
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("${currentMember.firstName}'s Legacy Details") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateTo(Screen.TreeView) }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.selectMember(currentMember)
                            viewModel.navigateTo(Screen.EditMember)
                        },
                        modifier = Modifier.testTag("edit_profile_btn")
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Profile")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Header Profile Card
            Card(
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(86.dp)
                            .clip(CircleShape)
                            .background(
                                if (currentMember.gender == "Male") Color(0xFFDCEAF5)
                                else if (currentMember.gender == "Female") Color(0xFFFBE4E5)
                                else Color(0xFFE2F0D9)
                            )
                            .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), CircleShape)
                    ) {
                        val hasCustomPhoto = !currentMember.photoUri.isNullOrBlank() && (
                                    currentMember.photoUri.startsWith("content://") || 
                                    currentMember.photoUri.startsWith("file://") || 
                                    currentMember.photoUri.startsWith("http") || 
                                    currentMember.photoUri.contains("/")
                                )
                        if (hasCustomPhoto) {
                            AsyncImage(
                                model = currentMember.photoUri,
                                contentDescription = "Profile Photo of ${currentMember.firstName}",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            val initial = currentMember.firstName.take(1).uppercase()
                            Text(
                                text = initial,
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (currentMember.gender == "Male") Color(0xFF335C7A)
                                else if (currentMember.gender == "Female") Color(0xFF7A3D41)
                                else ForestGreen
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "${currentMember.firstName} ${currentMember.lastName}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.testTag("profile_full_name")
                    )

                    Text(
                        text = "Generation ${currentMember.generation} Ancestor",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Vital lifespan
                    Text(
                        text = "Lifespan: ${currentMember.birthDate}  ➔  ${if (currentMember.isDeceased) currentMember.deathDate ?: "Deceased" else "Present / Living"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )

                    if (!currentMember.birthLocation.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Root Location: ${currentMember.birthLocation}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (!currentMember.occupation.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "Profession: ${currentMember.occupation}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // -- MAPPED RELATIONSHIPS PANEL --
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Connections Graph",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        IconButton(
                            onClick = { showRelDialog = true },
                            modifier = Modifier.testTag("link_relationship_btn")
                        ) {
                            Icon(Icons.Default.AddLink, contentDescription = "Add relationship edge")
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (mappedRels.isEmpty()) {
                        Text(
                            "No family links mapped yet. Select '+' to declare marriages or kids.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        // Horizontal scroll listing relationship cards
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                        ) {
                            mappedRels.forEach { (label, other) ->
                                Card(
                                    onClick = { viewModel.selectMember(other) },
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                    modifier = Modifier.width(160.dp)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.secondary,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "${other.firstName} ${other.lastName}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = "Born ${other.birthDate.split("-").firstOrNull() ?: ""}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // AI Features Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { viewModel.draftBioForActiveMember() },
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                    modifier = Modifier.weight(1f).testTag("ai_draft_bio_btn")
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("AI Legacy Bio")
                }

                Button(
                    onClick = { viewModel.analyzeTreeAnomalies() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    modifier = Modifier.weight(1f).testTag("ai_anomalies_btn")
                ) {
                    Icon(Icons.Default.QueryStats, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("AI Suggestions")
                }
            }

            // -- Display AI Loading or outcome --
            if (isAiRunning) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("AI Genealogist is thinking...", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            if (bioDraft != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = ForestGreen)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Drafted Heritage Biography", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(bioDraft!!, style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                            TextButton(onClick = { viewModel.draftBioForActiveMember() }) { Text("Regenerate") }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(onClick = { viewModel.saveDraftedBio() }) { Text("Save to Record") }
                        }
                    }
                }
            }

            if (aiSuggestions != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lightbulb, contentDescription = null, tint = Color(0xFFC38B23))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Genealogy Research Prompts", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(aiSuggestions!!, style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                            TextButton(onClick = { viewModel.selectMember(currentMember) }) { Text("Close") }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Tabs for Bio, Timeline, Stories, Documents
            TabRow(selectedTabIndex = activeTab) {
                Tab(selected = activeTab == 0, onClick = { activeTab = 0 }) {
                    Text("About", modifier = Modifier.padding(12.dp))
                }
                Tab(selected = activeTab == 1, onClick = { activeTab = 1 }) {
                    Text("Timeline", modifier = Modifier.padding(12.dp))
                }
                Tab(selected = activeTab == 2, onClick = { activeTab = 2 }) {
                    Text("Stories (${stories.size})", modifier = Modifier.padding(12.dp))
                }
                Tab(selected = activeTab == 3, onClick = { activeTab = 3 }) {
                    Text("Documents (${documents.size})", modifier = Modifier.padding(12.dp))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tab Content
            when (activeTab) {
                0 -> {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        // BIO / ABOUT Detail Display
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    "Ancestral Biography / Notes",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = currentMember.bio.ifBlank { "No biographical notes recorded for this ancestor yet. Draft one instantly using the AI Legacy Bio button above!" },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 20.sp
                                )
                            }
                        }

                        // INTERACTIVE KINSHIP CALCULATOR CARD (Kotlin counterpart to requested Dart utility)
                        Card(
                            modifier = Modifier.fillMaxWidth().testTag("kinship_calculator_card"),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Hub,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "Kinship Relationship Finder",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = "Determine the exact genealogical linkage, degree of consanguinity, and familial connection between ${currentMember.firstName} and any relative.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                // Dropdown to select second member
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    val otherMembersList = allMembers.filter { it.id != currentMember.id }
                                    val selectedOtherMember = otherMembersList.find { it.id == selectKinshipMemberId }

                                    OutlinedButton(
                                        onClick = { kinshipDropdownExpanded = true },
                                        modifier = Modifier.fillMaxWidth().testTag("select_other_kinship_member"),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Row(
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = if (selectedOtherMember != null) {
                                                    "${selectedOtherMember.firstName} ${selectedOtherMember.lastName} (${selectedOtherMember.gender})"
                                                } else {
                                                    "Select relative to compare..."
                                                },
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                            Icon(
                                                imageVector = Icons.Default.ArrowDropDown,
                                                contentDescription = "Expand comparison"
                                            )
                                        }
                                    }

                                    DropdownMenu(
                                        expanded = kinshipDropdownExpanded,
                                        onDismissRequest = { kinshipDropdownExpanded = false },
                                        modifier = Modifier.fillMaxWidth(0.9f)
                                    ) {
                                        if (otherMembersList.isEmpty()) {
                                            DropdownMenuItem(
                                                text = { Text("No other relatives found in tree") },
                                                onClick = { kinshipDropdownExpanded = false }
                                            )
                                        } else {
                                            otherMembersList.forEach { other ->
                                                DropdownMenuItem(
                                                    text = { Text("${other.firstName} ${other.lastName} (${other.gender})") },
                                                    onClick = {
                                                        selectKinshipMemberId = other.id
                                                        kinshipDropdownExpanded = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }

                                // Interactive Kinship calculation display output
                                if (selectKinshipMemberId != null) {
                                    val otherMember = allMembers.find { it.id == selectKinshipMemberId }
                                    if (otherMember != null) {
                                        val calculator = remember(allMembers, relationships) {
                                            KinshipCalculator(allMembers, relationships)
                                        }
                                        val result = remember(selectKinshipMemberId, currentMember.id, allMembers, relationships) {
                                            calculator.calculateRelationship(currentMember.id, otherMember.id)
                                        }

                                        Spacer(modifier = Modifier.height(16.dp))

                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                                                .border(
                                                    1.dp,
                                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                                    RoundedCornerShape(12.dp)
                                                )
                                                .padding(14.dp)
                                        ) {
                                            Column {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Text(
                                                        text = "Exact Connection Label:",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.secondary,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Surface(
                                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                                        contentColor = MaterialTheme.colorScheme.primary,
                                                        shape = RoundedCornerShape(8.dp)
                                                    ) {
                                                        Text(
                                                            text = "Consanguinity: ${if (result.degreeOfConsanguinity < 9) "Degree ${result.degreeOfConsanguinity}" else "None"}",
                                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                        )
                                                    }
                                                }

                                                Spacer(modifier = Modifier.height(6.dp))

                                                Text(
                                                    text = result.term,
                                                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                                    color = MaterialTheme.colorScheme.primary
                                                )

                                                Spacer(modifier = Modifier.height(6.dp))

                                                Text(
                                                    text = result.description,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )

                                                Spacer(modifier = Modifier.height(10.dp))

                                                Text(
                                                    text = "${currentMember.firstName} is the ${result.term.lowercase()} of ${otherMember.firstName}.",
                                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                                    color = MaterialTheme.colorScheme.secondary
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                1 -> {
                    // CHRONOLOGICAL VERTICAL TIMELINE COMPONENT
                    VerticalTimelineComponent(events = timelineEvents, birthDate = currentMember.birthDate)
                }
                2 -> {
                    // STORIES Grid / Lists
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Family Stories & Milestones", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Button(
                                onClick = { showAddStoryDialog = true },
                                modifier = Modifier.testTag("add_story_btn")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("New Story")
                            }
                        }

                        if (stories.isEmpty()) {
                            Text("No memories or milestones currently documented. Add the first family milestone!")
                        } else {
                            stories.forEach { story ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(story.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                            IconButton(onClick = { viewModel.deleteStory(story) }) {
                                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                            }
                                        }
                                        Text("Category: ${story.category} | ${story.dateOccurred ?: "Undated"}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(story.content, style = MaterialTheme.typography.bodyMedium)
                                    }
                                }
                            }
                        }
                    }
                }
                3 -> {
                    // DOCUMENTS
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Archive Certificates & Records", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Button(
                                onClick = { showAddDocDialog = true },
                                modifier = Modifier.testTag("add_doc_btn")
                            ) {
                                Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Attach Document")
                            }
                        }

                        if (documents.isEmpty()) {
                            Text("No high-resolution certificates, birth registries, or old letters uploaded yet.")
                        } else {
                            documents.forEach { doc ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.FilePresent, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column {
                                                Text(doc.title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                                                Text("Format: ${doc.fileType}", style = MaterialTheme.typography.bodySmall)
                                                if (!doc.description.isNullOrBlank()) {
                                                    Text(doc.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                }
                                            }
                                        }

                                        IconButton(onClick = { viewModel.deleteDocument(doc) }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }

        // Dialog: Link Relationship Screen
        if (showRelDialog) {
            val potentialPartners = allMembers.filter { it.id != currentMember.id }
            AlertDialog(
                onDismissRequest = { showRelDialog = false },
                title = { Text("Trace Genealogical Connection") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Connect '${currentMember.firstName}' to another family member in the repository:")
                        
                        var dropdownExpanded by remember { mutableStateOf(false) }
                        var selectedName by remember { mutableStateOf("Choose Family Member") }

                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = { dropdownExpanded = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(selectedName)
                                Spacer(modifier = Modifier.weight(1f))
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                            DropdownMenu(
                                expanded = dropdownExpanded,
                                onDismissRequest = { dropdownExpanded = false }
                            ) {
                                potentialPartners.forEach { partner ->
                                    DropdownMenuItem(
                                        text = { Text("${partner.firstName} ${partner.lastName} (${partner.gender})") },
                                        onClick = {
                                            relPartnerId = partner.id
                                            selectedName = "${partner.firstName} ${partner.lastName}"
                                            dropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Text("Connection Type Link:")
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = relType == "PARENT_OF", onClick = { relType = "PARENT_OF" })
                                Text("Is Parent of Target")
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = relType == "SPOUSE_OF", onClick = { relType = "SPOUSE_OF" })
                                Text("Is Spouse of Target")
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (relPartnerId != null) {
                                viewModel.createRelationship(relPartnerId!!, relType)
                                showRelDialog = false
                            }
                        }
                    ) {
                        Text("Create Edge Link")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showRelDialog = false }) { Text("Cancel") }
                }
            )
        }

        // Dialog: Add Story
        if (showAddStoryDialog) {
            AlertDialog(
                onDismissRequest = { showAddStoryDialog = false },
                title = { Text("Log Legacy Memory / Milestone") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(storyTitle, { storyTitle = it }, label = { Text("Heading Title") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(storyDate, { storyDate = it }, label = { Text("Date Occurred (YYYY-MM-DD)") }, modifier = Modifier.fillMaxWidth())
                        
                        Text("Category:")
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("Story", "Milestone", "Achievement").forEach { cat ->
                                FilterChip(
                                    selected = storyCategory == cat,
                                    onClick = { storyCategory = cat },
                                    label = { Text(cat) }
                                )
                            }
                        }

                        OutlinedTextField(
                            value = storyContent,
                            onValueChange = { storyContent = it },
                            label = { Text("Anecdote Story Context") },
                            modifier = Modifier.fillMaxWidth().height(100.dp)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (storyTitle.isNotBlank() && storyContent.isNotBlank()) {
                                viewModel.addStory(storyTitle, storyContent, storyCategory, storyDate)
                                storyTitle = ""
                                storyContent = ""
                                showAddStoryDialog = false
                            }
                        }
                    ) { Text("Save Story") }
                },
                dismissButton = {
                    TextButton(onClick = { showAddStoryDialog = false }) { Text("Cancel") }
                }
            )
        }

        // Dialog: Add Doc
        if (showAddDocDialog) {
            AlertDialog(
                onDismissRequest = { showAddDocDialog = false },
                title = { Text("Link Scanned Document / Certificate") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(docTitle, { docTitle = it }, label = { Text("Document Label Name") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(docDesc, { docDesc = it }, label = { Text("Short Description Notes") }, modifier = Modifier.fillMaxWidth())
                        
                        Text("Document Type:")
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf("Certificate", "Letter", "Will", "Photo").forEach { typ ->
                                FilterChip(
                                    selected = docType == typ,
                                    onClick = { docType = typ },
                                    label = { Text(typ) }
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (docTitle.isNotBlank()) {
                                viewModel.addDocument(docTitle, docDesc, docType, "cert_attached_${System.currentTimeMillis()}")
                                docTitle = ""
                                docDesc = ""
                                showAddDocDialog = false
                            }
                        }
                    ) { Text("Attach Document") }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDocDialog = false }) { Text("Cancel") }
                }
            )
        }
    }
}

fun size(dp: Int): Modifier {
    return Modifier.size(dp.dp)
}
