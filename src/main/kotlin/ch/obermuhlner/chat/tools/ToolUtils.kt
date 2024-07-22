package ch.obermuhlner.chat.tools

import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request

fun simpleGetRequest(client: OkHttpClient, url: HttpUrl): String {
    val request = Request.Builder()
        .url(url)
        .build()

    val response = client.newCall(request).execute()

    if (response.isSuccessful) {
        val responseData = response.body?.string()
        return responseData ?: "No response"
    } else {
        return "Request failed: ${response.code}"
    }
}
