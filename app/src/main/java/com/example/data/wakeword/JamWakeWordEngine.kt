package com.example.data.wakeword

import android.content.Context
import android.content.Intent
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale
import kotlin.math.sqrt

enum class WakeDetectionStatus {
    IDLE,
    LISTENING,
    WAKE_WORD_DETECTED,
    ERROR
}

class JamWakeWordEngine(private val context: Context) {

    private val mainHandler = Handler(Looper.getMainLooper())
    private val coroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private var speechRecognizer: SpeechRecognizer? = null
    private var isEngineActive = false
    private var audioRecord: AudioRecord? = null
    private var audioRecordJob: Job? = null

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _detectionStatus = MutableStateFlow(WakeDetectionStatus.IDLE)
    val detectionStatus: StateFlow<WakeDetectionStatus> = _detectionStatus.asStateFlow()

    private val _lastDetectedKeyword = MutableStateFlow<String?>(null)
    val lastDetectedKeyword: StateFlow<String?> = _lastDetectedKeyword.asStateFlow()

    private val _lastDetectedTimestamp = MutableStateFlow(0L)
    val lastDetectedTimestamp: StateFlow<Long> = _lastDetectedTimestamp.asStateFlow()

    private val _detectionCount = MutableStateFlow(0)
    val detectionCount: StateFlow<Int> = _detectionCount.asStateFlow()

    private val _audioAmplitude = MutableStateFlow(0f)
    val audioAmplitude: StateFlow<Float> = _audioAmplitude.asStateFlow()

    private val _sensitivity = MutableStateFlow(0.8f)
    val sensitivity: StateFlow<Float> = _sensitivity.asStateFlow()

    private var onWakeWordListener: ((String) -> Unit)? = null

    fun setOnWakeWordListener(listener: (String) -> Unit) {
        this.onWakeWordListener = listener
    }

    fun setSensitivity(value: Float) {
        _sensitivity.value = value.coerceIn(0.1f, 1.0f)
    }

    fun startListening() {
        if (isEngineActive) return
        isEngineActive = true
        _detectionStatus.value = WakeDetectionStatus.LISTENING
        _isListening.value = true

        mainHandler.post {
            initiateSpeechRecognizerLoop()
        }
        startAudioRmsMonitoring()
    }

    fun stopListening() {
        isEngineActive = false
        _isListening.value = false
        _detectionStatus.value = WakeDetectionStatus.IDLE
        _audioAmplitude.value = 0f

        mainHandler.post {
            try {
                speechRecognizer?.stopListening()
                speechRecognizer?.cancel()
                speechRecognizer?.destroy()
                speechRecognizer = null
            } catch (e: Exception) {
                Log.e(TAG, "Error stopping recognizer: ${e.message}")
            }
        }
        stopAudioRmsMonitoring()
    }

    private fun initiateSpeechRecognizerLoop() {
        if (!isEngineActive) return

        try {
            speechRecognizer?.destroy()
            if (!SpeechRecognizer.isRecognitionAvailable(context)) {
                Log.w(TAG, "Speech recognition service not directly available on device")
                return
            }

            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        if (isEngineActive) _isListening.value = true
                    }

                    override fun onBeginningOfSpeech() {
                        if (_audioAmplitude.value < 0.3f) {
                            _audioAmplitude.value = 0.4f
                        }
                    }

                    override fun onRmsChanged(rmsdB: Float) {
                        if (isEngineActive) {
                            val scaled = (rmsdB / 12f).coerceIn(0.05f, 1f)
                            _audioAmplitude.value = scaled
                        }
                    }

                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {
                        // Will restart loop onResults / onError
                    }

                    override fun onError(error: Int) {
                        Log.d(TAG, "Recognizer error code: $error")
                        // Common errors: ERROR_NO_MATCH, ERROR_SPEECH_TIMEOUT, ERROR_RECOGNIZER_BUSY
                        scheduleLoopRestart(if (error == SpeechRecognizer.ERROR_RECOGNIZER_BUSY) 500L else 200L)
                    }

                    override fun onResults(results: Bundle?) {
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        processSpokenResults(matches)
                        scheduleLoopRestart(150L)
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val partialMatches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        processSpokenResults(partialMatches)
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }

            val recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            }

            speechRecognizer?.startListening(recognizerIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Exception starting speech loop: ${e.message}")
            scheduleLoopRestart(1000L)
        }
    }

    private fun scheduleLoopRestart(delayMs: Long) {
        if (!isEngineActive) return
        mainHandler.postDelayed({
            if (isEngineActive) {
                initiateSpeechRecognizerLoop()
            }
        }, delayMs)
    }

    private fun processSpokenResults(matches: List<String>?) {
        if (matches.isNullOrEmpty()) return

        for (phrase in matches) {
            val lower = phrase.lowercase(Locale.ROOT).trim()
            Log.d(TAG, "Heard phrase: $lower")

            // Check for wake keywords: "jam", "hey jam", "ok jam", "hi jam", "jumble", "jumble p"
            if (isWakeWordMatch(lower)) {
                notifyWakeWordDetected("Jam")
                break
            }
        }
    }

    private fun isWakeWordMatch(phrase: String): Boolean {
        val keywords = listOf(
            "jam",
            "hey jam",
            "ok jam",
            "okay jam",
            "hi jam",
            "hello jam",
            "wake jam",
            "jumble",
            "jumble p",
            "gem",
            "hey jem",
            "gym" // common phonetic capture for "jam"
        )
        return keywords.any { kw -> phrase.contains(kw) }
    }

    fun notifyWakeWordDetected(keyword: String) {
        _lastDetectedKeyword.value = keyword
        _lastDetectedTimestamp.value = System.currentTimeMillis()
        _detectionCount.value += 1
        _detectionStatus.value = WakeDetectionStatus.WAKE_WORD_DETECTED

        mainHandler.post {
            onWakeWordListener?.invoke(keyword)
        }

        // Reset status back to listening after short celebration state
        coroutineScope.launch {
            delay(1500)
            if (isEngineActive) {
                _detectionStatus.value = WakeDetectionStatus.LISTENING
            }
        }
    }

    /**
     * Secondary acoustic decibel/RMS monitor to supply smooth visual waveform oscillations
     * even when SpeechRecognizer is idling between recognition turns.
     */
    private fun startAudioRmsMonitoring() {
        audioRecordJob?.cancel()
        audioRecordJob = coroutineScope.launch(Dispatchers.IO) {
            val sampleRate = 16000
            val channelConfig = AudioFormat.CHANNEL_IN_MONO
            val audioFormat = AudioFormat.ENCODING_PCM_16BIT
            val minBufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)

            if (minBufferSize <= 0) return@launch

            try {
                audioRecord = AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    sampleRate,
                    channelConfig,
                    audioFormat,
                    minBufferSize * 2
                )

                if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                    audioRecord?.release()
                    audioRecord = null
                    return@launch
                }

                audioRecord?.startRecording()
                val buffer = ShortArray(minBufferSize)

                while (isActive && isEngineActive) {
                    val readCount = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                    if (readCount > 0) {
                        var sum = 0.0
                        for (i in 0 until readCount) {
                            sum += buffer[i] * buffer[i]
                        }
                        val rms = sqrt(sum / readCount)
                        val normalized = (rms / 32768.0).toFloat() * 6f * _sensitivity.value
                        _audioAmplitude.value = normalized.coerceIn(0.02f, 1.0f)
                    }
                    delay(50)
                }
            } catch (e: SecurityException) {
                Log.e(TAG, "Permission missing for AudioRecord: ${e.message}")
            } catch (e: Exception) {
                Log.e(TAG, "Error in AudioRecord monitor: ${e.message}")
            } finally {
                try {
                    audioRecord?.stop()
                    audioRecord?.release()
                } catch (e: Exception) {
                    // ignore
                }
                audioRecord = null
            }
        }
    }

    private fun stopAudioRmsMonitoring() {
        audioRecordJob?.cancel()
        audioRecordJob = null
        try {
            audioRecord?.stop()
            audioRecord?.release()
        } catch (e: Exception) {
            // ignore
        }
        audioRecord = null
    }

    fun release() {
        stopListening()
        coroutineScope.cancel()
    }

    companion object {
        private const val TAG = "JamWakeWordEngine"

        @Volatile
        private var instance: JamWakeWordEngine? = null

        fun getInstance(context: Context): JamWakeWordEngine {
            return instance ?: synchronized(this) {
                instance ?: JamWakeWordEngine(context.applicationContext).also { instance = it }
            }
        }
    }
}
