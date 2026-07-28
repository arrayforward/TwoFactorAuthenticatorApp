package com.otp.authenticator.data

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

class OtpRepository(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "otp_accounts_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    private val _accounts = MutableStateFlow<List<OtpAccount>>(emptyList())
    val accounts: StateFlow<List<OtpAccount>> = _accounts.asStateFlow()

    init {
        loadAccounts()
    }

    fun loadAccounts() {
        val json = prefs.getString(KEY_ACCOUNTS, "[]") ?: "[]"
        val arr = JSONArray(json)
        val list = mutableListOf<OtpAccount>()
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            list.add(
                OtpAccount(
                    id = obj.getString("id"),
                    issuer = obj.getString("issuer"),
                    label = obj.getString("label"),
                    secret = obj.getString("secret"),
                    algorithm = obj.optString("algorithm", "SHA1"),
                    digits = obj.optInt("digits", 6),
                    period = obj.optInt("period", 30)
                )
            )
        }
        _accounts.value = list
    }

    fun addAccount(account: OtpAccount) {
        val list = _accounts.value.toMutableList()
        list.add(account)
        saveList(list)
    }

    fun removeAccount(id: String) {
        val list = _accounts.value.filter { it.id != id }
        saveList(list)
    }

    fun updateAccount(account: OtpAccount) {
        val list = _accounts.value.toMutableList()
        val index = list.indexOfFirst { it.id == account.id }
        if (index >= 0) {
            list[index] = account
            saveList(list)
        }
    }

    private fun saveList(list: List<OtpAccount>) {
        val arr = JSONArray()
        for (a in list) {
            arr.put(
                JSONObject().apply {
                    put("id", a.id)
                    put("issuer", a.issuer)
                    put("label", a.label)
                    put("secret", a.secret)
                    put("algorithm", a.algorithm)
                    put("digits", a.digits)
                    put("period", a.period)
                }
            )
        }
        prefs.edit().putString(KEY_ACCOUNTS, arr.toString()).apply()
        _accounts.value = list
    }

    companion object {
        private const val KEY_ACCOUNTS = "accounts"
    }
}
