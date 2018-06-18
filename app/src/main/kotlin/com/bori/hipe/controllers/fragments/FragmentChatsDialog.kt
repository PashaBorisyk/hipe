package com.bori.hipe.controllers.fragments

import android.app.Activity
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import com.bori.hipe.R
import com.bori.hipe.controllers.messenger.WebSocketConnector
import com.bori.hipe.controllers.messenger.callback.MessageCallbackAdapter
import com.bori.hipe.controllers.rest.RestService
import com.bori.hipe.controllers.rest.callbacks.RestCallbackAdapter
import com.bori.hipe.models.ChatMessageNOSQL
import com.bori.hipe.models.User
import com.bori.hipe.util.Const
import com.google.gson.Gson
import com.nostra13.universalimageloader.core.DisplayImageOptions
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.assist.ImageScaleType
import kotlinx.android.synthetic.main.fragment_chats_dialog.*
import java.util.*

class FragmentChatsDialog : Fragment(), View.OnClickListener {

    companion object {
        private const val TYPE_MINE = 0
        private const val TYPE_USER = 1
    }

    private lateinit var v: View
    private lateinit var recyclerView: RecyclerView
    private lateinit var messagesAdapter: MessagesAdapter
    private lateinit var nickname: String
    private lateinit var url: String
    private lateinit var message: EditText


    private var id: Long = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        v = inflater.inflate(R.layout.fragment_chats_dialog, container, false)

        val toolbar: Toolbar = v.findViewById(R.id.toolbar2)

        val activity = activity as AppCompatActivity
        activity.setSupportActionBar(toolbar)

        try {
            activity.actionBar!!.setDisplayHomeAsUpEnabled(true)
        } catch (e: NullPointerException) {
            e.printStackTrace()
        }
        nickname = User.thisUser.username
        nickname = "pashaborisyk"
        url = getActivity()!!.getSharedPreferences(Const.HIPE_APPLICATION_SHARED_PREFERENCES, Activity.MODE_PRIVATE).getString(Const.USER_IMAGE_SMALL, null)

        message = v.findViewById(R.id.edit_message)

        send_message_button.setOnClickListener(this)

        recyclerView = v.findViewById(R.id.messages_list)
        messagesAdapter = MessagesAdapter()
        recyclerView.adapter = messagesAdapter
        val linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.stackFromEnd = true
        recyclerView.layoutManager = linearLayoutManager
        RestService.registerCallback(messagesAdapter.restCallback)
        WebSocketConnector.registerCallback(messagesAdapter.messageCallback)

        return v
    }


    override fun onDestroyView() {
        super.onDestroyView()
        RestService.unregisterCallback(messagesAdapter.restCallback)
        WebSocketConnector.unregisterCallback(messagesAdapter.messageCallback)

    }

    override fun onClick(v: View) {

        when (v.id) {

            R.id.send_message_button -> {

                val myChatMessage = ChatMessageNOSQL(

                        eventID = id,
                        mills = System.currentTimeMillis(),
                        senderNickname = nickname,
                        senderSmallImageUrl = url,
                        message = message.text.toString()
                )

                WebSocketConnector.sendMessage(myChatMessage)
                message.setText("")
            }

            else -> {
            }
        }

    }

    private inner class MessagesAdapter : RecyclerView.Adapter<MessagesAdapter.VH>(), View.OnClickListener {

        private val chatMessages = arrayListOf<ChatMessageNOSQL>()
        private val layoutInflater: LayoutInflater = activity!!.layoutInflater

        private val imageLoader = ImageLoader.getInstance()
        private val displayImageOptions: DisplayImageOptions = DisplayImageOptions.Builder()
                .cacheOnDisk(true)
                .cacheInMemory(true)
                .imageScaleType(ImageScaleType.EXACTLY_STRETCHED).build()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {

            if (viewType == TYPE_MINE)
                return VH(layoutInflater.inflate(R.layout.chats_row_my_message, parent, false))

            return VH(layoutInflater.inflate(R.layout.chats_row_user_message, parent, false))

        }

        override fun onBindViewHolder(holder: VH, position: Int) {

            val chatMessage = chatMessages[position]

            imageLoader.displayImage(chatMessage.senderSmallImageUrl, holder.userPhoto, displayImageOptions)
            holder.userPhoto.tag = chatMessage.senderNickname
            holder.userPhoto.setOnClickListener(this)
            holder.nickname.text = chatMessage.senderNickname
            holder.time.text = Date(chatMessage.mills).toString()
            holder.message.text = chatMessage.message

        }

        override fun getItemCount() = 10

        override fun getItemViewType(position: Int): Int {

            if (chatMessages[position].senderNickname == nickname)
                return TYPE_MINE
            return TYPE_USER

        }

        val messageCallback = object : MessageCallbackAdapter() {

            override fun onTextMessage(payload: String) {

                try {
                    val chatMessage = Gson().fromJson<ChatMessageNOSQL>(payload, ChatMessageNOSQL::class.java)
                    if (!chatMessage.informative) {
                        chatMessages.add(0, chatMessage)
                        notifyItemInserted(0)
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }

        }

        val restCallback = object : RestCallbackAdapter() {

            override fun onFailure(requestID: Long, t: Throwable) {

            }

            override fun onOk(requestID: Long) {

            }

        }

        override fun onClick(v: View) {

            if (v.tag != null && v.tag is String) {

//                val username = v.tag as String
//                val intent = Intent(activity, UserActivity::class.java)
//                intent.putExtra(Const.USER_SELF_NICK, username)
//                startActivity(intent)
                return
            }

        }

        internal inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {

            var message: TextView = itemView.findViewById(R.id.message_message)
            var time: TextView = itemView.findViewById(R.id.messaage_time)
            var nickname: TextView = itemView.findViewById(R.id.message_nickname)
            var userPhoto: ImageView = itemView.findViewById(R.id.user_photo)

        }

    }

}
