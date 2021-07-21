package views

import AppPreferences
import GenController
import ParentView
import javafx.geometry.Insets
import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import tornadofx.*

class Preferences : Fragment("Настройки"){
    private val controller = find(GenController::class)
    private val mainView: ParentView by inject()

    override val root = pane {
        val m10 = Insets(10.0)
        padding = m10
        vbox {
            padding = Insets(20.0)
            checkbox("Проверять на пропуски выделов при сохранении",
                AppPreferences.checkSkippedProperty
            ){
                vboxConstraints { margin = m10 }
            }
            checkbox("Делать резервную копию при сохранении", AppPreferences.saveBackupsProperty){
                vboxConstraints { margin = m10 }
            }
            checkbox("Проверять изменение площадей", AppPreferences.checkAreasProperty) {
                vboxConstraints { margin = m10 }
                if (controller.tableData.isEmpty()) return@checkbox
                AppPreferences.checkAreasProperty.onChange {
                    if (it) {
                        mainView.openStrictAreaView()
                    }
                }
            }
            checkbox("Возможность сортировки столбцов", AppPreferences.sortingProperty){
                vboxConstraints { margin = m10 }
                action {
                    if (this.isSelected) controller.tableView.columns.forEach { it.isSortable = true }
                    else controller.tableView.columns.forEach { it.isSortable = false }
                }
            }
        }

    }
}