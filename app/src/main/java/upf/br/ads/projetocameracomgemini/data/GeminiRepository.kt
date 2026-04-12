package upf.br.ads.projetocameracomgemini.data

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.location.Geocoder
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.Locale

class GeminiRepository {

    // CHAVES DE API
    private val geminiApiKey = "AIzaSyAitDF32iMnfm4MQ4XkOv-3g-buEDtloZ4"
    private val serperApiKey = "f42428cfde8daf6e2c820e6337340d0e8162aab2"

    private val generativeModel = GenerativeModel(
        modelName = "gemini-3-flash-preview",
        apiKey = geminiApiKey
    )

    private val httpClient = OkHttpClient()

    /**
     * Fluxo: Identifica com Gemini -> Busca preços reais no Google Shopping via Serper
     */
    suspend fun analisarProduto(context: Context, image: Bitmap): String? =
        withContext(Dispatchers.IO) {
            try {
                // 1. Obtém a localização (Cidade, Estado) via GPS
                val regiaoUsuario = obterLocalizacaoEscrita(context)

                // 2. O Gemini identifica o nome exato do produto
                val promptIdentificacao =
                    "Identifique exatamente o produto nesta imagem. Responda apenas o nome e modelo, sem frases adicionais."
                val responseGemini = generativeModel.generateContent(
                    content {
                        image(image)
                        text(promptIdentificacao)
                    }
                )

                val nomeProduto = responseGemini.text?.trim()
                    ?: return@withContext "Não foi possível identificar o produto na imagem."

                // 3. Busca os dados de venda (Shopping) na Serper.dev enviando a região
                return@withContext buscarDadosShopping(nomeProduto, regiaoUsuario)

            } catch (e: Exception) {
                e.printStackTrace()
                "Erro na análise técnica: ${e.localizedMessage ?: "Erro desconhecido"}"
            }
        }

    private suspend fun buscarDadosShopping(produto: String, regiao: String): String {
        val mediaType = "application/json".toMediaType()

        val jsonBody = JSONObject().apply {
            put("q", produto)
            put("gl", "br")
            put("hl", "pt-br")
            put("location", regiao) // A Serper usa isso para priorizar resultados locais
        }

        val request = Request.Builder()
            .url("https://google.serper.dev/shopping")
            .post(jsonBody.toString().toRequestBody(mediaType))
            .addHeader("X-API-KEY", serperApiKey)
            .addHeader("Content-Type", "application/json")
            .build()

        return try {
            val response = httpClient.newCall(request).execute()
            val responseData = response.body?.string() ?: ""
            // Passamos a regiao para ser exibida no texto final
            formatarRespostaShopping(responseData, produto, regiao)
        } catch (e: Exception) {
            "Erro ao conectar com o serviço de preços: ${e.message}"
        }
    }

    /**
     * Formata a resposta e inclui a informação da região pesquisada
     */
    private fun formatarRespostaShopping(
        jsonString: String,
        produto: String,
        regiao: String
    ): String {
        val json = JSONObject(jsonString)
        val shoppingItems = json.optJSONArray("shopping")
            ?: return "Identifiquei *$produto*, mas não encontrei lojas em $regiao."

        val sb = StringBuilder()
        sb.append("### 🔎 Produto: $produto\n")
        sb.append("📍 **Região de busca:** $regiao\n\n")

        for (i in 0 until minOf(shoppingItems.length(), 3)) {
            val item = shoppingItems.getJSONObject(i)

            val tituloItem = item.optString("title")
            val preco = item.optString("price", "Consultar")
            val link = item.optString("link")
            val vendedor = item.optString("source", "Loja")

            sb.append("**Item:** $tituloItem\n")
            sb.append("**Preço:** $preco\n")
            sb.append("**Loja:** $vendedor\n")
            sb.append("🔗 [Ver Oferta]($link)\n")
            sb.append("----------------------------\n")
        }

        return sb.toString()
    }

    @SuppressLint("MissingPermission")
    private suspend fun obterLocalizacaoEscrita(context: Context): String {
        return try {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            // Solicita a localização com alta precisão
            val location = fusedLocationClient.getCurrentLocation(
                com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
                null
            ).await()

            if (location != null) {
                val geocoder = Geocoder(context, Locale.getDefault())
                val enderecos = geocoder.getFromLocation(location.latitude, location.longitude, 1)

                if (!enderecos.isNullOrEmpty()) {
                    val endereco = enderecos[0]

                    // Tentativa detalhada: prioriza cidade (locality ou subAdminArea)
                    val cidade = endereco.locality
                        ?: endereco.subAdminArea
                        ?: endereco.subLocality
                        ?: ""

                    val estado = endereco.adminArea ?: ""

                    if (cidade.isNotEmpty() && estado.isNotEmpty()) {
                        "$cidade, $estado"
                    } else if (cidade.isNotEmpty()) {
                        cidade
                    } else {
                        estado.ifEmpty { "Brasil" }
                    }
                } else {
                    "Brasil"
                }
            } else {
                "Brasil"
            }
        } catch (e: Exception) {
            "Brasil"
        }
    }
}