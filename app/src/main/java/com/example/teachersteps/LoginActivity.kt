package com.example.teachersteps

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import android.util.Log
import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query

class LoginActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        val loginButton: Button = findViewById(R.id.loginButton)

        loginButton.setOnClickListener {
            blockLogin()
        }
    }

    private fun blockLogin() {
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://echo-api-senac.vercel.app")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ApiService::class.java)

        val call = apiService.login(email, password)
        call.enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val loginResponses = response.body()!!
                    if (loginResponses.usuarioId != 0) {
                        val sharedPreferences = getSharedPreferences("Login", Context.MODE_PRIVATE)
                        val editor = sharedPreferences.edit()
                        val userId = loginResponses.usuarioId
                        Log.d("ProdutoDetalhesActivity", "UserId recuperado: $userId")
                        editor.putInt("userId", userId)
                        editor.apply()

                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(
                            this@LoginActivity,
                            "Usuário ou senha inválidos",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    Log.e(
                        "LoginActivity",
                        "Login failed: HTTP error code: " + response.code() + " msg: " + response.message()
                    )
                    Toast.makeText(this@LoginActivity, "Login failed", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Log.e("LoginActivity", "onFailure: " + t.message)
                Toast.makeText(this@LoginActivity, "Error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }



    interface ApiService {
        @GET("/login")
        fun login(
            @Query("usuario") usuario: String,
            @Query("senha") senha: String
        ): Call<LoginResponse>
    }

    data class LoginResponse(
        @SerializedName("USUARIO_ID") val usuarioId: Int,
        @SerializedName("USUARIO_NOME") val usuarioNome: String,
        @SerializedName("USUARIO_EMAIL") val usuarioEmail: String,
        @SerializedName("USUARIO_SENHA") val usuarioSenha: String,
        @SerializedName("USUARIO_CPF") val usuarioCpf: String,
    )
}