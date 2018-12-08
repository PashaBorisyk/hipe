package com.bori.hipe.controllers.fragments

import android.graphics.SurfaceTexture
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.*
import com.bori.hipe.R
import com.bori.hipe.controllers.fragments.base.HipeBaseFragment
import com.bori.hipe.controllers.translation.VideoTranslationHelper
import com.bori.hipe.controllers.views.AutoFitTextureView
import com.bori.hipe.util.extensions.setContentView
import java.net.URL

class StreamViewFragment : HipeBaseFragment() {

    private companion object {
        const val TAG = "StreamViewFragment"
    }

    private lateinit var autoFitTextureView: AutoFitTextureView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.d(TAG, "$TAG.onCreateView")
        setContentView(R.layout.fragment_video_stream_playback, inflater, container)

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    fun init() {
        autoFitTextureView.setAspectRatio(1080, 1920)
        autoFitTextureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {
            }

            override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {
            }

            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
                return true
            }

            override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {
                val url = URL(getString(R.string.video_stream_url_address))
                val videoTranslationHelper = VideoTranslationHelper(VideoTranslationHelper.Mode.DECODE, url)
                videoTranslationHelper.prepare(Size(width, height), Surface(surface)).subscribe()
            }

        }
    }

}