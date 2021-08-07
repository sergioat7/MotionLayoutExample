package com.udacity.project4.locationreminders.savereminder

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PointOfInterest
import com.udacity.project4.MyApp
import com.udacity.project4.R
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.getOrAwaitValue
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {

    // Subject under test
    private lateinit var saveReminderViewModel: SaveReminderViewModel

    private lateinit var fakeDataSource: FakeDataSource

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    // Set the main coroutines dispatcher for unit testing.
    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setupViewModel() {

        fakeDataSource = FakeDataSource(mutableListOf())
        saveReminderViewModel = SaveReminderViewModel(
            ApplicationProvider.getApplicationContext<MyApp>(),
            fakeDataSource
        )
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun clearAllLiveDataValues() {
        // GIVEN - All LiveData objects have value
        saveReminderViewModel.reminderTitle.value = "Title"
        saveReminderViewModel.reminderDescription.value = "Description"
        saveReminderViewModel.reminderSelectedLocationStr.value = "Location"
        saveReminderViewModel.selectedPOI.value =
            PointOfInterest(LatLng(37.0, -122.0), "1", "Location")
        saveReminderViewModel.latitude.value = 37.0
        saveReminderViewModel.longitude.value = -122.0

        assertThat(
            saveReminderViewModel.reminderTitle.value,
            `is`(not(nullValue()))
        )
        assertThat(
            saveReminderViewModel.reminderDescription.value,
            `is`(not(nullValue()))
        )
        assertThat(
            saveReminderViewModel.reminderSelectedLocationStr.value,
            `is`(not(nullValue()))
        )
        assertThat(
            saveReminderViewModel.selectedPOI.value,
            `is`(not(nullValue()))
        )
        assertThat(
            saveReminderViewModel.latitude.value,
            `is`(not(nullValue()))
        )
        assertThat(
            saveReminderViewModel.longitude.value,
            `is`(not(nullValue()))
        )

        // WHEN - Clear objects
        saveReminderViewModel.onClear()

        // THEN - ALl LiveData objects have a null value
        assertThat(
            saveReminderViewModel.reminderTitle.value,
            `is`(nullValue())
        )
        assertThat(
            saveReminderViewModel.reminderDescription.value,
            `is`(nullValue())
        )
        assertThat(
            saveReminderViewModel.reminderSelectedLocationStr.value,
            `is`(nullValue())
        )
        assertThat(
            saveReminderViewModel.selectedPOI.value,
            `is`(nullValue())
        )
        assertThat(
            saveReminderViewModel.latitude.value,
            `is`(nullValue())
        )
        assertThat(
            saveReminderViewModel.longitude.value,
            `is`(nullValue())
        )
    }

    @Test
    fun saveReminderWithAnEmptyDatabase() {
        // GIVEN - Empty reminders database

        // WHEN - Save a new reminder
        val newReminder =
            ReminderDataItem("New title", "New description", "New location", 37.0, -122.0)
        saveReminderViewModel.saveReminder(newReminder)

        // THEN - There must be one reminder
        assertThat(fakeDataSource.reminders.size, equalTo(1))
    }

    @Test
    fun saveReminderWithANonEmptyDatabase() {
        // GIVEN - A reminder stored in the database
        val reminder = ReminderDTO("Title", "Description", "Location", 37.0, -122.0)
        fakeDataSource.addReminders(reminder)

        // WHEN - Save a reminder
        val newReminder =
            ReminderDataItem("New title", "New description", "New location", 37.0, -122.0)
        saveReminderViewModel.saveReminder(newReminder)

        // THEN - There must be two reminders
        assertThat(fakeDataSource.reminders.size, equalTo(2))
    }

    @Test
    fun saveReminderAndCheckToast() {
        // GIVEN - Empty reminders database

        // WHEN - Save a new reminder
        val newReminder =
            ReminderDataItem("New title", "New description", "New location", 37.0, -122.0)
        saveReminderViewModel.saveReminder(newReminder)

        // THEN - Successful message must be shown
        assertThat(saveReminderViewModel.showToast.getOrAwaitValue(), equalTo("Reminder Saved !"))
    }

    @Test
    fun saveReminderAndCheckLoading() {
        // GIVEN - Empty reminders database
        mainCoroutineRule.pauseDispatcher()

        // WHEN - Save a reminder
        val newReminder =
            ReminderDataItem("New title", "New description", "New location", 37.0, -122.0)
        saveReminderViewModel.saveReminder(newReminder)

        // THEN - Loading must be shown
        assertThat(
            saveReminderViewModel.showLoading.getOrAwaitValue(),
            equalTo(true)
        )
        mainCoroutineRule.resumeDispatcher()
        assertThat(
            saveReminderViewModel.showLoading.getOrAwaitValue(),
            equalTo(false)
        )
    }

    @Test
    fun saveReminderWithInvalidDataAndCheckShowSnackBar() {
        // GIVEN - Empty reminders database

        // WHEN - Save a reminder with invalid data
        val newReminder = ReminderDataItem(null, "New description", "New location", 37.0, -122.0)
        val validEnteredData = saveReminderViewModel.validateEnteredData(newReminder)

        // THEN - Snack bar must be shown
        assertThat(validEnteredData, equalTo(false))
        assertThat(
            saveReminderViewModel.showSnackBarInt.getOrAwaitValue(),
            equalTo(R.string.err_enter_title)
        )
    }
}