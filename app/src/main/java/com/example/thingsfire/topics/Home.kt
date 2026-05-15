package com.example.thingsfire.topics

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

data class Channel(val name: String = "", val id: String = "")

@Composable
fun HomeScreen(onLogout: () -> Unit) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()

    // Realtime Database initialization
    val db = FirebaseDatabase.getInstance().reference
    val userId = auth.currentUser?.uid
    val userEmail = auth.currentUser?.email ?: "User"

    var showDialog by remember { mutableStateOf(false) }
    var channelName by remember { mutableStateOf("") }
    var channelId by remember { mutableStateOf("") }
    var channels by remember { mutableStateOf(listOf<Channel>()) }

    // 1. Listen for changes in Realtime Database automatically
    DisposableEffect(userId) {
        var userChannelsRef: com.google.firebase.database.DatabaseReference? = null
        var listener: ValueEventListener? = null

        if (userId != null) {
            userChannelsRef = db.child("users").child(userId).child("channels")

            listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val items = mutableListOf<Channel>()
                    for (childSnapshot in snapshot.children) {
                        val channel = childSnapshot.getValue(Channel::class.java)
                        if (channel != null) {
                            items.add(channel)
                        }
                    }
                    channels = items
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, "Failed to load data", Toast.LENGTH_SHORT).show()
                }
            }

            userChannelsRef.addValueEventListener(listener)
        }

        // Properly detach listener when leaving the screen to prevent memory leaks
        onDispose {
            if (userChannelsRef != null && listener != null) {
                userChannelsRef!!.removeEventListener(listener!!)
            }
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialog = true },
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Item")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("My Channels", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text("Logged in as: $userEmail", fontSize = 12.sp)

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
                items(channels) { channel ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                    ) {
                        ListItem(
                            headlineContent = { Text(channel.name) },
                            supportingContent = { Text("ID: ${channel.id}") }
                        )
                    }
                }
            }

            Button(onClick = { auth.signOut(); onLogout() }) {
                Text("Logout")
            }
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Add New Channel") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = channelName,
                            onValueChange = { channelName = it },
                            label = { Text("Name") }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = channelId,
                            onValueChange = { channelId = it },
                            label = { Text("Channel ID") }
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (channelName.isNotBlank() && channelId.isNotBlank() && userId != null) {
                                val newChannel = Channel(channelName, channelId)

                                // Save cleanly to Realtime Database keyed by the channel id
                                db.child("users")
                                    .child(userId)
                                    .child("channels")
                                    .child(channelId)
                                    .setValue(newChannel)
                                    .addOnSuccessListener {
                                        showDialog = false
                                        channelName = ""
                                        channelId = ""
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(context, "Error saving to Realtime DB", Toast.LENGTH_SHORT).show()
                                    }
                            }
                        }
                    ) { Text("Save") }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) { Text("Cancel") }
                }
            )
        }
    }
}