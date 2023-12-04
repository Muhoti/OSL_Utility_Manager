package ke.co.osl.umcollector.Forms

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.auth0.android.jwt.JWT
import ke.co.osl.umcollector.*
import ke.co.osl.umcollector.api.ApiInterface
import ke.co.osl.umcollector.models.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class LineProjects: AppCompatActivity() {
    lateinit var user:TextView
    lateinit var preferences: SharedPreferences
    lateinit var editor: SharedPreferences.Editor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lineproject)

        preferences = this.getSharedPreferences("ut_manager", MODE_PRIVATE)
        editor = preferences.edit()

        user = findViewById(R.id.user)

        val jwt = JWT(preferences.getString("token","gsdhjdsajfdsjkfdjk:gsdhjsdhjsdjhsdsdfjhsdfjh:ghsdghdsghvgdsh")!!)
        if (jwt.expiresAt!!.before(Date())) {
            startActivity(Intent(this, LoginPage::class.java))
            finish()
        }else {
            user.text = jwt.getClaim("Name").asString()
        }

        val back = findViewById<ImageView>(R.id.back)
        back.setOnClickListener {
            startActivity(Intent(this, Home::class.java))
        }

        chooseAction ()
    }

    private fun chooseAction() {
        postFormDetails()
    }

    private fun postFormDetails(){
        val next = findViewById<Button>(R.id.next)
        val error = findViewById<TextView>(R.id.error)
        val progress = findViewById<ProgressBar>(R.id.progress)
        val project_name = findViewById<EditText>(R.id.project_name)
        val material = findViewById<EditText>(R.id.material)
        val intake = findViewById<EditText>(R.id.intake)
        val type = findViewById<EditText>(R.id.type)
        val dma = findViewById<EditText>(R.id.dma)
        val schemeName = findViewById<EditText>(R.id.schemeName)
        val route = findViewById<EditText>(R.id.route)
        val zone = findViewById<Spinner>(R.id.zone)
        val size = findViewById<EditText>(R.id.size)

        next.setOnClickListener {
            error.text = ""
            progress.visibility = View.VISIBLE

            val projectslineupdateBody = ProjectsLinesUpdateBody(
                project_name.text.toString(),
                material.text.toString(),
                intake.text.toString(),
                type.text.toString(),
                dma.text.toString(),
                schemeName.text.toString(),
                route.text.toString(),
                zone.selectedItem.toString(),
                size.text.toString(),
                user.text.toString()
            )

            val ID =intent.getStringExtra("ID")
            val apiInterface = ApiInterface.create().putProjectLines(ID!!, projectslineupdateBody)

            apiInterface.enqueue( object : Callback<Message> {
                override fun onResponse(call: Call<Message>?, response: Response<Message>?) {
                    progress.visibility = View.GONE
                    System.out.println(response?.body())
                    if(response?.body()?.success !== null){
                        error.text = response?.body()?.success

                        val countdonwtimer = object: CountDownTimer(2000,1){
                            override fun onTick(p0: Long) {
                            }
                            override fun onFinish() {
                                progress.visibility = View.GONE
                                startActivity(Intent(this@LineProjects, Home::class.java))
                                finish()
                            }
                        }
                        countdonwtimer.start()
                    }
                    else {
                        error.text = response?.body()?.error
                    }
                }
                override fun onFailure(call: Call<Message>?, t: Throwable?) {
                    progress.visibility = View.GONE
                    error.text = "Connection to server failed"
                }
            })
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val i = Intent(this, PointHome::class.java)
        startActivity(i)
        finish()
    }

}