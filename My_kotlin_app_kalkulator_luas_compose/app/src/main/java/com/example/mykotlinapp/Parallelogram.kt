package com.example.mykotlinapp

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import java.text.DecimalFormat
import kotlinx.coroutines.delay

data class HasilJajarGenjang(
    val alas: Double,
    val tinggi: Double,
    val luas: Double
)

enum class ParallelogramUnit(val symbol: String, val displayName: String, val toMeter: Double) {
    MM("mm", "Milimeter", 0.001),
    CM("cm", "Sentimeter", 0.01),
    M("m", "Meter", 1.0),
    KM("km", "Kilometer", 1000.0),
    INCH("in", "Inch", 0.0254),
    FEET("ft", "Feet", 0.3048)
}

object ParallelogramCalculator {
    fun calculateParallelogramProperties(base: String, height: String): Pair<HasilJajarGenjang?, ValidationResult> {
        return try {
            when {
                base.trim().isEmpty() || height.trim().isEmpty() -> null to ValidationResult.Error("Alas atau tinggi tidak boleh kosong")
                else -> {
                    val alas = base.trim().replace(",", ".").toDoubleOrNull()
                    val tinggi = height.trim().replace(",", ".").toDoubleOrNull()
                    when {
                        alas == null || tinggi == null -> null to ValidationResult.Error("Masukkan angka yang valid")
                        alas <= 0 || tinggi <= 0 -> null to ValidationResult.Error("Alas dan tinggi harus lebih besar dari 0")
                        alas > 1_000_000 || tinggi > 1_000_000 -> null to ValidationResult.Error("Nilai terlalu besar (maksimal 1,000,000)")
                        alas < 0.0001 || tinggi < 0.0001 -> null to ValidationResult.Error("Nilai terlalu kecil (minimal 0.0001)")
                        else -> {
                            val luas = alas * tinggi
                            HasilJajarGenjang(alas, tinggi, luas) to ValidationResult.Success
                        }
                    }
                }
            }
        } catch (e: Exception) {
            null to ValidationResult.Error("Terjadi kesalahan dalam perhitungan")
        }
    }

    fun formatNumber(value: Double, precision: Int = 4): String {
        val formatter = DecimalFormat().apply {
            maximumFractionDigits = precision
            minimumFractionDigits = 0
            isGroupingUsed = true
        }
        return formatter.format(value)
    }

    fun convertValue(valueInBaseUnit: Double, fromUnit: ParallelogramUnit, toUnit: ParallelogramUnit): Double {
        return valueInBaseUnit * fromUnit.toMeter / toUnit.toMeter
    }
}

@Composable
fun ParallelogramIcon(
    modifier: Modifier = Modifier,
    isCalculating: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "rotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (isCalculating) 360f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val gradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFF4A148C),
            Color(0xFF7B1FA2)
        )
    )

    var startAnim by remember { mutableStateOf(false) }
    val animatedScale by animateFloatAsState(
        targetValue = if (startAnim) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "parallelogramAnim"
    )

    LaunchedEffect(Unit) {
        delay(300)
        startAnim = true
    }

    Canvas(
        modifier = modifier
            .size(160.dp)
            .semantics {
                contentDescription = "Visualisasi jajar genjang animasi"
                role = Role.Image
            }
    ) {
        val strokePx = 8.dp.toPx()
        val width = size.width * 0.8f * animatedScale
        val height = size.height * 0.6f * animatedScale
        val slant = width * 0.25f

        val topLeft = Offset(center.x - width / 2 + slant, center.y - height / 2)
        val topRight = Offset(center.x + width / 2 + slant, center.y - height / 2)
        val bottomRight = Offset(center.x + width / 2 - slant, center.y + height / 2)
        val bottomLeft = Offset(center.x - width / 2 - slant, center.y + height / 2)

        val path = Path().apply {
            moveTo(topLeft.x, topLeft.y)
            lineTo(topRight.x, topRight.y)
            lineTo(bottomRight.x, bottomRight.y)
            lineTo(bottomLeft.x, bottomLeft.y)
            close()
        }

        drawPath(
            path = path,
            brush = gradient,
            style = Stroke(width = strokePx)
        )

        if (animatedScale > 0.8f) {
            val tinggiPoint = Offset(topLeft.x, bottomLeft.y)
            drawLine(
                brush = gradient,
                start = topLeft,
                end = tinggiPoint,
                strokeWidth = strokePx * 0.6f
            )

            drawLine(
                brush = gradient,
                start = bottomLeft,
                end = bottomRight,
                strokeWidth = strokePx * 0.6f
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParallelogramAreaCalculatorScreen(navController: NavController) {
    var alasInBaseUnit by rememberSaveable { mutableStateOf<Double?>(null) }
    var tinggiInBaseUnit by rememberSaveable { mutableStateOf<Double?>(null) }
    var alas by rememberSaveable { mutableStateOf("") }
    var tinggi by rememberSaveable { mutableStateOf("") }
    var hasil by remember { mutableStateOf<HasilJajarGenjang?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var selectedUnit by rememberSaveable { mutableStateOf(ParallelogramUnit.CM) }
    var isCalculating by remember { mutableStateOf(false) }
    var showUnitPicker by remember { mutableStateOf(false) }
    var previousUnit by remember { mutableStateOf(ParallelogramUnit.CM) }

    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val hapticFeedback = LocalHapticFeedback.current
    val clipboardManager = LocalClipboardManager.current

    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFEDE7F6),
            Color(0xFFD1C4E9)
        )
    )

    fun updateDisplayedValues() {
        alasInBaseUnit?.let { a ->
            val convertedA = ParallelogramCalculator.convertValue(a, ParallelogramUnit.M, selectedUnit)
            alas = ParallelogramCalculator.formatNumber(convertedA)
        } ?: run { alas = "" }

        tinggiInBaseUnit?.let { t ->
            val convertedT = ParallelogramCalculator.convertValue(t, ParallelogramUnit.M, selectedUnit)
            tinggi = ParallelogramCalculator.formatNumber(convertedT)
        } ?: run { tinggi = "" }
    }

    val calculateArea = remember {
        {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
            isCalculating = true

            val (result, validation) = ParallelogramCalculator.calculateParallelogramProperties(alas, tinggi)

            when (validation) {
                is ValidationResult.Success -> {
                    hasil = result
                    errorMessage = null
                    result?.let {
                        alasInBaseUnit = it.alas * selectedUnit.toMeter
                        tinggiInBaseUnit = it.tinggi * selectedUnit.toMeter
                    }
                }
                is ValidationResult.Error -> {
                    hasil = null
                    errorMessage = validation.message
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    alasInBaseUnit = null
                    tinggiInBaseUnit = null
                }
            }

            isCalculating = false
        }
    }

    LaunchedEffect(selectedUnit) {
        if (previousUnit != selectedUnit) {
            updateDisplayedValues()
            if (alasInBaseUnit != null && tinggiInBaseUnit != null && alas.isNotEmpty() && tinggi.isNotEmpty()) {
                calculateArea()
            }
            previousUnit = selectedUnit
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Kalkulator Luas Jajar Genjang",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundGradient)
                .padding(innerPadding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
                .semantics {
                    contentDescription = "Layar kalkulator luas jajar genjang"
                },
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ParallelogramIcon(isCalculating = isCalculating)

            Text(
                text = "Masukkan panjang alas & tinggi untuk menghitung luas jajar genjang.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF3E5F5)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Satuan: ${selectedUnit.displayName}",
                            fontWeight = FontWeight.Medium
                        )
                        TextButton(onClick = { showUnitPicker = true }) {
                            Text("Ubah")
                        }
                    }

                    OutlinedTextField(
                        value = alas,
                        onValueChange = {
                            alas = it
                            errorMessage = null
                        },
                        label = { Text("Alas (a) dalam ${selectedUnit.symbol}") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Next
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                            .semantics {
                                contentDescription = "Input alas jajar genjang dalam satuan ${selectedUnit.displayName}"
                            },
                        singleLine = true,
                        isError = errorMessage != null,
                        trailingIcon = if (alas.isNotEmpty()) {
                            {
                                TextButton(onClick = { alas = "" }) {
                                    Text("Hapus")
                                }
                            }
                        } else null
                    )

                    OutlinedTextField(
                        value = tinggi,
                        onValueChange = {
                            tinggi = it
                            errorMessage = null
                        },
                        label = { Text("Tinggi (t) dalam ${selectedUnit.symbol}") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                keyboardController?.hide()
                                calculateArea()
                            }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics {
                                contentDescription = "Input tinggi jajar genjang dalam satuan ${selectedUnit.displayName}"
                            },
                        singleLine = true,
                        isError = errorMessage != null,
                        supportingText = errorMessage?.let { { Text(it) } },
                        trailingIcon = if (tinggi.isNotEmpty()) {
                            {
                                TextButton(onClick = { tinggi = "" }) {
                                    Text("Hapus")
                                }
                            }
                        } else null
                    )

                    Button(
                        onClick = calculateArea,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = MaterialTheme.shapes.medium,
                        enabled = !isCalculating,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF6A1B9A)
                        )
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isCalculating) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = Color.White
                                )
                            }
                            Text(
                                if (isCalculating) "Menghitung..." else "Hitung Luas",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = hasil != null,
                enter = fadeIn(animationSpec = tween(500)) +
                        slideInVertically(animationSpec = tween(500)) +
                        scaleIn(animationSpec = tween(500)),
                exit = fadeOut() + slideOutVertically() + scaleOut()
            ) {
                hasil?.let { result ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics {
                                contentDescription = "Hasil perhitungan luas jajar genjang"
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFFFFFF)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Hasil Perhitungan",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF6A1B9A)
                                )
                                TextButton(
                                    onClick = {
                                        val resultText = buildString {
                                            appendLine("=== HASIL PERHITUNGAN JAJAR GENJANG ===")
                                            appendLine("Alas: ${ParallelogramCalculator.formatNumber(result.alas)} ${selectedUnit.symbol}")
                                            appendLine("Tinggi: ${ParallelogramCalculator.formatNumber(result.tinggi)} ${selectedUnit.symbol}")
                                            appendLine("Luas: ${ParallelogramCalculator.formatNumber(result.luas)} ${selectedUnit.symbol}²")
                                        }
                                        clipboardManager.setText(AnnotatedString(resultText))
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                    }
                                ) {
                                    Text("Salin")
                                }
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                            ResultSection(
                                title = "Diketahui"
                            ) {
                                Text("Alas (a) = ${ParallelogramCalculator.formatNumber(result.alas)} ${selectedUnit.symbol}")
                                Text("Tinggi (t) = ${ParallelogramCalculator.formatNumber(result.tinggi)} ${selectedUnit.symbol}")
                            }

                            ResultSection(
                                title = "Rumus"
                            ) {
                                FormulaCard("Luas = a × t")
                            }

                            ResultSection(
                                title = "Penyelesaian"
                            ) {
                                Text("Luas = a × t = ${ParallelogramCalculator.formatNumber(result.alas)} × ${ParallelogramCalculator.formatNumber(result.tinggi)}")
                            }

                            ResultSection(
                                title = "Hasil Akhir"
                            ) {
                                SelectionContainer {
                                    FinalResult(
                                        label = "Luas",
                                        value = ParallelogramCalculator.formatNumber(result.luas),
                                        unit = "${selectedUnit.symbol}²",
                                        color = Color(0xFF6A1B9A)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showUnitPicker) {
        AlertDialog(
            onDismissRequest = { showUnitPicker = false },
            title = { Text("Pilih Satuan") },
            text = {
                Column {
                    ParallelogramUnit.values().forEach { unit ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedUnit = unit
                                    showUnitPicker = false
                                    hasil = null
                                    errorMessage = null
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            RadioButton(
                                selected = selectedUnit == unit,
                                onClick = {
                                    selectedUnit = unit
                                    showUnitPicker = false
                                    hasil = null
                                    errorMessage = null
                                }
                            )
                            Text(
                                text = "${unit.displayName} (${unit.symbol})",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showUnitPicker = false }) {
                    Text("Batal")
                }
            }
        )
    }

    LaunchedEffect(Unit) {
        delay(500)
        focusRequester.requestFocus()
    }
}

@Composable
private fun ResultSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            title,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF6A1B9A)
        )
        content()
    }
}

@Composable
private fun FormulaCard(formula: String, color: Color = Color(0xFF6A1B9A)) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = formula,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(12.dp),
            color = color
        )
    }
}

@Composable
private fun FinalResult(
    label: String,
    value: String,
    unit: String,
    color: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$label:",
                fontWeight = FontWeight.Medium,
                color = color
            )
            Text(
                text = "$value $unit",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}