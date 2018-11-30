package com.bori.hipe.util.extensions

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bori.hipe.controllers.fragments.base.HipeBaseFragment

private const val TAG = "FragmentExtension.kt"

fun HipeBaseFragment.setContentView(id: Int, inflater: LayoutInflater, container: ViewGroup?) {
    rootView = inflater.inflate(id, container)
}

inline fun <reified T : View> HipeBaseFragment.findViewById(id: Int): T = rootView.findViewById(id)

