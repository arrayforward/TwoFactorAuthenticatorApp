package com.otp.authenticator.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.otp.authenticator.ui.theme.*
import com.otp.authenticator.ui.viewmodel.MainViewModel
import com.otp.authenticator.ui.viewmodel.OtpDisplayItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigateToScan: () -> Unit,
    onNavigateToAdd: () -> Unit
) {
    val displayItems by viewModel.displayItems.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("OTP Authenticator", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkSurface,
                    titleContentColor = TextPrimary
                )
            )
        },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End) {
                SmallFloatingActionButton(
                    onClick = onNavigateToScan,
                    containerColor = AccentBlue
                ) {
                    Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan", tint = DarkBackground)
                }
                Spacer(modifier = Modifier.height(12.dp))
                SmallFloatingActionButton(
                    onClick = onNavigateToAdd,
                    containerColor = AccentGreen
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add", tint = DarkBackground)
                }
            }
        },
        containerColor = DarkBackground
    ) { padding ->
        if (displayItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No accounts yet.\nTap + to add one.",
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    fontSize = 16.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(displayItems, key = { it.account.id }) { item ->
                    OtpCard(
                        item = item,
                        onCopy = { copyToClipboard(context, item.code) },
                        onDelete = { viewModel.removeAccount(item.account.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun OtpCard(
    item: OtpDisplayItem,
    onCopy: () -> Unit,
    onDelete: () -> Unit
) {
    val progressColor by animateColorAsState(
        targetValue = when {
            item.remainingSeconds <= 5 -> AccentRed
            item.remainingSeconds <= 10 -> AccentOrange
            else -> AccentBlue
        },
        label = "progressColor"
    )

    var showDelete by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onCopy()
                showDelete = false
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.account.issuer.ifBlank { item.account.label },
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp
                    )
                    if (item.account.issuer.isNotBlank() && item.account.label.isNotBlank()) {
                        Text(
                            text = item.account.label,
                            color = TextSecondary,
                            fontSize = 14.sp
                        )
                    }
                }

                IconButton(onClick = {
                    showDelete = !showDelete
                }) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = if (showDelete) AccentRed else TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.code,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp,
                    color = TextPrimary,
                    letterSpacing = 4.sp
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${item.remainingSeconds}s",
                        color = progressColor,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        modifier = Modifier.width(36.dp),
                        textAlign = TextAlign.End
                    )
                    Icon(
                        Icons.Default.ContentCopy,
                        contentDescription = "Copy",
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = item.progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = progressColor,
                trackColor = DarkBackground,
            )
        }

        if (showDelete) {
            TextButton(
                onClick = onDelete,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AccentRed.copy(alpha = 0.15f))
            ) {
                Text("Confirm Delete", color = AccentRed)
            }
        }
    }
}

private fun copyToClipboard(context: Context, code: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("OTP Code", code))
}
