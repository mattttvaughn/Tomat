package io.github.mattpvaughn.tomat.injection.modules

import dagger.Module
import dagger.Provides
import io.github.mattpvaughn.tomat.application.MainActivity
import io.github.mattpvaughn.tomat.injection.scopes.ActivityScope
import io.github.mattpvaughn.tomat.navigation.Navigator

@Module
class ActivityModule(private val activity: MainActivity) {

    @Provides
    @ActivityScope
    fun navigator(): Navigator = Navigator(activity.supportFragmentManager)

}


