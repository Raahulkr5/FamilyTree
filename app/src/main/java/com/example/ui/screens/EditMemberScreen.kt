@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.ui.screens

import androidx.compose.material3.ExperimentalMaterial3Api

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import android.graphics.Bitmap
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import com.example.ui.MainViewModel
import com.example.ui.Screen

@Composable
fun EditMemberScreen(viewModel: MainViewModel, modifier: Modifier = Modifier) {
    val member by viewModel.activeMember.collectAsState()

    // Screen states pre-seeded if editing
    var firstName by remember { mutableStateOf(member?.firstName ?: "") }
    var lastName by remember { mutableStateOf(member?.lastName ?: "") }
    var gender by remember { mutableStateOf(member?.gender ?: "Male") }
    var birthDate by remember { mutableStateOf(member?.birthDate ?: "1990-01-01") }
    var birthLocation by remember { mutableStateOf(member?.birthLocation ?: "") }
    var isDeceased by remember { mutableStateOf(member?.isDeceased ?: false) }
    var deathDate by remember { mutableStateOf(member?.deathDate ?: "") }
    var deathLocation by remember { mutableStateOf(member?.deathLocation ?: "") }
    var bio by remember { mutableStateOf(member?.bio ?: "") }
    var occupation by remember { mutableStateOf(member?.occupation ?: "") }
    var contactPhone by remember { mutableStateOf(member?.contactPhone ?: "") }
    var contactEmail by remember { mutableStateOf(member?.contactEmail ?: "") }
    var photoUri by remember { mutableStateOf(member?.photoUri ?: "") }
    var generation by remember { mutableStateOf(member?.generation ?: 4) }

    var errorText by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        uri?.let {
            photoUri = it.toString()
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        bitmap?.let {
            val file = File(context.cacheDir, "avatar_${UUID.randomUUID()}.jpg")
            try {
                FileOutputStream(file).use { out ->
                    it.compress(Bitmap.CompressFormat.JPEG, 95, out)
                }
                photoUri = android.net.Uri.fromFile(file).toString()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(if (member == null) "Enroll New Ancestor" else "Refine Ancestral Record") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateTo(if (member == null) Screen.TreeView else Screen.ProfileDetails) }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Go Back")
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title descriptor
            Text(
                "Document biographical specifics, vital lifespan statistics, and generational tiers below to map connections.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (errorText != null) {
                Text(
                    text = errorText!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            // High-fidelity Relative Profile Picture Component (Native Android equivalent of Flutter image_picker workflow)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                ),
                border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Relative Profile Picture",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Avatar Circle Frame
                    Box(
                        modifier = Modifier
                            .size(105.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        val isCustomPhoto = photoUri.isNotBlank() && (
                                photoUri.startsWith("content://") || 
                                photoUri.startsWith("file://") || 
                                photoUri.startsWith("http") || 
                                photoUri.contains("/")
                            )
                        if (isCustomPhoto) {
                            AsyncImage(
                                model = photoUri,
                                contentDescription = "Profile Photo",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            // Empty avatar placeholder icon
                            Icon(
                                imageVector = Icons.Default.PhotoLibrary,
                                contentDescription = "Empty Photo",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Buttons Layout
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Gallery selection button
                        Button(
                            onClick = { galleryLauncher.launch("image/*") },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                            modifier = Modifier.testTag("gallery_picker_btn")
                        ) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Gallery", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                        }

                        // Camera capture button
                        Button(
                            onClick = { cameraLauncher.launch() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                            modifier = Modifier.testTag("camera_capture_btn")
                        ) {
                            Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Camera", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                        }

                        // Remove image option
                        if (photoUri.isNotBlank()) {
                            OutlinedButton(
                                onClick = { photoUri = "" },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error
                                ),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f)),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                                modifier = Modifier.testTag("remove_photo_btn")
                            ) {
                                Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Clear", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                            }
                        }
                    }
                }
            }

            // Input: Names
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = { Text("Given / First Name") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1.0f).testTag("field_first_name"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = { Text("Family / Last Name") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1.0f).testTag("field_last_name"),
                    singleLine = true
                )
            }

            // Input: Gender Selection
            Column {
                Text("Vitals Identifier (Gender):", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    listOf("Male", "Female", "Other").forEach { choice ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = gender == choice, onClick = { gender = choice })
                            Text(choice)
                        }
                    }
                }
            }

            // Input: Generation picker (Generations 1 to 7 supported)
            Column {
                Text("Generational Tier (Level 1 [Oldest] to 7 [Latest]):", style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Slider(
                        value = generation.toFloat(),
                        onValueChange = { generation = it.toInt() },
                        valueRange = 1f..7f,
                        steps = 5,
                        modifier = Modifier.weight(1f).testTag("generation_level_slider")
                    )
                    Text(
                        "Generation $generation",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 12.dp)
                    )
                }
            }

            // Input: Birth Vitals
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = birthDate,
                    onValueChange = { birthDate = it },
                    placeholder = { Text("YYYY-MM-DD") },
                    label = { Text("Date of Birth") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1.0f).testTag("field_birth_date"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = birthLocation,
                    onValueChange = { birthLocation = it },
                    placeholder = { Text("e.g. Portland, OR") },
                    label = { Text("Birth Location") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1.0f).testTag("field_birth_loc"),
                    singleLine = true
                )
            }

            // Input: Deceased Checkbox
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = isDeceased,
                    onCheckedChange = { isDeceased = it },
                    modifier = Modifier.testTag("field_is_deceased")
                )
                Column {
                    Text("Deceased / Departed", fontWeight = FontWeight.Bold)
                    Text("Check this box to record passing timelines and death location.", style = MaterialTheme.typography.bodySmall)
                }
            }

            // Input: Death specifics (visible if isDeceased)
            if (isDeceased) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = deathDate,
                        onValueChange = { deathDate = it },
                        placeholder = { Text("YYYY-MM-DD") },
                        label = { Text("Date of Death") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1.0f).testTag("field_death_date"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = deathLocation,
                        onValueChange = { deathLocation = it },
                        placeholder = { Text("e.g. Boston, MA") },
                        label = { Text("Death Location") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1.0f).testTag("field_death_loc"),
                        singleLine = true
                    )
                }
            }

            // Input: Occupation
            OutlinedTextField(
                value = occupation,
                onValueChange = { occupation = it },
                label = { Text("Occupation / Main Profession") },
                placeholder = { Text("e.g. Rail Car Engineer") },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().testTag("field_occupation"),
                singleLine = true
            )

            // Input: Contact details (for living ancestors)
            if (!isDeceased) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = contactPhone,
                        onValueChange = { contactPhone = it },
                        placeholder = { Text("+1 (123) 456-7890") },
                        label = { Text("Contact Phone") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1.0f),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = contactEmail,
                        onValueChange = { contactEmail = it },
                        placeholder = { Text("name@domain.com") },
                        label = { Text("Contact Email") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1.0f),
                        singleLine = true
                    )
                }
            }

            // Input: Brief notes initial bio
            OutlinedTextField(
                value = bio,
                onValueChange = { bio = it },
                label = { Text("Biographical Legacy Notes") },
                placeholder = { Text("Record initial notes here. The generative AI Biographer can later craft this into a flowing narrative story!") },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(120.dp).testTag("field_notes")
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Save trigger button
            Button(
                onClick = {
                    if (firstName.isBlank() || lastName.isBlank()) {
                        errorText = "Given name and family name are required!"
                    } else {
                        viewModel.saveMemberProfile(
                            id = member?.id ?: 0,
                            firstName = firstName,
                            lastName = lastName,
                            gender = gender,
                            birthDate = birthDate,
                            birthLocation = birthLocation,
                            isDeceased = isDeceased,
                            deathDate = deathDate,
                            deathLocation = deathLocation,
                            bio = bio,
                            occupation = occupation,
                            contactPhone = contactPhone,
                            contactEmail = contactEmail,
                            photoUri = photoUri,
                            generation = generation
                        )
                    }
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(50.dp).testTag("save_profile_btn")
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save Family Profile")
            }

            // Optional Delete Ancestor Option if editing
            if (member != null) {
                OutlinedButton(
                    onClick = { viewModel.removeMember(member!!) },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.fillMaxWidth().testTag("delete_member_btn")
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Delete Family Profile from Database")
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
