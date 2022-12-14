package com.gematriga.timewind


import android.annotation.SuppressLint
import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gematriga.timewind.DTO.ToDoItem
import com.gematriga.timewind.databinding.ActivityItemBinding
import java.util.Collections

class ItemActivity : AppCompatActivity() {

    private lateinit var binding : ActivityItemBinding
    lateinit var dbHandler : DBHandler
    var todoId : Long = -1

    var list : MutableList<ToDoItem>? = null
    var adapter : ItemAdapter? = null
    var touchHelper : ItemTouchHelper? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityItemBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.topAppBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.title = intent.getStringExtra(INTENT_TODO_NAME)
        todoId = intent.getLongExtra(INTENT_TODO_ID, -1)
        dbHandler = DBHandler(this)

        binding.twChoiceRv.layoutManager = LinearLayoutManager(this)


        binding.fabChoice.setOnClickListener {

            val dialog = AlertDialog.Builder(this)
            dialog.setTitle("Add ToDo Item")
            val view = layoutInflater.inflate(R.layout.dialog, null)
            val toDoName = view.findViewById<EditText>(R.id.tw_todo)
            dialog.setView(view)
            dialog.setPositiveButton("Add") { _: DialogInterface, _: Int ->
                if (toDoName.text.isNotEmpty()) {
                    val item = ToDoItem()
                    item.itemName = toDoName.text.toString()
                    item.toDoId = todoId
                    item.isCompleted = false
                    dbHandler.addToDoItem(item)
                    refreshList()
                }
            }
            dialog.setNegativeButton("Cancel") { _: DialogInterface, _: Int ->

            }
            dialog.show()

        }

        touchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or
        ItemTouchHelper.DOWN,0){
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val sourcePosition = viewHolder.adapterPosition
                val targetPosition = target.adapterPosition
                Collections.swap(list,sourcePosition,targetPosition)
                adapter?.notifyItemMoved(sourcePosition,targetPosition)
                return true

            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

            }

        })

        touchHelper?.attachToRecyclerView(binding.twChoiceRv)

    }

    fun updateItem(item : ToDoItem){

        val dialog = AlertDialog.Builder(this)
        dialog.setTitle("Update ToDo Item")
        val view = layoutInflater.inflate(R.layout.dialog, null)
        val toDoName = view.findViewById<EditText>(R.id.tw_todo)
        toDoName.setText(item.itemName)
        dialog.setView(view)
        dialog.setPositiveButton("Update") { _: DialogInterface, _: Int ->
            if (toDoName.text.isNotEmpty()) {
                item.itemName = toDoName.text.toString()
                item.toDoId = todoId
                item.isCompleted = false
                dbHandler.updateToDoItem(item)
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

    fun refreshList() {

        list = dbHandler.getToDoItems(todoId)
        adapter = ItemAdapter(this, list!!)
        binding.twChoiceRv.adapter = adapter

    }

    class ItemAdapter(val activity: ItemActivity, val list: MutableList<ToDoItem>) : RecyclerView.Adapter<ItemAdapter.ViewHolder>(){

        class ViewHolder (v: View) : RecyclerView.ViewHolder(v){
            val itemName : CheckBox = v.findViewById(R.id.cb_item)
            val edit : ImageView = v.findViewById(R.id.tw_edit)
            val delete : ImageView = v.findViewById(R.id.tw_delete)
            val move : ImageView = v.findViewById(R.id.tw_move)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

            return ViewHolder(LayoutInflater.from(activity).inflate(R.layout.tw_child_item,parent,false))

        }

        @SuppressLint("ClickableViewAccessibility")
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {

            holder.itemName.text = list[position].itemName
            holder.itemName.isChecked = list[position].isCompleted
            holder.itemName.setOnClickListener {

                list[position].isCompleted = !list[position].isCompleted
                activity.dbHandler.updateToDoItem(list[position])

            }

            holder.delete.setOnClickListener {

                val dialog = AlertDialog.Builder(activity)
                dialog.setTitle("Are you sure")
                dialog.setMessage("Do you want to delete this item?")
                dialog.setPositiveButton("Continue") { _: DialogInterface, _: Int ->

                    activity.dbHandler.deleteToDoItem(list[position].id)
                    activity.refreshList()

                }
                dialog.setNegativeButton("Cancel") { _: DialogInterface, _: Int ->



                }
                dialog.show()

            }

            holder.edit.setOnClickListener {

                activity.updateItem(list[position])

            }

            holder.move.setOnTouchListener {v, event ->

                if (event.actionMasked == MotionEvent.ACTION_DOWN){

                    activity.touchHelper?.startDrag(holder)

                }
                    false

            }

        }

        override fun getItemCount(): Int {
            return list.size
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if(item.itemId == android.R.id.home){
            finish()
            true
        }else
            super.onOptionsItemSelected(item)
    }
}