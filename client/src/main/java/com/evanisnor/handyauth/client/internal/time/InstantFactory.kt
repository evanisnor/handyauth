package com.evanisnor.handyauth.client.internal.time

import java.time.Instant

interface InstantFactory {
  fun now(): Instant
}
