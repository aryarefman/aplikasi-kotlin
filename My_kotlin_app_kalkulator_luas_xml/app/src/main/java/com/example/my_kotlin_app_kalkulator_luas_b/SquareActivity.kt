package com.example.kalkulatorgeometri

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.text.DecimalFormat

// Add this enum class
enum class Unit(val displayName: String, val symbol: String) {
    MM("Milimeter", "mm"),
    CM("Sentimeter", "cm"),
    M("Meter", "m"),
    KM("Kilometer", "km"),
    IN("Inci", "in"),
    FT("Kaki", "ft")
}

data class SquareResult(
    val side: Double,
    val area: Double
)

class SquareActivity : AppCompatActivity() {

    // UI Components - exact same as before
    private lateinit var backButton: ImageButton
    private lateinit var unitLabel: TextView
    private lateinit var changeUnitButton: Button
    private lateinit var sideInput: EditText
    private lateinit var clearButton: Button
    private lateinit var calculateButton: Button
    private lateinit var resultCard: androidx.cardview.widget.CardView
    private lateinit var copyButton: Button
    private lateinit var sideResult: TextView
    private lateinit var solutionResult: TextView
    private lateinit var finalResult: TextView

    // Variables
    private var selectedUnit = Unit.CM
    private var currentResult: SquareResult? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_square)

        try {
            initializeViews()
            setupListeners()
            updateUnitDisplay()
            setupBackPressedCallback()

            // Result card hidden at first
            resultCard.visibility = View.GONE
        } catch (e: Exception) {
            Toast.makeText(this, "Error initializing: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun initializeViews() {
        backButton = findViewById(R.id.back_button)
        unitLabel = findViewById(R.id.unit_label)
        changeUnitButton = findViewById(R.id.change_unit_button)
        sideInput = findViewById(R.id.side_input)
        clearButton = findViewById(R.id.clear_side_button)
        calculateButton = findViewById(R.id.calculate_button)
        resultCard = findViewById(R.id.result_card)
        copyButton = findViewById(R.id.copy_button)
        sideResult = findViewById(R.id.side_result)
        solutionResult = findViewById(R.id.solution_result)
        finalResult = findViewById(R.id.final_result)
    }

    private fun setupListeners() {
        backButton.setOnClickListener { finish() }

        changeUnitButton.setOnClickListener { showUnitPicker() }

        clearButton.setOnClickListener {
            sideInput.text.clear()
            hideResult()
        }

        calculateButton.setOnClickListener { calculateSquareArea() }

        copyButton.setOnClickListener { copyResultToClipboard() }

        sideInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                clearButton.visibility = if (!s.isNullOrEmpty()) View.VISIBLE else View.GONE
                if (resultCard.visibility == View.VISIBLE) hideResult()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupBackPressedCallback() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
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
        sideInput.hint = "contoh: 5, 8.9"
    }

    private fun calculateSquareArea() {
        val sideText = sideInput.text.toString().trim()

        if (sideText.isEmpty()) {
            showError("Sisi tidak boleh kosong")
            return
        }

        val side = sideText.replace(",", ".").toDoubleOrNull()
        if (side == null) {
            showError("Masukkan angka yang valid")
            return
        }

        when {
            side <= 0 -> {
                showError("Sisi harus lebih besar dari 0")
                return
            }
            side > 1_000_000 -> {
                showError("Nilai terlalu besar (maksimal 1,000,000)")
                return
            }
            side < 0.0001 -> {
                showError("Nilai terlalu kecil (minimal 0.0001)")
                return
            }
        }

        val area = side * side
        currentResult = SquareResult(side, area)

        displayResult(side, area)
        showResult()
    }

    private fun displayResult(side: Double, area: Double) {
        val formattedSide = formatNumber(side)
        val formattedArea = formatNumber(area)

        sideResult.text = "Sisi = $formattedSide ${selectedUnit.symbol}"
        solutionResult.text = "Luas = sisi × sisi = $formattedSide × $formattedSide"
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
            try {
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val resultText = """
                    === HASIL PERHITUNGAN PERSEGI ===
                    Sisi: ${formatNumber(result.side)} ${selectedUnit.symbol}
                    Luas: ${formatNumber(result.area)} ${selectedUnit.symbol}²
                    Rumus: Luas = sisi × sisi
                    Perhitungan: Luas = ${formatNumber(result.side)} × ${formatNumber(result.side)}
                """.trimIndent()

                clipboard.setPrimaryClip(ClipData.newPlainText("Hasil Persegi", resultText))
                Toast.makeText(this, "Hasil disalin ke clipboard", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this, "Gagal menyalin hasil", Toast.LENGTH_SHORT).show()
            }
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
}