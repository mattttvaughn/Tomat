package io.github.mattpvaughn.tomat.features.pomodoro

import androidx.lifecycle.*
import io.github.mattpvaughn.tomat.application.SECONDS_PER_MINUTE
import io.github.mattpvaughn.tomat.data.local.PrefsRepo
import io.github.mattpvaughn.tomat.data.local.PrefsRepo.Companion.KEY_NORMAL_TIMER_DURATION_MINUTES
import io.github.mattpvaughn.tomat.features.pomodoro.PomodoroService.State.Companion.RESTING_STRING
import io.github.mattpvaughn.tomat.util.Event
import io.github.mattpvaughn.tomat.util.postEvent


class PomodoroViewModel(prefsRepo: PrefsRepo) : ViewModel() {

    private var _state = MutableLiveData(RESTING_STRING)
    val state: LiveData<String>
        get() = _state

    private var _openSettingsEvent = MutableLiveData<Event<Unit>>()
    val openSettingsEvent: LiveData<Event<Unit>>
        get() = _openSettingsEvent

    private var _secondsRemaining = MutableLiveData(prefsRepo.getInt(KEY_NORMAL_TIMER_DURATION_MINUTES) * SECONDS_PER_MINUTE)
    val secondsRemaining: LiveData<Int>
        get() = _secondsRemaining

    private var _sendCountdownClickEvent = MutableLiveData<Event<Unit>>()
    val sendCountDownClickEvent: LiveData<Event<Unit>>
        get() = _sendCountdownClickEvent

    fun settingsIconClicked() {
        _openSettingsEvent.postEvent(Unit)
    }

    fun timerClicked() {
        _sendCountdownClickEvent.postEvent(Unit)
    }

    fun updateState(stateString: String, secondsRemaining: Int) {
        _secondsRemaining.postValue(secondsRemaining)
        _state.postValue(stateString)
    }

}
