package com.udacity.locationreminder.locationreminders.reminderslist

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.udacity.locationreminder.R
import com.udacity.locationreminder.authentication.AuthenticationActivity
import com.udacity.locationreminder.base.BaseFragment
import com.udacity.locationreminder.base.NavigationCommand
import com.udacity.locationreminder.databinding.FragmentRemindersBinding
import com.udacity.locationreminder.locationreminders.ReminderDescriptionActivity
import com.udacity.locationreminder.utils.setDisplayHomeAsUpEnabled
import com.udacity.locationreminder.utils.setTitle
import com.udacity.locationreminder.utils.setup
import org.koin.androidx.viewmodel.ext.android.viewModel

class ReminderListFragment : BaseFragment() {
    //use Koin to retrieve the ViewModel instance
    override val _viewModel: RemindersListViewModel by viewModel()
    private lateinit var binding: FragmentRemindersBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_reminders, container, false
            )
        binding.viewModel = _viewModel

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(false)
        setTitle(getString(R.string.app_name))

        binding.refreshLayout.setOnRefreshListener { _viewModel.loadReminders() }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        setupRecyclerView()
        binding.addReminderFAB.setOnClickListener {
            navigateToAddReminder()
        }
    }

    override fun onResume() {
        super.onResume()
        //load the reminders list on the ui
        _viewModel.loadReminders()
    }

    private fun navigateToAddReminder() {
        //use the navigationCommand live data to navigate between the fragments
        _viewModel.navigationCommand.postValue(
            NavigationCommand.To(
                ReminderListFragmentDirections.toSaveReminder()
            )
        )
    }

    private fun setupRecyclerView() {
        val adapter = RemindersListAdapter {
            val bundle = bundleOf(ReminderDescriptionActivity.EXTRA_ReminderDataItem to it)
            findNavController().navigate(R.id.to_reminder_description, bundle)
        }

//        setup the recycler view using the extension function
        binding.reminderssRecyclerView.setup(adapter)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {

            R.id.logout -> {
                _viewModel.deleteAllReminders()
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(context, AuthenticationActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
//        display logout as menu item
        inflater.inflate(R.menu.main_menu, menu)
    }

}
