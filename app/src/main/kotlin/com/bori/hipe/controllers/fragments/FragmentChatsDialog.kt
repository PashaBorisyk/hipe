package com.bori.hipe.controllers.fragments

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.bori.hipe.R
import com.bori.hipe.controllers.messenger.WebSocketConnector
import com.bori.hipe.controllers.messenger.callback.MessageCallbackAdapter
import com.bori.hipe.controllers.rest.callback.RestCallback
import com.bori.hipe.controllers.rest.callback.RestCallbackRepository
import com.bori.hipe.models.User
import com.bori.hipe.util.Const
import com.nostra13.universalimageloader.core.DisplayImageOptions
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.assist.ImageScaleType
import kotlinx.android.synthetic.main.fragment_chats_dialog.*

class FragmentChatsDialog : androidx.fragment.app.Fragment(), View.OnClickListener {

    companion object {
        private const val TAG = "FragmentChatsDialog"
        private const val TYPE_MINE = 0
        private const val TYPE_USER = 1
    }

    private lateinit var v: View
    private lateinit var recyclerView: androidx.recyclerview.widget.RecyclerView
    private lateinit var messagesAdapter: MessagesAdapter
    private lateinit var nickname: String
    private lateinit var url: String
    private lateinit var message: EditText


    private var id: Long = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.d(TAG, "FragmentChatsDialog.onCreateView")

        v = inflater.inflate(R.layout.fragment_chats_dialog, container, false)

        val toolbar: Toolbar = v.findViewById(R.id.toolbar2)

        val activity = activity as AppCompatActivity
        activity.setSupportActionBar(toolbar)

        try {
            activity.actionBar!!.setDisplayHomeAsUpEnabled(true)
        } catch (e: NullPointerException) {
            e.printStackTrace()
        }
        nickname = User().username
        nickname = "pashaborisyk"
        url = getActivity()!!.getSharedPreferences(Const.HIPE_APPLICATION_SHARED_PREFERENCES, Activity.MODE_PRIVATE).getString(Const.USER_IMAGE_SMALL, null)

        message = v.findViewById(R.id.edit_message)

        send_message_button.setOnClickListener(this)

        recyclerView = v.findViewById(R.id.messages_list)
        messagesAdapter = MessagesAdapter()
        recyclerView.adapter = messagesAdapter
        val linearLayoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
        linearLayoutManager.stackFromEnd = true
        recyclerView.layoutManager = linearLayoutManager
        RestCallbackRepository.registerCallback(messagesAdapter.restCallback)
        WebSocketConnector.registerCallback(messagesAdapter.messageCallback)

        return v
    }


    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "FragmentChatsDialog.onDestroyView")

        RestCallbackRepository.unregisterCallback(messagesAdapter.restCallback)
        WebSocketConnector.unregisterCallback(messagesAdapter.messageCallback)

    }

    override fun onClick(v: View) {
        Log.d(TAG, "FragmentChatsDialog.onClick")

        when (v.id) {

            R.id.send_message_button -> {

                val myChatMessage = Any()

                WebSocketConnector.sendMessage(myChatMessage)
                message.setText("")
            }

            else -> {
            }
        }

    }

    private inner class MessagesAdapter : androidx.recyclerview.widget.RecyclerView.Adapter<MessagesAdapter.VH>(), View.OnClickListener {

        private val layoutInflater: LayoutInflater = activity!!.layoutInflater

        private val imageLoader = ImageLoader.getInstance()
        private val displayImageOptions: DisplayImageOptions = DisplayImageOptions.Builder()
                .cacheOnDisk(true)
                .cacheInMemory(true)
                .imageScaleType(ImageScaleType.EXACTLY_STRETCHED).build()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            Log.d(TAG, "MessagesAdapter.onCreateViewHolder")

            if (viewType == TYPE_MINE)
                return VH(layoutInflater.inflate(R.layout.chats_row_my_message, parent, false))

            return VH(layoutInflater.inflate(R.layout.chats_row_user_message, parent, false))

        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            Log.d(TAG, "MessagesAdapter.onBindViewHolder")

        }

        override fun getItemCount() = 10

        override fun getItemViewType(position: Int): Int {
            return TYPE_USER
        }

        val messageCallback = object : MessageCallbackAdapter() {

            override fun onTextMessage(payload: String) {
                Log.d(TAG, "MessagesAdapter.onTextMessage")

                try {
                    notifyItemInserted(0)
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }

        }

        val restCallback = object : RestCallback() {

            override fun onFailure(requestID: Int, t: Throwable) {
                Log.d(TAG, "MessagesAdapter.onFailure")

            }

        }

        override fun onClick(v: View) {
            Log.d(TAG, "MessagesAdapter.onClick")

            if (v.tag != null && v.tag is String) {

//                val username = v.tag as String
//                val intent = Intent(activity, UserActivity::class.java)
//                intent.putExtra(Const.USER_SELF_NICK, username)
//                startActivity(intent)
                return
            }

        }

        internal inner class VH(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {

            var message: TextView = itemView.findViewById(R.id.message_message)
            var time: TextView = itemView.findViewById(R.id.messaage_time)
            var nickname: TextView = itemView.findViewById(R.id.message_nickname)
            var userPhoto: ImageView = itemView.findViewById(R.id.user_photo)

        }

    }

}
