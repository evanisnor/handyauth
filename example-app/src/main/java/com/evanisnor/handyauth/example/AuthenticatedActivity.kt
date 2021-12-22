package com.evanisnor.handyauth.example

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.evanisnor.handyauth.client.HandyAuth
import com.evanisnor.handyauth.databinding.AuthenticatedActivityBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AuthenticatedActivity : AppCompatActivity() {

    @Inject
    lateinit var handyAuth: HandyAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AuthenticatedActivityBinding.inflate(layoutInflater).apply {
            logout.setOnClickListener {
                lifecycleScope.launch {
                    handyAuth.logout()
                    startActivity(Intent(baseContext, MainActivity::class.java))
                }
            }

            setContentView(root)
        }
    }
}