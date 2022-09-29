package com.bignerdranch.android.lab11json

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.view.isVisible
import androidx.room.Room
import com.bignerdranch.android.lab11json.data.DATABASE_NAME
import com.bignerdranch.android.lab11json.data.TasksBD
import com.bignerdranch.android.lab11json.data.models.Tasks
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {
    private lateinit var nameTask: EditText
    private lateinit var namesTask: EditText
    private lateinit var textTask: EditText
    private lateinit var dateTask: CalendarView
    private lateinit var btnTask: Button
    private lateinit var delTask: Button
    private lateinit var btnTaskInfo: ImageButton
    private lateinit var tvHead: TextView
    private lateinit var checkTask : CheckBox

    private var date : String? = null
    private var uid: UUID? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        nameTask = findViewById(R.id.nameTask)
        namesTask = findViewById(R.id.namesTask)
        textTask = findViewById(R.id.textTask)
        btnTask = findViewById(R.id.buttonTask)
        delTask = findViewById(R.id.delTasks)
        dateTask = findViewById(R.id.calendarView)
        btnTaskInfo = findViewById(R.id.backTaskInfo)
        tvHead = findViewById(R.id.textView3)
        checkTask = findViewById(R.id.prioruty)

        var index = intent.getIntExtra("index",-1)
        var txtuid = intent.getStringExtra("uid")

        Log.d("UUID-TXT",txtuid.toString())

        if(txtuid != null){
            uid = UUID.fromString(txtuid)
        }
        dateTask.setOnDateChangeListener(){view, year, month, dayOfMonth -> date = "$dayOfMonth.${month+1}.$year"}
        val cl = Calendar.getInstance()

        if(index > -1){
            delTask.isVisible = true
            tvHead.text = "Редактирование"
            btnTask.text = "Изменить"
            var db: TasksBD = Room.databaseBuilder(this, TasksBD::class.java, DATABASE_NAME).build()
            val TaskDAO = db.TasksDAO()
            val Tasks = TaskDAO.getTasks(uid!!)
            Tasks.observe(this, androidx.lifecycle.Observer {
                it.forEach {
                    nameTask.setText(it.nameTask)
                    namesTask.setText(it.creatTask)
                    textTask.setText(it.text)
                    if(it.preorityId){
                        checkTask.isChecked = true
                    } else {
                        checkTask.isChecked = false
                    }
                    val date = it.dateTask.split(".")
                    cl.set(date?.get(2)!!.toInt(),date[1].toInt()-1,date[0].toInt())
                    dateTask.date = cl.timeInMillis
                }
            })
        }
        else {
            delTask.isVisible = false
        }

        //Отправка
        btnTask.setOnClickListener {
            if(index == -1){

                var db: TasksBD = Room.databaseBuilder(this, TasksBD::class.java, DATABASE_NAME).build()
                val TaskDAO = db.TasksDAO()
                val exec = Executors.newSingleThreadExecutor()
                exec.execute{
                    TaskDAO.addTasks(Tasks(UUID.randomUUID(),checkTask.isChecked,nameTask.text.toString(),namesTask.text.toString(),textTask.text.toString(),date.toString()))
                }
            }
            else
            {
                var db: TasksBD = Room.databaseBuilder(this, TasksBD::class.java, DATABASE_NAME).build()
                val TaskDAO = db.TasksDAO()
                val exec = Executors.newSingleThreadExecutor()
                exec.execute{
                    TaskDAO.saveTasks(Tasks(uid!!,checkTask.isChecked,nameTask.text.toString(),namesTask.text.toString(),textTask.text.toString(),date.toString()))
                }
            }
            super.onBackPressed()
        }
        btnTaskInfo.setOnClickListener {
            super.onBackPressed()
        }
        delTask.setOnClickListener(){
            var db: TasksBD = Room.databaseBuilder(this, TasksBD::class.java, DATABASE_NAME).build()
            val TaskDAO = db.TasksDAO()
            val exec = Executors.newSingleThreadExecutor()
            exec.execute{
                TaskDAO.delTasks(Tasks(uid!!,checkTask.isChecked,nameTask.text.toString(),namesTask.text.toString(),textTask.text.toString(),date.toString()))
            }
            super.onBackPressed()
        }

    }
}