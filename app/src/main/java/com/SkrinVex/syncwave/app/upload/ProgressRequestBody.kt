package com.SkrinVex.syncwave.app.upload

import android.content.ContentResolver
import android.net.Uri
import okhttp3.MediaType
import okhttp3.RequestBody
import okio.BufferedSink
import okio.Source
import okio.source
import java.io.IOException

class ProgressRequestBody(
    private val contentResolver: ContentResolver,
    private val uri: Uri,
    private val contentType: MediaType?,
    private val totalLength: Long,
    private val onProgress: (bytesWritten: Long, totalBytes: Long) -> Unit
) : RequestBody() {

    override fun contentType(): MediaType? = contentType

    override fun contentLength(): Long = totalLength

    @Throws(IOException::class)
    override fun writeTo(sink: BufferedSink) {
        val inputStream = contentResolver.openInputStream(uri)
            ?: throw IOException("Cannot open input stream for URI: $uri")

        val source: Source = inputStream.source()
        var totalBytesWritten: Long = 0
        val buffer = okio.Buffer()
        val segmentSize = 8192L // 8KB chunks

        try {
            var read: Long
            while (source.read(buffer, segmentSize).also { read = it } != -1L) {
                sink.write(buffer, read)
                sink.flush()
                totalBytesWritten += read
                onProgress(totalBytesWritten, totalLength)
            }
        } finally {
            try {
                source.close()
            } catch (_: Exception) {}
        }
    }
}
