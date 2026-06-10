@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.ui.screens

import androidx.compose.material3.ExperimentalMaterial3Api

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TreeEntity
import com.example.ui.MainViewModel
import com.example.ui.Screen
import com.example.ui.theme.ForestGreen

@Composable
fun HomeScreen(viewModel: MainViewModel, modifier: Modifier = Modifier) {
    val activeUser by viewModel.activeUser.collectAsState()
    val trees by viewModel.trees.collectAsState()
    val activeTree by viewModel.activeTree.collectAsState()
    val members by viewModel.members.collectAsState()
    val stories by viewModel.allStories.collectAsState()
    val docs by viewModel.allDocuments.collectAsState()

    var showCreateTreeDialog by remember { mutableStateOf(false) }
    var newTreeName by remember { mutableStateOf("") }
    var newTreeDesc by remember { mutableStateOf("") }

    var expandedTreeMenu by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.AccountTree,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Family Tree",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                },
                actions = {
                    // Tree Selection Dropdown
                    Box {
                        TextButton(
                            onClick = { expandedTreeMenu = true },
                            modifier = Modifier.testTag("switch_tree_dropdown")
                        ) {
                            Text(
                                activeTree?.name ?: "Select Tree",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }

                        DropdownMenu(
                            expanded = expandedTreeMenu,
                            onDismissRequest = { expandedTreeMenu = false }
                        ) {
                            trees.forEach { tree ->
                                DropdownMenuItem(
                                    text = { Text(tree.name) },
                                    onClick = {
                                        viewModel.selectTree(tree)
                                        expandedTreeMenu = false
                                    }
                                )
                            }
                            Divider()
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Create New Tree")
                                    }
                                },
                                onClick = {
                                    showCreateTreeDialog = true
                                    expandedTreeMenu = false
                                }
                            )
                        }
                    }

                    IconButton(onClick = { viewModel.logout() }, modifier = Modifier.testTag("logout_button")) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Log Out")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
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
            // Welcome Header
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Greetings, ${activeUser?.fullName ?: "Archivist"}!",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "You are currently managing: '${activeTree?.name ?: "No Tree Selected"}'.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }

            // Quick Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Ancestors",
                    count = members.size.toString(),
                    icon = Icons.Default.People,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Generations",
                    count = (members.maxByOrNull { it.generation }?.generation ?: 0).toString(),
                    icon = Icons.Default.Layers,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Our Stories",
                    count = stories.size.toString(),
                    icon = Icons.Default.MenuBook,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Documents",
                    count = docs.size.toString(),
                    icon = Icons.Default.Description,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Navigation Actions Grid
            Text(
                text = "Ancestral Explorer",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                HubActionCard(
                    title = "Interactive Tree",
                    description = "Visual Gen-Chart",
                    icon = Icons.Default.AccountTree,
                    color = MaterialTheme.colorScheme.primary,
                    onClick = { viewModel.navigateTo(Screen.TreeView) },
                    modifier = Modifier.weight(1f).testTag("goto_tree_card")
                )

                HubActionCard(
                    title = "AI Gen-Copilot",
                    description = "Gemini assistance",
                    icon = Icons.Default.AutoAwesome,
                    color = ForestGreen,
                    onClick = { viewModel.navigateTo(Screen.AiAssistant) },
                    modifier = Modifier.weight(1f).testTag("goto_ai_card")
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Celebrations & Notifications Reminders Box
            Card(
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Celebration,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Upcoming Reminders",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Badge(containerColor = MaterialTheme.colorScheme.error) {
                            Text("Live")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val upcomingReminders = remember(members) {
                        buildUpcomingReminders(members)
                    }

                    if (upcomingReminders.isEmpty()) {
                        Text(
                            text = "No upcoming birthday or anniversary milestones found this season.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            upcomingReminders.forEach { reminder ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        if (reminder.type == "Birthday") Icons.Default.Cake else Icons.Default.Favorite,
                                        contentDescription = null,
                                        tint = if (reminder.type == "Birthday") Color(0xFFC38A4B) else Color(0xFFC15C5E),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = reminder.title,
                                            style = MaterialTheme.typography.labelLarge,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = reminder.sub,
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

            // Delete Tree Admin button safely positioned
            Spacer(modifier = Modifier.height(40.dp))
            OutlinedButton(
                onClick = { viewModel.deleteActiveTree() },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("delete_tree_btn")
            ) {
                Icon(Icons.Default.DeleteForever, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Archive / Terminate Selected Tree")
            }
        }

        // --- Dialog to Create Family Tree ---
        if (showCreateTreeDialog) {
            AlertDialog(
                onDismissRequest = { showCreateTreeDialog = false },
                title = { Text("Assemble New Heritage Tree") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = newTreeName,
                            onValueChange = { newTreeName = it },
                            label = { Text("Tree Name") },
                            placeholder = { Text("e.g., Pendragon Lineage") },
                            modifier = Modifier.fillMaxWidth().testTag("dialog_tree_name_input"),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = newTreeDesc,
                            onValueChange = { newTreeDesc = it },
                            label = { Text("Description") },
                            placeholder = { Text("e.g., Chronicling roots from Somerset.") },
                            modifier = Modifier.fillMaxWidth().testTag("dialog_tree_desc_input")
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newTreeName.isNotBlank()) {
                                viewModel.createTree(newTreeName, newTreeDesc)
                                newTreeName = ""
                                newTreeDesc = ""
                                showCreateTreeDialog = false
                            }
                        },
                        modifier = Modifier.testTag("dialog_tree_confirm_btn")
                    ) {
                        Text("Create & Seed Sandbox")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCreateTreeDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun StatCard(
    title: String,
    count: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = count,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun HubActionCard(
    title: String,
    description: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f)),
        border = BorderStroke(1.dp, color.copy(alpha = 0.2f)),
        modifier = modifier.height(115.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(28.dp)
            )

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = color
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// Helpers for Reminders
data class FamilyReminder(val title: String, val sub: String, val type: String)

fun buildUpcomingReminders(members: List<com.example.data.model.MemberEntity>): List<FamilyReminder> {
    val reminders = mutableListOf<FamilyReminder>()
    // Process birthdays & simulated marriage cycles
    members.forEach { m ->
        if (!m.isDeceased) {
            val parts = m.birthDate.split("-")
            val birthMonth = parts.getOrNull(1)?.toIntOrNull()
            val birthDay = parts.getOrNull(2)?.toIntOrNull()
            if (birthMonth != null) {
                val monthName = getMonthName(birthMonth)
                reminders.add(
                    FamilyReminder(
                        title = "${m.firstName} ${m.lastName} Anniversary of Birth",
                        sub = "Celebration on $monthName ${birthDay ?: ""}",
                        type = "Birthday"
                    )
                )
            }
        }
    }
    // Return at most the first 3 elegant items
    return reminders.take(3)
}

fun getMonthName(m: Int): String {
    return when (m) {
        1 -> "January"
        2 -> "February"
        3 -> "March"
        4 -> "April"
        5 -> "May"
        6 -> "June"
        7 -> "July"
        8 -> "August"
        9 -> "September"
        10 -> "October"
        11 -> "November"
        12 -> "December"
        else -> "Month"
    }
}
