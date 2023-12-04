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
import ke.co.osl.umcollector.*
import ke.co.osl.umcollector.api.ApiInterface
import ke.co.osl.umcollector.models.Message
import ke.co.osl.umcollector.models.TankBody
import ke.co.osl.umcollector.models.TankGetBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

//12713082

class Tanks: AppCompatActivity() {
    lateinit var user:TextView
    lateinit var preferences: SharedPreferences
    lateinit var editor: SharedPreferences.Editor
    lateinit var dialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tanks_form)

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
        searchTanks(dialog)
        dialog.getWindow()?.setBackgroundDrawableResource(R.drawable.background_transparent);
        dialog.show()
    }

    private fun searchTanks(d: Dialog) {
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

            val apiInterface = ApiInterface.create().searchWaterTanks(searchId.text.toString())

            apiInterface.enqueue( object : Callback<List<TankGetBody>> {
                override fun onResponse(call: Call<List<TankGetBody>>, response: Response<List<TankGetBody>>?) {
                    progress.visibility = View.GONE
                    if(response?.body() !== null && response?.body()?.size!! > 0) {
                        System.out.println(response.body())
                        dialog.hide()
                        prefillForms(response?.body()?.get(0)!!)

                    }else {
                        error.text = "Feature not found!"
                    }
                }
                override fun onFailure(call: Call<List<TankGetBody>>, t: Throwable) {
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
        val tank_name = findViewById<EditText>(R.id.tank_name)
        val zone = findViewById<Spinner>(R.id.zone)
        val elevation = findViewById<EditText>(R.id.elevation)
        val area = findViewById<EditText>(R.id.area)
        val location = findViewById<EditText>(R.id.location)
        val inlet_pipe = findViewById<EditText>(R.id.inlet_pipe)
        val outlet_pipe = findViewById<EditText>(R.id.outlet_pipe)
        val material = findViewById<EditText>(R.id.material)
        val capacity = findViewById<EditText>(R.id.capacity)
        val status = findViewById<Spinner>(R.id.status)
        val remarks = findViewById<EditText>(R.id.remarks)

        val lat = intent.getDoubleExtra("lat",0.0)
        val lng = intent.getDoubleExtra("lng",0.0)

        next.setOnClickListener {
            error.text = ""

            if (TextUtils.isEmpty(tank_name.text.toString())) {
                error.text = "Tank Name cannot be empty!"
                return@setOnClickListener
            }

            if (TextUtils.isEmpty(location.text.toString())) {
                error.text = "Location cannot be empty!"
                return@setOnClickListener
            }

            progress.visibility = View.VISIBLE
            val tankBody = TankBody(
                tank_name.text.toString(),
                zone.selectedItem.toString(),
                lng,
                lat,
                elevation.text.toString(),
                area.text.toString(),
                location.text.toString(),
                inlet_pipe.text.toString(),
                outlet_pipe.text.toString(),
                material.text.toString(),
                capacity.text.toString(),
                status.selectedItem.toString(),
                remarks.text.toString(),
                user.text.toString()
            )

            System.out.println("THE TANK BODY IS $tankBody")

            val apiInterface = ApiInterface.create().postTanks(tankBody)

            apiInterface.enqueue( object : Callback<Message> {
                override fun onResponse(call: Call<Message>?, response: Response<Message>?) {
                    progress.visibility = View.GONE
                    System.out.println(response?.body())
                    if(response?.body()?.success !== null){
                        error.text = response?.body()?.success
                        val intent = Intent(this@Tanks, Home::class.java)
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

    private fun prefillForms (body: TankGetBody) {
        val next = findViewById<Button>(R.id.next)
        val error = findViewById<TextView>(R.id.error)
        val progress = findViewById<ProgressBar>(R.id.progress)
        val tank_name = findViewById<EditText>(R.id.tank_name)
        val zone = findViewById<Spinner>(R.id.zone)
        val elevation = findViewById<EditText>(R.id.elevation)
        val area = findViewById<EditText>(R.id.area)
        val location = findViewById<EditText>(R.id.location)
        val inlet_pipe = findViewById<EditText>(R.id.inlet_pipe)
        val outlet_pipe = findViewById<EditText>(R.id.outlet_pipe)
        val material = findViewById<EditText>(R.id.material)
        val capacity = findViewById<EditText>(R.id.capacity)
        val remarks = findViewById<EditText>(R.id.remarks)
        val status = findViewById<Spinner>(R.id.status)

        next.text = "Update"

        //Bind data
        tank_name.setText(body.Name)
        updateSpinner(zone,body.Zone)
        elevation.setText(body.Elevation)
        area.setText(body.Area)
        location.setText(body.Location)
        inlet_pipe.setText(body.InletPipe)
        outlet_pipe.setText(body.OutletPipe)
        material.setText(body.Material)
        capacity.setText(body.Capacity)
        updateSpinner(status,body.Status)
        remarks.setText(body.Remarks)


        val id = body.ID
        val objectid = body.ObjectID

        next.setOnClickListener {
            error.text = ""
            progress.visibility = View.VISIBLE

            val tankBody = TankGetBody(
                id,
                objectid,
                tank_name.text.toString(),
                zone.selectedItem.toString(),
                elevation.text.toString(),
                area.text.toString(),
                location.text.toString(),
                inlet_pipe.text.toString(),
                outlet_pipe.text.toString(),
                material.text.toString(),
                capacity.text.toString(),
                status.selectedItem.toString(),
                remarks.text.toString(),
                user.text.toString()
            )

            System.out.println("Tanks is " + tankBody)

            val apiInterface = ApiInterface.create().putWaterTanks(body.ID,tankBody)
            apiInterface.enqueue( object : Callback<Message> {
                override fun onResponse(call: Call<Message>?, response: Response<Message>?) {

                    if(response?.body()?.success !== null){
                        error.text = response?.body()?.success
                        val countdonwtimer = object: CountDownTimer(3000,1){
                            override fun onTick(p0: Long) {
                            }
                            override fun onFinish() {
                                progress.visibility = View.GONE
                                startActivity(Intent(this@Tanks, Home::class.java))
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

    fun updateSpinner(spinner: Spinner, value: String?) {
        val myAdap: ArrayAdapter<String> =
            spinner.getAdapter() as ArrayAdapter<String>
        val spinnerPosition = myAdap.getPosition(value)
        spinner.setSelection(spinnerPosition);
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val i = Intent(this, PointHome::class.java)
        startActivity(i)
        finish()
    }

}