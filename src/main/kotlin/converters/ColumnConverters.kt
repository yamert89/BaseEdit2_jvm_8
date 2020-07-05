package converters

import GenController
import javafx.util.StringConverter
import tornadofx.Controller
import tornadofx.find
import java.lang.NumberFormatException

class AreaConverter: StringConverter<Double>(){
    override fun fromString(string: String?): Double {
        var d = 0.0
        try{
            if(string == null) return d
            val string2 = string.replace(",", ".")
            d = string2.toDouble()
            if(string2.contains(".") && string2.length - string2.indexOfLast { it == '.' } > 2) throw Exception()
            return d
        }catch (e: NumberFormatException){
            tornadofx.error("Ошибка", "Не удалось преобразовать в число")
        }catch (e: Exception){
            tornadofx.error("Ошибка", "Введите десятичное число с одним знаком после запятой")
        }
        return 0.0
    }

    override fun toString(`object`: Double?): String {
        return `object`.toString()
    }

}

class NumberConverter: StringConverter<Int>() {
    val controller = find(GenController::class)
    override fun toString(`object`: Int?): String {
        return `object`.toString()
    }

    override fun fromString(string: String?): Int {
        var v = 0
        try{
            v = string?.toInt() ?: v
            if(controller.tableData.any { it.numberKv == controller.selected!!.numberKv && it.number == v }) throw IllegalStateException()
            return v
        }catch (e: IllegalStateException){
            tornadofx.error("Ошибка", "Выдел $v уже есть в базе")
        }catch (e: Exception){
            tornadofx.error("Ошибка", "Не удалось преобразовать в число")
        }
        return 0
    }
}