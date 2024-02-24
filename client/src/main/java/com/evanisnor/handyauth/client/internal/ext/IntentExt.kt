package com.evanisnor.handyauth.client.internal.ext

import android.content.Intent
import android.os.Build
import android.os.Parcelable
import kotlin.reflect.KClass

fun <T : Parcelable> Intent.getParcelableExtraCompat(name: String, clazz: KClass<T>) =
  if (Build.VERSION.SDK_INT > Build.VERSION_CODES.TIRAMISU) {
    getParcelableExtra(name, clazz.java)
  } else {
    @Suppress("DEPRECATION")
    getParcelableExtra(name)
  }
