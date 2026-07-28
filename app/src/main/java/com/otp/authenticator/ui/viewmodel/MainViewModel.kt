package com.otp.authenticator.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.otp.authenticator.OtpApp
import com.otp.authenticator.data.OtpAccount
import com.otp.authenticator.data.OtpRepository
import com.otp.authenticator.otp.TotpGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class OtpDisplayItem(
    val account: OtpAccount,
    val code: String,
    val remainingSeconds: Int,
    val progress: Float
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: OtpRepository = (application as OtpApp).repository

    private val _displayItems = MutableStateFlow<List<OtpDisplayItem>>(emptyList())
    val displayItems: StateFlow<List<OtpDisplayItem>> = _displayItems.asStateFlow()

    private val _scannedOtpUrl = MutableStateFlow<String?>(null)
    val scannedOtpUrl: StateFlow<String?> = _scannedOtpUrl.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var tickJob: Job? = null

    val accounts: StateFlow<List<OtpAccount>> = repository.accounts

    init {
        startTicking()
    }

    fun startTicking() {
        tickJob?.cancel()
        tickJob = viewModelScope.launch(Dispatchers.Default) {
            while (true) {
                updateCodes()
                delay(1000)
            }
        }
    }

    private fun updateCodes() {
        val now = System.currentTimeMillis()
        _displayItems.value = repository.accounts.value.map { account ->
            val secretBytes = try {
                TotpGenerator.decodeBase32(account.secret)
            } catch (_: Exception) {
                byteArrayOf()
            }
            val code = if (secretBytes.isNotEmpty()) {
                TotpGenerator.generate(secretBytes, now)
            } else {
                "------"
            }
            OtpDisplayItem(
                account = account,
                code = code,
                remainingSeconds = TotpGenerator.remainingSeconds(now),
                progress = TotpGenerator.progressFraction(now)
            )
        }
    }

    fun addAccount(account: OtpAccount) {
        repository.addAccount(account)
    }

    fun removeAccount(id: String) {
        repository.removeAccount(id)
    }

    fun setScannedUrl(url: String) {
        _scannedOtpUrl.value = url
    }

    fun clearScannedUrl() {
        _scannedOtpUrl.value = null
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun parseOtpAuthUrl(url: String): OtpAccount? {
        return try {
            if (!url.startsWith("otpauth://totp/") && !url.startsWith("otpauth://hotp/")) {
                return null
            }
            val withoutScheme = url.removePrefix("otpauth://totp/").removePrefix("otpauth://hotp/")
            val queryIndex = withoutScheme.indexOf('?')
            if (queryIndex < 0) return null

            val labelPart = withoutScheme.substring(0, queryIndex)
            val queryPart = withoutScheme.substring(queryIndex + 1)

            val params = mutableMapOf<String, String>()
            for (param in queryPart.split("&")) {
                val eq = param.indexOf('=')
                if (eq > 0) {
                    val key = java.net.URLDecoder.decode(param.substring(0, eq), "UTF-8")
                    val value = java.net.URLDecoder.decode(param.substring(eq + 1), "UTF-8")
                    params[key] = value
                }
            }

            val secret = params["secret"] ?: return null
            val issuer = params["issuer"] ?: ""
            val label = labelPart.replace("%20", " ").trim()
            val digits = params["digits"]?.toIntOrNull() ?: 6
            val period = params["period"]?.toIntOrNull() ?: 30
            val algorithm = params["algorithm"] ?: "SHA1"

            OtpAccount(
                issuer = issuer,
                label = label,
                secret = secret,
                algorithm = algorithm,
                digits = digits,
                period = period
            )
        } catch (_: Exception) {
            null
        }
    }

    override fun onCleared() {
        super.onCleared()
        tickJob?.cancel()
    }
}
