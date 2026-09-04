package com.epichypernova.scoretracker

import android.app.Application
import com.epichypernova.scoretracker.data.Repository
import com.epichypernova.scoretracker.data.ScoreStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/** Minimal service locator for the app-scoped [Repository]. */
object ServiceLocator {
    lateinit var repository: Repository
        private set

    fun init(app: Application) {
        if (::repository.isInitialized) return
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        repository = Repository(ScoreStore(app.applicationContext), scope)
    }
}

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        ServiceLocator.init(this)
    }
}
