package br.com.aguiabranca.inovagab.utils

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import java.io.File
import java.io.FileOutputStream

class AudioRecorder(private val context: Context) {
    private var recorder: MediaRecorder? = null

    fun startRecording(outputFile: File) {
        recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(FileOutputStream(outputFile).fd)
            prepare()
            start()
        }
    }

    fun stopRecording() {
        recorder?.stop()
        recorder?.release()
        recorder = null
    }
}

class AudioPlayer {
    private var player: MediaPlayer? = null

    fun playAudio(context: Context, url: String) {
        stopAudio()
        player = MediaPlayer().apply {
            setDataSource(context, Uri.parse(url))
            prepareAsync()
            setOnPreparedListener { start() }
            setOnCompletionListener { stopAudio() }
        }
    }

    fun stopAudio() {
        player?.stop()
        player?.release()
        player = null
    }
}
