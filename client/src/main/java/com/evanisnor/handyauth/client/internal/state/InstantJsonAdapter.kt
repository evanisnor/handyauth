package com.evanisnor.handyauth.client.internal.state

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonQualifier
import com.squareup.moshi.ToJson
import java.time.Instant

@Retention(AnnotationRetention.RUNTIME)
@JsonQualifier
annotation class EpochMilli

class InstantJsonAdapter {

  @ToJson
  fun toEpochMilli(@EpochMilli instant: Instant): Long = instant.toEpochMilli()

  @FromJson
  @EpochMilli
  fun toInstant(epochMilli: Long): Instant = Instant.ofEpochMilli(epochMilli)
}
