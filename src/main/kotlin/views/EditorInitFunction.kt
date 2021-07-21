package views

import SKLArea
import GenController
import com.sun.org.apache.bcel.internal.classfile.Code
import converters.AreaConverter
import converters.CategoryProtectionConverter
import converters.CodeMappingConverter
import converters.NumberConverter
import javafx.beans.property.Property
import javafx.collections.ObservableList
import javafx.scene.control.Tab
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.control.cell.ComboBoxTableCell
import javafx.scene.paint.Color
import javafx.util.StringConverter
import roslesinforg.porokhin.areatypes.GeneralTypes
import tornadofx.*
import java.lang.NumberFormatException

class EditorInitFunction(private val tab: Tab): TabInitFunction() {

    private val controller = find(GenController::class)
    private var tableViewEditModel: TableViewEditModel<SKLArea> by singleAssign()

    override fun getInitial() = tab.initial()

   private fun Tab.initial(): Tab.() -> Unit {
        return {
            //this.vgrow = Priority.ALWAYS
            println("initial func")
            isClosable = false
            controller.tableView = tableview(controller.getData()) {
                fixedCellSize = 22.0
                style(true) {
                    borderColor += box(Color.GRAY)
                }
                isEditable = true
                readonlyColumn("Кв", SKLArea::numberKv).apply { isSortable = AppPreferences.sorting }
                column("Выд", SKLArea::number).apply {
                    makeEditable(NumberConverter())
                    isSortable = AppPreferences.sorting

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


                column("Площадь", SKLArea::area).apply {
                    makeEditable(AreaConverter())
                    setOnEditCommit {
                        controller.updateStrictView()
                        columnOnEdit(it, 2) { it.newValue > 9999 || it.newValue < 0 }
                    }
                    isSortable = AppPreferences.sorting
                    //setOnEditCancel { columnOnEdit(it, 2){it.newValue > 9999 || it.newValue < 0} }

                }
                column("К. защитности", SKLArea::categoryProtection).useComboBox(
                    CodeMappingConverter(GeneralTypes::categoryProtectionLong),
                    GeneralTypes.categoryProtectionLong.keys.toList().plus(0).asObservable() ).apply { isSortable = AppPreferences.sorting }

                val kz = readonlyColumn("К. земель", SKLArea::categoryArea).apply {
                    isSortable = AppPreferences.sorting
                }
                    kz.cellFormat {
                        text = it
                        tooltip(GeneralTypes.categoryAreaLong[it.toInt()])
                        style {
                            if (it == "1108" || it == "1201") backgroundColor += c("#036907", 0.3)
                        }
                    }

                column("ОЗУ", SKLArea::ozu).useComboBox(CodeMappingConverter(GeneralTypes::typesOfProtectionLong), GeneralTypes.typesOfProtectionLong.keys.toList().asObservable()).apply { isSortable = AppPreferences.sorting }
                column("lesb", SKLArea::lesb).apply {
                    makeEditable()
                    isSortable = AppPreferences.sorting
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

    fun <S, T> TableColumn<S, T>.useComboBox(converter: StringConverter<T>? = null, items: ObservableList<T>, afterCommit: (TableColumn.CellEditEvent<S, T?>) -> Unit = {}) = apply {
        cellFactory = if (converter == null) ComboBoxTableCell.forTableColumn(items) else ComboBoxTableCell.forTableColumn(converter, items)
        setOnEditCommit {
            val property = it.tableColumn.getCellObservableValue(it.rowValue) as Property<T?>
            property.value = it.newValue
            afterCommit(it)
        }
    }

    private fun <T> TableView<SKLArea>.columnOnEdit(editEvent: TableColumn.CellEditEvent<SKLArea, T>, idx: Int, condition: () -> Boolean) {
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
}
