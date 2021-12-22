package com.evanisnor.handyauth.example

import android.content.Intent
import android.os.Bundle
import android.util.Log
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
            handyAuth.authorize(this) { result ->
                when (result) {
                    is HandyAuth.Result.Success -> {
                        onAuthenticated()
                    }
                    is HandyAuth.Result.Error -> {
                        Log.e("ExampleApp", result.error.toString())
                        onError()
                    }
                }
            }
        } else {
            onAuthenticated()
        }
    }

    private fun onAuthenticated() {
        startActivity(Intent(this, AuthenticatedActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        })
    }

    private fun onError() {
        startActivity(Intent(this, LoginErrorActivity::class.java))
    }


}