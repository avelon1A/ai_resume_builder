package com.airesumebuilder.di

import com.airesumebuilder.BuildConfig
import com.airesumebuilder.data.remote.api.ApiService
import com.airesumebuilder.data.repository.AuthRepositoryImpl
import com.airesumebuilder.data.repository.ResumeRepositoryImpl
import com.airesumebuilder.domain.repository.AuthRepository
import com.airesumebuilder.domain.repository.ResumeRepository
import com.airesumebuilder.presentation.viewmodel.AuthViewModel
import com.airesumebuilder.presentation.viewmodel.ResumeViewModel
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

val appModule = module {

    single {
        OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    single {
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL + "/")
            .client(get())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    single<AuthRepository> { AuthRepositoryImpl(get(), androidContext()) }

    single<ResumeRepository> { ResumeRepositoryImpl(get(), androidContext()) }

    viewModel { AuthViewModel(get()) }

    viewModel { ResumeViewModel(get()) }
}
