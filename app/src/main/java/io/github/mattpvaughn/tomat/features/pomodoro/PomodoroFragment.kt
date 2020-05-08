package io.github.mattpvaughn.tomat.features.pomodoro

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.text.format.DateUtils
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import io.github.mattpvaughn.tomat.R
import io.github.mattpvaughn.tomat.application.APP_NAME
import io.github.mattpvaughn.tomat.application.MainActivity
import io.github.mattpvaughn.tomat.data.local.PrefsRepo
import io.github.mattpvaughn.tomat.databinding.FragmentEditorBinding
import io.github.mattpvaughn.tomat.features.pomodoro.PomodoroService.*
import io.github.mattpvaughn.tomat.features.pomodoro.PomodoroService.Companion.COUNTDOWN_BROADCAST_NAME
import io.github.mattpvaughn.tomat.features.pomodoro.PomodoroService.Companion.EXTRA_COUNTDOWN_STATE
import io.github.mattpvaughn.tomat.features.pomodoro.PomodoroService.Companion.EXTRA_SECONDS_REMAINING
import io.github.mattpvaughn.tomat.navigation.Navigator
import javax.inject.Inject

class PomodoroFragment : Fragment() {

    companion object {
        fun newInstance() = PomodoroFragment()

        const val ACTION_SEND_CLICK = "countdown click"
    }

    private lateinit var viewModel: PomodoroViewModel

    @Inject
    lateinit var prefsRepo: PrefsRepo

    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var viewModelFactory: PomodoroViewModelFactory

    private val actionClickIntent = Intent(ACTION_SEND_CLICK)

    override fun onAttach(context: Context) {
        (activity as MainActivity).activityComponent.inject(this)
        super.onAttach(context)

        LocalBroadcastManager.getInstance(context).registerReceiver(
            countDownReceiver, IntentFilter(COUNTDOWN_BROADCAST_NAME)
        )

        val serviceIntent = Intent(context, PomodoroService::class.java)
        context.startService(serviceIntent)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentEditorBinding.inflate(inflater, container, false)

        viewModel = ViewModelProvider(this, viewModelFactory).get(PomodoroViewModel::class.java)


        val state = State.RESTING
        val restingString = State.RESTING_STRING
        val stateState = State.valueOf(restingString)

        binding.settingsIcon.setOnClickListener {
            viewModel.settingsIconClicked()
        }

        viewModel.openSettingsEvent.observe(viewLifecycleOwner, Observer { event ->
            if (!event.hasBeenHandled) {
                event.getContentIfNotHandled()
                navigator.openSettings()
            }
        })

        viewModel.state.observe(viewLifecycleOwner, Observer { stateString ->
            when (stateString) {
                State.BREAK_FINISHED_STRING, State.RESTING_STRING -> {
                    binding.root.background =
                        context!!.getDrawable(R.drawable.resting_background_gradient)
                }
                State.POMODORO_COUNTDOWN_STRING -> {
                    binding.root.background =
                        context!!.getDrawable(R.drawable.active_background_gradient)
                }
                State.BREAK_COUNTDOWN_STRING, State.POMODORO_FINISHED_STRING -> {
                    binding.root.background =
                        context!!.getDrawable(R.drawable.finished_background_gradient)
                }
                else -> throw NoWhenBranchMatchedException()
            }
        })

        viewModel.sendCountDownClickEvent.observe(viewLifecycleOwner, Observer { event ->
            Log.i(APP_NAME, "Countdown click event")
            if (!event.hasBeenHandled) {
                event.getContentIfNotHandled()
                Log.i(APP_NAME, "Handling countdown click event")
                LocalBroadcastManager.getInstance(context!!).sendBroadcast(actionClickIntent)
            }
        })

        viewModel.secondsRemaining.observe(viewLifecycleOwner, Observer { secondsRemaining ->
            binding.timerText.text = DateUtils.formatElapsedTime(secondsRemaining.toLong())
        })

        binding.root.setOnClickListener {
            viewModel.timerClicked()
        }

        return binding.root
    }

    override fun onDetach() {
        LocalBroadcastManager.getInstance(context!!).unregisterReceiver(countDownReceiver)
        super.onDetach()
    }

    private val countDownReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent != null && intent.extras != null) {
                val extras = intent.extras!!
                val state: String = extras.getString(EXTRA_COUNTDOWN_STATE) ?: ""
                val secondsRemaining: Int = extras.getInt(EXTRA_SECONDS_REMAINING)
                viewModel.updateState(state, secondsRemaining)
            }
        }
    }
}
