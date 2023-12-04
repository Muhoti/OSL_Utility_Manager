package ke.co.osl.umcollector.Forms

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import ke.co.osl.umcollector.PointHome
import ke.co.osl.umcollector.R
import ke.co.osl.umcollector.api.ApiInterface
import ke.co.osl.umcollector.models.Message
import ke.co.osl.umcollector.models.formBody2
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Form2: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_form2)

        val back = findViewById<ImageView>(R.id.back)
        back.setOnClickListener {
            startActivity(Intent(this, PointHome::class.java))
        }

        System.out.println("welcome to form 2")

//        var id = intent.getStringExtra("id")
        var isUpdating = intent.getStringExtra("isUpdating")
        if (isUpdating != null) {
            chooseAction(isUpdating)
        }

        System.out.println("The form is $isUpdating")
//        if(id == null)
//            id = ""
//        chooseAction (id)
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
        val accstatus = findViewById<Spinner>(R.id.acc_status)
        val connstatus = findViewById<Spinner>(R.id.conn_status)
        val brand = findViewById<Spinner>(R.id.brand)
        val material = findViewById<Spinner>(R.id.material)
        val mclass = findViewById<Spinner>(R.id.mclass)

        next.setOnClickListener {
            System.out.println("button clicked")
            error.text = ""
            progress.visibility = View.VISIBLE
            val formBody = formBody2(
                accstatus.selectedItem.toString(),
                connstatus.selectedItem.toString(),
                brand.selectedItem.toString(),
                material.selectedItem.toString(),
                mclass.selectedItem.toString()
            )
            val id=intent.getStringExtra("id")
            System.out.println("app crushing, id is $formBody")

            val apiInterface = ApiInterface.create().postForm2(id!!,formBody)
            System.out.println("api connection made $apiInterface")

            apiInterface.enqueue( object : Callback<Message> {
                override fun onResponse(call: Call<Message>?, response: Response<Message>?) {
                    progress.visibility = View.GONE
                    System.out.println(response?.body())
                    if(response?.body()?.success !== null){
                        error.text = response?.body()?.success
                        val intent = Intent(this@Form2, Form3::class.java)
                        intent.putExtra("id",response?.body()?.token)
                        intent.putExtra("isUpdating", "false")
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

    private fun prefillForms () {
        val accstatus = findViewById<Spinner>(R.id.acc_status)
        val connstatus = findViewById<Spinner>(R.id.conn_status)
        val brand = findViewById<Spinner>(R.id.brand)
        val material = findViewById<Spinner>(R.id.material)
        val mclass = findViewById<Spinner>(R.id.mclass)
        val next = findViewById<Button>(R.id.next)
        val error = findViewById<TextView>(R.id.error)
        val progress = findViewById<ProgressBar>(R.id.progress)
        next.text = "Update"

        System.out.println("value"+intent.getStringExtra("AccountNo"))

        //Bind data
        updateSpinner(accstatus,intent.getStringExtra("AccountStatus"))
        updateSpinner(connstatus,intent.getStringExtra("ConnectionStatus"))
        updateSpinner(brand,intent.getStringExtra("Brand"))
        updateSpinner(material,intent.getStringExtra("Material"))
        updateSpinner(mclass,intent.getStringExtra("Class"))

        next.setOnClickListener {
            error.text = ""
            progress.visibility = View.VISIBLE
            val formBody = formBody2(
                accstatus.selectedItem.toString(),
                connstatus.selectedItem.toString(),
                brand.selectedItem.toString(),
                material.selectedItem.toString(),
                mclass.selectedItem.toString(),
            )

            val id=intent.getStringExtra("id")
            val apiInterface = ApiInterface.create().postForm2(id!!,formBody)

            apiInterface.enqueue( object : Callback<Message> {
                override fun onResponse(call: Call<Message>?, response: Response<Message>?) {
                    progress.visibility = View.GONE
                    System.out.println(response?.body())
                    if(response?.body()?.success !== null){
                        error.text = response?.body()?.success
                        val intent1 = Intent(this@Form2, Form3::class.java)
                        intent1.putExtra("isUpdating", "true")
                        intent1.putExtra("id",response?.body()?.token)
                        intent1.putExtra("SchemeName", intent.getStringExtra("SchemeName"))
                        intent1.putExtra("Zone",intent.getStringExtra("Zone"))
                        intent1.putExtra("DMA", intent.getStringExtra("DMA"))
                        intent1.putExtra("Route", intent.getStringExtra("Route"))
                        intent1.putExtra("Location", intent.getStringExtra("Location"))
                        intent1.putExtra("Remarks", intent.getStringExtra("Remarks"))
                        startActivity(intent1)
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

    fun updateSpinner(spinner: Spinner, value: String?) {
        val myAdap: ArrayAdapter<String> =
            spinner.getAdapter() as ArrayAdapter<String>
        val spinnerPosition = myAdap.getPosition(value)
        spinner.setSelection(spinnerPosition);
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val i = Intent(this, Form1::class.java)
        startActivity(i)
        finish()
    }

}


//

