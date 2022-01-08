package com.example.cameraxyt

import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getMainExecutor
import com.example.cameraxyt.databinding.ActivityMainBinding
import java.io.File
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding
    private var imageCapture : ImageCapture? = null
    private lateinit var outputDirectory : File

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        outputDirectory = getOutputDirectory()

        if(allPermissionGranted()){
            startCamera()
        }else{
            ActivityCompat.requestPermissions(
                this,Constants.REQUIRED_PERMISSIOS,Constants.REQUEST_CODE_PERMISSION)
        }

        binding.btnTakePhoto.setOnClickListener{takePhoto()
        }

    }

    private fun getOutputDirectory() : File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let{mfile->
            File(mfile,"CameraXYT").apply{ mkdirs() }
        }
         return if (mediaDir != null && mediaDir.exists()) mediaDir else filesDir
    }

    private fun takePhoto(){
        val imageCapture = imageCapture ?: return
        val photofile = File(outputDirectory,SimpleDateFormat(Constants.FILE_NAME_FORMAT, Locale.getDefault()).
                                                                                         format(System.currentTimeMillis())+ ".jpg")
          val outputOption = ImageCapture.OutputFileOptions.Builder(photofile).build()

        imageCapture.takePicture(outputOption,ContextCompat.getMainExecutor(this),object : ImageCapture.OnImageSavedCallback{

            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                val savedUri = Uri.fromFile(photofile)
                val msg = "Photo Saved"

                Toast.makeText(this@MainActivity, "$msg $savedUri", Toast.LENGTH_LONG).show()
            }

            override fun onError(exception: ImageCaptureException) {
                Log.e(Constants.TAG,
                      "Onerror : ${exception.message}",
                        exception)
            }
        })
    }


    private  fun startCamera(){
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({

            val cameraProvider : ProcessCameraProvider = cameraProviderFuture.get()
                                           val preview =Preview.Builder().build().also { mPreview ->
                                                                                          mPreview.setSurfaceProvider(
                                                                                              binding.viewFinder.surfaceProvider
                                                                                              )
                                           }

                            imageCapture= ImageCapture.Builder()
                                .build()
             val cameraSelector =CameraSelector.DEFAULT_BACK_CAMERA

            try{
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
            }catch (e : Exception){
                Log.d(Constants.TAG, "startCamera Fail", e)
            }
        }, getMainExecutor(this))

    }

    fun OnRequestPermissionsResult(requestCode: Int, permissions :Array<String>, grantResults : IntArray){
       if(requestCode == Constants.REQUEST_CODE_PERMISSION){
           if(allPermissionGranted()){
               startCamera()
                                      }else{
                                             Toast.makeText(this,"Permission not granted by the user",Toast.LENGTH_SHORT).show()
                                             finish()
                                           }
                                                           }

                                                                                                                       }

    private fun allPermissionGranted() =
        Constants.REQUIRED_PERMISSIOS.all {
            ContextCompat.checkSelfPermission(
            baseContext,it
        ) == PackageManager.PERMISSION_GRANTED
                                          }


}