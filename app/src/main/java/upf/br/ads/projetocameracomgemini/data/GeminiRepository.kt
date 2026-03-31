package upf.br.ads.projetocameracomgemini.data

import android.graphics.Bitmap
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GeminiRepository {

    // Substitua pela sua chave real do Google AI Studio
    private val apiKey = "SUA_API_KEY_AQUI"

    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = apiKey
    )

    suspend fun analisarProduto(image: Bitmap): String? = withContext(Dispatchers.IO) {
        try {
            val response = generativeModel.generateContent(
                content {
                    image(image)
                    text("Analise este produto e me dê o nome e uma descrição curta para inventário.")
                }
            )
            response.text
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}