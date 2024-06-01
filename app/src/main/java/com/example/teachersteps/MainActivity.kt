package com.example.teachersteps

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import com.bumptech.glide.Glide

class MainActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CustomAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerViewProdutos)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val logging = HttpLoggingInterceptor { message ->
            Log.d("OkHttp", message)
        }.apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        val btnCarrinho = findViewById<Button>(R.id.Carrinho)
        val sharedPreferences = getSharedPreferences("Login", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getInt("userId", 0)

        btnCarrinho.setOnClickListener {
            val intent = Intent(this, CartActivity::class.java)
            intent.putExtra("userId", userId)
            startActivity(intent)
            finish()
        }

        val retrofit = Retrofit.Builder()
            .baseUrl("https://echo-api-senac.vercel.app")
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()

        val apiService = retrofit.create(ApiService::class.java)

        apiService.getProdutos().enqueue(object : Callback<List<Produto>> {
            override fun onResponse(call: Call<List<Produto>>, response: Response<List<Produto>>) {
                if (response.isSuccessful) {
                    val produtos = response.body() ?: emptyList()
                    adapter = CustomAdapter(produtos)
                    recyclerView.adapter = adapter
                } else {
                    Log.e("API Error", "Response not successful. Code: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<Produto>>, t: Throwable) {
                Log.e("API Failure", "Error fetching products", t)
            }
        })
    }

    data class ProdutoAtivo(
        @SerializedName("type") val type: String,
        @SerializedName("data") val data: List<Int>
    )

    data class Produto(
        @SerializedName("PRODUTO_ID") val produtoId: Int,
        @SerializedName("PRODUTO_NOME") val produtoNome: String,
        @SerializedName("PRODUTO_DESC") val produtoDesc: String,
        @SerializedName("PRODUTO_PRECO") val produtoPreco: String,
        @SerializedName("PRODUTO_DESCONTO") val produtoDesconto: String,
        @SerializedName("CATEGORIA_ID") val categoriaId: Int,
        @SerializedName("PRODUTO_ATIVO") val produtoAtivo: ProdutoAtivo,
        @SerializedName("IMAGEM_URL") val imagemUrl: String?,
        @SerializedName("QUANTIDADE_DISPONIVEL") val quantidadeDisponivel: Int
    )

    interface ApiService {
        @GET("/products")
        fun getProdutos(): Call<List<Produto>>
    }

    class CustomAdapter(private val dataSet: List<Produto>) :
        RecyclerView.Adapter<CustomAdapter.ViewHolder>() {

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val nome: TextView = view.findViewById(R.id.nomeProduto)
            val descricao: TextView = view.findViewById(R.id.descricaoProduto)
            val valor: TextView = view.findViewById(R.id.valorProduto)
            val imagem: ImageView = view.findViewById(R.id.imagem_produto)
            val btnComprar: Button = view.findViewById(R.id.btnComprar)
        }

        override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.item_produto, viewGroup, false)

            return ViewHolder(view)
        }

        override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
            val produto = dataSet[position]

            viewHolder.nome.text = produto.produtoNome
            viewHolder.descricao.text = produto.produtoDesc
            viewHolder.valor.text = produto.produtoPreco

            Glide.with(viewHolder.itemView.context)
                .load(produto.imagemUrl)
                .placeholder(R.drawable.ic_launcher_background)
                .error(com.google.android.material.R.drawable.mtrl_ic_error)
                .into(viewHolder.imagem)

            viewHolder.btnComprar.setOnClickListener {
                val intent = Intent(viewHolder.itemView.context, ProductDetailActivity::class.java)
                intent.putExtra("ID_PRODUTO", produto.produtoId)
                intent.putExtra("NOME_PRODUTO", produto.produtoNome)
                intent.putExtra("DESCRICAO_PRODUTO", produto.produtoDesc)
                intent.putExtra("QUANTIDADE_DISPONIVEL", produto.quantidadeDisponivel)
                viewHolder.itemView.context.startActivity(intent)
            }
        }

        override fun getItemCount() = dataSet.size
    }
}
