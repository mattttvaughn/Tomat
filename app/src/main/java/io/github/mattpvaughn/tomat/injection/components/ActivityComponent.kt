package io.github.mattpvaughn.tomat.injection.components

import dagger.Component
import io.github.mattpvaughn.tomat.application.MainActivity
import io.github.mattpvaughn.tomat.application.MainActivityViewModelFactory
import io.github.mattpvaughn.tomat.features.pomodoro.PomodoroFragment
import io.github.mattpvaughn.tomat.features.settings.SettingsFragment
import io.github.mattpvaughn.tomat.injection.modules.ActivityModule
import io.github.mattpvaughn.tomat.injection.scopes.ActivityScope
import io.github.mattpvaughn.tomat.navigation.Navigator

@ActivityScope
@Component(dependencies = [AppComponent::class], modules = [ActivityModule::class])
interface ActivityComponent {
    fun mainActivityViewModelFactory(): MainActivityViewModelFactory
    fun navigator(): Navigator

    fun inject(activity: MainActivity)
    fun inject(pomodoroFragment: PomodoroFragment)
    fun inject(settingsFragment: SettingsFragment)
}

