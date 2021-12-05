package com.evanisnor.handyauth.client.internal.secure

import okio.ByteString.Companion.encodeUtf8
import java.security.SecureRandom
import kotlin.random.Random
import kotlin.random.asKotlinRandom

class CodeGenerator(
    private val random: Random = SecureRandom().asKotlinRandom()
) {

    companion object {
        const val codeChars = "-._~0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
    }

    /**
     * Generate a secure random string of the specified length using [codeChars]
     */
    fun generate(length: Int): String = StringBuilder().apply {
        for (i in 0 until length) {
            append(codeChars[random.nextInt(codeChars.length)])
        }
    }.toString()

    /**
     * Generate a code challenge from a code verifier string
     * Code Verifier -> SHA256 -> Base64Url
     */
    fun codeChallenge(codeVerifier: String): String =
        codeVerifier.encodeUtf8()
            .sha256()
            .base64Url()
            .replace("=", "")
}