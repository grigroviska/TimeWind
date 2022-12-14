package com.gematriga.timewind

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gematriga.timewind.DTO.ToDo
import com.gematriga.timewind.databinding.ActivityDashboardBinding

class DashboardActivity : AppCompatActivity() {
    private lateinit var recyclerView : RecyclerView
    private lateinit var binding : ActivityDashboardBinding
    lateinit var dbHandler: DBHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        recyclerView = binding.twDashboard

        dbHandler = DBHandler(this)
        binding.twDashboard.layoutManager = LinearLayoutManager(this)
        binding.fabDashboard.setOnClickListener{

            val dialog = AlertDialog.Builder(this)
            dialog.setTitle("Add ToDo")
            val view = layoutInflater.inflate(R.layout.dialog, null)
            val toDoName = view.findViewById<EditText>(R.id.tw_todo)
            dialog.setView(view)
            dialog.setPositiveButton("Add") { _: DialogInterface, _: Int ->
                if (toDoName.text.isNotEmpty()) {
                    val toDo = ToDo()
                    toDo.name = toDoName.text.toString()
                    dbHandler.addToDo(toDo)
                    refreshList()
                }
            }
            dialog.setNegativeButton("Cancel") { _: DialogInterface, _: Int ->

            }
            dialog.show()

        }

    }

    fun updateToDo(toDo: ToDo){

        val dialog = AlertDialog.Builder(this)
        dialog.setTitle("Update ToDo")
        val view = layoutInflater.inflate(R.layout.dialog, null)
        val toDoName = view.findViewById<EditText>(R.id.tw_todo)
        toDoName.setText(toDo.name)
        dialog.setView(view)
        dialog.setPositiveButton("Update") { _: DialogInterface, _: Int ->
            if (toDoName.text.isNotEmpty()) {
                toDo.name = toDoName.text.toString()
                dbHandler.updateToDo(toDo)
                refreshList()
            }
        }
        dialog.setNegativeButton("Cancel") { _: DialogInterface, _: Int ->

        }
        dialog.show()

    }

    override fun onResume() {
        refreshList()
        super.onResume()
    }

    private fun refreshList(){

        binding.twDashboard.adapter = DashboardAdapter(this, dbHandler.getTodos())

    }

    class DashboardAdapter(val activity: DashboardActivity, val list: MutableList<ToDo>) : RecyclerView.Adapter<DashboardAdapter.ViewHolder>(){

        class ViewHolder (v: View) : RecyclerView.ViewHolder(v){
            val toDoName : TextView = v.findViewById(R.id.tw_todo_name)
            val menu : ImageView = v.findViewById(R.id.tw_menu)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

            return ViewHolder(LayoutInflater.from(activity).inflate(R.layout.tw_child_dashboard,parent,false))

        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {

            holder.toDoName.text = list[position].name

            holder.toDoName.setOnClickListener {

                val intent = Intent(activity, ItemActivity::class.java)
                intent.putExtra(INTENT_TODO_ID, list[position].id)
                intent.putExtra(INTENT_TODO_NAME, list[position].name)
                activity.startActivity(intent)

            }

            holder.menu.setOnClickListener{

                val popup = PopupMenu(activity , holder.menu)
                popup.inflate(R.menu.dashboard_child)
                popup.setOnMenuItemClickListener {

                    when(it.itemId){

                        R.id.menu_edit -> {

                            activity.updateToDo(list[position])

                        }

                        R.id.menu_delete -> {

                            val dialog = AlertDialog.Builder(activity)
                            dialog.setTitle("Are you sure")
                            dialog.setMessage("Do you want to delete this task?")
                            dialog.setPositiveButton("Continue") { _: DialogInterface, _: Int ->

                                activity.dbHandler.deleteToDo(list[position].id)
                                activity.refreshList()

                            }
                            dialog.setNegativeButton("Cancel") { _: DialogInterface, _: Int ->



                            }
                            dialog.show()

                        }

                        R.id.menu_marked -> {

                            activity.dbHandler.updateToDoItemCompletedStatus(list[position].id, true)

                        }

                        R.id.menu_reset -> {

                            activity.dbHandler.updateToDoItemCompletedStatus(list[position].id, false)

                        }

                    }

                    true
                }

                popup.show()

            }

        }

        override fun getItemCount(): Int {
            return list.size
        }

    }
}