package com.udacity.locationreminder.locationreminders.reminderslist

import android.os.Bundle
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.google.firebase.auth.FirebaseAuth
import com.udacity.locationreminder.FakeTestRepository
import com.udacity.locationreminder.R
import com.udacity.locationreminder.locationreminders.data.dto.ReminderDTO
import com.udacity.locationreminder.locationreminders.data.local.LocalDB
import com.udacity.locationreminder.locationreminders.data.local.RemindersDatabase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.inject
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest : KoinTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val repository: FakeTestRepository by inject()

    private val testModules = module {
        viewModel {
            RemindersListViewModel(
                get(),
                get() as FakeTestRepository
            )
        }
        single {
            Room.inMemoryDatabaseBuilder(
                getApplicationContext(),
                RemindersDatabase::class.java
            ).allowMainThreadQueries().build()
        }
        single { FakeTestRepository() }
        single { LocalDB.createRemindersDao(androidContext()) }
    }

    @Before
    fun setup() {
        stopKoin()
        startKoin {
            androidContext(getApplicationContext())
            loadKoinModules(testModules)
        }
        FirebaseAuth.getInstance().signInWithEmailAndPassword("test@test.com", "Test123")
    }

    @After
    fun tearDown() = runBlockingTest {
        repository.deleteAllReminders()
        stopKoin()
    }

    @Test
    fun noReminders_showNoDataView() {
        // GIVEN - Repository without data

        // WHEN - Load reminders
        launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)

        // THEN - No data view is shown
        onView(withId(R.id.noDataTextView)).check(matches(isDisplayed()))
    }

    @Test
    fun reminders_showRemindersList() {
        // GIVEN - Repository with data
        val reminder1 = ReminderDTO("Title 1", "Description 1", "Location 1", 37.0, -122.0)
        val reminder2 = ReminderDTO("Title 2", "Description 2", "Location 2", 37.0, -122.0)
        repository.addReminders(reminder1, reminder2)

        // WHEN - Load reminders
        launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)

        // THEN - Stored reminders are shown
        onView(withText("Title 1")).check(matches(isDisplayed()))
        onView(withText("Title 2")).check(matches(isDisplayed()))
    }

    @Test
    fun repositoryError_showSnackBar() {
        // GIVEN - Repository with an error state
        repository.setReturnError(true)

        // WHEN - Load reminders
        launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)

        // THEN - A snack bar is shown
        onView(withId(com.google.android.material.R.id.snackbar_text)).check(matches(withText("Test exception")))
    }

    @Test
    fun clickAddReminderButton_navigateToSaveReminderFragment() {
        // GIVEN - On the home screen
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        val navController = mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        // WHEN - Click on the "+" button
        onView(withId(R.id.addReminderFAB)).perform(click())

        // THEN - We navigate to the save screen
        verify(navController).navigate(ReminderListFragmentDirections.toSaveReminder())
    }
}