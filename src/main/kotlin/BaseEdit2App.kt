import javafx.animation.FadeTransition
import javafx.beans.property.IntegerProperty
import javafx.beans.property.Property
import javafx.event.EventType
import javafx.geometry.Insets
import javafx.scene.Parent
import javafx.scene.control.*
import javafx.scene.control.cell.TextFieldTableCell
import javafx.scene.image.ImageView
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyEvent
import javafx.scene.layout.Background
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import javafx.util.converter.IntegerStringConverter
import tornadofx.*
import tornadofx.Stylesheet.Companion.progressBar


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
    private var editEvent: TableColumn.CellEditEvent<Area, String>? = null
    private val model: AreaModel by inject()
    private val dataTypes = DataTypes()
    private var colum: TableColumn<Area, String?>? = null
    private var tableViewEditModel: TableViewEditModel<Area> by singleAssign()
    private var status = Label()

    private var progress = ProgressBar().apply {
        vgrow = Priority.ALWAYS

    }
    init {
        primaryStage.setOnCloseRequest {
            AppPreferences.savePreferences()
            if (controller.tableData.isEmpty()) return@setOnCloseRequest
            confirm("Подтверждение", "Сохранить?", confirmButton = ButtonType.OK, cancelButton = ButtonType.NO){
                if (!controller.preSaveCheck()) return@setOnCloseRequest
                controller.save(null)
            }

        }

        primaryStage.setOnShown {
            /*primaryStage.scene.setOnKeyPressed {

                if (it.code == KeyCode.DOWN || it.code == KeyCode.UP || it.code == KeyCode.LEFT || it.code == KeyCode.RIGHT) {
                    println(tableView!!.columns[6].onEditStartProperty())

                    val tableView = tableView as TableView<Area>
                    //println(tableView.editingCellProperty().value)
                    //tableView.regainFocusAfterEdit()
                    //selectedCol.setOnEditStart {  }

                    //controller.tableData[selectedRow].lesb = "777"



                    val property = tableView.columns[6].getCellObservableValue(selectedRow) as Property<String>
                    property.value = "&&&"
                    tableViewEditModel.commit(controller.tableData[selectedRow])

                    tableView.selectionModel.focus(selectedRow + 1)
                    tableView.selectionModel.select(selectedRow + 1, tableView.selectedColumn)


                }

            }*/
        }

    }


    override val root = vbox {
        try {
            menubar {
                padding = Insets(0.0)
                menu("Файл"){
                    item("Открыть"){
                        action {
                            val files = chooseFile(
                                "Выберите файл",
                                owner = primaryStage,
                                mode = FileChooserMode.Single,
                                filters = arrayOf()
                            )
                            if (files.isEmpty()) return@action
                            controller.tableData.clear()
                            //todo filters


                            runAsyncWithProgress(progress = progress) {
                                controller.initData(files[0])
                                println("end init")
                            } ui {
                                println("visible false")
                            }
                        }
                    }
                    item("Сохранить"){
                        action {
                            if (!controller.preSaveCheck()) return@action
                            runAsyncWithProgress {
                                controller.save(null)
                            }
                        }
                    }
                    item("Сохранить как .."){
                        action {
                            if (!controller.preSaveCheck()) return@action
                            val list = chooseFile(
                                "Выберите файл",
                                mode = FileChooserMode.Save,
                                filters = arrayOf(),
                                owner = primaryStage
                            )
                            val path = list[0].absolutePath
                            runAsyncWithProgress {
                                controller.save(path)
                            }
                        }

                    }
                }
                menu("?"){
                    item("Горячие клавиши"){
                        action {
                            information("Горячие клавиши", "Num+ - добавить выдел\n" +
                                    "Num- - удалить выдел")
                        }
                    }
                    item("Настройки"){
                        action {
                            find(Preferences::class).openModal()
                        }
                    }
                    item("О программе"){
                        action {
                            information(
                                "BaseEdit2 (SKL редактор)  v. 1.0",
                                "Разработчик - Порохин Александр\n\nРОСЛЕСИНФОРГ 2020",
                                owner = primaryStage
                            )
                        }
                    }
                }
            }

            hbox {

                buttonbar {
                    hboxConstraints {
                        padding = Insets(1.0)
                        minHeight = 24.0
                        prefHeight = 24.0
                        margin = Insets(5.0)

                    }

                    val buttonFontSize = Dimension(7.0, Dimension.LinearUnits.pt)




                    /*button("Открыть") {

                        style {
                            fontSize = buttonFontSize
                        }

                        onHover {
                            println(this.width)
                            println(this.prefWidth)
                            println(this.maxWidth)
                        }


                        //println(resources["/PNG/New Document.png"])

                        *//*graphic = resources.imageview("/PNG/Add.png").apply {
                            fitHeight = 24.0
                            fitWidth = 24.0
                        }*//*
                        action {
                            val files = chooseFile(
                                "Выберите файл",
                                owner = primaryStage,
                                mode = FileChooserMode.Single,
                                filters = arrayOf()
                            )
                            if (files.isEmpty()) return@action
                            controller.tableData.clear()
                            //todo filters


                            runAsyncWithProgress*//*(progress = progressBar)*//* {
                                controller.initData(files[0])
                                println("end init")
                            } ui {
                                println("visible false")
                            }

                        }
                        //hboxConstraints { margin = Insets(10.0) }

                    }*/


                    button("Добавить") {
                        style {
                            fontSize = buttonFontSize
                        }

                        /*style{
                        maxHeight = Dimension(10.0, Dimension.LinearUnits.px)
                        maxWidth = Dimension(10.0, Dimension.LinearUnits.px)
                        backgroundRadius = multi(box(Dimension(10.0, Dimension.LinearUnits.px)))
                        //background = Background.EMPTY

                    }*/

                        /*onHover {
                        style{
                            backgroundColor += c("#000000")
                        }
                    }*/

                        //maxWidth = 10.0
                        //prefWidth = 10.0

                        //prefHeight = 100.0


                        /*val imageView = resources.imageview("/Add Green Button.png")
                        imageView.fitHeight = 24.0
                        imageView.fitWidth = 24.0
                        graphic = imageView*/
                        /*graphic = FontAwesomeIconView(PLUS_CIRCLE).apply {
                        style {
                            fill = c("#818181")
                        }
                        glyphSize = 18
                    }
*/
                        //addClass("icon-only")


                        /*hboxConstraints {
                        margin = Insets(10.0)
                        prefWidth = 32.0

                    }*/
                        action {
                            if (selected == null) {
                                println("selected is null")
                                return@action
                            }
                            var item = selected!!
                            item =
                                Area(0, item.numberKv, 0.0, item.categoryArea, "-", item.ozu, item.lesb, item.rawData)

                            controller.tableData.add(selectedRow, item)

                            tableView!!.selectionModel!!.select(selectedRow, tableView!!.columns[1])

                        }

                        shortcut(KeyCodeCombination(KeyCode.ADD))

                    }
                    button("Удалить") {
                        style {
                            fontSize = buttonFontSize
                        }
                        //hboxConstraints { margin = Insets(10.0) }
                        /*graphic = resources.imageview("/Minus Green Button.png").apply {
                            fitHeight = 24.0
                            fitWidth = 24.0
                        }*/
                        action {
                            alert(
                                Alert.AlertType.CONFIRMATION,
                                "Удалить?",
                                owner = primaryStage,
                                actionFn = { buttonType ->
                                    if (buttonType == ButtonType.OK) {
                                        controller.tableData.removeAt(selectedRow)
                                        tableView!!.selectionModel.select(selectedRow + 1, selectedCol)

                                    }
                                })

                        }
                        shortcut(KeyCodeCombination(KeyCode.SUBTRACT))
                    }



                }


            }
        }catch (e: Exception){
            e.printStackTrace()
        }
        tabpane {
            vgrow = Priority.ALWAYS

            tab("Редактор"){
                vgrow = Priority.ALWAYS
                isClosable = false
                tableView = tableview(controller.getData()) {
                    fixedCellSize = 22.0

                    fun <T> columnOnEdit(editEvent: TableColumn.CellEditEvent<Area, T>, idx: Int, condition: () -> Boolean){
                        if(condition()){
                            error("Невалидное значение")
                            editModel.rollbackSelected()
                        }
                        val property = editEvent.tableColumn.getCellObservableValue(editEvent.rowValue) as Property<T?>
                        if (idx == 6){
                            var res = editEvent.newValue as String
                            if((editEvent.newValue as String).length < 4) while (res.length < 4) res = "0$res"
                            property.value = res as T
                        } else property.value = editEvent.newValue
                        selectionModel.focus(selectedRow)
                        selectionModel.select(selectedRow, tableView!!.columns[idx])

                    }

                    style(true) {
                        borderColor += box(Color.GRAY)
                    }
                    isEditable = true
                    readonlyColumn("Кв", Area::numberKv)
                     column("Выд", Area::number).apply {
                         makeEditable()

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

                         setOnEditCommit { columnOnEdit(it, 1){ it.newValue > 999 || it.newValue < 0}
                             //selectionModel.select(selectedRow, tableView!!.columns[2])
                             //tableView.edit(selectedRow, tableView.columns[2]) //todo
                         }
                         //setOnEditCancel { columnOnEdit(it, 1){it.newValue > 999 || it.newValue < 0} }
                     }


                    column("Площадь", Area::area).apply {
                        makeEditable()
                        setOnEditCommit { columnOnEdit(it, 2){it.newValue > 9999 || it.newValue < 0} }
                        //setOnEditCancel { columnOnEdit(it, 2){it.newValue > 9999 || it.newValue < 0} }

                    }
                    column("К. защитности", Area::categoryProtection).makeEditable().useComboBox(dataTypes.categoryProtection.values.toList().asObservable())
                    readonlyColumn("К. земель", Area::categoryArea)
                    column("ОЗУ", Area::ozu).makeEditable().useComboBox(dataTypes.ozu.values.toList().asObservable())
                    column("lesb", Area::lesb).apply {
                        makeEditable()
                        setOnEditCommit {
                            columnOnEdit(it, 6){it.newValue.length > 4}
                        }
                       // setOnEditCancel { columnOnEdit(it, 6){it.newValue.length > 4} }
                    }

                    selectionModel.selectedItemProperty().onChange {

                        selected = this.selectedItem
                        selectedRow = this.selectedCell?.row ?: selectedRow
                        selectedCol = this.selectedColumn
                        println("select")
                        if (selectedCol == tableView!!.columns[3]) tableView!!.edit(selectedRow, selectedCol)
                    }






                    enableCellEditing() //enables easier cell navigation/editing
                        //enableDirtyTracking() //flags cells that are dirty

                    tableViewEditModel = editModel




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

                                val res = controller.executeUtil(
                                    par1Key!!.value to par1Val!!.text,
                                    par2Key!!.value to par2Val!!.text,
                                    parRes!!.value to parResVal!!.text)

                                when(res){
                                    0 -> error("Отмена", "Искомые записи не найдены")
                                    -1 -> error("Отмена", "Неправильное значение")
                                    1 -> status.text = "Операция выполнена"
                                }


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
            fade.duration = javafx.util.Duration(1000.0)
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
        //progress = progressbar(0.0) {



    }







}

class Preferences : Fragment("Настройки"){

    override val root = pane {
        val m10 = Insets(10.0)
        padding = m10
        vbox {
            padding = Insets(20.0)
            checkbox("Проверять на пропуски выделов при сохранении", AppPreferences.checkSkippedProperty ){
                vboxConstraints { margin = m10 }
            }
            checkbox("Делать резервную копию при сохранении", AppPreferences.saveBackupsProperty){
                vboxConstraints { margin = m10 }
            }
        }

    }
}

class TextFieldTableCellMod<T> : TableCell<Area, T>(){
    override fun cancelEdit() {
        super.cancelEdit()
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

