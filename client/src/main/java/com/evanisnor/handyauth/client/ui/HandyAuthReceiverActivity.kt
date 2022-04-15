package com.evanisnor.handyauth.client.ui

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

/**
 * Receiver Activity for intercepting OAuth Redirects from the server
 */
class HandyAuthReceiverActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        HandyAuthActivity.startWithResponseUri(this, intent.data ?: Uri.EMPTY)
        finish()
    }

}