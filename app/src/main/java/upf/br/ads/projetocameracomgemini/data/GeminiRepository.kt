package upf.br.ads.projetocameracomgemini.data

import android.graphics.Bitmap
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GeminiRepository {

    // Substitua pela sua chave real do Google AI Studio
    private val apiKey = "AIzaSyADjen0roD3dIx8jgJ2VXA9O7UKUs1_ItA"

    private val generativeModel = GenerativeModel(
        modelName = "gemini-3-flash-preview",
        apiKey = apiKey
    )

    suspend fun analisarProduto(image: Bitmap): String? = withContext(Dispatchers.IO) {
        try {
            val response = generativeModel.generateContent(
                content {
                    image(image)
                    text("Analise a imagem, identifique o produto e use sua ferramenta de busca para encontrar preços reais na região de [INSERIR_CIDADE_OU_ESTADO]. " +
                            "Consulte sites de grandes mercados, atacados e portais de transparência de preços do governo (como Preço da Hora ou Menor Preço). " +
                            "Não invente valores. Se não encontrar o preço exato na cidade, use a média estadual. " +
                            "Responda estritamente neste formato, sem textos adicionais: " +
                            "[nome do produto] - [Média do valor na região] " +
                            "Fonte do preço: [nome do site1] [valor] " +
                            "[link_direto_1] " +
                            "Fonte do preço: [nome do site2] [valor] " +
                            "[link_direto_2] " +
                            "Fonte do preço: [nome do site3] [valor] " +
                            "[link_direto_3] " +
                            "Região: [nome da região consultada]"
                    )
                }
            )
            response.text
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}