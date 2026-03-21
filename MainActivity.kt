// @(#) $Id: MainActivity.kt,v 1.5 2026/03/21 20:44:42 ralph Exp $
// $Header: /home/cvs/src/android/GCNow/MainActivity.kt,v 1.5 2026/03/21 20:44:42 ralph Exp $

package com.example.rose_swe

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import androidx.core.content.edit
import androidx.core.net.toUri
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { MainScreen() }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val context = LocalContext.current
    // Hinweis: Falls du rose_prefs statt team_prefs nutzen willst, hier ändern:
    val prefs = remember { context.getSharedPreferences("team_prefs", Context.MODE_PRIVATE) }

    var teamText by remember { mutableStateOf(prefs.getString("team_name", "rose_swe") ?: "rose_swe") }
    var imageUriString by remember { mutableStateOf(prefs.getString("image_uri", null)) }
    var bgColorInt by remember { mutableIntStateOf(prefs.getInt("bg_color", Color(0xFF202020).toArgb())) }
    var isDarkMode by remember { mutableStateOf(prefs.getBoolean("dark_mode", true)) }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var showAboutDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showColorDialog by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                imageUriString = uri.toString()
                prefs.edit { putString("image_uri", imageUriString) }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    val colorScheme = if (isDarkMode) {
        darkColorScheme(primary = Color(0xFF00FFCC), background = Color(bgColorInt), surface = Color(bgColorInt))
    } else {
        lightColorScheme(primary = Color(0xFF007A66), background = Color(bgColorInt), surface = Color(bgColorInt))
    }

    MaterialTheme(colorScheme = colorScheme) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet(drawerContainerColor = if (isDarkMode) Color(0xFF1A1A1A) else Color(0xFFF0F0F0)) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("GC Now! Menü", modifier = Modifier.padding(16.dp), fontSize = 22.sp, fontWeight = FontWeight.Bold)

                    val menuIconColor = Color(0xFF00FFCC)
                    val menuColors = NavigationDrawerItemDefaults.colors(
                        unselectedIconColor = menuIconColor,
                        selectedIconColor = menuIconColor,
                        unselectedTextColor = if(isDarkMode) Color.White else Color.Black
                    )

                    NavigationDrawerItem(
                        label = { Text(if (isDarkMode) "Dunkel-Modus" else "Hell-Modus") },
                        selected = false,
                        icon = { Icon(if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode, null) },
                        badge = { Switch(checked = isDarkMode, onCheckedChange = {
                            isDarkMode = it
                            prefs.edit { putBoolean("dark_mode", it) }
                        }) },
                        colors = menuColors,
                        onClick = {}
                    )

                    HorizontalDivider(Modifier.padding(16.dp))

                    NavigationDrawerItem(
                        label = { Text("Bild ändern") },
                        selected = false,
                        icon = { Icon(Icons.Default.Photo, null) },
                        colors = menuColors,
                        onClick = { scope.launch { drawerState.close() }; photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
                    )
                    NavigationDrawerItem(
                        label = { Text("Text ändern") },
                        selected = false,
                        icon = { Icon(Icons.Default.Edit, null) },
                        colors = menuColors,
                        onClick = { scope.launch { drawerState.close() }; showEditDialog = true }
                    )
                    NavigationDrawerItem(
                        label = { Text("Hintergrundfarbe") },
                        selected = false,
                        icon = { Icon(Icons.Default.Palette, null) },
                        colors = menuColors,
                        onClick = { scope.launch { drawerState.close() }; showColorDialog = true }
                    )
                    NavigationDrawerItem(
                        label = { Text("Über") },
                        selected = false,
                        icon = { Icon(Icons.Default.Info, null) },
                        colors = menuColors,
                        onClick = { scope.launch { drawerState.close() }; showAboutDialog = true }
                    )
                }
            }
        ) {
            Scaffold(
                topBar = {
                    CenterAlignedTopAppBar(
                        title = { Text("GC Now!", fontWeight = FontWeight.Black, color = if(isDarkMode) Color.White else Color.Black) },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Menu, "Menu", tint = Color(0xFF00FFCC))
                            }
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                    )
                }
            ) { paddingValues ->
                Surface(
                    modifier = Modifier.padding(paddingValues).fillMaxSize(),
                    color = Color(bgColorInt)
                ) {
                    TeamContent(teamText, imageUriString, isDarkMode)
                }
            }
        }
    }

    if (showEditDialog) {
        var tempText by remember { mutableStateOf(teamText) }
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Text ändern") },
            text = { OutlinedTextField(value = tempText, onValueChange = { tempText = it }, label = { Text("Eingabe") }) },
            confirmButton = {
                Button(onClick = {
                    teamText = tempText
                    prefs.edit { putString("team_name", teamText) }
                    showEditDialog = false
                }) { Text("Speichern") }
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
            title = { Text("Hintergrund wählen") },
            text = {
                Column {
                    colorOptions.chunked(4).forEach { rowColors ->
                        Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                            rowColors.forEach { color ->
                                Box(
                                    modifier = Modifier
                                        .size(50.dp)
                                        .background(color, CircleShape)
                                        // MOD: Kontur etwas dunkler für bessere Sichtbarkeit
                                        .border(1.dp, Color.Gray.copy(alpha = 0.5f), CircleShape)
                                        .clickable {
                                            bgColorInt = color.toArgb()
                                            prefs.edit { putInt("bg_color", bgColorInt) }
                                            showColorDialog = false
                                        }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showColorDialog = false }) { Text("Schließen") } }
        )
    }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            confirmButton = { TextButton(onClick = { showAboutDialog = false }) { Text("OK") } },
            title = { Text("Über GC Now!") },
            text = {
                Text("Geocaching Tool\n(C) by ROSE_SWE, Ralph Roth\n\n" +
                        "Build Date: 21.03.2026\n" +
                        "Support: Android 12-16\n" +
                        "Features: Dark Mode, Persistence, Adaptive Layout.\n\$Id: MainActivity.kt,v 1.5 2026/03/21 20:44:42 ralph Exp $")
            }
        )
    }
}

@Composable
fun TeamContent(displayName: String, imageUri: String?, isDarkMode: Boolean) {
    var currentTime by remember { mutableStateOf(LocalDateTime.now()) }
    val config = LocalConfiguration.current
    val isLandscape = config.orientation == Configuration.ORIENTATION_LANDSCAPE

    LaunchedEffect(Unit) {
        while (true) { currentTime = LocalDateTime.now(); delay(1000) }
    }

    val imgMod = Modifier.size(if (isLandscape) 180.dp else 220.dp).padding(8.dp)

    if (isLandscape) {
        Row(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            DisplayImage(imageUri, imgMod)
            Spacer(Modifier.width(40.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) { InfoTexts(displayName, currentTime, isDarkMode) }
        }
    } else {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            DisplayImage(imageUri, imgMod)
            Spacer(Modifier.height(30.dp))
            InfoTexts(displayName, currentTime, isDarkMode)
        }
    }
}

@Composable
fun DisplayImage(imageUri: String?, modifier: Modifier) {
    if (imageUri != null) {
        AsyncImage(
            model = imageUri.toUri(),
            contentDescription = null,
            modifier = modifier,
            contentScale = ContentScale.Fit
        )
    } else {
        Image(painter = painterResource(id = R.drawable.user_image), contentDescription = null, modifier = modifier, contentScale = ContentScale.Fit)
    }
}

@Composable
fun InfoTexts(displayName: String, currentTime: LocalDateTime, isDarkMode: Boolean) {
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
    Spacer(Modifier.height(12.dp))
    Text(text = currentTime.format(DateTimeFormatter.ofPattern("HH:mm:ss")), fontSize = 46.sp, fontWeight = FontWeight.Bold, color = timeColor)
}