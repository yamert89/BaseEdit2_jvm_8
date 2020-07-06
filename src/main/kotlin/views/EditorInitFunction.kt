package views

import Area
import GenController
import converters.AreaConverter
import converters.NumberConverter
import javafx.beans.property.Property
import javafx.scene.control.Tab
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import tornadofx.*
import java.lang.NumberFormatException
import kotlin.error

class EditorInitFunction(private val tab: Tab): TabInitFunction() {

    private val controller = find(GenController::class)
    private var tableViewEditModel: TableViewEditModel<Area> by singleAssign()

    override fun getInitial() = tab.initial()

   private fun Tab.initial(): Tab.() -> Unit {
        return {
            //this.vgrow = Priority.ALWAYS
            println("initial func")
            isClosable = false
            controller.tableView = tableview(controller.getData()) {
                fixedCellSize = 22.0

                fun <T> columnOnEdit(
                    editEvent: TableColumn.CellEditEvent<Area, T>,
                    idx: Int,
                    condition: () -> Boolean
                ) {
                    if (condition()) {
                        tornadofx.error("Невалидное значение")
                        editModel.rollbackSelected()
                        return
                    }
                    val property = editEvent.tableColumn.getCellObservableValue(editEvent.rowValue) as Property<T?>
                    if (idx == 6) {
                        var res = editEvent.newValue as String
                        if ((editEvent.newValue as String).length < 4) while (res.length < 4) res = "0$res"
                        property.value = res as T
                    } else property.value = editEvent.newValue
                    selectionModel.focus(controller.selectedRow)
                    selectionModel.select(controller.selectedRow, controller.tableView.columns[idx])

                }

                style(true) {
                    borderColor += box(Color.GRAY)
                }
                isEditable = true
                readonlyColumn("Кв", Area::numberKv)
                column("Выд", Area::number).apply {
                    makeEditable(NumberConverter())

                    /* setOnEditStart {
                     println(it.newValue ?: "null")
                     setOnKeyPressed {event ->
                         if (event.code == KeyCode.DOWN){
                             println("down")
                             val property = it.tableColumn.getCellObservableValue(it.rowValue) as Property<Int?>
                             tableViewEditModel.commit(controller.tableData[selectedRow])
                             property.value = it.newValue
                             tableView.selectionModel.focus(selectedRow + 1)
                             tableView.selectionModel.select(selectedRow + 1, tableView.selectedColumn)
                         }
                     }
                 }*/

                    setOnEditCommit {
                        println(it.newValue ?: "null")
                    }

                    setOnEditCommit {
                        columnOnEdit(it, 1) { it.newValue > 999 || it.newValue < 0 }
                        //selectionModel.select(selectedRow, tableView!!.columns[2])
                        //tableView.edit(selectedRow, tableView.columns[2]) //todo
                    }
                    //setOnEditCancel { columnOnEdit(it, 1){it.newValue > 999 || it.newValue < 0} }
                }


                column("Площадь", Area::area).apply {
                    makeEditable(AreaConverter())
                    setOnEditCommit { columnOnEdit(it, 2) { it.newValue > 9999 || it.newValue < 0 } }
                    //setOnEditCancel { columnOnEdit(it, 2){it.newValue > 9999 || it.newValue < 0} }

                }
                column("К. защитности", Area::categoryProtection).makeEditable()
                    .useComboBox(DataTypes.categoryProtection.values.toList().asObservable())
                readonlyColumn("К. земель", Area::categoryArea).cellFormat {
                    text = it
                    style {
                        if (it == "1108" || it == "1201") backgroundColor += c("#036907", 0.3)
                    }
                }
                column("ОЗУ", Area::ozu).makeEditable().useComboBox(DataTypes.ozu.values.toList().asObservable())
                column("lesb", Area::lesb).apply {
                    makeEditable()
                    setOnEditCommit {
                        var notValid = false
                        try {
                            val intv = it.newValue.toInt()
                            notValid = intv > 9999 || intv < 0
                        } catch (e: NumberFormatException) {
                            notValid = true
                        }
                        columnOnEdit(it, 6) { notValid || it.newValue.length > 4 }
                    }
                    // setOnEditCancel { columnOnEdit(it, 6){it.newValue.length > 4} }
                }

                selectionModel.selectedItemProperty().onChange {
                    with(controller) {
                        selected = tableView.selectedItem
                        selectedRow = tableView.selectedCell?.row ?: selectedRow
                        selectedCol = tableView.selectedColumn
                        if (selectedCol == tableView.columns[3]) tableView.edit(selectedRow, selectedCol)
                    }
                }

                enableCellEditing() //enables easier cell navigation/editing
                //enableDirtyTracking() //flags cells that are dirty

                tableViewEditModel = editModel
            }
        }
    }
}
