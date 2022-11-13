package com.evanisnor.handyauth.client.internal.state

import android.content.Context
import android.content.SharedPreferences
import com.evanisnor.handyauth.client.internal.state.model.AuthState
import com.evanisnor.handyauth.client.internal.state.model.AuthStateJsonAdapter

class SharedPrefsCache(
  context: Context,
  instanceIdentifier: String,
  private val authStateJsonAdapter: AuthStateJsonAdapter,
) : AuthStateCache {

  companion object {
    private const val STATE_KEY = "authState"
  }

  private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
    "com.evanisnor.handyauth.client.internal.$instanceIdentifier",
    Context.MODE_PRIVATE,
  )

  override fun save(authState: AuthState) {
    sharedPreferences.edit().apply {
      putString(STATE_KEY, authStateJsonAdapter.toJson(authState))
      apply()
    }
  }

  override fun read(): AuthState {
    val stateJson = sharedPreferences.getString(STATE_KEY, null) ?: "{}"
    return authStateJsonAdapter.fromJson(stateJson) ?: AuthState()
  }

  override fun clear() = sharedPreferences.edit().clear().apply()
}
