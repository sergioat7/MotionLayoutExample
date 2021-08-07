package com.udacity.project4.locationreminders.reminderdescription

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.udacity.project4.base.BaseViewModel
import com.udacity.project4.locationreminders.data.ReminderDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReminderDescriptionViewModel(
    app: Application,
    private val dataSource: ReminderDataSource
) : BaseViewModel(app) {

    fun deleteReminder(reminderId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            dataSource.deleteReminder(reminderId)
        }
    }
}