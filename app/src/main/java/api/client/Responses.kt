package api.client

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

class Responses {

    data class Login(
        @SerializedName("USUARIO_ID") val usuarioId: Int,
        @SerializedName("USUARIO_NOME") val usuarioNome: String,
        @SerializedName("USUARIO_EMAIL") val usuarioEmail: String,
        @SerializedName("USUARIO_SENHA") val usuarioSenha: String,
        @SerializedName("USUARIO_CPF") val usuarioCpf: String,
    )

    data class Product(
        @SerializedName("PRODUTO_ID") val produtoId: Int,
        @SerializedName("PRODUTO_NOME") val produtoNome: String,
        @SerializedName("PRODUTO_DESC") val produtoDesc: String,
        @SerializedName("PRODUTO_PRECO") val produtoPreco: String,
        @SerializedName("PRODUTO_DESCONTO") val produtoDesconto: String,
        @SerializedName("CATEGORIA_ID") val categoriaId: Int,
        @SerializedName("PRODUTO_ATIVO") val produtoAtivo: AtivatedProduct,
        @SerializedName("IMAGEM_URL") val imagemUrl: String?,
        @SerializedName("QUANTIDADE_DISPONIVEL") val quantidadeDisponivel: Int
    )

    data class AtivatedProduct(
        @SerializedName("type") val type: String,
        @SerializedName("data") val data: List<Int>
    )

    @Parcelize
    data class ProductCart(
        @SerializedName("PRODUTO_ID") val produtoId: Int,
        @SerializedName("PRODUTO_NOME") val produtoNome: String,
        @SerializedName("PRODUTO_DESC") val produtoDesc: String?,
        @SerializedName("PRODUTO_PRECO") val produtoPreco: Double,
        @SerializedName("PRODUTO_DESCONTO") val produtoDesconto: String?,
        @SerializedName("CATEGORIA_ID") val categoriaId: Int,
        @SerializedName("PRODUTO_ATIVO") val produtoAtivo: Int,
        @SerializedName("IMAGEM_URL") val imagemUrl: String,
        @SerializedName("QUANTIDADE_DISPONIVEL") val quantidadeDisponivel: Int
    ) : Parcelable

    data class Address(
        val ENDERECO_ID: Int,
        val USUARIO_ID: Int,
        val ENDERECO_NOME: String,
        val ENDERECO_LOGRADOURO: String,
        val ENDERECO_NUMERO: String,
        val ENDERECO_COMPLEMENTO: String?,
        val ENDERECO_CEP: String,
        val ENDERECO_CIDADE: String,
        val ENDERECO_ESTADO: String,
        val ENDERECO_APAGADO: Int
    )

    data class OrderRequest(
        val userId: Int,
        val total: Double,
        val products: List<ProductRequest>,
        val addressId: Int
    )

    data class ProductRequest(
        val produtoId: Int,
        val quantidade: Int
    )

    data class OrderResponse(
        val status: String,
        val code: Int,
        val message: String
    )
    data class RegisterUser(
        val nome: String,
        val email: String,
        val senha: String,
        val cpf: String
    )
    data class RegisterUserResponse(
        val id: Int,
        val message: String
    )
}