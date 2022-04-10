package com.example.android_pip_example

import android.app.PendingIntent
import android.app.PictureInPictureParams
import android.app.RemoteAction
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Bundle
import android.util.Rational
import android.view.View
import android.widget.MediaController
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.android_pip_example.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    companion object {
        private const val ACTION_VIDEO_PLAY_OR_PAUSE = "com.example.android_pip_example.ACTION_VIDEO_PLAY_OR_PAUSE"
    }

    private lateinit var binding: ActivityMainBinding

    private val pipActionReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            binding.videoView.run {
                if (isPlaying) {
                    pause()
                } else {
                    start()
                }
            }
            setPictureInPictureParams(createPipParams())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        registerReceiver(
                pipActionReceiver,
                IntentFilter(ACTION_VIDEO_PLAY_OR_PAUSE)
        )
        initVideo()

        binding.videoView.start()

        binding.enterPipMode.setOnClickListener {
            enterPictureInPictureMode(createPipParams())
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        unregisterReceiver(pipActionReceiver)
    }

    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean, newConfig: Configuration?) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)

        if (isInPictureInPictureMode) {
            binding.enterPipMode.visibility = View.INVISIBLE
        } else {
            binding.enterPipMode.visibility = View.VISIBLE
        }
    }

    private fun initVideo() {
        binding.videoView.run {
            setVideoURI(Uri.parse("android.resource://" + packageName + "/" + R.raw.movie))
            MediaController(this@MainActivity).apply {
                setAnchorView(this@run)
            }.let {
                setMediaController(it)
            }
            setOnPreparedListener {
                it.isLooping = true
            }
        }
    }

    private fun createPipParams(): PictureInPictureParams {
        val rational = Rational(binding.videoView.width, binding.videoView.height)
        val remoteAction: RemoteAction = let {
            val icon = if (binding.videoView.isPlaying) {
                Icon.createWithResource(this, R.drawable.ic_round_pause_24)
            } else {
                Icon.createWithResource(this, R.drawable.ic_round_play_arrow_24)
            }
            val pendingIntent = PendingIntent.getBroadcast(
                    this,
                    0,
                    Intent(ACTION_VIDEO_PLAY_OR_PAUSE),
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            RemoteAction(icon, "title", "description", pendingIntent)
        }
        val remoteActions = listOf(remoteAction)

        return PictureInPictureParams.Builder()
                .setAspectRatio(rational)
                .setActions(remoteActions)
                .build()
    }

}