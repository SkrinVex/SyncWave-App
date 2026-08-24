package com.SkrinVex.syncwave.app.di

import android.content.Context
import com.SkrinVex.syncwave.app.data.local.SessionDataStore
import com.SkrinVex.syncwave.app.data.remote.api.SyncWaveApiService
import com.SkrinVex.syncwave.app.data.remote.interceptor.AuthInterceptor
import com.SkrinVex.syncwave.app.data.remote.interceptor.DynamicBaseUrlInterceptor
import com.SkrinVex.syncwave.app.data.repository.AuthRepositoryImpl
import com.SkrinVex.syncwave.app.data.repository.PlaylistRepositoryImpl
import com.SkrinVex.syncwave.app.data.repository.SettingsRepositoryImpl
import com.SkrinVex.syncwave.app.data.repository.SyncRepositoryImpl
import com.SkrinVex.syncwave.app.data.repository.TrackRepositoryImpl
import com.SkrinVex.syncwave.app.domain.repository.AuthRepository
import com.SkrinVex.syncwave.app.domain.repository.PlaylistRepository
import com.SkrinVex.syncwave.app.domain.repository.SettingsRepository
import com.SkrinVex.syncwave.app.domain.repository.SyncRepository
import com.SkrinVex.syncwave.app.domain.repository.TrackRepository
import com.SkrinVex.syncwave.app.domain.usecase.auth.CheckAuthStatusUseCase
import com.SkrinVex.syncwave.app.domain.usecase.auth.GetCurrentUserUseCase
import com.SkrinVex.syncwave.app.domain.usecase.auth.GetSavedSessionUseCase
import com.SkrinVex.syncwave.app.domain.usecase.auth.GetServerUrlUseCase
import com.SkrinVex.syncwave.app.domain.usecase.auth.LoginUseCase
import com.SkrinVex.syncwave.app.domain.usecase.auth.LogoutUseCase
import com.SkrinVex.syncwave.app.domain.usecase.auth.SaveServerUrlUseCase
import com.SkrinVex.syncwave.app.domain.usecase.auth.SetupAdminUseCase
import com.SkrinVex.syncwave.app.domain.usecase.playlist.CreatePlaylistUseCase
import com.SkrinVex.syncwave.app.domain.usecase.playlist.DeletePlaylistUseCase
import com.SkrinVex.syncwave.app.domain.usecase.playlist.GetPlaylistsUseCase
import com.SkrinVex.syncwave.app.domain.usecase.playlist.SyncPlaylistUseCase
import com.SkrinVex.syncwave.app.domain.usecase.settings.GetSettingsUseCase
import com.SkrinVex.syncwave.app.domain.usecase.sync.GetSyncLogsUseCase
import com.SkrinVex.syncwave.app.domain.usecase.sync.GetSyncProgressUseCase
import com.SkrinVex.syncwave.app.domain.usecase.sync.TriggerSyncUseCase
import com.SkrinVex.syncwave.app.domain.usecase.track.DeleteTrackUseCase
import com.SkrinVex.syncwave.app.domain.usecase.track.GetLibraryStatsUseCase
import com.SkrinVex.syncwave.app.domain.usecase.track.GetTracksUseCase
import com.SkrinVex.syncwave.app.player.AudioPlayerManager
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class DependencyContainer(val context: Context) {

    val gson: Gson by lazy {
        GsonBuilder()
            .setLenient()
            .create()
    }

    val sessionDataStore: SessionDataStore by lazy {
        SessionDataStore(context, gson)
    }

    val authInterceptor: AuthInterceptor by lazy {
        AuthInterceptor(sessionDataStore)
    }

    val dynamicBaseUrlInterceptor: DynamicBaseUrlInterceptor by lazy {
        DynamicBaseUrlInterceptor(sessionDataStore)
    }

    val loggingInterceptor: HttpLoggingInterceptor by lazy {
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(dynamicBaseUrlInterceptor)
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    val unauthenticatedOkHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://syncwave.skrinvex.com/") // Initial dummy, overridden dynamically by DynamicBaseUrlInterceptor
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    val apiService: SyncWaveApiService by lazy {
        retrofit.create(SyncWaveApiService::class.java)
    }

    // Repositories
    val authRepository: AuthRepository by lazy {
        AuthRepositoryImpl(apiService, sessionDataStore, unauthenticatedOkHttpClient, gson)
    }

    val trackRepository: TrackRepository by lazy {
        TrackRepositoryImpl(apiService, sessionDataStore)
    }

    val playlistRepository: PlaylistRepository by lazy {
        PlaylistRepositoryImpl(apiService)
    }

    val syncRepository: SyncRepository by lazy {
        SyncRepositoryImpl(apiService)
    }

    val settingsRepository: SettingsRepository by lazy {
        SettingsRepositoryImpl(apiService)
    }

    // Audio Player Manager
    val audioPlayerManager: AudioPlayerManager by lazy {
        AudioPlayerManager(context, sessionDataStore, trackRepository)
    }

    // Use Cases
    val checkAuthStatusUseCase by lazy { CheckAuthStatusUseCase(authRepository) }
    val loginUseCase by lazy { LoginUseCase(authRepository) }
    val setupAdminUseCase by lazy { SetupAdminUseCase(authRepository) }
    val getCurrentUserUseCase by lazy { GetCurrentUserUseCase(authRepository) }
    val getSavedSessionUseCase by lazy { GetSavedSessionUseCase(authRepository) }
    val saveServerUrlUseCase by lazy { SaveServerUrlUseCase(authRepository) }
    val getServerUrlUseCase by lazy { GetServerUrlUseCase(authRepository) }
    val logoutUseCase by lazy { LogoutUseCase(authRepository) }

    val getTracksUseCase by lazy { GetTracksUseCase(trackRepository) }
    val getAllReadyTracksUseCase by lazy { com.SkrinVex.syncwave.app.domain.usecase.track.GetAllReadyTracksUseCase(trackRepository) }
    val getLibraryStatsUseCase by lazy { GetLibraryStatsUseCase(trackRepository) }
    val deleteTrackUseCase by lazy { DeleteTrackUseCase(trackRepository) }

    val getPlaylistsUseCase by lazy { GetPlaylistsUseCase(playlistRepository) }
    val createPlaylistUseCase by lazy { CreatePlaylistUseCase(playlistRepository) }
    val deletePlaylistUseCase by lazy { DeletePlaylistUseCase(playlistRepository) }
    val syncPlaylistUseCase by lazy { SyncPlaylistUseCase(playlistRepository) }

    val getSyncProgressUseCase by lazy { GetSyncProgressUseCase(syncRepository) }
    val getSyncLogsUseCase by lazy { GetSyncLogsUseCase(syncRepository) }
    val triggerSyncUseCase by lazy { TriggerSyncUseCase(syncRepository) }
    val cancelSyncUseCase by lazy { com.SkrinVex.syncwave.app.domain.usecase.sync.CancelSyncUseCase(syncRepository) }
    val clearSyncLogsUseCase by lazy { com.SkrinVex.syncwave.app.domain.usecase.sync.ClearSyncLogsUseCase(syncRepository) }

    val getSettingsUseCase by lazy { GetSettingsUseCase(settingsRepository) }
}
