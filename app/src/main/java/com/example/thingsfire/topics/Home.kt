package com.example.thingsfire.topics

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth

@Composable
fun HomeScreen(onLogout: () -> Unit) {
    val auth = FirebaseAuth.getInstance()
    val userEmail = auth.currentUser?.email ?: "User"

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Welcome Home!",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(text = "Logged in as: $userEmail")

        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = {
            auth.signOut() // Logs user out of Firebase
            onLogout()     // Triggers the navigation back to Login
        }) {
            Text("Logout")
        }
    }
}