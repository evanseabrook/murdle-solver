package ca.evanseabrook.murdlesolver

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.camera.lifecycle.ProcessCameraProvider

import android.Manifest
import android.content.pm.PackageManager

import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import java.nio.ByteBuffer

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import org.json.JSONException
import android.view.View
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import com.squareup.moshi.Moshi
import org.json.JSONArray

class MainActivity : ComponentActivity() { // Or AppCompatActivity

    private lateinit var cameraExecutor: ExecutorService
    private lateinit var previewView: PreviewView // Declare previewView

    private var imageCapture: ImageCapture? = null

    // ActivityResultLauncher for requesting camera permission
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                Log.i(TAG, "Permission granted")
                startCamera()
            } else {
                Log.e(TAG, "Permission denied")
                Toast.makeText(
                    this,
                    "Camera permission is required to use the camera.",
                    Toast.LENGTH_LONG
                ).show()
                // You might want to explain to the user why the permission is needed
                // or disable camera-related functionality.
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) // Make sure this layout has a PreviewView

        previewView =
            findViewById(R.id.previewView) // Initialize previewView (replace with your ID)

        val decoderButton = findViewById<Button>(R.id.decoderButton)
        decoderButton.setOnClickListener {
            useDecoderRing()
        }

        // Request camera permission
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            requestCameraPermission()
        }

        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    private fun tryAgain() {
        findViewById<RelativeLayout>(R.id.responseView).visibility = View.GONE
        findViewById<ProgressBar>(R.id.progressSpinner).visibility = View.GONE
        findViewById<Button>(R.id.backButton).visibility = View.GONE
        findViewById<TextView>(R.id.responseText).visibility = View.GONE
        findViewById<PreviewView>(R.id.previewView).visibility = View.VISIBLE
        findViewById<Button>(R.id.decoderButton).visibility = View.VISIBLE
    }

    private fun sendPostRequestWithImage(imageData: ByteArray) {
        val client = OkHttpClient.Builder().readTimeout(60, java.util.concurrent.TimeUnit.SECONDS).build()

        fun sendImage() {
            val moshi = Moshi.Builder().build()
            val jsonAdapter = moshi.adapter(AdkRunRequest::class.java)
            val json = jsonAdapter.toJson(createAdkRunRequest(imageData))

            Log.i(TAG, "JSON request: $json")

            val requestBody = json.toRequestBody("application/json".toMediaTypeOrNull())

            val request = Request.Builder()
                .url(getString(R.string.base_api_url) + "/run") // Replace with your actual URL
                .post(requestBody)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e(TAG, "POST request failed: ${e.message}", e)
                }

                override fun onResponse(call: Call, response: Response) {
                    val responseBodyString = response.body?.string()
                    Log.i(TAG, "Response body: $responseBodyString")

                    try {
                        val json = JSONArray(responseBodyString)
                        Log.i(TAG, "JSON response: $json")

                        val decodedMessage = json.getJSONObject(json.length() - 1).getJSONObject("content").getJSONArray("parts").getJSONObject(0)?.getString("text")


                        runOnUiThread {
                            val respText = findViewById<TextView>(R.id.responseText)
                            findViewById<ProgressBar>(R.id.progressSpinner).visibility = View.GONE
                            respText.visibility = View.VISIBLE
                            respText.text = decodedMessage
                            findViewById<Button>(R.id.backButton).visibility = View.VISIBLE

                            findViewById<Button>(R.id.backButton).setOnClickListener {
                                tryAgain()
                                startCamera()
                            }
                        }
                    } catch (e: JSONException) {
                        Log.e(TAG, "JSON parsing failed: ${e.message}", e)
                        runOnUiThread {
                            tryAgain()
                            startCamera()
                            Toast.makeText(baseContext, "Failed to decode message.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            })
        }

        val sessionRequestBody = "".toRequestBody(null)
        val sessionRequestUrl = "%s/apps/%s/users/%s/sessions/%s".format(getString(R.string.base_api_url).toString(), adkAppName, adkUserId, adkSessionId)
        Log.i(TAG, "Session request URL: $sessionRequestUrl")
        val sessionRequest = Request.Builder()
            .url(sessionRequestUrl).post(sessionRequestBody).build()

        client.newCall(sessionRequest).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                sendImage()
            }
            override fun onResponse(call: Call, response: Response) {
                sendImage()
            }
        })


    }

    private fun useDecoderRing() {
        val imageCapture = imageCapture ?: return

        imageCapture.takePicture(
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    val buffer: ByteBuffer = image.planes[0].buffer
                    val bytes = ByteArray(buffer.remaining())
                    buffer.get(bytes)
                    image.close()

                    Log.d(TAG, "Photo captured in memory. Size: ${bytes.size} bytes")
                    Toast.makeText(baseContext, "Decoding, JUST a moment...", Toast.LENGTH_LONG).show()
                    findViewById<PreviewView>(R.id.previewView).visibility = View.GONE
                    findViewById<RelativeLayout>(R.id.responseView).visibility = View.VISIBLE
                    findViewById<ProgressBar>(R.id.progressSpinner).visibility = View.VISIBLE
                    findViewById<Button>(R.id.decoderButton).visibility = View.GONE

                    sendPostRequestWithImage(bytes)
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exception.message}", exception)
                }
            }
        )

    }

    private fun requestCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Permission is already granted
                startCamera()
            }

            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                // Explain to the user why you need the permission
                Toast.makeText(
                    this,
                    "Camera permission is needed to display the preview.",
                    Toast.LENGTH_LONG
                ).show()
                // Then request the permission
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }

            else -> {
                // Directly request the permission
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview Use Case
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

            // Initialize ImageCapture use case
            imageCapture = ImageCapture.Builder()
                .setCaptureMode(CAPTURE_MODE_MAXIMIZE_QUALITY)
                // You can set options here, e.g., target resolution, flash mode
                // .setTargetResolution(Size(1280, 720))
                .build()

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, // LifecycleOwner
                    cameraSelector,
                    preview,
                    imageCapture
                ) // Add other use cases like ImageCapture here if needed

            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
                Toast.makeText(this, "Failed to start camera: ${exc.message}", Toast.LENGTH_LONG)
                    .show()
            }

        }, ContextCompat.getMainExecutor(this)) // Run on the main thread
    }


    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    companion object {
        private const val TAG = "CameraXApp"
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }
}