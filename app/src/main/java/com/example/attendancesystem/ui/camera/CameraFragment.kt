package com.example.attendancesystem.ui.camera

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.Size
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.toolbox.HttpHeaderParser
import com.android.volley.toolbox.Volley
import com.example.attendancesystem.R
import com.example.attendancesystem.utils.config.App
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.default
import id.zelory.compressor.constraint.destination
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.json.JSONTokener
import java.io.File
import java.io.IOException
import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraFragment : Fragment() {

    companion object {
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
    }

    private var imageCapture: ImageCapture? = null
    private lateinit var outputDirectory: File
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var permissionLauncher : ActivityResultLauncher<Array<String>>
    private  lateinit var viewFinder : PreviewView
    private var actualImage: File? = null
    private var compressedImage: File? = null
    private var frontCamera : Boolean = false
    private lateinit var confirmDialog : AlertDialog
    private lateinit var loadingDialog : AlertDialog
    private var id : Int? = null
    private var name : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            id = it.getInt("id")
            name =  it.getString("name")
        }
        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){ results ->
            val granted = results.entries.all { it.value == true }
            if(granted){
                startCamera()
            } else {
                //
                Toast.makeText(requireContext(), "Camera Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }


    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.camera_fragment, container, false)
        viewFinder = view.findViewById(R.id.viewFinder)
        val text: TextView = view.findViewById(R.id.main_text)

        if(name.isNullOrEmpty()) text.text = resources.getString(R.string.text_attendance_camera)
            else text.text = "Prenez une photo de $name"


        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
            // Requesting the permission
            //ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), STORAGE_PERMISSION_CODE)
            val permissions = arrayOf(Manifest.permission.CAMERA)
            permissionLauncher.launch(permissions)
        } else {
            startCamera()

        }
        // set on click listener for the button of capture photo
        // it calls a method which is implemented below
        view.findViewById<Button>(R.id.camera_capture_button).setOnClickListener {
            takePhoto()
        }
        view.findViewById<ImageView>(R.id.switch_camera).setOnClickListener {
            frontCamera = !frontCamera
            startCamera()
        }
        outputDirectory = getOutputDirectory()
        cameraExecutor = Executors.newSingleThreadExecutor()

        return view
    }

    private fun takePhoto() {
        // Get a stable reference of the
        // modifiable image capture use case

        val imageCapture = imageCapture ?: return

        // Create time-stamped output file to hold the image
        val photoFile = File(
            outputDirectory,
            SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis()) + ".jpg"
        )

        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        // Set up image capture listener,
        // which is triggered after photo has
        // been taken
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Toast.makeText(requireContext(), exc.toString(), Toast.LENGTH_SHORT).show()

                }
//                val savedUri = Uri.fromFile(photoFile)

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    Toast.makeText(requireContext(), "Compression de l\' image", Toast.LENGTH_SHORT).show()

                    actualImage = photoFile
                    compressImage()
                }
            })
    }
    private fun compressImage() {
        actualImage?.let { imageFile ->
            lifecycleScope.launch {
                // Default compression
                compressedImage = Compressor.compress(requireContext(), imageFile){
                    default()
                    destination(actualImage!!)
                }
                setCompressedImage()
            }
        }
    }
    private fun setCompressedImage() {
        compressedImage?.let {
            showConfirmLoading()
        }
    }


    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val imageAnalysis = ImageAnalysis.Builder()
                .setTargetResolution(Size(300, 300))
                .build()


            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewFinder.surfaceProvider)
                }
            imageCapture = ImageCapture.Builder().build()
            // Select back camera as a default
            var cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            if(frontCamera){
                cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
            }

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture, imageAnalysis
                )

            } catch(exc: Exception) {
//                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }
    // creates a folder inside internal storage
    private fun getOutputDirectory(): File {
        val mediaDir = requireContext().externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else requireContext().filesDir
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }


    private fun showConfirmLoading(){
        val dialogView = layoutInflater.inflate(R.layout.dialog_confirmation, null)
        val imageView : ImageView = dialogView.findViewById(R.id.image)
        imageView.setImageBitmap(BitmapFactory.decodeFile(compressedImage?.path))
        confirmDialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(false)
            .setNegativeButton(resources.getString(R.string.cancel)) { _, _ ->
                // Respond to neutral button press
            }
            .setPositiveButton(resources.getString(R.string.publish)) { _, _ ->
                // Respond to positive button press
                Toast.makeText(requireContext(), "Traitement de l\' image", Toast.LENGTH_SHORT).show()
                dialogLoading()
                if(id != null) enrollEmployee(createImageData(Uri.fromFile(compressedImage)))
                    else checkEmployee(createImageData(Uri.fromFile(compressedImage)))

            }
            .show()
    }

    private fun dialogLoading(){
        val dialogView = layoutInflater.inflate(R.layout.dialog_message, null)
        loadingDialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(false)
            .show()
    }

    private fun enrollEmployee(imageDataByteArray : ByteArray?) {
        val queue = Volley.newRequestQueue(context)
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val app =  App()
        val request = object : VolleyFileUploadRequest(
            Method.POST,
            "${app.url}/enroll/${id}",
            Response.Listener {
                Toast.makeText(context,"L'etudiant est inscrit avec succès", Toast.LENGTH_LONG).show()
                loadingDialog.dismiss()
            },
            Response.ErrorListener {
                Toast.makeText(context,"Error", Toast.LENGTH_LONG).show()
                println("error is: $it")
                loadingDialog.dismiss()
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                val params: MutableMap<String, String> = HashMap()
                params["Accept"] = "application/json"
                return params
            }
            override fun getByteData(): MutableMap<String, DataPart> {
                val params = HashMap<String, DataPart>()
                params["photo"] = DataPart("PNG_${timeStamp}_.png", imageDataByteArray!! , "image/*")
                return params
            }
        }
        queue.add(request).retryPolicy = DefaultRetryPolicy(0, 0, 0F)
    }

    private fun checkEmployee(imageDataByteArray : ByteArray?) {
        val queue = Volley.newRequestQueue(context)
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val app =  App()
        val request = object : VolleyFileUploadRequest(
            Method.POST,
            "${app.url}/check",
        Response.Listener {response ->
                val responseData = String(response.data, Charset.forName("UTF-8"))
                val jsonResponse = JSONObject(responseData)


            // Récupération et personnalisation des données JSON
            val name = jsonResponse.optString("name")
            val sexe  = jsonResponse.optString("sexe")
            val adresse  = jsonResponse.optString("adresse")
            val promotion  = jsonResponse.optString("promotion")
            val annee_academique  = jsonResponse.optString("annee_academique")
            // Création d'un texte formaté pour un affichage clair
            val formattedText = """
                Name : $name
                Sexe : $sexe
                Adresse  : $adresse
                Promotion  : $promotion
                Annee  : $annee_academique

            """.trimIndent()

            // Création d'un TextView pour afficher un texte personnalisé
            val textView = TextView(context)
            textView.text = formattedText
            textView.setPadding(40, 20, 40, 20) // Espacement interne
            textView.textSize = 16f // Taille du texte
            // Toast.makeText(context,jsonResponse.toString(), Toast.LENGTH_LONG).show()
            // Afficher le JSON dans un AlertDialog
            // Affichage dans un AlertDialog
            AlertDialog.Builder(context)
                .setTitle("Information sur l' etudiant")
                .setView(textView)
                .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                .show()
            loadingDialog.dismiss()

        },

            Response.ErrorListener {
                val response = String(it.networkResponse?.data ?: ByteArray(0), Charset.forName(
                    HttpHeaderParser.parseCharset(it.networkResponse?.headers)))
                if(it.networkResponse?.statusCode != 400 ){
                    Toast.makeText(context,"error", Toast.LENGTH_LONG).show()

                }else{
                    val r = JSONTokener(response).nextValue() as JSONObject
                    val error = r.getString("message")
                    Toast.makeText(context,error, Toast.LENGTH_LONG).show()
                }
                loadingDialog.dismiss()
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                val params: MutableMap<String, String> = HashMap()
                params["Accept"] = "application/json"
                return params
            }
            override fun getByteData(): MutableMap<String, DataPart> {
                val params = HashMap<String, DataPart>()
                params["photo"] = DataPart("PNG_${timeStamp}_.png", imageDataByteArray!! , "image/*")
                return params
            }
        }
        queue.add(request).retryPolicy = DefaultRetryPolicy(0, 0, 0F)
    }

    @Throws(IOException::class)
    private fun createImageData(uri: Uri) : ByteArray {
        var byte : ByteArray? = null
        val inputStream = requireActivity().contentResolver.openInputStream(uri)
        inputStream?.buffered()?.use {
            byte = it.readBytes()
        }
        return byte!!
    }


}