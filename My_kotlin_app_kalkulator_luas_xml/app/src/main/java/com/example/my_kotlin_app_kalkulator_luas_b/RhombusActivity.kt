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

data class RhombusResult(
    val diagonal1: Double,
    val diagonal2: Double,
    val area: Double
)

class RhombusActivity : AppCompatActivity() {

    private lateinit var backButton: ImageButton
    private lateinit var unitLabel: TextView
    private lateinit var changeUnitButton: Button
    private lateinit var diagonal1Input: EditText
    private lateinit var clearDiagonal1Button: Button
    private lateinit var diagonal2Input: EditText
    private lateinit var clearDiagonal2Button: Button
    private lateinit var calculateButton: com.google.android.material.button.MaterialButton
    private lateinit var resultCard: androidx.cardview.widget.CardView
    private lateinit var copyButton: Button
    private lateinit var diagonal1Result: TextView
    private lateinit var diagonal2Result: TextView
    private lateinit var solutionResult: TextView
    private lateinit var finalResult: TextView

    private var selectedUnit = Unit.CM
    private var currentResult: RhombusResult? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rhombus)

        initializeViews()
        setupListeners()
        updateUnitDisplay()

        resultCard.visibility = View.GONE
    }

    private fun initializeViews() {
        backButton = findViewById(R.id.back_button)
        unitLabel = findViewById(R.id.unit_label)
        changeUnitButton = findViewById(R.id.change_unit_button)
        diagonal1Input = findViewById(R.id.diagonal1_input)
        clearDiagonal1Button = findViewById(R.id.clear_diagonal1_button)
        diagonal2Input = findViewById(R.id.diagonal2_input)
        clearDiagonal2Button = findViewById(R.id.clear_diagonal2_button)
        calculateButton = findViewById(R.id.calculate_button)
        resultCard = findViewById(R.id.result_card)
        copyButton = findViewById(R.id.copy_button)
        diagonal1Result = findViewById(R.id.diagonal1_result)
        diagonal2Result = findViewById(R.id.diagonal2_result)
        solutionResult = findViewById(R.id.solution_result)
        finalResult = findViewById(R.id.final_result)
    }

    private fun setupListeners() {
        backButton.setOnClickListener { onBackPressed() }

        changeUnitButton.setOnClickListener { showUnitPicker() }

        clearDiagonal1Button.setOnClickListener {
            diagonal1Input.text.clear()
            clearDiagonal1Button.visibility = View.GONE
            if (resultCard.visibility == View.VISIBLE) hideResult()
        }

        clearDiagonal2Button.setOnClickListener {
            diagonal2Input.text.clear()
            clearDiagonal2Button.visibility = View.GONE
            if (resultCard.visibility == View.VISIBLE) hideResult()
        }

        calculateButton.setOnClickListener { calculateRhombusArea() }

        copyButton.setOnClickListener { copyResultToClipboard() }

        diagonal1Input.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                clearDiagonal1Button.visibility = if (!s.isNullOrEmpty()) View.VISIBLE else View.GONE
                if (resultCard.visibility == View.VISIBLE) hideResult()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        diagonal2Input.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                clearDiagonal2Button.visibility = if (!s.isNullOrEmpty()) View.VISIBLE else View.GONE
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
        diagonal1Input.hint = "contoh: 5, 8.9"
        diagonal2Input.hint = "contoh: 7, 10.5"
    }

    private fun calculateRhombusArea() {
        val diagonal1Text = diagonal1Input.text.toString().trim()
        val diagonal2Text = diagonal2Input.text.toString().trim()

        if (diagonal1Text.isEmpty() || diagonal2Text.isEmpty()) {
            showError("Diagonal 1 dan Diagonal 2 tidak boleh kosong")
            return
        }

        val diagonal1 = diagonal1Text.replace(",", ".").toDoubleOrNull()
        val diagonal2 = diagonal2Text.replace(",", ".").toDoubleOrNull()

        if (diagonal1 == null || diagonal2 == null) {
            showError("Masukkan angka yang valid")
            return
        }

        when {
            diagonal1 <= 0 || diagonal2 <= 0 -> {
                showError("Diagonal 1 dan Diagonal 2 harus lebih besar dari 0")
                return
            }
            diagonal1 > 1_000_000 || diagonal2 > 1_000_000 -> {
                showError("Nilai terlalu besar (maksimal 1,000,000)")
                return
            }
            diagonal1 < 0.0001 || diagonal2 < 0.0001 -> {
                showError("Nilai terlalu kecil (minimal 0.0001)")
                return
            }
        }

        val area = 0.5 * diagonal1 * diagonal2
        currentResult = RhombusResult(diagonal1, diagonal2, area)

        displayResult(diagonal1, diagonal2, area)
        showResult()
    }

    private fun displayResult(diagonal1: Double, diagonal2: Double, area: Double) {
        val formattedDiagonal1 = formatNumber(diagonal1)
        val formattedDiagonal2 = formatNumber(diagonal2)
        val formattedArea = formatNumber(area)

        diagonal1Result.text = "Diagonal 1 (d1) = $formattedDiagonal1 ${selectedUnit.symbol}"
        diagonal2Result.text = "Diagonal 2 (d2) = $formattedDiagonal2 ${selectedUnit.symbol}"
        solutionResult.text = "Luas = 0.5 × d1 × d2 = 0.5 × $formattedDiagonal1 × $formattedDiagonal2"
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
                === HASIL PERHITUNGAN BELAH KETUPAT ===
                Diagonal 1 (d1): ${formatNumber(result.diagonal1)} ${selectedUnit.symbol}
                Diagonal 2 (d2): ${formatNumber(result.diagonal2)} ${selectedUnit.symbol}
                Luas: ${formatNumber(result.area)} ${selectedUnit.symbol}²
                Rumus: Luas = 0.5 × d1 × d2
                Perhitungan: Luas = 0.5 × ${formatNumber(result.diagonal1)} × ${formatNumber(result.diagonal2)}
            """.trimIndent()

            clipboard.setPrimaryClip(ClipData.newPlainText("Hasil Belah Ketupat", resultText))
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