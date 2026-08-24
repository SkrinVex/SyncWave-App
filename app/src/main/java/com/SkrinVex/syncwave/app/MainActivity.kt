package com.SkrinVex.syncwave.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import com.SkrinVex.syncwave.app.domain.model.Resource
import com.SkrinVex.syncwave.app.ui.components.StudioSoundwaveLogo
import com.SkrinVex.syncwave.app.ui.navigation.AppNavGraph
import com.SkrinVex.syncwave.app.ui.navigation.Screen
import com.SkrinVex.syncwave.app.ui.theme.StudioBg
import com.SkrinVex.syncwave.app.ui.theme.SyncWaveTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            SyncWaveTheme {
                val navController = rememberNavController()
                val container = (application as SyncWaveApplication).container

                var startDestination by remember { mutableStateOf<String?>(null) }

                // Request POST_NOTIFICATIONS runtime permission on Android 13+ (API 33+)
                val notificationPermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { /* Permission result handled by system */ }

                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        if (ContextCompat.checkSelfPermission(
                                this@MainActivity,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                }

                LaunchedEffect(Unit) {
                    val session = container.sessionDataStore.getSavedSession()
                    if (session != null && session.token.isNotBlank()) {
                        // Verify token validity
                        when (container.getCurrentUserUseCase()) {
                            is Resource.Success -> {
                                startDestination = Screen.Main.route
                            }
                            is Resource.Error -> {
                                startDestination = Screen.Auth.route
                            }
                            Resource.Loading -> {}
                        }
                    } else {
                        startDestination = Screen.Auth.route
                    }
                }

                if (startDestination == null) {
                    // Splash Loading Screen
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(StudioBg),
                        contentAlignment = Alignment.Center
                    ) {
                        StudioSoundwaveLogo(
                            size = 72.dp,
                            isAnimated = true
                        )
                    }
                } else {
                    AppNavGraph(
                        navController = navController,
                        startDestination = startDestination!!
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        (application as? SyncWaveApplication)?.container?.audioPlayerManager?.release()
    }
}
