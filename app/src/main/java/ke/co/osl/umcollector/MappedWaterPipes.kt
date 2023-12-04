package ke.co.osl.umcollector

import android.content.Intent
import android.os.Bundle
import android.util.Log.d
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ke.co.osl.umcollector.api.ApiInterface
import ke.co.osl.umcollector.models.mappedWaterPipesBody
import retrofit2.Call
import retrofit2.Response

class MappedWaterPipes: AppCompatActivity() {
    lateinit var myAdapter: WaterPipesAdapter
    lateinit var linearLayoutManager: LinearLayoutManager
    lateinit var recyclerlist: RecyclerView
    lateinit var pages: TextView
    var offset = 0
    var total = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_water_pipes)

        recyclerlist = findViewById(R.id.recycler_list)
        recyclerlist.setHasFixedSize(true)
        linearLayoutManager = LinearLayoutManager(this)
        recyclerlist.layoutManager = linearLayoutManager

        val back = findViewById<ImageView>(R.id.back)
        val prev = findViewById<ImageView>(R.id.previous)
        val next = findViewById<ImageView>(R.id.next)
        pages = findViewById<TextView>(R.id.pages)

        back.setOnClickListener{
            startActivity(Intent(this@MappedWaterPipes,Home::class.java))
        }

        prev.setOnClickListener{
            if(offset !== 0){
                offset = offset - 10
                displayMappedList(offset)
            }
        }

        next.setOnClickListener{
            if(offset+10 <= total) {
                offset = offset + 10
                displayMappedList(offset)
            }
        }

        displayMappedList(offset)
    }

    private fun displayMappedList(offset: Int) {
        val progress = findViewById<ProgressBar>(R.id.progress)
        progress.visibility = View.VISIBLE
        val apiInterface = ApiInterface.create().showWaterPipes(offset)
        apiInterface.enqueue(object : retrofit2.Callback<mappedWaterPipesBody> {
            override fun onResponse(call: Call<mappedWaterPipesBody>,response: Response<mappedWaterPipesBody>) {
                progress.visibility = View.GONE
                val responseBody = response.body()!!
                myAdapter = WaterPipesAdapter(baseContext, responseBody?.data!!,offset)
                myAdapter.notifyDataSetChanged()
                recyclerlist.adapter = myAdapter
                total = responseBody?.total!!
                val pgs = Math.ceil((responseBody?.total!!/10.0)).toInt()
                val txt = if(offset == 0){
                    1.toString() + "/" + pgs.toString()
                }else ((offset/10)+1).toString() + "/" + pgs.toString()

                pages.text = txt
            }

            override fun onFailure(call: Call<mappedWaterPipesBody>, t: Throwable) {
                progress.visibility = View.GONE
                d("MappedPoints", "onFailure: " + t.message)
            }
        })


    }

    override fun onBackPressed() {
        super.onBackPressed()
        val i = Intent(this, Home::class.java)
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        finish()
    }
}