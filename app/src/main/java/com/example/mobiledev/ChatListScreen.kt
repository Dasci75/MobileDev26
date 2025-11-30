package com.example.mobiledev

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.mobiledev.data.Chat
import com.example.mobiledev.ui.ChatViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun ChatListScreen(navController: NavController, chatViewModel: ChatViewModel = viewModel(), paddingValues: PaddingValues) {
    val chats by chatViewModel.chats.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(chats) { chat ->
            ChatItem(chat = chat) {
                navController.navigate("chat/${chat.id}")
            }
        }
    }
}

@Composable
fun ChatItem(chat: Chat, onClick: () -> Unit) {
    val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Chat with owner", fontWeight = FontWeight.Bold) // Placeholder
                chat.lastMessage?.let {
                    Text(text = it.text, style = MaterialTheme.typography.bodySmall)
                }
            }
            chat.lastMessage?.timestamp?.let {
                Text(
                    text = dateFormat.format(it.toDate()),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
