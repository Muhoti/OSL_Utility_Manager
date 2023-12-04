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
import ke.co.osl.umcollector.models.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class WaterPipesForm: AppCompatActivity() {
    lateinit var user:TextView
    lateinit var preferences: SharedPreferences
    lateinit var editor: SharedPreferences.Editor
    lateinit var dialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.waterpipes_form)

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
        searchWaterPipes(dialog)
        dialog.getWindow()?.setBackgroundDrawableResource(R.drawable.background_transparent);
        dialog.show()
    }

    private fun searchWaterPipes(d: Dialog) {
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
                error.text = "Enter account number here to search!"
                return@setOnClickListener
            }

            val apiInterface = ApiInterface.create().searchWaterPipes(searchId.text.toString())

            apiInterface.enqueue( object : Callback<List<WaterpipesDataGetBody>> {
                override fun onResponse(call: Call<List<WaterpipesDataGetBody>>, response: Response<List<WaterpipesDataGetBody>>?) {
                    progress.visibility = View.GONE
                    if(response?.body()?.size!! > 0) {
                        System.out.println(response.body())
                        dialog.hide()
                        prefillForms(response?.body()?.get(0)!!)

                    }else {
                        error.text = "Feature not found!"
                    }
                }
                override fun onFailure(call: Call<List<WaterpipesDataGetBody>>, t: Throwable) {
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
        val linename = findViewById<EditText>(R.id.linename)
        val material = findViewById<EditText>(R.id.material)
        val intake = findViewById<EditText>(R.id.intake)
        val type = findViewById<Spinner>(R.id.type)
        val function = findViewById<EditText>(R.id.function)
        val dma = findViewById<EditText>(R.id.dma)
        val route = findViewById<EditText>(R.id.route)
        val zone = findViewById<Spinner>(R.id.zone)
        val schemeName = findViewById<Spinner>(R.id.schemeName)
        val size = findViewById<EditText>(R.id.size)
        val status = findViewById<Spinner>(R.id.status)
        val remarks = findViewById<EditText>(R.id.remarks)

        next.setOnClickListener {
            error.text = ""

            if (TextUtils.isEmpty(material.text.toString())) {
                error.text = "Material cannot be empty!"
                return@setOnClickListener
            }

            progress.visibility = View.VISIBLE
            val waterpipesdataupdatebody = WaterpipesDataUpdateBody(
                linename.text.toString(),
                material.text.toString(),
                intake.text.toString(),
                type.selectedItem.toString(),
                function.text.toString(),
                dma.text.toString(),
                route.text.toString(),
                zone.selectedItem.toString(),
                schemeName.selectedItem.toString(),
                size.text.toString(),
                status.selectedItem.toString(),
                remarks.text.toString(),
                user.text.toString()
            )

            val ID =intent.getStringExtra("ID")
            System.out.println("The ID IS $ID")
            val apiInterface = ApiInterface.create().putWaterPipes(ID!!,waterpipesdataupdatebody)

            apiInterface.enqueue( object : Callback<Message> {
                override fun onResponse(call: Call<Message>?, response: Response<Message>?) {
                    if(response?.body()?.success !== null){
                        error.text = response?.body()?.success

                        val countdonwtimer = object: CountDownTimer(3000,1){
                            override fun onTick(p0: Long) {
                            }
                            override fun onFinish() {
                                progress.visibility = View.GONE
                                val intent = Intent(this@WaterPipesForm, Home::class.java)
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

    private fun prefillForms (body: WaterpipesDataGetBody) {
        val next = findViewById<Button>(R.id.next)
        val error = findViewById<TextView>(R.id.error)
        val progress = findViewById<ProgressBar>(R.id.progress)
        val linename = findViewById<EditText>(R.id.linename)
        val material = findViewById<EditText>(R.id.material)
        val intake = findViewById<EditText>(R.id.intake)
        val type = findViewById<Spinner>(R.id.type)
        val function = findViewById<EditText>(R.id.function)
        val dma = findViewById<EditText>(R.id.dma)
        val route = findViewById<EditText>(R.id.route)
        val zone = findViewById<Spinner>(R.id.zone)
        val schemeName = findViewById<Spinner>(R.id.schemeName)
        val size = findViewById<EditText>(R.id.size)
        val status = findViewById<Spinner>(R.id.status)
        val remarks = findViewById<EditText>(R.id.remarks)

        next.text = "Update"

        //Bind data
        linename.setText(body.LineName)
        material.setText(body.Material)
        intake.setText(body.Intake)
        updateSpinner(type,body.Type)
        function.setText(body.Function)
        dma.setText(body.DMA)
        route.setText(body.Route)
        updateSpinner(zone,body.Zone)
        updateSpinner(schemeName,body.SchemeName)
        size.setText(body.Size)
        updateSpinner(status,body.Status)
        remarks.setText(body.Remarks)

        next.setOnClickListener {
            error.text = ""

            progress.visibility = View.VISIBLE
            val waterpipesdataupdatebody = WaterpipesDataUpdateBody(
                linename.text.toString(),
                material.text.toString(),
                intake.text.toString(),
                type.selectedItem.toString(),
                function.text.toString(),
                dma.text.toString(),
                route.text.toString(),
                zone.selectedItem.toString(),
                schemeName.selectedItem.toString(),
                size.text.toString(),
                type.selectedItem.toString(),
                remarks.text.toString(),
                user.text.toString()
            )

            val apiInterface = ApiInterface.create().putWaterPipes(body.ObjectID,waterpipesdataupdatebody)

            apiInterface.enqueue( object : Callback<Message> {
                override fun onResponse(call: Call<Message>?, response: Response<Message>?) {
                    if(response?.body()?.success !== null){
                        error.text = response?.body()?.success
                        val countdonwtimer = object: CountDownTimer(3000,1){
                            override fun onTick(p0: Long) {
                            }
                            override fun onFinish() {
                                progress.visibility = View.GONE
                                val intent = Intent(this@WaterPipesForm, Home::class.java)
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

    fun updateSpinner(spinner: Spinner, value: String?) {
        val myAdap: ArrayAdapter<String> =
            spinner.getAdapter() as ArrayAdapter<String>
        val spinnerPosition = myAdap.getPosition(value)
        spinner.setSelection(spinnerPosition);
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val i = Intent(this, LineHome::class.java)
        startActivity(i)
        finish()
    }

}
