package com.example.teachersteps

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonNull
import api.client.Responses
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import api.client.Services
import java.lang.reflect.Type

class PaymentActivity : AppCompatActivity() {
    private lateinit var radioGroup: RadioGroup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)

        val sharedPreferences = getSharedPreferences("Login", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getInt("userId", 0)

        val totalValue = intent.getDoubleExtra("TOTAL", 0.0)
        val productList: ArrayList<Responses.ProductCart>? = intent.getParcelableArrayListExtra("PRODUCT_LIST")

        findViewById<TextView>(R.id.totalValueText).text = "Total: R$${String.format("%.2f", totalValue)}"

        val finishPaymentButton: Button = findViewById(R.id.finishPaymentButton)
        radioGroup = findViewById(R.id.addressRadioGroup)

        loadUserAddresses(userId)

        finishPaymentButton.setOnClickListener {
            val selectedRadioButtonId = radioGroup.checkedRadioButtonId
            if (selectedRadioButtonId != -1) {
                val selectedRadioButton = findViewById<RadioButton>(selectedRadioButtonId)
                val selectedAddressId = selectedRadioButton.tag.toString().toInt()
                enviaOrdem(userId, totalValue, productList ?: arrayListOf(), selectedAddressId)
            } else {
                Toast.makeText(this, "Por favor, selecione um endereço", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun loadUserAddresses(userId: Int) {
        val apiService = Client.createService(Services.Order::class.java)
        val call = apiService.getUserAddresses(userId)
        call.enqueue(object : Callback<List<Responses.Address>> {
            override fun onResponse(call: Call<List<Responses.Address>>, response: Response<List<Responses.Address>>) {
                if (response.isSuccessful) {
                    val addresses = response.body()
                    Log.d("API_RESPONSE", "Response: $addresses")
                    if (addresses != null) {
                        populateAddressRadioButtons(addresses)
                    } else {
                        Log.e("API_ERROR", "Received null address list")
                        Toast.makeText(this@PaymentActivity, "Erro ao carregar endereços", Toast.LENGTH_LONG).show()
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("API_ERROR", "Erro ao carregar endereços: $errorBody")
                    Toast.makeText(this@PaymentActivity, "Erro ao carregar endereços", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<List<Responses.Address>>, t: Throwable) {
                Log.e("API_ERROR", "Falha na conexão", t)
                Toast.makeText(this@PaymentActivity, "Falha na conexão", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun populateAddressRadioButtons(addresses: List<Responses.Address>) {
        addresses.forEach { address ->
            val radioButton = RadioButton(this)
            radioButton.text = "${address.ENDERECO_LOGRADOURO}, ${address.ENDERECO_NUMERO} - ${address.ENDERECO_COMPLEMENTO ?: ""}, ${address.ENDERECO_CIDADE} - ${address.ENDERECO_ESTADO}, ${address.ENDERECO_CEP}"
            radioButton.tag = address.ENDERECO_ID
            radioGroup.addView(radioButton)
        }
    }

    private fun enviaOrdem(userId: Int, total: Double, products: ArrayList<Responses.ProductCart>, addressId: Int) {
        val gson = GsonBuilder().setLenient().create()

        // Converter Produto para ProductRequest
        val productRequests = products.map { Responses.ProductRequest(it.produtoId, it.quantidadeDisponivel) }

        val orderRequest = Responses.OrderRequest(userId, total, productRequests, addressId)
        val jsonOrderRequest = gson.toJson(orderRequest)
        Log.d("API_REQUEST_JSON", "Order Request JSON: $jsonOrderRequest")

        val apiService = Client.createService(Services.Order::class.java)
        val call = apiService.createOrder(orderRequest)
        call.enqueue(object : Callback<Responses.OrderResponse> {
            override fun onResponse(call: Call<Responses.OrderResponse>, response: Response<Responses.OrderResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@PaymentActivity, "Pedido realizado com sucesso", Toast.LENGTH_LONG).show()
                    val intent = Intent(this@PaymentActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("API_ERROR", "Erro ao realizar pedido: $errorBody")
                    Toast.makeText(this@PaymentActivity, "Erro ao realizar pedido", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<Responses.OrderResponse>, t: Throwable) {
                Log.e("API_ERROR", "Falha na conexão", t)
                Toast.makeText(this@PaymentActivity, "Falha na conexão", Toast.LENGTH_LONG).show()
            }
        })
    }



    class EnderecoDeserializer : JsonDeserializer<Responses.Address> {
        override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): Responses.Address {
            val jsonObject = json?.asJsonObject!!

            val enderecoApagado = if (jsonObject.get("ENDERECO_APAGADO").isJsonObject) {
                jsonObject.getAsJsonObject("ENDERECO_APAGADO").getAsJsonArray("data")[0].asInt
            } else {
                0
            }

            return Responses.Address(
                ENDERECO_ID = jsonObject.get("ENDERECO_ID").asInt,
                USUARIO_ID = jsonObject.get("USUARIO_ID").asInt,
                ENDERECO_NOME = jsonObject.get("ENDERECO_NOME").asString,
                ENDERECO_LOGRADOURO = jsonObject.get("ENDERECO_LOGRADOURO").asString,
                ENDERECO_NUMERO = jsonObject.get("ENDERECO_NUMERO").asString,
                ENDERECO_COMPLEMENTO = if (jsonObject.get("ENDERECO_COMPLEMENTO") is JsonNull) null else jsonObject.get("ENDERECO_COMPLEMENTO").asString,
                ENDERECO_CEP = jsonObject.get("ENDERECO_CEP").asString,
                ENDERECO_CIDADE = jsonObject.get("ENDERECO_CIDADE").asString,
                ENDERECO_ESTADO = jsonObject.get("ENDERECO_ESTADO").asString,
                ENDERECO_APAGADO = enderecoApagado
            )
        }
    }


}
