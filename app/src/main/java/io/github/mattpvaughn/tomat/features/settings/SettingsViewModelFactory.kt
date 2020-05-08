package io.github.mattpvaughn.tomat.features.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.github.mattpvaughn.tomat.data.local.PrefsRepo
import javax.inject.Inject


class SettingsViewModelFactory @Inject constructor(private val prefsRepo: PrefsRepo) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            return SettingsViewModel(prefsRepo) as T
        }
        throw IllegalArgumentException("Cannot cast type $modelClass to SettingsViewModel")
    }
}
