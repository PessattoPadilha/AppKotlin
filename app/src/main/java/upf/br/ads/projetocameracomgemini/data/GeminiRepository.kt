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
    private val apiKey = "AIzaSyD3CmuXQIzuvFvziQTfRChnY7iHAMo2qIk"

    private val generativeModel = GenerativeModel(
        modelName = "gemini-3-flash-preview",
        apiKey = apiKey
    )


    suspend fun analisarProduto(context: Context, image: Bitmap): String? = withContext(Dispatchers.IO) {
        try {
            // 1. Obtém a localização do usuário
            val regiaoUsuario = obterLocalizacao(context)

            val promptIa = """
                Analise a imagem e identifique o produto. 
                Localização: $regiaoUsuario.
                
                REGRAS CRÍTICAS:
                1. Forneça exatamente 3 opções de lojas diferentes.
                2. Use EXCLUSIVAMENTE o formato Markdown: [Nome da Loja](URL).
                3. NÃO escreva introduções como "Aqui está a análise" ou conclusões.
                4. Responda APENAS o bloco de texto abaixo.
                5. De preços atualizados da localização atual 
                6. Não invente 
            
                FORMATO DE RESPOSTA (ESTRITO):
                [Nome do Produto]
                Média de preço: [Valor]
            
                1. [Nome da Loja 1](URL_DA_LOJA_1) - [Preço]
                2. [Nome da Loja 2](URL_DA_LOJA_2) - [Preço]
                3. [Nome da Loja 3](URL_DA_LOJA_3) - [Preço]
            
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
                    val addr = enderecos[0]
                    // Tenta Localidade, se não houver, tenta SubAdminArea (comum em cidades menores)
                    val cidade = addr.locality ?: addr.subAdminArea ?: addr.subLocality ?: "Cidade desconhecida"
                    val estado = addr.adminArea ?: ""
                    "$cidade, $estado"
                }else {
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