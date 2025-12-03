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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

@Composable
fun ChatListScreen(navController: NavController, chatViewModel: ChatViewModel = viewModel(), paddingValues: PaddingValues) {
    val chats by chatViewModel.chats.collectAsState()

    // Haal de UID op van de huidige gebruiker (bijv. "rumWV...")
    val currentUser = Firebase.auth.currentUser
    val currentUserId = currentUser?.uid

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(chats) { chat ->
            // Geef de ID door aan ChatItem
            ChatItem(chat = chat, currentUserId = currentUserId) {
                navController.navigate("chat/${chat.id}")
            }
        }
    }
}

@Composable
fun ChatItem(chat: Chat, currentUserId: String?, onClick: () -> Unit) {
    val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    // 1. Zoek het ID van de andere persoon (het ID dat NIET gelijk is aan mijn ID)
    val otherUserId = chat.userIds.firstOrNull { it != currentUserId }

    // 2. Maak een variabele om het e-mailadres in op te slaan (standaard even "Laden...")
    var otherUserEmail by remember { mutableStateOf("Laden...") }

    // 3. Haal de gegevens op uit Firestore zodra dit item op het scherm komt
    LaunchedEffect(otherUserId) {
        if (otherUserId != null) {
            Firebase.firestore.collection("users").document(otherUserId).get()
                .addOnSuccessListener { document ->
                    // Haal het veld 'email' op uit het user document
                    // Pas "email" aan als je veld in de database anders heet (bijv. "mail" of "username")
                    otherUserEmail = document.getString("email") ?: "Geen email gevonden"
                }
                .addOnFailureListener {
                    otherUserEmail = "Fout bij laden"
                }
        } else {
            otherUserEmail = "Zelf of onbekend"
        }
    }

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
                // 4. Toon hier het opgehaalde e-mailadres
                Text(
                    text = otherUserEmail,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
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