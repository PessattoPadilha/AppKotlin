package upf.br.ads.projetocameracomgemini.Model

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import upf.br.ads.projetocameracomgemini.R

class LoginActivity : AppCompatActivity() {
    var EmailLogin: EditText? = null;
    var SenhaLogin: EditText? = null;


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        EmailLogin = findViewById(R.id.EmailLogin)
        SenhaLogin = findViewById(R.id.SenhaLogin)

    }

    fun fazerLogin(view: View?) {
        val email = EmailLogin?.text.toString()
        val senha = SenhaLogin?.text.toString()
        if (email.equals("1") && senha.equals("1")) {
            Toast.makeText(this, "Autenticado com sucesso", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, MenuActivity::class.java)
            startActivity(intent)
            finish()

        } else {
            Toast.makeText(getApplicationContext(), "Email ou senha incorretos", Toast.LENGTH_SHORT)
                .show()
        }
    }
}