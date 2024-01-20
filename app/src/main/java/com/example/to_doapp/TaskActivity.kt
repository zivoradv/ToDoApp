package com.example.to_doapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*

class TaskActivity : AppCompatActivity(), TaskRecyclerViewAdapter.OnItemClickListener {

    private lateinit var currentUserID: String
    private lateinit var recyclerView: RecyclerView
    private lateinit var taskAdapter: TaskRecyclerViewAdapter
    private lateinit var taskList: MutableList<TaskItem>

    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var auth: FirebaseAuth

    private lateinit var imageNoTasks: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_list)

        recyclerView = findViewById(R.id.recycler_view_tasks)
        recyclerView.layoutManager = LinearLayoutManager(this)
        taskList = mutableListOf()
        taskAdapter = TaskRecyclerViewAdapter(taskList, this)
        recyclerView.adapter = taskAdapter

        auth = FirebaseAuth.getInstance()
        firebaseDatabase = FirebaseDatabase.getInstance("https://to-do-80f5e-default-rtdb.europe-west1.firebasedatabase.app")

        imageNoTasks = findViewById(R.id.imageNoTasks)

        val imageViewAddTask = findViewById<ImageView>(R.id.imageViewAddTask)
        imageViewAddTask.setOnClickListener {
            addNewTask()
        }

        val profileImage = findViewById<ImageView>(R.id.profileImage)
        profileImage.setOnClickListener {
            val intent = Intent(this@TaskActivity, ProfileActivity::class.java)
            startActivity(intent)
        }

        fetchTasks()
    }

    private fun fetchTasks() {
        val taskRef = firebaseDatabase.reference.child("tasks")

        taskRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                taskList.clear()
                for (taskSnapshot in snapshot.children) {
                    val task = taskSnapshot.getValue(TaskItem::class.java)
                    task?.let {
                        taskList.add(it)
                    }
                }
                taskAdapter.notifyDataSetChanged()

                if (taskList.isEmpty()) {
                    imageNoTasks.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                } else {
                    imageNoTasks.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun addNewTask() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_new_task, null)

        val alertDialogBuilder = AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("Add") { dialog, _ ->
                val taskName = dialogView.findViewById<EditText>(R.id.editTextTaskName).text.toString()
                val taskDescription = dialogView.findViewById<EditText>(R.id.editTextTaskDescription).text.toString()

                saveTaskToFirebase(taskName, taskDescription, completed = false, expanded = false)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }

        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    private fun saveTaskToFirebase(taskName: String, taskDescription: String, completed: Boolean, expanded: Boolean) {
        val taskRef = firebaseDatabase.reference.child("tasks")

        val taskKey = taskRef.push().key
        val taskData = HashMap<String, Any>()
        taskData["taskName"] = taskName
        taskData["taskDescription"] = "— $taskDescription"
        taskData["completed"] = completed
        taskData["expanded"] = expanded

        taskKey?.let {
            taskRef.child(it).setValue(taskData)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this@TaskActivity, "Task added successfully", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@TaskActivity, "Error in database.", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    override fun onDeleteClick(taskItem: TaskItem) {
        deleteTaskFromFirebase(taskItem)
    }

    private fun deleteTaskFromFirebase(taskItem: TaskItem) {
        val taskRef = firebaseDatabase.reference.child("tasks")

        val query = taskRef.orderByChild("taskName").equalTo(taskItem.taskName)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (taskSnapshot in dataSnapshot.children) {
                    val dialogBuilder = AlertDialog.Builder(this@TaskActivity)
                        .setMessage("Are you sure you want to delete this task?")
                        .setCancelable(false)
                        .setPositiveButton("Yes") { dialog, _ ->
                            taskSnapshot.ref.removeValue()
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        // Deletion successful
                                        Toast.makeText(
                                            this@TaskActivity,
                                            "Task deleted successfully",
                                            Toast.LENGTH_SHORT
                                        ).show()

                                        fetchTasks()
                                    } else {
                                        Toast.makeText(
                                            this@TaskActivity,
                                            "Error deleting task from the database",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                        }
                        .setNegativeButton("No") { dialog, _ ->
                            dialog.dismiss()
                        }
                    val alert = dialogBuilder.create()
                    alert.show()

                    return
                }
                Toast.makeText(
                    this@TaskActivity,
                    "Task not found in the database",
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("FirebaseDeletion", "Error: ${databaseError.message}")
                Toast.makeText(
                    this@TaskActivity,
                    "Error: ${databaseError.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }



    override fun onItemClick(position: Int) {
        val clickedTask = taskList[position]
        clickedTask.expanded = !clickedTask.expanded

        taskAdapter.notifyDataSetChanged()
    }

    override fun onEditClick(taskItem: TaskItem) {
        showEditDialog(taskItem)
    }

    private fun showEditDialog(taskItem: TaskItem) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_new_task, null)
        val editTextTaskName = dialogView.findViewById<EditText>(R.id.editTextTaskName)
        val editTextTaskDescription = dialogView.findViewById<EditText>(R.id.editTextTaskDescription)
        editTextTaskName.setText(taskItem.taskName)
        editTextTaskDescription.setText(taskItem.taskDescription)

        val alertDialogBuilder = AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("Save") { dialog, _ ->
                val editedTaskName = editTextTaskName.text.toString()
                val editedTaskDescription = editTextTaskDescription.text.toString()

                if (editedTaskName != taskItem.taskName) {
                    Toast.makeText(this@TaskActivity, "Task name cannot be changed", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                updateTaskInFirebase(taskItem, editedTaskDescription)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }

        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    private fun updateTaskInFirebase(taskItem: TaskItem, editedTaskDescription: String) {
        val taskRef = firebaseDatabase.reference.child("tasks")
        val query = taskRef.orderByChild("taskName").equalTo(taskItem.taskName)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (taskSnapshot in dataSnapshot.children) {
                    taskSnapshot.ref.child("taskDescription").setValue(editedTaskDescription)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(this@TaskActivity, "Task updated successfully", Toast.LENGTH_SHORT).show()
                                fetchTasks()
                            } else {
                                Toast.makeText(this@TaskActivity, "Error updating task", Toast.LENGTH_SHORT).show()
                            }
                        }
                    return
                }
                Toast.makeText(this@TaskActivity, "Task not found", Toast.LENGTH_SHORT).show()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("FirebaseUpdate", "Error: ${databaseError.message}")
                Toast.makeText(this@TaskActivity, "Error: ${databaseError.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }


}

