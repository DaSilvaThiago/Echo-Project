package com.example.teachersteps

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import retrofit2.converter.gson.GsonConverterFactory

class ProductDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_detail)

        val nomeProduto = intent.getStringExtra("NOME_PRODUTO") ?: "Nome não disponível"
        val descricaoProduto = intent.getStringExtra("DESCRICAO_PRODUTO") ?: "Descrição não disponível"
        val produtoId = intent.getIntExtra("ID_PRODUTO", 0)
        val quantidadeDisponivel = intent.getIntExtra("QUANTIDADE_DISPONIVEL", 0)

        findViewById<TextView>(R.id.txtNomeProduto).text = nomeProduto
        findViewById<TextView>(R.id.txtDescricaoProduto).text = descricaoProduto
        findViewById<TextView>(R.id.txtQuantidadeDisponivel).text = quantidadeDisponivel.toString()

        val editTextQuantidade = findViewById<EditText>(R.id.editQuantidadeDesejada)
        val btnAdicionarCarrinho = findViewById<Button>(R.id.btnAdicionarAoCarrinho)

        val sharedPreferences = getSharedPreferences("Login", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getInt("userId", 0)

        btnAdicionarCarrinho.setOnClickListener {
            val quantidadeDesejada = editTextQuantidade.text.toString().toIntOrNull() ?: 0

            if (quantidadeDesejada <= 0) {
                Toast.makeText(this, "Por favor, insira uma quantidade válida", Toast.LENGTH_SHORT).show()
            } else {
                adicionarAoCarrinho(userId, produtoId, quantidadeDesejada)
            }
        }
    }

    private fun adicionarAoCarrinho(userId: Int, produtoId: Int, quantidade: Int) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://echo-api-senac.vercel.app")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(ApiService::class.java)
        api.adicionarAoCarrinho(userId, produtoId, quantidade).enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@ProductDetailActivity, response.body() ?: "Sucesso!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@ProductDetailActivity, "Resposta não bem-sucedida: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Toast.makeText(this@ProductDetailActivity, "Erro na API: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    interface ApiService {
        @FormUrlEncoded
        @POST("/cart")
        fun adicionarAoCarrinho(
            @Field("USUARIO_ID") userId: Int,
            @Field("PRODUTO_ID") produtoId: Int,
            @Field("ITEM_QTD") quantidade: Int
        ): Call<String>
    }
}

