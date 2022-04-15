package com.evanisnor.handyauth.client.ext

import java.net.URLEncoder


fun String.urlEncode(): String = URLEncoder.encode(this, "UTF-8")