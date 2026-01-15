package com.example.deadlinetrackapp

import androidx.room.*

@Dao
interface TaskDao {


    @Query("SELECT * FROM tasks ORDER BY id DESC")
    fun getAll(): List<TaskEntity>

    @Query("SELECT * FROM tasks WHERE id = :id LIMIT 1")
    fun getById(id: Long): TaskEntity?

    @Insert
    fun insert(task: TaskEntity): Long

    @Update
    fun update(task: TaskEntity)

    @Delete
    fun delete(task: TaskEntity)
}
