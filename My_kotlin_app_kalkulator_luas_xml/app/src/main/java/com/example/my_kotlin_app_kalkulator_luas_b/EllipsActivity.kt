package com.example.kalkulatorgeometri

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.text.DecimalFormat
import kotlin.math.PI

data class EllipseResult(
    val majorAxis: Double,
    val minorAxis: Double,
    val area: Double
)

enum class MeasurementUnitUnit(val symbol: String, val displayName: String, val toMeter: Double) {
    MM("mm", "milimeter", 0.001),
    CM("cm", "sentimeter", 0.01),
    M("m", "meter", 1.0),
    KM("km", "kilometer", 1000.0),
    INCH("in", "inchi", 0.0254),
    FEET("ft", "feet", 0.3048)
}

class EllipsActivity : AppCompatActivity() {

    // UI Components
    private lateinit var backButton: ImageButton
    private lateinit var unitLabel: TextView
    private lateinit var changeUnitButton: Button
    private lateinit var majorAxisInput: EditText
    private lateinit var clearMajorButton: Button
    private lateinit var minorAxisInput: EditText
    private lateinit var clearMinorButton: Button
    private lateinit var calculateButton: Button
    private lateinit var resultCard: androidx.cardview.widget.CardView
    private lateinit var copyButton: Button
    private lateinit var majorAxisResult: TextView
    private lateinit var minorAxisResult: TextView
    private lateinit var solutionResult: TextView
    private lateinit var finalResult: TextView

    // Variables
    private var selectedUnit = Unit.CM
    private var currentResult: EllipseResult? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ellips)

        initializeViews()
        setupListeners()
        updateUnitDisplay()

        // result card hidden at first
        resultCard.visibility = View.GONE
    }

    private fun initializeViews() {
        backButton = findViewById(R.id.back_button)
        unitLabel = findViewById(R.id.unit_label)
        changeUnitButton = findViewById(R.id.change_unit_button)
        majorAxisInput = findViewById(R.id.major_axis_input)
        clearMajorButton = findViewById(R.id.clear_major_button)
        minorAxisInput = findViewById(R.id.minor_axis_input)
        clearMinorButton = findViewById(R.id.clear_minor_button)
        calculateButton = findViewById(R.id.calculate_button)
        resultCard = findViewById(R.id.result_card)
        copyButton = findViewById(R.id.copy_button)
        majorAxisResult = findViewById(R.id.major_axis_result)
        minorAxisResult = findViewById(R.id.minor_axis_result)
        solutionResult = findViewById(R.id.solution_result)
        finalResult = findViewById(R.id.final_result)
    }

    private fun setupListeners() {
        backButton.setOnClickListener { onBackPressed() }

        changeUnitButton.setOnClickListener { showUnitPicker() }

        clearMajorButton.setOnClickListener {
            majorAxisInput.text.clear()
            hideResult()
        }

        clearMinorButton.setOnClickListener {
            minorAxisInput.text.clear()
            hideResult()
        }

        calculateButton.setOnClickListener { calculateEllipseArea() }

        copyButton.setOnClickListener { copyResultToClipboard() }

        majorAxisInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                clearMajorButton.visibility = if (!s.isNullOrEmpty()) View.VISIBLE else View.GONE
                if (resultCard.visibility == View.VISIBLE) hideResult()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        minorAxisInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                clearMinorButton.visibility = if (!s.isNullOrEmpty()) View.VISIBLE else View.GONE
                if (resultCard.visibility == View.VISIBLE) hideResult()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun showUnitPicker() {
        val units = Unit.values()
        val unitNames = units.map { "${it.displayName} (${it.symbol})" }.toTypedArray()
        val currentIndex = units.indexOf(selectedUnit)

        AlertDialog.Builder(this)
            .setTitle("Pilih Satuan")
            .setSingleChoiceItems(unitNames, currentIndex) { dialog, which ->
                selectedUnit = units[which]
                updateUnitDisplay()
                hideResult()
                dialog.dismiss()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun updateUnitDisplay() {
        unitLabel.text = "Satuan: ${selectedUnit.displayName}"
        majorAxisInput.hint = "contoh: 5"
        minorAxisInput.hint = "contoh: 3"
    }

    private fun calculateEllipseArea() {
        val majorAxisText = majorAxisInput.text.toString().trim()
        val minorAxisText = minorAxisInput.text.toString().trim()

        if (majorAxisText.isEmpty() || minorAxisText.isEmpty()) {
            showError("Sumbu mayor dan minor tidak boleh kosong")
            return
        }

        val majorAxis = majorAxisText.replace(",", ".").toDoubleOrNull()
        val minorAxis = minorAxisText.replace(",", ".").toDoubleOrNull()

        if (majorAxis == null || minorAxis == null) {
            showError("Masukkan angka yang valid")
            return
        }

        when {
            majorAxis <= 0 || minorAxis <= 0 -> {
                showError("Sumbu harus lebih besar dari 0")
                return
            }
            majorAxis > 1_000_000 || minorAxis > 1_000_000 -> {
                showError("Nilai terlalu besar (maksimal 1,000,000)")
                return
            }
            majorAxis < 0.0001 || minorAxis < 0.0001 -> {
                showError("Nilai terlalu kecil (minimal 0.0001)")
                return
            }
        }

        val area = PI * majorAxis * minorAxis
        currentResult = EllipseResult(majorAxis, minorAxis, area)

        displayResult(majorAxis, minorAxis, area)
        showResult()
    }

    private fun displayResult(majorAxis: Double, minorAxis: Double, area: Double) {
        val formattedMajorAxis = formatNumber(majorAxis)
        val formattedMinorAxis = formatNumber(minorAxis)
        val formattedArea = formatNumber(area)

        majorAxisResult.text = "Sumbu mayor (a) = $formattedMajorAxis ${selectedUnit.symbol}"
        minorAxisResult.text = "Sumbu minor (b) = $formattedMinorAxis ${selectedUnit.symbol}"
        solutionResult.text = "Luas = π × a × b = ${formatNumber(PI, 2)} × $formattedMajorAxis × $formattedMinorAxis"
        finalResult.text = "$formattedArea ${selectedUnit.symbol}²"
    }

    private fun showResult() {
        resultCard.visibility = View.VISIBLE
        resultCard.post {
            val scrollView = findViewById<ScrollView>(R.id.scroll_view)
            scrollView?.smoothScrollTo(0, resultCard.bottom)
        }
    }

    private fun hideResult() {
        resultCard.visibility = View.GONE
        currentResult = null
    }

    private fun copyResultToClipboard() {
        currentResult?.let { result ->
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val resultText = """
                === HASIL PERHITUNGAN ELLIPS ===
                Sumbu mayor (a): ${formatNumber(result.majorAxis)} ${selectedUnit.symbol}
                Sumbu minor (b): ${formatNumber(result.minorAxis)} ${selectedUnit.symbol}
                Luas: ${formatNumber(result.area)} ${selectedUnit.symbol}²
                Rumus: Luas = π × a × b
                Perhitungan: Luas = ${formatNumber(PI, 2)} × ${formatNumber(result.majorAxis)} × ${formatNumber(result.minorAxis)}
            """.trimIndent()

            clipboard.setPrimaryClip(ClipData.newPlainText("Hasil Ellips", resultText))
            Toast.makeText(this, "Hasil disalin ke clipboard", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        hideResult()
    }

    private fun formatNumber(value: Double, precision: Int = 4): String {
        val formatter = DecimalFormat().apply {
            maximumFractionDigits = precision
            minimumFractionDigits = 0
            isGroupingUsed = true
        }
        return formatter.format(value)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}