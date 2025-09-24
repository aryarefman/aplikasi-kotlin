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

data class TriangleResult(
    val base: Double,
    val height: Double,
    val area: Double
)

class TriangleActivity : AppCompatActivity() {

    // UI Components
    private lateinit var backButton: ImageButton
    private lateinit var unitLabel: TextView
    private lateinit var changeUnitButton: Button
    private lateinit var baseInput: EditText
    private lateinit var clearBaseButton: Button
    private lateinit var heightInput: EditText
    private lateinit var clearHeightButton: Button
    private lateinit var calculateButton: Button
    private lateinit var resultCard: androidx.cardview.widget.CardView
    private lateinit var copyButton: Button
    private lateinit var baseResult: TextView
    private lateinit var heightResult: TextView
    private lateinit var solutionResult: TextView
    private lateinit var finalResult: TextView

    // Variables
    private var selectedUnit = Unit.CM
    private var currentResult: TriangleResult? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_triangle)

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
        baseInput = findViewById(R.id.base_input)
        clearBaseButton = findViewById(R.id.clear_base_button)
        heightInput = findViewById(R.id.height_input)
        clearHeightButton = findViewById(R.id.clear_height_button)
        calculateButton = findViewById(R.id.calculate_button)
        resultCard = findViewById(R.id.result_card)
        copyButton = findViewById(R.id.copy_button)
        baseResult = findViewById(R.id.base_result)
        heightResult = findViewById(R.id.height_result)
        solutionResult = findViewById(R.id.solution_result)
        finalResult = findViewById(R.id.final_result)
    }

    private fun setupListeners() {
        backButton.setOnClickListener { onBackPressed() }

        changeUnitButton.setOnClickListener { showUnitPicker() }

        clearBaseButton.setOnClickListener {
            baseInput.text.clear()
            hideResult()
        }

        clearHeightButton.setOnClickListener {
            heightInput.text.clear()
            hideResult()
        }

        calculateButton.setOnClickListener { calculateTriangleArea() }

        copyButton.setOnClickListener { copyResultToClipboard() }

        baseInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                clearBaseButton.visibility = if (!s.isNullOrEmpty()) View.VISIBLE else View.GONE
                if (resultCard.visibility == View.VISIBLE) hideResult()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        heightInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                clearHeightButton.visibility = if (!s.isNullOrEmpty()) View.VISIBLE else View.GONE
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
        baseInput.hint = "contoh: 5"
        heightInput.hint = "contoh: 7"
    }

    private fun calculateTriangleArea() {
        val baseText = baseInput.text.toString().trim()
        val heightText = heightInput.text.toString().trim()

        if (baseText.isEmpty() || heightText.isEmpty()) {
            showError("Alas dan tinggi tidak boleh kosong")
            return
        }

        val base = baseText.replace(",", ".").toDoubleOrNull()
        val height = heightText.replace(",", ".").toDoubleOrNull()

        if (base == null || height == null) {
            showError("Masukkan angka yang valid")
            return
        }

        when {
            base <= 0 || height <= 0 -> {
                showError("Alas dan tinggi harus lebih besar dari 0")
                return
            }
            base > 1_000_000 || height > 1_000_000 -> {
                showError("Nilai terlalu besar (maksimal 1,000,000)")
                return
            }
            base < 0.0001 || height < 0.0001 -> {
                showError("Nilai terlalu kecil (minimal 0.0001)")
                return
            }
        }

        val area = 0.5 * base * height
        currentResult = TriangleResult(base, height, area)

        displayResult(base, height, area)
        showResult()
    }

    private fun displayResult(base: Double, height: Double, area: Double) {
        val formattedBase = formatNumber(base)
        val formattedHeight = formatNumber(height)
        val formattedArea = formatNumber(area)

        baseResult.text = "Alas = $formattedBase ${selectedUnit.symbol}"
        heightResult.text = "Tinggi = $formattedHeight ${selectedUnit.symbol}"
        solutionResult.text = "Luas = 0.5 × alas × tinggi = 0.5 × $formattedBase × $formattedHeight"
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
                === HASIL PERHITUNGAN SEGITIGA ===
                Alas: ${formatNumber(result.base)} ${selectedUnit.symbol}
                Tinggi: ${formatNumber(result.height)} ${selectedUnit.symbol}
                Luas: ${formatNumber(result.area)} ${selectedUnit.symbol}²
                Rumus: Luas = 0.5 × alas × tinggi
                Perhitungan: Luas = 0.5 × ${formatNumber(result.base)} × ${formatNumber(result.height)}
            """.trimIndent()

            clipboard.setPrimaryClip(ClipData.newPlainText("Hasil Segitiga", resultText))
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