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


class AudioManager (mediaProjection: MediaProjection){
    companion object AudioProperties {
        val matchingUsage = AudioAttributes.USAGE_MEDIA
        val channelMask = AudioFormat.CHANNEL_IN_MONO
        val encoding = AudioFormat.ENCODING_PCM_16BIT
        val sampleRate = 48000
        val bufferSizeInBytes = 128
    }

    private val audioConfiguration = AudioPlaybackCaptureConfiguration.Builder(mediaProjection)
        .addMatchingUsage(matchingUsage)
        .build()

    private val audioFormat = AudioFormat.Builder()
        .setChannelMask(channelMask)
        .setEncoding(encoding)
        .setSampleRate(sampleRate)
        .build()

    var audioRecord: AudioRecord? = null

    fun setAudioRecord(context: Context) {
        if (!checkPermission(context)) {
            Toast.makeText(context, "Record audio permission not granted.", Toast.LENGTH_LONG).show()
            return
        }

        audioRecord = AudioRecord.Builder()
            .setBufferSizeInBytes(bufferSizeInBytes)
            .setAudioPlaybackCaptureConfig(audioConfiguration)
            .setAudioFormat(audioFormat)
            .build()
    }

    private fun checkPermission(context: Context): Boolean {
        val result = ActivityCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
        return result == PackageManager.PERMISSION_GRANTED
    }

}