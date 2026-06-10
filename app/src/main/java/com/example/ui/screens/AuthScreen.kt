package com.example.ui.screens

import android.app.Activity
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.OtpStatus
import com.example.ui.Screen
import com.example.ui.theme.ForestGreen

@Composable
fun AuthScreen(viewModel: MainViewModel, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val activity = remember(context) { context as? Activity }
    val otpState by viewModel.otpState.collectAsState()

    var isOtpTab by remember { mutableStateOf(false) }
    var emailOrPhone by remember { mutableStateOf("") }
    var passwordOrOtpCount by remember { mutableStateOf("") }
    var otpSent by remember { mutableStateOf(false) }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(otpState) {
        when (val state = otpState) {
            is OtpStatus.Idle -> {}
            is OtpStatus.Sending -> {
                errorMessage = null
                successMessage = "Processing secure verification parameters..."
            }
            is OtpStatus.CodeSent -> {
                otpSent = true
                successMessage = "Verification OTP code sent successfully! Verification ID is active."
                errorMessage = null
            }
            is OtpStatus.Verified -> {
                successMessage = "Firebase Secure Login Succeeded! Access Granted."
                errorMessage = null
            }
            is OtpStatus.Error -> {
                errorMessage = state.message
                successMessage = null
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        // App Logo Icon
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.AccountTree,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Family Tree",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Preserving family lineages for generations to come",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Tab Selector for Email vs Phone OTP
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = {
                    isOtpTab = false
                    errorMessage = null
                    successMessage = null
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (!isOtpTab) MaterialTheme.colorScheme.primary else Color.Transparent,
                    contentColor = if (!isOtpTab) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .weight(1.0f)
                    .testTag("email_login_tab")
            ) {
                Text("Email Account")
            }
            Button(
                onClick = {
                    isOtpTab = true
                    errorMessage = null
                    successMessage = null
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isOtpTab) MaterialTheme.colorScheme.primary else Color.Transparent,
                    contentColor = if (isOtpTab) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .weight(1.0f)
                    .testTag("otp_login_tab")
            ) {
                Text("Mobile OTP")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Display alerts
        if (errorMessage != null) {
            Text(
                text = errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            )
        }
        if (successMessage != null) {
            Text(
                text = successMessage!!,
                color = ForestGreen,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            )
        }

        // Fields
        if (!isOtpTab) {
            // Email Input
            OutlinedTextField(
                value = emailOrPhone,
                onValueChange = { emailOrPhone = it },
                label = { Text("Email Address") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("email_input"),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Password Input
            OutlinedTextField(
                value = passwordOrOtpCount,
                onValueChange = { passwordOrOtpCount = it },
                label = { Text("Security Password") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                shape = RoundedCornerShape(12.dp),
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("password_input"),
                singleLine = true
            )
        } else {
            // Phone input
            OutlinedTextField(
                value = emailOrPhone,
                onValueChange = { emailOrPhone = it },
                label = { Text("Mobile Number (incl. country code)") },
                placeholder = { Text("+1 (555) 019-2834") },
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("phone_input"),
                singleLine = true
            )

            if (otpSent) {
                Spacer(modifier = Modifier.height(16.dp))
                // OTP Code input
                OutlinedTextField(
                    value = passwordOrOtpCount,
                    onValueChange = { passwordOrOtpCount = it },
                    label = { Text("6-Digit Verification Code") },
                    placeholder = { Text("e.g. 192837") },
                    leadingIcon = { Icon(Icons.Default.VerifiedUser, contentDescription = null) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("otp_code_input"),
                    singleLine = true
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Main action buttons
        if (isOtpTab && !otpSent) {
            Button(
                onClick = {
                    if (emailOrPhone.isBlank()) {
                        errorMessage = "Please enter your mobile phone number"
                    } else if (activity == null) {
                        errorMessage = "Required system application context is missing"
                    } else {
                        viewModel.sendOtpCode(emailOrPhone, activity)
                    }
                },
                enabled = otpState !is OtpStatus.Sending,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("send_otp_button")
            ) {
                if (otpState is OtpStatus.Sending) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Request OTP Verification Code")
                }
            }
        } else {
            Button(
                onClick = {
                    if (emailOrPhone.isBlank() || passwordOrOtpCount.isBlank()) {
                        errorMessage = "Please fill out all required details"
                    } else {
                        if (isOtpTab) {
                            viewModel.verifyOtpCode(
                                code = passwordOrOtpCount,
                                phone = emailOrPhone,
                                onSuccess = {
                                    successMessage = "Access Granted via Firebase Authentication"
                                    errorMessage = null
                                },
                                onError = {
                                    errorMessage = it
                                    successMessage = null
                                }
                            )
                        } else {
                            viewModel.login(
                                emailOrPhone = emailOrPhone,
                                authCode = passwordOrOtpCount,
                                isOtpFlow = isOtpTab,
                                onSuccess = {
                                    successMessage = "Access Granted"
                                    errorMessage = null
                                },
                                onError = {
                                    errorMessage = it
                                    successMessage = null
                                }
                            )
                        }
                    }
                },
                enabled = otpState !is OtpStatus.Sending,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("login_submit_button")
            ) {
                if (otpState is OtpStatus.Sending) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(if (isOtpTab) "Verify and Log In" else "Sign In")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Extra redirect options
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(
                onClick = { viewModel.navigateTo(Screen.ForgotPassword) },
                modifier = Modifier.testTag("forgot_password_link")
            ) {
                Text("Forgot Password?")
            }

            TextButton(
                onClick = { viewModel.navigateTo(Screen.Register) },
                modifier = Modifier.testTag("register_redirect_link")
            ) {
                Text("Create Account")
            }
        }

        // Guest quick access trigger
        Spacer(modifier = Modifier.height(24.dp))
        Divider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = {
                // Instantly log in guest as Thomas Pendragon flow of user
                viewModel.login(
                    emailOrPhone = "archivist@legacypreserve.com",
                    authCode = "development_secret",
                    isOtpFlow = false,
                    onSuccess = {},
                    onError = {}
                )
            },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("guest_sandbox_button")
        ) {
            Icon(Icons.Default.Visibility, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Explore Seeded Sandbox (Friction-Free)")
        }
    }
}

@Composable
fun RegisterScreen(viewModel: MainViewModel, modifier: Modifier = Modifier) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var error by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.navigateTo(Screen.Auth) }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Go Back")
            }
            Text(
                text = "Registry Form",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (error != null) {
            Text(
                text = error!!,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 12.dp),
                textAlign = TextAlign.Center
            )
        }

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Full Name") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().testTag("reg_name_input")
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email Address") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().testTag("reg_email_input")
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Phone Number") },
            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().testTag("reg_phone_input")
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            shape = RoundedCornerShape(12.dp),
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth().testTag("reg_password_input")
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                viewModel.register(name, email, phone, password,
                    onSuccess = {},
                    onError = { error = it }
                )
            },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().height(50.dp).testTag("reg_submit_button")
        ) {
            Text("Register & Seed Family Database")
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = { viewModel.navigateTo(Screen.Auth) }) {
            Text("Already registered? Sign In")
        }
    }
}

@Composable
fun ForgotPasswordScreen(viewModel: MainViewModel, modifier: Modifier = Modifier) {
    var email by remember { mutableStateOf("") }
    var alertMessage by remember { mutableStateOf<String?>(null) }
    var isSuccess by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.navigateTo(Screen.Auth) }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "Recovery Vault",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Enter your family profile email below, and we will send secure verification parameters to reset password instantly.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (alertMessage != null) {
            Text(
                text = alertMessage!!,
                color = if (isSuccess) ForestGreen else MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 12.dp),
                textAlign = TextAlign.Center
            )
        }

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Registered Email") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().testTag("recovery_email_input")
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                viewModel.handleForgotPassword(email,
                    onSuccess = {
                        alertMessage = "Password recovery credentials dispatched. Please check your spam folder!"
                        isSuccess = true
                    },
                    onError = {
                        alertMessage = it
                        isSuccess = false
                    }
                )
            },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().height(50.dp).testTag("recovery_submit_button")
        ) {
            Text("Request Secure Reset")
        }
    }
}
