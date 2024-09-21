package com.aiko.aikospvision.di

import com.aiko.data.remote.VisionService
import com.aiko.data.remote.adapters.internal.NetworkResultCallAdapterFactory
import com.aiko.data.repository.SPVisionRepositoryImpl
import com.aiko.domain.repository.SPVisionRepository
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

val mainModule = module {

    single<SPVisionRepository> { SPVisionRepositoryImpl(get()) }

    single {
        Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    single<VisionService> { get<Retrofit>().create(VisionService::class.java) }


    single {
        val apiKey = "KEY"

        val authInterceptor = Interceptor { chain ->
            val original = chain.request()
            val requestBuilder = original.newBuilder()
                .header("Authorization", "Bearer $apiKey")
            val request = requestBuilder.build()
            chain.proceed(request)
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .build()

        Retrofit.Builder()
            .baseUrl("https://api.olhovivo.sptrans.com.br/v2.1/")
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(get()))
            .addCallAdapterFactory(NetworkResultCallAdapterFactory.create())
            .build()
    }
}