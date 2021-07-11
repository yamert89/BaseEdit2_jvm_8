package views

import SKLArea
import GenController
import Notification
import javafx.geometry.Insets
import javafx.scene.control.MenuBar
import javafx.stage.Stage
import tornadofx.*
import java.nio.file.Paths

class MenuBarInitFunction(private val menuBar: MenuBar, private val primaryStage: Stage): InitFunction<MenuBar> {
    private val controller = find(GenController::class)
    private val notification = find(Notification::class)
    override fun getInitial(): MenuBar.() -> Unit {
        return menuBar.initial()
    }
    private fun MenuBar.initial(): MenuBar.() -> Unit{
        return{
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
                        controller.openFile(files[0])
                    }
                }
                val recentPath = Paths.get(AppPreferences.recentPath)

                if (recentPath.toString().isNotEmpty() && recentPath.toFile().exists()) item("Открыть последний: <${recentPath.fileName}>"){
                    action {
                        controller.openFile(recentPath.toFile())
                    }
                }
                item("Сохранить"){
                    action {
                        if (controller.tableData.isEmpty() || !controller.preSaveCheck()) return@action
                        runAsyncWithProgress {
                            controller.save(null)
                        }ui {notification.notif("Сохранено")}
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
                        }ui {notification.notif("Сохранено")}
                    }

                }
            }
            menu("Правка"){
                item("Восстановить удалённый выдел"){
                    action{
                        if(controller.deletedRows.isEmpty()) {
                            notification.notif("Нет удалённых выделов")
                            return@action
                        }
                        val pairOfArea = controller.deletedRows.pollLast() as Pair<Int, SKLArea>
                        controller.tableData[pairOfArea.first] = pairOfArea.second
                        notification.notif("Выдел ${pairOfArea.second.number} квартала ${pairOfArea.second.numberKv} восстановлен")
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
                            "BaseEdit2 (SKL редактор)  v.1.4.5",
                            "Порохин Александр\n\nРОСЛЕСИНФОРГ 2020",
                            owner = primaryStage
                        )
                    }
                }
            }
        }

    }


}