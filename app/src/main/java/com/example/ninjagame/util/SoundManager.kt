package com.example.ninjagame.util

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import com.example.ninjagame.R

class SoundManager(private val context: Context) {

    // Lưu trữ giá trị âm lượng (0.0 đến 1.0)
    private var musicVolume: Float = 0.5f
    private var sfxVolume: Float = 0.8f

    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(10) // Tăng lên 10 để tránh mất tiếng khi nổ nhiều mục tiêu
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private var throwSound: Int = 0
    private var explodeSound: Int = 0
    private var gameOverSound: Int = 0

    private var bgmMenuPlayer: MediaPlayer? = null
    private var bgmGamePlayer: MediaPlayer? = null

    init {
        // Load các hiệu ứng âm thanh
        throwSound = soundPool.load(context, R.raw.hrow, 1)
        explodeSound = soundPool.load(context, R.raw.explosion, 1)
        gameOverSound = soundPool.load(context, R.raw.game_over, 1)
    }

    // --- QUẢN LÝ ÂM LƯỢNG ---

    fun setMusicVolume(volume: Float) {
        musicVolume = volume
        bgmMenuPlayer?.setVolume(volume, volume)
        bgmGamePlayer?.setVolume(volume, volume)
    }

    fun setSFXVolume(volume: Float) {
        sfxVolume = volume
    }

    fun getMusicVolume(): Float = musicVolume
    fun getSFXVolume(): Float = sfxVolume

    // --- HIỆU ỨNG ÂM THANH (SFX) ---

    fun playThrow() {
        soundPool.play(throwSound, sfxVolume, sfxVolume, 1, 0, 1f)
    }

    fun playExplode() {
        soundPool.play(explodeSound, sfxVolume, sfxVolume, 1, 0, 1f)
    }

    fun playGameOver() {
        soundPool.play(gameOverSound, sfxVolume, sfxVolume, 1, 0, 1f)
    }

    // --- NHẠC NỀN (BGM) ---

    fun startMenuMusic() {
        if (bgmGamePlayer?.isPlaying == true) stopGameMusic()

        if (bgmMenuPlayer == null) {
            bgmMenuPlayer = MediaPlayer.create(context, R.raw.nhacgame).apply {
                isLooping = true
                setVolume(musicVolume, musicVolume)
                start()
            }
        } else if (!bgmMenuPlayer!!.isPlaying) {
            bgmMenuPlayer?.start()
        }
    }

    fun stopMenuMusic() {
        bgmMenuPlayer?.let {
            if (it.isPlaying) it.stop()
            it.release()
        }
        bgmMenuPlayer = null
    }

    fun startGameMusic() {
        if (bgmMenuPlayer?.isPlaying == true) stopMenuMusic()

        if (bgmGamePlayer == null) {
            bgmGamePlayer = MediaPlayer.create(context, R.raw.nhacingame).apply {
                isLooping = true
                setVolume(musicVolume, musicVolume)
                start()
            }
        } else if (!bgmGamePlayer!!.isPlaying) {
            bgmGamePlayer?.start()
        }
    }

    fun stopGameMusic() {
        bgmGamePlayer?.let {
            if (it.isPlaying) it.stop()
            it.release()
        }
        bgmGamePlayer = null
    }

    // Tạm dừng tất cả nhạc (dùng khi ẩn app)
    fun pauseAll() {
        bgmMenuPlayer?.pause()
        bgmGamePlayer?.pause()
    }

    // Tiếp tục phát (dùng khi quay lại app)
    fun resumeAll(isIngame: Boolean) {
        if (isIngame) bgmGamePlayer?.start()
        else bgmMenuPlayer?.start()
    }

    fun releaseAll() {
        try {
            soundPool.release()
            stopMenuMusic()
            stopGameMusic()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}