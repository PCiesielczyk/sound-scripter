package com.example.sound_scripter

import android.content.Intent
import android.content.pm.PackageManager
import android.Manifest
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioPlaybackCaptureConfiguration
import android.media.AudioRecord
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.paddingFromBaseline
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import com.example.sound_scripter.services.AudioCaptureService
import com.example.sound_scripter.ui.theme.SoundScripterTheme

class MainActivity : ComponentActivity() {

    private lateinit var mediaProjectionManager: MediaProjectionManager
    private var mediaProjection: MediaProjection? = null
    private lateinit var audioRecord: AudioRecord

    private val mediaProjectionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            mediaProjection = mediaProjectionManager.getMediaProjection(result.resultCode, result.data!!)
        }
    }

    private val recordAudioPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted && mediaProjection != null) {
            audioRecord = getAudioRecord()
            audioRecord.startRecording()
        } else {
            Toast.makeText(this, "Record audio permission not granted.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mediaProjectionManager = getSystemService(MediaProjectionManager::class.java)
        val createScreenCaptureIntent = mediaProjectionManager.createScreenCaptureIntent()

        val audioCaptureServiceIntent = Intent(this, AudioCaptureService::class.java)
        startForegroundService(audioCaptureServiceIntent)

        mediaProjectionLauncher.launch(createScreenCaptureIntent)

        setContent {
            SoundScripterTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    TranscriptDisplay(recordAudioPermissionLauncher)
                }
            }
        }
    }

    private fun getAudioRecord(): AudioRecord {
        if (!checkPermission()) {
            Toast.makeText(this, "Record audio permission not granted.", Toast.LENGTH_LONG).show()
        }
        val audioConfiguration = AudioPlaybackCaptureConfiguration.Builder(mediaProjection!!)
            .addMatchingUsage(AudioAttributes.USAGE_MEDIA)
            .build()

        val audioFormat = AudioFormat.Builder()
            .setChannelMask(AudioFormat.CHANNEL_IN_MONO)
            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
            .setSampleRate(48000)
            .build()

        return AudioRecord.Builder()
            .setBufferSizeInBytes(128)
            .setAudioPlaybackCaptureConfig(audioConfiguration)
            .setAudioFormat(audioFormat)
            .build()
    }
    private fun checkPermission(): Boolean {
        val result = ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
        return result == PackageManager.PERMISSION_GRANTED
    }

}

@Composable
fun TranscriptDisplay(recordAudioPermissionLauncher: ActivityResultLauncher<String>,
                      modifier: Modifier = Modifier) {
    Column (
        modifier = modifier.padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {

        val initialDisplayText = "Your transcription will be here"
        val listeningDisplayText = "Sound Scripter is listening..."

        var enabled by remember { mutableStateOf(false) }
        var displayedText by remember { mutableStateOf(initialDisplayText) }

        val onEnabled: (Boolean) -> Unit = {
            enabled = it

            if (enabled) {
                displayedText = listeningDisplayText
                recordAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            } else {
                displayedText = initialDisplayText
            }
        }

        IconToggleButton(checked = enabled,
            onCheckedChange = onEnabled,
            modifier = modifier
                .size(64.dp)
                .align(Alignment.CenterHorizontally)
        ) {

            if (enabled) {
                Icon(
                    painter = painterResource(R.drawable.baseline_stop_circle_96),
                    contentDescription = null,
                    tint = Color(0xFFED2939)
                )
            } else {
                Icon(
                    painter = painterResource(R.drawable.baseline_play_circle_96),
                    contentDescription = null
                )
            }
        }

        Text(
            text = displayedText,
            modifier = modifier.paddingFromBaseline(bottom = 20.dp),
            fontSize = 21.sp,
            color = Color.LightGray
        )
    }
}
