package com.example.sound_scripter

import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sound_scripter.ui.theme.SoundScripterTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val mediaProjectionManager = getSystemService(MediaProjectionManager::class.java)
        var mediaProjection: MediaProjection

        val startMediaProjection = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                mediaProjection = mediaProjectionManager
                    .getMediaProjection(result.resultCode, result.data!!)
            }
        }
        startMediaProjection.launch(mediaProjectionManager.createScreenCaptureIntent())

        setContent {
            SoundScripterTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    TranscriptDisplay()
                }
            }
        }
    }
}

@Composable
fun TranscriptDisplay(modifier: Modifier = Modifier) {
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
            displayedText = if (it) listeningDisplayText else initialDisplayText

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



@Preview(showBackground = true)
@Composable
fun TranscriptDisplayPreview() {
    SoundScripterTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            TranscriptDisplay()
        }
    }
}