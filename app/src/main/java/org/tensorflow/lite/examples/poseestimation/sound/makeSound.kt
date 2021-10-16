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
import android.graphics.PointF
import android.util.Log

class makeSound (){
    private val sampleRate = 44100 //샘플링 정도 혹시 너무 버벅거리면 샘플링 줄이기

    private var ratio = 1.0 //비율 값
    private var playState =true //재생중:true, 정지:false
    private var angle: Double = 0.0
    private var audioTrack: AudioTrack? = null
    private var startFrequency = 32.70 // 초기 주파수 값 ==> 시작점
    private var synthFrequency = 32.70 // 시작점으로부터 시작하는 주파수 변화
    private var minSize = AudioTrack.getMinBufferSize(sampleRate,
        AudioFormat.CHANNEL_OUT_STEREO,
        AudioFormat.ENCODING_PCM_16BIT)
    private var buffer = ShortArray(minSize)// 버퍼
    private var player = getAudioTrack() // 소리 재생 클라스 생성
    private var soundThread: Thread? = null //스레드
    @RequiresApi(Build.VERSION_CODES.M)
    private var soundGen = Runnable { //버퍼 생성 스레드
        Thread.currentThread().priority = Thread.MIN_PRIORITY

        while(playState) {
            this.setNoteFrequencies(ratio)
            generateTone()
            player?.write(buffer, 0, buffer.size, WRITE_BLOCKING)
        }
    }
    private var Right_Wrist: PointF = PointF(0.0F,0.0F)


    @RequiresApi(Build.VERSION_CODES.M)
    private fun makeSound(){ //소리 재생
        playState = true
        player?.play()
        soundThread = Thread(soundGen)
        soundThread!!.start()
    }

    private fun stopSound(){ //소리 멈춤
        playState = false
        player?.stop()
    }

    private fun getStartNoteFrequencies(): Double { //주파수 리턴 함수
        return startFrequency
    }

    private fun getSynthNoteFrequencies():Double{
        return synthFrequency
    }
    private fun setStartFrequencies(note:Double){//ex) input(note) = (Note.C4.note) ==>파라미터
        startFrequency = note
    }
    private fun setNoteFrequencies(ratio:Double){ //주파수 조절 함수 : 아직 확정 못 지음
        synthFrequency = startFrequency + (ratio * startFrequency)
    }

    private fun oscillator(amplify: Int, frequencies: Double): Double { //파형 조절 함수 amplify:진폭 frequencies:주파수
        return sin( Math.PI * amplify * frequencies)
    }

    private fun generateTone(){// 버퍼 생성 함수 array에 집어넣을 값
        for (i in buffer.indices) {
            val angularFrequency: Double =
                synthFrequency * (Math.PI) / sampleRate
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

    fun soundPlay(ratio: Float,right_wrist: PointF,is_in_body: Boolean){
        var distance = Math.pow((right_wrist.x - Right_Wrist.x).toDouble(), 2.0) + Math.pow((right_wrist.y - Right_Wrist.y).toDouble(),2.0)
        if(distance > 100) {
            Log.d("test", distance.toString())
            makeSound()
        } else {
            stopSound()
        }
        Right_Wrist = right_wrist
    }
}