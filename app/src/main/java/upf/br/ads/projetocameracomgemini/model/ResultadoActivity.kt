package upf.br.ads.projetocameracomgemini.model

import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import upf.br.ads.projetocameracomgemini.R
import java.io.File

class ResultadoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_resultado)

        //pegando os elementos da tela
        val imageView = findViewById<ImageView>(R.id.fotoResultado)
        val textView = findViewById<TextView>(R.id.textoDescricao)

        //pegando foto e descrição da ia
        val caminhoFoto = intent.getStringExtra("CAMINHO_FOTO")
        val descricaoIA = intent.getStringExtra("DESCRICAO_IA")

        // 3. Exibir o texto do Gemini
        textView.text = descricaoIA ?: "Não foi possível obter a descrição."

        // 4. Exibir a foto salva no cache
        if (caminhoFoto != null) {
            val file = File(caminhoFoto)
            if (file.exists()) {
                imageView.setImageURI(Uri.fromFile(file))
            }
        }

    }
}