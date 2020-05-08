package io.github.mattpvaughn.tomat.features.settings

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.CompoundButton
import android.widget.FrameLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import io.github.mattpvaughn.tomat.application.Injector
import io.github.mattpvaughn.tomat.data.local.PrefsRepo
import io.github.mattpvaughn.tomat.databinding.PreferenceClickableBinding
import io.github.mattpvaughn.tomat.databinding.PreferenceIncrementableIntBinding
import io.github.mattpvaughn.tomat.features.settings.PreferenceType.Companion.VIEW_TYPE_CLICKABLE
import io.github.mattpvaughn.tomat.features.settings.PreferenceType.Companion.VIEW_TYPE_INCREMENTABLE

/**
 * A view which shows
 */
class SettingsList: FrameLayout {

    // These constructors allow us to place our canvas in XML
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context, attrs, defStyle
    )

    private val prefsRepo = Injector.get().prefsRepo()
    private val prefAdapter = PreferencesListAdapter(prefsRepo)

    private var list: RecyclerView = RecyclerView(context).apply {
        adapter = prefAdapter
        layoutManager = LinearLayoutManager(context)
        layoutParams = LayoutParams(MATCH_PARENT, MATCH_PARENT)
    }

    fun setPreferences(prefs: List<PreferenceModel>) {
        prefAdapter.submitList(prefs)
    }

    init {
        addView(list)
    }

    class PreferencesListAdapter(private val prefsRepo: PrefsRepo) : ListAdapter<PreferenceModel, RecyclerView.ViewHolder>(
        PreferenceItemDiffCallback()
    ) {
        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): RecyclerView.ViewHolder {
            return when (viewType) {
                VIEW_TYPE_CLICKABLE -> ClickablePreferenceViewHolder.from(parent)
                VIEW_TYPE_INCREMENTABLE -> IncrementablePreferenceViewHolder.from(parent, prefsRepo)
                else -> throw NoWhenBranchMatchedException("Unknown view type passed to SettingsList")
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if (holder is ClickablePreferenceViewHolder) {
                holder.bind(getItem(position))
            }
            if (holder is IncrementablePreferenceViewHolder) {
                holder.bind(getItem(position) as IncrementablePreferenceModel)
            }
        }

        override fun getItemViewType(position: Int): Int {
            return getItem(position).type.typeInt
        }

        class ClickablePreferenceViewHolder(val binding: PreferenceClickableBinding) :
            RecyclerView.ViewHolder(binding.root) {
            fun bind(preferenceModel: PreferenceModel) {
                binding.bind(preferenceModel)
            }

            companion object {
                fun from(viewGroup: ViewGroup): ClickablePreferenceViewHolder {
                    val inflater = LayoutInflater.from(viewGroup.context)
                    val binding = PreferenceClickableBinding.inflate(inflater, viewGroup, false)
                    return ClickablePreferenceViewHolder(binding)
                }
            }
        }

        class IncrementablePreferenceViewHolder(
            val binding: PreferenceIncrementableIntBinding,
            private val prefsRepo: PrefsRepo
        ) : RecyclerView.ViewHolder(binding.root) {
            fun bind(preferenceModel: IncrementablePreferenceModel) {
                binding.bind(prefsRepo, preferenceModel)
            }

            companion object {
                fun from(
                    viewGroup: ViewGroup,
                    prefsRepo: PrefsRepo
                ): IncrementablePreferenceViewHolder {
                    val inflater = LayoutInflater.from(viewGroup.context)
                    val binding = PreferenceIncrementableIntBinding.inflate(inflater, viewGroup, false)
                    return IncrementablePreferenceViewHolder(binding, prefsRepo)
                }
            }
        }

    }

    class PreferenceItemDiffCallback : DiffUtil.ItemCallback<PreferenceModel>() {
        override fun areItemsTheSame(oldItem: PreferenceModel, newItem: PreferenceModel): Boolean {
            return oldItem.title == newItem.title
        }

        override fun areContentsTheSame(oldItem: PreferenceModel, newItem: PreferenceModel): Boolean {
            return oldItem == newItem
        }
    }
}
