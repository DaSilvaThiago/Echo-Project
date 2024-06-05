package com.example.teachersteps

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import api.client.Adapters
import api.client.Client
import com.bumptech.glide.Glide
import retrofit2.Call
import api.client.Responses
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import api.client.Services

class CartActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var totalTextView: TextView
    private lateinit var goToPaymentButton: Button
    private var total: Double = 0.0
    private var userId: Int = 0
    private var items: MutableList<Responses.ProductCart> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        val sharedPreferences = getSharedPreferences("Login", Context.MODE_PRIVATE)
        userId = sharedPreferences.getInt("userId", 0)

        recyclerView = findViewById(R.id.cartRecyclerView)
        totalTextView = findViewById(R.id.totalTextView)
        goToPaymentButton = findViewById(R.id.goToPaymentButton)

        recyclerView.layoutManager = LinearLayoutManager(this)
        fetchCartItems()

        val returnButton = findViewById<ImageButton>(R.id.imageButton)
        returnButton.setOnClickListener{
            val intent = Intent(this,MainActivity::class.java)
            startActivity(
                intent)
        }

        goToPaymentButton.setOnClickListener {
            val intent = Intent(this, PaymentActivity::class.java).apply {
                putExtra("TOTAL", total)
                putExtra("USER", userId)
                putParcelableArrayListExtra("PRODUCT_LIST", ArrayList(items))
            }
            Log.d("CART_ACTIVITY", "Lista de produtos a ser enviada: $items")
            startActivity(intent)
        }
    }

    private fun fetchCartItems() {
        val apiService = Client.createService(Services.Cart::class.java)

        apiService.getCartItems(userId).enqueue(object : Callback<List<Responses.ProductCart>> {
            override fun onResponse(call: Call<List<Responses.ProductCart>>, response: Response<List<Responses.ProductCart>>) {
                if (response.isSuccessful) {
                    val cartItems = response.body()?.filter { it.quantidadeDisponivel > 0 }?.toMutableList() ?: mutableListOf()
                    if (cartItems.isEmpty()) {
                        Log.d("CartActivity", "No items in cart.")
                    } else {
                        recyclerView.adapter = Adapters.AdapterCart(cartItems, this@CartActivity, userId) {
                            updateTotal(cartItems)
                        }
                    }
                    items = cartItems // Atualizando a lista de items
                    updateTotal(cartItems)
                    Log.d("CartActivity", "Cart items retrieved: $cartItems")
                } else {
                    Toast.makeText(this@CartActivity, "Failed to fetch cart items", Toast.LENGTH_SHORT).show()
                    Log.e("CartActivity", "Error fetching cart items: ${response.code()} - ${response.message()}")
                }
            }

            override fun onFailure(call: Call<List<Responses.ProductCart>>, t: Throwable) {
                Toast.makeText(this@CartActivity, "Error connecting to the server", Toast.LENGTH_SHORT).show()
                Log.e("CartActivity", "Error connecting to the server: ${t.message}", t)
            }
        })
    }

    private fun updateTotal(cartItems: List<Responses.ProductCart>) {
        total = cartItems.sumOf { it.produtoPreco * it.quantidadeDisponivel }
        totalTextView.text = "Total: R$${String.format("%.2f", total)}"
    }



}
