import converters.AreaConverter
import converters.NumberConverter
import javafx.animation.KeyFrame
import javafx.animation.KeyValue
import javafx.animation.Timeline
import javafx.beans.property.Property
import javafx.geometry.Insets
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import javafx.util.Duration
import tornadofx.*
import views.ButtonsPaneInitFunction
import views.EditorInitFunction
import views.MenuBarInitFunction
import views.PackageUpdateInitFunction
import java.io.File
import java.lang.NumberFormatException
import java.nio.file.Paths


fun main() {
    launch<BaseEdit2>()
}

//todo coloring rows
class BaseEdit2: App(ParentView::class)

class ParentView : View(){
    private val controller = find(GenController::class)
    private var status = Label()
    private var forceSaving = false

    init {
        title = "BaseEdit2"
        primaryStage.setOnCloseRequest { event ->
            AppPreferences.savePreferences()
            if (controller.tableData.isEmpty()) return@setOnCloseRequest
            val header = if (controller.preSaveCheck()) "Сохранить?" else "Имеются неисправленные ошибки. Вы уверены, что хотите сохранить?"
            val save = ButtonType("Сохранить")
            val no = ButtonType("Выйти без сохранения")
            val cancel = ButtonType("Отмена")
            alert(Alert.AlertType.CONFIRMATION, header, null, save, no, cancel, owner = primaryStage, title = "Подтверждение"){
                when(it){
                    no -> return@setOnCloseRequest
                    cancel -> {
                        event.consume()
                        return@setOnCloseRequest
                    }
                    save -> controller.save(null)
                }
            }


        }

        primaryStage.setOnShown {
            primaryStage.icons.add(resources.image("Desktop.png"))
        }

    }


    override val root = vbox {

        controller.menuBar = menubar { MenuBarInitFunction(this, primaryStage, this@ParentView).getInitial().invoke(this) }

        hbox {
            ButtonsPaneInitFunction(this, this@ParentView).getInitial().invoke(this)
        }

        tabpane {
            vgrow = Priority.ALWAYS
            //enableWhen { controller.fileOpened }
            tab("Редактор"){ EditorInitFunction(this).getInitial().invoke(this) }
            tab("Пакетное обновление"){ PackageUpdateInitFunction(this).getInitial().invoke(this) }
        }

        status = label{
            style{
                textFill = Color.RED
                fontSize = Dimension(11.0, Dimension.LinearUnits.pt)
            }

            vboxConstraints {
                margin = Insets(5.0)
                minWidth = 500.0
            }

            val timeLine = Timeline().apply {
                keyFrames.add(KeyFrame(Duration(0.0), KeyValue(this@label.opacityProperty(), 0.0)))
                keyFrames.add(KeyFrame(Duration(500.0), KeyValue(this@label.opacityProperty(), 1.0)))
                keyFrames.add(KeyFrame(Duration(2000.0), KeyValue(this@label.opacityProperty(), 1.0)))
                keyFrames.add(KeyFrame(Duration(3000.0), KeyValue(this@label.opacityProperty(), 0.0), KeyValue(this@label.textProperty(), "")))
            }

            textProperty().onChange {
                //fade.playFromStart()
                timeLine.playFromStart()
            }
        }

        find(Notification::class, Pair(PAR_LABEL, status)) // create notification with label
    }

    fun openStrictAreaView() = find<BE2StrictView>().openWindow(owner = this.currentWindow)
}

