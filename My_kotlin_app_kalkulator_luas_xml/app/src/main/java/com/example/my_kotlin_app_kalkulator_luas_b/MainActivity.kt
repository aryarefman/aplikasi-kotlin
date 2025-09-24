package com.example.kalkulatorgeometri

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.bumptech.glide.Glide

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val eduLink1 = findViewById<CardView>(R.id.edu_link_1)
        val eduLink2 = findViewById<CardView>(R.id.edu_link_2)
        val eduLink3 = findViewById<CardView>(R.id.edu_link_3)
        val eduLink4 = findViewById<CardView>(R.id.edu_link_4)
        val eduLink5 = findViewById<CardView>(R.id.edu_link_5)

        val thumbnail1 = findViewById<ImageView>(R.id.thumbnail_1)
        val thumbnail2 = findViewById<ImageView>(R.id.thumbnail_2)
        val thumbnail3 = findViewById<ImageView>(R.id.thumbnail_3)
        val thumbnail4 = findViewById<ImageView>(R.id.thumbnail_4)
        val thumbnail5 = findViewById<ImageView>(R.id.thumbnail_5)

        loadThumbnail(thumbnail1, "zqndUSoY3rY")
        loadThumbnail(thumbnail2, "8foN4sm0GQE")
        loadThumbnail(thumbnail3, "zQMby2EU4xA")
        loadThumbnail(thumbnail4, "NSlVOk3gwFQ")
        loadThumbnail(thumbnail5, "QGFlBbDXmwY")

        eduLink1.setOnClickListener {
            openYouTubeVideo("https://www.youtube.com/watch?v=zqndUSoY3rY")
            Toast.makeText(this, "Opening Tutorial Luas Persegi", Toast.LENGTH_SHORT).show()
        }

        eduLink2.setOnClickListener {
            openYouTubeVideo("https://www.youtube.com/watch?v=8foN4sm0GQE")
            Toast.makeText(this, "Opening Tutorial Luas Segitiga", Toast.LENGTH_SHORT).show()
        }

        eduLink3.setOnClickListener {
            openYouTubeVideo("https://www.youtube.com/watch?v=zQMby2EU4xA")
            Toast.makeText(this, "Opening Tutorial Luas Lingkaran", Toast.LENGTH_SHORT).show()
        }

        eduLink4.setOnClickListener {
            openYouTubeVideo("https://www.youtube.com/watch?v=NSlVOk3gwFQ")
            Toast.makeText(this, "Opening Tutorial Luas Belah Ketupat", Toast.LENGTH_SHORT).show()
        }

        eduLink5.setOnClickListener {
            openYouTubeVideo("https://www.youtube.com/watch?v=QGFlBbDXmwY")
            Toast.makeText(this, "Opening Tutorial Luas Elips", Toast.LENGTH_SHORT).show()
        }

        // Card navigasi ke Activity per bangun datar
        val cardLingkaran = findViewById<CardView>(R.id.card_lingkaran)
        val cardElips = findViewById<CardView>(R.id.card_elips)
        val cardPersegi = findViewById<CardView>(R.id.card_persegi)
        val cardPersegiPanjang = findViewById<CardView>(R.id.card_persegi_panjang)
        val cardSegitiga = findViewById<CardView>(R.id.card_segitiga)
        val cardJajarGenjang = findViewById<CardView>(R.id.card_jajar_genjang)
        val cardTrapesium = findViewById<CardView>(R.id.card_trapesium)
        val cardLayangLayang = findViewById<CardView>(R.id.card_layang_layang)
        val cardBelahKetupat = findViewById<CardView>(R.id.card_belah_ketupat)

        cardLingkaran.setOnClickListener {
            Toast.makeText(this, "Lingkaran dipilih", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, CircleActivity::class.java))
        }

        cardElips.setOnClickListener {
            Toast.makeText(this, "Elips dipilih", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, EllipsActivity::class.java))
        }

        cardPersegi.setOnClickListener {
            Toast.makeText(this, "Persegi dipilih", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, SquareActivity::class.java))
        }

        cardPersegiPanjang.setOnClickListener {
            Toast.makeText(this, "Persegi Panjang dipilih", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, RectangleActivity::class.java))
        }

        cardSegitiga.setOnClickListener {
            Toast.makeText(this, "Segitiga dipilih", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, TriangleActivity::class.java))
        }

        cardJajarGenjang.setOnClickListener {
            Toast.makeText(this, "Jajar Genjang dipilih", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, ParallelogramActivity::class.java))
        }

        cardTrapesium.setOnClickListener {
            Toast.makeText(this, "Trapesium dipilih", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, TrapeziumActivity::class.java))
        }

        cardLayangLayang.setOnClickListener {
            Toast.makeText(this, "Layang-layang dipilih", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, KiteActivity::class.java))
        }

        cardBelahKetupat.setOnClickListener {
            Toast.makeText(this, "Belah Ketupat dipilih", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, RhombusActivity::class.java))
        }
    }

    private fun loadThumbnail(imageView: ImageView, videoId: String) {
        val thumbnailUrl = "https://img.youtube.com/vi/$videoId/maxresdefault.jpg"
        Glide.with(this)
            .load(thumbnailUrl)
            .error(R.drawable.ic_launcher_background)
            .into(imageView)
    }

    private fun openYouTubeVideo(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        intent.setPackage("com.google.android.youtube")
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            intent.setPackage(null)
            startActivity(intent)
        }
    }
}