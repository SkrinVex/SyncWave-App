package com.SkrinVex.syncwave.app

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import com.SkrinVex.syncwave.app.di.DependencyContainer

class SyncWaveApplication : Application(), ImageLoaderFactory {

    lateinit var container: DependencyContainer
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        container = DependencyContainer(this)
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.30)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizeBytes(100L * 1024 * 1024) // 100 MB
                    .build()
            }
            .diskCachePolicy(CachePolicy.ENABLED)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .respectCacheHeaders(false)
            .allowHardware(true)
            .crossfade(true)
            .build()
    }

    companion object {
        lateinit var instance: SyncWaveApplication
            private set
    }
}
