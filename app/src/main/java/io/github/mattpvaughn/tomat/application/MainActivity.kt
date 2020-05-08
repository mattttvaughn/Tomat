package io.github.mattpvaughn.tomat.application

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import io.github.mattpvaughn.tomat.databinding.ActivityMainBinding
import io.github.mattpvaughn.tomat.injection.components.ActivityComponent
import io.github.mattpvaughn.tomat.injection.components.DaggerActivityComponent
import io.github.mattpvaughn.tomat.injection.modules.ActivityModule
import io.github.mattpvaughn.tomat.injection.scopes.ActivityScope
import io.github.mattpvaughn.tomat.navigation.Navigator
import io.github.mattpvaughn.tomat.util.observeEvent
import javax.inject.Inject


@ActivityScope
open class MainActivity : AppCompatActivity() {

    private val viewModel: MainActivityViewModel by lazy {
        ViewModelProvider(this, mainActivityViewModelFactory).get(MainActivityViewModel::class.java)
    }

    @Inject
    lateinit var mainActivityViewModelFactory: MainActivityViewModelFactory

    @Inject
    lateinit var navigator: Navigator

    lateinit var activityComponent: ActivityComponent

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.i(APP_NAME, "MainActivity onCreate()")
        activityComponent = DaggerActivityComponent.builder()
            .appComponent((application as CustomApplication).appComponent)
            .activityModule(ActivityModule(this))
            .build()
        activityComponent.inject(this)

        super.onCreate(savedInstanceState)

        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Check if the app is going to be restored, if not, show the library page
        if (savedInstanceState == null) {
            navigator.openPomodoro()
        }

        viewModel.errorMessage.observeEvent(this) { errorMessage ->
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
        }
    }


    override fun onPause() {
        Log.i(APP_NAME, "MainActivity onPause()")
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        Log.i(APP_NAME, "MainActivity onResume()")
    }

    override fun onStart() {
        super.onStart()
        Log.i(APP_NAME, "MainActivity onStart()")
    }

    override fun onStop() {
        Log.i(APP_NAME, "MainActivity onStop()")
        super.onStop()
    }
}
