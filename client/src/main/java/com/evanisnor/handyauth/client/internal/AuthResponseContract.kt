package com.evanisnor.handyauth.client.internal

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.evanisnor.handyauth.client.internal.model.AuthRequest
import com.evanisnor.handyauth.client.internal.model.AuthResponse
import com.evanisnor.handyauth.client.ui.HandyAuthActivity

class AuthResponseContract : ActivityResultContract<AuthRequest, AuthResponse?>() {
    override fun createIntent(context: Context, input: AuthRequest): Intent =
        Intent(context, HandyAuthActivity::class.java).apply {
            putExtra(HandyAuthActivity.authorizationRequestExtra, input)
        }

    override fun parseResult(resultCode: Int, intent: Intent?): AuthResponse? = when (resultCode) {
        Activity.RESULT_OK -> intent?.getParcelableExtra(HandyAuthActivity.authorizationResponseExtra)
        Activity.RESULT_CANCELED -> null
        else -> null
    }
}