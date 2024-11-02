package com.example.firebasepart_1

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.firebasepart_1.ui.theme.FirebasePart_1Theme
import com.google.firebase.auth.FirebaseAuth

class LogInActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val auth = FirebaseAuth.getInstance()
        setContent {
            LoginScreen(auth)
        }
    }
}

@Composable
fun LoginScreen(auth: FirebaseAuth) {

    val context = LocalContext.current

    var email by remember {
        mutableStateOf("")
    }

    var password by remember {
        mutableStateOf("")
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Log In Screen", style = TextStyle(fontSize = 24.sp))
        OutlinedTextField(value = email, onValueChange = {
            email = it
        },
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(top = 20.dp),
            label = {
                Text(text = "Email")
            })

        OutlinedTextField(value = password, onValueChange = {
            password = it
        },
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(top = 8.dp),
            label = {
                Text(text = "Password")
            })

        Button(onClick = {
                         logInUser(auth, email, password, context)
        }, modifier = Modifier
            .padding(top = 8.dp)
            .fillMaxWidth(0.9f)) {
            Text(text = "LOG IN")
        }

        Button(onClick = {
            val intent = Intent(context, SignUpActivity::class.java)
            context.startActivity(
                intent
            )
        }, modifier = Modifier
            .padding(top = 8.dp)
            .fillMaxWidth(0.9f)) {
            Text(text = "SIGN UP")
        }
    }
}

fun logInUser(
    auth: FirebaseAuth,
    email: String,
    password: String,
    context: Context
) {
    if(email.isNotBlank() && password.isNotBlank()) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if(task.isSuccessful) {
                    Toast.makeText(context, "User Logged In Successfully", Toast.LENGTH_SHORT).show()
                    context.startActivity(
                        Intent(
                            context, MainActivity::class.java
                        )
                    )
                    (context as Activity).finish()
                } else {
                    Toast.makeText(context, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    } else {
        Toast.makeText(context, "Please enter valid user name and password", Toast.LENGTH_SHORT).show()
    }
}








