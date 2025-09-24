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

data class TrapeziumResult(
    val topBase: Double,
    val bottomBase: Double,
    val height: Double,
    val area: Double
)

class TrapeziumActivity : AppCompatActivity() {

    private lateinit var backButton: ImageButton
    private lateinit var unitLabel: TextView
    private lateinit var changeUnitButton: Button
    private lateinit var topBaseInput: EditText
    private lateinit var clearTopBaseButton: Button
    private lateinit var bottomBaseInput: EditText
    private lateinit var clearBottomBaseButton: Button
    private lateinit var heightInput: EditText
    private lateinit var clearHeightButton: Button
    private lateinit var calculateButton: com.google.android.material.button.MaterialButton
    private lateinit var resultCard: androidx.cardview.widget.CardView
    private lateinit var copyButton: Button
    private lateinit var topBaseResult: TextView
    private lateinit var bottomBaseResult: TextView
    private lateinit var heightResult: TextView
    private lateinit var solutionResult: TextView
    private lateinit var finalResult: TextView

    private var selectedUnit = Unit.CM
    private var currentResult: TrapeziumResult? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trapezium)

        initializeViews()
        setupListeners()
        updateUnitDisplay()

        resultCard.visibility = View.GONE
    }

    private fun initializeViews() {
        backButton = findViewById(R.id.back_button)
        unitLabel = findViewById(R.id.unit_label)
        changeUnitButton = findViewById(R.id.change_unit_button)
        topBaseInput = findViewById(R.id.top_base_input)
        clearTopBaseButton = findViewById(R.id.clear_top_base_button)
        bottomBaseInput = findViewById(R.id.bottom_base_input)
        clearBottomBaseButton = findViewById(R.id.clear_bottom_base_button)
        heightInput = findViewById(R.id.height_input)
        clearHeightButton = findViewById(R.id.clear_height_button)
        calculateButton = findViewById(R.id.calculate_button)
        resultCard = findViewById(R.id.result_card)
        copyButton = findViewById(R.id.copy_button)
        topBaseResult = findViewById(R.id.top_base_result)
        bottomBaseResult = findViewById(R.id.bottom_base_result)
        heightResult = findViewById(R.id.height_result)
        solutionResult = findViewById(R.id.solution_result)
        finalResult = findViewById(R.id.final_result)
    }

    private fun setupListeners() {
        backButton.setOnClickListener { onBackPressed() }

        changeUnitButton.setOnClickListener { showUnitPicker() }

        clearTopBaseButton.setOnClickListener {
            topBaseInput.text.clear()
            clearTopBaseButton.visibility = View.GONE
            if (resultCard.visibility == View.VISIBLE) hideResult()
        }

        clearBottomBaseButton.setOnClickListener {
            bottomBaseInput.text.clear()
            clearBottomBaseButton.visibility = View.GONE
            if (resultCard.visibility == View.VISIBLE) hideResult()
        }

        clearHeightButton.setOnClickListener {
            heightInput.text.clear()
            clearHeightButton.visibility = View.GONE
            if (resultCard.visibility == View.VISIBLE) hideResult()
        }

        calculateButton.setOnClickListener { calculateTrapeziumArea() }

        copyButton.setOnClickListener { copyResultToClipboard() }

        topBaseInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                clearTopBaseButton.visibility = if (!s.isNullOrEmpty()) View.VISIBLE else View.GONE
                if (resultCard.visibility == View.VISIBLE) hideResult()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        bottomBaseInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                clearBottomBaseButton.visibility = if (!s.isNullOrEmpty()) View.VISIBLE else View.GONE
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
        topBaseInput.hint = "contoh: 5, 8.9"
        bottomBaseInput.hint = "contoh: 7, 10.5"
        heightInput.hint = "contoh: 4, 6.5"
    }

    private fun calculateTrapeziumArea() {
        val topBaseText = topBaseInput.text.toString().trim()
        val bottomBaseText = bottomBaseInput.text.toString().trim()
        val heightText = heightInput.text.toString().trim()

        if (topBaseText.isEmpty() || bottomBaseText.isEmpty() || heightText.isEmpty()) {
            showError("Alas atas, alas bawah, dan tinggi tidak boleh kosong")
            return
        }

        val topBase = topBaseText.replace(",", ".").toDoubleOrNull()
        val bottomBase = bottomBaseText.replace(",", ".").toDoubleOrNull()
        val height = heightText.replace(",", ".").toDoubleOrNull()

        if (topBase == null || bottomBase == null || height == null) {
            showError("Masukkan angka yang valid")
            return
        }

        when {
            topBase <= 0 || bottomBase <= 0 || height <= 0 -> {
                showError("Alas atas, alas bawah, dan tinggi harus lebih besar dari 0")
                return
            }
            topBase > 1_000_000 || bottomBase > 1_000_000 || height > 1_000_000 -> {
                showError("Nilai terlalu besar (maksimal 1,000,000)")
                return
            }
            topBase < 0.0001 || bottomBase < 0.0001 || height < 0.0001 -> {
                showError("Nilai terlalu kecil (minimal 0.0001)")
                return
            }
        }

        val area = 0.5 * (topBase + bottomBase) * height
        currentResult = TrapeziumResult(topBase, bottomBase, height, area)

        displayResult(topBase, bottomBase, height, area)
        showResult()
    }

    private fun displayResult(topBase: Double, bottomBase: Double, height: Double, area: Double) {
        val formattedTopBase = formatNumber(topBase)
        val formattedBottomBase = formatNumber(bottomBase)
        val formattedHeight = formatNumber(height)
        val formattedArea = formatNumber(area)

        topBaseResult.text = "Alas atas = $formattedTopBase ${selectedUnit.symbol}"
        bottomBaseResult.text = "Alas bawah = $formattedBottomBase ${selectedUnit.symbol}"
        heightResult.text = "Tinggi = $formattedHeight ${selectedUnit.symbol}"
        solutionResult.text = "Luas = 0.5 × (alas atas + alas bawah) × tinggi = 0.5 × ($formattedTopBase + $formattedBottomBase) × $formattedHeight"
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
                === HASIL PERHITUNGAN TRAPESIUM ===
                Alas atas: ${formatNumber(result.topBase)} ${selectedUnit.symbol}
                Alas bawah: ${formatNumber(result.bottomBase)} ${selectedUnit.symbol}
                Tinggi: ${formatNumber(result.height)} ${selectedUnit.symbol}
                Luas: ${formatNumber(result.area)} ${selectedUnit.symbol}²
                Rumus: Luas = 0.5 × (alas atas + alas bawah) × tinggi
                Perhitungan: Luas = 0.5 × (${formatNumber(result.topBase)} + ${formatNumber(result.bottomBase)}) × ${formatNumber(result.height)}
            """.trimIndent()

            clipboard.setPrimaryClip(ClipData.newPlainText("Hasil Trapesium", resultText))
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