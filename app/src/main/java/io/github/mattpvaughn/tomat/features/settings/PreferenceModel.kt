package io.github.mattpvaughn.tomat.features.settings

open class PreferenceModel(
    val type: PreferenceType,
    val title: String,
    val key: String,
    val click: PreferenceClick = object : PreferenceClick {
        override fun onClick() {
            // Do nothing by default
        }
    }
) {
    override fun equals(other: Any?): Boolean {
        if (other !is PreferenceModel) {
            return false
        }
        return other.type == type && other.title == title && other.key == key
    }
}

class IncrementablePreferenceModel(
    title: String, key: String, val incrementBy: Int = 1, val allowNegatives: Boolean = false
) : PreferenceModel(PreferenceType.IncrementableInt(incrementBy), title, key)

interface PreferenceClick {
    fun onClick()
}

sealed class PreferenceType(val typeInt: Int) {
    object Clickable : PreferenceType(typeInt = 1)
    data class IncrementableInt(val incrementBy: Int) : PreferenceType(typeInt = 2)

    companion object {
        const val VIEW_TYPE_CLICKABLE = 1
        const val VIEW_TYPE_INCREMENTABLE = 2
    }
}

