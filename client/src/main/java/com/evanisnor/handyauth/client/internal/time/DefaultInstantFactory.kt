package com.evanisnor.handyauth.client.internal.time

import java.time.Instant

class DefaultInstantFactory : InstantFactory {
  override fun now(): Instant = Instant.now()
}
