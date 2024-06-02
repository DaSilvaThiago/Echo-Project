package com.example.teachersteps

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Query
import retrofit2.http.Body

class CartActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var totalTextView: TextView
    private lateinit var goToPaymentButton: Button
    private var total: Double = 0.0
    private var userId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        // Initialize shared preferences and userId
        val sharedPreferences = getSharedPreferences("Login", Context.MODE_PRIVATE)
        userId = sharedPreferences.getInt("userId", 0)

        recyclerView = findViewById(R.id.cartRecyclerView)
        totalTextView = findViewById(R.id.totalTextView)
        goToPaymentButton = findViewById(R.id.goToPaymentButton)

        recyclerView.layoutManager = LinearLayoutManager(this)
        fetchCartItems()

        goToPaymentButton.setOnClickListener {
            // Ir para tela de pagamento enviando os dados
        }
    }

    private fun fetchCartItems() {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://echo-api-senac.vercel.app")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(CartApiService::class.java)

        api.getCartItems(userId).enqueue(object : Callback<List<Produto>> {
            override fun onResponse(call: Call<List<Produto>>, response: Response<List<Produto>>) {
                if (response.isSuccessful) {
                    val cartItems = response.body()?.filter { it.quantidadeDisponivel > 0 }?.toMutableList() ?: mutableListOf()
                    if (cartItems.isEmpty()) {
                        Log.d("CartActivity", "No items in cart.")
                    } else {
                        recyclerView.adapter = CartAdapter(cartItems, this@CartActivity, userId) {
                            total = cartItems.sumOf { it.produtoPreco * it.quantidadeDisponivel }
                            totalTextView.text = "Total: R$${String.format("%.2f", total)}"

                        }
                    }
                    // Log the retrieved data
                    Log.d("CartActivity", "Cart items retrieved: $cartItems")
                } else {
                    Toast.makeText(this@CartActivity, "Failed to fetch cart items", Toast.LENGTH_SHORT).show()
                    Log.e("CartActivity", "Error fetching cart items: ${response.code()} - ${response.message()}")
                }
            }

            override fun onFailure(call: Call<List<Produto>>, t: Throwable) {
                Toast.makeText(this@CartActivity, "Error connecting to the server", Toast.LENGTH_SHORT).show()
                Log.e("CartActivity", "Error connecting to the server: ${t.message}", t)
            }
        })
    }

    class CartAdapter(
        private val items: MutableList<Produto>,
        private val context: Context,
        private val userId: Int,
        private val updateTotal: () -> Unit
    ) : RecyclerView.Adapter<CartAdapter.ViewHolder>() {

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val productName: TextView = view.findViewById(R.id.productNameTextView)
            val productPrice: TextView = view.findViewById(R.id.productPriceTextView)
            val productQuantity: TextView = view.findViewById(R.id.productQuantityTextView)
            val productImage: ImageView = view.findViewById(R.id.productImageView)
            val deleteButton: Button = view.findViewById(R.id.deleteButton)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cart_detail, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            Log.d("CartAdapter", "Binding item at position $position: $item")
            holder.productName.text = item.produtoNome ?: "Nome não disponível"
            holder.productPrice.text = "R$${String.format("%.2f", item.produtoPreco)}"
            holder.productQuantity.text = "Qtd: ${item.quantidadeDisponivel}"
            Glide.with(context).load(item.imagemUrl).into(holder.productImage)


            holder.deleteButton.setOnClickListener {
                removeItemFromCart(item, position)
            }
        }

        private fun removeItemFromCart(item: Produto, position: Int) {
            val requestBody = mapOf("userId" to userId, "productId" to item.produtoId)
            val retrofit = Retrofit.Builder()
                .baseUrl("https://echo-api-senac.vercel.app/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val api = retrofit.create(CartApiService::class.java)
            api.updateCartItemQuantityToZero(requestBody).enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        items.removeAt(position)
                        notifyItemRemoved(position)
                        notifyItemRangeChanged(position, items.size)
                        updateTotal()
                        Toast.makeText(context, "Item deletado com sucesso", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Failed to update item", Toast.LENGTH_SHORT).show()
                        Log.e("CartAdapter", "Error updating item: ${response.code()} - ${response.message()}")
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(context, "Error connecting to the server", Toast.LENGTH_SHORT).show()
                    Log.e("CartAdapter", "Error connecting to the server: ${t.message}", t)
                }
            })
        }

        override fun getItemCount() = items.size
    }

    interface CartApiService {
        @GET("/cart")
        fun getCartItems(@Query("userId") userId: Int): Call<List<Produto>>

        @PUT("/cart")
        fun updateCartItemQuantityToZero(@Body requestBody: Map<String, Int>): Call<Void>
    }

    // Classe Produto
    data class Produto(
        @SerializedName("PRODUTO_ID") val produtoId: Int,
        @SerializedName("PRODUTO_NOME") val produtoNome: String,
        @SerializedName("PRODUTO_PRECO") val produtoPreco: Double,
        @SerializedName("QUANTIDADE_DISPONIVEL") val quantidadeDisponivel: Int,
        @SerializedName("IMAGEM_URL") val imagemUrl: String
    )
}
