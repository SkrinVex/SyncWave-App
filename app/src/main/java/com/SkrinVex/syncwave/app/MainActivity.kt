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
import androidx.compose.runtime.saveable.rememberSaveable
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

                // Survives configuration changes, so a rotation no longer flashes the
                // splash screen while the saved session is re-read.
                var startDestination by rememberSaveable { mutableStateOf<String?>(null) }

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
                        // If saved session exists, allow instant access (including offline mode)
                        startDestination = Screen.Main.route
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
        // The player is application-scoped and cannot be rebuilt once released. Releasing
        // it on every onDestroy killed playback on a rotation, a theme switch or a move
        // into multi-window, and left the app unable to play anything until a restart.
        // Only tear it down when the user is actually leaving.
        if (isFinishing && !isChangingConfigurations) {
            (application as? SyncWaveApplication)?.container?.audioPlayerManager?.release()
        }
    }
}
