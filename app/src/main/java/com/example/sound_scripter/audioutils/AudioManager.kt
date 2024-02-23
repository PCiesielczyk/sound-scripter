package com.example.sound_scripter.audioutils


import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioPlaybackCaptureConfiguration
import android.media.AudioRecord
import android.media.projection.MediaProjection
import android.widget.Toast
import androidx.core.app.ActivityCompat


class AudioManager (mediaProjection: MediaProjection) : Thread() {
    companion object AudioProperties {
        const val MATCHING_USAGE = AudioAttributes.USAGE_MEDIA
        const val CHANNEL_MASK = AudioFormat.CHANNEL_IN_MONO
        const val ENCODING = AudioFormat.ENCODING_PCM_16BIT
        const val SAMPLE_RATE = 48000
        const val BUFFER_SIZE_IN_BYTES = 128
    }

    private var enabled = true
    var bytesRead: Int? = 0

    private val audioConfiguration = AudioPlaybackCaptureConfiguration.Builder(mediaProjection)
        .addMatchingUsage(MATCHING_USAGE)
        .build()

    private val audioFormat = AudioFormat.Builder()
        .setChannelMask(CHANNEL_MASK)
        .setEncoding(ENCODING)
        .setSampleRate(SAMPLE_RATE)
        .build()

    var audioRecord: AudioRecord? = null

    override fun run() {
        super.run()

        val buffer = ByteArray(BUFFER_SIZE_IN_BYTES)
        audioRecord?.startRecording()

        while (enabled) {
            bytesRead = audioRecord?.read(buffer, 0, buffer.size)
        }
        audioRecord?.stop()
        audioRecord?.release()
    }

    fun stopRecording() {
        enabled = false
    }

    fun setAudioRecord(context: Context) {
        if (!checkPermission(context)) {
            Toast.makeText(context, "Record audio permission not granted.", Toast.LENGTH_LONG).show()
            return
        }

        audioRecord = AudioRecord.Builder()
            .setBufferSizeInBytes(BUFFER_SIZE_IN_BYTES)
            .setAudioPlaybackCaptureConfig(audioConfiguration)
            .setAudioFormat(audioFormat)
            .build()
    }

    private fun checkPermission(context: Context): Boolean {
        val result = ActivityCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
        return result == PackageManager.PERMISSION_GRANTED
    }

}