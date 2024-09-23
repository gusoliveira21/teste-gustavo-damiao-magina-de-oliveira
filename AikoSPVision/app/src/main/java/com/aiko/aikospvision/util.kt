package com.aiko.aikospvision

import com.aiko.domain.model.Parada
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

object util {
    val moshi = Moshi.Builder().add(
        KotlinJsonAdapterFactory()
    ).build()
    val paradaAdapter = moshi.adapter(Parada::class.java)
}