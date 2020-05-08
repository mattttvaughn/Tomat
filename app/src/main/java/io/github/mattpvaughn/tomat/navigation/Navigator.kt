package io.github.mattpvaughn.tomat.navigation

import android.util.Log
import androidx.fragment.app.FragmentManager
import io.github.mattpvaughn.tomat.features.settings.SettingsFragment
import io.github.mattpvaughn.tomat.R
import io.github.mattpvaughn.tomat.application.APP_NAME
import io.github.mattpvaughn.tomat.features.pomodoro.PomodoroFragment

class Navigator(private val fragmentManager: FragmentManager) {

    private val pomodoroFragment by lazy {
        PomodoroFragment.newInstance()
    }

    private val settingsFragment by lazy {
        SettingsFragment.newInstance()
    }

    fun openPomodoro() {
        fragmentManager.beginTransaction().replace(R.id.fragNavHost, pomodoroFragment).commit()
    }

    fun openSettings() {
        fragmentManager.beginTransaction().replace(R.id.fragNavHost, settingsFragment)
            .addToBackStack(null).commit()
    }

    fun onBackPressed() {
        fragmentManager.popBackStack()
    }

}