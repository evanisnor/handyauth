package com.evanisnor.handyauth.client.internal.state

import com.evanisnor.handyauth.client.internal.state.model.AuthState

interface AuthStateCache {

  fun save(authState: AuthState)

  fun read(): AuthState

  fun clear()
}
