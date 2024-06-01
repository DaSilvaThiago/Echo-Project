package com.example.teachersteps

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import com.bumptech.glide.Glide
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Query

class CartActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var totalTextView: TextView
    private lateinit var goToPaymentButton: Button
    private var total: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

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
            .baseUrl("https://de54397e-a23c-4def-9eb4-b07dde659faa-00-14c35j45l3isu.picard.repl.co/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(CartApiService::class.java)
        api.getCartItems(userId = 271).enqueue(object : Callback<List<MainActivity.Produto>> {
            override fun onResponse(call: Call<List<MainActivity.Produto>>, response: Response<List<MainActivity.Produto>>) {
                if (response.isSuccessful) {
                    val cartItems = response.body()?.toMutableList() ?: mutableListOf()
                    recyclerView.adapter = CartAdapter(cartItems, this@CartActivity) {
                        total = cartItems.sumOf { it.produtoPreco.toDouble() * it.quantidadeDisponivel }
                        totalTextView.text = "Total: R$${String.format("%.2f", total)}"
                    }
                }
            }

            override fun onFailure(call: Call<List<MainActivity.Produto>>, t: Throwable) {
                // Tratamento de falhas
            }
        })
    }

    class CartAdapter(private val items: MutableList<MainActivity.Produto>, private val context: Context, private val updateTotal: () -> Unit) : RecyclerView.Adapter<CartAdapter.ViewHolder>() {

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
            holder.productName.text = item.produtoNome
            holder.productPrice.text = "R$${item.produtoPreco}"//String.format("%.2f", item.produtoPreco.toDouble())}"
            holder.productQuantity.text = "Qtd: ${item.quantidadeDisponivel}"
            Glide.with(context).load(item.imagemUrl).into(holder.productImage)

            holder.deleteButton.setOnClickListener {
                removeItemFromCart(item, position)
            }
        }

        private fun removeItemFromCart(item: MainActivity.Produto, position: Int) {
            val retrofit = Retrofit.Builder()
                .baseUrl("https://echo-api-senac.vercel.app/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val api = retrofit.create(CartApiService::class.java)
            api.deleteCartItem(item.produtoId, userId = 271).enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        items.removeAt(position)
                        notifyItemRemoved(position)
                        notifyItemRangeChanged(position, items.size)
                        updateTotal()
                    } else {
                        Toast.makeText(context, "Failed to delete item", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(context, "Error connecting to the server", Toast.LENGTH_SHORT).show()
                }
            })
        }

        override fun getItemCount() = items.size
    }

    interface CartApiService {
        @GET("/cart")
        fun getCartItems(@Query("userId") userId: Int): Call<List<MainActivity.Produto>>

        @DELETE("/cart")
        fun deleteCartItem(@Query("produtoId") produtoId: Int, @Query("userId") userId: Int): Call<Void>
    }
}
