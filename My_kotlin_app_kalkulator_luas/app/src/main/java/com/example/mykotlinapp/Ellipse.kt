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
import kotlin.math.PI
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.ui.graphics.StrokeCap
import java.text.DecimalFormat
import kotlinx.coroutines.delay

data class HasilElips(
    val sumbuBesar: Double,
    val sumbuKecil: Double,
    val luas: Double
)

enum class EllipseUnit(val symbol: String, val displayName: String, val toMeter: Double) {
    MM("mm", "Milimeter", 0.001),
    CM("cm", "Sentimeter", 0.01),
    M("m", "Meter", 1.0),
    KM("km", "Kilometer", 1000.0),
    INCH("in", "Inch", 0.0254),
    FEET("ft", "Feet", 0.3048)
}

sealed class ValidationResult {
    object Success : ValidationResult()
    data class Error(val message: String) : ValidationResult()
}

object EllipseCalculator {
    fun calculateEllipseProperties(a: String, b: String): Pair<HasilElips?, ValidationResult> {
        return try {
            when {
                a.trim().isEmpty() || b.trim().isEmpty() -> null to ValidationResult.Error("Sumbu tidak boleh kosong")
                else -> {
                    val sumbuBesar = a.trim().replace(",", ".").toDoubleOrNull()
                    val sumbuKecil = b.trim().replace(",", ".").toDoubleOrNull()
                    when {
                        sumbuBesar == null || sumbuKecil == null -> null to ValidationResult.Error("Masukkan angka yang valid")
                        sumbuBesar <= 0 || sumbuKecil <= 0 -> null to ValidationResult.Error("Sumbu harus lebih besar dari 0")
                        sumbuBesar > 1_000_000 || sumbuKecil > 1_000_000 -> null to ValidationResult.Error("Nilai terlalu besar (maksimal 1,000,000)")
                        sumbuBesar < 0.0001 || sumbuKecil < 0.0001 -> null to ValidationResult.Error("Nilai terlalu kecil (minimal 0.0001)")
                        else -> {
                            val luas = PI * sumbuBesar * sumbuKecil
                            HasilElips(sumbuBesar, sumbuKecil, luas) to ValidationResult.Success
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

    fun convertValue(valueInBaseUnit: Double, fromUnit: EllipseUnit, toUnit: EllipseUnit): Double {
        return valueInBaseUnit * fromUnit.toMeter / toUnit.toMeter
    }
}

@Composable
fun EllipseIcon(
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
            Color(0xFF37474F),
            Color(0xFF78909C)
        )
    )

    var startAnim by remember { mutableStateOf(false) }
    val animatedScale by animateFloatAsState(
        targetValue = if (startAnim) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "ellipseAnim"
    )

    LaunchedEffect(Unit) {
        delay(300)
        startAnim = true
    }

    Canvas(
        modifier = modifier
            .size(160.dp)
            .semantics {
                contentDescription = "Visualisasi elips animasi"
                role = Role.Image
            }
    ) {
        val strokePx = 8.dp.toPx()
        val width = size.width * 0.8f * animatedScale
        val height = size.height * 0.5f * animatedScale

        drawOval(
            brush = gradient,
            style = Stroke(width = strokePx),
            topLeft = Offset(center.x - width / 2, center.y - height / 2),
            size = androidx.compose.ui.geometry.Size(width, height)
        )

        if (animatedScale > 0.8f) {
            val horizontalLineLength = width
            val verticalLineLength = height

            val cosRad = kotlin.math.cos(Math.toRadians(rotation.toDouble())).toFloat()
            val sinRad = kotlin.math.sin(Math.toRadians(rotation.toDouble())).toFloat()

            val hStartX = center.x - (horizontalLineLength / 2) * cosRad
            val hStartY = center.y - (horizontalLineLength / 2) * sinRad
            val hEndX = center.x + (horizontalLineLength / 2) * cosRad
            val hEndY = center.y + (horizontalLineLength / 2) * sinRad

            val vStartX = center.x + (verticalLineLength / 2) * sinRad
            val vStartY = center.y - (verticalLineLength / 2) * cosRad
            val vEndX = center.x - (verticalLineLength / 2) * sinRad
            val vEndY = center.y + (verticalLineLength / 2) * cosRad

            drawLine(
                brush = gradient,
                start = Offset(hStartX, hStartY),
                end = Offset(hEndX, hEndY),
                strokeWidth = strokePx * 0.6f,
                cap = StrokeCap.Round
            )

            drawLine(
                brush = gradient,
                start = Offset(vStartX, vStartY),
                end = Offset(vEndX, vEndY),
                strokeWidth = strokePx * 0.6f,
                cap = StrokeCap.Round
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EllipseAreaCalculatorScreen(navController: NavController) {
    var sumbuBesarInBaseUnit by rememberSaveable { mutableStateOf<Double?>(null) }
    var sumbuKecilInBaseUnit by rememberSaveable { mutableStateOf<Double?>(null) }
    var sumbuBesar by rememberSaveable { mutableStateOf("") }
    var sumbuKecil by rememberSaveable { mutableStateOf("") }
    var hasil by remember { mutableStateOf<HasilElips?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var selectedUnit by rememberSaveable { mutableStateOf(EllipseUnit.CM) }
    var isCalculating by remember { mutableStateOf(false) }
    var showUnitPicker by remember { mutableStateOf(false) }
    var previousUnit by remember { mutableStateOf(EllipseUnit.CM) }

    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val hapticFeedback = LocalHapticFeedback.current
    val clipboardManager = LocalClipboardManager.current

    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFCFD8DC),
            Color(0xFFECEFF1)
        )
    )

    fun updateDisplayedValues() {
        sumbuBesarInBaseUnit?.let { a ->
            val convertedA = EllipseCalculator.convertValue(a, EllipseUnit.M, selectedUnit)
            sumbuBesar = EllipseCalculator.formatNumber(convertedA)
        } ?: run { sumbuBesar = "" }

        sumbuKecilInBaseUnit?.let { b ->
            val convertedB = EllipseCalculator.convertValue(b, EllipseUnit.M, selectedUnit)
            sumbuKecil = EllipseCalculator.formatNumber(convertedB)
        } ?: run { sumbuKecil = "" }
    }

    val calculateArea = remember {
        {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
            isCalculating = true

            val (result, validation) = EllipseCalculator.calculateEllipseProperties(sumbuBesar, sumbuKecil)

            when (validation) {
                is ValidationResult.Success -> {
                    hasil = result
                    errorMessage = null
                    result?.let {
                        sumbuBesarInBaseUnit = it.sumbuBesar * selectedUnit.toMeter
                        sumbuKecilInBaseUnit = it.sumbuKecil * selectedUnit.toMeter
                    }
                }
                is ValidationResult.Error -> {
                    hasil = null
                    errorMessage = validation.message
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    sumbuBesarInBaseUnit = null
                    sumbuKecilInBaseUnit = null
                }
            }

            isCalculating = false
        }
    }

    LaunchedEffect(selectedUnit) {
        if (previousUnit != selectedUnit) {
            updateDisplayedValues()
            if (sumbuBesarInBaseUnit != null && sumbuKecilInBaseUnit != null && sumbuBesar.isNotEmpty() && sumbuKecil.isNotEmpty()) {
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
                        "Kalkulator Luas Elips",
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
                    contentDescription = "Layar kalkulator luas elips"
                },
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            EllipseIcon(isCalculating = isCalculating)

            Text(
                text = "Masukkan panjang sumbu besar & kecil untuk menghitung luas elips.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFECEFF1)
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
                        value = sumbuBesar,
                        onValueChange = {
                            sumbuBesar = it
                            errorMessage = null
                        },
                        label = { Text("Sumbu Besar (a) dalam ${selectedUnit.symbol}") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Next
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                            .semantics {
                                contentDescription = "Input sumbu besar elips dalam satuan ${selectedUnit.displayName}"
                            },
                        singleLine = true,
                        isError = errorMessage != null,
                        trailingIcon = if (sumbuBesar.isNotEmpty()) {
                            {
                                TextButton(onClick = { sumbuBesar = "" }) {
                                    Text("Hapus")
                                }
                            }
                        } else null
                    )

                    OutlinedTextField(
                        value = sumbuKecil,
                        onValueChange = {
                            sumbuKecil = it
                            errorMessage = null
                        },
                        label = { Text("Sumbu Kecil (b) dalam ${selectedUnit.symbol}") },
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
                                contentDescription = "Input sumbu kecil elips dalam satuan ${selectedUnit.displayName}"
                            },
                        singleLine = true,
                        isError = errorMessage != null,
                        supportingText = errorMessage?.let { { Text(it) } },
                        trailingIcon = if (sumbuKecil.isNotEmpty()) {
                            {
                                TextButton(onClick = { sumbuKecil = "" }) {
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
                            containerColor = Color(0xFF455A64)
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
                                contentDescription = "Hasil perhitungan luas elips"
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
                                    color = Color(0xFF455A64)
                                )
                                TextButton(
                                    onClick = {
                                        val resultText = buildString {
                                            appendLine("=== HASIL PERHITUNGAN ELIPS ===")
                                            appendLine("Sumbu Besar: ${EllipseCalculator.formatNumber(result.sumbuBesar)} ${selectedUnit.symbol}")
                                            appendLine("Sumbu Kecil: ${EllipseCalculator.formatNumber(result.sumbuKecil)} ${selectedUnit.symbol}")
                                            appendLine("Luas: ${EllipseCalculator.formatNumber(result.luas)} ${selectedUnit.symbol}²")
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
                                Text("Sumbu Besar (a) = ${EllipseCalculator.formatNumber(result.sumbuBesar)} ${selectedUnit.symbol}")
                                Text("Sumbu Kecil (b) = ${EllipseCalculator.formatNumber(result.sumbuKecil)} ${selectedUnit.symbol}")
                            }

                            ResultSection(
                                title = "Rumus"
                            ) {
                                FormulaCard("Luas = π × a × b")
                            }

                            ResultSection(
                                title = "Penyelesaian"
                            ) {
                                Text("Luas = π × a × b = π × ${EllipseCalculator.formatNumber(result.sumbuBesar)} × ${EllipseCalculator.formatNumber(result.sumbuKecil)}")
                            }

                            ResultSection(
                                title = "Hasil Akhir"
                            ) {
                                SelectionContainer {
                                    FinalResult(
                                        label = "Luas",
                                        value = EllipseCalculator.formatNumber(result.luas),
                                        unit = "${selectedUnit.symbol}²",
                                        color = Color(0xFF455A64)
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
                    EllipseUnit.values().forEach { unit ->
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
            color = Color(0xFF455A64)
        )
        content()
    }
}

@Composable
private fun FormulaCard(formula: String, color: Color = Color(0xFF455A64)) {
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