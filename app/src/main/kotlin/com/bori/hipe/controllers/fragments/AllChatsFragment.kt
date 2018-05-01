package com.bori.hipe.controllers.fragments


import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.bori.hipe.R
import com.bori.hipe.controllers.fragments.base.HipeBaseFragment
import com.bori.hipe.controllers.messenger.callback.MessageCallbackAdapter
import com.bori.hipe.models.ChatMessageNOSQL
import com.bori.hipe.models.Event
import com.bori.hipe.util.extensions.findViewById
import com.bori.hipe.util.extensions.setContentView


class AllChatsFragment : HipeBaseFragment() {

    private var v: View? = null
    private var recyclerView: RecyclerView? = null
    private var eventChatsAdapter: EventChatsAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        setContentView(R.layout.fragment_all_chats, inflater ,container)
        recyclerView = findViewById(R.id.fragment_all_chats_last_messages_list)
        recyclerView?.layoutManager = LinearLayoutManager(context)

        eventChatsAdapter = EventChatsAdapter()
        recyclerView?.adapter = eventChatsAdapter

        return null
    }

    private val onMessageListener = object : MessageCallbackAdapter() {
        override fun onTextMessage(payload: String) {

        }

    }

    internal inner class EventChatsAdapter : RecyclerView.Adapter<EventChatsAdapter.VH>(), View.OnClickListener {

        private val eventList = arrayListOf<Event>()
        private val lastChatMessages = arrayListOf<ChatMessageNOSQL>()
        private val layoutInflater: LayoutInflater = activity!!.layoutInflater

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
                VH(layoutInflater.inflate(R.layout.item_row_all_chats, parent, false))

        override fun onBindViewHolder(holder: VH, position: Int) {

            val event = eventList[position]
            val chatMessage = lastChatMessages[position]

            holder.v.tag = position

            holder.membersCount.text = "${chatMessage.users.size} участников"
            holder.eventName.text = event.localName
            holder.lastMessage.text = "${chatMessage} : ${chatMessage.message}"
        }

        override fun getItemCount() = eventList.size

        override fun onClick(v: View) {

            val tag = v.tag ?: return
            if (tag is Int) {
//                val eventId = eventList[tag].id
//                val bundle = Bundle()
//                bundle.putLong(Const.EVENT_ID, eventId)
//                val intent = Intent(activity, ConversationActivity::class.java)
//                intent.putExtras(bundle)
//                activity!!.startActivity(intent)

            }

        }

        internal inner class VH(val v: View) : RecyclerView.ViewHolder(v) {

            val eventName: TextView = v.findViewById(R.id.all_chats_event_name)
            val lastMessage: TextView = v.findViewById(R.id.all_chats_last_message)
            val membersCount: TextView = v.findViewById(R.id.all_chats_count)

        }

    }

}
