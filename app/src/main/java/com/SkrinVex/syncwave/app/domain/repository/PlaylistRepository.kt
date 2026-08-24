package com.SkrinVex.syncwave.app.domain.repository

import com.SkrinVex.syncwave.app.domain.model.Playlist
import com.SkrinVex.syncwave.app.domain.model.Resource

interface PlaylistRepository {
    suspend fun getPlaylists(): Resource<List<Playlist>>
    suspend fun createPlaylist(title: String, urlOrId: String, autoSync: Boolean, syncIntervalMinutes: Int): Resource<Playlist>
    suspend fun updatePlaylist(id: String, title: String, autoSync: Boolean, syncIntervalMinutes: Int): Resource<Playlist>
    suspend fun deletePlaylist(id: String): Resource<Unit>
    suspend fun syncPlaylist(id: String): Resource<Unit>
}
