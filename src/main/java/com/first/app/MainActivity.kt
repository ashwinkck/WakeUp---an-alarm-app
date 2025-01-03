package com.first.app

import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.first.app.ui.theme.AppfirstTheme
import com.first.app.alarm.AlarmManagerUtil
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppfirstTheme {
                HomeScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    var selectedTab by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        delay(1500)
        isLoading = false
    }

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { 
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "WakeUp!", 
                            color = Color.White,
                            style = MaterialTheme.typography.headlineLarge
                        ) 
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = Color.Black,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.Black
            ) {
                listOf("Clock", "Alarm").forEachIndexed { index, title ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                when (index) {
                                    0 -> Icons.Default.Home
                                    else -> Icons.Default.Person
                                },
                                contentDescription = title
                            )
                        },
                        label = { Text(title) },
                        selected = selectedTab == index,
                        onClick = {
                            selectedTab = index
                            scope.launch {
                                isLoading = true
                                delay(500)
                                isLoading = false
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            unselectedIconColor = Color.Gray,
                            selectedTextColor = Color.White,
                            unselectedTextColor = Color.Gray,
                            indicatorColor = Color.DarkGray
                        )
                    )
                }
            }
        },
        containerColor = Color.Black
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.Black)
        ) {
            AnimatedVisibility(
                visible = isLoading,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                LoadingScreen()
            }

            AnimatedVisibility(
                visible = !isLoading,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                ContentScreen(selectedTab)
            }
        }
    }
}

@Composable
fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(64.dp),
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun ContentScreen(tab: Int) {
    when (tab) {
        0 -> HomeContent()
        1 -> AlarmContent()
    }
}

@Composable
fun AlarmContent() {
    val context = LocalContext.current
    val alarmManager = remember { AlarmManagerUtil(context) }
    var selectedTime by remember { mutableStateOf("No alarm set") }
    val calendar = Calendar.getInstance()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Set Alarm",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White
        )

        Text(
            text = selectedTime,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White
        )

        Button(
            onClick = {
                TimePickerDialog(
                    context,
                    { _, hour, minute ->
                        calendar.set(Calendar.HOUR_OF_DAY, hour)
                        calendar.set(Calendar.MINUTE, minute)
                        
                        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                        selectedTime = "Alarm set for: ${timeFormat.format(calendar.time)}"
                        
                        alarmManager.setAlarm(hour, minute, 1)
                        Toast.makeText(context, "Alarm set!", Toast.LENGTH_SHORT).show()
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true
                ).show()
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.DarkGray,
                contentColor = Color.White
            )
        ) {
            Text("Set Alarm")
        }

        Button(
            onClick = {
                alarmManager.cancelAlarm(1)
                selectedTime = "No alarm set"
                Toast.makeText(context, "Alarm cancelled!", Toast.LENGTH_SHORT).show()
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Red,
                contentColor = Color.White
            )
        ) {
            Text("Cancel Alarm")
        }
    }
}

@Composable
fun HomeContent() {
    val currentTime = remember { mutableStateOf(Calendar.getInstance()) }
    
    // Update time every second
    LaunchedEffect(Unit) {
        while(true) {
            currentTime.value = Calendar.getInstance()
            delay(1000)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Clock(currentTime.value)
    }
}

@Composable
fun Clock(calendar: Calendar) {
    val hours = calendar.get(Calendar.HOUR)
    val minutes = calendar.get(Calendar.MINUTE)
    val seconds = calendar.get(Calendar.SECOND)

    Canvas(
        modifier = Modifier
            .size(300.dp)
            .padding(16.dp)
    ) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = size.width / 2f - 20f

        // Draw clock circle
        drawCircle(
            color = Color.White,
            radius = radius,
            center = center,
            style = Stroke(width = 2f)
        )

        // Draw hour markers
        for (i in 0..11) {
            val angle = (i * 30f) * (PI / 180f)
            val markerLength = if (i % 3 == 0) 20f else 10f
            val startRadius = radius - markerLength
            val startX = center.x + cos(angle).toFloat() * radius
            val startY = center.y + sin(angle).toFloat() * radius
            val endX = center.x + cos(angle).toFloat() * startRadius
            val endY = center.y + sin(angle).toFloat() * startRadius

            drawLine(
                color = Color.White,
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = if (i % 3 == 0) 2f else 1f
            )
        }

        // Hour hand
        rotate(degrees = (hours * 30f + minutes / 2f)) {
            drawLine(
                color = Color.White,
                start = center,
                end = Offset(center.x, center.y - radius * 0.5f),
                strokeWidth = 3f
            )
        }

        // Minute hand
        rotate(degrees = minutes * 6f) {
            drawLine(
                color = Color.White,
                start = center,
                end = Offset(center.x, center.y - radius * 0.7f),
                strokeWidth = 2f
            )
        }

        // Second hand
        rotate(degrees = seconds * 6f) {
            drawLine(
                color = Color(0xFFFF4444),
                start = center,
                end = Offset(center.x, center.y - radius * 0.8f),
                strokeWidth = 1f
            )
        }

        // Center dot
        drawCircle(
            color = Color.White,
            radius = 4f,
            center = center
        )
    }
}