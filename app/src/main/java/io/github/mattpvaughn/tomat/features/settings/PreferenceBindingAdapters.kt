package io.github.mattpvaughn.tomat.features.settings

import io.github.mattpvaughn.tomat.data.local.PrefsRepo
import io.github.mattpvaughn.tomat.databinding.PreferenceClickableBinding
import io.github.mattpvaughn.tomat.databinding.PreferenceIncrementableIntBinding
import kotlinx.android.synthetic.main.preference_incrementable_int.view.*

fun PreferenceIncrementableIntBinding.bind(
    prefsRepo: PrefsRepo,
    pref: IncrementablePreferenceModel
) {
    prefText.text = pref.title + prefsRepo.getInt(pref.key)
    incrementButton.setOnClickListener {
        val currentValue = prefsRepo.getInt(pref.key)
        prefsRepo.incrementInt(
            key = pref.key, incrementBy = pref.incrementBy, allowNegatives = pref.allowNegatives
        )
        prefText.text = pref.title + (currentValue + pref.incrementBy)
    }
    decrementButton.setOnClickListener {
        val currentValue = prefsRepo.getInt(pref.key)
        if (!pref.allowNegatives && currentValue - pref.incrementBy < 1) {
            return@setOnClickListener
        }
        prefsRepo.decrementInt(
            key = pref.key, decrementBy = pref.incrementBy, allowNegatives = pref.allowNegatives
        )
        prefText.text = pref.title + (currentValue - pref.incrementBy)
    }
}

fun PreferenceClickableBinding.bind(pref: PreferenceModel) {
    prefText.text = pref.title
    root.setOnClickListener {
        pref.click.onClick()
    }
}
