package io.github.mattpvaughn.tomat.application

import io.github.mattpvaughn.tomat.injection.components.AppComponent

class Injector private constructor() {
    companion object {
        fun get() : AppComponent = CustomApplication.get().appComponent
    }
}