package com.example.ui.screens

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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MemberEntity
import com.example.ui.MainViewModel
import com.example.ui.Screen
import com.example.ui.theme.ForestGreen

@Composable
fun TreeViewScreen(viewModel: MainViewModel, modifier: Modifier = Modifier) {
    val members by viewModel.filteredMembers.collectAsState()
    val relationships by viewModel.relationships.collectAsState()
    val activeTree by viewModel.activeTree.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedGenFilter by viewModel.generationFilter.collectAsState()

    var showFilters by remember { mutableStateOf(false) }

    // Group members by generation
    val generationGroups = remember(members) {
        members.groupBy { it.generation }.toSortedMap()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Column(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(bottom = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = activeTree?.name ?: "Family Tree",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.testTag("tree_title")
                        )
                        Text(
                            text = "Interactive Gen-Graph: 7 Generations Support",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row {
                        IconButton(
                            onClick = { showFilters = !showFilters },
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = if (showFilters) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                            )
                        ) {
                            Icon(Icons.Default.FilterList, contentDescription = "Toggle Filters")
                        }
                        IconButton(onClick = { viewModel.navigateTo(Screen.Home) }) {
                            Icon(Icons.Default.Home, contentDescription = "Go Home")
                        }
                    }
                }

                // Smooth search and generation filters
                AnimatedVisibility(visible = showFilters) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.updateSearchQuery(it) },
                            placeholder = { Text("Search ancestor by name...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("search_bar_input"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(text = "Quick Generation Filter", style = MaterialTheme.typography.labelMedium)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = selectedGenFilter == null,
                                onClick = { viewModel.updateGenerationFilter(null) },
                                label = { Text("Display All") }
                            )
                            (1..7).forEach { gen ->
                                FilterChip(
                                    selected = selectedGenFilter == gen,
                                    onClick = { viewModel.updateGenerationFilter(gen) },
                                    label = { Text("Gen $gen") }
                                )
                            }
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    viewModel.selectMember(null)
                    viewModel.navigateTo(Screen.EditMember)
                },
                modifier = Modifier.testTag("add_ancestor_fab")
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = "Add Family Member")
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Elegant Concentric Dashed Watermark (Geometric Balance Theme)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawBehind {
                        val strokeWidth = 1.dp.toPx()
                        val circleColor = Color(0xFF6750A4).copy(alpha = 0.07f)
                        val intervals = floatArrayOf(25f, 20f)
                        val pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(intervals, 0f)
                        
                        drawCircle(
                            color = circleColor,
                            radius = 160.dp.toPx(),
                            center = center,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(
                                width = strokeWidth,
                                pathEffect = pathEffect
                            )
                        )
                        drawCircle(
                            color = circleColor,
                            radius = 280.dp.toPx(),
                            center = center,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(
                                width = strokeWidth,
                                pathEffect = pathEffect
                            )
                        )
                        drawCircle(
                            color = circleColor,
                            radius = 420.dp.toPx(),
                            center = center,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(
                                width = strokeWidth,
                                pathEffect = pathEffect
                            )
                        )
                    }
            )

            if (members.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.AccountTree,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Build Your Family Tree",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Click the '+' button below to introduce your primary ancestor.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                // Multi-Generation Tree horizontal/vertical scroll container
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .horizontalScroll(rememberScrollState())
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp)
                ) {
                    val lineColor = MaterialTheme.colorScheme.outlineVariant
                    
                    // Root row that aligns generations in sequence
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .drawBehind {
                                // Draw elegant forest pathways connecting generations together
                                // We can connect column-to-column conceptually
                            },
                        horizontalArrangement = Arrangement.spacedBy(80.dp)
                    ) {
                        generationGroups.forEach { (genLevel, genMembers) ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(32.dp),
                                modifier = Modifier.width(220.dp)
                            ) {
                                // Generation Header Badge
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(16.dp),
                                    tonalElevation = 4.dp,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                ) {
                                    Text(
                                        text = getGenerationTitle(genLevel),
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                    )
                                }

                                genMembers.forEach { member ->
                                    // Calculate spouse names to show couples easily on same node block
                                    val spName = remember(member, relationships, members) {
                                        relationships.find { r ->
                                            r.relationshipType == "SPOUSE_OF" && (r.memberId == member.id || r.relatedMemberId == member.id)
                                        }?.let { rel ->
                                            val spouseId = if (rel.memberId == member.id) rel.relatedMemberId else rel.memberId
                                            members.find { it.id == spouseId }?.let { "${it.firstName} ${it.lastName}" }
                                        }
                                    }

                                    FamilyNodeCard(
                                        member = member,
                                        spouseName = spName,
                                        onSelected = {
                                            viewModel.selectMember(member)
                                            viewModel.navigateTo(Screen.ProfileDetails)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FamilyNodeCard(
    member: MemberEntity,
    spouseName: String?,
    onSelected: () -> Unit
) {
    Card(
        onClick = onSelected,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (member.isDeceased) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = BorderStroke(
            width = 2.dp,
            color = if (member.gender == "Male") Color(0xFF6750A4) else if (member.gender == "Female") Color(0xFF7D5260) else Color(0xFF625B71)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("node_${member.firstName}_${member.lastName}")
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Initial Circle Avatar (Geometric Balance spec)
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                        .background(
                            if (member.gender == "Male") Color(0xFFEADDFF)
                            else if (member.gender == "Female") Color(0xFFFFD8E4)
                            else Color(0xFFE8DEF8)
                        )
                ) {
                    val initial = member.firstName.take(1).uppercase()
                    Text(
                        text = initial,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (member.gender == "Male") Color(0xFF21005D)
                        else if (member.gender == "Female") Color(0xFF31111D)
                        else Color(0xFF1D192B)
                    )
                }

                Column {
                    Text(
                        text = "${member.firstName} ${member.lastName}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${member.birthDate.split("-").firstOrNull() ?: ""} - ${
                            if (member.isDeceased) member.deathDate?.split("-")?.firstOrNull() ?: "Deceased" else "Living"
                        }",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (!member.occupation.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.CardTravel,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = member.occupation,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }

            if (spouseName != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Divider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.Favorite,
                        contentDescription = "Spouse",
                        tint = Color(0xFFD62828),
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = spouseName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

fun getGenerationTitle(gen: Int): String {
    return when (gen) {
        1 -> "Gen I: Ancestors"
        2 -> "Gen II: Grandparents"
        3 -> "Gen III: Parents"
        4 -> "Gen IV: Self & Spouse"
        5 -> "Gen V: Children"
        6 -> "Gen VI: Grandchildren"
        7 -> "Gen VII: Lineage"
        else -> "Gen $gen"
    }
}
