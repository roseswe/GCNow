// GCNow! - Geocaching Helper App v2.4.1 -- _VERSION_
// @(#) $Id: MainActivity.kt,v 2.4 2026/05/05 08:23:36 ralph Exp $
// Optimized for Android 15/16 (16KB Aligned)
// Features: 25 Curated Themes, Live Camera (Screenshot Fixed), WGS84 GPS.

package com.example.rose_swe

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Rect
import android.media.MediaActionSound
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.view.PixelCopy
import android.view.View
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.AsyncImage
import com.google.android.gms.location.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        enableEdgeToEdge()
        setContent { MainScreen(fusedLocationClient) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(fusedLocationClient: FusedLocationProviderClient) {
    val context = LocalContext.current
    val view = LocalView.current
    val prefs = remember { context.getSharedPreferences("rose_prefs", Context.MODE_PRIVATE) }
    val shutterSound = remember { MediaActionSound() }

    // --- State Persistence ---
    var teamText by remember { mutableStateOf(prefs.getString("team_name", "rose_swe") ?: "rose_swe") }
    var imageUriString by remember { mutableStateOf(prefs.getString("image_uri", null)) }
    var bgColorInt by remember { mutableIntStateOf(prefs.getInt("bg_color", Color(0xFF121212).toArgb())) }
    var isDarkMode by remember { mutableStateOf(prefs.getBoolean("dark_mode", true)) }

    // --- Feature Toggles ---
    var showGps by remember { mutableStateOf(prefs.getBoolean("show_gps", false)) }
    var showScreenshotBtn by remember { mutableStateOf(prefs.getBoolean("show_screenshot_btn", false)) }
    var showLiveCamera by remember { mutableStateOf(prefs.getBoolean("show_live_camera", false)) }
    var isCapturing by remember { mutableStateOf(false) }
    var updateInterval by remember { mutableFloatStateOf(prefs.getFloat("gps_interval", 3f)) }
    var gpsCoords by remember { mutableStateOf("Locating...") }
    var gpsAccuracy by remember { mutableStateOf("") }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var showAboutDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showColorDialog by remember { mutableStateOf(false) }

    // Logic: Screenshot Handler
    LaunchedEffect(isCapturing) {
        if (isCapturing) {
            shutterSound.play(MediaActionSound.SHUTTER_CLICK)
            delay(200)
            captureScreenshot(context as Activity, view) { isCapturing = false }
        }
    }

    // Logic: GPS Tracking
    LaunchedEffect(showGps, updateInterval) {
        if (showGps && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, (updateInterval * 1000).toLong()).build()
            val callback = object : LocationCallback() {
                override fun onLocationResult(res: LocationResult) {
                    res.lastLocation?.let {
                        gpsCoords = formatWGS84(it.latitude, it.longitude)
                        gpsAccuracy = String.format(Locale.US, "+/- %.2f m", it.accuracy)
                    }
                }
            }
            fusedLocationClient.requestLocationUpdates(request, callback, Looper.getMainLooper())
        }
    }

    // Launchers
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    val gpsLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    val photoPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            imageUriString = uri.toString()
            prefs.edit { putString("image_uri", imageUriString) }
        }
    }

    val colorScheme = if (isDarkMode) darkColorScheme(primary = Color(0xFF00FFCC), background = Color(bgColorInt))
    else lightColorScheme(primary = Color(0xFF007A66), background = Color(bgColorInt))

    MaterialTheme(colorScheme = colorScheme) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet(drawerContainerColor = if (isDarkMode) Color(0xFF1A1A1A) else Color(0xFFF0F0F0)) {
                    Spacer(Modifier.height(16.dp))
                    Text("GCNow! Controls", Modifier.padding(16.dp), fontSize = 22.sp, fontWeight = FontWeight.Bold)

                    val accent = Color(0xFF00FFCC)
                    val navColors = NavigationDrawerItemDefaults.colors(unselectedIconColor = accent, selectedIconColor = accent)

                    NavigationDrawerItem(label = { Text("Live Camera") }, selected = false, icon = { Icon(Icons.Default.Videocam, null) },
                        badge = { Switch(checked = showLiveCamera, onCheckedChange = {
                            showLiveCamera = it
                            prefs.edit { putBoolean("show_live_camera", it) }
                            if (it) cameraLauncher.launch(Manifest.permission.CAMERA)
                        }) }, colors = navColors, onClick = {})

                    NavigationDrawerItem(label = { Text("GPS Tracking") }, selected = false, icon = { Icon(Icons.Default.LocationOn, null) },
                        badge = { Switch(checked = showGps, onCheckedChange = {
                            showGps = it; prefs.edit { putBoolean("show_gps", it) }
                            if (it) gpsLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                        }) }, colors = navColors, onClick = {})

                    NavigationDrawerItem(label = { Text("Screenshot Button") }, selected = false, icon = { Icon(Icons.Default.Screenshot, null) },
                        badge = { Switch(checked = showScreenshotBtn, onCheckedChange = {
                            showScreenshotBtn = it; prefs.edit { putBoolean("show_screenshot_btn", it) }
                        }) }, colors = navColors, onClick = {})

                    NavigationDrawerItem(label = { Text(if (isDarkMode) "Dark Mode" else "Light Mode") }, selected = false,
                        icon = { Icon(if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode, null) },
                        badge = { Switch(checked = isDarkMode, onCheckedChange = { isDarkMode = it; prefs.edit { putBoolean("dark_mode", it) } }) },
                        colors = navColors, onClick = {}
                    )

                    HorizontalDivider(Modifier.padding(16.dp))

                    NavigationDrawerItem(label = { Text("Change Image") }, selected = false, icon = { Icon(Icons.Default.Photo, null) }, colors = navColors,
                        onClick = { scope.launch { drawerState.close() }; photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) })

                    NavigationDrawerItem(label = { Text("Edit Team Name") }, selected = false, icon = { Icon(Icons.Default.Edit, null) }, colors = navColors,
                        onClick = { scope.launch { drawerState.close() }; showEditDialog = true })

                    NavigationDrawerItem(label = { Text("Dashboard Color") }, selected = false, icon = { Icon(Icons.Default.Palette, null) }, colors = navColors,
                        onClick = { scope.launch { drawerState.close() }; showColorDialog = true })

                    NavigationDrawerItem(label = { Text("About GCNow!") }, selected = false, icon = { Icon(Icons.Default.Info, null) }, colors = navColors,
                        onClick = { scope.launch { drawerState.close() }; showAboutDialog = true })
                }
            }
        ) {
            Scaffold(
                topBar = {
                    CenterAlignedTopAppBar(
                        title = { Text("GCNow!", fontWeight = FontWeight.Black) },
                        navigationIcon = { IconButton(onClick = { scope.launch { drawerState.open() } }) { Icon(Icons.Default.Menu, "Menu", tint = Color(0xFF00FFCC)) } },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                    )
                }
            ) { pv ->
                Box(modifier = Modifier.fillMaxSize()) {
                    Surface(Modifier.padding(pv).fillMaxSize(), color = Color(bgColorInt)) {
                        TeamContent(teamText, imageUriString, isDarkMode, showGps, gpsCoords, gpsAccuracy, showLiveCamera)
                    }

                    if (showScreenshotBtn && !isCapturing) {
                        IconButton(
                            onClick = { isCapturing = true },
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(start = 24.dp, bottom = 100.dp)
                                .background(Color.Black.copy(0.4f), CircleShape)
                        ) { Icon(Icons.Default.PhotoCamera, "Capture", tint = Color(0xFF00FFCC)) }
                    }
                }
            }
        }
    }

    // --- Dialog: Top 25 Curated Colors ---
    if (showColorDialog) {
        val colorOptions = listOf(
            // Row 1: Deep Blacks & Tech
            0xFF121212, 0xFF000000, 0xFF0D1B2A, 0xFF1B263B, 0xFF212121,
            // Row 2: Tactical Greens & Sea
            0xFF1B5E20, 0xFF33691E, 0xFF4D4D00, 0xFF012E40, 0xFF004D40,
            // Row 3: Earth & Adventure
            0xFF3E2723, 0xFF4E342E, 0xFFBF360C, 0xFF4A148C, 0xFF880E4F,
            // Row 4: Professional Greys
            0xFF263238, 0xFF37474F, 0xFF455A64, 0xFF4E4E50, 0xFF757575,
            // Row 5: Light/Outdoor Modes
            0xFFFFFFFF, 0xFFF5F5F5, 0xFFE3F2FD, 0xFFE8F5E9, 0xFFFFF9C4
        ).map { Color(it) }

        AlertDialog(
            onDismissRequest = { showColorDialog = false },
            title = { Text("Select Background") },
            text = {
                Column {
                    colorOptions.chunked(5).forEach { row ->
                        Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), Arrangement.SpaceEvenly) {
                            row.forEach { color ->
                                Box(Modifier.size(42.dp).background(color, CircleShape).clickable {
                                    bgColorInt = color.toArgb()
                                    prefs.edit { putInt("bg_color", bgColorInt) }
                                    showColorDialog = false
                                }.border(1.dp, Color.Gray.copy(0.5f), CircleShape))
                            }
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }

    if (showEditDialog) {
        var t by remember { mutableStateOf(teamText) }
        AlertDialog(onDismissRequest = { showEditDialog = false }, title = { Text("Team Name") },
            text = { OutlinedTextField(value = t, onValueChange = { t = it }, label = { Text("Enter text") }) },
            confirmButton = { Button(onClick = { teamText = t; prefs.edit { putString("team_name", t) }; showEditDialog = false }) { Text("Save") } })
    }

    if (showAboutDialog) {
        AlertDialog(onDismissRequest = { showAboutDialog = false }, confirmButton = { TextButton(onClick = { showAboutDialog = false }) { Text("OK") } },
            title = { Text("About GCNow!") },
            text = { Text("GCNow!\nGeocaching Helper App\nv2.4.1\n(C) by ROSE_SWE, Ralph Roth\nBuild: 03.05.2026\nArchitecture: 16KB Aligned | CameraX Ready") })  //_VERSION_
    }
}

@Composable
fun TeamContent(name: String, uri: String?, dark: Boolean, gpsOn: Boolean, coords: String, acc: String, camOn: Boolean) {
    var time by remember { mutableStateOf(LocalDateTime.now()) }
    val landscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    LaunchedEffect(Unit) { while (true) { time = LocalDateTime.now(); delay(1000) } }

    if (landscape) {
        Row(Modifier.fillMaxSize().padding(16.dp), Arrangement.SpaceEvenly, Alignment.CenterVertically) {
            Column(Modifier.weight(0.9f), horizontalAlignment = Alignment.CenterHorizontally) {
                DisplayImage(uri, Modifier.size(100.dp))
                if (camOn) {
                    Spacer(Modifier.height(8.dp))
                    CameraPreview(Modifier.fillMaxWidth().height(120.dp).clip(RoundedCornerShape(12.dp)))
                }
            }
            Column(Modifier.weight(1.1f), horizontalAlignment = Alignment.CenterHorizontally) {
                InfoTexts(name, time, dark, gpsOn, coords, acc, camOn)
            }
        }
    } else {
        Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            DisplayImage(uri, Modifier.size(if (camOn) 120.dp else 220.dp))
            if (camOn) {
                Spacer(Modifier.height(12.dp))
                CameraPreview(Modifier.weight(1f).fillMaxWidth().clip(RoundedCornerShape(12.dp)))
            }
            Spacer(Modifier.height(if (camOn) 12.dp else 30.dp))
            InfoTexts(name, time, dark, gpsOn, coords, acc, camOn)
        }
    }
}

@Composable
fun CameraPreview(modifier: Modifier) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    AndroidView(factory = { ctx ->
        val previewView = PreviewView(ctx)
        // CRITICAL: Set implementation mode to COMPATIBLE so PixelCopy captures the feed
        previewView.implementationMode = PreviewView.ImplementationMode.COMPATIBLE

        cameraProviderFuture.addListener({
            val provider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also { it.setSurfaceProvider(previewView.surfaceProvider) }
            try {
                provider.unbindAll()
                provider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview)
            } catch (e: Exception) { }
        }, ContextCompat.getMainExecutor(ctx))
        previewView
    }, modifier = modifier)
}

@Composable
fun InfoTexts(name: String, time: LocalDateTime, dark: Boolean, gpsOn: Boolean, coords: String, acc: String, camOn: Boolean) {
    val factor = if (camOn) 0.7f else 1f
    val mainColor = if (dark) Color.White else Color.Black
    Text(name, fontSize = (48 * factor).sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF00FFCC), textAlign = TextAlign.Center)
    Spacer(Modifier.height((12 * factor).dp))
    Text(time.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")), fontSize = (30 * factor).sp, color = mainColor)
    Text(time.format(DateTimeFormatter.ofPattern("HH:mm:ss")), fontSize = (38 * factor).sp, fontWeight = FontWeight.Bold, color = Color.Yellow)
    if (gpsOn) {
        Spacer(Modifier.height((10 * factor).dp))
        Text(coords, fontSize = (20 * factor).sp, color = Color(0xFF00FFCC), textAlign = TextAlign.Center, lineHeight = 24.sp)
        Text(acc, fontSize = (14 * factor).sp, color = Color.Gray)
    }
}

@Composable
fun DisplayImage(uri: String?, mod: Modifier) {
    if (uri != null) AsyncImage(model = uri.toUri(), contentDescription = null, modifier = mod, contentScale = ContentScale.Fit)
    else Image(painter = painterResource(id = R.drawable.user_image), contentDescription = null, modifier = mod, contentScale = ContentScale.Fit)
}

/**
 * CaptureScreenshot: Uses PixelCopy to render the current View into a Bitmap.
 * Note: CameraX must be in COMPATIBLE mode to be visible here.
 */
fun captureScreenshot(activity: Activity, view: View, onDone: () -> Unit) {
    val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
    val loc = IntArray(2)
    view.getLocationInWindow(loc)
    PixelCopy.request(activity.window, Rect(loc[0], loc[1], loc[0] + view.width, loc[1] + view.height), bitmap, {
        if (it == PixelCopy.SUCCESS) {
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, "GCNow_${System.currentTimeMillis()}.jpg")
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/GCNow")
            }
            val uri = activity.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            uri?.let { activity.contentResolver.openOutputStream(it).use { s -> bitmap.compress(Bitmap.CompressFormat.JPEG, 95, s!!) } }
        }
        onDone()
    }, Handler(Looper.getMainLooper()))
}

fun formatWGS84(lat: Double, lon: Double): String {
    fun conv(c: Double): String {
        val d = Math.abs(c).toInt()
        val m = (Math.abs(c) - d) * 60
        return String.format(Locale.US, "%02d° %06.3f'", d, m)
    }
    return "${if (lat >= 0) "N" else "S"} ${conv(lat)}\n${if (lon >= 0) "E" else "W"} ${conv(lon)}"
}
