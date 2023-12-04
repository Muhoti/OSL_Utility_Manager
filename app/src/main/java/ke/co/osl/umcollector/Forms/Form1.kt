package ke.co.osl.umcollector.Forms

import android.app.Dialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
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
import ke.co.osl.umcollector.models.Message
import ke.co.osl.umcollector.models.dataBody
import ke.co.osl.umcollector.models.FormBody1
import ke.co.osl.umcollector.models.FormBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class Form1: AppCompatActivity() {
    lateinit var user:TextView
    lateinit var preferences: SharedPreferences
    lateinit var editor: SharedPreferences.Editor
    lateinit var dialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_form1)

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
            startActivity(Intent(this, PointHome::class.java))
        }

        dialog = Dialog(this)
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setContentView(R.layout.searchfeature)

        var isEditing = intent.getBooleanExtra("isUpdating", false)

        chooseAction(isEditing)

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
        searchCustomerMeters(dialog)
        dialog.getWindow()?.setBackgroundDrawableResource(R.drawable.background_transparent);
        dialog.show()
    }

    private fun searchCustomerMeters(d: Dialog) {
        val searchtitle = d.findViewById<TextView>(R.id.searchtitle)
        val submit = d.findViewById<Button>(R.id.submit)
        val error = d.findViewById<TextView>(R.id.error)
        val searchId = d.findViewById<EditText>(R.id.searchId)
        val progress = d.findViewById<ProgressBar>(R.id.progress)

        searchtitle.text = "Search by AccountNo"

        submit.setOnClickListener {

            progress.visibility = View.VISIBLE

            if (TextUtils.isEmpty(searchtitle.text.toString())) {
                progress.visibility = View.GONE
                error.text = "Enter account number here to search!"
                return@setOnClickListener
            }

            val apiInterface = ApiInterface.create().searchCustomerDetails(searchId.text.toString())

            apiInterface.enqueue( object : Callback<List<dataBody>> {
                override fun onResponse(call: Call<List<dataBody>>, response: Response<List<dataBody>>?) {
                    progress.visibility = View.GONE
                    if(response?.body()?.size!! > 0) {
                        System.out.println(response.body())
                        dialog.hide()
                        prefillForms(response?.body()?.get(0)!!)

                    }else {
                        error.text = "Feature not found!"
                    }
                }
                override fun onFailure(call: Call<List<dataBody>>, t: Throwable) {
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
        val name = findViewById<EditText>(R.id.name)
        val accnumber = findViewById<EditText>(R.id.acc_number)
        val meternumber = findViewById<EditText>(R.id.meter_number)
        val metersize = findViewById<Spinner>(R.id.meter_size)
        val meterstatus = findViewById<Spinner>(R.id.meter_status)
        val progress = findViewById<ProgressBar>(R.id.progress)

        val lat = intent.getDoubleExtra("lat",0.0)
        val lng = intent.getDoubleExtra("lng",0.0)

        next.setOnClickListener {
            error.text = ""

            if (TextUtils.isEmpty(accnumber.text.toString())) {
                error.text = "Account number cannot be empty!"
                return@setOnClickListener
            }

            if(accnumber.text.toString().length !== 5) {
                error.text = "Account Number needs to be 5 digits!"
                return@setOnClickListener
            }

            if (TextUtils.isEmpty(name.text.toString())) {
                error.text = "Name cannot be empty!"
                return@setOnClickListener
            }

            progress.visibility = View.VISIBLE

            val formBody = FormBody(
                name.text.toString(),
                lng,
                lat,
                accnumber.text.toString(),
                meternumber.text.toString(),
                metersize.selectedItem.toString(),
                meterstatus.selectedItem.toString(),
                user.text.toString(),
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                ""
            )

            val apiInterface = ApiInterface.create().postCustomerMeters(formBody)

            apiInterface.enqueue( object : Callback<Message> {
                override fun onResponse(call: Call<Message>?, response: Response<Message>?) {
                    progress.visibility = View.GONE
                    if(response?.body()?.success !== null){
                        error.text = response?.body()?.success
                        val intent = Intent(this@Form1, Form2::class.java)
                        intent.putExtra("id",response?.body()?.token)
                        intent.putExtra("isUpdating", "false")
                        startActivity(intent)
                    }
                    else {
                        error.text = response?.body()?.error
                    }
                }
                override fun onFailure(call: Call<Message>?, t: Throwable?) {
                    progress.visibility = View.GONE
                    System.out.println(t)
                    error.text = "Connection to server failed"
                }
            })
        }
    }

    private fun prefillForms (body: dataBody) {
        val error = findViewById<TextView>(R.id.error)
        val name = findViewById<EditText>(R.id.name)
        val accnumber = findViewById<EditText>(R.id.acc_number)
        val meternumber = findViewById<EditText>(R.id.meter_number)
        val metersize = findViewById<Spinner>(R.id.meter_size)
        val meterstatus = findViewById<Spinner>(R.id.meter_status)
        val progress = findViewById<ProgressBar>(R.id.progress)
        val next = findViewById<Button>(R.id.next)

        next.text = "Update"

        //Bind data
        name.setText(body.Name)
        accnumber.setText(body.AccountNo)
        meternumber.setText(body.MeterNo)
        updateSpinner(metersize,body.MeterSize)
        updateSpinner(meterstatus,body.MeterStatus)

        val id = body.ID

        next.setOnClickListener {
            error.text = ""

            if (TextUtils.isEmpty(accnumber.text.toString())) {
                error.text = "Account number cannot be empty!"
                return@setOnClickListener
            }
            if(accnumber.text.toString().length !== 5) {
                error.text = "Account Number needs to be 5 digits!"
                return@setOnClickListener
            }

            progress.visibility = View.VISIBLE
            val formBody1 = FormBody1(
                id,
                name.text.toString(),
                accnumber.text.toString(),
                meternumber.text.toString(),
                metersize.selectedItem.toString(),
                meterstatus.selectedItem.toString(),
                user.text.toString()
            )

            System.out.println("The form data is " + formBody1)

            val apiInterface = ApiInterface.create().putCustomerMeters(body.ID,formBody1)

            apiInterface.enqueue( object : Callback<Message> {
                override fun onResponse(call: Call<Message>?, response: Response<Message>?) {
                    progress.visibility = View.GONE
                    if(response?.body()?.success !== null){
                        System.out.println("The body is "+ response.body())
                        val intent = Intent(this@Form1, Form2::class.java)
                        intent.putExtra("isUpdating", "true")
                        intent.putExtra("id",response?.body()?.token)
                        intent.putExtra("AccountStatus", body.AccountStatus)
                        intent.putExtra("ConnectionStatus", body.ConnectionStatus)
                        intent.putExtra("Brand", body.Brand)
                        intent.putExtra("Material", body.Material)
                        intent.putExtra("Class", body.Class)
                        intent.putExtra("SchemeName", body.SchemeName)
                        intent.putExtra("Zone", body.Zone)
                        intent.putExtra("DMA", body.DMA)
                        intent.putExtra("Route", body.Route)
                        intent.putExtra("Location", body.Location)
                        intent.putExtra("Remarks", body.Remarks)
                        startActivity(intent)
                        finish()
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