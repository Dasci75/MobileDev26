package com.example.mobiledev.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobiledev.data.Chat
import com.example.mobiledev.data.Message
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ChatViewModel : ViewModel() {

    private val db = Firebase.firestore
    private val auth = Firebase.auth

    private val _chats = MutableStateFlow<List<Chat>>(emptyList())
    val chats: StateFlow<List<Chat>> = _chats.asStateFlow()

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val _currentChatId = MutableStateFlow<String?>(null)

    init {
        auth.currentUser?.uid?.let {
            loadChats(it)
        }
    }

    fun loadChats(userId: String) {
        db.collection("chats")
            .whereArrayContains("userIds", userId)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w("ChatViewModel", "Listen failed.", e)
                    return@addSnapshotListener
                }

                val chatList = snapshots?.map {
                    it.toObject(Chat::class.java).copy(id = it.id)
                } ?: emptyList()
                _chats.value = chatList
            }
    }

    fun loadMessages(chatId: String) {
        _currentChatId.value = chatId
        db.collection("chats").document(chatId)
            .collection("messages")
            .orderBy("timestamp")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w("ChatViewModel", "Listen failed.", e)
                    return@addSnapshotListener
                }
                val messageList = snapshots?.map { it.toObject(Message::class.java) } ?: emptyList()
                _messages.value = messageList
            }
    }

    fun sendMessage(chatId: String, text: String) {
        val senderId = auth.currentUser?.uid ?: return
        val message = Message(
            senderId = senderId,
            text = text,
            timestamp = com.google.firebase.Timestamp.now()
        )

        viewModelScope.launch {
            try {
                db.collection("chats").document(chatId)
                    .collection("messages").add(message).await()

                db.collection("chats").document(chatId).update("lastMessage", message).await()
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error sending message", e)
            }
        }
    }

    fun findOrCreateChat(ownerId: String, tripId: String, callback: (String) -> Unit) {
        val currentUser = auth.currentUser ?: return

        db.collection("chats")
            .whereEqualTo("tripId", tripId)
            .whereArrayContains("userIds", currentUser.uid!!)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    val newChat = Chat(
                        userIds = listOf(currentUser.uid!!, ownerId),
                        tripId = tripId,
                        createdAt = com.google.firebase.Timestamp.now()
                    )
                    db.collection("chats")
                        .add(newChat)
                        .addOnSuccessListener { documentReference ->
                            callback(documentReference.id)
                        }
                } else {
                    callback(documents.documents[0].id)
                }
            }
    }
}
