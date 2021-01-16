package views

import GenController
import Notification
import javafx.geometry.Insets
import javafx.scene.Node
import javafx.scene.control.ComboBox
import javafx.scene.control.Tab
import javafx.scene.control.TextField
import tornadofx.*

class PackageUpdateInitFunction(private val tab: Tab): TabInitFunction() {
    private val controller = find(GenController::class)
    private val notification = find(Notification::class)
    override fun getInitial(): Tab.() -> Unit {
        return tab.initial()
    }

    private fun Tab.initial(): Tab.() -> Unit {
        return {
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
                                tornadofx.error("Значения не выбраны")
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
                                1 -> notification.notif("Операция выполнена. Обновлено ${res[1]} записей")
                            }


                            //tableViewEditModel.commit()
                        }

                    }
                    vboxConstraints { margin = margins }
                }

            }
        }

    }
}