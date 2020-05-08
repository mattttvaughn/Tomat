package io.github.mattpvaughn.tomat.features.pomodoro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.github.mattpvaughn.tomat.data.local.PrefsRepo
import javax.inject.Inject

class PomodoroViewModelFactory @Inject constructor(private val prefsRepo: PrefsRepo) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(PomodoroViewModel::class.java!!)) {
            PomodoroViewModel(prefsRepo) as T
        } else {
            throw IllegalArgumentException("ViewModel Not Found")
        }
    }
}
