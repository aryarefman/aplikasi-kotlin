package com.example.mykotlinapp

import android.content.res.Configuration
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.*
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mykotlinapp.ui.theme.MyKotlinAppTheme
import kotlinx.coroutines.delay
import kotlin.math.PI

import com.example.mykotlinapp.CircleAreaCalculatorScreen
import com.example.mykotlinapp.EllipseAreaCalculatorScreen
import com.example.mykotlinapp.KiteAreaCalculatorScreen
import com.example.mykotlinapp.ParallelogramAreaCalculatorScreen
import com.example.mykotlinapp.RectangleAreaCalculatorScreen
import com.example.mykotlinapp.RhombusAreaCalculatorScreen
import com.example.mykotlinapp.SquareAreaCalculatorScreen
import com.example.mykotlinapp.TrapezoidAreaCalculatorScreen
import com.example.mykotlinapp.TriangleAreaCalculatorScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyKotlinAppTheme {
                val navController = rememberNavController()
                AppNavHost(navController)
            }
        }
    }
}

@Composable
fun AppNavHost(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "loading") {
        composable("loading") {
            LoadingScreen(navController)
        }
        composable("home") {
            HomeScreen(navController)
        }
        composable("square") { SquareAreaCalculatorScreen(navController) }
        composable("rectangle") { RectangleAreaCalculatorScreen(navController) }
        composable("triangle") { TriangleAreaCalculatorScreen(navController) }
        composable("parallelogram") { ParallelogramAreaCalculatorScreen(navController) }
        composable("trapezoid") { TrapezoidAreaCalculatorScreen(navController) }
        composable("kite") { KiteAreaCalculatorScreen(navController) }
        composable("rhombus") { RhombusAreaCalculatorScreen(navController) }
        composable("ellipse") { EllipseAreaCalculatorScreen(navController) }
        composable("circle") { CircleAreaCalculatorScreen(navController) }
    }
}

@Composable
fun LoadingScreen(navController: NavHostController) {
    var isVisible by remember { mutableStateOf(false) }
    val infiniteTransition = rememberInfiniteTransition(label = "loadingAnimation")

    val animatedRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "loadingRotation"
    )

    val animatedOpacity by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "textOpacity"
    )

    val animatedScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    LaunchedEffect(Unit) {
        isVisible = true
        delay(1500)
        navController.navigate("home") {
            popUpTo("loading") { inclusive = true }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFF8FAFC),
                        Color(0xFFF1F5F9),
                        Color(0xFFE2E8F0)
                    ),
                    radius = 1000f
                )
            )
            .semantics {
                contentDescription = "Aplikasi sedang memuat, mohon tunggu sebentar"
            },
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(animationSpec = tween(800)) + scaleIn(
                animationSpec = tween(800, easing = EaseOutBack)
            )
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Box(
                    modifier = Modifier.scale(animatedScale),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(
                        modifier = Modifier.size(160.dp)
                    ) {
                        val center = Offset(size.width / 2, size.height / 2)
                        val radius = size.minDimension * 0.32f
                        val shapeSize = 24.dp.toPx()

                        val shapes = listOf(
                            Triple(0f, Color(0xFFE91E63), "square"),
                            Triple(2 * PI.toFloat() / 3, Color(0xFF2196F3), "circle"),
                            Triple(4 * PI.toFloat() / 3, Color(0xFF4CAF50), "triangle")
                        )

                        shapes.forEach { (baseAngle, color, type) ->
                            val angle = (animatedRotation / 360) * 2 * PI.toFloat() + baseAngle
                            val x = center.x + radius * kotlin.math.cos(angle.toDouble()).toFloat()
                            val y = center.y + radius * kotlin.math.sin(angle.toDouble()).toFloat()

                            when (type) {
                                "square" -> {
                                    drawRect(
                                        color = Color.Black.copy(alpha = 0.1f),
                                        topLeft = Offset(x - shapeSize / 2 + 2, y - shapeSize / 2 + 2),
                                        size = Size(shapeSize, shapeSize)
                                    )
                                    drawRect(
                                        color = color,
                                        topLeft = Offset(x - shapeSize / 2, y - shapeSize / 2),
                                        size = Size(shapeSize, shapeSize)
                                    )
                                }
                                "circle" -> {
                                    drawCircle(
                                        color = Color.Black.copy(alpha = 0.1f),
                                        radius = shapeSize / 2,
                                        center = Offset(x + 1, y + 1)
                                    )
                                    drawCircle(
                                        color = color,
                                        radius = shapeSize / 2,
                                        center = Offset(x, y)
                                    )
                                }
                                "triangle" -> {
                                    val shadowPath = androidx.compose.ui.graphics.Path().apply {
                                        moveTo(x + 1, y - shapeSize / 2 + 1)
                                        lineTo(x + shapeSize / 2 + 1, y + shapeSize / 2 + 1)
                                        lineTo(x - shapeSize / 2 + 1, y + shapeSize / 2 + 1)
                                        close()
                                    }
                                    drawPath(path = shadowPath, color = Color.Black.copy(alpha = 0.1f))

                                    val path = androidx.compose.ui.graphics.Path().apply {
                                        moveTo(x, y - shapeSize / 2)
                                        lineTo(x + shapeSize / 2, y + shapeSize / 2)
                                        lineTo(x - shapeSize / 2, y + shapeSize / 2)
                                        close()
                                    }
                                    drawPath(path = path, color = color)
                                }
                            }
                        }

                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF64748B).copy(alpha = 0.3f),
                                    Color(0xFF475569).copy(alpha = 0.1f)
                                )
                            ),
                            radius = radius + 8.dp.toPx(),
                            center = center,
                            style = Stroke(width = 1.5.dp.toPx())
                        )
                    }
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Kalkulator Geometri",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Memuat aplikasi...",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF475569).copy(alpha = animatedOpacity),
                        textAlign = TextAlign.Center
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    repeat(3) { index ->
                        val dotDelay = index * 200
                        val dotScale by infiniteTransition.animateFloat(
                            initialValue = 0.3f,
                            targetValue = 1f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(600, delayMillis = dotDelay),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "dot$index"
                        )

                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .scale(dotScale)
                                .background(
                                    Color(0xFF64748B),
                                    CircleShape
                                )
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavHostController) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val isTablet = configuration.screenWidthDp >= 600

    var isVisible by remember { mutableStateOf(false) }

    val shapes = listOf(
        EnhancedShapeData(
            name = "Persegi",
            route = "square",
            primaryColor = Color(0xFFE91E63),
            secondaryColor = Color(0xFFFCE4EC),
            formula = "s²"
        ),
        EnhancedShapeData(
            name = "Persegi Panjang",
            route = "rectangle",
            primaryColor = Color(0xFF2196F3),
            secondaryColor = Color(0xFFE3F2FD),
            formula = "p × l"
        ),
        EnhancedShapeData(
            name = "Segitiga",
            route = "triangle",
            primaryColor = Color(0xFF4CAF50),
            secondaryColor = Color(0xFFE8F5E9),
            formula = "½ × a × t"
        ),
        EnhancedShapeData(
            name = "Jajar Genjang",
            route = "parallelogram",
            primaryColor = Color(0xFF9C27B0),
            secondaryColor = Color(0xFFF3E5F5),
            formula = "a × t"
        ),
        EnhancedShapeData(
            name = "Trapesium",
            route = "trapezoid",
            primaryColor = Color(0xFFFF9800),
            secondaryColor = Color(0xFFFFF3E0),
            formula = "½ × (a+b) × t"
        ),
        EnhancedShapeData(
            name = "Layang-Layang",
            route = "kite",
            primaryColor = Color(0xFFFBC02D),
            secondaryColor = Color(0xFFFFF9C4),
            formula = "½ × d₁ × d₂"
        ),
        EnhancedShapeData(
            name = "Belah Ketupat",
            route = "rhombus",
            primaryColor = Color(0xFF009688),
            secondaryColor = Color(0xFFE0F7FA),
            formula = "½ × d₁ × d₂"
        ),
        EnhancedShapeData(
            name = "Elips",
            route = "ellipse",
            primaryColor = Color(0xFF607D8B),
            secondaryColor = Color(0xFFECEFF1),
            formula = "π × a × b"
        ),
        EnhancedShapeData(
            name = "Lingkaran",
            route = "circle",
            primaryColor = Color(0xFF3F51B5),
            secondaryColor = Color(0xFFE8EAF6),
            formula = "π × r²"
        )
    )

    LaunchedEffect(Unit) {
        delay(100)
        isVisible = true
    }

    Box(modifier = Modifier.fillMaxSize()) {
        EnhancedBackground()

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                "Kalkulator Area Geometri",
                                fontWeight = FontWeight.Bold,
                                fontSize = if (isTablet) 26.sp else 22.sp,
                                color = Color(0xFF1E293B)
                            )
                            Text(
                                "Pilih bentuk untuk menghitung luas",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF64748B),
                                fontSize = if (isTablet) 16.sp else 14.sp
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    ),
                    modifier = Modifier.semantics {
                        contentDescription = "Halaman utama kalkulator geometri"
                    }
                )
            }
        ) { innerPadding ->
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(animationSpec = tween(600)) + slideInVertically(
                    animationSpec = tween(600, easing = EaseOutCubic)
                )
            ) {
                LazyVerticalGrid(
                    columns = when {
                        isTablet -> GridCells.Adaptive(minSize = 200.dp)
                        isLandscape -> GridCells.Fixed(3)
                        else -> GridCells.Fixed(2)
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentPadding = PaddingValues(
                        horizontal = if (isTablet) 32.dp else 20.dp,
                        vertical = 16.dp
                    ),
                    horizontalArrangement = Arrangement.spacedBy(if (isTablet) 20.dp else 16.dp),
                    verticalArrangement = Arrangement.spacedBy(if (isTablet) 20.dp else 16.dp)
                ) {
                    items(shapes) { shape ->
                        EnhancedShapeCard(
                            shape = shape,
                            isTablet = isTablet,
                            onClick = { navController.navigate(shape.route) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EnhancedBackground() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF8FAFC),
                        Color(0xFFF1F5F9),
                        Color(0xFFE2E8F0)
                    )
                )
            )
    ) {
        EnhancedPatterns()
    }
}

@Composable
fun EnhancedPatterns() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val patternElements = listOf(
            PatternElement(Offset(100f, 150f), 80f, Color(0x08E91E63), "circle"),
            PatternElement(Offset(350f, 100f), 60f, Color(0x082196F3), "square"),
            PatternElement(Offset(200f, 300f), 70f, Color(0x084CAF50), "triangle"),
            PatternElement(Offset(300f, 250f), 50f, Color(0x089C27B0), "circle"),
            PatternElement(Offset(150f, 450f), 65f, Color(0x08FF9800), "square"),
            PatternElement(Offset(400f, 350f), 45f, Color(0x083F51B5), "triangle"),
            PatternElement(Offset(80f, 600f), 55f, Color(0x08009688), "circle"),
            PatternElement(Offset(380f, 550f), 40f, Color(0x08607D8B), "square")
        )

        patternElements.forEach { element ->
            when (element.type) {
                "circle" -> drawCircle(
                    color = element.color,
                    radius = element.size,
                    center = element.position
                )
                "square" -> drawRect(
                    color = element.color,
                    topLeft = Offset(
                        element.position.x - element.size / 2,
                        element.position.y - element.size / 2
                    ),
                    size = Size(element.size, element.size)
                )
                "triangle" -> {
                    val path = androidx.compose.ui.graphics.Path().apply {
                        moveTo(element.position.x, element.position.y - element.size / 2)
                        lineTo(element.position.x + element.size / 2, element.position.y + element.size / 2)
                        lineTo(element.position.x - element.size / 2, element.position.y + element.size / 2)
                        close()
                    }
                    drawPath(path = path, color = element.color)
                }
            }
        }

        val gridSpacing = 120f
        val gridAlpha = 0.03f

        for (x in 0 until (size.width / gridSpacing).toInt()) {
            drawLine(
                color = Color(0xFF64748B).copy(alpha = gridAlpha),
                start = Offset(x * gridSpacing, 0f),
                end = Offset(x * gridSpacing, size.height),
                strokeWidth = 0.5.dp.toPx()
            )
        }

        for (y in 0 until (size.height / gridSpacing).toInt()) {
            drawLine(
                color = Color(0xFF64748B).copy(alpha = gridAlpha),
                start = Offset(0f, y * gridSpacing),
                end = Offset(size.width, y * gridSpacing),
                strokeWidth = 0.5.dp.toPx()
            )
        }
    }
}

data class PatternElement(
    val position: Offset,
    val size: Float,
    val color: Color,
    val type: String
)

data class EnhancedShapeData(
    val name: String,
    val route: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val formula: String
)

@Composable
fun EnhancedShapeCard(
    shape: EnhancedShapeData,
    isTablet: Boolean = false,
    onClick: () -> Unit
) {
    var isHovered by remember { mutableStateOf(false) }
    var startAnim by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current

    val animatedScale by animateFloatAsState(
        targetValue = if (startAnim) 1f else 0f,
        animationSpec = tween(800, easing = EaseOutBack),
        label = "cardAnimation"
    )

    val hoverScale by animateFloatAsState(
        targetValue = if (isHovered) 1.02f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "hoverScale"
    )

    val cardElevation by animateIntAsState(
        targetValue = if (isHovered) 8 else 2,
        animationSpec = tween(200),
        label = "cardElevation"
    )

    LaunchedEffect(Unit) {
        delay(100)
        startAnim = true
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .scale(hoverScale)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(
                    bounded = true,
                    radius = if (isTablet) 120.dp else 100.dp,
                    color = shape.primaryColor
                )
            ) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            }
            .semantics {
                contentDescription = "Kalkulator ${shape.name}. Formula: ${shape.formula}. Ketuk untuk membuka kalkulator."
            },
        elevation = CardDefaults.cardElevation(defaultElevation = cardElevation.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(if (isTablet) 16.dp else 12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (isTablet) 24.dp else 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(if (isTablet) 20.dp else 12.dp)
            ) {

                Box(
                    modifier = Modifier
                        .size(if (isTablet) 80.dp else 64.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    shape.secondaryColor,
                                    shape.secondaryColor.copy(alpha = 0.7f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    when (shape.route) {
                        "square" -> EnhancedSquareIcon(shape.primaryColor, animatedScale, isTablet)
                        "rectangle" -> EnhancedRectangleIcon(shape.primaryColor, animatedScale, isTablet)
                        "triangle" -> EnhancedTriangleIcon(shape.primaryColor, animatedScale, isTablet)
                        "parallelogram" -> EnhancedParallelogramIcon(shape.primaryColor, animatedScale, isTablet)
                        "trapezoid" -> EnhancedTrapezoidIcon(shape.primaryColor, animatedScale, isTablet)
                        "kite" -> EnhancedKiteIcon(shape.primaryColor, animatedScale, isTablet)
                        "rhombus" -> EnhancedRhombusIcon(shape.primaryColor, animatedScale, isTablet)
                        "ellipse" -> EnhancedEllipseIcon(shape.primaryColor, animatedScale, isTablet)
                        "circle" -> EnhancedCircleIcon(shape.primaryColor, animatedScale, isTablet)
                    }
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = shape.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B),
                        fontSize = if (isTablet) 18.sp else 16.sp,
                        textAlign = TextAlign.Center
                    )

                    Box(
                        modifier = Modifier
                            .background(
                                shape.primaryColor.copy(alpha = 0.1f),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = shape.formula,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = shape.primaryColor,
                            fontSize = if (isTablet) 18.sp else 16.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EnhancedSquareIcon(color: Color, scale: Float, isTablet: Boolean = false) {
    val size = if (isTablet) 36.dp else 28.dp
    Canvas(modifier = Modifier.size(size)) {
        val strokeWidth = if (isTablet) 3.dp.toPx() else 2.5.dp.toPx()
        drawRect(
            color = color,
            size = Size(size.toPx() * 0.7f * scale, size.toPx() * 0.7f * scale),
            topLeft = Offset(size.toPx() * 0.15f, size.toPx() * 0.15f),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    }
}

@Composable
fun EnhancedRectangleIcon(color: Color, scale: Float, isTablet: Boolean = false) {
    val size = if (isTablet) 36.dp else 28.dp
    Canvas(modifier = Modifier.size(size)) {
        val strokeWidth = if (isTablet) 3.dp.toPx() else 2.5.dp.toPx()
        drawRect(
            color = color,
            size = Size(size.toPx() * 0.8f * scale, size.toPx() * 0.5f * scale),
            topLeft = Offset(size.toPx() * 0.1f, size.toPx() * 0.25f),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    }
}

@Composable
fun EnhancedTriangleIcon(color: Color, scale: Float, isTablet: Boolean = false) {
    val size = if (isTablet) 36.dp else 28.dp
    Canvas(modifier = Modifier.size(size)) {
        val strokeWidth = if (isTablet) 3.dp.toPx() else 2.5.dp.toPx()
        val path = androidx.compose.ui.graphics.Path().apply {
            moveTo(size.toPx() * 0.5f, size.toPx() * 0.15f)
            lineTo(size.toPx() * 0.85f * scale, size.toPx() * 0.8f * scale)
            lineTo(size.toPx() * 0.15f * scale, size.toPx() * 0.8f * scale)
            close()
        }
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    }
}

@Composable
fun EnhancedParallelogramIcon(color: Color, scale: Float, isTablet: Boolean = false) {
    val size = if (isTablet) 36.dp else 28.dp
    Canvas(modifier = Modifier.size(size)) {
        val strokeWidth = if (isTablet) 3.dp.toPx() else 2.5.dp.toPx()
        val path = androidx.compose.ui.graphics.Path().apply {
            moveTo(size.toPx() * 0.15f, size.toPx() * 0.15f)
            lineTo(size.toPx() * (0.15f + 0.7f * scale), size.toPx() * 0.15f)
            lineTo(size.toPx() * (0.25f + 0.7f * scale), size.toPx() * 0.85f)
            lineTo(size.toPx() * 0.25f, size.toPx() * 0.85f)
            close()
        }
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    }
}

@Composable
fun EnhancedTrapezoidIcon(color: Color, scale: Float, isTablet: Boolean = false) {
    val size = if (isTablet) 36.dp else 28.dp
    Canvas(modifier = Modifier.size(size)) {
        val strokeWidth = if (isTablet) 3.dp.toPx() else 2.5.dp.toPx()
        val path = androidx.compose.ui.graphics.Path().apply {
            moveTo(size.toPx() * 0.3f, size.toPx() * 0.2f)
            lineTo(size.toPx() * 0.7f, size.toPx() * 0.2f)
            lineTo(size.toPx() * 0.85f * scale, size.toPx() * 0.8f)
            lineTo(size.toPx() * 0.15f * scale, size.toPx() * 0.8f)
            close()
        }
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    }
}

@Composable
fun EnhancedKiteIcon(color: Color, scale: Float, isTablet: Boolean = false) {
    val size = if (isTablet) 36.dp else 28.dp
    Canvas(modifier = Modifier.size(size)) {
        val strokeWidth = if (isTablet) 3.dp.toPx() else 2.5.dp.toPx()
        val path = androidx.compose.ui.graphics.Path().apply {
            moveTo(size.toPx() * 0.5f, size.toPx() * 0.15f)
            lineTo(size.toPx() * (0.5f + 0.35f * scale), size.toPx() * 0.45f)
            lineTo(size.toPx() * 0.5f, size.toPx() * 0.85f * scale)
            lineTo(size.toPx() * (0.5f - 0.35f * scale), size.toPx() * 0.45f)
            close()
        }
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    }
}

@Composable
fun EnhancedRhombusIcon(color: Color, scale: Float, isTablet: Boolean = false) {
    val size = if (isTablet) 36.dp else 28.dp
    Canvas(modifier = Modifier.size(size)) {
        val strokeWidth = if (isTablet) 3.dp.toPx() else 2.5.dp.toPx()
        val path = androidx.compose.ui.graphics.Path().apply {
            moveTo(size.toPx() * 0.5f, size.toPx() * 0.15f)
            lineTo(size.toPx() * (0.5f + 0.35f * scale), size.toPx() * 0.5f)
            lineTo(size.toPx() * 0.5f, size.toPx() * 0.85f * scale)
            lineTo(size.toPx() * (0.5f - 0.35f * scale), size.toPx() * 0.5f)
            close()
        }
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    }
}

@Composable
fun EnhancedEllipseIcon(color: Color, scale: Float, isTablet: Boolean = false) {
    val size = if (isTablet) 36.dp else 28.dp
    Canvas(modifier = Modifier.size(size)) {
        val strokeWidth = if (isTablet) 3.dp.toPx() else 2.5.dp.toPx()
        drawOval(
            color = color,
            topLeft = Offset(size.toPx() * 0.05f, size.toPx() * 0.2f),
            size = Size(size.toPx() * 0.9f * scale, size.toPx() * 0.6f * scale),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    }
}

@Composable
fun EnhancedCircleIcon(color: Color, scale: Float, isTablet: Boolean = false) {
    val size = if (isTablet) 36.dp else 28.dp
    Canvas(modifier = Modifier.size(size)) {
        val strokeWidth = if (isTablet) 3.dp.toPx() else 2.5.dp.toPx()
        drawCircle(
            color = color,
            radius = size.toPx() * 0.35f * scale,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    }
}