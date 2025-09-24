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

data class CircleResult(
    val radius: Double,
    val area: Double
)

enum class MeasurementUnit(val symbol: String, val displayName: String, val toMeter: Double) {
    MM("mm", "milimeter", 0.001),
    CM("cm", "sentimeter", 0.01),
    M("m", "meter", 1.0),
    KM("km", "kilometer", 1000.0),
    INCH("in", "inchi", 0.0254),
    FEET("ft", "feet", 0.3048)
}

class CircleActivity : AppCompatActivity() {
    private lateinit var backButton: ImageButton
    private lateinit var unitLabel: TextView
    private lateinit var changeUnitButton: Button
    private lateinit var radiusInput: EditText
    private lateinit var clearButton: Button
    private lateinit var calculateButton: Button
    private lateinit var resultCard: androidx.cardview.widget.CardView
    private lateinit var copyButton: Button
    private lateinit var radiusResult: TextView
    private lateinit var solutionResult: TextView
    private lateinit var finalResult: TextView
    private var selectedUnit = Unit.CM
    private var currentResult: CircleResult? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_circle)

        initializeViews()
        setupListeners()
        updateUnitDisplay()

        resultCard.visibility = View.GONE
    }

    private fun initializeViews() {
        backButton = findViewById(R.id.back_button)
        unitLabel = findViewById(R.id.unit_label)
        changeUnitButton = findViewById(R.id.change_unit_button)
        radiusInput = findViewById(R.id.radius_input)
        clearButton = findViewById(R.id.clear_button)
        calculateButton = findViewById(R.id.calculate_button)
        resultCard = findViewById(R.id.result_card)
        copyButton = findViewById(R.id.copy_button)
        radiusResult = findViewById(R.id.radius_result)
        solutionResult = findViewById(R.id.solution_result)
        finalResult = findViewById(R.id.final_result)
    }

    private fun setupListeners() {
        backButton.setOnClickListener { onBackPressed() }

        changeUnitButton.setOnClickListener { showUnitPicker() }

        clearButton.setOnClickListener {
            radiusInput.text.clear()
            hideResult()
        }

        calculateButton.setOnClickListener { calculateCircleArea() }

        copyButton.setOnClickListener { copyResultToClipboard() }

        radiusInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                clearButton.visibility = if (!s.isNullOrEmpty()) View.VISIBLE else View.GONE
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
        radiusInput.hint = "contoh: 5"
    }

    private fun calculateCircleArea() {
        val radiusText = radiusInput.text.toString().trim()

        if (radiusText.isEmpty()) {
            showError("Jari-jari tidak boleh kosong")
            return
        }

        val radius = radiusText.replace(",", ".").toDoubleOrNull()
        if (radius == null) {
            showError("Masukkan angka yang valid")
            return
        }

        when {
            radius <= 0 -> {
                showError("Jari-jari harus lebih besar dari 0")
                return
            }
            radius > 1_000_000 -> {
                showError("Nilai terlalu besar (maksimal 1,000,000)")
                return
            }
            radius < 0.0001 -> {
                showError("Nilai terlalu kecil (minimal 0.0001)")
                return
            }
        }

        val area = PI * radius * radius
        currentResult = CircleResult(radius, area)

        displayResult(radius, area)
        showResult()
    }

    private fun displayResult(radius: Double, area: Double) {
        val formattedRadius = formatNumber(radius)
        val formattedArea = formatNumber(area)

        radiusResult.text = "Jari-jari (r) = $formattedRadius ${selectedUnit.symbol}"
        solutionResult.text = "Luas = π × r² = ${formatNumber(PI, 2)} × $formattedRadius × $formattedRadius"
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
                === HASIL PERHITUNGAN LINGKARAN ===
                Jari-jari: ${formatNumber(result.radius)} ${selectedUnit.symbol}
                Luas: ${formatNumber(result.area)} ${selectedUnit.symbol}²
                Rumus: Luas = π × r²
                Perhitungan: Luas = ${formatNumber(PI, 2)} × ${formatNumber(result.radius)}²
            """.trimIndent()

            clipboard.setPrimaryClip(ClipData.newPlainText("Hasil Lingkaran", resultText))
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