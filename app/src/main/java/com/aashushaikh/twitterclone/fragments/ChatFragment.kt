import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.aashushaikh.twitterclone.R
import com.aashushaikh.twitterclone.data.Message
import com.aashushaikh.twitterclone.databinding.FragmentChatBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class ChatFragment : Fragment() {

    private lateinit var binding: FragmentChatBinding
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
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_chat, container, false)
        userEmail = arguments?.getString("email").toString()
        userProfile = arguments?.getString("profile").toString()
        userId = arguments?.getString("userId").toString()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Toast.makeText(requireContext(), "User Email: $userEmail and User Profile link: $userProfile", Toast.LENGTH_SHORT).show()

        val currentUser = Firebase.auth.currentUser!!.uid
        chatAdapter = ChatAdapter(messages, currentUser)
        binding.rvChats.adapter = chatAdapter

        initChat()

        binding.sendChat.setOnClickListener {
            sendMessage(chatId, Firebase.auth.currentUser!!.uid, userId, binding.etChat.text.toString())
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
        Firebase.database.getReference("chats").child(chatId).child("messages")
            .addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val message = snapshot.getValue(Message::class.java)
                    if (message != null) {
                        messages.add(message)
                        chatAdapter.notifyItemInserted(messages.size - 1)
                        binding.rvChats.scrollToPosition(messages.size - 1)
                    }
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onChildRemoved(snapshot: DataSnapshot) {}
                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onCancelled(error: DatabaseError) {}
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

    private fun checkOrCreateChat(userId1: String, userId2: String, onChatIdReady: (String) -> Unit) {
        val user1ChatsRef = Firebase.database.reference.child("users").child(userId1).child("listOfChats")

        user1ChatsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var chatId: String? = null
                for (chatSnapshot in snapshot.children) {
                    val chatData = chatSnapshot.value as Map<*, *>
                    val participants = chatData["participants"] as? Map<*, *>
                    if (participants != null && participants.containsKey(userId2)) {
                        chatId = chatSnapshot.key
                        break
                    }
                }

                if (chatId == null) {
                    // No existing chat found, create a new one
                    chatId = Firebase.database.reference.child("chats").push().key
                    if (chatId != null) {
                        val chatData = mapOf(
                            "participants" to mapOf(userId1 to true, userId2 to true)
                        )

                        val chatRef = Firebase.database.reference.child("chats").child(chatId)
                        chatRef.setValue(chatData)

                        // Update users with the new chat ID
                        user1ChatsRef.child(chatId).setValue(mapOf("lastMessage" to "", "lastMessageTimestamp" to 0))

                        val user2ChatsRef = Firebase.database.reference.child("users").child(userId2).child("listOfChats")
                        user2ChatsRef.child(chatId).setValue(mapOf("lastMessage" to "", "lastMessageTimestamp" to 0))
                    }
                }

                if (chatId != null) {
                    onChatIdReady(chatId)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    private fun sendMessage(chatId: String, senderId: String, receiverId: String, message: String) {
        val messageId = Firebase.database.reference.child("chats").child(chatId).child("messages").push().key
        val messageData = mapOf(
            "sender" to senderId,
            "receiver" to receiverId,
            "message" to message,
            "timestamp" to System.currentTimeMillis()
        )

        if (messageId != null) {
            val messageRef = Firebase.database.reference.child("chats").child(chatId).child("messages").child(messageId)
            messageRef.setValue(messageData)

            // Update last message in both users' chat lists
            val user1ChatsRef = Firebase.database.reference.child("users").child(senderId).child("listOfChats").child(chatId)
            user1ChatsRef.child("lastMessage").setValue(message)
            user1ChatsRef.child("lastMessageTimestamp").setValue(System.currentTimeMillis())

            val user2ChatsRef = Firebase.database.reference.child("users").child(receiverId).child("listOfChats").child(chatId)
            user2ChatsRef.child("lastMessage").setValue(message)
            user2ChatsRef.child("lastMessageTimestamp").setValue(System.currentTimeMillis())
        }
    }
}
