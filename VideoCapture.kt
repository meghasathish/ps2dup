package com.example.ps2dup

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import android.media.ImageReader
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.util.Log
import android.view.Surface
import android.view.TextureView
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.*
import androidx.camera.video.VideoCapture
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import com.example.ps2.Constants.TAG
import com.example.ps2dup.databinding.ActivityMainBinding
import com.example.ps2dup.databinding.ActivityVideoBinding

import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

//import androidx.core.content.pm.ShortcutInfoCompat.Surface as Surface
//typealias LumaListener = (luma: Double) -> Unit
class VideoCapture:Camera() {
    private lateinit var viewBinding: ActivityVideoBinding

    private var videoCapture: VideoCapture<Recorder>? = null
    private var recording: Recording? = null

    private val curRecording = recording

    private lateinit var cameraExecutor: ExecutorService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityVideoBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            requestPermissions()
        }
        viewBinding.videoCaptureButton.setOnClickListener { captureVideo() }

        cameraExecutor = Executors.newSingleThreadExecutor()



    }
    // Implements VideoCapture use case, including start and stop capturing.
    private fun captureVideo() {
        val videoCapture = this.videoCapture ?: return

        viewBinding.videoCaptureButton.isEnabled = false

        val curRecording = recording
        if (curRecording != null) {
            // Stop the current recording session.
            curRecording.stop()
            recording = null
            return
        }

        // create and start a new recording session
        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US)
            .format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/CameraX-Video")
            }
        }

        val mediaStoreOutputOptions = MediaStoreOutputOptions
            .Builder(contentResolver, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
            .setContentValues(contentValues)
            .build()
        recording = videoCapture.output
            .prepareRecording(this, mediaStoreOutputOptions)
            .apply {
                if (PermissionChecker.checkSelfPermission(this@VideoCapture,
                        Manifest.permission.RECORD_AUDIO) ==
                    PermissionChecker.PERMISSION_GRANTED)
                {
                    withAudioEnabled()
                }
            }
            .start(ContextCompat.getMainExecutor(this)) { recordEvent ->
                when(recordEvent) {
                    is VideoRecordEvent.Start -> {
                        viewBinding.videoCaptureButton.apply {
                            starTimer(pauseOffSet)
                            text = getString(R.string.stop_capture)
                            isEnabled = true
                        }
                    }
                    is VideoRecordEvent.Finalize -> {
                        if (!recordEvent.hasError()) {
                            val msg = "Video capture succeeded: " +
                                    "${recordEvent.outputResults.outputUri}"
                            Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT)
                                .show()
                            Log.d(TAG, msg)
                        } else {
                            recording?.close()
                            recording = null
                            Log.e(TAG, "Video capture ends with error: " +
                                    "${recordEvent.error}")
                        }
                        viewBinding.videoCaptureButton.apply {
                            text = getString(R.string.start_capture)
                            isEnabled = true
                        }
                    }
                }
            }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewBinding.viewFinder.surfaceProvider)
                }
            val recorder = Recorder.Builder()
                .setQualitySelector(QualitySelector.from(Quality.HIGHEST))
                .build()
            videoCapture = VideoCapture.withOutput(recorder)
//            imageCapture = ImageCapture.Builder().build()

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview,videoCapture)

            } catch(exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun requestPermissions() {
        activityResultLauncher.launch(REQUIRED_PERMISSIONS)
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    companion object {
        private const val TAG = "CameraXApp"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private val REQUIRED_PERMISSIONS =
            mutableListOf (
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
    }

    private val activityResultLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions())
        { permissions ->
            // Handle Permission granted/rejected
            var permissionGranted = true
            permissions.entries.forEach {
                if (it.key in REQUIRED_PERMISSIONS && it.value == false)
                    permissionGranted = false
            }
            if (!permissionGranted) {
                Toast.makeText(baseContext,
                    "Permission request denied",
                    Toast.LENGTH_SHORT).show()
            } else {
                startCamera()
            }
        }

    private fun starTimer(pauseOffSetL : Long){
        val tv=findViewById<TextView>(R.id.tv)
        countdowntimer = object : CountDownTimer(time_in_milliseconds - pauseOffSetL, 1000){
            override fun onTick(millisUntilFinished: Long) {
                pauseOffSet = time_in_milliseconds - millisUntilFinished
                tv.text= (millisUntilFinished/1000).toString()
            }
            override fun onFinish() {
                val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US)
                    .format(System.currentTimeMillis())
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, name)
                    put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                        put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/CameraX-Video")
                    }
                }
                recording?.stop()
                Toast.makeText(this@VideoCapture, "Timer finished", Toast.LENGTH_LONG).show()
            }
        }.start()
    }

}





//    private lateinit var binding: ActivityVideoBinding
//    private var imageCapture: ImageCapture?=null
//    private lateinit var outputDirectory: File
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        binding=ActivityVideoBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        outputDirectory=getOutputDirectory()
//
//        if(allPermissionsGranted()){
//            startCamera()
//        }else{
//            ActivityCompat.requestPermissions(
//                this,Constants.REQUIRED_PERMISSIONS, Constants.REQUEST_CODE_PERMISSIONS
//            )
//        }
//
//    }
//    private fun allPermissionsGranted()=
//        Constants.REQUIRED_PERMISSIONS.all{
//            ContextCompat.checkSelfPermission(baseContext,it)==PackageManager.PERMISSION_GRANTED
//        }
//
//    private fun getOutputDirectory(): File {
//        val mediaDir=externalMediaDirs.firstOrNull()?.let{mFile->
//            File(mFile,resources.getString(R.string.app_name)).apply {
//                mkdirs()
//            }
//        }
//        return if(mediaDir!= null && mediaDir.exists())
//            mediaDir else filesDir
//    }
//
//    private fun takePhoto(){
//        val imageCapture=imageCapture?: return
//        val photoFile= File(
//            outputDirectory,
//            SimpleDateFormat(Constants.FILE_NAME_FORMAT,
//                Locale.getDefault()).format(System.currentTimeMillis())+".jpg")
//
//        val outputOption=ImageCapture.OutputFileOptions.Builder(photoFile).build()
//
//        imageCapture.takePicture(outputOption, ContextCompat.getMainExecutor(this),
//            object :ImageCapture.OnImageSavedCallback{
//                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
//                    val savedUri= Uri.fromFile(photoFile)
//                    val msg="Photo saved"
//
//                    Toast.makeText(this@VideoCapture,"$msg $savedUri", Toast.LENGTH_SHORT).show()
//                }
//
//                override fun onError(exception: ImageCaptureException) {
//                    Log.d(TAG,"onError:${exception.message}",exception)
//                }
//            }
//        )
//    }
//    private fun startCamera(){
//        val cameraProviderFuture= ProcessCameraProvider.getInstance(this)
//
//        cameraProviderFuture.addListener({
//            val cameraProvider:ProcessCameraProvider=cameraProviderFuture.get()
//            val preview= Preview.Builder().build().also {
//                    mPreview->mPreview.setSurfaceProvider(binding.viewFinder.surfaceProvider)
//            }
//            imageCapture=ImageCapture.Builder().build()
//            val cameraSelector= CameraSelector.DEFAULT_BACK_CAMERA
//            try{
//                cameraProvider.unbindAll()
//                cameraProvider.bindToLifecycle(this,cameraSelector,preview,imageCapture)
//
//            }catch(e:Exception){
//                Log.d(Constants.TAG,"start cam fail",e)
//            }
//        }, ContextCompat.getMainExecutor(this))
//    }
//}


















//    lateinit var cameraManager:CameraManager
//    lateinit var textureView: TextureView
//    lateinit var cameraCaptureSession: CameraCaptureSession
//    lateinit var cameraDevice: CameraDevice
//    lateinit var captureRequest: CaptureRequest
//    lateinit var handler: Handler
//    lateinit var handlerThread: HandlerThread
//    lateinit var capReq:CaptureRequest.Builder
//    lateinit var videoReader: ImageReader
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_video)
//
//        textureView=findViewById(R.id.textureView)
//        cameraManager=getSystemService(Context.CAMERA_SERVICE)as CameraManager
//        handlerThread= HandlerThread("videoThread")
//        handlerThread.start()
//        handler= Handler((handlerThread).looper)
//
//
//        textureView.surfaceTextureListener=object :TextureView.SurfaceTextureListener{
//            override fun onSurfaceTextureAvailable(p0: SurfaceTexture, p1: Int, p2: Int) {
//                open_camera()
//
//            }
//
//            override fun onSurfaceTextureSizeChanged(p0: SurfaceTexture, p1: Int, p2: Int) {
//
//            }
//
//            override fun onSurfaceTextureDestroyed(p0: SurfaceTexture): Boolean {
//                return false
//
//            }
//
//            override fun onSurfaceTextureUpdated(p0: SurfaceTexture) {
//
//            }
//        }
//
//        val rcrd=findViewById<Button>(R.id.record)
//        rcrd.setOnClickListener(){
//            capReq=cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD)
//            var surface= Surface(textureView.surfaceTexture)
//            capReq.addTarget(surface)
//            cameraCaptureSession.setRepeatingRequest(capReq.build(),null,null)
//        }
//    }
//
//    fun open_camera(){
//        if (ActivityCompat.checkSelfPermission(
//                this,
//                Manifest.permission.CAMERA
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            return
//        }
//        cameraManager.openCamera(cameraManager.cameraIdList[1],object :CameraDevice.StateCallback(){
//            override fun onOpened(p0: CameraDevice) {
//                cameraDevice=p0
//                capReq=cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
//                var surface= Surface(textureView.surfaceTexture)
//                capReq.addTarget(surface)
//
//                cameraDevice.createCaptureSession(listOf(surface),object :CameraCaptureSession.StateCallback(){
//                    override fun onConfigured(p0: CameraCaptureSession) {
//                        cameraCaptureSession=p0
//                        cameraCaptureSession.setRepeatingRequest(capReq.build(),null,null)
//
//                    }
//
//                    override fun onConfigureFailed(p0: CameraCaptureSession) {
//
//                    }
//                },handler)
//
//            }
//
//            override fun onDisconnected(p0: CameraDevice) {
//
//            }
//
//            override fun onError(p0: CameraDevice, p1: Int) {
//
//            }
//        },handler)
//    }
//
//}