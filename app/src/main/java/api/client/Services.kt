package api.client

import api.client.Responses
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query

class Services {

    interface Login {
        @GET("/login")
        fun login(
            @Query("usuario") usuario: String,
            @Query("senha") senha: String
        ): Call<Responses.Login>
    }

    interface Products {
        @GET("/products")
        fun getProdutos(): Call<List<Responses.Product>>
    }

    interface ProductDetail {
        @FormUrlEncoded
        @POST("/cart")
        fun adicionarAoCarrinho(
            @Field("USUARIO_ID") userId: Int,
            @Field("PRODUTO_ID") produtoId: Int,
            @Field("ITEM_QTD") quantidade: Int
        ): Call<String>
    }

    interface Cart {
        @GET("/cart")
        fun getCartItems(@Query("userId") userId: Int): Call<List<Responses.ProductCart>>

        @PUT("/cart")
        fun updateCartItemQuantityToZero(@Body requestBody: Map<String, Int>): Call<Void>
    }

    interface Order {
        @POST("/createOrder")
        fun createOrder(@Body orderRequest: Responses.OrderRequest): Call<Responses.OrderResponse>

        @GET("/getUserAddresses")
        fun getUserAddresses(@Query("userId") userId: Int): Call<List<Responses.Address>>
    }
}