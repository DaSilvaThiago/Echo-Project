package com.example.teachersteps

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import api.client.Adapters
import api.client.Client
import api.client.Responses
import api.client.Services
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {
    private lateinit var recyclerViewProdutos: RecyclerView
    private lateinit var recyclerViewProdutosInternacionais: RecyclerView
    private lateinit var recyclerViewProdutosIntercambio: RecyclerView

    private lateinit var adapterProdutos: Adapters.AdapterMainActivity
    private lateinit var adapterProdutosInternacionais: Adapters.AdapterMainActivity
    private lateinit var adapterProdutosIntercambio: Adapters.AdapterMainActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerViewProdutos = findViewById(R.id.recyclerViewProdutos)
        recyclerViewProdutos.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        recyclerViewProdutosInternacionais = findViewById(R.id.recyclerViewProdutosInternacionais)
        recyclerViewProdutosInternacionais.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        recyclerViewProdutosIntercambio = findViewById(R.id.recyclerViewProdutosintercambio)
        recyclerViewProdutosIntercambio.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        val btnCarrinho = findViewById<ImageButton>(R.id.cartBtn)
        val sharedPreferences = getSharedPreferences("Login", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getInt("userId", 0)

        btnCarrinho.setOnClickListener {
            val intent = Intent(this, CartActivity::class.java)
            intent.putExtra("userId", userId)
            startActivity(intent)
            finish()
        }

        val apiService = Client.createService(Services.Products::class.java)

        // Carregar produtos
        apiService.getProdutos().enqueue(object : Callback<List<Responses.Product>> {
            override fun onResponse(call: Call<List<Responses.Product>>, response: Response<List<Responses.Product>>) {
                if (response.isSuccessful) {
                    val produtos = response.body() ?: emptyList()
                    adapterProdutos = Adapters.AdapterMainActivity(produtos)
                    recyclerViewProdutos.adapter = adapterProdutos
                } else {
                    Log.e("API Error", "Response not successful. Code: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<Responses.Product>>, t: Throwable) {
                Log.e("API Failure", "Error fetching products", t)
            }
        })

        // Carregar produtos internacionais (substitua `getProdutosInternacionais` pela API correta)
        apiService.getProdutos().enqueue(object : Callback<List<Responses.Product>> {
            override fun onResponse(call: Call<List<Responses.Product>>, response: Response<List<Responses.Product>>) {
                if (response.isSuccessful) {
                    val produtosInternacionais = response.body() ?: emptyList()
                    adapterProdutosInternacionais = Adapters.AdapterMainActivity(produtosInternacionais)
                    recyclerViewProdutosInternacionais.adapter = adapterProdutosInternacionais
                } else {
                    Log.e("API Error", "Response not successful. Code: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<Responses.Product>>, t: Throwable) {
                Log.e("API Failure", "Error fetching international products", t)
            }
        })

        // Carregar produtos de intercâmbio (substitua `getProdutosIntercambio` pela API correta)
        apiService.getProdutos().enqueue(object : Callback<List<Responses.Product>> {
            override fun onResponse(call: Call<List<Responses.Product>>, response: Response<List<Responses.Product>>) {
                if (response.isSuccessful) {
                    val produtosIntercambio = response.body() ?: emptyList()
                    adapterProdutosIntercambio = Adapters.AdapterMainActivity(produtosIntercambio)
                    recyclerViewProdutosIntercambio.adapter = adapterProdutosIntercambio
                } else {
                    Log.e("API Error", "Response not successful. Code: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<Responses.Product>>, t: Throwable) {
                Log.e("API Failure", "Error fetching exchange products", t)
            }
        })
    }
}
