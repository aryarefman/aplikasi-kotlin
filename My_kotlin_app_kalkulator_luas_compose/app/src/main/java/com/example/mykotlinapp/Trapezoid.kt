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

data class HasilTrapesium(
    val sisiAtas: Double,
    val sisiBawah: Double,
    val tinggi: Double,
    val luas: Double
)

enum class TrapezoidUnit(val symbol: String, val displayName: String, val toMeter: Double) {
    MM("mm", "Milimeter", 0.001),
    CM("cm", "Sentimeter", 0.01),
    M("m", "Meter", 1.0),
    KM("km", "Kilometer", 1000.0),
    INCH("in", "Inch", 0.0254),
    FEET("ft", "Feet", 0.3048)
}

object TrapezoidCalculator {
    fun calculateTrapezoidProperties(topSide: String, bottomSide: String, height: String): Pair<HasilTrapesium?, ValidationResult> {
        return try {
            when {
                topSide.trim().isEmpty() || bottomSide.trim().isEmpty() || height.trim().isEmpty() ->
                    null to ValidationResult.Error("Sisi atau tinggi tidak boleh kosong")
                else -> {
                    val sisiAtas = topSide.trim().replace(",", ".").toDoubleOrNull()
                    val sisiBawah = bottomSide.trim().replace(",", ".").toDoubleOrNull()
                    val tinggi = height.trim().replace(",", ".").toDoubleOrNull()
                    when {
                        sisiAtas == null || sisiBawah == null || tinggi == null ->
                            null to ValidationResult.Error("Masukkan angka yang valid")
                        sisiAtas <= 0 || sisiBawah <= 0 || tinggi <= 0 ->
                            null to ValidationResult.Error("Sisi dan tinggi harus lebih besar dari 0")
                        sisiAtas > 1_000_000 || sisiBawah > 1_000_000 || tinggi > 1_000_000 ->
                            null to ValidationResult.Error("Nilai terlalu besar (maksimal 1,000,000)")
                        sisiAtas < 0.0001 || sisiBawah < 0.0001 || tinggi < 0.0001 ->
                            null to ValidationResult.Error("Nilai terlalu kecil (minimal 0.0001)")
                        else -> {
                            val luas = 0.5 * (sisiAtas + sisiBawah) * tinggi
                            HasilTrapesium(sisiAtas, sisiBawah, tinggi, luas) to ValidationResult.Success
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

    fun convertValue(valueInBaseUnit: Double, fromUnit: TrapezoidUnit, toUnit: TrapezoidUnit): Double {
        return valueInBaseUnit * fromUnit.toMeter / toUnit.toMeter
    }
}

@Composable
fun TrapezoidIcon(
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
            Color(0xFFFF4500),
            Color(0xFFFF8C00)
        )
    )

    var startAnim by remember { mutableStateOf(false) }
    val animatedScale by animateFloatAsState(
        targetValue = if (startAnim) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "trapezoidAnim"
    )

    LaunchedEffect(Unit) {
        delay(300)
        startAnim = true
    }

    Canvas(
        modifier = modifier
            .size(160.dp)
            .semantics {
                contentDescription = "Visualisasi trapesium animasi"
                role = Role.Image
            }
    ) {
        val strokePx = 8.dp.toPx()
        val widthBottom = size.width * 0.8f * animatedScale
        val widthTop = size.width * 0.5f * animatedScale
        val height = size.height * 0.6f * animatedScale

        val topLeft = Offset(center.x - widthTop / 2, center.y - height / 2)
        val topRight = Offset(center.x + widthTop / 2, center.y - height / 2)
        val bottomRight = Offset(center.x + widthBottom / 2, center.y + height / 2)
        val bottomLeft = Offset(center.x - widthBottom / 2, center.y + height / 2)

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
            val tinggiStart = Offset(center.x, topLeft.y)
            val tinggiEnd = Offset(center.x, bottomLeft.y)
            drawLine(
                brush = gradient,
                start = tinggiStart,
                end = tinggiEnd,
                strokeWidth = strokePx * 0.6f
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrapezoidAreaCalculatorScreen(navController: NavController) {
    var sisiAtasInBaseUnit by rememberSaveable { mutableStateOf<Double?>(null) }
    var sisiBawahInBaseUnit by rememberSaveable { mutableStateOf<Double?>(null) }
    var tinggiInBaseUnit by rememberSaveable { mutableStateOf<Double?>(null) }
    var sisiAtas by rememberSaveable { mutableStateOf("") }
    var sisiBawah by rememberSaveable { mutableStateOf("") }
    var tinggi by rememberSaveable { mutableStateOf("") }
    var hasil by remember { mutableStateOf<HasilTrapesium?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var selectedUnit by rememberSaveable { mutableStateOf(TrapezoidUnit.CM) }
    var isCalculating by remember { mutableStateOf(false) }
    var showUnitPicker by remember { mutableStateOf(false) }
    var previousUnit by remember { mutableStateOf(TrapezoidUnit.CM) }

    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val hapticFeedback = LocalHapticFeedback.current
    val clipboardManager = LocalClipboardManager.current

    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFFFE0B2),
            Color(0xFFFFF3E0)
        )
    )

    fun updateDisplayedValues() {
        sisiAtasInBaseUnit?.let { a ->
            val convertedA = TrapezoidCalculator.convertValue(a, TrapezoidUnit.M, selectedUnit)
            sisiAtas = TrapezoidCalculator.formatNumber(convertedA)
        } ?: run { sisiAtas = "" }

        sisiBawahInBaseUnit?.let { b ->
            val convertedB = TrapezoidCalculator.convertValue(b, TrapezoidUnit.M, selectedUnit)
            sisiBawah = TrapezoidCalculator.formatNumber(convertedB)
        } ?: run { sisiBawah = "" }

        tinggiInBaseUnit?.let { t ->
            val convertedT = TrapezoidCalculator.convertValue(t, TrapezoidUnit.M, selectedUnit)
            tinggi = TrapezoidCalculator.formatNumber(convertedT)
        } ?: run { tinggi = "" }
    }

    val calculateArea = remember {
        {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
            isCalculating = true

            val (result, validation) = TrapezoidCalculator.calculateTrapezoidProperties(sisiAtas, sisiBawah, tinggi)

            when (validation) {
                is ValidationResult.Success -> {
                    hasil = result
                    errorMessage = null
                    result?.let {
                        sisiAtasInBaseUnit = it.sisiAtas * selectedUnit.toMeter
                        sisiBawahInBaseUnit = it.sisiBawah * selectedUnit.toMeter
                        tinggiInBaseUnit = it.tinggi * selectedUnit.toMeter
                    }
                }
                is ValidationResult.Error -> {
                    hasil = null
                    errorMessage = validation.message
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    sisiAtasInBaseUnit = null
                    sisiBawahInBaseUnit = null
                    tinggiInBaseUnit = null
                }
            }

            isCalculating = false
        }
    }

    LaunchedEffect(selectedUnit) {
        if (previousUnit != selectedUnit) {
            updateDisplayedValues()
            if (sisiAtasInBaseUnit != null && sisiBawahInBaseUnit != null && tinggiInBaseUnit != null &&
                sisiAtas.isNotEmpty() && sisiBawah.isNotEmpty() && tinggi.isNotEmpty()) {
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
                        "Kalkulator Luas Trapesium",
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
                    contentDescription = "Layar kalkulator luas trapesium"
                },
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TrapezoidIcon(isCalculating = isCalculating)

            Text(
                text = "Masukkan panjang sisi sejajar (atas & bawah) serta tinggi untuk menghitung luas trapesium.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF5F5F5)
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
                        value = sisiAtas,
                        onValueChange = {
                            sisiAtas = it
                            errorMessage = null
                        },
                        label = { Text("Sisi Atas (a) dalam ${selectedUnit.symbol}") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Next
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                            .semantics {
                                contentDescription = "Input sisi atas trapesium dalam satuan ${selectedUnit.displayName}"
                            },
                        singleLine = true,
                        isError = errorMessage != null,
                        trailingIcon = if (sisiAtas.isNotEmpty()) {
                            {
                                TextButton(onClick = { sisiAtas = "" }) {
                                    Text("Hapus")
                                }
                            }
                        } else null
                    )

                    OutlinedTextField(
                        value = sisiBawah,
                        onValueChange = {
                            sisiBawah = it
                            errorMessage = null
                        },
                        label = { Text("Sisi Bawah (b) dalam ${selectedUnit.symbol}") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Next
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics {
                                contentDescription = "Input sisi bawah trapesium dalam satuan ${selectedUnit.displayName}"
                            },
                        singleLine = true,
                        isError = errorMessage != null,
                        trailingIcon = if (sisiBawah.isNotEmpty()) {
                            {
                                TextButton(onClick = { sisiBawah = "" }) {
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
                                contentDescription = "Input tinggi trapesium dalam satuan ${selectedUnit.displayName}"
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
                            containerColor = Color(0xFFFF4500)
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
                                contentDescription = "Hasil perhitungan luas trapesium"
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
                                    color = Color(0xFFE65100)
                                )
                                TextButton(
                                    onClick = {
                                        val resultText = buildString {
                                            appendLine("=== HASIL PERHITUNGAN TRAPESIUM ===")
                                            appendLine("Sisi Atas: ${TrapezoidCalculator.formatNumber(result.sisiAtas)} ${selectedUnit.symbol}")
                                            appendLine("Sisi Bawah: ${TrapezoidCalculator.formatNumber(result.sisiBawah)} ${selectedUnit.symbol}")
                                            appendLine("Tinggi: ${TrapezoidCalculator.formatNumber(result.tinggi)} ${selectedUnit.symbol}")
                                            appendLine("Luas: ${TrapezoidCalculator.formatNumber(result.luas)} ${selectedUnit.symbol}²")
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
                                Text("Sisi Atas (a) = ${TrapezoidCalculator.formatNumber(result.sisiAtas)} ${selectedUnit.symbol}")
                                Text("Sisi Bawah (b) = ${TrapezoidCalculator.formatNumber(result.sisiBawah)} ${selectedUnit.symbol}")
                                Text("Tinggi (t) = ${TrapezoidCalculator.formatNumber(result.tinggi)} ${selectedUnit.symbol}")
                            }

                            ResultSection(
                                title = "Rumus"
                            ) {
                                FormulaCard("Luas = ½ × (a + b) × t")
                            }

                            ResultSection(
                                title = "Penyelesaian"
                            ) {
                                Text("Luas = ½ × (a + b) × t = ½ × (${TrapezoidCalculator.formatNumber(result.sisiAtas)} + ${TrapezoidCalculator.formatNumber(result.sisiBawah)}) × ${TrapezoidCalculator.formatNumber(result.tinggi)}")
                            }

                            ResultSection(
                                title = "Hasil Akhir"
                            ) {
                                SelectionContainer {
                                    FinalResult(
                                        label = "Luas",
                                        value = TrapezoidCalculator.formatNumber(result.luas),
                                        unit = "${selectedUnit.symbol}²",
                                        color = Color(0xFFE65100)
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
                    TrapezoidUnit.values().forEach { unit ->
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
            color = Color(0xFFE65100)
        )
        content()
    }
}

@Composable
private fun FormulaCard(formula: String, color: Color = Color(0xFFE65100)) {
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