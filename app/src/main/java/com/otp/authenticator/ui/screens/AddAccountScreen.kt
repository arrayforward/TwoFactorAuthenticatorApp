package com.otp.authenticator.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.otp.authenticator.data.OtpAccount
import com.otp.authenticator.ui.theme.*
import com.otp.authenticator.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAccountScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    var issuer by remember { mutableStateOf("") }
    var label by remember { mutableStateOf("") }
    var secret by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Account") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkSurface,
                    titleContentColor = TextPrimary
                )
            )
        },
        containerColor = DarkBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Manual Entry",
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            )

            Text(
                text = "Enter the details from your OTP setup. The secret key is usually provided as a Base32 string.",
                color = TextSecondary,
                fontSize = 14.sp
            )

            OutlinedTextField(
                value = issuer,
                onValueChange = { issuer = it },
                label = { Text("Issuer (e.g. GitHub)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors(),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
            )

            OutlinedTextField(
                value = label,
                onValueChange = { label = it },
                label = { Text("Account (e.g. username)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors()
            )

            OutlinedTextField(
                value = secret,
                onValueChange = {
                    secret = it.replace(" ", "").uppercase()
                    errorMessage = null
                },
                label = { Text("Secret Key (Base32)") },
                singleLine = false,
                maxLines = 3,
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors(),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters)
            )

            errorMessage?.let {
                Text(
                    text = it,
                    color = AccentRed,
                    fontSize = 14.sp
                )
            }

            Button(
                onClick = {
                    if (secret.isBlank()) {
                        errorMessage = "Secret key is required"
                        return@Button
                    }
                    try {
                        com.otp.authenticator.otp.TotpGenerator.decodeBase32(secret)
                    } catch (_: Exception) {
                    }
                    val account = OtpAccount(
                        issuer = issuer.trim(),
                        label = label.trim().ifBlank { issuer.trim() },
                        secret = secret.trim()
                    )
                    viewModel.addAccount(account)
                    onNavigateBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
            ) {
                Text(
                    text = "Save Account",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
fun textFieldColors(): TextFieldColors {
    return OutlinedTextFieldDefaults.colors(
        focusedTextColor = TextPrimary,
        unfocusedTextColor = TextPrimary,
        cursorColor = AccentBlue,
        focusedBorderColor = AccentBlue,
        unfocusedBorderColor = TextSecondary.copy(alpha = 0.5f),
        focusedLabelColor = AccentBlue,
        unfocusedLabelColor = TextSecondary
    )
}
