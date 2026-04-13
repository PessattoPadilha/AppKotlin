package upf.br.ads.projetocameracomgemini

import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import io.noties.markwon.Markwon
import java.io.File

class ResultadoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_resultado)

        val imageView = findViewById<ImageView>(R.id.fotoResultado)
        val textView = findViewById<TextView>(R.id.textoDescricao)

        val caminhoFoto = intent.getStringExtra("CAMINHO_FOTO")
        val descricaoIA = intent.getStringExtra("DESCRICAO_IA")

        val markwon = Markwon.create(this)

        if (descricaoIA != null) {
            markwon.setMarkdown(textView, descricaoIA)
        } else {
            textView.text = "Não foi possível obter a descrição."
        }

        if (caminhoFoto != null) {
            val file = File(caminhoFoto)
            if (file.exists()) {
                imageView.setImageURI(Uri.fromFile(file))
            }
        }
    }
}