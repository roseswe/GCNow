// @(#) $Id: MainActivity.kt,v 1.9 2026/04/22 14:12:43 ralph Exp $
// $Header: /home/cvs/src/android/GCNow/MainActivity.kt,v 1.9 2026/04/22 14:12:43 ralph Exp $

package com.example.rose_swe

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration // FIX: Import für Configuration
import android.os.Bundle
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.edit
import androidx.core.net.toUri
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
    val prefs = remember { context.getSharedPreferences("team_prefs", Context.MODE_PRIVATE) }

    var teamText by remember { mutableStateOf(prefs.getString("team_name", "rose_swe") ?: "rose_swe") }
    var imageUriString by remember { mutableStateOf(prefs.getString("image_uri", null)) }
    var bgColorInt by remember { mutableIntStateOf(prefs.getInt("bg_color", Color(0xFF202020).toArgb())) }
    var isDarkMode by remember { mutableStateOf(prefs.getBoolean("dark_mode", true)) }

    // GPS & Intervall States
    var showGps by remember { mutableStateOf(prefs.getBoolean("show_gps", false)) }
    var updateInterval by remember { mutableFloatStateOf(prefs.getFloat("gps_interval", 3f)) }
    var gpsCoords by remember { mutableStateOf("Suche Satelliten...") }
    var gpsAccuracy by remember { mutableStateOf("") }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var showAboutDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showColorDialog by remember { mutableStateOf(false) }

    // GPS Update Logik
    LaunchedEffect(showGps, updateInterval) {
        if (showGps) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                val locationRequest = LocationRequest.Builder(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    (updateInterval * 1000).toLong()
                ).build()

                val locationCallback = object : LocationCallback() {
                    override fun onLocationResult(result: LocationResult) {
                        result.lastLocation?.let {
                            gpsCoords = formatWGS84(it.latitude, it.longitude)
                            gpsAccuracy = "+/- ${it.accuracy.roundToInt()} m"
                        }
                    }
                }
                fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
            } else {
                gpsCoords = "Keine Berechtigung/Access Denied"
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) { /* GPS startet automatisch durch den Re-Compose */ }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                imageUriString = uri.toString()
                prefs.edit { putString("image_uri", imageUriString) }
            } catch (e: Exception) { e.printStackTrace() }
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
                    Text("GC Now! Einstellungen", Modifier.padding(16.dp), fontSize = 20.sp, fontWeight = FontWeight.Bold)

                    val menuIconColor = Color(0xFF00FFCC)
                    val menuColors = NavigationDrawerItemDefaults.colors(unselectedIconColor = menuIconColor, selectedIconColor = menuIconColor)

                    NavigationDrawerItem(
                        label = { Text("GPS Anzeige") },
                        selected = false,
                        icon = { Icon(Icons.Default.LocationOn, null) },
                        badge = { Switch(checked = showGps, onCheckedChange = {
                            showGps = it
                            prefs.edit { putBoolean("show_gps", it) }
                            if (it) permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                        }) },
                        colors = menuColors,
                        onClick = {}
                    )

                    if (showGps) {
                        Column(Modifier.padding(horizontal = 28.dp, vertical = 8.dp)) {
                            Text("Intervall: ${updateInterval.toInt()} Sek.", fontSize = 14.sp)
                            Slider(
                                value = updateInterval,
                                onValueChange = { updateInterval = it },
                                onValueChangeFinished = { prefs.edit { putFloat("gps_interval", updateInterval) } },
                                valueRange = 1f..10f,
                                steps = 8,
                                colors = SliderDefaults.colors(thumbColor = Color(0xFF00FFCC), activeTrackColor = Color(0xFF00FFCC))
                            )
                        }
                    }

                    NavigationDrawerItem(
                        label = { Text(if (isDarkMode) "Dunkel-Modus" else "Hell-Modus") },
                        selected = false,
                        icon = { Icon(if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode, null) },
                        badge = { Switch(checked = isDarkMode, onCheckedChange = { isDarkMode = it; prefs.edit { putBoolean("dark_mode", it) } }) },
                        colors = menuColors,
                        onClick = {}
                    )

                    HorizontalDivider(Modifier.padding(16.dp))
                    NavigationDrawerItem(label = { Text("Hintergrundfarbe") }, selected = false, icon = { Icon(Icons.Default.Palette, null) }, colors = menuColors, onClick = { showColorDialog = true })
                    NavigationDrawerItem(label = { Text("Bild ändern") }, selected = false, icon = { Icon(Icons.Default.Photo, null) }, colors = menuColors, onClick = { scope.launch { drawerState.close() }; photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) })
                    NavigationDrawerItem(label = { Text("Text ändern") }, selected = false, icon = { Icon(Icons.Default.Edit, null) }, colors = menuColors, onClick = { scope.launch { drawerState.close() }; showEditDialog = true })
                    NavigationDrawerItem(label = { Text("Über") }, selected = false, icon = { Icon(Icons.Default.Info, null) }, colors = menuColors, onClick = { scope.launch { drawerState.close() }; showAboutDialog = true })
                }
            }
        ) {
            Scaffold(
                topBar = {
                    CenterAlignedTopAppBar(
                        title = { Text("GC Now!", fontWeight = FontWeight.Black, color = if(isDarkMode) Color.White else Color.Black) },
                        navigationIcon = { IconButton(onClick = { scope.launch { drawerState.open() } }) { Icon(Icons.Default.Menu, "Menu", tint = Color(0xFF00FFCC)) } },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                    )
                }
            ) { pv ->
                Surface(Modifier.padding(pv).fillMaxSize(), color = Color(bgColorInt)) {
                    TeamContent(teamText, imageUriString, isDarkMode, showGps, gpsCoords, gpsAccuracy)
                }
            }
        }
    }

    // --- DIALOGE ---
    if (showEditDialog) {
        var tempText by remember { mutableStateOf(teamText) }
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Text ändern") },
            text = { OutlinedTextField(value = tempText, onValueChange = { tempText = it }, label = { Text("Eingabe") }) },
            confirmButton = {
                Button(onClick = { teamText = tempText; prefs.edit { putString("team_name", teamText) }; showEditDialog = false }) { Text("Speichern") }
            }
        )
    }

    if (showColorDialog) {
        val colorOptions = listOf(
            Color(0xFF202020), Color.Black, Color(0xFF1A237E), Color(0xFF004D40),
            Color(0xFF1B5E20), Color(0xFF3E2723), Color(0xFFB71C1C), Color(0xFF4A148C),
            Color(0xFFFFFFFF), Color(0xFFF5F5F5), Color(0xFFE3F2FD), Color(0xFFE8F5E9),
            Color(0xFFFFF9C4), Color(0xFFFFE0B2), Color(0xFFF3E5F5), Color(0xFFD1D1D1)
        )
        AlertDialog(
            onDismissRequest = { showColorDialog = false },
            title = { Text("Farbe wählen") },
            text = {
                Column {
                    colorOptions.chunked(4).forEach { rowColors ->
                        Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), Arrangement.SpaceEvenly) {
                            rowColors.forEach { color ->
                                Box(Modifier.size(45.dp).background(color, CircleShape).clickable {
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

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            confirmButton = { TextButton(onClick = { showAboutDialog = false }) { Text("OK") } },
            title = { Text("About/Über GC Now!") },
            text = {
                Text("Geocaching Tool 2.0\n(C) by ROSE_SWE, Ralph Roth\n\n" +
                        "Build Date: 22.04.2026\n" +
                        "Support: Android 12-16\n" +
                        "Features: GPS Accuracy Meter & Interval Slider.\n"+
                        "\$Id: MainActivity.kt,v 1.9 2026/04/22 14:12:43 ralph Exp $")
            }
        )
    }
}

@Composable
fun TeamContent(displayName: String, imageUri: String?, isDarkMode: Boolean, showGps: Boolean, gpsCoords: String, gpsAccuracy: String) {
    var currentTime by remember { mutableStateOf(LocalDateTime.now()) }
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    LaunchedEffect(Unit) { while (true) { currentTime = LocalDateTime.now(); delay(1000) } }

    val imgMod = Modifier.size(if (isLandscape) 180.dp else 220.dp).padding(8.dp)

    if (isLandscape) {
        Row(Modifier.fillMaxSize().padding(16.dp), Arrangement.Center, Alignment.CenterVertically) {
            DisplayImage(imageUri, imgMod)
            Spacer(Modifier.width(40.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                InfoTexts(displayName, currentTime, isDarkMode, showGps, gpsCoords, gpsAccuracy)
            }
        }
    } else {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            DisplayImage(imageUri, imgMod)
            Spacer(Modifier.height(30.dp))
            InfoTexts(displayName, currentTime, isDarkMode, showGps, gpsCoords, gpsAccuracy)
        }
    }
}

@Composable
fun DisplayImage(imageUri: String?, modifier: Modifier) {
    if (imageUri != null) {
        AsyncImage(model = imageUri.toUri(), contentDescription = null, modifier = modifier, contentScale = ContentScale.Fit)
    } else {
        Image(painter = painterResource(id = R.drawable.user_image), contentDescription = null, modifier = modifier, contentScale = ContentScale.Fit)
    }
}

@Composable
fun InfoTexts(displayName: String, currentTime: LocalDateTime, isDarkMode: Boolean, showGps: Boolean, gpsCoords: String, gpsAccuracy: String) {
    val dynamicFontSize = when {
        displayName.length > 15 -> 28.sp
        displayName.length > 10 -> 36.sp
        else -> 52.sp
    }
    val mainColor = if (isDarkMode) Color.White else Color.Black
    val timeColor = if (isDarkMode) Color.Yellow else Color(0xFFD32F2F)

    Text(text = displayName, fontSize = dynamicFontSize, fontWeight = FontWeight.ExtraBold, color = Color(0xFF00FFCC), textAlign = TextAlign.Center, lineHeight = dynamicFontSize)
    Spacer(Modifier.height(35.dp))
    Text(text = currentTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")), fontSize = 42.sp, fontWeight = FontWeight.Black, color = mainColor)
    Text(text = currentTime.format(DateTimeFormatter.ofPattern("HH:mm:ss")), fontSize = 46.sp, fontWeight = FontWeight.Bold, color = timeColor)

    if (showGps) {
        Spacer(Modifier.height(20.dp))
        Text(text = gpsCoords, fontSize = 24.sp, fontWeight = FontWeight.Medium, color = Color(0xFF00FFCC), textAlign = TextAlign.Center, lineHeight = 30.sp)
        Text(text = gpsAccuracy, fontSize = 18.sp, color = if(isDarkMode) Color.LightGray else Color.Gray)
    }
}

fun formatWGS84(lat: Double, lon: Double): String {
    val latHem = if (lat >= 0) "N" else "S"
    val lonHem = if (lon >= 0) "E" else "W"
    fun conv(c: Double): String {
        val d = Math.abs(c).toInt()
        val m = (Math.abs(c) - d) * 60
        return String.format(Locale.US, "%02d° %06.3f'", d, m)
    }
    return "$latHem ${conv(lat)}\n$lonHem ${conv(lon)}"
}