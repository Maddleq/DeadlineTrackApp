package com.example.deadlinetrackapp

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.deadlinetrackapp.databinding.ActivityMainBinding
import androidx.recyclerview.widget.ItemTouchHelper
import com.google.android.material.dialog.MaterialAlertDialogBuilder


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: TaskAdapter
    private lateinit var dao: TaskDao

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        dao = AppDatabase.getInstance(applicationContext).taskDao()
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        adapter = TaskAdapter { task ->
            openEdit(taskId = task.id) // редактирование
        }

        binding.rvTasks.layoutManager = LinearLayoutManager(this)
        binding.rvTasks.adapter = adapter
        setupSwipeToDelete()

        binding.fabAdd.setOnClickListener {
            val intent = Intent(this, TaskEditActivity::class.java)
            startActivity(intent)
        }
    }
    override fun onResume() {
        super.onResume()

        Thread {
            val list = dao.getAll()
            val uiList = list.map { TaskUi(it.id, it.title, it.customer) }
            runOnUiThread {
                adapter.submitList(uiList)
            }
        }.start()
    }
    private fun openEdit(taskId: Long?) {
        val intent = Intent(this, TaskEditActivity::class.java)
        taskId?.let { intent.putExtra(TaskEditActivity.EXTRA_TASK_ID, it) }
        startActivity(intent) // пока без результата, для MVP ок
    }
    private fun setupSwipeToDelete() {
        val callback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val pos = viewHolder.adapterPosition
                val taskUi = adapter.getItem(pos)

                MaterialAlertDialogBuilder(this@MainActivity)
                    .setTitle(R.string.delete_confirm_title)
                    .setMessage(R.string.delete_confirm_message)
                    .setPositiveButton(R.string.yes) { _, _ ->
                        Thread {
                            val entity = dao.getById(taskUi.id)
                            if (entity != null) dao.delete(entity)

                            val list = dao.getAll()
                            val uiList = list.map { TaskUi(it.id, it.title, it.customer) }
                            runOnUiThread { adapter.submitList(uiList) }
                        }.start()
                    }
                    .setNegativeButton(R.string.no) { _, _ ->
                        adapter.notifyItemChanged(pos) // вернули строку на место
                    }
                    .setOnCancelListener {
                        adapter.notifyItemChanged(pos) // если закрыли диалог
                    }
                    .show()
            }
        }

        ItemTouchHelper(callback).attachToRecyclerView(binding.rvTasks)
    }

}

class VerticalSpaceItemDecoration(private val spacePx: Int) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        outRect.bottom = spacePx
    }
}
