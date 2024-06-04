package com.example.teachersteps

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import api.client.Client
import api.client.Responses
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Response

class RegisterUserActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_user)

        val loginPage = findViewById<Button>(R.id.loginPage)
        val returnButton = findViewById<ImageButton>(R.id.imageButton)
        loginPage.setOnClickListener{
            val intent = Intent(this,LoginActivity::class.java)
            startActivity(intent)
        }
        returnButton.setOnClickListener{
            val intent = Intent(this,LoginActivity::class.java)
            startActivity(
                intent)
        }

        val nameEditText = findViewById<EditText>(R.id.nameEditText)
        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val cpfEditText = findViewById<EditText>(R.id.cpfEditText)
        val passwordEditText = findViewById<TextInputEditText>(R.id.passwordEditText)
        val registerButton = findViewById<Button>(R.id.createBtn)

        registerButton.setOnClickListener {
            val name = nameEditText.text.toString()
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            val cpf = cpfEditText.text.toString()


            val user = Responses.RegisterUser(name, email, password, cpf)
            registerUser(user)
        }
    }

    private fun registerUser(user: Responses.RegisterUser) {
        val intent = Intent(this,CongratulationsActivity::class.java)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response: Response<Responses.RegisterUserResponse> = Client.RetrofitRegister.api.registerUser(user)
                runOnUiThread {
                    if (response.isSuccessful) {
                        startActivity(intent)
                    } else {
                        Toast.makeText(this@RegisterUserActivity, "Registration Failed: ${response.errorBody()?.string()}", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@RegisterUserActivity, "An error occurred: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }





}