package com.udacity.project4

import android.app.Activity
import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderdescription.ReminderDescriptionViewModel
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorActivity
import com.udacity.project4.utils.EspressoIdlingResource
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.not
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
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get

@RunWith(AndroidJUnit4::class)
@LargeTest
//END TO END test to black box test the app
class RemindersActivityTest :
    AutoCloseKoinTest() {// Extended Koin Test - embed autoclose @after method to close Koin after every test

    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application

    // An Idling Resource that waits for Data Binding to have no pending bindings
    private val dataBindingIdlingResource = DataBindingIdlingResource()

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in out testing.
     */
    @Before
    fun init() {
        stopKoin()//stop the original app koin
        appContext = getApplicationContext()
        val testModules = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            viewModel {
                ReminderDescriptionViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single {
                SaveReminderViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }//DO NOT remove the cast
            single { LocalDB.createRemindersDao(appContext) }
        }
        //declare a new koin module
        startKoin {
            androidContext(getApplicationContext())
            loadKoinModules(testModules)
        }
        //Get our real repository
        repository = get()

        //clear the data to start fresh
        runBlocking {
            repository.deleteAllReminders()
        }
    }

    @Before
    fun setup() {
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)

        runBlocking {
            FirebaseAuth.getInstance().signOut()
            FirebaseAuth.getInstance().signInWithEmailAndPassword("test@test.com", "Test123")
        }
    }

    @After
    fun unregisterIdlingResources() {
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
    }

    @Test
    fun addReminder() {
        // Start up Reminders screen
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        // Open save reminder view and set a title
        onView(withId(R.id.addReminderFAB)).perform(click())
        onView(withId(R.id.reminderTitle)).perform(typeText("Title"), closeSoftKeyboard())

        // Open select location view and create a marker
        onView(withId(R.id.selectLocation)).perform(click())
        onView(withId(R.id.map)).check(matches(isDisplayed()))
        onView(withContentDescription("My Location")).perform(click())
        onView(withId(R.id.button_save)).perform(click())
        onView(withId(com.google.android.material.R.id.snackbar_text)).check(matches(withText("Please select location")))
        runBlocking { delay(3000) }
        onView(withId(R.id.map)).perform(longClick())
        onView(withId(R.id.button_save)).perform(click())

        // Save reminder and verify has been created
        onView(withId(R.id.saveReminder)).perform(click())
        onView(withText("Reminder Saved !")).inRoot(withDecorView(not(`is`(getActivity(activityScenario)?.window?.decorView)))).check(matches(isDisplayed()))
        onView(withText("Title")).check(matches(isDisplayed()))

        // Make sure the activity is closed before resetting the db
        activityScenario.close()
    }

    @Test
    fun seeReminder() = runBlocking {
        val reminder = ReminderDTO("Title", "Description", "Location", 37.0, -122.0)
        repository.saveReminder(reminder)

        // Start up Reminders screen
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        // Click on the reminder on the list and verify that all the data is correct
        onView(withText("Title")).perform(click())
        onView(withId(R.id.tvTitle)).check(matches(withText("Title")))
        onView(withId(R.id.tvDescription)).check(matches(withText("Description")))
        onView(withId(R.id.tvLocation)).check(matches(withText("Location")))
        onView(withId(R.id.tvLatLng)).check(matches(withText("Lat: 37.00000, Long: -122.00000")))

        // Make sure the activity is closed before resetting the db
        activityScenario.close()
    }

    @Test
    fun addReminder_seeReminder_deleteReminder() {
        // Start up Reminders screen
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        // Open save reminder view and set a title
        onView(withId(R.id.addReminderFAB)).perform(click())
        onView(withId(R.id.reminderTitle)).perform(typeText("Title"), closeSoftKeyboard())

        // Open select location view and create a marker
        onView(withId(R.id.selectLocation)).perform(click())
        onView(withId(R.id.map)).check(matches(isDisplayed()))
        onView(withContentDescription("My Location")).perform(click())
        onView(withId(R.id.button_save)).perform(click())
        onView(withId(com.google.android.material.R.id.snackbar_text)).check(matches(withText("Please select location")))
        runBlocking { delay(3000) }
        onView(withId(R.id.map)).perform(longClick())
        onView(withId(R.id.button_save)).perform(click())

        // Save reminder and verify has been created
        onView(withId(R.id.saveReminder)).perform(click())
        onView(withText("Reminder Saved !")).inRoot(withDecorView(not(`is`(getActivity(activityScenario)?.window?.decorView)))).check(matches(isDisplayed()))
        onView(withText("Title")).check(matches(isDisplayed()))

        // Click on the new reminder on the list and verify that all the data is correct
        onView(withText("Title")).perform(click())
        onView(withId(R.id.tvTitle)).check(matches(withText("Title")))
        onView(withId(R.id.tvLocation)).check(matches(withText("Custom marker")))

        // Delete reminder and verify has been deleted
        onView(withId(R.id.button_delete)).perform(click())
        onView(withText("Title")).check(doesNotExist())

        // Make sure the activity is closed before resetting the db
        activityScenario.close()
    }

    // get activity context
    private fun getActivity(activityScenario: ActivityScenario<RemindersActivity>): Activity? {
        var activity: Activity? = null
        activityScenario.onActivity {
            activity = it
        }
        return activity
    }
}
