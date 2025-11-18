package com.example.mobiledev

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    auth: FirebaseAuth = Firebase.auth,
    db: FirebaseFirestore = Firebase.firestore,
    onLoginSuccess: () -> Unit = {}
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var isLoginMode by remember { mutableStateOf(true) } // true = login, false = register

    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // App Title
        Text(
            text = "CityTrip",
            style = MaterialTheme.typography.headlineLarge,
            color = Color(0xFFFF5C00),
            modifier = Modifier.padding(bottom = 40.dp)
        )

        // Login/Register Title
        Text(
            text = if (isLoginMode) "Login" else "Registreer",
            style = MaterialTheme.typography.headlineMedium,
            color = Color(0xFFFF5C00),
            modifier = Modifier.padding(bottom = 40.dp)
        )

        // Error Message
        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = Color.Red,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )
        }

        // Email Field
        OutlinedTextField(
            value = username,
            onValueChange = {
                username = it
                errorMessage = ""
            },
            label = { Text("Email") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        // Password Field
        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                errorMessage = ""
            },
            label = { Text("Wachtwoord") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true
        )

        // Submit Button
        Button(
            onClick = {
                if (username.isEmpty() || password.isEmpty()) {
                    errorMessage = "Vul alle velden in"
                    return@Button
                }

                isLoading = true
                coroutineScope.launch {
                    try {
                        if (isLoginMode) {
                            // Login
                            auth.signInWithEmailAndPassword(username, password)
                                .addOnCompleteListener { task ->
                                    isLoading = false
                                    if (task.isSuccessful) {
                                        onLoginSuccess()
                                    } else {
                                        errorMessage = "Login mislukt: ${task.exception?.message}"
                                    }
                                }
                        } else {
                            // Register
                            auth.createUserWithEmailAndPassword(username, password)
                                .addOnCompleteListener { task ->
                                    isLoading = false
                                    if (task.isSuccessful) {
                                        // Save user data to Firestore
                                        val user = auth.currentUser
                                        if (user != null) {
                                            val userData = hashMapOf(
                                                "email" to username,
                                                "createdAt" to com.google.firebase.Timestamp.now(),
                                                "userId" to user.uid
                                            )

                                            db.collection("users").document(user.uid)
                                                .set(userData)
                                                .addOnSuccessListener {
                                                    errorMessage = "Registratie succesvol!"
                                                }
                                                .addOnFailureListener { e ->
                                                    errorMessage = "Registratie mislukt: ${e.message}"
                                                }
                                        }
                                    } else {
                                        errorMessage = "Registratie mislukt: ${task.exception?.message}"
                                    }
                                }
                        }
                    } catch (e: Exception) {
                        isLoading = false
                        errorMessage = "Fout: ${e.message}"
                    }
                }
            },
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(bottom = 16.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White)
            } else {
                Text(if (isLoginMode) "Login" else "Registreer")
            }
        }

        // Switch between Login and Register
        TextButton(
            onClick = {
                isLoginMode = !isLoginMode
                errorMessage = ""
            }
        ) {
            Text(
                text = if (isLoginMode) "Nog geen account? Registreer" else "Al account? Login",
                color = Color(0xFFFF5C00)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            LoginScreen()
        }
    }
}
