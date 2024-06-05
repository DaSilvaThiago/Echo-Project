package api.client

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.teachersteps.ProductDetailActivity
import com.example.teachersteps.R
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class Adapters {

    class AdapterMainActivity(private val dataSet: List<Responses.Product>) :
        RecyclerView.Adapter<AdapterMainActivity.ViewHolder>() {

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val nome: TextView = view.findViewById(R.id.nomeProduto)
            val descricao: TextView = view.findViewById(R.id.descricaoProduto)
            //val valor: TextView = view.findViewById(R.id.valorProduto)
            val imagem: ImageButton = view.findViewById(R.id.imagem_produto)
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
            //viewHolder.valor.text = produto.produtoPreco

            Glide.with(viewHolder.itemView.context)
                .load(produto.imagemUrl)
                .placeholder(R.drawable.ic_launcher_background)
                .error(com.google.android.material.R.drawable.mtrl_ic_error)
                .into(viewHolder.imagem)

            viewHolder.imagem.setOnClickListener {
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

    class AdapterCart(private val items: MutableList<Responses.ProductCart>, private val context: Context, private val userId: Int, private val updateTotal: () -> Unit) :
        RecyclerView.Adapter<AdapterCart.ViewHolder>() {

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

        private fun removeItemFromCart(item: Responses.ProductCart, position: Int) {
            val requestBody = mapOf("userId" to userId, "productId" to item.produtoId)
            val retrofit = Retrofit.Builder()
                .baseUrl("https://echo-api-senac.vercel.app/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val api = retrofit.create(Services.Cart::class.java)
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

}