package com.evanisnor.handyauth.client

data class HandyAccessToken(
    val token: String = "",
    val tokenType: String = ""
) {
    fun asHeaderValue(): String = "$tokenType $token"
}
