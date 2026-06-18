package com.pichangapp.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pichangapp.domain.model.SportType
import com.pichangapp.ui.components.*

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateBack   : () -> Unit,
    viewModel        : RegisterViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.registerSuccess) {
        if (uiState.registerSuccess) onRegisterSuccess()
    }
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            PichangTopBar(
                title          = "Crear cuenta",
                onNavigateBack = onNavigateBack
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            Text(
                text  = "Únete a la comunidad",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text  = "Completa tu perfil para empezar a jugar",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(4.dp))

            PichangTextField(
                value         = uiState.name,
                onValueChange = viewModel::onNameChange,
                label         = "Nombre completo",
                leadingIcon   = Icons.Rounded.Person,
                isError       = uiState.nameError != null,
                errorMessage  = uiState.nameError,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )

            PichangTextField(
                value         = uiState.email,
                onValueChange = viewModel::onEmailChange,
                label         = "Correo electrónico",
                leadingIcon   = Icons.Rounded.Email,
                isError       = uiState.emailError != null,
                errorMessage  = uiState.emailError,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction    = ImeAction.Next
                )
            )

            PichangTextField(
                value         = uiState.password,
                onValueChange = viewModel::onPasswordChange,
                label         = "Contraseña",
                leadingIcon   = Icons.Rounded.Lock,
                isPassword    = true,
                isError       = uiState.passwordError != null,
                errorMessage  = uiState.passwordError,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction    = ImeAction.Next
                )
            )

            PichangTextField(
                value         = uiState.confirmPassword,
                onValueChange = viewModel::onConfirmPasswordChange,
                label         = "Confirmar contraseña",
                leadingIcon   = Icons.Rounded.LockOpen,
                isPassword    = true,
                isError       = uiState.confirmError != null,
                errorMessage  = uiState.confirmError,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction    = ImeAction.Done
                )
            )

            // ── Selección de deportes favoritos ───────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text  = "¿Qué deportes practicas?",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text  = "Selecciona los deportes de tu interés",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement   = Arrangement.spacedBy(8.dp)
                ) {
                    SportType.entries.forEach { sport ->
                        val selected = sport in uiState.selectedSports
                        FilterChip(
                            selected = selected,
                            onClick  = { viewModel.toggleSport(sport) },
                            label    = {
                                Text("${sport.emoji} ${sport.displayName}")
                            },
                            leadingIcon = if (selected) {
                                { Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                            } else null
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            PichangButton(
                text      = "Crear cuenta",
                onClick   = viewModel::register,
                enabled   = uiState.isFormValid,
                isLoading = uiState.isLoading,
                icon      = Icons.Rounded.PersonAdd
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}