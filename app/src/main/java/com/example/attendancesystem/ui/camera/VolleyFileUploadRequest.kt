package com.example.attendancesystem.ui.camera

import com.android.volley.*
import com.android.volley.toolbox.HttpHeaderParser
import java.io.*
import kotlin.math.min


open class VolleyFileUploadRequest(method: Int, url: String?,errList:Response.Listener<NetworkResponse>, errorListener: Response.ErrorListener?) : Request<NetworkResponse>(method, url, errorListener) {


    internal val twoHyphens = "--"
    private val lineEnd = "\r\n"
    internal var mListener: Response.Listener<NetworkResponse>
    internal var mErrorListener: Response.ErrorListener
    lateinit var mHeaders :Map<String, String>

    internal var boundary = "apiclient-" + System.currentTimeMillis()
    init {
        this.mListener = errList
        this.mErrorListener = errorListener!!

    }
    override fun parseNetworkResponse(response: NetworkResponse?): Response<NetworkResponse> {

        try {
            return Response.success(response, HttpHeaderParser.parseCacheHeaders(response))
        } catch (e: Exception) {
            return Response.error(ParseError(e))
        }

    }

    override fun deliverResponse(response: NetworkResponse?) {

        mListener.onResponse(response)
    }


    @Throws(AuthFailureError::class)
    override fun getHeaders(): Map<String, String> {
        return mHeaders
    }

    override fun getBodyContentType(): String {
        return "multipart/form-data;boundary=$boundary"
    }


    @Throws(AuthFailureError::class)
    override fun getBody(): ByteArray? {
        val bos = ByteArrayOutputStream()
        val dos = DataOutputStream(bos)

        try {
            // populate text payload
            val params = params
            if (params != null && params.size > 0) {
                textParse(dos, params, paramsEncoding)
            }

            // populate data byte payload
            val data = getByteData()
            if (data != null && data!!.size > 0) {
                dataParse(dos, data!!)
            }

            // close multipart form data after text and file data
            dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd)

            return bos.toByteArray()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return null
    }

    @Throws(IOException::class)
    private fun textParse(dataOutputStream: DataOutputStream, params: Map<String, String>, encoding: String) {
        try {
            for ((key, value) in params) {
                buildTextPart(dataOutputStream, key, value)
            }
        } catch (uee: UnsupportedEncodingException) {
            throw RuntimeException("Encoding not supported: $encoding", uee)
        }

    }
    @Throws(AuthFailureError::class)
    protected open fun getByteData(): Map<String, DataPart>? {
        return null
    }

    @Throws(IOException::class)
    private fun dataParse(dataOutputStream: DataOutputStream, data: Map<String, DataPart>) {
        for ((key, value) in data) {
            buildDataPart(dataOutputStream, value, key)
        }
    }


    @Throws(IOException::class)
    private fun buildDataPart(dataOutputStream: DataOutputStream, dataFile: DataPart, inputName: String) {
        dataOutputStream.writeBytes(twoHyphens + boundary + lineEnd)

        dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"" +
                inputName + "\"; filename=\"" + dataFile.fileName + "\"" + lineEnd)
        if (!dataFile.type.trim({ it <= ' ' }).isEmpty()) {
            dataOutputStream.writeBytes("Content-Type: " + dataFile.type + lineEnd)
        }
        dataOutputStream.writeBytes(lineEnd)

        val fileInputStream = ByteArrayInputStream(dataFile.content)
        var bytesAvailable = fileInputStream.available()

        val maxBufferSize = 1024 * 1024
        var bufferSize = Math.min(bytesAvailable, maxBufferSize)
        val buffer = ByteArray(bufferSize)

        var bytesRead = fileInputStream.read(buffer, 0, bufferSize)

        while (bytesRead > 0) {
            dataOutputStream.write(buffer, 0, bufferSize)
            bytesAvailable = fileInputStream.available()
            bufferSize = Math.min(bytesAvailable, maxBufferSize)
            bytesRead = fileInputStream.read(buffer, 0, bufferSize)
        }

        dataOutputStream.writeBytes(lineEnd)
    }


    @Throws(IOException::class)
    private fun buildTextPart(dataOutputStream: DataOutputStream, parameterName: String, parameterValue: String) {
        dataOutputStream.writeBytes(twoHyphens + boundary + lineEnd)
        dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"$parameterName\"$lineEnd")
        dataOutputStream.writeBytes(lineEnd)
        dataOutputStream.writeBytes(parameterValue + lineEnd)
    }


    class DataPart (
        var fileName: String,
        var content: ByteArray,
        var type: String
    )

}

