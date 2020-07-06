import javafx.scene.control.Label
import tornadofx.Component
import tornadofx.ScopedInstance

class Notification(): Component(), ScopedInstance {

    fun notif(message: String){
        (params[PAR_LABEL] as Label).text = message
    }
}