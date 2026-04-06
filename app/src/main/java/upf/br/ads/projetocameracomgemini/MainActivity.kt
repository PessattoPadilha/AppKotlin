package upf.br.ads.projetocameracomgemini

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.launch
import upf.br.ads.projetocameracomgemini.data.GeminiRepository
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

// --- IMPORT NECESSÁRIO ADICIONADO ABAIXO ---
// Se sua ResultadoActivity estiver na pasta 'model', você PRECISA desta linha:
import upf.br.ads.projetocameracomgemini.model.ResultadoActivity

class MainActivity : AppCompatActivity() {

    private val geminiRepository = GeminiRepository()
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var viewFinder: PreviewView
    private lateinit var progressBar: ProgressBar
    private var imageCapture: ImageCapture? = null
    private val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) startCamera() else Toast.makeText(this, "Permissão negada.", Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewFinder = findViewById(R.id.viewFinder)
        progressBar = findViewById(R.id.progressBar)

        findViewById<Button>(R.id.botaoFoto).setOnClickListener {
            takePhoto()
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun startCamera() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()
                bindPreview(cameraProvider)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    fun bindPreview(cameraProvider: ProcessCameraProvider) {
        cameraProvider.unbindAll()

        val preview = Preview.Builder().build()
        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()

        preview.setSurfaceProvider(viewFinder.surfaceProvider)

        imageCapture = ImageCapture.Builder()
            .setTargetRotation(viewFinder.display.rotation)
            .build()

        try {
            cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun takePhoto() {
        val imageCapture = imageCapture ?: return

        val photoFile = File(externalCacheDir, "foto.jpg")
        val outputFileOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        progressBar.visibility = View.VISIBLE

        imageCapture.takePicture(outputFileOptions, cameraExecutor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(error: ImageCaptureException) {
                    runOnUiThread {
                        progressBar.visibility = View.GONE
                        Toast.makeText(baseContext, "Erro: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    val bitmap = BitmapFactory.decodeFile(photoFile.absolutePath)

                    runOnUiThread {
                        lifecycleScope.launch {
                            val resultado = geminiRepository.analisarProduto(bitmap)
                            progressBar.visibility = View.GONE

                            if (resultado != null) {
                                // Se o import lá em cima estiver certo, este erro some:
                                val intent = Intent(this@MainActivity, ResultadoActivity::class.java).apply {
                                    putExtra("CAMINHO_FOTO", photoFile.absolutePath)
                                    putExtra("DESCRICAO_IA", resultado)
                                }
                                startActivity(intent)
                            } else {
                                Toast.makeText(this@MainActivity, "Erro na análise.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            })
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}