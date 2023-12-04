package ke.co.osl.umcollector

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity

class LandingPage: AppCompatActivity() {
    lateinit var preferences: SharedPreferences
    lateinit var editor: SharedPreferences.Editor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_landing)

        preferences = this.getSharedPreferences("ut_manager", MODE_PRIVATE)
        editor = preferences.edit()

        val timer = object: CountDownTimer(5000, 1) {
            override fun onTick(millisUntilFinished: Long) {

            }

            override fun onFinish() {
                val token = preferences.getString("publictoken", null)
                System.out.println("the token is $token")
                if (token == null){
                    startActivity(Intent(this@LandingPage,PublicLoginPage::class.java))
                    finish()
                } else {
                    startActivity(Intent(this@LandingPage,Incidences::class.java))
                    finish()
                }

            }
        }
        timer.start()
    }
}


