package com.example.firebasepart_1

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.integerArrayResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.firebasepart_1.ui.theme.FirebasePart_1Theme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class MainActivity : ComponentActivity() {

    private val updateLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if(result.resultCode == RESULT_OK) {
            recreate()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val auth = FirebaseAuth.getInstance()
        val databaseReference = FirebaseDatabase.getInstance().reference
        setContent {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                auth.currentUser?.email?.let {
                    Text(text = it, style = TextStyle(fontSize = 22.sp), modifier = Modifier.padding(12.dp))
                }
                FirebaseDBNotesList(auth, databaseReference, {
                    val intent = Intent(this@MainActivity, EnterNoteActivity::class.java)
                    intent.putExtra("uid", it.uniqueId)
                    intent.putExtra("heading", it.heading)
                    intent.putExtra("content", it.content)
                    updateLauncher.launch(intent)
                }, {
                    databaseReference.child("users").child(auth.uid!!).child(it.uniqueId).removeValue()
                        .addOnCompleteListener { task ->
                            if(task.isSuccessful) {
                                this@MainActivity.recreate()
                            }
                        }
                })
            }
            LogOut(auth)
            FabAddNote()
        }
    }
}

@Composable
fun LogOut(auth: FirebaseAuth) {
    val context = LocalContext.current
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomStart
    ) {
        FloatingActionButton(onClick = {
            auth.signOut()
            (context as Activity).finish()
            context.startActivity(
                Intent(context, LogInActivity::class.java)
            )
        }, modifier = Modifier.padding(16.dp)) {
            Image(
                painter = painterResource(id = R.drawable.logout),
                modifier = Modifier.size(16.dp),
                contentDescription = null
            )
        }
    }
}

@Composable
fun FabAddNote() {
    val context = LocalContext.current
    val addEntryLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) { result ->
        if(result.resultCode == Activity.RESULT_OK) {
            Toast.makeText(context, "screen refreshed!!!", Toast.LENGTH_SHORT).show()
            (context as Activity).recreate()
        }
    }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomEnd
    ) {
        FloatingActionButton(onClick = {
            val intent = Intent(context, EnterNoteActivity::class.java)
            addEntryLauncher.launch(intent)
        }, modifier = Modifier.padding(16.dp)) {
            Image(painter = painterResource(id = R.drawable.ic_plus), contentDescription = null)
        }
    }
}

@Composable
fun FirebaseDBNotesList(
    auth: FirebaseAuth,
    databaseReference: DatabaseReference,
    onNoteClicked: (Note) -> Unit,
    onDeleteClicked: (Note) -> Unit
) {
    val currentUser = auth.currentUser
    var mainNoteList by remember {
        mutableStateOf<List<Note?>>(emptyList())
    }

    LaunchedEffect(currentUser) {
        if(currentUser != null) {
            val databaseSnapShot = databaseReference.child("users").child(currentUser.uid).get().await()
            val notes = mutableListOf<Note?>()

            databaseSnapShot.children.forEach { child ->
                val note = child.getValue(Note::class.java)
                if(note != null) {
                    notes.add(note)
                }
            }

            mainNoteList = notes
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        if(mainNoteList.isEmpty()) {
            Text(text = "No data found", style = TextStyle(fontSize = 16.sp))
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {
                items(mainNoteList) {
                    if(it != null) {
                        NoteUI(note = it, {
                            onNoteClicked(it)
                        }, {
                            onDeleteClicked(it)
                        })
                    }
                }
            }
        }

    }
}

@Composable
fun NoteUI(note: Note, onNoteClicked: () -> Unit, onDeleteClicked:() -> Unit) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable {
                onNoteClicked()
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .padding(12.dp)
                    .weight(1f)
            ) {
                Text(text = note.heading, style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = note.content,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            Image(
                painter = painterResource(id = R.drawable.ic_delete),
                modifier = Modifier
                    .size(36.dp)
                    .clickable {
                        onDeleteClicked()
                    },
                contentDescription = null
            )
        }
    }
}











