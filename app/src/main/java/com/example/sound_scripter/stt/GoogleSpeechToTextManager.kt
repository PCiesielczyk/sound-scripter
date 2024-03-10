package com.example.sound_scripter.stt

import com.google.api.gax.rpc.ClientStream
import com.google.api.gax.rpc.ResponseObserver
import com.google.api.gax.rpc.StreamController
import com.google.cloud.speech.v1.RecognitionConfig
import com.google.cloud.speech.v1.SpeechClient
import com.google.cloud.speech.v1.SpeechRecognitionAlternative
import com.google.cloud.speech.v1.StreamingRecognitionConfig
import com.google.cloud.speech.v1.StreamingRecognizeRequest
import com.google.cloud.speech.v1.StreamingRecognizeResponse
import com.google.cloud.speech.v1.StreamingRecognitionResult
import com.google.protobuf.ByteString


class GoogleSpeechToTextManager {
    private lateinit var speechClient: SpeechClient

    private val recognitionConfig = RecognitionConfig.newBuilder()
        .setEncoding(RecognitionConfig.AudioEncoding.LINEAR16)
        .setLanguageCode("en-US")
        .setSampleRateHertz(16000)
        .build()

    private val streamingRecognitionConfig =
        StreamingRecognitionConfig.newBuilder().setConfig(recognitionConfig).build()

    private var request = StreamingRecognizeRequest.newBuilder()
        .setStreamingConfig(streamingRecognitionConfig)
        .build()

    private val responseObserver = object : ResponseObserver<StreamingRecognizeResponse> {
        var responses = ArrayList<StreamingRecognizeResponse>()
        override fun onStart(controller: StreamController) {}
        override fun onResponse(response: StreamingRecognizeResponse) {
            responses.add(response)
        }

        override fun onComplete() {
            for (response in responses) {
                val result: StreamingRecognitionResult = response.resultsList[0]
                val alternative: SpeechRecognitionAlternative = result.alternativesList[0]
                System.out.printf("Transcript : %s\n", alternative.getTranscript())
            }
        }

        override fun onError(t: Throwable) {
            println(t)
        }
    }

    private fun initializeSpeechClient() {
        speechClient = SpeechClient.create()
    }

    private fun closeSpeechClient() {
        speechClient.close()
    }

    fun transcribeAudio(byteArray: ByteArray) {
        val clientStream: ClientStream<StreamingRecognizeRequest> =
            speechClient.streamingRecognizeCallable().splitCall(responseObserver)

        clientStream.send(request)
        request = StreamingRecognizeRequest.newBuilder()
            .setAudioContent(ByteString.copyFrom(byteArray))
            .build()
        clientStream.send(request)
        closeSpeechClient()
    }
}