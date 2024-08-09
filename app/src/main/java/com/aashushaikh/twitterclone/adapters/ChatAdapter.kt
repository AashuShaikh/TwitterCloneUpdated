import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.aashushaikh.twitterclone.R
import com.aashushaikh.twitterclone.data.Message
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatAdapter(private val messages: List<Message>, private val currentUserId: String) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_RECEIVED = 1
        private const val TYPE_SENT = 2
    }

    override fun getItemViewType(position: Int): Int {
        val message = messages[position]
        Log.d("TAGG", "Message sender: ${message.sender}, Current user: $currentUserId")
        return if (message.sender == currentUserId) {
            TYPE_SENT
        } else {
            TYPE_RECEIVED
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_SENT -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_message_sent, parent, false)
                SentMessageViewHolder(view)
            }
            TYPE_RECEIVED -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_message_received, parent, false)
                ReceivedMessageViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        when (holder) {
            is SentMessageViewHolder -> holder.bind(message)
            is ReceivedMessageViewHolder -> holder.bind(message)
        }
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    class SentMessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val messageTextView: TextView = view.findViewById(R.id.tv_message)
        private val timestampTextView: TextView = view.findViewById(R.id.tv_timestamp)

        fun bind(message: Message) {
            messageTextView.text = message.message
            timestampTextView.text = message.timestamp.toString()
        }
    }

    class ReceivedMessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val messageTextView: TextView = view.findViewById(R.id.tv_message)
        private val timestampTextView: TextView = view.findViewById(R.id.tv_timestamp)

        fun bind(message: Message) {
            messageTextView.text = message.message
            timestampTextView.text = message.timestamp.toString()
        }
    }
}
