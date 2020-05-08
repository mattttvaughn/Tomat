package io.github.mattpvaughn.tomat.injection.components

import android.content.Context
import android.content.SharedPreferences
import dagger.Component
import io.github.mattpvaughn.tomat.application.CustomApplication
import io.github.mattpvaughn.tomat.data.local.*
import io.github.mattpvaughn.tomat.features.pomodoro.PomodoroService
import io.github.mattpvaughn.tomat.features.pomodoro.PomodoroViewModelFactory
import io.github.mattpvaughn.tomat.features.settings.SettingsFragment
import io.github.mattpvaughn.tomat.injection.modules.AppModule
import java.io.File
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class])
interface AppComponent {
    fun applicationContext(): Context
    fun internalFilesDir(): File
    fun externalDeviceDirs(): List<File>
    fun sharedPrefs(): SharedPreferences
    fun prefsRepo(): PrefsRepo
    fun editorViewModelFactory(): PomodoroViewModelFactory

    // Inject
    fun inject(customApplication: CustomApplication)
    fun inject(pomodoroService: PomodoroService)
}