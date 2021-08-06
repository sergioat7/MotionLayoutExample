package com.udacity.locationreminder.locationreminders.reminderslist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.locationreminder.MyApp
import com.udacity.locationreminder.locationreminders.MainCoroutineRule
import com.udacity.locationreminder.locationreminders.data.FakeTestRepository
import com.udacity.locationreminder.locationreminders.data.dto.ReminderDTO
import com.udacity.locationreminder.locationreminders.getOrAwaitValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    // Subject under test
    private lateinit var remindersListViewModel: RemindersListViewModel

    private lateinit var fakeRepository: FakeTestRepository

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    // Set the main coroutines dispatcher for unit testing.
    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setupViewModel() {

        val reminder1 = ReminderDTO("Title 1", "Description 1", "Location 1", 37.0, -122.0)
        val reminder2 = ReminderDTO("Title 2", "Description 2", "Location 2", 37.0, -122.0)
        val reminder3 = ReminderDTO("Title 3", "Description 3", "Location 3", 37.0, -122.0)

        fakeRepository = FakeTestRepository()
        fakeRepository.addReminders(reminder1, reminder2, reminder3)
        remindersListViewModel =
            RemindersListViewModel(getApplicationContext<MyApp>(), fakeRepository, Dispatchers.Main)
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun loadReminders() {
        // GIVEN - Three reminders saved

        // WHEN - Load all reminders
        remindersListViewModel.loadReminders()

        // THEN - There must be three reminders
        assertThat(
            remindersListViewModel.remindersList.getOrAwaitValue()?.size, equalTo(3)
        )
    }

    @Test
    fun deleteReminders() = runBlockingTest {
        // GIVEN - Three reminders saved

        // WHEN - Delete all reminders
        remindersListViewModel.deleteAllReminders()

        // THEN - There must be no reminders
        remindersListViewModel.loadReminders()
        assertThat(remindersListViewModel.remindersList.getOrAwaitValue()?.size, equalTo(0))
    }

    @Test
    fun deleteRemindersAndCheckShowNoData() {
        // GIVEN - Three reminders saved

        // WHEN - Delete all reminders
        remindersListViewModel.deleteAllReminders()

        // THEN - No data view must be shown
        remindersListViewModel.loadReminders()
        assertThat(remindersListViewModel.showNoData.getOrAwaitValue(), equalTo(true))
    }

    @Test
    fun loadRemindersAndCheckLoading() {
        // GIVEN - Three reminders saved
        mainCoroutineRule.pauseDispatcher()

        // WHEN - Load all reminders
        remindersListViewModel.loadReminders()

        // THEN - Loading must be shown
        assertThat(remindersListViewModel.showLoading.getOrAwaitValue(), equalTo(true))
        mainCoroutineRule.resumeDispatcher()
        assertThat(remindersListViewModel.showLoading.getOrAwaitValue(), equalTo(false))
    }

    @Test
    fun loadRemindersAndCheckShowSnackBar() {
        // GIVEN - Repository in a error state
        fakeRepository.setReturnError(true)

        // WHEN - Load all reminders
        remindersListViewModel.loadReminders()

        // THEN - Snack bar must be shown
        assertThat(remindersListViewModel.showSnackBar.getOrAwaitValue(), equalTo("Test exception"))
        assertThat(
            remindersListViewModel.remindersList.value.isNullOrEmpty(),
            equalTo(true)
        )
    }
}