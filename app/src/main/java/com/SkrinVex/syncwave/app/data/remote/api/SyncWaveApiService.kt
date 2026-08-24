package com.SkrinVex.syncwave.app.data.remote.api

import com.SkrinVex.syncwave.app.data.remote.dto.AuthRequestDto
import com.SkrinVex.syncwave.app.data.remote.dto.AuthResponseDto
import com.SkrinVex.syncwave.app.data.remote.dto.AuthStatusDto
import com.SkrinVex.syncwave.app.data.remote.dto.BatchDeleteRequestDto
import com.SkrinVex.syncwave.app.data.remote.dto.CreatePlaylistRequestDto
import com.SkrinVex.syncwave.app.data.remote.dto.PlaylistDto
import com.SkrinVex.syncwave.app.data.remote.dto.SyncLogDto
import com.SkrinVex.syncwave.app.data.remote.dto.SyncProgressDto
import com.SkrinVex.syncwave.app.data.remote.dto.SystemSettingsDto
import com.SkrinVex.syncwave.app.data.remote.dto.TrackDto
import com.SkrinVex.syncwave.app.data.remote.dto.TrackListResponseDto
import com.SkrinVex.syncwave.app.data.remote.dto.TrackStatsDto
import com.SkrinVex.syncwave.app.data.remote.dto.UpdatePlaylistRequestDto
import com.SkrinVex.syncwave.app.data.remote.dto.UserDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface SyncWaveApiService {

    // Auth
    @GET("/api/v1/auth/status")
    suspend fun getAuthStatus(): Response<AuthStatusDto>

    @POST("/api/v1/auth/setup")
    suspend fun setupAdmin(@Body body: AuthRequestDto): Response<AuthResponseDto>

    @POST("/api/v1/auth/login")
    suspend fun login(@Body body: AuthRequestDto): Response<AuthResponseDto>

    @GET("/api/v1/auth/me")
    suspend fun getMe(): Response<UserDto>

    // Tracks
    @GET("/api/v1/tracks")
    suspend fun getTracks(
        @Query("q") query: String? = null,
        @Query("playlist_id") playlistId: String? = null,
        @Query("status") status: String? = null,
        @Query("sort_by") sortBy: String? = "created_at",
        @Query("order") order: String? = "desc",
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 50
    ): Response<TrackListResponseDto>

    @GET("/api/v1/tracks/ready")
    suspend fun getAllReadyTracks(
        @Query("playlist_id") playlistId: String? = null
    ): Response<List<TrackDto>>

    @GET("/api/v1/tracks/stats")
    suspend fun getTrackStats(): Response<TrackStatsDto>

    @GET("/api/v1/tracks/{id}")
    suspend fun getTrack(@Path("id") id: String): Response<TrackDto>

    @DELETE("/api/v1/tracks/{id}")
    suspend fun deleteTrack(@Path("id") id: String): Response<Map<String, String>>

    @POST("/api/v1/tracks/batch-delete")
    suspend fun batchDeleteTracks(@Body body: BatchDeleteRequestDto): Response<Map<String, String>>

    // Playlists
    @GET("/api/v1/playlists")
    suspend fun getPlaylists(): Response<List<PlaylistDto>>

    @POST("/api/v1/playlists")
    suspend fun createPlaylist(@Body body: CreatePlaylistRequestDto): Response<PlaylistDto>

    @GET("/api/v1/playlists/{id}")
    suspend fun getPlaylist(@Path("id") id: String): Response<PlaylistDto>

    @PUT("/api/v1/playlists/{id}")
    suspend fun updatePlaylist(
        @Path("id") id: String,
        @Body body: UpdatePlaylistRequestDto
    ): Response<PlaylistDto>

    @DELETE("/api/v1/playlists/{id}")
    suspend fun deletePlaylist(@Path("id") id: String): Response<Map<String, String>>

    @POST("/api/v1/playlists/{id}/sync")
    suspend fun syncPlaylist(@Path("id") id: String): Response<Map<String, String>>

    // Sync
    @POST("/api/v1/sync/trigger")
    suspend fun triggerSyncAll(): Response<Map<String, String>>

    @POST("/api/v1/sync/cancel")
    suspend fun cancelSync(): Response<Map<String, String>>

    @GET("/api/v1/sync/progress")
    suspend fun getSyncProgress(): Response<SyncProgressDto>

    @GET("/api/v1/sync/logs")
    suspend fun getSyncLogs(@Query("limit") limit: Int = 100): Response<List<SyncLogDto>>

    @DELETE("/api/v1/sync/logs")
    suspend fun clearSyncLogs(): Response<Map<String, String>>

    // Settings
    @GET("/api/v1/settings")
    suspend fun getSettings(): Response<SystemSettingsDto>
}
