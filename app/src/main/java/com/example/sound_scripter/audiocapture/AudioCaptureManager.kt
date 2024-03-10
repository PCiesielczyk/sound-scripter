package com.example.sound_scripter.audiocapture


import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioPlaybackCaptureConfiguration
import android.media.AudioRecord
import android.media.projection.MediaProjection
import android.os.Environment
import android.widget.Toast
import androidx.core.app.ActivityCompat
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException


class AudioCaptureManager (mediaProjection: MediaProjection) : Runnable {
    companion object AudioProperties {
        const val CHANNEL_MASK = AudioFormat.CHANNEL_IN_MONO
        const val ENCODING = AudioFormat.ENCODING_PCM_16BIT
        const val SAMPLE_RATE = 16000
        val bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_MASK, ENCODING)
    }

    private val audioConfiguration = AudioPlaybackCaptureConfiguration.Builder(mediaProjection)
        .addMatchingUsage(AudioAttributes.USAGE_UNKNOWN)
        .addMatchingUsage(AudioAttributes.USAGE_MEDIA)
        .addMatchingUsage(AudioAttributes.USAGE_GAME)
        .build()

    private val audioFormat = AudioFormat.Builder()
        .setChannelMask(CHANNEL_MASK)
        .setEncoding(ENCODING)
        .setSampleRate(SAMPLE_RATE)
        .build()

    private var audioRecord: AudioRecord? = null
    private var capturingEnabled = true

    var dataRead = ByteArray(bufferSize / 2)

    override fun run() {
        val fileName =  Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath + "/capturedAudio2" + ".pcm"
        audioRecord?.startRecording()
        capturingEnabled = true

        val outputStream: FileOutputStream?
        try {
            outputStream = FileOutputStream(fileName)
        } catch (e: FileNotFoundException) {
            return
        }
        while (capturingEnabled) {
            val bytesRead = audioRecord!!.read(dataRead, 0, dataRead.size)
            try {
                outputStream.write(dataRead, 0, bytesRead)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        try {
            outputStream.flush()
            outputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun stopRecording() {
        capturingEnabled = false
    }

    fun setAudioRecord(context: Context) {
        if (!checkPermission(context)) {
            Toast.makeText(context, "Record audio permission not granted.", Toast.LENGTH_LONG).show()
            return
        }

        audioRecord = AudioRecord.Builder()
            .setBufferSizeInBytes(bufferSize)
            .setAudioPlaybackCaptureConfig(audioConfiguration)
            .setAudioFormat(audioFormat)
            .build()
    }

    private fun checkPermission(context: Context): Boolean {
        val result = ActivityCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
        return result == PackageManager.PERMISSION_GRANTED
    }

}