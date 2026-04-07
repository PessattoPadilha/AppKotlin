package upf.br.ads.projetocameracomgemini.data

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.location.Geocoder
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.BlockThreshold
import com.google.ai.client.generativeai.type.HarmCategory
import com.google.ai.client.generativeai.type.SafetySetting
import com.google.ai.client.generativeai.type.content
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Locale

class GeminiRepository {

    // Configurações de segurança para evitar que a IA ignore a imagem (retorno vazio)
    private val safetySettings = listOf(
        SafetySetting(HarmCategory.HARASSMENT, BlockThreshold.NONE),
        SafetySetting(HarmCategory.HATE_SPEECH, BlockThreshold.NONE),
        SafetySetting(HarmCategory.SEXUALLY_EXPLICIT, BlockThreshold.NONE),
        SafetySetting(HarmCategory.DANGEROUS_CONTENT, BlockThreshold.NONE)
    )

    // Chave de API e instância do modelo Gemini 3 Flash Preview
    private val apiKey = "AIzaSyDu1Qdx0QXvpqtf2GiRBPE4VyXANN-nMo8"

    private val generativeModel = GenerativeModel(
        modelName = "gemini-3-flash-preview",
        apiKey = apiKey,
        safetySettings = safetySettings
    )

    /**
     * Analisa a imagem e retorna uma String com o resultado ou a mensagem de erro.
     */
    suspend fun analisarProduto(context: Context, image: Bitmap): String? = withContext(Dispatchers.IO) {
        try {
            // 1. Obtém a localização do usuário
            val regiaoUsuario = obterLocalizacaoEscrita(context)

            // 2. Envia a imagem e o prompt para o Gemini
            val response = generativeModel.generateContent(
                content {
                    image(image)
                    text("Analise a imagem, identifique o produto e use sua ferramenta de busca para encontrar preços reais e atuais na região de $regiaoUsuario. " +
                            "Consulte sites de mercados locais, atacados e o portal de transparência de preços do governo regional. " +
                            "Não invente valores nem use médias nacionais. " +
                            "Responda estritamente neste formato: " +
                            "[nome do produto] - [Media do valor na região] " +
                            "Fonte do preço: [nome do site1] [valor] " +
                            "[link1] " +
                            "Fonte do preço: [nome do site2] [valor] " +
                            "[link2] " +
                            "Fonte do preço: [nome do site3] [valor] " +
                            "[link3] " +
                            "Região: $regiaoUsuario"+
                            "cidade: $regiaoUsuario")
                }
            )

            // 3. Valida se o texto retornou nulo ou bloqueado
            val textoGerado = response.text

            if (textoGerado.isNullOrBlank()) {
                "A IA identificou a imagem, mas a resposta foi bloqueada pelos filtros de segurança do modelo Preview."
            } else {
                textoGerado
            }

        } catch (e: Exception) {
            // Em caso de erro de rede, API ou GPS, retorna o erro técnico para o usuário
            e.printStackTrace()
            "Erro na análise técnica: ${e.localizedMessage ?: "Erro desconhecido"}"
        }
    }

    /**
     * Função privada para obter o nome da cidade/estado via GPS
     */
    @SuppressLint("MissingPermission")
    private suspend fun obterLocalizacaoEscrita(context: Context): String {
        return try {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            // Aguarda a última localização conhecida (requer play-services-location e coroutines-play-services)
            val location = fusedLocationClient.lastLocation.await()

            if (location != null) {
                val geocoder = Geocoder(context, Locale.getDefault())
                val enderecos = geocoder.getFromLocation(location.latitude, location.longitude, 1)

                if (!enderecos.isNullOrEmpty()) {
                    val cidade = enderecos[0].locality ?: ""
                    val estado = enderecos[0].adminArea ?: ""
                    "$cidade, $estado"
                } else {
                    "Brasil"
                }
            } else {
                "Brasil"
            }
        } catch (e: Exception) {
            "Brasil" // Fallback caso o GPS falhe
        }
    }
}