package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAlarm
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.MaintenanceConfig
import com.example.data.MaintenanceControlConfig
import com.example.data.SystemControlConfig
import com.example.ui.theme.KcetErrorRed
import com.example.ui.theme.KcetIndigoPrimary
import com.example.ui.theme.KcetSuccessGreen
import com.example.ui.theme.KcetWarningAmber
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MaintenanceAdminSection(
    config: MaintenanceConfig,
    isMaintenanceActive: Boolean,
    remainingMillisFlow: StateFlow<Long>,
    serverTimeOffsetMs: Long,
    isUpdating: Boolean,
    feedback: Pair<Boolean, String>?,
    onActivateMaintenance: (hours: Int, minutes: Int) -> Unit,
    onExtendMaintenance: (hours: Int, minutes: Int, replaceFromNow: Boolean) -> Unit,
    onDisableMaintenance: () -> Unit,
    onDismissFeedback: () -> Unit
) {
    // Draft configuration state (NEVER activates maintenance until Admin confirms OK / Start Maintenance)
    var isConfiguringNewMaintenance by rememberSaveable { mutableStateOf(false) }
    var startHoursText by rememberSaveable { mutableStateOf("0") }
    var startMinutesText by rememberSaveable { mutableStateOf("30") }

    // Extend / Update timer draft state when maintenance is active
    var extendReplaceFromNow by rememberSaveable { mutableStateOf(false) }
    var extendHoursText by rememberSaveable { mutableStateOf("0") }
    var extendMinutesText by rememberSaveable { mutableStateOf("30") }

    // Turn OFF confirmation dialog
    var showTurnOffDialog by remember { mutableStateOf(false) }

    // Keep preview clock fresh every 15 seconds while configuring/extending
    var previewAnchorMs by remember { mutableStateOf(System.currentTimeMillis() + serverTimeOffsetMs) }
    LaunchedEffect(isConfiguringNewMaintenance, isMaintenanceActive, serverTimeOffsetMs) {
        while (true) {
            previewAnchorMs = System.currentTimeMillis() + serverTimeOffsetMs
            delay(15_000L)
        }
    }

    // Close the draft panel automatically once maintenance becomes active
    LaunchedEffect(isMaintenanceActive) {
        if (isMaintenanceActive) {
            isConfiguringNewMaintenance = false
        }
    }

    val startValidation = remember(startHoursText, startMinutesText) {
        MaintenanceControlConfig.validateDurationStrings(startHoursText, startMinutesText)
    }
    val extendValidation = remember(extendHoursText, extendMinutesText) {
        MaintenanceControlConfig.validateDurationStrings(extendHoursText, extendMinutesText)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("admin_maintenance_section"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Feedback Banner
        if (feedback != null) {
            val (isSuccess, message) = feedback
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (isSuccess) KcetSuccessGreen.copy(alpha = 0.14f) else KcetErrorRed.copy(alpha = 0.14f),
                border = BorderStroke(
                    1.dp,
                    if (isSuccess) KcetSuccessGreen.copy(alpha = 0.5f) else KcetErrorRed.copy(alpha = 0.5f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_maintenance_feedback_banner")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = if (isSuccess) KcetSuccessGreen else KcetErrorRed,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = message,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(
                        onClick = onDismissFeedback,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss maintenance message",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Main Header Card: App Maintenance / Maintenance Mode + Toggle Switch
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isMaintenanceActive) {
                    KcetWarningAmber.copy(alpha = 0.10f)
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
            ),
            border = BorderStroke(
                1.dp,
                if (isMaintenanceActive) KcetWarningAmber.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outlineVariant
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isMaintenanceActive) KcetWarningAmber.copy(alpha = 0.2f) else KcetIndigoPrimary.copy(alpha = 0.14f),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Build,
                                contentDescription = "Maintenance Mode Icon",
                                tint = if (isMaintenanceActive) KcetWarningAmber else KcetIndigoPrimary,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "App Maintenance / Maintenance Mode",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = when {
                                    isMaintenanceActive -> "ACTIVE • All student & user screens locked"
                                    isConfiguringNewMaintenance -> "Configuring duration (not active until confirmed)"
                                    else -> "OFF • KCET Gen Z is live for all users"
                                },
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = if (isMaintenanceActive) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = if (isMaintenanceActive) KcetWarningAmber else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Switch(
                            checked = isMaintenanceActive || isConfiguringNewMaintenance,
                            onCheckedChange = { wantEnabled ->
                                if (isUpdating) return@Switch
                                if (isMaintenanceActive) {
                                    if (!wantEnabled) {
                                        showTurnOffDialog = true
                                    }
                                } else {
                                    isConfiguringNewMaintenance = wantEnabled
                                }
                            },
                            enabled = !isUpdating,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = if (isMaintenanceActive) KcetWarningAmber else KcetIndigoPrimary
                            ),
                            modifier = Modifier
                                .testTag("admin_maintenance_toggle")
                                .semantics { contentDescription = "Maintenance Mode toggle" }
                        )
                        Text(
                            text = "Maintenance Mode",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Active Maintenance Status & Quick Turn-OFF Control
                if (isMaintenanceActive) {
                    HorizontalDivider(color = KcetWarningAmber.copy(alpha = 0.35f))

                    AdminLiveMaintenanceStatusPanel(
                        config = config,
                        remainingMillisFlow = remainingMillisFlow,
                        serverTimeOffsetMs = serverTimeOffsetMs
                    )

                    Button(
                        onClick = { showTurnOffDialog = true },
                        enabled = !isUpdating,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = KcetErrorRed,
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .testTag("admin_turn_off_maintenance_button")
                    ) {
                        if (isUpdating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        } else {
                            Icon(
                                imageVector = Icons.Default.PowerSettingsNew,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(
                            text = "Turn OFF Maintenance",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Duration Configuration Card (Shown ONLY when Admin turns toggle ON prior to confirmation)
        AnimatedVisibility(
            visible = !isMaintenanceActive && isConfiguringNewMaintenance,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_maintenance_duration_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, KcetIndigoPrimary.copy(alpha = 0.35f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = KcetIndigoPrimary
                        )
                        Text(
                            text = "Set Maintenance Duration",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Text(
                        text = "Select a preset or enter custom Hours and Minutes. Maintenance activates only after you tap OK / Start Maintenance.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        MaintenanceControlConfig.START_PRESETS.forEach { preset ->
                            val isSelected = startHoursText.trim().toIntOrNull() == preset.hours &&
                                startMinutesText.trim().toIntOrNull() == preset.minutes
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    startHoursText = preset.hours.toString()
                                    startMinutesText = preset.minutes.toString()
                                },
                                label = {
                                    Text(
                                        text = preset.label,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = KcetIndigoPrimary.copy(alpha = 0.16f),
                                    selectedLabelColor = KcetIndigoPrimary
                                ),
                                modifier = Modifier
                                    .heightIn(min = 48.dp)
                                    .testTag("admin_maintenance_preset_${preset.label.replace(" ", "_")}")
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = startHoursText,
                            onValueChange = { input ->
                                if (input.length <= 4 && input.all { it.isDigit() || it == '-' }) {
                                    startHoursText = input
                                }
                            },
                            label = { Text("Hours") },
                            placeholder = { Text("0") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("admin_maintenance_hours_input")
                        )

                        OutlinedTextField(
                            value = startMinutesText,
                            onValueChange = { input ->
                                if (input.length <= 4 && input.all { it.isDigit() || it == '-' }) {
                                    startMinutesText = input
                                }
                            },
                            label = { Text("Minutes") },
                            placeholder = { Text("30") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("admin_maintenance_minutes_input")
                        )
                    }

                    if (startValidation.isValid) {
                        val estimatedEndMs = previewAnchorMs + startValidation.totalDurationMillis
                        val formattedEnd = MaintenanceControlConfig.formatShortTime(estimatedEndMs, previewAnchorMs)
                        val summary = MaintenanceControlConfig.formatDurationSummary(startValidation.totalDurationMillis)
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = KcetIndigoPrimary.copy(alpha = 0.09f),
                            border = BorderStroke(1.dp, KcetIndigoPrimary.copy(alpha = 0.25f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = KcetIndigoPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Estimated end time: $formattedEnd ($summary)",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.testTag("admin_maintenance_estimated_end")
                                )
                            }
                        }
                    } else {
                        Text(
                            text = startValidation.errorMessage ?: "Please enter a valid positive duration.",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = KcetErrorRed,
                            modifier = Modifier.testTag("admin_maintenance_validation_error")
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                isConfiguringNewMaintenance = false
                            },
                            enabled = !isUpdating,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .testTag("admin_maintenance_cancel_button")
                        ) {
                            Text("Cancel", fontWeight = FontWeight.SemiBold)
                        }

                        Button(
                            onClick = {
                                if (startValidation.isValid) {
                                    onActivateMaintenance(startValidation.hours, startValidation.minutes)
                                }
                            },
                            enabled = startValidation.isValid && !isUpdating,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = KcetIndigoPrimary,
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .weight(1.4f)
                                .heightIn(min = 48.dp)
                                .testTag("admin_start_maintenance_button")
                        ) {
                            if (isUpdating) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text("OK / Start Maintenance", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Extend Maintenance / Update Timer Card (Shown while maintenance is active)
        AnimatedVisibility(
            visible = isMaintenanceActive,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_maintenance_extend_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddAlarm,
                            contentDescription = null,
                            tint = KcetIndigoPrimary
                        )
                        Text(
                            text = "Extend Maintenance / Update Timer",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Text(
                        text = "Update the active maintenance countdown smoothly without unlocking the app first.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Mode chips: Add Extra Time vs Set New Duration
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = !extendReplaceFromNow,
                            onClick = { extendReplaceFromNow = false },
                            label = { Text("Add Extra Time", fontWeight = FontWeight.SemiBold) },
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("admin_maintenance_mode_add")
                        )
                        FilterChip(
                            selected = extendReplaceFromNow,
                            onClick = { extendReplaceFromNow = true },
                            label = { Text("Set New Duration", fontWeight = FontWeight.SemiBold) },
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("admin_maintenance_mode_replace")
                        )
                    }

                    val presets = if (extendReplaceFromNow) {
                        MaintenanceControlConfig.START_PRESETS
                    } else {
                        MaintenanceControlConfig.EXTEND_PRESETS
                    }
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        presets.forEach { preset ->
                            val isSelected = extendHoursText.trim().toIntOrNull() == preset.hours &&
                                extendMinutesText.trim().toIntOrNull() == preset.minutes
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    extendHoursText = preset.hours.toString()
                                    extendMinutesText = preset.minutes.toString()
                                },
                                label = { Text(preset.label, fontWeight = FontWeight.Medium) },
                                modifier = Modifier.heightIn(min = 48.dp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = extendHoursText,
                            onValueChange = { input ->
                                if (input.length <= 4 && input.all { it.isDigit() || it == '-' }) {
                                    extendHoursText = input
                                }
                            },
                            label = { Text("Hours") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("admin_maintenance_extend_hours_input")
                        )
                        OutlinedTextField(
                            value = extendMinutesText,
                            onValueChange = { input ->
                                if (input.length <= 4 && input.all { it.isDigit() || it == '-' }) {
                                    extendMinutesText = input
                                }
                            },
                            label = { Text("Minutes") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("admin_maintenance_extend_minutes_input")
                        )
                    }

                    if (extendValidation.isValid) {
                        val updatedEndMs = if (extendReplaceFromNow) {
                            previewAnchorMs + extendValidation.totalDurationMillis
                        } else {
                            maxOf(config.maintenanceEndTime, previewAnchorMs) + extendValidation.totalDurationMillis
                        }
                        val formattedNewEnd = MaintenanceControlConfig.formatShortTime(updatedEndMs, previewAnchorMs)
                        val summary = MaintenanceControlConfig.formatDurationSummary(extendValidation.totalDurationMillis)
                        Text(
                            text = if (extendReplaceFromNow) {
                                "New end time: $formattedNewEnd ($summary from now)"
                            } else {
                                "New extended end time: $formattedNewEnd (+$summary)"
                            },
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = KcetIndigoPrimary
                        )
                    } else {
                        Text(
                            text = extendValidation.errorMessage ?: "Please enter a valid positive duration.",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = KcetErrorRed
                        )
                    }

                    Button(
                        onClick = {
                            if (extendValidation.isValid) {
                                onExtendMaintenance(
                                    extendValidation.hours,
                                    extendValidation.minutes,
                                    extendReplaceFromNow
                                )
                            }
                        },
                        enabled = extendValidation.isValid && !isUpdating,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = KcetIndigoPrimary,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .testTag("admin_extend_maintenance_button")
                    ) {
                        Text(
                            text = if (extendReplaceFromNow) "Update Timer" else "Extend Maintenance",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    if (showTurnOffDialog) {
        AlertDialog(
            onDismissRequest = { if (!isUpdating) showTurnOffDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = KcetWarningAmber
                )
            },
            title = {
                Text(
                    text = "Turn OFF Maintenance Mode?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "This will immediately restore full access to KCET Gen Z for all students and users."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showTurnOffDialog = false
                        onDisableMaintenance()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = KcetErrorRed,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.testTag("admin_confirm_turn_off_maintenance_button")
                ) {
                    Text("Turn OFF Maintenance", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showTurnOffDialog = false }
                ) {
                    Text("Keep Active")
                }
            }
        )
    }
}

@Composable
private fun AdminLiveMaintenanceStatusPanel(
    config: MaintenanceConfig,
    remainingMillisFlow: StateFlow<Long>,
    serverTimeOffsetMs: Long
) {
    val remainingMillis by remainingMillisFlow.collectAsStateWithLifecycle()
    val parts = remember(remainingMillis) {
        MaintenanceControlConfig.formatCountdownParts(remainingMillis)
    }
    val nowMs = System.currentTimeMillis() + serverTimeOffsetMs
    val expectedBack = remember(config.maintenanceEndTime, serverTimeOffsetMs) {
        MaintenanceControlConfig.formatShortTime(config.maintenanceEndTime, nowMs)
    }
    val startedAt = remember(config.maintenanceStartTime, serverTimeOffsetMs) {
        MaintenanceControlConfig.formatShortTime(config.maintenanceStartTime, nowMs)
    }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, KcetWarningAmber.copy(alpha = 0.4f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("admin_maintenance_active_status_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Time Remaining",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${parts.hoursFormatted} : ${parts.minutesFormatted} : ${parts.secondsFormatted}",
                    style = TextStyle(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFeatureSettings = "tnum"
                    ),
                    color = KcetWarningAmber,
                    modifier = Modifier.testTag("admin_maintenance_live_timer")
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Started: $startedAt",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Ends: $expectedBack",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun SystemControlsMaintenanceCard(
    isSystemShutdown: Boolean,
    isResettingLeaderboard: Boolean,
    leaderboardCycle: SystemControlConfig.LeaderboardCycleStatus,
    onRequestResetLeaderboard: () -> Unit,
    onRequestToggleSystemShutdown: (Boolean) -> Unit,
    onTogglePauseLeaderboard: (Boolean) -> Unit,
    onExtendLeaderboard: (Long) -> Unit
) {
    Surface(
        color = if (isSystemShutdown) Color(0xFF450A0A) else Color(0xFF1E1B4B),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(
            1.dp,
            if (isSystemShutdown) Color(0xFFEF4444).copy(alpha = 0.7f) else Color(0xFF4338CA).copy(alpha = 0.6f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("admin_system_controls_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "System & Leaderboard Controls",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = Color.White
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onRequestResetLeaderboard,
                    enabled = !isResettingLeaderboard,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD97706),
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .testTag("admin_reset_leaderboard_button")
                ) {
                    if (isResettingLeaderboard) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = "Reset Leaderboard",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isResettingLeaderboard) "Resetting XP..." else "Reset Leaderboard",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = if (isSystemShutdown) "Shutdown ON" else "System Online",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isSystemShutdown) Color(0xFFFCA5A5) else Color(0xFF6EE7B7)
                        )
                        Text(
                            text = if (isSystemShutdown) "Portals Locked" else "Tap to Shutdown",
                            fontSize = 10.sp,
                            color = Color(0xFFCBD5E1)
                        )
                    }
                    Switch(
                        checked = isSystemShutdown,
                        onCheckedChange = onRequestToggleSystemShutdown,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFFEF4444),
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Color(0xFF10B981)
                        ),
                        modifier = Modifier
                            .testTag("admin_system_shutdown_toggle")
                            .semantics { contentDescription = "System Shutdown toggle" }
                    )
                }
            }

            HorizontalDivider(color = Color.White.copy(alpha = 0.12f))

            // Leaderboard Timer Pause & Extra Time Controls
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "30-Day Season Timer Control",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = if (leaderboardCycle.isPaused) "Timer Paused ⏸️" else "Timer Running ⏱️",
                            fontSize = 10.sp,
                            color = if (leaderboardCycle.isPaused) Color(0xFFFBBF24) else Color(0xFF34D399)
                        )
                    }
                    Button(
                        onClick = { onTogglePauseLeaderboard(!leaderboardCycle.isPaused) },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (leaderboardCycle.isPaused) Color(0xFF059669) else Color(0xFFB45309)
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.heightIn(min = 36.dp)
                    ) {
                        Text(
                            text = if (leaderboardCycle.isPaused) "Resume Timer" else "Pause Timer",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Text(
                    text = "Add Extra Time to Season Timer:",
                    fontSize = 11.sp,
                    color = Color(0xFFCBD5E1),
                    fontWeight = FontWeight.Medium
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Button(
                        onClick = { onExtendLeaderboard(3600_000L) }, // +1 Hour
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF312E81)),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(4.dp)
                    ) {
                        Text("+1 Hr", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { onExtendLeaderboard(24L * 3600_000L) }, // +1 Day
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF312E81)),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(4.dp)
                    ) {
                        Text("+1 Day", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { onExtendLeaderboard(7L * 24L * 3600_000L) }, // +7 Days
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF312E81)),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(4.dp)
                    ) {
                        Text("+7 Days", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { onExtendLeaderboard(30L * 24L * 3600_000L) }, // +30 Days
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4C1D95)),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(4.dp)
                    ) {
                        Text("+30 Days", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (isSystemShutdown) {
                Text(
                    text = "⚠️ Student & Mentor portals are showing Not Available. Admin Portal remains active.",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFFEE2E2)
                )
            }
        }
    }
}

