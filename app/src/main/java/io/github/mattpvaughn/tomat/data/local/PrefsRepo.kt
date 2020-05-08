package io.github.mattpvaughn.tomat.data.local

import android.content.SharedPreferences
import io.github.mattpvaughn.tomat.data.local.PrefsRepo.Companion.DEFAULT_BREAK_TIMER_DURATION_MINUTES
import io.github.mattpvaughn.tomat.data.local.PrefsRepo.Companion.DEFAULT_NORMAL_TIMER_DURATION_MINUTES
import io.github.mattpvaughn.tomat.data.local.PrefsRepo.Companion.KEY_BREAK_TIMER_DURATION_MINUTES
import io.github.mattpvaughn.tomat.data.local.PrefsRepo.Companion.KEY_NORMAL_TIMER_DURATION_MINUTES
import java.lang.IllegalArgumentException
import javax.inject.Inject

/**
 * An interface for getting/setting persistent preferences for Chronicle
 */
interface PrefsRepo {
    fun getInt(key: String): Int
    fun incrementInt(key: String, incrementBy: Int = 1, allowNegatives: Boolean = true)
    fun decrementInt(key: String, decrementBy: Int = 1, allowNegatives: Boolean = true)
    fun registerListener(listener: SharedPreferences.OnSharedPreferenceChangeListener)
    fun unRegisterListener(listener: SharedPreferences.OnSharedPreferenceChangeListener)

    companion object {
        const val KEY_NORMAL_TIMER_DURATION_MINUTES = "normal timer duration"
        const val KEY_BREAK_TIMER_DURATION_MINUTES = "break timer duration"

        const val DEFAULT_NORMAL_TIMER_DURATION_MINUTES = 25
        const val DEFAULT_BREAK_TIMER_DURATION_MINUTES = 5
    }
}

/** An implementation of [PrefsRepo] wrapping [SharedPreferences] */
class SharedPreferencesPrefsRepo @Inject constructor(private val sharedPreferences: SharedPreferences) :
    PrefsRepo {

    private fun getDefaultIntValue(key: String): Int {
        return when (key) {
            KEY_NORMAL_TIMER_DURATION_MINUTES -> DEFAULT_NORMAL_TIMER_DURATION_MINUTES
            KEY_BREAK_TIMER_DURATION_MINUTES -> DEFAULT_BREAK_TIMER_DURATION_MINUTES
            else -> throw NoWhenBranchMatchedException("Unknown key!")
        }
    }

    override fun getInt(key: String): Int {
        return sharedPreferences.getInt(key, getDefaultIntValue(key))
    }

    override fun incrementInt(key: String, incrementBy: Int, allowNegatives: Boolean) {
        val currentValue = sharedPreferences.getInt(key, getDefaultIntValue(key))
        val newValue = currentValue + incrementBy
        if (!allowNegatives && newValue < 0) {
            throw IllegalArgumentException(
                "Tried to increment to negative value! Value = $newValue"
            )
        }
        sharedPreferences.edit().putInt(key, newValue).apply()
    }

    override fun decrementInt(key: String, decrementBy: Int, allowNegatives: Boolean) {
        val currentValue = sharedPreferences.getInt(key, getDefaultIntValue(key))
        val newValue = currentValue - decrementBy
        if (!allowNegatives && newValue < 1) {
            throw IllegalArgumentException(
                "Tried to increment to negative value! Value = $newValue"
            )
        }
        sharedPreferences.edit().putInt(key, newValue).apply()
    }

    override fun registerListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
    }

    override fun unRegisterListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener)
    }
}
