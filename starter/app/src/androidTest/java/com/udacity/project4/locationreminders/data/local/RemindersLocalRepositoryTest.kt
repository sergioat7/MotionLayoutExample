package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

    // Subject under test
    private lateinit var remindersLocalRepository: RemindersLocalRepository

    private lateinit var database: RemindersDatabase

    private val reminder1 = ReminderDTO("Title 1", "Description 1", "Location 1", 37.0, -122.0)
    private val reminder2 = ReminderDTO("Title 2", "Description 2", "Location 2", 37.0, -122.0)

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        // Using an in-memory database so that the information stored here disappears when the
        // process is killed.
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).allowMainThreadQueries().build()

        remindersLocalRepository =
            RemindersLocalRepository(database.reminderDao(), Dispatchers.Main)
    }

    @After
    fun closeDb() = database.close()

    @Test
    fun getAllReminders() = runBlocking {
        // GIVEN - Two reminders saved
        addReminders(reminder1, reminder2)

        // WHEN - Get all reminders
        val result = remindersLocalRepository.getReminders()

        // THEN - There must be two reminders
        assertThat(result is Result.Success, equalTo(true))
        result as Result.Success
        assertThat(result.data.size, equalTo(2))
    }

    @Test
    fun getAllRemindersWithEmptyDatabase() = runBlocking {
        // GIVEN - No reminders saved

        // WHEN - Get all reminders
        val result = remindersLocalRepository.getReminders()

        // THEN - There must be no reminders
        assertThat(result is Result.Success, equalTo(true))
        result as Result.Success
        assertThat(result.data.size, equalTo(0))
    }

    @Test
    fun getReminderById() = runBlocking {
        // GIVEN - Two reminders saved
        addReminders(reminder1, reminder2)

        // WHEN - Get a specific reminder
        val result = remindersLocalRepository.getReminder(reminder1.id)

        // THEN - Specific reminder returned
        assertThat(result is Result.Success, equalTo(true))
        result as Result.Success
        assertThat(result.data, equalTo(reminder1))
    }

    @Test
    fun getNonExistentReminder() = runBlocking {
        // GIVEN - One reminder saved
        addReminders(reminder1)

        // WHEN - Get a specific reminder
        val result = remindersLocalRepository.getReminder(reminder2.id)

        // THEN - An error should be shown
        assertThat(result is Result.Error, equalTo(true))
        result as Result.Error
        assertThat(result.message, equalTo("Reminder not found!"))
    }

    @Test
    fun saveReminder() = runBlocking {
        // GIVEN - No reminders saved

        // WHEN - Save a reminder
        remindersLocalRepository.saveReminder(reminder1)

        // THEN - There must be one reminder
        assertThat(database.reminderDao().getReminders().size, equalTo(1))
    }

    @Test
    fun deleteAllReminders() = runBlocking {
        // GIVEN - Two reminders saved
        addReminders(reminder1, reminder2)

        // WHEN - Delete all reminders
        remindersLocalRepository.deleteAllReminders()

        // THEN - The database must be empty
        assertThat(database.reminderDao().getReminders().size, equalTo(0))
    }

    @Test
    fun deleteReminder() = runBlocking {
        // GIVEN - Two reminders saved
        addReminders(reminder1, reminder2)

        // WHEN - Delete a specific reminder
        remindersLocalRepository.deleteReminder(reminder1.id)

        // THEN - There must be just one reminder
        val loadedData = database.reminderDao().getReminders()
        assertThat(loadedData.size, equalTo(1))
        assertThat(loadedData.first(), equalTo(reminder2))
    }

    private suspend fun addReminders(vararg reminders: ReminderDTO) {
        for (reminder in reminders) {
            database.reminderDao().saveReminder(reminder)
        }
    }
}