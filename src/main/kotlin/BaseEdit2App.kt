import javafx.animation.FadeTransition
import javafx.animation.KeyFrame
import javafx.animation.KeyValue
import javafx.animation.Timeline
import javafx.beans.property.IntegerProperty
import javafx.beans.property.Property
import javafx.event.EventHandler
import javafx.event.EventType
import javafx.geometry.Insets
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.control.*
import javafx.scene.control.cell.TextFieldTableCell
import javafx.scene.image.ImageView
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyEvent
import javafx.scene.layout.Background
import javafx.scene.layout.Pane
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import javafx.util.Duration
import javafx.util.StringConverter
import javafx.util.converter.IntegerStringConverter
import tornadofx.*
import tornadofx.Stylesheet.Companion.disabled
import tornadofx.Stylesheet.Companion.progressBar
import java.lang.NumberFormatException
import java.lang.reflect.Field
import java.net.URI


fun main() {

    launch<BaseEdit2>()

}

//todo coloring rows
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
    private var addButton = Button()
    private var delButton = addButton
    private var saveButton = addButton

    private var progress = ProgressBar().apply {
        vgrow = Priority.ALWAYS

    }
    init {
        title = "BaseEdit2"
        primaryStage.setOnCloseRequest {
            AppPreferences.savePreferences()
            if (controller.tableData.isEmpty()) return@setOnCloseRequest
            /*confirm("Подтверждение", "Сохранить?",
                confirmButton = ButtonType.OK, cancelButton = ButtonType.NO,
                owner = primaryStage){
                if (!controller.preSaveCheck()) return@setOnCloseRequest
                controller.save(null)
            }*/
            alert(Alert.AlertType.CONFIRMATION, "Сохранить?", null, ButtonType.OK, ButtonType.NO, owner = primaryStage, title = "Подтверждение"){
                if (it == ButtonType.NO) return@setOnCloseRequest
                if (!controller.preSaveCheck()) information("Операция закрытия необратима. Файл будет сохранен с ошибками")
                controller.save(null)
            }

        }

        primaryStage.setOnShown {
            primaryStage.icons.add(resources.image("Desktop.png"))

            /*primaryStage.scene.setOnKeyPressed {


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


                            var res = false
                            runAsyncWithProgress(progress = progress) {
                                try {
                                    controller.initData(files[0])
                                    res = true
                                }catch (e: Exception){
                                    return@runAsyncWithProgress
                                }
                                println("end init")
                            } ui {

                                addButton.disableProperty().set(false)
                                delButton.disableProperty().set(false)
                                saveButton.disableProperty().set(false)
                                if(!res) error("Ошибка", "Ошибка чтения файла")

                            }

                        }
                    }
                    item("Сохранить"){
                        action {
                            if (controller.tableData.isEmpty() || !controller.preSaveCheck()) return@action
                            runAsyncWithProgress {
                                controller.save(null)
                            }ui {status.text = "Сохранено"}
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
                            }ui {status.text = "Сохранено"}
                        }

                    }
                }
                menu("Правка"){
                    item("Восстановить удалённый выдел"){
                        action{
                            if(controller.deletedRows.isEmpty()) {
                                status.text = "Нет удалённых выделов"
                                return@action
                            }
                            val pairOfArea = controller.deletedRows.pollLast() as Pair<Int, Area>
                            controller.tableData[pairOfArea.first] = pairOfArea.second
                            status.text = "Выдел ${pairOfArea.second.number} квартала ${pairOfArea.second.numberKv} восстановлен"
                        }
                    }
                }
                menu("?"){
                    item("Настройки"){
                        action {
                            find(Preferences::class).openModal()
                        }
                    }
                    item("О программе"){
                        action {
                            information(
                                "BaseEdit2 (SKL редактор)  v.1.3",
                                "Порохин Александр\n\nРОСЛЕСИНФОРГ 2020",
                                owner = primaryStage
                            )
                        }
                    }
                }
            }



                hbox {

                    padding = Insets(3.0)

                    val buttonFontSize = Dimension(7.0, Dimension.LinearUnits.pt)

                    saveButton = button{
                        disableProperty().set(true)
                        hboxConstraints { marginLeftRight(10.0) }
                        style {
                            fontSize = buttonFontSize
                            prefWidth = Dimension(25.0, Dimension.LinearUnits.px)
                            padding = box(Dimension(1.0, Dimension.LinearUnits.px))

                        }
                        graphic = resources.imageview("/Export To Document.png").apply {
                            fitHeight = 20.0
                            fitWidth = 20.0
                        }
                        action {
                            if (controller.tableData.isEmpty() || !controller.preSaveCheck()) return@action
                            runAsyncWithProgress {
                                controller.save(null)
                            }ui{status.text = "Сохранено"}
                        }
                        tooltip("Сохранить"){style{fontSize = buttonFontSize + 2}}
                    }

                    addButton = button(/*"Добавить"*/) {
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

                        val imageView = resources.imageview("/Add Green Button.png")
                        imageView.fitHeight = 20.0
                        imageView.fitWidth = 20.0
                        graphic = imageView

                        action {
                            if (selected == null) {
                                println("selected is null")
                                return@action
                            }
                            var item = selected!!
                            item = Area(0, item.numberKv, 0.0, item.categoryArea, dataTypes.EMPTY_CATEGORY_PROTECTION, item.ozu, item.lesb, item.rawData)

                            controller.tableData.add(selectedRow, item)

                            tableView!!.selectionModel!!.select(selectedRow, tableView!!.columns[1])

                        }

                        shortcut(KeyCodeCombination(KeyCode.ADD))

                    }
                    delButton = button(/*"Удалить"*/) {
                        disableProperty().set(true)
                        style {
                            fontSize = buttonFontSize
                            prefWidth = Dimension(25.0, Dimension.LinearUnits.px)
                            padding = box(Dimension(1.0, Dimension.LinearUnits.px))

                        }
                        graphic = resources.imageview("/Minus Green Button.png").apply {
                            fitHeight = 20.0
                            fitWidth = 20.0
                        }
                        action {
                            alert(
                                Alert.AlertType.CONFIRMATION,
                                "Удалить?",
                                owner = primaryStage,
                                actionFn = { buttonType ->
                                    if (buttonType == ButtonType.OK) {
                                        controller.deletedRows.add(selectedRow to controller.tableData[selectedRow])
                                        controller.tableData.removeAt(selectedRow)
                                        tableView!!.selectionModel.select(selectedRow + 1, selectedCol)
                                    }
                                })

                        }
                        tooltip("Удалить выдел ( Num - )"){style{fontSize = buttonFontSize + 2}}
                        shortcut(KeyCodeCombination(KeyCode.SUBTRACT))
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
                            return
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
                         makeEditable(object: StringConverter<Int>(){
                             override fun toString(`object`: Int?): String {
                                 return `object`.toString()
                             }

                             override fun fromString(string: String?): Int {
                                 var v = 0
                                 try{
                                     v = string?.toInt() ?: v
                                     if(controller.tableData.any { it.numberKv == selected!!.numberKv && it.number == v }) throw IllegalStateException()
                                     return v
                                 }catch (e: IllegalStateException){
                                     error("Ошибка", "Выдел $v уже есть в базе")
                                 }catch (e: Exception){
                                    error("Ошибка", "Не удалось преобразовать в число")
                                 }
                                 return 0
                             }

                         })

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
                        makeEditable(object: StringConverter<Double>() {
                            override fun toString(`object`: Double?): String {
                                return `object`.toString()
                            }

                            override fun fromString(string: String?): Double {
                                var d = 0.0
                                try{
                                    if(string == null) return d
                                    val string2 = string.replace(",", ".")
                                    d = string2.toDouble()
                                    if(string2.contains(".") && string2.length - string2.indexOfLast { it == '.' } > 2) throw Exception()
                                    return d
                                }catch (e: NumberFormatException){
                                    error("Ошибка", "Не удалось преобразовать в число")
                                }catch (e: Exception){
                                    error("Ошибка", "Введите десятичное число с одним знаком после запятой")
                                }
                                return 0.0
                            }
                        })
                        setOnEditCommit { columnOnEdit(it, 2){it.newValue > 9999 || it.newValue < 0} }
                        //setOnEditCancel { columnOnEdit(it, 2){it.newValue > 9999 || it.newValue < 0} }

                    }
                    column("К. защитности", Area::categoryProtection).makeEditable().useComboBox(dataTypes.categoryProtection.values.toList().asObservable())
                    readonlyColumn("К. земель", Area::categoryArea)
                    column("ОЗУ", Area::ozu).makeEditable().useComboBox(dataTypes.ozu.values.toList().asObservable())
                    column("lesb", Area::lesb).apply {
                        makeEditable()
                        setOnEditCommit {
                            var notValid = false
                            try{
                                val intv = it.newValue.toInt()
                                notValid = intv > 9999 || intv < 0
                            }catch (e: NumberFormatException){
                                notValid = true
                            }
                            columnOnEdit(it, 6){notValid || it.newValue.length > 4}
                        }
                       // setOnEditCancel { columnOnEdit(it, 6){it.newValue.length > 4} }
                    }

                    selectionModel.selectedItemProperty().onChange {

                        selected = this.selectedItem
                        selectedRow = this.selectedCell?.row ?: selectedRow
                        selectedCol = this.selectedColumn
                        if (selectedCol == tableView!!.columns[3]) tableView!!.edit(selectedRow, selectedCol)
                    }






                    enableCellEditing() //enables easier cell navigation/editing
                        //enableDirtyTracking() //flags cells that are dirty

                    tableViewEditModel = editModel




                }
            }

            tab("Пакетное обновление"){//todo inactive while file not opened
                var par1Key: ComboBox<String>? = null
                var par2Key: ComboBox<String>? = null
                var par1Val: Node? = null
                var par2Val: Node? = null
                var parRes: ComboBox<String>? = null
                var parResVal: Node? = null

                isClosable = false
                val margins = Insets(10.0)
                vbox {
                    fun initComboBoxChangeListener(comboBox: ComboBox<String>, fieldNumber: Int){
                        comboBox.valueProperty().onChange {
                            var newNode : Node? = null
                            when(it){
                                dataTypes.KV, dataTypes.CATEGORY_AREA, dataTypes.LESB -> newNode = TextField()
                                dataTypes.CATEGORY_PROTECTION ->  newNode = ComboBox(dataTypes.categoryProtection.values.toList().plus(dataTypes.EMPTY_CATEGORY_PROTECTION).toObservable()).apply { selectionModel.select(0) }
                                dataTypes.OZU -> newNode = ComboBox(dataTypes.ozu.values.toList().toObservable()).apply { selectionModel.select(0) }
                                else -> newNode = TextField().apply{this.isDisable = true}
                            }
                            when(fieldNumber){
                                1 -> {
                                    par1Val!!.replaceWith(newNode)
                                    par1Val = newNode
                                }
                                2 -> {
                                    par2Val!!.replaceWith(newNode)
                                    par2Val = newNode
                                }
                                3 -> {
                                    parResVal!!.replaceWith(newNode)
                                    parResVal = newNode
                                }
                            }

                        }
                    }

                    padding = margins
                    val filterParameters = dataTypes.filterParameters
                    hbox {
                        vboxConstraints { margin = margins }
                        label("Отобрать значения:")
                    }
                    hbox {
                        vboxConstraints { margin = margins }
                        par1Key = combobox(values = filterParameters) {}
                        initComboBoxChangeListener(par1Key as ComboBox<String>, 1)
                        label("="){
                            hboxConstraints {
                                marginLeftRight(10.0)
                            }
                        }
                        par1Val = textfield { isDisable = true }

                    }
                    hbox {
                        vboxConstraints { margin = margins }
                        par2Key = combobox(values = filterParameters) { }
                        initComboBoxChangeListener(par2Key as ComboBox<String>, 2)
                        label("="){
                            hboxConstraints {
                                marginLeftRight(10.0)
                            }
                        }
                        par2Val = textfield { isDisable = true }

                    }
                    hbox{
                        vboxConstraints { margin = margins }
                        label("И применить:"){hboxConstraints { marginRight = 10.0 }}
                    }

                    hbox {
                        vboxConstraints { margin = margins }

                        parRes = combobox(values = dataTypes.executeParameters) {  }
                        initComboBoxChangeListener(parRes!!, 3)
                        label("="){
                            hboxConstraints {
                                marginLeftRight(10.0)
                            }
                        }
                        parResVal = textfield { isDisable = true }


                    }


                    vbox{
                        label("Формат значений должен совпадать с табличным "){
                            vboxConstraints { maxWidth = 400.0 }

                        }
                        button("Применить") {
                            action{
                                if(controller.tableData.isEmpty()) error("Oтмена","Таблица пуста"){return@action}
                                if(parRes!!.value == null || (par1Key!!.value == null && par2Key!!.value == null)){
                                    error("Значения не выбраны")
                                    return@action
                                }
                                val param1: String = when(par1Val){ //todo more pithiness
                                    is ComboBox<*> -> (par1Val as ComboBox<String>).selectedItem!!
                                    else -> (par1Val as TextField).text
                                }

                                val param2: String = when(par2Val){
                                    is ComboBox<*> -> (par2Val as ComboBox<String>).selectedItem!!
                                    else -> (par2Val as TextField).text
                                }

                                val resParam = when(parResVal){
                                    is ComboBox<*> -> (parResVal as ComboBox<String>).selectedItem!!
                                    else -> (parResVal as TextField).text
                                }

                                val res = controller.executeUtil(
                                    par1Key!!.value to param1,
                                    par2Key!!.value to param2,
                                    parRes!!.value to resParam)

                                when(res[0]){
                                    0 -> error("Отмена", "Искомые записи не найдены")
                                    -1 -> error("Отмена", "Неправильное значение")
                                    1 -> status.text = "Операция выполнена. Обновлено ${res[1]} записей"
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
            style{
                textFill = Color.RED
                fontSize = Dimension(11.0, Dimension.LinearUnits.pt)
            }
            /*val fade = FadeTransition()
            fade.onFinished = EventHandler { this.text = "" }
            fade.node = this
            fade.fromValue = 1.0
            fade.toValue = 0.0
            fade.isAutoReverse = true
            fade.cycleCount = 1
            fade.duration = javafx.util.Duration(2500.0)*/


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
        //progress = progressbar(0.0) {



    }





}

class Preferences : Fragment("Настройки"){
    private val controller: GenController by inject()

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
            checkbox("Проверять изменение площадей", AppPreferences.checkAreasProperty) {
                vboxConstraints { margin = m10 }
                if (controller.tableData.isEmpty()) return@checkbox
                AppPreferences.checkAreasProperty.onChange {
                    if (it) alert(
                    Alert.AlertType.CONFIRMATION,
                    "Подтверждение",
                    "Посчитать текущие площади как начальные?",
                    ButtonType.YES,
                    ButtonType.NO,
                    owner = primaryStage,
                    title = "?"
                    ){ btnType ->
                    if (btnType == ButtonType.YES){
                        controller.sumAreasForKv = controller.calculateAreasForKv()
                    } else if(btnType == ButtonType.NO) close()

                } }
            }
        }

    }
}


