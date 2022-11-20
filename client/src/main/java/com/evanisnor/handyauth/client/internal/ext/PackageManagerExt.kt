package com.evanisnor.handyauth.client.internal.ext

import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Build

fun PackageManager.queryIntentActivitiesCompat(
  intent: Intent,
  flags: Int,
): MutableList<ResolveInfo> =
  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(flags.toLong()))
  } else {
    @Suppress("DEPRECATION")
    queryIntentActivities(intent, flags)
  }

fun PackageManager.resolveServiceCompat(
  intent: Intent,
  flags: Int,
): ResolveInfo? =
  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    resolveService(intent, PackageManager.ResolveInfoFlags.of(flags.toLong()))
  } else {
    @Suppress("DEPRECATION")
    resolveService(intent, flags)
  }
