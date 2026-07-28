package com.otp.authenticator

import android.app.Application
import com.otp.authenticator.data.OtpRepository

class OtpApp : Application() {
    lateinit var repository: OtpRepository
        private set

    override fun onCreate() {
        super.onCreate()
        repository = OtpRepository(this)
    }
}
