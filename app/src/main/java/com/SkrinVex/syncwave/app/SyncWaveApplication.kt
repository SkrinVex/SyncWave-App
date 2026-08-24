package com.SkrinVex.syncwave.app

import android.app.Application
import com.SkrinVex.syncwave.app.di.DependencyContainer

class SyncWaveApplication : Application() {

    lateinit var container: DependencyContainer
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        container = DependencyContainer(this)
    }

    companion object {
        lateinit var instance: SyncWaveApplication
            private set
    }
}
