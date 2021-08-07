package com.udacity.project4.locationreminders.reminderdescription

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.udacity.project4.R
import com.udacity.project4.databinding.ActivityReminderDescriptionBinding
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Activity that displays the reminder details after the user clicks on the notification
 */
class ReminderDescriptionActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_ReminderDataItem = "EXTRA_ReminderDataItem"

        //        receive the reminder object after the user clicks on the notification
        fun newIntent(context: Context, reminderDataItem: ReminderDataItem): Intent {
            val intent = Intent(context, ReminderDescriptionActivity::class.java)
            intent.putExtra(EXTRA_ReminderDataItem, reminderDataItem)
            return intent
        }
    }

    //use Koin to retrieve the ViewModel instance
    private val _viewModel: ReminderDescriptionViewModel by viewModel()
    private lateinit var binding: ActivityReminderDescriptionBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_reminder_description
        )
        (intent.extras?.get(EXTRA_ReminderDataItem) as? ReminderDataItem)?.let { reminder ->
            binding.reminderDataItem = reminder
            binding.buttonDelete.setOnClickListener {
                _viewModel.deleteReminder(reminder.id)
                finish()
            }
        }
    }
}
