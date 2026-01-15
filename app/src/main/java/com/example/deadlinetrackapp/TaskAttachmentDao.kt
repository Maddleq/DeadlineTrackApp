package com.example.deadlinetrackapp

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface TaskAttachmentDao {

    @Insert
    fun insert(entity: TaskAttachmentEntity): Long

    @Query("SELECT * FROM task_attachments WHERE taskId = :taskId")
    fun getByTaskId(taskId: Long): List<TaskAttachmentEntity>

    @Query("DELETE FROM task_attachments WHERE taskId = :taskId")
    fun deleteByTaskId(taskId: Long)
}
