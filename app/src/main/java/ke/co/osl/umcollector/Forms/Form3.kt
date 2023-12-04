package ke.co.osl.umcollector.Forms

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.CountDownTimer
import android.text.TextUtils
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import ke.co.osl.umcollector.Home
import ke.co.osl.umcollector.PointHome
import ke.co.osl.umcollector.R
import ke.co.osl.umcollector.api.ApiInterface
import ke.co.osl.umcollector.models.Message
import ke.co.osl.umcollector.models.formBody3
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Form3: AppCompatActivity() {
    lateinit var user:TextView
    lateinit var preferences: SharedPreferences
    lateinit var editor: SharedPreferences.Editor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_form3)

        val back = findViewById<ImageView>(R.id.back)
        back.setOnClickListener {
            startActivity(Intent(this, PointHome::class.java))
        }

        var isUpdating = intent.getStringExtra("isUpdating")

        if (isUpdating != null) {
            chooseAction(isUpdating)
        }

        System.out.println("Now in form 3 isupdating is $isUpdating")

    }

    private fun chooseAction(isUpdating: String) {
        if(isUpdating == "false") {
            postFormDetails()
        } else if(isUpdating == "true") {
            prefillForms()
        }
    }

    private fun postFormDetails(){
        val next = findViewById<Button>(R.id.next)
        val error = findViewById<TextView>(R.id.error)
        val progress = findViewById<ProgressBar>(R.id.progress)
        val scheme = findViewById<Spinner>(R.id.schemeName)
        val zone = findViewById<Spinner>(R.id.zone)
        val dma = findViewById<EditText>(R.id.dma)
        val route = findViewById<EditText>(R.id.route)
        val location = findViewById<EditText>(R.id.location)
        val remarks = findViewById<EditText>(R.id.remarks)

        next.setOnClickListener {
            error.text = ""

            if (TextUtils.isEmpty(dma.text.toString())) {
                error.text = "DMA cannot be empty!"
                return@setOnClickListener
            }

            val lat = intent.getDoubleExtra("lat",0.0)
            val lng = intent.getDoubleExtra("lng",0.0)

            System.out.println(lat)
            System.out.println(lng)

            progress.visibility = View.VISIBLE
            val formBody = formBody3(
                scheme.selectedItem.toString(),
                zone.selectedItem.toString(),
                dma.text.toString(),
                route.text.toString(),
                location.text.toString(),
                remarks.text.toString(),
            )

            val id=intent.getStringExtra("id")

            val apiInterface = ApiInterface.create().postForm3(id!!,formBody)

            apiInterface.enqueue( object : Callback<Message> {
                override fun onResponse(call: Call<Message>?, response: Response<Message>?) {
                    progress.visibility = View.GONE
                    System.out.println(response?.body())
                    if(response?.body()?.success !== null){
                        error.text = response?.body()?.success
                        val countdonwtimer = object: CountDownTimer(3000,1){
                            override fun onTick(p0: Long) {
                            }
                            override fun onFinish() {
                                progress.visibility = View.GONE
                                val intent = Intent(this@Form3, Home::class.java)
                                startActivity(intent)
                            }
                        }
                        countdonwtimer.start()
                    }

                    else {
                        progress.visibility = View.GONE
                        error.text = response?.body()?.error
                    }
                }
                override fun onFailure(call: Call<Message>?, t: Throwable?) {
                    System.out.println(t)
                    progress.visibility = View.GONE
                    error.text = "Connection to server failed"
                }
            })
        }

    }

    private fun prefillForms () {
        val next = findViewById<Button>(R.id.next)
        val error = findViewById<TextView>(R.id.error)
        val progress = findViewById<ProgressBar>(R.id.progress)
        val scheme = findViewById<Spinner>(R.id.schemeName)
        val zone = findViewById<Spinner>(R.id.zone)
        val dma = findViewById<EditText>(R.id.dma)
        val route = findViewById<EditText>(R.id.route)
        val location = findViewById<EditText>(R.id.location)
        val remarks = findViewById<EditText>(R.id.remarks)
        next.text = "Update"

        //Bind data
        updateSpinner(scheme,intent.getStringExtra("SchemeName"))
        updateSpinner(zone,intent.getStringExtra("Zone"))
        dma.setText(intent.getStringExtra("DMA"))
        route.setText(intent.getStringExtra("Route"))
        location.setText(intent.getStringExtra("Location"))
        remarks.setText(intent.getStringExtra("Remarks"))

        next.setOnClickListener {
            error.text = ""

            progress.visibility = View.VISIBLE
            val formBody = formBody3(
                zone.selectedItem.toString(),
                route.text.toString(),
                dma.text.toString(),
                location.text.toString(),
                scheme.selectedItem.toString(),
                remarks.text.toString(),
            )

            val id=intent.getStringExtra("id")
            val apiInterface = ApiInterface.create().postForm3(id!!,formBody)

            apiInterface.enqueue( object : Callback<Message> {
                override fun onResponse(call: Call<Message>?, response: Response<Message>?) {
                    progress.visibility = View.GONE
                    System.out.println(response?.body())
                    if(response?.body()?.success !== null){
                        error.text = response?.body()?.success
                        val countdonwtimer = object: CountDownTimer(3000,1){
                            override fun onTick(p0: Long) {
                            }
                            override fun onFinish() {
                                progress.visibility = View.GONE
                                val intent = Intent(this@Form3, Home::class.java)
                                startActivity(intent)
                            }
                        }
                        countdonwtimer.start()
                    }

                    else {
                        progress.visibility = View.GONE
                        error.text = response?.body()?.error
                    }
                }
                override fun onFailure(call: Call<Message>?, t: Throwable?) {
                    System.out.println(t)
                    progress.visibility = View.GONE
                    error.text = "Connection to server failed"
                }
            })
        }

    }

    override fun onBackPressed() {
        super.onBackPressed()
        val i = Intent(this, Home::class.java)
        startActivity(i)
        finish()
    }

    fun updateSpinner(spinner: Spinner, value: String?) {
        val myAdap: ArrayAdapter<String> =
            spinner.getAdapter() as ArrayAdapter<String>
        val spinnerPosition = myAdap.getPosition(value)
        spinner.setSelection(spinnerPosition);
    }

}