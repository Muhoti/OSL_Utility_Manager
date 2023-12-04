package ke.co.osl.umcollector

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ke.co.osl.umcollector.Forms.Form1
import ke.co.osl.umcollector.models.WaterLinesCoordsBody

class WaterPipesAdapter (val context: Context, val userList: List<WaterLinesCoordsBody>, val offset: Int) :
    RecyclerView.Adapter<WaterPipesAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var no: TextView
        val name: TextView
        val type: TextView

        init {
            no = itemView.findViewById(R.id.no)
            name = itemView.findViewById(R.id.name)
            type = itemView.findViewById(R.id.type)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(context).inflate(R.layout.mapped_waterpipes, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.no.text = (offset+position+1).toString()+"."
        holder.name.text = userList[position].LineName
        holder.type.text = userList[position].Type

        holder.itemView.setOnClickListener {
            val intent = Intent(context, Form1::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.putExtra("LineName", userList[position].LineName)
            intent.putExtra("Material", userList[position].Material)
            intent.putExtra("Intake", userList[position].Intake)
            intent.putExtra("Type", userList[position].Type)
            intent.putExtra("Function", userList[position].Function)
            intent.putExtra("DMA", userList[position].DMA)

            intent.putExtra("SchemeName", userList[position].SchemeName)
            intent.putExtra("Route", userList[position].Route)
            intent.putExtra("Zone", userList[position].Zone)
            intent.putExtra("Size", userList[position].Size)
            intent.putExtra("Status", userList[position].Status)
            intent.putExtra("Remarks", userList[position].Remarks)

            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return userList.size
    }
}