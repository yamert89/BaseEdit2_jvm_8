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
import views.EditorInitFunction
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

    private val model: AreaModel by inject()
    private var tableViewEditModel: TableViewEditModel<Area> by singleAssign()
    private var status = Label()
    private var addButton = Button()
    private var delButton = addButton
    private var saveButton = addButton
    private var menuBar: MenuBar? = null

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

            fun openFile(file: File){
                controller.tableData.clear()
                var res = false
                menuBar!!.runAsyncWithProgress(progress = progress) {
                    try {
                        controller.initData(file)
                        res = true
                        AppPreferences.recentPath = file.absolutePath
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

            menuBar = menubar {
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
                            openFile(files[0])
                        }
                    }
                    val recentPath = Paths.get(AppPreferences.recentPath)

                    if (recentPath.toString().isNotEmpty() && recentPath.toFile().exists()) item("Открыть последний: <${recentPath.fileName}>"){
                        action {
                            openFile(recentPath.toFile())
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
                                "BaseEdit2 (SKL редактор)  v.1.4.2",
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
                            if (controller.selected == null) {
                                println("selected is null")
                                return@action
                            }
                            var item = controller.selected!!
                            item = Area(0, item.numberKv, 0.0, item.categoryArea, DataTypes.EMPTY_CATEGORY_PROTECTION, item.ozu, item.lesb, item.rawData)

                            controller.addArea(item)

                            controller.tableView.selectionModel!!.select(controller.selectedRow, controller.tableView.columns[1])

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
                                        with(controller){
                                           deletedRows.add(selectedRow to tableData[selectedRow])
                                           tableData.removeAt(selectedRow)
                                            tableView.selectionModel.select(selectedRow + 1, selectedCol)
                                        }
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
            //enableWhen { controller.fileOpened }

            tab("Редактор"){EditorInitFunction(this)}

            tab("Пакетное обновление"){
                var par1Key: ComboBox<String>? = null
                var par2Key: ComboBox<String>? = null
                var par1Val: Node? = null
                var par2Val: Node? = null
                var parRes: ComboBox<String>? = null
                var parResVal: Node? = null
                this.enableWhen(controller.fileOpened)
                isClosable = false
                val margins = Insets(10.0)
                vbox {
                    fun initComboBoxChangeListener(comboBox: ComboBox<String>, fieldNumber: Int){
                        comboBox.valueProperty().onChange {
                            var newNode : Node? = null
                            when(it){
                                DataTypes.KV, DataTypes.CATEGORY_AREA, DataTypes.LESB -> newNode = TextField()
                                DataTypes.CATEGORY_PROTECTION ->  newNode = ComboBox(DataTypes.categoryProtection.values.toList().plus(DataTypes.EMPTY_CATEGORY_PROTECTION).toObservable()).apply { selectionModel.select(0) }
                                DataTypes.OZU -> newNode = ComboBox(DataTypes.ozu.values.toList().toObservable()).apply { selectionModel.select(0) }
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
                    val filterParameters = DataTypes.filterParameters
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

                        parRes = combobox(values = DataTypes.executeParameters) {  }
                        initComboBoxChangeListener(parRes!!, 3)
                        label("="){
                            hboxConstraints {
                                marginLeftRight(10.0)
                            }
                        }
                        parResVal = textfield { isDisable = true }


                    }


                    vbox{

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

