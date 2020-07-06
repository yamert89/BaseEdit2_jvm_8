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
        //progress = progressbar(0.0) {

    }


}

