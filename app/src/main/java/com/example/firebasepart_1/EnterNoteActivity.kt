package com.example.firebasepart_1

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.firebasepart_1.ui.theme.FirebasePart_1Theme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class EnterNoteActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dbRef = FirebaseDatabase.getInstance().reference
        val auth = FirebaseAuth.getInstance()

        val noteId = intent.getStringExtra("uid") ?: ""
        val noteHeading = intent.getStringExtra("heading") ?: ""
        val noteContent = intent.getStringExtra("content") ?: ""
        setContent {
            EnterNoteScreen(dbRef, auth, noteId, noteHeading, noteContent)
        }
    }
}

@Composable
fun EnterNoteScreen(databaseReference: DatabaseReference, auth: FirebaseAuth, noteId: String, heading: String, content: String) {
    val context = LocalContext.current
    var noteHeading by remember {
        mutableStateOf(heading)
    }
    var noteContent by remember {
        mutableStateOf(content)
    }
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(value = noteHeading, onValueChange = {
            noteHeading = it
        },
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(top = 8.dp),
            label = {
                Text(text = "Heading")
            })

        OutlinedTextField(value = noteContent, onValueChange = {
            noteContent = it
        },
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(top = 8.dp)
                .weight(1f),
            label = {
                Text(text = "Enter your thought")
            })

        Button(onClick = {
            if(noteId.isBlank()) {
                val dbRef = databaseReference.child("users").child(auth.currentUser!!.uid).push()
                val note = Note(noteHeading, noteContent, dbRef.key!!)
                dbRef.setValue(note)
                    .addOnCompleteListener { task ->
                        if(task.isSuccessful) {
                            Toast.makeText(context, "note added successfully", Toast.LENGTH_SHORT).show()
                            val activity = context as Activity
                            activity.setResult(Activity.RESULT_OK)
                            activity.finish()
                        } else {
                            Toast.makeText(context, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                val note = Note(noteHeading, noteContent, noteId)
                val dbRef = databaseReference.child("users").child(auth.currentUser!!.uid).child(noteId)
                dbRef.setValue(note)
                    .addOnCompleteListener { task ->
                        if(task.isSuccessful) {
                            Toast.makeText(context, "note updated successfully", Toast.LENGTH_SHORT).show()
                            val activity = context as Activity
                            activity.setResult(Activity.RESULT_OK)
                            activity.finish()
                        } else {
                            Toast.makeText(context, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            }

        }, modifier = Modifier
            .padding(top = 8.dp)
            .fillMaxWidth(0.9f)) {
            Text(text = "Add Note")
        }
    }
}











