package views

import SKLArea
import GenController
import Notification
import ParentView
import javafx.geometry.Insets
import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.layout.HBox
import tornadofx.*

class ButtonsPaneInitFunction(private val hBox: HBox, private val rootView: ParentView) : InitFunction<HBox> {
    private val controller = find(GenController::class)
    private val notification = find(Notification::class)
    override fun getInitial(): HBox.() -> Unit {
        return hBox.initial()
    }
    private fun HBox.initial(): HBox.() -> Unit {
        return {
            padding = Insets(3.0)

            val buttonFontSize = Dimension(7.0, Dimension.LinearUnits.pt)

            controller.saveButton = button{
                disableProperty().set(true)
                hboxConstraints { marginLeftRight(10.0) }
                style {
                    fontSize = buttonFontSize
                    prefWidth = Dimension(25.0, Dimension.LinearUnits.px)
                    padding = box(Dimension(1.0, Dimension.LinearUnits.px))

                }
                graphic = rootView.resources.imageview("/Export To Document.png").apply {
                    fitHeight = 20.0
                    fitWidth = 20.0
                }
                action {
                    if (controller.tableData.isEmpty() || !controller.preSaveCheck()) return@action
                    runAsyncWithProgress {
                        controller.save(null)
                    }ui{notification.notif("Сохранено")}
                }
                tooltip("Сохранить"){style{fontSize = buttonFontSize + 2}}
            }

            controller.addButton = button(/*"Добавить"*/) {
                hboxConstraints {
                    marginLeftRight(6.0)
                }
                tooltip("Добавить выдел ( Num + )"){style{fontSize = buttonFontSize + 2}}

                style {
                    fontSize = buttonFontSize
                    prefWidth = Dimension(25.0, Dimension.LinearUnits.px)
                    padding = box(Dimension(1.0, Dimension.LinearUnits.px))

                }

                disableProperty().set(true)

                val imageView = rootView.resources.imageview("/Add Green Button.png")
                imageView.fitHeight = 20.0
                imageView.fitWidth = 20.0
                graphic = imageView

                action {
                    if (controller.selected == null) {
                        println("selected is null")
                        return@action
                    }
                    var item = controller.selected!!
                    item = SKLArea(0, item.numberKv, 0f, item.categoryArea, DataTypes.EMPTY_CATEGORY_PROTECTION, item.ozu, item.lesb, item.rawData)

                    controller.addArea(item)

                    controller.tableView.selectionModel!!.select(controller.selectedRow, controller.tableView.columns[1])

                }

                with(rootView){shortcut(KeyCodeCombination(KeyCode.ADD))}

            }
            controller.delButton = button(/*"Удалить"*/) {
                disableProperty().set(true)
                style {
                    fontSize = buttonFontSize
                    prefWidth = Dimension(25.0, Dimension.LinearUnits.px)
                    padding = box(Dimension(1.0, Dimension.LinearUnits.px))

                }
                graphic = rootView.resources.imageview("/Minus Green Button.png").apply {
                    fitHeight = 20.0
                    fitWidth = 20.0
                }
                action {
                    alert(
                        Alert.AlertType.CONFIRMATION,
                        "Удалить?",
                        owner = rootView.primaryStage,
                        actionFn = { buttonType ->
                            if (buttonType == ButtonType.OK) {
                                controller.delArea()
                            }
                        })

                }
                tooltip("Удалить выдел ( Num - )"){style{fontSize = buttonFontSize + 2}}
                with(rootView){shortcut(KeyCodeCombination(KeyCode.SUBTRACT))}
            }
        }
    }
}


