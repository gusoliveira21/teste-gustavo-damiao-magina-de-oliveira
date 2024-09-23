package com.aiko.aikospvision.di

import com.aiko.aikospvision.ui.screens_app.home.ViewModelHome
import com.aiko.aikospvision.ui.screens_app.home.ViewModelHomeImpl
import com.aiko.aikospvision.ui.screens_app.stops.ViewModelStops
import com.aiko.aikospvision.ui.screens_app.stops.ViewModelStopsImpl
import com.aiko.data.remote.VisionService
import com.aiko.data.remote.adapters.internal.NetworkResultCallAdapterFactory
import com.aiko.data.repository.SPVisionRepositoryImpl
import com.aiko.domain.repository.SPVisionRepository
import com.aiko.domain.usecase.GetForecastUseCase
import com.aiko.domain.usecase.GetLinesUseCase
import com.aiko.domain.usecase.GetStopsBySearchTermUseCase
import com.aiko.domain.usecase.GetVehiclePositionUseCase
import com.aiko.domain.usecase.PostAuthUseCase
import com.aiko.domain.usecase.SearchLinesUseCase
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import net.gotev.cookiestore.okhttp.JavaNetCookieJar
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.net.CookieManager


val mainModule = module {

    factory { GetForecastUseCase(get()) }
    factory { GetLinesUseCase(get()) }
    factory { GetStopsBySearchTermUseCase(get()) }
    factory { GetVehiclePositionUseCase(get()) }
    factory { SearchLinesUseCase(get()) }
    factory { PostAuthUseCase(get()) }

    single<SPVisionRepository> { SPVisionRepositoryImpl(get()) }

    single {
        Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    single<VisionService> { get<Retrofit>().create(VisionService::class.java) }

    single {

        val apiKey = "8df2ebbc7fed5723047bf5505576bc6f3986f9f6e43e1efc0b40086fc4e438c1"
        val cookieManager = CookieManager()

        val authInterceptor = Interceptor { chain ->
            val original = chain.request()
            val requestBuilder = original.newBuilder().header("Authorization", "Bearer $apiKey")
            val request = requestBuilder.build()
            chain.proceed(request)
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .cookieJar(JavaNetCookieJar(cookieManager))
            .build()

        Retrofit.Builder()
            .baseUrl("https://api.olhovivo.sptrans.com.br/v2.1/")
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(get()))
            .addCallAdapterFactory(NetworkResultCallAdapterFactory.create())
            .build()
    }

    viewModel<ViewModelHome> { ViewModelHomeImpl(get(), get()) }
    viewModel<ViewModelStops> { ViewModelStopsImpl(get(), get()) }

}