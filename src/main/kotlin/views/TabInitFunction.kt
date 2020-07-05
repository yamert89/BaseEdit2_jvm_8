package views

import javafx.scene.control.Tab

abstract class TabInitFunction: InitFunction<Tab> {
    abstract override fun getInitial(): Tab.() -> Unit
}