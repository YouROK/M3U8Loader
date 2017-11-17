package ru.yourok.dwl.client

import ru.yourok.dwl.settings.Settings
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.HttpURLConnection.*
import java.net.URL


/**
 * Created by yourok on 07.11.17.
 */
class Http(url: String) : Client {
    private var currUrl: String = url
    private var isConn: Boolean = false
    private var connection: HttpURLConnection? = null
    private var errMsg: String = ""

    override fun connect() {
        var responseCode: Int
        var redirCount = 0
        do {
            var url = URL(currUrl)
            connection = url.openConnection() as HttpURLConnection
            connection!!.setConnectTimeout(1000)
            connection!!.setReadTimeout(5000)
            connection!!.setRequestMethod("GET")
            connection!!.setDoInput(true)

            connection!!.setRequestProperty("UserAgent", "DWL/1.1.0 (Android)")
            connection!!.setRequestProperty("Accept", "*/*")
            connection!!.setRequestProperty("Accept-Encoding", "gzip")

            if (Settings.headers.isNotEmpty()) {
                Settings.headers.forEach { (k, v) ->
                    connection!!.setRequestProperty(k, v)
                }
            }

            connection!!.connect()
            responseCode = connection!!.getResponseCode()
            val redirected = responseCode == HTTP_MOVED_PERM || responseCode == HTTP_MOVED_TEMP || responseCode == HTTP_SEE_OTHER
            if (redirected) {
                currUrl = connection!!.getHeaderField("Location")
                connection!!.disconnect()
                redirCount++
            }
            if (redirCount > 5) {
                throw IOException("Error connect to: " + currUrl + " too many redirects")
            }
        } while (redirected)


        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw IOException("Error connect to: " + currUrl + " " + connection!!.responseMessage)
        }
        isConn = true
    }

    override fun isConnected(): Boolean {
        return isConn
    }

    override fun getSize(): Long {
        if (!isConn)
            return 0
        var cl = connection!!.getHeaderField("Content-Length")
        try {
            if (!cl.isNullOrEmpty()) {
                return cl.toLong()
            }
        } catch (e: Exception) {
        }

        cl = connection!!.getHeaderField("Content-Range")
        try {
            if (!cl.isNullOrEmpty()) {
                val cr = cl.split("/")
                if (cr.isNotEmpty())
                    cl = cr.last()
                return cl.toLong()
            }
        } catch (e: Exception) {
        }
        return 0
    }

    override fun getUrl(): String {
        return currUrl
    }

    override fun getInputStream(): InputStream? {
        return connection!!.inputStream
    }

    override fun getErrorMessage(): String {
        return errMsg
    }

    override fun close() {
        try {
            connection?.inputStream?.close()
        } catch (e: Exception) {
        }
        connection?.disconnect()
        isConn = false
    }
}