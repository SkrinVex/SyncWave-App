package com.SkrinVex.syncwave.app.ui.screens.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.SkrinVex.syncwave.app.ui.components.StudioButton
import com.SkrinVex.syncwave.app.ui.components.StudioCard
import com.SkrinVex.syncwave.app.ui.components.StudioSoundwaveLogo
import com.SkrinVex.syncwave.app.ui.components.StudioTextField
import com.SkrinVex.syncwave.app.ui.theme.StudioAccent
import com.SkrinVex.syncwave.app.ui.theme.StudioBg
import com.SkrinVex.syncwave.app.ui.theme.StudioBorder
import com.SkrinVex.syncwave.app.ui.theme.StudioElevated
import com.SkrinVex.syncwave.app.ui.theme.StudioEmerald
import com.SkrinVex.syncwave.app.ui.theme.StudioRed
import com.SkrinVex.syncwave.app.ui.theme.StudioSurface
import com.SkrinVex.syncwave.app.ui.theme.Zinc100
import com.SkrinVex.syncwave.app.ui.theme.Zinc400
import com.SkrinVex.syncwave.app.ui.theme.Zinc500

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onNavigateToMain: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is AuthEvent.NavigateToMain -> onNavigateToMain()
                is AuthEvent.ShowToast -> {}
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(StudioBg)
            .imePadding()
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp, vertical = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Animated Soundwave Brand Logo
            StudioSoundwaveLogo(
                size = 56.dp,
                isAnimated = true,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Brand Title & Exact Web Subtitle
            Text(
                text = "SyncWave",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Zinc100,
                letterSpacing = (-0.5).sp
            )
            Text(
                text = "Автономный сервер синхронизации и стриминга YouTube Music",
                fontSize = 12.sp,
                color = Zinc400,
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp, start = 16.dp, end = 16.dp),
                textAlign = TextAlign.Center,
                lineHeight = 16.sp
            )

            // Main Auth Card
            StudioCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = StudioSurface,
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Server Connection & URL Configuration Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(StudioElevated)
                            .border(1.dp, StudioBorder, RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Dns,
                                        contentDescription = null,
                                        tint = if (uiState.isServerConnected) StudioEmerald else StudioAccent,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Сервер SyncWave",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Zinc100
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    if (uiState.isCheckingServer) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(14.dp),
                                            color = StudioAccent,
                                            strokeWidth = 1.5.dp
                                        )
                                    } else if (uiState.isServerConnected) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Подключено",
                                            tint = StudioEmerald,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = { viewModel.checkServerStatus(isInitialCheck = false) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = "Проверить",
                                            tint = Zinc400,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Server URL input field
                            StudioTextField(
                                value = uiState.serverUrl,
                                onValueChange = { viewModel.onServerUrlChange(it) },
                                label = "URL адрес сервера",
                                placeholder = "https://syncwave.skrinvex.com",
                                leadingIcon = Icons.Default.Tune,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Uri,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        focusManager.clearFocus()
                                        viewModel.checkServerStatus(isInitialCheck = false)
                                    }
                                )
                            )
                        }
                    }

                    // Mode Header matching Web i18n
                    Column {
                        Text(
                            text = if (uiState.needsSetup) "Инициализация администратора" else "Вход в систему",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Zinc100
                        )
                        Text(
                            text = if (uiState.needsSetup)
                                "Создайте основную учетную запись администратора сервера"
                            else
                                "Введите учетные данные для доступа к вашей медиатеке",
                            fontSize = 12.sp,
                            color = Zinc400,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    // Username Input matching Web i18n
                    StudioTextField(
                        value = uiState.username,
                        onValueChange = { viewModel.onUsernameChange(it) },
                        label = "Имя пользователя",
                        placeholder = "например, admin",
                        leadingIcon = Icons.Default.Person,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        )
                    )

                    // Password Input matching Web i18n
                    StudioTextField(
                        value = uiState.password,
                        onValueChange = { viewModel.onPasswordChange(it) },
                        label = "Пароль",
                        placeholder = "••••••••",
                        leadingIcon = Icons.Default.Lock,
                        isPassword = true,
                        isPasswordVisible = uiState.isPasswordVisible,
                        onPasswordVisibilityToggle = { viewModel.togglePasswordVisibility() },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                viewModel.submit()
                            }
                        )
                    )

                    // Error Message Banner
                    AnimatedVisibility(visible = !uiState.errorMessage.isNullOrBlank()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(StudioRed.copy(alpha = 0.12f))
                                .border(1.dp, StudioRed.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = StudioRed,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = uiState.errorMessage ?: "",
                                fontSize = 12.sp,
                                color = StudioRed,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Submit Action Button matching Web i18n
                    StudioButton(
                        text = if (uiState.needsSetup) "Создать администратора и начать" else "Войти в аккаунт",
                        onClick = {
                            focusManager.clearFocus()
                            viewModel.submit()
                        },
                        isLoading = uiState.isLoading,
                        enabled = !uiState.isLoading
                    )
                }
            }

            // Exact Slogan from Website i18n (auth.slogan)
            Text(
                text = "SyncWave Core • Полная независимость от облачных стримингов",
                fontSize = 12.sp,
                color = Zinc400,
                modifier = Modifier.padding(top = 24.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}
