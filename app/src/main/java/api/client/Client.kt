package api.client

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonNull
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.lang.reflect.Type

object Client {

    private const val BASE_URL = "https://echo-api-senac.vercel.app/"

    private val gson: Gson by lazy {
        GsonBuilder()
            .setLenient()
            .registerTypeAdapter(
                Responses.Address::class.java,
                EnderecoDeserializer()
            )
            .create()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }


    private val retrofitScalar: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(ScalarsConverterFactory.create())
            .build()
    }

    object RetrofitRegister {
        val api: Services.Register by lazy {
            Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(Services.Register::class.java)
        }
    }

    fun <T> createService(serviceClass: Class<T>): T {
        return retrofit.create(serviceClass)
    }

    fun <T> createServiceScalar(serviceClass: Class<T>): T {
        return retrofitScalar.create(serviceClass)
    }
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

