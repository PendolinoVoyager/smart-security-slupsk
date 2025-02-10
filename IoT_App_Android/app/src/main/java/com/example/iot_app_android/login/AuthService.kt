package com.example.iot_app_android.login

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.logging.Level
import java.util.logging.Logger

object AuthService {
    private const val LOGIN_URL = "http://192.168.0.3:8080/api/v1/auth/login"
    private val logger: Logger = Logger.getLogger(AuthService::class.java.name)

    suspend fun login(context: Context, email: String, password: String): Pair<Boolean, String> {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL(LOGIN_URL)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.doOutput = true

                val json = JSONObject()
                json.put("email", email)
                json.put("password", password)

                val writer = OutputStreamWriter(connection.outputStream)
                writer.write(json.toString())
                writer.flush()
                writer.close()

                val responseCode = connection.responseCode
                val responseMessage = connection.responseMessage

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val responseBuffer = BufferedReader(connection.inputStream.reader())
                    val responseText = responseBuffer.use { it.readText() }
                    responseBuffer.close()

                    val jsonResponse = JSONObject(responseText)
                    val token = jsonResponse.optString("token", "")
                    val username = jsonResponse.optString("email", "")

                    saveToken(context, token)
                    saveUsername(context, username)

                    connection.disconnect()
                    return@withContext Pair(true, "Success!")
                } else {
                    connection.disconnect()
                    return@withContext Pair(false, "Wyjebalo sie: $responseMessage")
                }
            } catch (e: Exception) {
                logger.log(Level.SEVERE, "Chujoza totalna: ${e.localizedMessage}", e)
                return@withContext Pair(false, "chujoza: ${e.localizedMessage}")
            }
        }
    }

    private fun saveToken(context: Context, token: String) {
        val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().putString("authToken", token).apply()
    }

    private fun saveUsername(context: Context, username: String) {
        val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().putString("username", username).apply()
    }

    fun logout(context: Context) {
        val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().clear().apply()
    }
}