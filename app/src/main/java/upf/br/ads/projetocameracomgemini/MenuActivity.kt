package upf.br.ads.projetocameracomgemini

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MenuActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_menu)

        // Configuração dos Insets (seu código original)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // --- Exemplo de clique de botão ---
        val buttonMenu = findViewById<Button>(R.id.buttonMenu) // O ID que você deu no XML
        buttonMenu.setOnClickListener {
            irParaCam()
        }
    }

    // A função simples que você pediu
    private fun irParaCam() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
}
