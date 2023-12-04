package ke.co.osl.umcollector.Forms

import android.app.Dialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.CountDownTimer
import android.text.TextUtils
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.auth0.android.jwt.JWT
import ke.co.osl.umcollector.Home
import ke.co.osl.umcollector.LoginPage
import ke.co.osl.umcollector.PointHome
import ke.co.osl.umcollector.R
import ke.co.osl.umcollector.api.ApiInterface
import ke.co.osl.umcollector.models.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class PRV: AppCompatActivity() {
    lateinit var user:TextView
    lateinit var preferences: SharedPreferences
    lateinit var editor: SharedPreferences.Editor
    lateinit var dialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_prv)

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

        dialog = Dialog(this)
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setContentView(R.layout.searchfeature)


        var isUpdating = intent.getBooleanExtra("isUpdating", false)

        chooseAction(isUpdating)
    }

    private fun chooseAction(editing: Boolean) {
        if(editing) {
            showDialog()
        }
        else {
            postFormDetails()
        }
    }

    private fun showDialog() {
        searchPRV(dialog)
        dialog.getWindow()?.setBackgroundDrawableResource(R.drawable.background_transparent);
        dialog.show()
    }

    private fun searchPRV(d: Dialog) {
        val searchtitle = d.findViewById<TextView>(R.id.searchtitle)
        val submit = d.findViewById<Button>(R.id.submit)
        val error = d.findViewById<TextView>(R.id.error)
        val searchId = d.findViewById<EditText>(R.id.searchId)
        val progress = d.findViewById<ProgressBar>(R.id.progress)

        searchtitle.text = "Search by ObjectID"

        submit.setOnClickListener {

            progress.visibility = View.VISIBLE

            if (TextUtils.isEmpty(searchtitle.text.toString())) {
                progress.visibility = View.GONE
                error.text = "Enter Object ID here to search!"
                return@setOnClickListener
            }

            val apiInterface = ApiInterface.create().searchPRV(searchId.text.toString())

            apiInterface.enqueue( object : Callback<List<PRVGetBody>> {
                override fun onResponse(call: Call<List<PRVGetBody>>, response: Response<List<PRVGetBody>>?) {
                    progress.visibility = View.GONE
                    if(response?.body() !== null && response?.body()?.size!! > 0) {
                        System.out.println(response.body())
                        dialog.hide()
                        prefillForms(response?.body()?.get(0)!!)

                    }else {
                        error.text = "Feature not found!"
                    }
                }
                override fun onFailure(call: Call<List<PRVGetBody>>, t: Throwable) {
                    progress.visibility = View.GONE
                    System.out.println(t)
                    error.text = "Connection to server failed"
                }
            })

        }
    }


    private fun postFormDetails(){
        val next = findViewById<Button>(R.id.next)
        val error = findViewById<TextView>(R.id.error)
        val progress = findViewById<ProgressBar>(R.id.progress)
        val name = findViewById<EditText>(R.id.name)
        val location = findViewById<EditText>(R.id.location)
        val dma = findViewById<EditText>(R.id.dma)
        val remarks = findViewById<EditText>(R.id.remarks)
        val route = findViewById<EditText>(R.id.route)
        val zone = findViewById<EditText>(R.id.zone)
        val size = findViewById<EditText>(R.id.size)
        val lat = intent.getDoubleExtra("lat",0.0)
        val lng = intent.getDoubleExtra("lng",0.0)

        next.setOnClickListener {
            error.text = ""

            progress.visibility = View.VISIBLE
            val prvbody = PRVBody(
                name.text.toString(),
                size.text.toString(),
                route.text.toString(),
                zone.text.toString(),
                dma.text.toString(),
                location.text.toString(),
                remarks.text.toString(),
                user.text.toString(),
                lng,
                lat,
            )
            val apiInterface = ApiInterface.create().postPRV(prvbody)

            apiInterface.enqueue( object : Callback<Message> {
                override fun onResponse(call: Call<Message>?, response: Response<Message>?) {
                    progress.visibility = View.GONE
                    System.out.println(response?.body())
                    if(response?.body()?.success !== null){
                        error.text = response?.body()?.success
                        val intent = Intent(this@PRV, Home::class.java)
                        startActivity(intent)
                    }
                    else {
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

    private fun prefillForms (body: PRVGetBody) {
        val next = findViewById<Button>(R.id.next)
        val error = findViewById<TextView>(R.id.error)
        val progress = findViewById<ProgressBar>(R.id.progress)
        val name = findViewById<EditText>(R.id.name)
        val location = findViewById<EditText>(R.id.location)
        val dma = findViewById<EditText>(R.id.dma)
        val remarks = findViewById<EditText>(R.id.remarks)
        val route = findViewById<EditText>(R.id.route)
        val zone = findViewById<EditText>(R.id.zone)
        val size = findViewById<EditText>(R.id.size)

        next.text = "Update"

        //Bind data
        name.setText(body.Name)
        location.setText(body.Location)
        dma.setText(body.DMA)
        remarks.setText(body.Remarks)
        route.setText(body.Route)
        zone.setText(body.Zone)
        size.setText(body.Size)

        val id = body.ID
        val objectid = body.ObjectID

        next.setOnClickListener {
            error.text = ""
            progress.visibility = View.VISIBLE

            val prvbody = PRVGetBody(
                id,
                objectid,
                name.text.toString(),
                location.text.toString(),
                dma.text.toString(),
                route.text.toString(),
                zone.text.toString(),
                size.text.toString(),
                remarks.text.toString(),
                user.text.toString()
            )

            val apiInterface = ApiInterface.create().putPRV(body.ID,prvbody)
            apiInterface.enqueue( object : Callback<Message> {
                override fun onResponse(call: Call<Message>?, response: Response<Message>?) {

                    if(response?.body()?.success !== null){
                        error.text = response?.body()?.success
                        val countdonwtimer = object: CountDownTimer(3000,1){
                            override fun onTick(p0: Long) {
                            }
                            override fun onFinish() {
                                progress.visibility = View.GONE
                                startActivity(Intent(this@PRV, Home::class.java))
                                finish()
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