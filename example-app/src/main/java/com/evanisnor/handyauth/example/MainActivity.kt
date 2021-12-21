package com.evanisnor.handyauth.example

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.evanisnor.handyauth.R
import com.evanisnor.handyauth.client.HandyAuth
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var handyAuth: HandyAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        if (!handyAuth.isAuthorized) {
            handyAuth.authorize(this)
        } else {
            startActivity(Intent(this, AuthenticatedActivity::class.java))
        }
    }


}