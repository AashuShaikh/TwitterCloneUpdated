package com.aashushaikh.twitterclone.fragments

import ChatAdapter
import android.app.Application
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.aashushaikh.twitterclone.R
import com.aashushaikh.twitterclone.data.Message
import com.aashushaikh.twitterclone.databinding.FragmentChatsBinding
import com.aashushaikh.twitterclone.id_calls.AppIds
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallService
import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallInvitationConfig
import com.zegocloud.uikit.prebuilt.call.invite.widget.ZegoSendCallInvitationButton
import com.zegocloud.uikit.service.defines.ZegoUIKitUser
import kotlinx.coroutines.tasks.await

class ChatsFragment : Fragment() {

    private lateinit var binding: FragmentChatsBinding
    private lateinit var userEmail: String
    private lateinit var userProfile: String
    private lateinit var userId: String
    private lateinit var chatId: String
    private lateinit var chatAdapter: ChatAdapter
    private val messages: MutableList<Message> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_chats, container, false)
        userEmail = arguments?.getString("email").toString()
        userProfile = arguments?.getString("profile").toString()
        userId = arguments?.getString("userId").toString()
        createMenu()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Toast.makeText(
            requireContext(),
            "User Email: $userEmail and User Profile link: $userProfile",
            Toast.LENGTH_SHORT
        ).show()

        val config = ZegoUIKitPrebuiltCallInvitationConfig()
        ZegoUIKitPrebuiltCallService.init(
            requireActivity().application,
            AppIds.appId.toLong(),
            AppIds.appSign,
            Firebase.auth.currentUser!!.uid,
            Firebase.auth.currentUser!!.email,
            config
        )

        val currentUser = Firebase.auth.currentUser!!.uid
        chatAdapter = ChatAdapter(messages, currentUser)
        binding.rvChats.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = chatAdapter
        }

        initChat()

        binding.sendChat.setOnClickListener {
            val messageText = binding.etChat.text.toString().trim()
            if (messageText.isNotEmpty()) {
                sendMessage(chatId, Firebase.auth.uid.toString(), userId, messageText)
                binding.etChat.text?.clear()
            }
        }
    }

    private fun createMenu() {
        // Add MenuProvider
        binding.appToolbar.menu.clear()
        binding.appToolbar.inflateMenu(R.menu.message_menu)

        binding.appToolbar.title = userEmail
        binding.appToolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.voice_call -> {
                    // Navigate to settings screen.
                    Toast.makeText(requireContext(), "VoiceCall", Toast.LENGTH_SHORT).show()
                    val voiceCallButton = ZegoSendCallInvitationButton(requireContext())
                    voiceCallButton.setIsVideoCall(false)
                    voiceCallButton.setResourceID("zego_uikit_call")
                    voiceCallButton.setInvitees(listOf(ZegoUIKitUser(userId, userEmail)))
                    it.actionView = voiceCallButton
                    true
                }

                R.id.video_call -> {
                    // Save profile changes.
                    Toast.makeText(requireContext(), "VideoCall", Toast.LENGTH_SHORT).show()
                    val videoCallButton = ZegoSendCallInvitationButton(requireContext())
                    videoCallButton.setIsVideoCall(true)
                    videoCallButton.setResourceID("zego_uikit_call")
                    videoCallButton.setInvitees(listOf(ZegoUIKitUser(userId, userEmail)))
                    it.actionView = videoCallButton
                    true
                }

                R.id.profile -> {
                    Toast.makeText(requireContext(), "Profile", Toast.LENGTH_SHORT).show()
                    true
                }

                else -> false
            }
        }
    }

    private fun initChat() {
        val currentUser = Firebase.auth.currentUser!!.uid
        val chatWithUser = userId
        checkOrCreateChat(currentUser, chatWithUser) { chatId ->
            this.chatId = chatId
            listenForMessages(chatId)
        }
    }

    private fun listenForMessages(chatId: String) {
        val messagesRef = Firebase.database.getReference("chats").child(chatId).child("messages")
        messagesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                messages.clear()
                for (snapshot in dataSnapshot.children) {
                    val message = snapshot.getValue(Message::class.java)
                    Log.d("TAGG", "Message iss: $message")
                    if (message != null) {
                        messages.add(message)
                    }
                }
                chatAdapter.notifyDataSetChanged()
                binding.rvChats.scrollToPosition(messages.size - 1)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle possible errors.
                Log.w("TAG", "loadPost:onCancelled", databaseError.toException())
            }
        })
    }


    override fun onResume() {
        super.onResume()
        updateToolbar()
    }

    private fun updateToolbar() {
        (activity as? AppCompatActivity)?.supportActionBar?.apply {
            title = userEmail
            // Set other toolbar properties if needed, such as menu items
        }
    }

    private fun checkOrCreateChat(
        userId1: String,
        userId2: String,
        onChatIdReady: (String) -> Unit
    ) {
        val chatsRef = Firebase.database.reference.child("chats")
        val user1ChatsRef =
            Firebase.database.reference.child("users").child(userId1).child("listOfChats")

        user1ChatsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
//                var chatIdFound = false
//                var chatId: String? = null
//                val chatPromises = mutableListOf<Task<DataSnapshot>>()
//
//                if (snapshot.children.count() == 0) {
//                    Log.d("chats", "checkOrCreateChat: creating new chat because snapshot children is 0")
//                    createNewChat(chatsRef, user1ChatsRef, userId1, userId2, onChatIdReady)
//                    return
//                }
//
//                for (chatSnapshot in snapshot.children) {
//                    val chatKey = chatSnapshot.key
//                    Log.d("chats", "chatKey: $chatKey")
//                    val chatPromise = chatsRef.child(chatKey!!).child("participants").get()
//                    chatPromises.add(chatPromise)
//                    Log.d("chats", "chatPromises list: $chatPromises")
//
//                    chatPromise.addOnSuccessListener { participantsSnapshot ->
//                        val participants = participantsSnapshot.getValue(object : GenericTypeIndicator<Map<String, Boolean>>() {})
//                        Log.d("chats", "participants: $participants")
//                        if (participants != null && participants.containsKey(userId2)) {
//                            chatId = chatKey
//                            chatIdFound = true
//                            Log.d("chats", "chatIdFound: $chatIdFound")
//                            onChatIdReady(chatId!!)
//                        }
//
//                        if (chatPromises.size == snapshot.children.count() && !chatIdFound) {
//                            Log.d("TAGG", "checkOrCreateChat: creating new chat because chatPromises.size is 0")
//                            createNewChat(chatsRef, user1ChatsRef, userId1, userId2, onChatIdReady)
//                        }
//                    }.addOnFailureListener {
//                        // Handle error
//                    }
//                }
//                Tasks.whenAll(chatPromises).addOnCompleteListener {
//                    if (chatIdFound && chatId != null) {
//                        onChatIdReady(chatId!!)
//                    } else {
//                        Log.d("chats", "checkOrCreateChat: creating new chat because no existing chat was found")
//                        createNewChat(chatsRef, user1ChatsRef, userId1, userId2, onChatIdReady)
//                    }
//                }

                var chatPromises = mutableListOf<Task<DataSnapshot>>()
                var chatKey: String? = null

                if (snapshot.children.count() == 0) {
                    Log.d(
                        "chats",
                        "checkOrCreateChat: creating new chat because snapshot children is 0"
                    )
                    createNewChat(chatsRef, user1ChatsRef, userId1, userId2, onChatIdReady)
                    return
                }

                for (chatSnapshot in snapshot.children) {
                    chatKey = chatSnapshot.key
                    Log.d("chats", "chatKey: $chatKey")
                    val chatPromise = chatsRef.child(chatKey!!).child("participants").get()
                    chatPromises.add(chatPromise)
                }

                Tasks.whenAll(chatPromises).addOnCompleteListener {
                    Log.d("chats", "chatpromises: $chatPromises")
                    var chatIdFound = false
                    var chatId: String? = null

                    for (task in chatPromises) {
                        if (task.isSuccessful) {
                            val participantsSnapshot = task.result
                            val participants = participantsSnapshot.getValue(object :
                                GenericTypeIndicator<Map<String, Boolean>>() {})
                            Log.d("chats", "participants: $participants")
                            if (participants != null && participants.containsKey(userId2)) {
                                chatId = participantsSnapshot.ref.parent?.key
                                chatIdFound = true
//                                onChatIdReady(chatId!!)
                                break
                            }
//                            else if (chatPromises.size == snapshot.children.count() && !chatIdFound) {
//                                Log.d(
//                                    "chats",
//                                    "checkOrCreateChat: creating new chat ${chatPromises.size == snapshot.children.count()}"
//                                )
//                                createNewChat(
//                                    chatsRef,
//                                    user1ChatsRef,
//                                    userId1,
//                                    userId2,
//                                    onChatIdReady
//                                )
//                            }
                        }
                    }

                    if (chatIdFound && chatId != null) {
                        Log.d("chats", "Existing chat found, chatId: $chatId")
                        onChatIdReady(chatId)
                    }else{
                        Log.d("chats", "Creating new chat")
                        createNewChat(chatsRef, user1ChatsRef, userId1, userId2, onChatIdReady)
                    }
                }

            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    private fun createNewChat(
        chatsRef: DatabaseReference,
        user1ChatsRef: DatabaseReference,
        userId1: String,
        userId2: String,
        onChatIdReady: (String) -> Unit
    ) {
        val chatId = chatsRef.push().key
        if (chatId != null) {
            val chatData = mapOf(
                "participants" to mapOf(userId1 to true, userId2 to true)
            )

            val chatRef = chatsRef.child(chatId)
            chatRef.setValue(chatData)

            // Update users with the new chat ID
            user1ChatsRef.child(chatId)
                .setValue(mapOf("lastMessage" to "", "lastMessageTimestamp" to 0))

            val user2ChatsRef =
                Firebase.database.reference.child("users").child(userId2).child("listOfChats")
            user2ChatsRef.child(chatId)
                .setValue(mapOf("lastMessage" to "", "lastMessageTimestamp" to 0))

            onChatIdReady(chatId)
        }
    }


    private fun sendMessage(chatId: String, senderId: String, receiverId: String, message: String) {
        val messageId =
            Firebase.database.reference.child("chats").child(chatId).child("messages").push().key
        val messageData = mapOf(
            "sender" to senderId,
            "receiver" to receiverId,
            "message" to message,
            "timestamp" to System.currentTimeMillis()
        )
        Log.d("TAGG", "messageData: " + messageData.toString())

        if (messageId != null) {
            val messageRef =
                Firebase.database.reference.child("chats").child(chatId).child("messages")
                    .child(messageId)
            messageRef.setValue(messageData)

            // Update last message in both users' chat lists
            val user1ChatsRef =
                Firebase.database.reference.child("users").child(senderId).child("listOfChats")
                    .child(chatId)
            user1ChatsRef.child("lastMessage").setValue(message)
            user1ChatsRef.child("lastMessageTimestamp").setValue(System.currentTimeMillis())

            val user2ChatsRef =
                Firebase.database.reference.child("users").child(receiverId).child("listOfChats")
                    .child(chatId)
            user2ChatsRef.child("lastMessage").setValue(message)
            user2ChatsRef.child("lastMessageTimestamp").setValue(System.currentTimeMillis())
        }
    }

    private fun setupVoiceCall() {
        // Setup voice call

    }

    private fun setupVideoCall() {
        // Setup video call
    }
}
