package io.github.mattpvaughn.tomat.features.settings

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import io.github.mattpvaughn.tomat.application.APP_NAME
import io.github.mattpvaughn.tomat.application.Injector
import io.github.mattpvaughn.tomat.application.MainActivity
import io.github.mattpvaughn.tomat.databinding.FragmentSettingsBinding
import io.github.mattpvaughn.tomat.navigation.Navigator
import io.github.mattpvaughn.tomat.util.observeEvent
import javax.inject.Inject


class SettingsFragment : Fragment() {

    companion object {
        @JvmStatic
        fun newInstance() = SettingsFragment()
    }

    @Inject
    lateinit var settingsViewModelFactory: SettingsViewModelFactory

    @Inject
    lateinit var navigator: Navigator

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (activity as MainActivity).activityComponent.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        Log.i(APP_NAME, "Settings fragment onCreateView")

        val context = context!!

        val binding = FragmentSettingsBinding.inflate(inflater, container, false)
        val viewModel =
            ViewModelProvider(this, settingsViewModelFactory).get(SettingsViewModel::class.java)

        viewModel.prefs.observe(viewLifecycleOwner, Observer { prefs ->
            binding.settingsList.setPreferences(prefs)
        })

        viewModel.messageForUser.observeEvent(viewLifecycleOwner) {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }

        viewModel.openLicenseActivity.observe(viewLifecycleOwner, Observer { event ->
            if (!event.hasBeenHandled) {
                event.getContentIfNotHandled()
                startActivity(Intent(context, OssLicensesMenuActivity::class.java))
            }
        })

        // tbh using fragmentmanager default behavior would be okay
        binding.settingsToolbar.setNavigationOnClickListener {
            navigator.onBackPressed()
        }

//        (activity as MainActivity).setSupportActionBar(binding.settingsToolbar)

        return binding.root
    }
}
