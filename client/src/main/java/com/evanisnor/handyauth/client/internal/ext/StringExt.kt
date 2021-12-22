package com.evanisnor.handyauth.client.ui

import java.net.URLDecoder


fun String.urlDecode(): String = URLDecoder.decode(this, "UTF-8")