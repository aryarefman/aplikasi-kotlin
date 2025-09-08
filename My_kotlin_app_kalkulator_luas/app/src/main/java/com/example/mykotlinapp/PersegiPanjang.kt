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

data class HasilPersegiPanjang(
    val panjang: Double,
    val lebar: Double,
    val luas: Double
)

enum class RectangleUnit(val symbol: String, val displayName: String, val toMeter: Double) {
    MM("mm", "Milimeter", 0.001),
    CM("cm", "Sentimeter", 0.01),
    M("m", "Meter", 1.0),
    KM("km", "Kilometer", 1000.0),
    INCH("in", "Inch", 0.0254),
    FEET("ft", "Feet", 0.3048)
}

object RectangleCalculator {
    fun calculateRectangleProperties(length: String, width: String): Pair<HasilPersegiPanjang?, ValidationResult> {
        return try {
            when {
                length.trim().isEmpty() || width.trim().isEmpty() -> null to ValidationResult.Error("Panjang atau lebar tidak boleh kosong")
                else -> {
                    val panjang = length.trim().replace(",", ".").toDoubleOrNull()
                    val lebar = width.trim().replace(",", ".").toDoubleOrNull()
                    when {
                        panjang == null || lebar == null -> null to ValidationResult.Error("Masukkan angka yang valid")
                        panjang <= 0 || lebar <= 0 -> null to ValidationResult.Error("Panjang dan lebar harus lebih besar dari 0")
                        panjang > 1_000_000 || lebar > 1_000_000 -> null to ValidationResult.Error("Nilai terlalu besar (maksimal 1,000,000)")
                        panjang < 0.0001 || lebar < 0.0001 -> null to ValidationResult.Error("Nilai terlalu kecil (minimal 0.0001)")
                        else -> {
                            val luas = panjang * lebar
                            HasilPersegiPanjang(panjang, lebar, luas) to ValidationResult.Success
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

    fun convertValue(valueInBaseUnit: Double, fromUnit: RectangleUnit, toUnit: RectangleUnit): Double {
        return valueInBaseUnit * fromUnit.toMeter / toUnit.toMeter
    }
}

@Composable
fun RectangleIcon(
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
            Color(0xFF0D47A1),
            Color(0xFF42A5F5)
        )
    )

    var startAnim by remember { mutableStateOf(false) }
    val animatedScale by animateFloatAsState(
        targetValue = if (startAnim) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "rectAnim"
    )

    LaunchedEffect(Unit) {
        delay(300)
        startAnim = true
    }

    Canvas(
        modifier = modifier
            .size(160.dp)
            .semantics {
                contentDescription = "Visualisasi persegi panjang animasi"
                role = Role.Image
            }
    ) {
        val strokePx = 8.dp.toPx()
        val width = size.width * 0.7f * animatedScale
        val height = size.height * 0.4f * animatedScale

        val left = center.x - width / 2
        val top = center.y - height / 2
        val right = center.x + width / 2
        val bottom = center.y + height / 2

        val path = Path().apply {
            moveTo(left, top)
            lineTo(right, top)
            lineTo(right, bottom)
            lineTo(left, bottom)
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
                start = Offset(left, top),
                end = Offset(right, top),
                strokeWidth = strokePx * 0.6f
            )

            drawLine(
                brush = gradient,
                start = Offset(left, top),
                end = Offset(left, bottom),
                strokeWidth = strokePx * 0.6f
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RectangleAreaCalculatorScreen(navController: NavController) {
    var panjangInBaseUnit by rememberSaveable { mutableStateOf<Double?>(null) }
    var lebarInBaseUnit by rememberSaveable { mutableStateOf<Double?>(null) }
    var panjang by rememberSaveable { mutableStateOf("") }
    var lebar by rememberSaveable { mutableStateOf("") }
    var hasil by remember { mutableStateOf<HasilPersegiPanjang?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var selectedUnit by rememberSaveable { mutableStateOf(RectangleUnit.CM) }
    var isCalculating by remember { mutableStateOf(false) }
    var showUnitPicker by remember { mutableStateOf(false) }
    var previousUnit by remember { mutableStateOf(RectangleUnit.CM) }

    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val hapticFeedback = LocalHapticFeedback.current
    val clipboardManager = LocalClipboardManager.current

    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFBBDEFB),
            Color(0xFFE3F2FD)
        )
    )

    fun updateDisplayedValues() {
        panjangInBaseUnit?.let { p ->
            val convertedP = RectangleCalculator.convertValue(p, RectangleUnit.M, selectedUnit)
            panjang = RectangleCalculator.formatNumber(convertedP)
        } ?: run { panjang = "" }

        lebarInBaseUnit?.let { l ->
            val convertedL = RectangleCalculator.convertValue(l, RectangleUnit.M, selectedUnit)
            lebar = RectangleCalculator.formatNumber(convertedL)
        } ?: run { lebar = "" }
    }

    val calculateArea = remember {
        {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
            isCalculating = true

            val (result, validation) = RectangleCalculator.calculateRectangleProperties(panjang, lebar)

            when (validation) {
                is ValidationResult.Success -> {
                    hasil = result
                    errorMessage = null
                    result?.let {
                        panjangInBaseUnit = it.panjang * selectedUnit.toMeter
                        lebarInBaseUnit = it.lebar * selectedUnit.toMeter
                    }
                }
                is ValidationResult.Error -> {
                    hasil = null
                    errorMessage = validation.message
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    panjangInBaseUnit = null
                    lebarInBaseUnit = null
                }
            }

            isCalculating = false
        }
    }

    LaunchedEffect(selectedUnit) {
        if (previousUnit != selectedUnit) {
            updateDisplayedValues()
            if (panjangInBaseUnit != null && lebarInBaseUnit != null && panjang.isNotEmpty() && lebar.isNotEmpty()) {
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
                        "Kalkulator Luas Persegi Panjang",
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
                    contentDescription = "Layar kalkulator luas persegi panjang"
                },
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            RectangleIcon(isCalculating = isCalculating)

            Text(
                text = "Masukkan panjang & lebar untuk menghitung luas persegi panjang.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFE3F2FD)
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
                        value = panjang,
                        onValueChange = {
                            panjang = it
                            errorMessage = null
                        },
                        label = { Text("Panjang (p) dalam ${selectedUnit.symbol}") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Next
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                            .semantics {
                                contentDescription = "Input panjang persegi panjang dalam satuan ${selectedUnit.displayName}"
                            },
                        singleLine = true,
                        isError = errorMessage != null,
                        trailingIcon = if (panjang.isNotEmpty()) {
                            {
                                TextButton(onClick = { panjang = "" }) {
                                    Text("Hapus")
                                }
                            }
                        } else null
                    )

                    OutlinedTextField(
                        value = lebar,
                        onValueChange = {
                            lebar = it
                            errorMessage = null
                        },
                        label = { Text("Lebar (l) dalam ${selectedUnit.symbol}") },
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
                                contentDescription = "Input lebar persegi panjang dalam satuan ${selectedUnit.displayName}"
                            },
                        singleLine = true,
                        isError = errorMessage != null,
                        supportingText = errorMessage?.let { { Text(it) } },
                        trailingIcon = if (lebar.isNotEmpty()) {
                            {
                                TextButton(onClick = { lebar = "" }) {
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
                            containerColor = Color(0xFF1976D2)
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
                                contentDescription = "Hasil perhitungan luas persegi panjang"
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
                                    color = Color(0xFF1976D2)
                                )
                                TextButton(
                                    onClick = {
                                        val resultText = buildString {
                                            appendLine("=== HASIL PERHITUNGAN PERSEGI PANJANG ===")
                                            appendLine("Panjang: ${RectangleCalculator.formatNumber(result.panjang)} ${selectedUnit.symbol}")
                                            appendLine("Lebar: ${RectangleCalculator.formatNumber(result.lebar)} ${selectedUnit.symbol}")
                                            appendLine("Luas: ${RectangleCalculator.formatNumber(result.luas)} ${selectedUnit.symbol}²")
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
                                Text("Panjang (p) = ${RectangleCalculator.formatNumber(result.panjang)} ${selectedUnit.symbol}")
                                Text("Lebar (l) = ${RectangleCalculator.formatNumber(result.lebar)} ${selectedUnit.symbol}")
                            }

                            ResultSection(
                                title = "Rumus"
                            ) {
                                FormulaCard("Luas = p × l")
                            }

                            ResultSection(
                                title = "Penyelesaian"
                            ) {
                                Text("Luas = p × l = ${RectangleCalculator.formatNumber(result.panjang)} × ${RectangleCalculator.formatNumber(result.lebar)}")
                            }

                            ResultSection(
                                title = "Hasil Akhir"
                            ) {
                                SelectionContainer {
                                    FinalResult(
                                        label = "Luas",
                                        value = RectangleCalculator.formatNumber(result.luas),
                                        unit = "${selectedUnit.symbol}²",
                                        color = Color(0xFF1976D2)
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
                    RectangleUnit.values().forEach { unit ->
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
            color = Color(0xFF1976D2)
        )
        content()
    }
}

@Composable
private fun FormulaCard(formula: String, color: Color = Color(0xFF1976D2)) {
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