package com.evanisnor.handyauth.client.util

import com.evanisnor.handyauth.client.internal.time.InstantFactory
import java.time.Instant

class TestInstantFactory : InstantFactory {

    var now: Instant = Instant.ofEpochMilli(0)

    override fun now(): Instant = now

}