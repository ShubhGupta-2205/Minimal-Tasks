package com.example.alarm

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sin

/**
 * Handles synthesized cyber neon alarm tones & media playback with USAGE_ALARM.
 */
object AudioToneGenerator {

    private var mediaPlayer: MediaPlayer? = null
    private var synthJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    fun startPlayingRingtone(context: Context, ringtoneName: String, ringtoneUriStr: String?) {
        stopPlaying()

        // 1. If custom URI provided
        if (!ringtoneUriStr.isNullOrBlank()) {
            try {
                val uri = Uri.parse(ringtoneUriStr)
                mediaPlayer = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    setDataSource(context, uri)
                    isLooping = true
                    prepare()
                    start()
                }
                return
            } catch (e: Exception) {
                Log.e("AudioToneGenerator", "Failed to play custom URI, falling back", e)
            }
        }

        // 2. Synthesize distinctive Cyber Tones based on name
        when (ringtoneName) {
            "Digital Echo", "Digital Beep" -> startSynthesizedEcho()
            "Zen Chime" -> startSynthesizedZen()
            "Neon Warning" -> startSynthesizedWarning()
            "Cyber Pulse" -> startSynthesizedCyberPulse()
            else -> {
                // System default alarm ringtone fallback
                try {
                    val alertUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                        ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                        ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                    
                    mediaPlayer = MediaPlayer().apply {
                        setAudioAttributes(
                            AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_ALARM)
                                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                                .build()
                        )
                        setDataSource(context, alertUri)
                        isLooping = true
                        prepare()
                        start()
                    }
                } catch (e: Exception) {
                    Log.e("AudioToneGenerator", "Failed system fallback, using synth pulse", e)
                    startSynthesizedCyberPulse()
                }
            }
        }
    }

    private fun startSynthesizedCyberPulse() {
        synthJob = scope.launch {
            val sampleRate = 44100
            val frequencies = listOf(880.0, 1174.66, 1318.51, 1760.0) // A5, D6, E6, A6

            while (isActive) {
                for (freq in frequencies) {
                    if (!isActive) break
                    playPcmTone(freq, durationMs = 180, sampleRate = sampleRate)
                    delay(80)
                }
                delay(600)
            }
        }
    }

    private fun startSynthesizedEcho() {
        synthJob = scope.launch {
            val sampleRate = 44100
            while (isActive) {
                playPcmTone(1200.0, durationMs = 120, sampleRate = sampleRate)
                delay(100)
                playPcmTone(1200.0, durationMs = 120, sampleRate = sampleRate)
                delay(100)
                playPcmTone(1500.0, durationMs = 250, sampleRate = sampleRate)
                delay(900)
            }
        }
    }

    private fun startSynthesizedZen() {
        synthJob = scope.launch {
            val sampleRate = 44100
            val chords = listOf(528.0, 660.0, 792.0, 1056.0)
            while (isActive) {
                for (chord in chords) {
                    if (!isActive) break
                    playPcmTone(chord, durationMs = 350, sampleRate = sampleRate)
                    delay(150)
                }
                delay(1200)
            }
        }
    }

    private fun startSynthesizedWarning() {
        synthJob = scope.launch {
            val sampleRate = 44100
            while (isActive) {
                playPcmTone(900.0, durationMs = 220, sampleRate = sampleRate)
                delay(80)
                playPcmTone(750.0, durationMs = 220, sampleRate = sampleRate)
                delay(400)
            }
        }
    }

    private fun playPcmTone(frequency: Double, durationMs: Int, sampleRate: Int) {
        val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
        if (numSamples <= 0) return

        val sample = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val time = i.toDouble() / sampleRate
            // Sine wave with smooth envelope fade in/out to prevent audio clicking
            val envelope = when {
                i < numSamples * 0.1 -> i / (numSamples * 0.1)
                i > numSamples * 0.8 -> (numSamples - i) / (numSamples * 0.2)
                else -> 1.0
            }
            val angle = 2.0 * Math.PI * frequency * time
            sample[i] = (sin(angle) * Short.MAX_VALUE * 0.85 * envelope).toInt().toShort()
        }

        val bufferSize = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        var audioTrack: AudioTrack? = null
        try {
            audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize.coerceAtLeast(numSamples * 2))
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            audioTrack.write(sample, 0, numSamples)
            audioTrack.play()
            Thread.sleep(durationMs.toLong())
        } catch (e: Exception) {
            Log.e("AudioToneGenerator", "Pcm Tone error", e)
        } finally {
            try {
                audioTrack?.stop()
                audioTrack?.release()
            } catch (_: Exception) {}
        }
    }

    fun stopPlaying() {
        synthJob?.cancel()
        synthJob = null
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (_: Exception) {}
        mediaPlayer = null
    }

    private var previewJob: Job? = null
    private var previewPlayer: MediaPlayer? = null

    fun previewTone(context: Context, ringtoneName: String, ringtoneUriStr: String?) {
        stopPreview()
        if (!ringtoneUriStr.isNullOrBlank()) {
            try {
                val uri = Uri.parse(ringtoneUriStr)
                previewPlayer = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    setDataSource(context, uri)
                    prepare()
                    start()
                    setOnCompletionListener {
                        stopPreview()
                    }
                }
                return
            } catch (e: Exception) {
                Log.e("AudioToneGenerator", "Failed to preview custom URI", e)
            }
        }

        // Preview system default tone or fallback
        try {
            val alertUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            if (alertUri != null) {
                previewPlayer = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    setDataSource(context, alertUri)
                    prepare()
                    start()
                    setOnCompletionListener {
                        stopPreview()
                    }
                }
                return
            }
        } catch (_: Exception) {}

        // Fallback tone preview briefly
        previewJob = scope.launch {
            val sampleRate = 44100
            val freqs = listOf(880.0, 1174.66, 1318.51, 1760.0)
            for (f in freqs) {
                playPcmTone(f, durationMs = 160, sampleRate = sampleRate)
                delay(60)
            }
        }
    }

    fun playTaskPing(context: Context, toneName: String?) {
        scope.launch {
            when (toneName) {
                "Cyber Pulse" -> {
                    val sampleRate = 44100
                    playPcmTone(880.0, 120, sampleRate)
                    delay(50)
                    playPcmTone(1318.51, 120, sampleRate)
                    delay(50)
                    playPcmTone(1760.0, 200, sampleRate)
                }
                "Digital Beep" -> {
                    val sampleRate = 44100
                    playPcmTone(1200.0, 100, sampleRate)
                    delay(70)
                    playPcmTone(1500.0, 220, sampleRate)
                }
                "Zen Chime" -> {
                    val sampleRate = 44100
                    playPcmTone(528.0, 250, sampleRate)
                    delay(100)
                    playPcmTone(792.0, 300, sampleRate)
                }
                "Neon Warning" -> {
                    val sampleRate = 44100
                    playPcmTone(900.0, 180, sampleRate)
                    delay(60)
                    playPcmTone(750.0, 200, sampleRate)
                }
                "Cosmic Ping" -> {
                    val sampleRate = 44100
                    playPcmTone(1046.50, 100, sampleRate) // C6
                    delay(50)
                    playPcmTone(1567.98, 100, sampleRate) // G6
                    delay(50)
                    playPcmTone(2093.00, 250, sampleRate) // C7
                }
                else -> {
                    // System default notification ping
                    try {
                        val alertUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                        if (alertUri != null) {
                            val mp = MediaPlayer().apply {
                                setAudioAttributes(
                                    AudioAttributes.Builder()
                                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                                        .build()
                                )
                                setDataSource(context, alertUri)
                                prepare()
                                start()
                                setOnCompletionListener { release() }
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("AudioToneGenerator", "Failed system ping", e)
                    }
                }
            }
        }
    }

    fun stopPreview() {
        previewJob?.cancel()
        previewJob = null
        try {
            previewPlayer?.stop()
            previewPlayer?.release()
        } catch (_: Exception) {}
        previewPlayer = null
    }
}
