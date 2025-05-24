package com.example.new1

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.alpha
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.core.*
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.graphicsLayer

class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = androidx.compose.material3.darkColorScheme(
                    background = Color.Black,
                    onBackground = Color.White
                )
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SplashScreen()
                }
            }
        }
        // Navigate to MainActivity after delay
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, 2000) // 2 seconds delay
    }
}

@Composable
fun SplashScreen() {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    
    // Subtle scale animation
    val logoScale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.95f,
        animationSpec = tween(
            durationMillis = 800,
            easing = EaseOutCubic
        ),
        label = ""
    )
    
    // Smooth fade-in animation
    val logoAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(
            durationMillis = 600,
            easing = EaseInOut
        ),
        label = ""
    )

    // Subtle background gradient
    val gradient = Brush.verticalGradient(
        colors = listOf(
            Color.Black,
            Color(0xFF1A1A1A)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = gradient),
        contentAlignment = Alignment.Center
    ) {
        // Centered logo with subtle animation
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(durationMillis = 600))
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo_wolf),
                contentDescription = "App Logo",
                modifier = Modifier
                    .size(160.dp)
                    .scale(logoScale)
                    .alpha(logoAlpha)
                    .shadow(
                        elevation = 8.dp,
                        shape = CircleShape,
                        ambientColor = Color.White.copy(alpha = 0.1f)
                    )
            )
        }

        // Designer credits with elegant fade
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(end = 24.dp, bottom = 24.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(tween(durationMillis = 800, delayMillis = 400))
                ) {
                    Text(
                        text = "Designed by Production for Production",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Light,
                        letterSpacing = 0.5.sp
                    )
                }
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(tween(durationMillis = 800, delayMillis = 500))
                ) {
                    Text(
                        text = "— Nashid",
                        color = Color.White.copy(alpha = 0.3f),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Light,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SplashScreenPreview() {
    SplashScreen()
} 