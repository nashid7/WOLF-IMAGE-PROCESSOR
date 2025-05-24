package com.example.new1

import android.Manifest
import android.app.Activity
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Typeface
import android.net.Uri as AndroidUri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.Surface
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executor
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import kotlin.random.Random
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import android.view.WindowManager
import android.os.Build
import android.media.MediaScannerConnection
import android.widget.Toast
import android.os.Vibrator
import android.os.VibrationEffect
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalConfiguration
import kotlin.math.roundToInt
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.input.pointer.util.addPointerInputChange
import android.app.ActivityManager
import android.content.pm.ApplicationInfo
import android.provider.Settings
import android.content.Intent
import android.provider.Settings.Secure
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import android.util.Base64
import java.security.SecureRandom

class MainActivity : ComponentActivity() {
    private val requiredPermissions = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_MEDIA_IMAGES
    )

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            Log.d("MainActivity", "All permissions granted")
        } else {
            Log.d("MainActivity", "Some permissions were denied")
        }
    }

    private fun isDeviceRooted(): Boolean {
        val buildTags = Build.TAGS
        if (buildTags != null && buildTags.contains("test-keys")) return true

        val paths = arrayOf(
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su"
        )

        for (path in paths) {
            if (File(path).exists()) return true
        }

        return false
    }

    private fun isEmulator(): Boolean {
        return (Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")
                || "google_sdk" == Build.PRODUCT)
    }

    private fun isDebuggerConnected(): Boolean {
        return android.os.Debug.isDebuggerConnected()
    }

    private fun checkSecurity() {
        if (isDeviceRooted()) {
            Toast.makeText(this, "Security violation: Rooted device detected", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        if (isEmulator()) {
            Toast.makeText(this, "Security violation: Emulator detected", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // Only check for debug mode in release builds
        if (!BuildConfig.DEBUG) {
            if (isDebuggerConnected()) {
                Toast.makeText(this, "Security violation: Debugger detected", Toast.LENGTH_LONG).show()
                finish()
                return
            }

            if (0 != applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) {
                Toast.makeText(this, "Security violation: Debug mode detected", Toast.LENGTH_LONG).show()
                finish()
                return
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Check security before proceeding
        checkSecurity()
        
        // Prevent screen capture
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
        
        // Set the window background to black
        window.setBackgroundDrawableResource(android.R.color.black)
        
        // Handle display cutout
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes = window.attributes.apply {
                layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
        }
        
        requestPermissionLauncher.launch(requiredPermissions)
        
        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = Color.Black,
                    onPrimary = Color.White,
                    secondary = Color.White,
                    onSecondary = Color.Black,
                    background = Color.Black,
                    surface = Color.Black,
                    surfaceVariant = Color.Black,
                    onSurface = Color.White,
                    onBackground = Color.White,
                    primaryContainer = Color.Black,
                    onPrimaryContainer = Color.White,
                    secondaryContainer = Color.Black,
                    onSecondaryContainer = Color.White
                )
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = "camera") {
                        composable("camera") {
                            CameraScreen(
                                onNavigateToGallery = { navController.navigate("gallery") }
                            )
                        }
                        composable("gallery") {
                            GalleryScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Recheck security on resume
        checkSecurity()
    }
}

@Composable
fun CameraScreen(
    onNavigateToGallery: () -> Unit
) {
    var currentSerialNumber by remember { mutableStateOf("") }
    var showSerialNumberEditor by remember { mutableStateOf(false) }
    var showFlash by remember { mutableStateOf(false) }
    var lastCapturedImageUri by remember { mutableStateOf<AndroidUri?>(null) }
    var captureButtonScale by remember { mutableStateOf(1f) }
    var focusPoint by remember { mutableStateOf<Offset?>(null) }
    var showFocusIndicator by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val hasCameraPermission = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED

    if (!hasCameraPermission) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Camera permission is required",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Light),
                color = Color.White
            )
        }
        return
    }

    val previewView = remember { PreviewView(context) }
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }

    LaunchedEffect(previewView) {
        try {
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder()
                .setTargetRotation(previewView.display.rotation)
                .build()
            
            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .setTargetRotation(previewView.display.rotation)
                .build()

            preview.setSurfaceProvider(previewView.surfaceProvider)

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )
            } catch (e: Exception) {
                Log.e("CameraScreen", "Error binding camera", e)
                Toast.makeText(context, "Failed to start camera: ${e.message}", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Log.e("CameraScreen", "Camera initialization failed", e)
            Toast.makeText(context, "Failed to initialize camera: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            try {
                cameraProviderFuture.get().unbindAll()
            } catch (e: Exception) {
                Log.e("CameraScreen", "Error unbinding camera", e)
            }
        }
    }

    fun captureImage() {
        try {
            val currentImageCapture = imageCapture
            if (currentImageCapture == null) {
                Toast.makeText(context, "Camera not ready. Please try again.", Toast.LENGTH_SHORT).show()
                return
            }

            // Animate capture button
            captureButtonScale = 0.92f
            Handler(Looper.getMainLooper()).postDelayed({ captureButtonScale = 1f }, 120)
            
            // Show flash
            showFlash = true
            Handler(Looper.getMainLooper()).postDelayed({ showFlash = false }, 120)
            
            // Create timestamped name with full serial number and first 4 digits
            val timestamp = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US)
                .format(System.currentTimeMillis())
            val firstFourDigits = currentSerialNumber.take(4)
            val name = "${currentSerialNumber}_${firstFourDigits}_${timestamp}"
            
            // First save to app's private storage
            val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            if (storageDir == null) {
                Toast.makeText(context, "Failed to access storage", Toast.LENGTH_SHORT).show()
                return
            }

            val imageFile = File.createTempFile(
                name,
                ".jpg",
                storageDir
            )
            
            val outputOptions = ImageCapture.OutputFileOptions.Builder(imageFile).build()

            currentImageCapture.takePicture(
                outputOptions,
                ContextCompat.getMainExecutor(context),
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                        var bitmap: Bitmap? = null
                        try {
                            // Get the saved image as a bitmap
                            bitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
                            
                            if (bitmap == null) {
                                Toast.makeText(context, "Failed to process image", Toast.LENGTH_SHORT).show()
                                return
                            }

                            val matrix = Matrix()
                            val displayMetrics = context.resources.displayMetrics
                            val screenWidth = displayMetrics.widthPixels
                            val screenHeight = displayMetrics.heightPixels
                            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                            val rotation = windowManager.defaultDisplay.rotation
                            val isLandscape = context.resources.configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
                                
                            when (rotation) {
                                Surface.ROTATION_0 -> if (!isLandscape) matrix.postRotate(90f)
                                Surface.ROTATION_90 -> if (isLandscape) matrix.postRotate(0f) else matrix.postRotate(0f)
                                Surface.ROTATION_180 -> if (!isLandscape) matrix.postRotate(270f)
                                Surface.ROTATION_270 -> if (isLandscape) matrix.postRotate(180f) else matrix.postRotate(180f)
                            }
                                
                            val rotatedBitmap = Bitmap.createBitmap(
                                bitmap,
                                0,
                                0,
                                bitmap.width,
                                bitmap.height,
                                matrix,
                                true
                            )
                                
                            // Add serial number overlay
                            val overlayBitmap = addSerialNumberOverlay(rotatedBitmap, currentSerialNumber)
                                
                            // Save to MediaStore
                            val contentValues = ContentValues().apply {
                                put(MediaStore.Images.Media.DISPLAY_NAME, "${name}_${System.currentTimeMillis()}.jpg")
                                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                                put(MediaStore.Images.Media.IS_PENDING, 1)
                                put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000)
                                put(MediaStore.Images.Media.DATE_MODIFIED, System.currentTimeMillis() / 1000)
                            }
                                
                            val uri = context.contentResolver.insert(
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                contentValues
                            )
                                
                            if (uri != null) {
                                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                                    overlayBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                                    outputStream.flush()
                                }
                                    
                                // Update the MediaStore entry to make it visible
                                contentValues.clear()
                                contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                                context.contentResolver.update(uri, contentValues, null, null)
                                    
                                // Notify media scanner
                                MediaScannerConnection.scanFile(
                                    context,
                                    arrayOf(uri.toString()),
                                    arrayOf("image/jpeg"),
                                    null
                                )
                                    
                                lastCapturedImageUri = uri
                                Log.d("CameraScreen", "Image saved successfully to: $uri")
                                    
                                Handler(Looper.getMainLooper()).post {
                                    Toast.makeText(
                                        context,
                                        "Image saved to Pictures folder",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } else {
                                Log.e("CameraScreen", "Failed to create MediaStore entry")
                                Handler(Looper.getMainLooper()).post {
                                    Toast.makeText(
                                        context,
                                        "Failed to save image",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("CameraScreen", "Error processing image: ${e.message}", e)
                            Handler(Looper.getMainLooper()).post {
                                Toast.makeText(
                                    context,
                                    "Error processing image: ${e.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } finally {
                            try {
                                // Clean up bitmaps
                                bitmap?.recycle()
                                // Delete temporary file
                                if (imageFile.exists()) {
                                    imageFile.delete()
                                }
                            } catch (e: Exception) {
                                Log.e("CameraScreen", "Error cleaning up resources: ${e.message}", e)
                            }
                        }
                    }
                    
                    override fun onError(exception: ImageCaptureException) {
                        Log.e("CameraScreen", "Photo capture failed: ${exception.message}", exception)
                        Handler(Looper.getMainLooper()).post {
                            Toast.makeText(
                                context,
                                "Failed to capture image: ${exception.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            )
        } catch (e: Exception) {
            Log.e("CameraScreen", "Error in captureImage: ${e.message}", e)
            Toast.makeText(context, "Error capturing image: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    try {
                        focusPoint = offset
                        showFocusIndicator = true
                        // Hide focus indicator after delay
                        Handler(Looper.getMainLooper()).postDelayed({
                            showFocusIndicator = false
                        }, 1000)
                    } catch (e: Exception) {
                        Log.e("CameraScreen", "Error handling tap gesture", e)
                    }
                }
            }
    ) {
        // Camera Preview with rounded corners
        AndroidView(
            factory = { previewView },
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .clip(RoundedCornerShape(24.dp))
        )

        // Focus Indicator
        if (showFocusIndicator && focusPoint != null) {
            Box(
                modifier = Modifier
                    .offset { 
                        IntOffset(
                            (focusPoint!!.x - 40.dp.toPx()).roundToInt(),
                            (focusPoint!!.y - 40.dp.toPx()).roundToInt()
                        )
                    }
                    .size(80.dp)
                    .graphicsLayer {
                        scaleX = 1f
                        scaleY = 1f
                        alpha = 1f
                    }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .border(2.dp, Color.White, CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .align(Alignment.Center)
                        .border(1.dp, Color.White, CircleShape)
                )
            }
        }

        // Flash overlay with smoother animation
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White.copy(alpha = if (showFlash) 0.7f else 0f))
        ) {}

        // Top Controls with enhanced styling
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 72.dp, end = 28.dp),
            contentAlignment = Alignment.TopEnd
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Gallery Button with enhanced styling
                IconButton(
                    onClick = onNavigateToGallery,
                    modifier = Modifier
                        .size(52.dp)
                        .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                        .shadow(12.dp, CircleShape, clip = false)
                        .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoLibrary,
                        contentDescription = "Gallery",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Serial Number Button with enhanced styling
                IconButton(
                    onClick = { showSerialNumberEditor = true },
                    modifier = Modifier
                        .height(52.dp)
                        .padding(horizontal = 16.dp)
                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(26.dp))
                        .shadow(12.dp, RoundedCornerShape(26.dp), clip = false)
                        .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(26.dp))
                ) {
                    Text(
                        text = if (currentSerialNumber.isEmpty()) "Enter SN" else currentSerialNumber,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 0.5.sp
                        )
                    )
                }
            }
        }

        // Bottom Controls with enhanced styling
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 72.dp),
            contentAlignment = Alignment.Center
        ) {
            // Capture Button with enhanced styling
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .background(Color.White, CircleShape)
                    .shadow(24.dp, CircleShape, clip = false)
                    .border(2.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                    .clickable { captureImage() },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .background(Color.Black, CircleShape)
                        .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                )
            }
        }

        // Serial Number Editor with enhanced styling
        AnimatedVisibility(
            visible = showSerialNumberEditor,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it })
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.9f))
                    .clickable { showSerialNumberEditor = false },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Text(
                        text = "Enter Serial Number",
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    )
                    
                    OutlinedTextField(
                        value = currentSerialNumber,
                        onValueChange = { 
                            if (it.length <= 10) {
                                currentSerialNumber = it.uppercase()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White.copy(alpha = 0.1f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.1f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = Color.White,
                            focusedIndicatorColor = Color.White.copy(alpha = 0.5f),
                            unfocusedIndicatorColor = Color.White.copy(alpha = 0.3f)
                        ),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 1.sp
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        placeholder = { Text("Max 10 characters", color = Color.White.copy(alpha = 0.5f)) }
                    )
                    
                    Button(
                        onClick = { showSerialNumberEditor = false },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Done",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }
            }
        }
    }
}

fun addSerialNumberOverlay(bitmap: Bitmap, serialNumber: String): Bitmap {
    // Create a new bitmap with extra space at the top
    val newBitmap = Bitmap.createBitmap(
        bitmap.width,
        bitmap.height + 100, // Add 100 pixels at the top
        Bitmap.Config.ARGB_8888
    )
    
    val canvas = Canvas(newBitmap)
    
    // Fill the top area with white
    canvas.drawColor(android.graphics.Color.WHITE)
    
    // Draw the original image below the white space
    canvas.drawBitmap(bitmap, 0f, 100f, null)
    
    val paint = Paint().apply {
        color = android.graphics.Color.BLACK
        textSize = 60f // Increased font size
        typeface = Typeface.DEFAULT_BOLD // Make it bold
        isAntiAlias = true
    }
    
    // Position text at top left with some padding
    val x = 20f // Left padding
    val y = 70f // Position in the white space
    
    // Draw the text
    canvas.drawText(serialNumber, x, y, paint)
    
    return newBitmap
}

@Composable
fun GalleryScreen(
    onNavigateBack: () -> Unit
) {
    var images by remember { mutableStateOf<List<AndroidUri>>(emptyList()) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATE_ADDED
        )
        val selection = "${MediaStore.Images.Media.DISPLAY_NAME} LIKE ?"
        val selectionArgs = arrayOf("%_%_%") // Match our naming pattern
        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

        val cursor = context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )

        cursor?.use {
            val idColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val uris = mutableListOf<AndroidUri>()
            while (it.moveToNext()) {
                val id = it.getLong(idColumn)
                val contentUri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    id
                )
                uris.add(contentUri)
            }
            images = uris
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Top Bar with enhanced styling
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.8f))
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .size(48.dp)
                    .background(Color.White.copy(alpha = 0.1f), CircleShape)
                    .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Text(
                text = "Gallery",
                style = MaterialTheme.typography.titleLarge.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                ),
                modifier = Modifier.align(Alignment.Center)
            )
        }

        if (images.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoLibrary,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.3f),
                        modifier = Modifier.size(64.dp)
                    )
                    Text(
                        "No images found",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Medium
                        )
                    )
                    Text(
                        "Check your device's Pictures folder",
                        color = Color.White.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(top = 72.dp)
            ) {
                items(images) { uri ->
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(uri)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Captured image",
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                            .shadow(8.dp, RoundedCornerShape(16.dp), clip = false),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
}

// Add encryption utilities
object SecurityUtils {
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val KEY_ALIAS = "AppKey"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"

    fun encryptData(data: String): String {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null)

        if (!keyStore.containsAlias(KEY_ALIAS)) {
            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                ANDROID_KEYSTORE
            )

            val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(true)
                .build()

            keyGenerator.init(keyGenParameterSpec)
            keyGenerator.generateKey()
        }

        val cipher = Cipher.getInstance(TRANSFORMATION)
        val key = keyStore.getKey(KEY_ALIAS, null) as SecretKey
        cipher.init(Cipher.ENCRYPT_MODE, key)

        val encrypted = cipher.doFinal(data.toByteArray(Charsets.UTF_8))
        val combined = ByteArray(cipher.iv.size + encrypted.size)
        System.arraycopy(cipher.iv, 0, combined, 0, cipher.iv.size)
        System.arraycopy(encrypted, 0, combined, cipher.iv.size, encrypted.size)

        return Base64.encodeToString(combined, Base64.DEFAULT)
    }

    fun decryptData(encryptedData: String): String {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null)

        val combined = Base64.decode(encryptedData, Base64.DEFAULT)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val key = keyStore.getKey(KEY_ALIAS, null) as SecretKey

        val iv = ByteArray(12)
        System.arraycopy(combined, 0, iv, 0, iv.size)
        val encrypted = ByteArray(combined.size - iv.size)
        System.arraycopy(combined, iv.size, encrypted, 0, encrypted.size)

        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(128, iv))
        return String(cipher.doFinal(encrypted), Charsets.UTF_8)
    }
} 