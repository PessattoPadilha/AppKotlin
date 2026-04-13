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
//    private val safetySettings = listOf(
//        SafetySetting(HarmCategory.HARASSMENT, BlockThreshold.NONE),
//        SafetySetting(HarmCategory.HATE_SPEECH, BlockThreshold.NONE),
//        SafetySetting(HarmCategory.SEXUALLY_EXPLICIT, BlockThreshold.NONE),
//        SafetySetting(HarmCategory.DANGEROUS_CONTENT, BlockThreshold.NONE)
//    )

    // Chave de API e instância do modelo Gemini 3 Flash Preview
    private val apiKey = "AIzaSyB3JfmDRaVVBiuNQDubUtesYf2e1BHSn5s"

    private val generativeModel = GenerativeModel(
        modelName = "gemini-3-flash-preview",
        apiKey = apiKey
    )

    /**
     * Analisa a imagem e retorna uma String com o resultado ou a mensagem de erro.
     */
    suspend fun analisarProduto(context: Context, image: Bitmap): String? = withContext(Dispatchers.IO) {
        try {
            // 1. Obtém a localização do usuário
            val regiaoUsuario = obterLocalizacao(context)

            val promptIa = """
                Analise a imagem e identifique o produto. 
                Busque preços REAIS e ATUAIS na região de $regiaoUsuario.
                
                REGRAS DE FORMATO:
                1. Use Markdown para criar hyperlinks nos nomes das fontes.
                2. Mantenha os espaços entre os blocos para legibilidade.
                3. Responda estritamente no formato abaixo.
                4. nao gere nenhum texto fora do padrão abaixo
            
                FORMATO DE RESPOSTA:
                [Nome do Produto] - [Média do valor na região]
            
                Fonte do preço: [Clique aqui para ver o site](URL_DO_SITE) - [Valor]
            
                Fonte do preço: [Clique aqui para ver o site](URL_DO_SITE) - [Valor]
            
                Fonte do preço: [Clique aqui para ver o site](URL_DO_SITE) - [Valor]
            
                Região: [Cidade e Estado]
            """.trimIndent()


            // 2. Envia a imagem e o prompt para o Gemini
            val response = generativeModel.generateContent(
                content {
                    image(image)
                    text(promptIa)
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
            // Em caso de erro de rede API ou GPS
            e.printStackTrace()
            "Erro na análise técnica: ${e.localizedMessage ?: "Erro desconhecido"}"
        }
    }


    @SuppressLint("MissingPermission")
    private suspend fun obterLocalizacao(context: Context): String {
        return try {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            // pega a localização atual (requer play-services-location e coroutines-play-services)
            val location = fusedLocationClient.getCurrentLocation(
                com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
                null
            ).await()

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
            "Brasil"
        }
    }
}