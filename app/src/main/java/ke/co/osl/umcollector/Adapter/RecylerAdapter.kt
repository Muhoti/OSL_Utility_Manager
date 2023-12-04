package ke.co.osl.umcollector.Adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import ke.co.osl.umcollector.Forms.*
import ke.co.osl.umcollector.LineHome
import ke.co.osl.umcollector.Params
import ke.co.osl.umcollector.PointHome
import ke.co.osl.umcollector.R

class RecylerAdapter (private val paramList:ArrayList<Params>, val context: Context) :
    RecyclerView.Adapter<RecylerAdapter.ParamViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParamViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.adapter_home, parent, false)
        return ParamViewHolder(view,context)
    }

    override fun onBindViewHolder(holder: ParamViewHolder, position: Int) {
        val param = paramList[position]
        holder.imageView.setImageResource(param.image)
        holder.textView.text = param.name
    }

    override fun getItemCount(): Int {
        return paramList.size
    }

    class ParamViewHolder(itemView:View,val context: Context) : RecyclerView.ViewHolder(itemView){
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
        val textView: TextView = itemView.findViewById(R.id.textView)
        val options: LinearLayout = itemView.findViewById(R.id.options)
        val addnew: LinearLayout = itemView.findViewById(R.id.addnew)
        val update: LinearLayout = itemView.findViewById(R.id.update)
        val item: CardView = itemView.findViewById(R.id.item)

        init {
            item.setOnClickListener {
                if(options.visibility == View.GONE){
                    options.visibility = View.VISIBLE
                }else {
                    options.visibility = View.GONE
                }
            }

            addnew.setOnClickListener{
                options.visibility = View.GONE
                when(textView.text){
                    "Customer Meters" -> mapCustomerMeters()
                    "Water Pipes" -> mapWaterPipes()
                    "Water Tanks" -> mapTanks()
                    "Valves" -> mapValves()
                    "Sewer Lines" -> mapSewerlines()
                    "Manholes" -> mapManHoles()
                    "Point Project" -> mapPointProjects()
                    "Line Project" -> mapLineProjects()
                    "Master Meters" -> mapMasterMeters()
                    "PRV" -> mapPRV()
                    else -> {}
                }
            }

            update.setOnClickListener{
                options.visibility = View.GONE
                when(textView.text){
                    "Customer Meters" -> updateCustomerMeters()
                    "Water Pipes" -> updateWaterPipes()
                    "Master Meters" -> updateMasterMeters()
                    "PRV" -> updatePRV()
                    "Water Tanks" -> updateTanks()
                    "Valves" -> updateValves()
                    "Sewer Lines" -> updateSewerlines()
                    "Manholes" -> updateManHoles()
                    "Point Project" -> mapPointProjects()
                    "Line Project" -> mapLineProjects()
                    else -> {}
                }

            }


        }

        private fun mapCustomerMeters() {
            val intent = Intent(context, PointHome::class.java)
            intent.putExtra("MappedItem", "Customer Meters")
            intent.putExtra("isUpdating", false)
            context.startActivity(intent)
        }

        private fun updateCustomerMeters() {
            val intent = Intent(context, Form1::class.java)
            intent.putExtra("isUpdating", true)
            context.startActivity(intent)
        }

        private fun mapWaterPipes() {
            val intent = Intent(context, LineHome::class.java)
            intent.putExtra("MappedItem", "waterpipes")
            intent.putExtra("isUpdating", false)
            context.startActivity(intent)
        }

        private fun updateWaterPipes() {
            val intent = Intent(context, WaterPipesForm::class.java)
            intent.putExtra("isUpdating", true)
            context.startActivity(intent)
        }

        private fun mapMasterMeters() {
            val intent = Intent(context, PointHome::class.java)
            intent.putExtra("isUpdating", false)
            intent.putExtra("MappedItem", "Master Meters")
            context.startActivity(intent)
        }

        private fun updateMasterMeters() {
            val intent = Intent(context, MasterMeters::class.java)
            intent.putExtra("isUpdating", true)
            context.startActivity(intent)
        }

        private fun mapPRV() {
            val intent = Intent(context, PointHome::class.java)
            intent.putExtra("isUpdating", false)
            intent.putExtra("MappedItem", "PRV")
            context.startActivity(intent)
        }

        private fun updatePRV() {
            val intent = Intent(context, PRV::class.java)
            intent.putExtra("isUpdating", true)
            context.startActivity(intent)
        }

        private fun mapTanks() {
            val intent = Intent(context, PointHome::class.java)
            intent.putExtra("isUpdating", false)
            intent.putExtra("MappedItem", "Water Tanks")
            context.startActivity(intent)
        }

        private fun updateTanks() {
            val intent = Intent(context, Tanks::class.java)
            intent.putExtra("isUpdating", true)
            context.startActivity(intent)
        }

        private fun mapValves() {
            val intent = Intent(context, PointHome::class.java)
            intent.putExtra("MappedItem", "Valves")
            intent.putExtra("isUpdating", false)
            context.startActivity(intent)
        }

        private fun updateValves() {
            val intent = Intent(context, ValvesForm::class.java)
            intent.putExtra("isUpdating", true)
            context.startActivity(intent)
        }

        private fun mapSewerlines() {
            val intent = Intent(context, LineHome::class.java)
            intent.putExtra("MappedItem", "sewerlines")
            intent.putExtra("isUpdating", false)
            context.startActivity(intent)
        }

        private fun updateSewerlines() {
            val intent = Intent(context, SewerlinesForm::class.java)
            intent.putExtra("isUpdating", true)
            context.startActivity(intent)
        }

        private fun mapLineProjects() {
            val intent = Intent(context, LineHome::class.java)
            intent.putExtra("MappedItem", "lineprojects")
            intent.putExtra("isUpdating", false)
            context.startActivity(intent)
        }

        private fun mapManHoles() {
            val intent = Intent(context, PointHome::class.java)
            intent.putExtra("MappedItem", "Man Holes")
            intent.putExtra("isUpdating", false)
            context.startActivity(intent)
        }

        private fun updateManHoles() {
            val intent = Intent(context, ManHolesForm::class.java)
            intent.putExtra("isUpdating", true)
            context.startActivity(intent)
        }

        private fun mapPointProjects() {
            val intent = Intent(context, PointHome::class.java)
            intent.putExtra("MappedItem", "Point Projects")
            context.startActivity(intent)
        }

    }
}