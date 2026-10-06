package com.example.pengaturanprofil

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.widget.doOnTextChanged

class MainActivity : AppCompatActivity() {

    companion object {
        private const val NAMA_PREFS = "profil_prefs"

        // studi kasus
        private const val KEY_NAMA   = "nama"
        private const val KEY_EMAIL  = "email"
        private const val KEY_BIO    = "bio"
        private const val KEY_AVATAR = "avatar"
        private const val KEY_GELAP  = "mode_gelap"

        // LATIHAN: kunci baru
        private const val KEY_PANGGILAN = "nama_panggilan"   // Latihan 1
        private const val KEY_KOTA      = "kota"             // Latihan 2
        private const val KEY_JUMLAH    = "jumlah_buka"      // Latihan 3

        private val DAFTAR_AVATAR = listOf(
            R.drawable.avatar_1, R.drawable.avatar_2, R.drawable.avatar_3
        )
    }

    private val prefs by lazy { getSharedPreferences(NAMA_PREFS, MODE_PRIVATE) }
    private var indeksAvatar = 0

    private lateinit var layoutUtama: View
    private lateinit var ivAvatar: ImageView
    private lateinit var daftarPilihan: List<ImageView>
    private lateinit var tvNamaKartu: TextView
    private lateinit var tvEmailKartu: TextView
    private lateinit var tvLabelAvatar: TextView
    private lateinit var etNama: EditText
    private lateinit var etEmail: EditText
    private lateinit var etBio: EditText
    private lateinit var swGelap: SwitchCompat

    // LATIHAN
    private lateinit var tvKotaKartu: TextView      // Latihan 2
    private lateinit var etKota: EditText           // Latihan 2
    private lateinit var tvJudulLatihan1: TextView  // Latihan 1
    private lateinit var tvSapaan: TextView         // Latihan 1
    private lateinit var etPanggilan: EditText      // Latihan 1
    private lateinit var tvJumlah: TextView         // Latihan 3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1) ambil semua view
        layoutUtama   = findViewById(R.id.layoutUtama)
        ivAvatar      = findViewById(R.id.ivAvatar)
        tvNamaKartu   = findViewById(R.id.tvNamaKartu)
        tvEmailKartu  = findViewById(R.id.tvEmailKartu)
        tvLabelAvatar = findViewById(R.id.tvLabelAvatar)
        etNama        = findViewById(R.id.etNama)
        etEmail       = findViewById(R.id.etEmail)
        etBio         = findViewById(R.id.etBio)
        swGelap       = findViewById(R.id.swGelap)
        daftarPilihan = listOf(
            findViewById(R.id.ivPilih1),
            findViewById(R.id.ivPilih2),
            findViewById(R.id.ivPilih3)
        )

        // LATIHAN: view tambahan
        tvKotaKartu     = findViewById(R.id.tvKotaKartu)
        etKota          = findViewById(R.id.etKota)
        tvJudulLatihan1 = findViewById(R.id.tvJudulLatihan1)
        tvSapaan        = findViewById(R.id.tvSapaan)
        etPanggilan     = findViewById(R.id.etPanggilan)
        tvJumlah        = findViewById(R.id.tvJumlah)

        // 2) aksi tombol & pilihan avatar
        daftarPilihan.forEachIndexed { i, iv -> iv.setOnClickListener { pilihAvatar(i) } }
        findViewById<Button>(R.id.btnSimpan).setOnClickListener { simpanProfil() }
        findViewById<Button>(R.id.btnReset).setOnClickListener { hapusSemuaData() }
        findViewById<Button>(R.id.btnSimpanPanggilan).setOnClickListener { simpanPanggilan() }   // Latihan 1

        // 3) kartu ikut berubah saat mengetik
        etNama.doOnTextChanged { _, _, _, _ -> perbaruiKartu() }
        etEmail.doOnTextChanged { _, _, _, _ -> perbaruiKartu() }
        etKota.doOnTextChanged { _, _, _, _ -> perbaruiKartu() }          // Latihan 2

        // mode gelap langsung berubah saat sakelar digeser
        swGelap.setOnCheckedChangeListener { _, isChecked -> terapkanModeGelap(isChecked) }

        // LATIHAN 3: tambah 1 setiap aplikasi benar-benar dibuka
        // (savedInstanceState == null -> bukan karena layar diputar)
        if (savedInstanceState == null) {
            val baru = prefs.getInt(KEY_JUMLAH, 0) + 1
            prefs.edit().putInt(KEY_JUMLAH, baru).apply()
        }

        // 4) tampilkan data tersimpan
        muatProfil()
    }

    // ===== menyimpan profil =====
    private fun simpanProfil() {
        prefs.edit()
            .putString(KEY_NAMA,   etNama.text.toString().trim())
            .putString(KEY_EMAIL,  etEmail.text.toString().trim())
            .putString(KEY_BIO,    etBio.text.toString().trim())
            .putString(KEY_KOTA,   etKota.text.toString().trim())     // Latihan 2: simpan
            .putInt(KEY_AVATAR,    indeksAvatar)
            .putBoolean(KEY_GELAP, swGelap.isChecked)
            .apply()

        terapkanModeGelap(swGelap.isChecked)
        Toast.makeText(this, getString(R.string.pesan_tersimpan), Toast.LENGTH_SHORT).show()
    }

    // ===== LATIHAN 1: simpan nama panggilan =====
    private fun simpanPanggilan() {
        val panggilan = etPanggilan.text.toString().trim()
        if (panggilan.isEmpty()) {
            Toast.makeText(this, getString(R.string.pesan_panggilan_kosong), Toast.LENGTH_SHORT).show()
            return
        }
        prefs.edit()
            .putString(KEY_PANGGILAN, panggilan)
            .apply()
        Toast.makeText(this, getString(R.string.pesan_panggilan_tersimpan), Toast.LENGTH_SHORT).show()
        tampilkanSapaan()
    }

    // ===== membaca =====
    private fun muatProfil() {
        val nama  = prefs.getString(KEY_NAMA, "") ?: ""
        val email = prefs.getString(KEY_EMAIL, "") ?: ""
        val bio   = prefs.getString(KEY_BIO, "") ?: ""
        val kota  = prefs.getString(KEY_KOTA, "") ?: ""               // Latihan 2: baca
        indeksAvatar = prefs.getInt(KEY_AVATAR, 0).coerceIn(0, DAFTAR_AVATAR.lastIndex)
        val gelap = prefs.getBoolean(KEY_GELAP, false)

        etNama.setText(nama)
        etEmail.setText(email)
        etBio.setText(bio)
        etKota.setText(kota)
        ivAvatar.setImageResource(DAFTAR_AVATAR[indeksAvatar])
        tandaiPilihan()
        swGelap.isChecked = gelap

        terapkanModeGelap(gelap)
        perbaruiKartu()

        // LATIHAN 1 dan 3
        tampilkanSapaan()
        tampilkanJumlah()
    }

    // LATIHAN 1: baca nama panggilan, tampilkan di TextView
    private fun tampilkanSapaan() {
        val panggilan = prefs.getString(KEY_PANGGILAN, "") ?: ""
        etPanggilan.setText(panggilan)
        tvSapaan.text = if (panggilan.isEmpty()) {
            getString(R.string.sapaan_kosong)
        } else {
            getString(R.string.sapaan_isi, panggilan)
        }
    }

    // LATIHAN 3: baca jumlah buka, tampilkan di TextView
    private fun tampilkanJumlah() {
        val jumlah = prefs.getInt(KEY_JUMLAH, 0)
        tvJumlah.text = getString(R.string.teks_jumlah, jumlah)
    }

    // ===== menghapus =====
    private fun hapusSemuaData() {
        prefs.edit().clear().apply()      // semua kunci ikut terhapus, termasuk latihan
        Toast.makeText(this, getString(R.string.pesan_reset), Toast.LENGTH_SHORT).show()
        muatProfil()
    }

    // ===== tampilan =====
    private fun pilihAvatar(indeks: Int) {
        indeksAvatar = indeks
        ivAvatar.setImageResource(DAFTAR_AVATAR[indeksAvatar])
        tandaiPilihan()
    }

    private fun tandaiPilihan() {
        daftarPilihan.forEachIndexed { i, iv ->
            iv.setBackgroundResource(if (i == indeksAvatar) R.drawable.bg_avatar_terpilih else 0)
        }
    }

    private fun perbaruiKartu() {
        val nama  = etNama.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val kota  = etKota.text.toString().trim()
        tvNamaKartu.text  = nama.ifEmpty { getString(R.string.nama_default) }
        tvEmailKartu.text = email.ifEmpty { getString(R.string.email_default) }
        tvKotaKartu.text  = kota.ifEmpty { getString(R.string.kota_default) }   // Latihan 2: tampil
    }

    private fun terapkanModeGelap(aktif: Boolean) {
        val latar     = Color.parseColor(if (aktif) "#0F172A" else "#F8FAFC")
        val teksUtama = Color.parseColor(if (aktif) "#F1F5F9" else "#1B2430")
        val teksSamar = Color.parseColor(if (aktif) "#94A3B8" else "#64748B")

        layoutUtama.setBackgroundColor(latar)
        tvNamaKartu.setTextColor(teksUtama)
        tvEmailKartu.setTextColor(teksSamar)
        tvKotaKartu.setTextColor(teksSamar)
        tvLabelAvatar.setTextColor(teksSamar)
        swGelap.setTextColor(teksUtama)

        // LATIHAN: ikut diwarnai
        tvJudulLatihan1.setTextColor(teksSamar)
        tvSapaan.setTextColor(teksUtama)
        tvJumlah.setTextColor(teksSamar)

        listOf(etNama, etEmail, etBio, etKota, etPanggilan).forEach {
            it.setTextColor(teksUtama)
            it.setHintTextColor(teksSamar)
            it.setBackgroundResource(if (aktif) R.drawable.bg_input_gelap else R.drawable.bg_input)
        }
    }
}