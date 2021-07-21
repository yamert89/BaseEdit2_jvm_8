package converters

import GenController
import javafx.util.StringConverter
import roslesinforg.porokhin.areatypes.GeneralTypes
import tornadofx.Controller
import tornadofx.find
import java.lang.NumberFormatException
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty0
import kotlin.reflect.KProperty1

class AreaConverter: StringConverter<Float>(){
    override fun fromString(string: String?): Float {
        var d = 0f
        try{
            if(string == null) return d
            val string2 = string.replace(",", ".")
            d = string2.toFloat()
            if(string2.contains(".") && string2.length - string2.indexOfLast { it == '.' } > 2) throw Exception()
            return d
        }catch (e: NumberFormatException){
            tornadofx.error("Ошибка", "Не удалось преобразовать в число")
        }catch (e: Exception){
            tornadofx.error("Ошибка", "Введите десятичное число с одним знаком после запятой")
        }
        return 0f
    }

    override fun toString(`object`: Float?): String {
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

class CodeMappingConverter(private val map: KProperty0<Map<Int, String>>): StringConverter<Int>(){
    override fun fromString(string: String): Int {
        //if (string != "-") println("$string : ${map.get().entries.find { it.value == string }}")
        return if (string == "-") 0 else map.get().entries.find { it.value == string }?.key ?: string.toInt()
    }

    override fun toString(`object`: Int?): String {
        //println("$`object` : ${map.get().containsKey(`object`)}")
        return if (`object` == 0) "-" else map.get()[`object`] ?: `object`.toString()
    }

}

class CategoryProtectionConverter: StringConverter<Int>(){
    override fun fromString(string: String): Int {
        return GeneralTypes.categoryProtection.entries.find { it.value == string }!!.key
    }

    override fun toString(`object`: Int): String {
        return GeneralTypes.categoryProtection[`object`]
    }
}