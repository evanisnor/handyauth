package com.evanisnor.handyauth.client.internal.ext

import java.net.URLDecoder

fun String.urlDecode(): String = URLDecoder.decode(this, "UTF-8")
