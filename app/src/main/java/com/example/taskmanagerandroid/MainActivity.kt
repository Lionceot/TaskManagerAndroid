package com.example.taskmanagerandroid

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.setMargins
import androidx.core.view.setPadding
import org.json.JSONObject
import java.io.File
import java.time.Instant

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private val storageFilename = "tasks.json"
    private lateinit var storageFile : File
    private lateinit var storageData : JSONObject

    private lateinit var openTaskCounter : TextView
    private lateinit var openTaskButton: Button
    private lateinit var newTaskInput : EditText
    private lateinit var taskContainer : LinearLayout
    private lateinit var closeTaskButtons : MutableSet<Int>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize views values
        openTaskCounter = findViewById(R.id.open_task_amount)
        openTaskButton = findViewById(R.id.open_task_btn)
        taskContainer = findViewById(R.id.task_container)
        closeTaskButtons = mutableSetOf()

        // Load data from JSON file
        storageData = loadData()

        for (key in storageData.keys()) {
            addNewTask(storageData.get(key).toString(), key.toInt())
        }

        // Set counter value
        openTaskCounter.text = taskContainer.childCount.toString()

        openTaskButton.setOnClickListener(this)
    }

    override fun onClick(v: View) {

        if (v.id == R.id.open_task_btn) {

            newTaskInput = findViewById(R.id.new_task_content)
            if (newTaskInput.text.isBlank()) {
                return
            }

            val newId = addNewTask(newTaskInput.text.toString())
            storageData.accumulate(newId.toString(), newTaskInput.text)
            saveData()

            newTaskInput.setText(resources.getString(R.string.empty))
            openTaskCounter.text = taskContainer.childCount.toString()


        } else if (v.id in closeTaskButtons) {

            taskContainer.removeView(v.parent as View)
            closeTaskButtons.remove(v.id)
            openTaskCounter.text = taskContainer.childCount.toString()

            storageData.remove(v.id.toString())
            saveData()

        }

    }

    private fun addNewTask(taskContent: String, id: Int? = null) : Int {
        val newTask = LinearLayout(this)
        val label = TextView(this)
        val closeButton = Button(this)

        // Layout
        val newTaskLayoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
        )
        newTaskLayoutParams.setMargins(dpToPixels(15f, this).toInt())
        newTask.layoutParams = newTaskLayoutParams
        newTask.orientation = LinearLayout.HORIZONTAL
        newTask.setPadding(dpToPixels(5f, this).toInt())
        newTask.gravity = Gravity.CENTER
        newTask.background = AppCompatResources.getDrawable(this, R.drawable.border)

        // Label
        val labelLayoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
        )
        labelLayoutParams.weight = 10f
        label.layoutParams = labelLayoutParams
        label.setPadding(dpToPixels(4f, this).toInt())
        label.text = taskContent
        label.textSize = 18f
        label.background = ColorDrawable(Color.TRANSPARENT)

        // Button
        if (id != null) {
            closeButton.id = id
        } else {
            closeButton.id = Instant.now().nano
        }
        val closeButtonLayoutParams = LinearLayout.LayoutParams(
            dpToPixels(50f, this).toInt(), dpToPixels(50f, this).toInt()
        )
        closeButtonLayoutParams.setMargins(dpToPixels(4f, this).toInt())
        closeButton.layoutParams = closeButtonLayoutParams
        closeButton.setPadding(0)
        closeButton.text = getString(R.string.task_close_label)
        closeButton.setTextColor(getColor(R.color.white))
        closeButton.textSize = 20f
        closeButton.typeface = Typeface.DEFAULT_BOLD
        closeButton.background = AppCompatResources.getDrawable(this, R.drawable.task_close_button)
        closeButton.setOnClickListener(this)

        newTask.addView(label)
        newTask.addView(closeButton)
        taskContainer.addView(newTask)
        closeTaskButtons.add(closeButton.id)

        return closeButton.id
    }

    private fun dpToPixels(dp: Float, context: Context) : Float {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.resources.displayMetrics)
    }

    private fun loadData(filename: String = storageFilename) : JSONObject {
        val text : String
        try {
            storageFile = File(filesDir, storageFilename)
            text = storageFile.readText()

        } catch (e: Exception) {
            openFileOutput(storageFilename, Context.MODE_PRIVATE).use {
                it.write("{}".toByteArray())
            }
            return loadData(filename)
        }
        return JSONObject(text)
    }

    private fun saveData() {
         openFileOutput(storageFilename, Context.MODE_PRIVATE).use {
            it.write(storageData.toString().toByteArray())
        }
    }
}