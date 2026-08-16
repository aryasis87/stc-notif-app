package id.stcautotrade.notif

import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.security.MessageDigest

// Gerbang password di layar awal — mencegah orang lain memakai app.
// Password TIDAK disimpan sebagai teks; hanya hash SHA-256-nya yang ditanam &
// dibandingkan. Wajib cocok untuk masuk ke layar utama.
class LockActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lock)

        val et = findViewById<EditText>(R.id.etPass)
        val err = findViewById<TextView>(R.id.tvErr)
        val btn = findViewById<Button>(R.id.btnUnlock)

        fun tryUnlock() {
            if (sha256(et.text.toString()) == PASS_HASH) {
                startActivity(Intent(this, MainActivity::class.java))
                overridePendingTransition(0, 0)
                finish()
            } else {
                err.text = "Password salah."
                et.setText("")
            }
        }

        btn.setOnClickListener { tryUnlock() }
        et.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_GO) {
                tryUnlock(); true
            } else false
        }
    }

    private fun sha256(s: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(s.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }

    companion object {
        // SHA-256("Aryasis87@")
        private const val PASS_HASH =
            "1a5ff7c4a505855ffec803d46b21cbbe99de8ade188218f8e1c916866001112d"
    }
}
