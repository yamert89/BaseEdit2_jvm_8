import javafx.animation.FadeTransition
import javafx.beans.property.IntegerProperty
import javafx.beans.property.Property
import javafx.geometry.Insets
import javafx.scene.control.*
import javafx.scene.image.ImageView
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import javafx.util.converter.IntegerStringConverter
import tornadofx.*


fun main() {
    launch<BaseEdit2>()

}


class BaseEdit2: App(ParentView::class)

class ParentView : View(){
    private val controller: GenController by inject()

    private var selected: Area? = null
    private var selectedRow: Int = 0
    private var selectedCol: TableColumn<Area, *>? = null
    private var tableView: TableView<Area>? = null
    private val model: AreaModel by inject()
    private val dataTypes = DataTypes()
    private var colum: TableColumn<Area, String?>? = null
    private var tableViewEditModel: TableViewEditModel<Area> by singleAssign()
    private var status = Label()
    init {
        primaryStage.setOnCloseRequest {
            if (controller.tableData.isEmpty()) return@setOnCloseRequest
            confirm("Подтверждение", "Сохранить?", cancelButton = ButtonType.NO){
                if (!controller.preSaveCheck()) return@setOnCloseRequest
                controller.save(null)
            }
        }
    }


    override val root = vbox {

        hbox {

            button ("Открыть"){
                action {
                    val files = chooseFile("Выберите файл", owner = primaryStage, mode = FileChooserMode.Single, filters = arrayOf())
                    if (files.isEmpty()) return@action
                    controller.tableData.clear()
                    //todo filters


                    runAsyncWithProgress/*(progress = progressBar)*/ {
                        controller.initData(files[0])
                        println("end init")
                    }ui{
                       println("visible false")
                    }

                }
                hboxConstraints { margin = Insets(10.0) }

            }


             button{
                 val imageView = resources.imageview("AddGreenButton.ico").apply {
                     fitHeight = 32.0
                     fitWidth = 32.0
                 }
                 graphic = imageView

                 addClass("icon-only")


                 hboxConstraints {
                     margin = Insets(10.0)
                     prefWidth = 32.0

                 }
                action {
                    if(selected == null) {
                        println("selected is null")
                        return@action
                    }
                    var item = selected!!
                    item = Area(0, item.numberKv, 0.0, item.categoryArea, "-", item.ozu, item.lesb, item.rawData)

                    controller.tableData.add(selectedRow, item)

                    tableView!!.selectionModel!!.select(selectedRow,  tableView!!.columns[1])

                }

                shortcut(KeyCodeCombination(KeyCode.ADD))

            }
            button("Удалить"){
                hboxConstraints { margin = Insets(10.0) }
                action {
                    alert(Alert.AlertType.CONFIRMATION, "Удалить?", owner = primaryStage, actionFn = {buttonType ->
                        if (buttonType == ButtonType.OK) {
                            controller.tableData.removeAt(selectedRow)
                            tableView!!.selectionModel.select(selectedRow + 1, selectedCol)

                        }
                    } )

                }
                shortcut(KeyCodeCombination(KeyCode.SUBTRACT))
            }
            button("Сохранить") {
                hboxConstraints { margin = Insets(10.0) }
                action {
                    if(!controller.preSaveCheck()) return@action
                    runAsyncWithProgress {
                        controller.save(null)
                    }
                }

            }

            button("Сохранить как") {
                hboxConstraints { margin = Insets(10.0) }
                action {
                    if(!controller.preSaveCheck()) return@action
                    val list = chooseFile("Выберите файл", mode = FileChooserMode.Save, filters = arrayOf(), owner = primaryStage)
                    val path = list[0].absolutePath
                    runAsyncWithProgress {
                        controller.save(path)
                    }
                }
            }

        }
        tabpane {
            vgrow = Priority.ALWAYS

            tab("Редактор"){
                vgrow = Priority.ALWAYS
                isClosable = false
                tableView = tableview(controller.getData()) {
                    style(true) {
                        borderColor += box(Color.GRAY)
                    }
                    isEditable = true
                    readonlyColumn("Кв", Area::numberKv)
                     column("Выд", Area::number).apply {
                         makeEditable()
                         setOnEditCommit {
                             val property = it.tableColumn.getCellObservableValue(it.rowValue) as Property<Int?>
                             property.value = it.newValue
                             selectionModel.focus(selectedRow)
                             selectionModel.select(selectedRow, tableView!!.columns[1])
                             //tableView.edit(2, ) //todo
                         }
                         setOnEditCancel { println("cancel") }
                     }

                    /*.setOnEditCommit {

                         if (it.newValue > 999 || it.newValue < 0) {
                             error("Невалидное значение")
                             //editModel.rollbackSelected()
                         }
                         //editModel.commit()
                         //tableView!!.selectionModel.select(selectedRow, tableView!!.columns[3])

                     }*/
                    column("Площадь", Area::area).makeEditable().setOnEditCommit {

                        if(it.newValue > 9999 || it.newValue < 0){
                            error("Невалидное значение")
                            editModel.rollbackSelected()
                        }
                        val property = it.tableColumn.getCellObservableValue(it.rowValue) as Property<Double?>
                        property.value = it.newValue
                        selectionModel.focus(selectedRow)
                        selectionModel.select(selectedRow, tableView!!.columns[2])
                    }
                    column("К. защитности", Area::categoryProtection).makeEditable().useComboBox(dataTypes.categoryProtection.values.toList().asObservable())
                    readonlyColumn("К. земель", Area::categoryArea)
                    column("ОЗУ", Area::ozu).makeEditable().useComboBox(dataTypes.ozu.values.toList().asObservable())
                    column("lesb", Area::lesb).makeEditable().setOnEditCommit {
                        if(it.newValue.length > 4){
                            error("Невалидное значение")
                            editModel.rollbackSelected()
                        }
                        val property = it.tableColumn.getCellObservableValue(it.rowValue) as Property<String?>
                        property.value = it.newValue
                        selectionModel.focus(selectedRow)
                        selectionModel.select(selectedRow, tableView!!.columns[6])
                    }
                    selectionModel.selectedItemProperty().onChange {
                        selected = this.selectedItem
                        selectedRow = this.selectedCell?.row ?: selectedRow
                        selectedCol = this.selectedColumn
                    }



                    enableCellEditing() //enables easier cell navigation/editing
                    //enableDirtyTracking() //flags cells that are dirty

                    tableViewEditModel = editModel





                   /* areaColumn.setOnEditCommit {
                        try{
                            it.newValue
                        }
                    }*/


                    /*areaColumn.setOnEditCommit {
                        if (it.newValue < 0) alert(Alert.AlertType.ERROR, "Error", "Отрицательная площадь")
                    }*/

                }
            }

            tab("Пакетное обновление"){
                var par1Key: ComboBox<String>? = null
                var par2Key: ComboBox<String>? = null
                var par1Val: TextField? = null
                var par2Val: TextField? = null
                var parRes: ComboBox<String>? = null
                var parResVal: TextField? = null

                isClosable = false
                val margins = Insets(20.0)
                vbox {
                    hbox{
                        val filterParameters = dataTypes.filterParameters
                        vbox {

                            label("Параметр 1")
                            par1Key = combobox(values = filterParameters) { }
                            label("Параметр 2")
                            par2Key = combobox(values = filterParameters) { }
                            vboxConstraints { margin = margins }
                        }
                        vbox {
                            label("Значение 1")
                            par1Val = textfield {  }
                            label("Значение 2")
                            par2Val = textfield {  }
                            vboxConstraints { margin = margins }
                        }
                        vbox {
                            label("Применить к...")
                            parRes = combobox(values = dataTypes.executeParameters) {  }
                            vboxConstraints { margin = margins }
                        }
                        vbox {
                            label("Значение")
                            parResVal = textfield{}
                            vboxConstraints { margin = margins }
                        }
                        vboxConstraints { margin = margins }
                    }
                    vbox{
                        label("Осторожно! Формат значений должен строго совпадать с табличным во избежание потери данных"){
                            vboxConstraints { maxWidth = 400.0 }

                        }
                        button("Применить") {
                            action{
                                if(parRes!!.value == null || (par1Key!!.value == null && par2Key!!.value == null)){
                                    error("Значения не выбраны")
                                    return@action
                                }

                                controller.executeUtil(
                                    par1Key!!.value to par1Val!!.text,
                                    par2Key!!.value to par2Val!!.text,
                                    parRes!!.value to parResVal!!.text)
                                status.text = "Операция выполнена" //todo ani

                                    //tableViewEditModel.commit()
                            }

                        }
                        vboxConstraints { margin = margins }
                    }
                }

            }


        }

        status = label{
            val fade = FadeTransition()
            fade.node = this
            fade.fromValue = 0.0
            fade.toValue = 1.0
            fade.isAutoReverse = true
            fade.cycleCount = 2
            fade.duration = javafx.util.Duration(500.0)
            vboxConstraints {
                margin = Insets(5.0)
                minWidth = 500.0
            }
            /*style{
                backgroundColor += c(255, 123, 123, 1.0)
            }*/
            textProperty().onChange {
                fade.playFromStart()
            }
        }

    }




}

class IntToStringConverter(private val dataMap: Map<Int, String>): IntegerStringConverter(){
    override fun toString(value: Int?): String {
        return dataMap[value]!!
    }

    override fun fromString(value: String?): Int {
        return dataMap.filterValues { it == value }.iterator().next().key
    }
}

