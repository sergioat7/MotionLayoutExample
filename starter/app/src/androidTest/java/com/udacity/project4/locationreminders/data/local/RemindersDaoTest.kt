package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

    private lateinit var database: RemindersDatabase

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun initDb() {
        // Using an in-memory database so that the information stored here disappears when the
        // process is killed.
        database = Room.inMemoryDatabaseBuilder(
            getApplicationContext(),
            RemindersDatabase::class.java
        ).build()
    }

    @After
    fun closeDb() = database.close()

    @Test
    fun insertReminderAndGetById() = runBlockingTest {

        // GIVEN - Insert a reminder.
        val reminder = ReminderDTO("Title", "Description", "Location", 37.0, -122.0)
        database.reminderDao().saveReminder(reminder)

        // WHEN - Get the reminder by id from the database.
        val loaded = database.reminderDao().getReminderById(reminder.id)

        // THEN - The loaded data contains the expected values.
        assertThat(loaded as ReminderDTO, notNullValue())
        assertThat(loaded.id, `is`(reminder.id))
        assertThat(loaded.title, `is`(reminder.title))
        assertThat(loaded.description, `is`(reminder.description))
        assertThat(loaded.location, `is`(reminder.location))
        assertThat(loaded.latitude, `is`(reminder.latitude))
        assertThat(loaded.longitude, `is`(reminder.longitude))
    }

    @Test
    fun insertReminderAndGetAll() = runBlockingTest {

        // GIVEN - Insert a reminder.
        val reminder = ReminderDTO("Title", "Description", "Location", 37.0, -122.0)
        database.reminderDao().saveReminder(reminder)

        // WHEN - Get all reminders
        val loadedData = database.reminderDao().getReminders()

        // THEN - There must be just one reminder
        assertThat(loadedData.size, equalTo(1))
    }

    @Test
    fun noRemindersAndGetAll() = runBlockingTest {

        // WHEN - Get all reminders
        val loadedData = database.reminderDao().getReminders()

        // THEN - There must be no reminders
        assertThat(loadedData.size, equalTo(0))
    }

    @Test
    fun insertRemindersAndDeleteAll() = runBlockingTest {

        // GIVEN - Insert a reminder.
        val reminder1 = ReminderDTO("Title 1", "Description 1", "Location 1", 37.0, -122.0)
        val reminder2 = ReminderDTO("Title 2", "Description 2", "Location 2", 37.0, -122.0)
        database.reminderDao().saveReminder(reminder1)
        database.reminderDao().saveReminder(reminder2)

        // WHEN - Delete all reminders
        database.reminderDao().deleteAllReminders()

        // THEN - There must be no reminders
        val loadedData = database.reminderDao().getReminders()
        assertThat(loadedData.size, equalTo(0))
    }

    @Test
    fun noRemindersAndDeleteAll() = runBlockingTest {

        // WHEN - Delete all reminders
        database.reminderDao().deleteAllReminders()

        // THEN - There must be no reminders
        val loadedData = database.reminderDao().getReminders()
        assertThat(loadedData.size, equalTo(0))
    }

    @Test
    fun insertReminderAndDeleteIt() = runBlockingTest {

        // GIVEN - Insert a reminder.
        val reminder = ReminderDTO("Title", "Description", "Location", 37.0, -122.0)
        database.reminderDao().saveReminder(reminder)

        // WHEN - Delete the reminder
        database.reminderDao().deleteReminder(reminder)

        // THEN - There must be no such reminder
        val loaded = database.reminderDao().getReminderById(reminder.id)
        assertThat(loaded, nullValue())
    }

    @Test
    fun deleteNonExistentReminder() = runBlockingTest {

        // WHEN - Delete non-existent reminder
        val reminder = ReminderDTO("Title", "Description", "Location", 37.0, -122.0)
        database.reminderDao().deleteReminder(reminder)

        // THEN - There is no such reminder
        val loaded = database.reminderDao().getReminderById(reminder.id)
        assertThat(loaded, nullValue())
    }
}