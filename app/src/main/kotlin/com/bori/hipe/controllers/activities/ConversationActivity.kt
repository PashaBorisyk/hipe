package com.bori.hipe.controllers.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity

import com.bori.hipe.controllers.fragments.FragmentChatsDialog


private const val TAG = "ConversationActivity"

class ConversationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportFragmentManager.beginTransaction().add(FragmentChatsDialog(), TAG).commit()

    }

}
