package com.evanisnor.handyauth.client.internal.state

import com.evanisnor.handyauth.client.internal.state.model.AuthState
import java.util.concurrent.atomic.AtomicReference

class MemoryCache(
    private val persistentCache: AuthStateCache
) : AuthStateCache {

    private val authState: AtomicReference<AuthState> = AtomicReference()

    override fun save(authState: AuthState) {
        persistentCache.save(authState)
        this.authState.set(authState)
    }

    override fun read(): AuthState {
        if (authState.get() == null) {
            val savedAuthState = persistentCache.read()
            authState.set(savedAuthState)
        }
        return authState.get()
    }

    override fun clear() {
        persistentCache.clear()
        authState.set(null)
    }

}