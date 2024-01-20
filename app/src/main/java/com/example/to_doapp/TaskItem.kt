package com.example.to_doapp

data class TaskItem(
    val taskName: String = "",
    val taskDescription: String = "",
    val completed: Boolean = false,
    var expanded: Boolean = false
)
