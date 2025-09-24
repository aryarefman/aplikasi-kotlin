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

data class HasilLayangLayang(
    val diagonal1: Double,
    val diagonal2: Double,
    val luas: Double
)

enum class KiteUnit(val symbol: String, val displayName: String, val toMeter: Double) {
    MM("mm", "Milimeter", 0.001),
    CM("cm", "Sentimeter", 0.01),
    M("m", "Meter", 1.0),
    KM("km", "Kilometer", 1000.0),
    INCH("in", "Inch", 0.0254),
    FEET("ft", "Feet", 0.3048)
}

object KiteCalculator {
    fun calculateKiteProperties(d1: String, d2: String): Pair<HasilLayangLayang?, ValidationResult> {
        return try {
            when {
                d1.trim().isEmpty() || d2.trim().isEmpty() -> null to ValidationResult.Error("Diagonal tidak boleh kosong")
                else -> {
                    val diagonal1 = d1.trim().replace(",", ".").toDoubleOrNull()
                    val diagonal2 = d2.trim().replace(",", ".").toDoubleOrNull()
                    when {
                        diagonal1 == null || diagonal2 == null -> null to ValidationResult.Error("Masukkan angka yang valid")
                        diagonal1 <= 0 || diagonal2 <= 0 -> null to ValidationResult.Error("Diagonal harus lebih besar dari 0")
                        diagonal1 > 1_000_000 || diagonal2 > 1_000_000 -> null to ValidationResult.Error("Nilai terlalu besar (maksimal 1,000,000)")
                        diagonal1 < 0.0001 || diagonal2 < 0.0001 -> null to ValidationResult.Error("Nilai terlalu kecil (minimal 0.0001)")
                        else -> {
                            val luas = 0.5 * diagonal1 * diagonal2
                            HasilLayangLayang(diagonal1, diagonal2, luas) to ValidationResult.Success
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

    fun convertValue(valueInBaseUnit: Double, fromUnit: KiteUnit, toUnit: KiteUnit): Double {
        return valueInBaseUnit * fromUnit.toMeter / toUnit.toMeter
    }
}

@Composable
fun KiteIcon(
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
            Color(0xFFFFD54F),
            Color(0xFFFFB300)
        )
    )

    var startAnim by remember { mutableStateOf(false) }
    val animatedScale by animateFloatAsState(
        targetValue = if (startAnim) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "kiteAnim"
    )

    LaunchedEffect(Unit) {
        delay(300)
        startAnim = true
    }

    Canvas(
        modifier = modifier
            .size(160.dp)
            .semantics {
                contentDescription = "Visualisasi layang-layang animasi"
                role = Role.Image
            }
    ) {
        val strokePx = 8.dp.toPx()
        val width = size.width * 0.7f * animatedScale
        val height = size.height * 0.9f * animatedScale

        val top = Offset(center.x, center.y - height * 0.35f)
        val bottom = Offset(center.x, center.y + height * 0.5f)
        val left = Offset(center.x - width / 2, center.y)
        val right = Offset(center.x + width / 2, center.y)

        val path = Path().apply {
            moveTo(top.x, top.y)
            lineTo(right.x, right.y)
            lineTo(bottom.x, bottom.y)
            lineTo(left.x, left.y)
            close()
        }

        drawPath(
            path = path,
            brush = gradient,
            style = Stroke(width = strokePx)
        )

        if (animatedScale > 0.8f) {
            drawLine(
                brush = gradient,
                start = top,
                end = bottom,
                strokeWidth = strokePx * 0.6f
            )

            drawLine(
                brush = gradient,
                start = left,
                end = right,
                strokeWidth = strokePx * 0.6f
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KiteAreaCalculatorScreen(navController: NavController) {
    var diagonal1InBaseUnit by rememberSaveable { mutableStateOf<Double?>(null) }
    var diagonal2InBaseUnit by rememberSaveable { mutableStateOf<Double?>(null) }
    var diagonal1 by rememberSaveable { mutableStateOf("") }
    var diagonal2 by rememberSaveable { mutableStateOf("") }
    var hasil by remember { mutableStateOf<HasilLayangLayang?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var selectedUnit by rememberSaveable { mutableStateOf(KiteUnit.CM) }
    var isCalculating by remember { mutableStateOf(false) }
    var showUnitPicker by remember { mutableStateOf(false) }
    var previousUnit by remember { mutableStateOf(KiteUnit.CM) }

    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val hapticFeedback = LocalHapticFeedback.current
    val clipboardManager = LocalClipboardManager.current

    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFFFECB3),
            Color(0xFFFFE082)
        )
    )

    fun updateDisplayedValues() {
        diagonal1InBaseUnit?.let { d1 ->
            val convertedD1 = KiteCalculator.convertValue(d1, KiteUnit.M, selectedUnit)
            diagonal1 = KiteCalculator.formatNumber(convertedD1)
        } ?: run { diagonal1 = "" }

        diagonal2InBaseUnit?.let { d2 ->
            val convertedD2 = KiteCalculator.convertValue(d2, KiteUnit.M, selectedUnit)
            diagonal2 = KiteCalculator.formatNumber(convertedD2)
        } ?: run { diagonal2 = "" }
    }

    val calculateArea = remember {
        {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
            isCalculating = true

            val (result, validation) = KiteCalculator.calculateKiteProperties(diagonal1, diagonal2)

            when (validation) {
                is ValidationResult.Success -> {
                    hasil = result
                    errorMessage = null
                    result?.let {
                        diagonal1InBaseUnit = it.diagonal1 * selectedUnit.toMeter
                        diagonal2InBaseUnit = it.diagonal2 * selectedUnit.toMeter
                    }
                }
                is ValidationResult.Error -> {
                    hasil = null
                    errorMessage = validation.message
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    diagonal1InBaseUnit = null
                    diagonal2InBaseUnit = null
                }
            }

            isCalculating = false
        }
    }

    LaunchedEffect(selectedUnit) {
        if (previousUnit != selectedUnit) {
            updateDisplayedValues()
            if (diagonal1InBaseUnit != null && diagonal2InBaseUnit != null && diagonal1.isNotEmpty() && diagonal2.isNotEmpty()) {
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
                        "Kalkulator Luas Layang-Layang",
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
                    contentDescription = "Layar kalkulator luas layang-layang"
                },
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            KiteIcon(isCalculating = isCalculating)

            Text(
                text = "Masukkan panjang kedua diagonal untuk menghitung luas layang-layang.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFF9C4)
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
                        value = diagonal1,
                        onValueChange = {
                            diagonal1 = it
                            errorMessage = null
                        },
                        label = { Text("Diagonal 1 (d₁) dalam ${selectedUnit.symbol}") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Next
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                            .semantics {
                                contentDescription = "Input diagonal 1 layang-layang dalam satuan ${selectedUnit.displayName}"
                            },
                        singleLine = true,
                        isError = errorMessage != null,
                        trailingIcon = if (diagonal1.isNotEmpty()) {
                            {
                                TextButton(onClick = { diagonal1 = "" }) {
                                    Text("Hapus")
                                }
                            }
                        } else null
                    )

                    OutlinedTextField(
                        value = diagonal2,
                        onValueChange = {
                            diagonal2 = it
                            errorMessage = null
                        },
                        label = { Text("Diagonal 2 (d₂) dalam ${selectedUnit.symbol}") },
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
                                contentDescription = "Input diagonal 2 layang-layang dalam satuan ${selectedUnit.displayName}"
                            },
                        singleLine = true,
                        isError = errorMessage != null,
                        supportingText = errorMessage?.let { { Text(it) } },
                        trailingIcon = if (diagonal2.isNotEmpty()) {
                            {
                                TextButton(onClick = { diagonal2 = "" }) {
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
                            containerColor = Color(0xFFFFA000)
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
                                contentDescription = "Hasil perhitungan luas layang-layang"
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
                                    color = Color(0xFFFFA000)
                                )
                                TextButton(
                                    onClick = {
                                        val resultText = buildString {
                                            appendLine("=== HASIL PERHITUNGAN LAYANG-LAYANG ===")
                                            appendLine("Diagonal 1: ${KiteCalculator.formatNumber(result.diagonal1)} ${selectedUnit.symbol}")
                                            appendLine("Diagonal 2: ${KiteCalculator.formatNumber(result.diagonal2)} ${selectedUnit.symbol}")
                                            appendLine("Luas: ${KiteCalculator.formatNumber(result.luas)} ${selectedUnit.symbol}²")
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
                                Text("Diagonal 1 (d₁) = ${KiteCalculator.formatNumber(result.diagonal1)} ${selectedUnit.symbol}")
                                Text("Diagonal 2 (d₂) = ${KiteCalculator.formatNumber(result.diagonal2)} ${selectedUnit.symbol}")
                            }

                            ResultSection(
                                title = "Rumus"
                            ) {
                                FormulaCard("Luas = ½ × d₁ × d₂")
                            }

                            ResultSection(
                                title = "Penyelesaian"
                            ) {
                                Text("Luas = ½ × d₁ × d₂ = ½ × ${KiteCalculator.formatNumber(result.diagonal1)} × ${KiteCalculator.formatNumber(result.diagonal2)}")
                            }

                            ResultSection(
                                title = "Hasil Akhir"
                            ) {
                                SelectionContainer {
                                    FinalResult(
                                        label = "Luas",
                                        value = KiteCalculator.formatNumber(result.luas),
                                        unit = "${selectedUnit.symbol}²",
                                        color = Color(0xFFFFA000)
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
                    KiteUnit.values().forEach { unit ->
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
            color = Color(0xFFFFA000)
        )
        content()
    }
}

@Composable
private fun FormulaCard(formula: String, color: Color = Color(0xFFFFA000)) {
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