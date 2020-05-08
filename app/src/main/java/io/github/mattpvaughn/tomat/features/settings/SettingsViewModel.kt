package io.github.mattpvaughn.tomat.features.settings

import androidx.lifecycle.*
import io.github.mattpvaughn.tomat.data.local.PrefsRepo
import io.github.mattpvaughn.tomat.data.local.PrefsRepo.Companion.KEY_BREAK_TIMER_DURATION_MINUTES
import io.github.mattpvaughn.tomat.data.local.PrefsRepo.Companion.KEY_NORMAL_TIMER_DURATION_MINUTES
import io.github.mattpvaughn.tomat.util.Event
import io.github.mattpvaughn.tomat.util.postEvent
import javax.inject.Inject


class SettingsViewModel @Inject constructor(
    private val prefsRepo: PrefsRepo
) : ViewModel() {

    companion object {
        const val NORMAL_TIMER_INCREMENT_VALUE = 5
        const val BREAK_TIMER_INCREMENT_VALUE = 1
    }

    private var _messageForUser = MutableLiveData<Event<String>>()
    val messageForUser: LiveData<Event<String>>
        get() = _messageForUser

    private var _openLicenseActivityEvent = MutableLiveData<Event<Unit>>()
    val openLicenseActivity: LiveData<Event<Unit>>
        get() = _openLicenseActivityEvent

    private var _prefs = MutableLiveData<List<PreferenceModel>>()
    val prefs : LiveData<List<PreferenceModel>>
        get() = _prefs

    init {
        _prefs.postValue(makePreferences())
    }

    private fun makePreferences(): List<PreferenceModel> {
        return mutableListOf(
            IncrementablePreferenceModel(
                title = "Pomodoro: ",
                key = KEY_NORMAL_TIMER_DURATION_MINUTES,
                incrementBy = NORMAL_TIMER_INCREMENT_VALUE,
                allowNegatives = false

            ), IncrementablePreferenceModel(
                title = "Break: ",
                key = KEY_BREAK_TIMER_DURATION_MINUTES,
                incrementBy = BREAK_TIMER_INCREMENT_VALUE,
                allowNegatives = false

            ), PreferenceModel(PreferenceType.Clickable,
                title = "Licenses",
                key = "",
                click = object : PreferenceClick {
                    override fun onClick() {
                        _openLicenseActivityEvent.postEvent(Unit)

                    }
                })
        )
    }

    private fun notifyUser(s: String) {
        _messageForUser.postEvent(s)
    }
}
