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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MemberEntity
import com.example.ui.MainViewModel
import com.example.ui.Screen
import com.example.ui.theme.ForestGreen

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

    var activeTab by remember { mutableStateOf(0) } // 0 = BIO, 1 = STORIES, 2 = DOCUMENTS

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
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(
                                if (currentMember.gender == "Male") Color(0xFFDCEAF5)
                                else if (currentMember.gender == "Female") Color(0xFFFBE4E5)
                                else Color(0xFFE2F0D9)
                            )
                    ) {
                        val initial = currentMember.firstName.take(1).uppercase()
                        Text(
                            text = initial,
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (currentMember.gender == "Male") Color(0xFF335C7A)
                            else if (currentMember.gender == "Female") Color(0xFF7A3D41)
                            else ForestGreen
                        )
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

            // Tabs for Bio, Stories, Documents
            TabRow(selectedTabIndex = activeTab) {
                Tab(selected = activeTab == 0, onClick = { activeTab = 0 }) {
                    Text("About", modifier = Modifier.padding(12.dp))
                }
                Tab(selected = activeTab == 1, onClick = { activeTab = 1 }) {
                    Text("Stories (${stories.size})", modifier = Modifier.padding(12.dp))
                }
                Tab(selected = activeTab == 2, onClick = { activeTab = 2 }) {
                    Text("Documents (${documents.size})", modifier = Modifier.padding(12.dp))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tab Content
            when (activeTab) {
                0 -> {
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
                }
                1 -> {
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
                2 -> {
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
