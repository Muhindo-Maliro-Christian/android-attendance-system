package com.example.attendancesystem.utils.adapters


import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.attendancesystem.R
import com.example.attendancesystem.models.employee.Employee


class RecyclerViewAdapter(private val listener: RowClickListener, var context: Context, var activity : Activity) : RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder>() {

    var items = ArrayList<Employee>()

    fun setListData(data : ArrayList<Employee>){
        this.items = data
        notifyDataSetChanged()
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val inflater = LayoutInflater.from(parent.context).inflate(R.layout.employees_items_list, parent, false)
        return MyViewHolder(inflater, listener, context)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val model =  items[position]

        holder.name.text = model.name
        holder.employee.setOnClickListener {
            listener.onItemClickListener(model)
        }

    }
    //    override fun getItemCount(): Int = filterItems?.size ?: items.size
    override fun getItemCount(): Int = items.size

    class MyViewHolder(view: View, val listener: RowClickListener, context: Context) : RecyclerView.ViewHolder(view) {

        var name: TextView = view.findViewById(R.id.name)
        var employee: LinearLayout = view.findViewById(R.id.employee)

    }

    interface RowClickListener{
        fun onItemClickListener(employee: Employee)
    }

}
