package com.example.deadlinetrackapp

import androidx.activity.addCallback
import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.example.deadlinetrackapp.databinding.ActivityTaskEditBinding
import java.util.Calendar
import android.util.Log
import android.content.Intent
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts
class TaskEditActivity : AppCompatActivity() {
    private lateinit var dao: TaskDao
    private var selectedDeadlineMillis: Long? = null

    private var originalEntity: TaskEntity? = null

    private val pickedUris = mutableListOf<android.net.Uri>()

    private fun openAttachment(uriString: String) {
        val uri = Uri.parse(uriString)

        val mime = contentResolver.getType(uri) ?: "*/*"

        val viewIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mime)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        startActivity(Intent.createChooser(viewIntent, getString(R.string.open_with)))
    }

    private fun buildEntityFromUi(taskId: Long): TaskEntity {
        return TaskEntity(
            id = taskId,
            title = binding.etTitle.text.toString().trim(),
            description = binding.etDescription.text.toString().trim(),
            statusCode = binding.spStatus.selectedItemPosition,
            priorityCode = binding.spPriority.selectedItemPosition,
            paymentStatusCode = binding.spPaymentStatus.selectedItemPosition,
            deadlineMillis = selectedDeadlineMillis
        )
    }

    private fun hasUnsavedChanges(taskId: Long): Boolean {
        val orig = originalEntity ?: return false
        return buildEntityFromUi(taskId) != orig
    }

    companion object {
        const val EXTRA_TASK_ID = "task_id"
    }

    private lateinit var binding: ActivityTaskEditBinding

    private fun renderDeadline(millis: Long?) {
        if (millis == null) {
            binding.tvDeadlineValue.text = getString(R.string.deadline_not_set_full)
        } else {
            val sdf = java.text.SimpleDateFormat("dd.MM.yyyy", java.util.Locale.getDefault())
            val dateStr = sdf.format(java.util.Date(millis))
            binding.tvDeadlineValue.text = getString(R.string.deadline_prefix, dateStr)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    private val openDocument =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val data = result.data ?: return@registerForActivityResult
            val uri = data.data ?: return@registerForActivityResult

            val takeFlags = data.flags and
                    (Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)

            contentResolver.takePersistableUriPermission(uri, takeFlags)

            pickedUris.add(uri)
            addAttachment(uri)
        }

    private fun addAttachment(uri: Uri) {
        val uriStr = uri.toString()
        val tv = android.widget.TextView(this).apply {
            text = uriStr
            setPadding(0, 8, 0, 8)
            isClickable = true
            setOnClickListener { openAttachment(uriStr) }
        }
        binding.attachmentsContainer.addView(tv)
    }

    private fun renderAttachments(list: List<TaskAttachmentEntity>) {
        binding.attachmentsContainer.removeAllViews()

        list.forEach { a ->
            val tv = android.widget.TextView(this).apply {
                text = a.uri
                setPadding(0, 8, 0, 8)
                isClickable = true
                setOnClickListener {
                    openAttachment(a.uri)   // <-- ВОТ ОНО
                }
            }
            binding.attachmentsContainer.addView(tv)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        binding = ActivityTaskEditBinding.inflate(layoutInflater)

        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        binding.toolbar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        dao = AppDatabase.getInstance(applicationContext).taskDao()

        val attachmentDao = AppDatabase.getInstance(applicationContext).attachmentDao()

        val taskId = intent.getLongExtra(EXTRA_TASK_ID, -1L)

        onBackPressedDispatcher.addCallback(this) {
            if (originalEntity == null) { // если ещё не успели загрузить
                finish()
                return@addCallback
            }

            val compareId = if (taskId == -1L) 0L else taskId
            if (!hasUnsavedChanges(compareId)) {
                finish()
                return@addCallback
            }

            com.google.android.material.dialog.MaterialAlertDialogBuilder(this@TaskEditActivity)
                .setTitle(R.string.discard_changes_title)
                .setMessage(R.string.discard_changes_message)
                .setPositiveButton(R.string.yes) { _, _ -> finish() }
                .setNegativeButton(R.string.no, null)
                .show()
        }

        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed() // чтобы сработал диалог при изменениях
        }

        binding.btnAddAttachment.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "*/*"
                putExtra(Intent.EXTRA_MIME_TYPES, arrayOf(
                    "image/*",
                    "application/pdf",
                    "application/zip",
                    "application/x-zip-compressed"
                ))
            }
            openDocument.launch(intent)
        }

        binding.btnSave.setOnClickListener {
            val title = binding.etTitle.text.toString().trim()
            val description = binding.etDescription.text.toString().trim()
            val customer = binding.etCustomer.text.toString().trim()

            if (title.isEmpty()) {
                binding.etTitle.error = getString(R.string.error_title_required)
                return@setOnClickListener
            }

            val entity = TaskEntity(
                title = title,
                description = description,
                customer = customer,
                statusCode = binding.spStatus.selectedItemPosition,
                priorityCode = binding.spPriority.selectedItemPosition,
                paymentStatusCode = binding.spPaymentStatus.selectedItemPosition,
                deadlineMillis = selectedDeadlineMillis
            )

            Thread {
                val savedTaskId: Long =
                    if (taskId != -1L) {
                        dao.update(entity.copy(id = taskId))
                        taskId
                    } else {
                        dao.insert(entity) // вернёт id новой задачи
                    }

                // Вариант: для редактирования можно очистить и вставить заново
                attachmentDao.deleteByTaskId(savedTaskId)

                pickedUris.forEach { uri ->
                    attachmentDao.insert(
                        TaskAttachmentEntity(
                            taskId = savedTaskId,
                            uri = uri.toString()
                        )
                    )
                }

                runOnUiThread { finish() }
            }.start()
        }

        binding.spStatus.adapter =
            ArrayAdapter.createFromResource(
                this,
                R.array.status_items,
                android.R.layout.simple_spinner_item
            ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        binding.spPriority.adapter =
            ArrayAdapter.createFromResource(
                this,
                R.array.priority_items,
                android.R.layout.simple_spinner_item
            ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        binding.spPaymentStatus.adapter =
            ArrayAdapter.createFromResource(
                this,
                R.array.payment_items,
                android.R.layout.simple_spinner_item
            ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }


        if (taskId != -1L) {
            Thread {
                val entityFromDb = dao.getById(taskId)
                val attachmentsFromDb = attachmentDao.getByTaskId(taskId)

                runOnUiThread {
                    if (entityFromDb != null) {
                        originalEntity = entityFromDb

                        binding.etTitle.setText(entityFromDb.title)
                        binding.etDescription.setText(entityFromDb.description)
                        binding.etCustomer.setText(entityFromDb.customer)

                        binding.spStatus.setSelection(entityFromDb.statusCode)
                        binding.spPriority.setSelection(entityFromDb.priorityCode)
                        binding.spPaymentStatus.setSelection(entityFromDb.paymentStatusCode)

                        selectedDeadlineMillis = entityFromDb.deadlineMillis
                        renderDeadline(selectedDeadlineMillis)

                        renderAttachments(attachmentsFromDb)
                    }
                }
            }.start()
        }


        binding.btnPickDeadline.setOnClickListener {
            val c = Calendar.getInstance()

            val dialog = DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    // month = 0..11
                    val cal = Calendar.getInstance().apply {
                        set(Calendar.YEAR, year)
                        set(Calendar.MONTH, month)
                        set(Calendar.DAY_OF_MONTH, dayOfMonth)
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }

                    selectedDeadlineMillis = cal.timeInMillis
                    renderDeadline(selectedDeadlineMillis)
                },
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH)
            )

            dialog.show()
        }
        renderDeadline(selectedDeadlineMillis) // покажет deadline_not_set
        if (taskId == -1L) {
            originalEntity = buildEntityFromUi(0L) // считаем начальное состояние "без изменений"
        }
    }
}