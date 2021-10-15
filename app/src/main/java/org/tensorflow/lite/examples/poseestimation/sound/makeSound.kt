package org.tensorflow.lite.examples.poseestimation.sound

import android.media.AudioFormat
import android.media.AudioTrack
import android.media.AudioTrack.MODE_STREAM
import android.media.AudioTrack.WRITE_BLOCKING
import android.os.Build
import android.os.Bundle
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.sin
import org.tensorflow.lite.examples.poseestimation.data.Note


class makeSound {
    private val sampleRate = 44100

    private var playState =true
    private var angle: Double = 0.0
    private var audioTrack: AudioTrack? = null
    private var synthFrequency = Note.A4
    private var minSize = AudioTrack.getMinBufferSize(sampleRate,
        AudioFormat.CHANNEL_OUT_STEREO,
        AudioFormat.ENCODING_PCM_16BIT)
    private var buffer = ShortArray(minSize)
    private var player2 = getAudioTrack()
    private var soundThread: Thread? = null
    @RequiresApi(Build.VERSION_CODES.M)
    private var soundGen = Runnable { //버퍼 생성 스레드
        Thread.currentThread().priority = Thread.MIN_PRIORITY

        while(playState) {
            generateTone()
            player2?.write(buffer, 0, buffer.size, WRITE_BLOCKING)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun makeSound(){ //소리 재생
        playState = true
        player2?.play()
        soundThread = Thread(soundGen)
        soundThread!!.start()
    }

    private fun stopSound(){ //소리 멈춤
        playState = false
        player2?.stop()
    }

    private fun getNoteFrequencies(): Double { //주파수 리턴 함수
        return synthFrequency.note
    }

    private fun setNoteFrequencies(ratio:Double){ //주파수 조절 함수 : 아직 확정 못 지음

    }

    private fun oscillator(amplify: Int, frequencies: Double): Double { //파형 조절 함수 amplify:진폭 frequencies:주파수
        return sin( Math.PI * amplify * frequencies)
    }

    private fun generateTone(){// 버퍼 생성 함수 array에 집어넣을 값
        for (i in buffer.indices) {
            val angularFrequency: Double =
                getNoteFrequencies() * (Math.PI) / sampleRate
            buffer[i] = (Short.MAX_VALUE * oscillator(1,angle).toFloat()).toInt().toShort()
            angle += angularFrequency
        }
    }

    private fun getAudioTrack(): AudioTrack? {// 오디오 트랙 빌더 => 오디오 트랙 생성
        if (audioTrack == null) audioTrack = AudioTrack.Builder().setTransferMode(MODE_STREAM)
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                    .build()
            )
            .setBufferSizeInBytes(minSize)
            .build()
        return audioTrack
    }
}