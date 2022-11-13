package com.evanisnor.handyauth.client.fakes

import com.evanisnor.handyauth.client.internal.HandyAuthComponent
import java.io.Closeable

internal class TestHandyAuthComponent(
  private val handyAuthComponent: HandyAuthComponent,
) : Closeable {

  val handyAuth = handyAuthComponent.handyAuth

  val memoryCache = handyAuthComponent.memoryCache

  override fun close() {
    handyAuthComponent.persistentCache.clear()
  }
}
