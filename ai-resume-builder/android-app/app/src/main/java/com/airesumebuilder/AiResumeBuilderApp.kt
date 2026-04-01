package com.airesumebuilder

import android.app.Application
import com.airesumebuilder.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class AiResumeBuilderApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@AiResumeBuilderApp)
            modules(appModule)
        }
    }
}
