package com.example.ninjagame.util

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.example.ninjagame.R

class SoundManager(context: Context) {
    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(5)
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

    init {
        // Bạn cần thêm file vào res/raw. Nếu chưa có, code sẽ không crash nhưng không có tiếng.
        throwSound = soundPool.load(context, R.raw.hrow, 1)
        explodeSound = soundPool.load(context, R.raw.explosion, 1)
        gameOverSound = soundPool.load(context, R.raw.game_over, 1)
    }

    fun playThrow() = soundPool.play(throwSound, 0.5f, 0.5f, 1, 0, 1f)
    fun playExplode() = soundPool.play(explodeSound, 0.7f, 0.7f, 1, 0, 1f)
    fun playGameOver() = soundPool.play(gameOverSound, 1f, 1f, 1, 0, 1f)
}
