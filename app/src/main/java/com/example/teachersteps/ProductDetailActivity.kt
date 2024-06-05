package com.example.teachersteps

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import api.client.Client
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import api.client.Services
import com.bumptech.glide.Glide

class ProductDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_detail)

        val produtoNome = intent.getStringExtra("NOME_PRODUTO") ?: "Nome não disponível"
        val produtoDesc = intent.getStringExtra("DESCRICAO_PRODUTO") ?: "Descrição não disponível"
        val produtoId = intent.getIntExtra("ID_PRODUTO", 0)
        val produtoImg = intent.getStringExtra("IMAGEM_URL") ?: "Imagem não disponível"
        val produtoQtd = intent.getIntExtra("QUANTIDADE_DISPONIVEL", 0)

        findViewById<TextView>(R.id.txtNomeProduto).text = produtoNome
        findViewById<TextView>(R.id.txtDescricaoProduto).text = produtoDesc
        findViewById<TextView>(R.id.txtQuantidadeDisponivel).text = produtoQtd.toString()
        Glide.with(this).load(produtoImg).into(findViewById(R.id.imgProduto))

        val editTextQuantidade = findViewById<EditText>(R.id.editQuantidadeDesejada)
        val btnAdicionarCarrinho = findViewById<Button>(R.id.btnAdicionarAoCarrinho)

        val sharedPreferences = getSharedPreferences("Login", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getInt("userId", 0)

        val returnButton = findViewById<ImageButton>(R.id.imageButton)
        returnButton.setOnClickListener{
            val intent = Intent(this,MainActivity::class.java)
            startActivity(
                intent)
        }

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

        val apiService = Client.createServiceScalar(Services.ProductDetail::class.java)

        apiService.adicionarAoCarrinho(userId, produtoId, quantidade).enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@ProductDetailActivity, response.body() ?: "Sucesso!", Toast.LENGTH_SHORT).show()
                } else {
                    val errorMsg = when (response.code()) {
                        400 -> "Pedido inválido"
                        401 -> "Não autorizado"
                        500 -> "Erro no servidor"
                        else -> "Erro desconhecido"
                    }
                    Log.e("API_ERROR", "Resposta não bem-sucedida: ${response.code()} - ${response.message()}")
                    Toast.makeText(this@ProductDetailActivity, errorMsg, Toast.LENGTH_SHORT).show()
                }
            }


            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.e("API_ERROR", "Erro na API: ${t.message}", t)
                Toast.makeText(this@ProductDetailActivity, "Erro na API: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

}
