package ke.co.osl.umcollector

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ke.co.osl.umcollector.Forms.Form1
import ke.co.osl.umcollector.models.dataBody

class MyAdapter (val context: Context, val userList: List<dataBody>, val offset: Int) :
    RecyclerView.Adapter<MyAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var no: TextView
        val name: TextView
        val accountNumber: TextView

        init {
            no = itemView.findViewById(R.id.no)
            name = itemView.findViewById(R.id.name)
            accountNumber = itemView.findViewById(R.id.accountNumber)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(context).inflate(R.layout.mapped_items, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.no.text = (offset+position+1).toString()+"."
        holder.name.text = userList[position].Name
        holder.accountNumber.text = userList[position].AccountNo

        holder.itemView.setOnClickListener {
            val intent = Intent(context, Form1::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.putExtra("id", userList[position].ID)
            intent.putExtra("Name", userList[position].Name)
            intent.putExtra("Zone", userList[position].Zone)
            intent.putExtra("Route", userList[position].Route)
            intent.putExtra("DMA", userList[position].DMA)
            intent.putExtra("Location", userList[position].Location)
            intent.putExtra("SchemeName", userList[position].SchemeName)

            intent.putExtra("AccountNo", userList[position].AccountNo)
            intent.putExtra("MeterNo", userList[position].MeterNo)
            intent.putExtra("MeterSize", userList[position].MeterSize)
            intent.putExtra("MeterStatus", userList[position].MeterStatus)
            intent.putExtra("ConnectionStatus", userList[position].ConnectionStatus)
            intent.putExtra("AccountStatus", userList[position].AccountStatus)

            intent.putExtra("Brand", userList[position].Brand)
            intent.putExtra("Material", userList[position].Material)
            intent.putExtra("Class", userList[position].Class)
            intent.putExtra("User", userList[position].User)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return userList.size
    }
}