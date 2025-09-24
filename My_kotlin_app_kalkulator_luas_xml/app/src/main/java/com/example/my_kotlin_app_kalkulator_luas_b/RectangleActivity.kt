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

data class RectangleResult(
    val length: Double,
    val width: Double,
    val area: Double,
    val perimeter: Double
)

class RectangleActivity : AppCompatActivity() {

    private lateinit var backButton: ImageButton
    private lateinit var unitLabel: TextView
    private lateinit var changeUnitButton: Button
    private lateinit var lengthInput: EditText
    private lateinit var clearLengthButton: Button
    private lateinit var widthInput: EditText
    private lateinit var clearWidthButton: Button
    private lateinit var calculateButton: com.google.android.material.button.MaterialButton
    private lateinit var resultCard: androidx.cardview.widget.CardView
    private lateinit var copyButton: Button
    private lateinit var lengthResult: TextView
    private lateinit var widthResult: TextView
    private lateinit var solutionResult: TextView
    private lateinit var finalResult: TextView

    private var selectedUnit = Unit.CM
    private var currentResult: RectangleResult? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rectangle)

        initializeViews()
        setupListeners()
        updateUnitDisplay()

        resultCard.visibility = View.GONE
    }

    private fun initializeViews() {
        backButton = findViewById(R.id.back_button)
        unitLabel = findViewById(R.id.unit_label)
        changeUnitButton = findViewById(R.id.change_unit_button)
        lengthInput = findViewById(R.id.length_input)
        clearLengthButton = findViewById(R.id.clear_length_button)
        widthInput = findViewById(R.id.width_input)
        clearWidthButton = findViewById(R.id.clear_width_button)
        calculateButton = findViewById(R.id.calculate_button)
        resultCard = findViewById(R.id.result_card)
        copyButton = findViewById(R.id.copy_button)
        lengthResult = findViewById(R.id.length_result)
        widthResult = findViewById(R.id.width_result)
        solutionResult = findViewById(R.id.solution_result)
        finalResult = findViewById(R.id.final_result)
    }

    private fun setupListeners() {
        backButton.setOnClickListener { onBackPressed() }

        changeUnitButton.setOnClickListener { showUnitPicker() }

        clearLengthButton.setOnClickListener {
            lengthInput.text.clear()
            clearLengthButton.visibility = View.GONE
            if (resultCard.visibility == View.VISIBLE) hideResult()
        }

        clearWidthButton.setOnClickListener {
            widthInput.text.clear()
            clearWidthButton.visibility = View.GONE
            if (resultCard.visibility == View.VISIBLE) hideResult()
        }

        calculateButton.setOnClickListener { calculateRectangle() }

        copyButton.setOnClickListener { copyResultToClipboard() }

        lengthInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                clearLengthButton.visibility = if (!s.isNullOrEmpty()) View.VISIBLE else View.GONE
                if (resultCard.visibility == View.VISIBLE) hideResult()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        widthInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                clearWidthButton.visibility = if (!s.isNullOrEmpty()) View.VISIBLE else View.GONE
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
        lengthInput.hint = "contoh: 5, 8.9"
        widthInput.hint = "contoh: 7, 10.5"
    }

    private fun calculateRectangle() {
        val lengthText = lengthInput.text.toString().trim()
        val widthText = widthInput.text.toString().trim()

        if (lengthText.isEmpty() || widthText.isEmpty()) {
            showError("Panjang dan lebar tidak boleh kosong")
            return
        }

        val length = lengthText.replace(",", ".").toDoubleOrNull()
        val width = widthText.replace(",", ".").toDoubleOrNull()

        if (length == null || width == null) {
            showError("Masukkan angka yang valid")
            return
        }

        when {
            length <= 0 || width <= 0 -> {
                showError("Panjang dan lebar harus lebih besar dari 0")
                return
            }
            length > 1_000_000 || width > 1_000_000 -> {
                showError("Nilai terlalu besar (maksimal 1,000,000)")
                return
            }
            length < 0.0001 || width < 0.0001 -> {
                showError("Nilai terlalu kecil (minimal 0.0001)")
                return
            }
        }

        val area = length * width
        val perimeter = 2 * (length + width)
        currentResult = RectangleResult(length, width, area, perimeter)

        displayResult(length, width, area, perimeter)
        showResult()
    }

    private fun displayResult(length: Double, width: Double, area: Double, perimeter: Double) {
        val formattedLength = formatNumber(length)
        val formattedWidth = formatNumber(width)
        val formattedArea = formatNumber(area)
        val formattedPerimeter = formatNumber(perimeter)

        lengthResult.text = "Panjang = $formattedLength ${selectedUnit.symbol}"
        widthResult.text = "Lebar = $formattedWidth ${selectedUnit.symbol}"
        solutionResult.text = "Luas = $formattedLength × $formattedWidth = $formattedArea ${selectedUnit.symbol}²"
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
                === HASIL PERHITUNGAN PERSEGI PANJANG ===
                Panjang: ${formatNumber(result.length)} ${selectedUnit.symbol}
                Lebar: ${formatNumber(result.width)} ${selectedUnit.symbol}
                Luas: ${formatNumber(result.area)} ${selectedUnit.symbol}²
                Keliling: ${formatNumber(result.perimeter)} ${selectedUnit.symbol}
                Rumus Luas: Panjang × Lebar
                Rumus Keliling: 2 × (Panjang + Lebar)
            """.trimIndent()

            clipboard.setPrimaryClip(ClipData.newPlainText("Hasil Persegi Panjang", resultText))
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