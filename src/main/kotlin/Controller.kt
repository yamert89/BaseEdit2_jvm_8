import javafx.collections.ObservableList
import javafx.scene.control.Alert
import tornadofx.Controller
import tornadofx.alert
import tornadofx.asObservable
import tornadofx.confirm
import java.io.File
import java.lang.Exception
import java.lang.IllegalArgumentException
import java.math.BigDecimal
import java.nio.file.Files
import java.nio.file.Paths
import java.text.DecimalFormat
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.locks.Condition
import kotlin.math.abs

class GenController: Controller() {
    val tableData = emptyList<Area>().toMutableList().asObservable()
    var sumAreasForKv :  Map<Int, Double>? = null
    private var filePath = ""
    private val dataTypes = DataTypes()


    fun getData(): ObservableList<Area> {

        return tableData
    }

    fun save(path: String?){

        if (path == null){
            if (AppPreferences.saveBackups) File(filePath).renameTo(File("${filePath}_${LocalTime.now().format(DateTimeFormatter.ofPattern("HH_mm_ss"))}.bak"))
        }else filePath = path
        FileExecutor().saveFile(File(filePath), tableData)
    }

    fun initData(file: File){
        tableData.addAll(FileExecutor().parseFile(file))
        filePath = file.absolutePath
        if(AppPreferences.checkAreas) sumAreasForKv = calculateAreasForKv()
        print("init data done")
    }

    fun executeUtil(param1: Pair<Any?, String>, param2: Pair<Any?, String>, resParam: Pair<Any, String>): Int{
        println("Записей было ${tableData.size}")

        val filteredData = tableData.filter {
            (if(param1.first != dataTypes.EMTPTY && param1.first != null) {
                when(param1.first){
                    dataTypes.KV -> it.numberKv == param1.second.toInt()
                    dataTypes.CATEGORY_AREA -> it.categoryArea == param1.second
                    dataTypes.CATEGORY_PROTECTION -> it.categoryProtection == param1.second
                    dataTypes.OZU -> it.ozu == param1.second
                    dataTypes.LESB -> it.lesb == param1.second
                    else -> throw IllegalArgumentException("invalid param")
                }
            } else true) &&
                    (if(param2.first != "" && param2.first != null) {
                        when(param2.first){
                            dataTypes.KV -> it.numberKv == param2.second.toInt()
                            dataTypes.CATEGORY_AREA -> it.categoryArea == param2.second
                            dataTypes.CATEGORY_PROTECTION -> it.categoryProtection == param2.second
                            dataTypes.OZU -> it.ozu == param2.second
                            dataTypes.LESB -> it.lesb == param2.second
                            else -> throw IllegalArgumentException("invalid param")
                        }
                    } else true)

        }

        println("Изменений ${filteredData}")

        val tempList = HashMap<String, Area>()

        filteredData.forEach { tempList["${it.numberKv}|${it.number}"] = it }
        if(tempList.isEmpty()) return 0

        try {
            tempList.forEach {
                when(resParam.first){
                    dataTypes.CATEGORY_AREA -> {
                        val intView = resParam.second.toInt()
                        if(resParam.second.length != 4 || (intView < 1101 || intView > 2556)) return -1
                        it.value.categoryArea = resParam.second
                    }
                    dataTypes.CATEGORY_PROTECTION -> {
                        val contKey = dataTypes.categoryProtection.containsKey(resParam.second)
                        val contValue = dataTypes.categoryProtection.containsValue(resParam.second)
                        if(!contKey && !contValue) return -1
                        it.value.categoryProtection = if(contKey) dataTypes.categoryProtection[resParam.second] else resParam.second
                    }
                    dataTypes.OZU -> {
                        val conKey = dataTypes.ozu.containsKey(resParam.second)
                        val contValue = dataTypes.ozu.containsValue(resParam.second)
                        if(!conKey && !contValue) return -1
                        it.value.ozu = if (conKey) dataTypes.ozu[resParam.second] else resParam.second
                    }
                    dataTypes.LESB -> {
                        if (resParam.second.length != 4) return -1
                        it.value.lesb = resParam.second
                    }
                }
            }
        }catch (e: Exception){

            return -1
        }

        val indexedMap = HashMap<Int, Area>()

        for (i in 0 until tableData.size){
            val area = tableData[i]
            val key = "${area.numberKv}|${area.number}"
            if (tempList.containsKey(key)) indexedMap[i] = tempList[key]!!
        }
        indexedMap.forEach { tableData.set(it.key, it.value) }
        println("Записей стало ${tableData.size}")
        return 1

    }

    fun preSaveCheck(): Boolean{
        //tableViewEditModel.commit()
        val checkSkipped = AppPreferences.checkSkipped
        val dublicate = mutableListOf<Area>()
        val catProt = mutableListOf<Area>()
        val skipped = HashMap<Area, Int>()
        val zeroNumber = mutableListOf<Area>()
        val map = mutableMapOf<Int, MutableList<Int>>()
        tableData.forEach {
            if (it.categoryProtection == "-") catProt.add(it)
            if(it.number == 0) zeroNumber.add(it)
            if (!map.containsKey(it.numberKv)) map[it.numberKv] = mutableListOf()
            map[it.numberKv]!!.add(it.number)
        }
        if (map.isNotEmpty()){
            map.forEach {
                if (it.value.distinct().size != it.value.size) dublicate.add(tableData.first { el -> el.numberKv == it.key})
                if (!checkSkipped) return@forEach
                if(!skipped.contains(tableData.first { el -> el.numberKv == it.key})){
                    val skippedNumber = it.value.containsSkipped()
                    if( skippedNumber != 0) skipped[tableData.first { el -> el.numberKv == it.key}] = skippedNumber
                }

            }
        }

        var message = ""

        if (catProt.size > 0){
            message += "Категория защитности не проставлена в ${catProt.joinToString(", "){"кв: ${it.numberKv} выд: ${it.number}"}}"
        }
        if (zeroNumber.size > 0){
            message += "\nНомер выдела не проставлен в кв ${zeroNumber.joinToString(", "){ it.numberKv.toString()}}"
        }
        if (dublicate.size > 0){
            message += "\nДубликаты в ${dublicate.joinToString { "кв: ${it.numberKv}"}}"
        }
        if (sumAreasForKv != null && AppPreferences.checkAreas){
            val resMap = HashMap<Int, Double>()
            val check = calculateAreasForKv()
            check.forEach{
                val diff = it.value - sumAreasForKv!![it.key]!!
                if(diff != 0.0) resMap[it.key] = abs(diff)
            }
            if (resMap.isNotEmpty()) message += "\nНе совпадают площади в " + resMap.entries.joinToString { "кв: ${it.key} на ${DecimalFormat("####.#").format(it.value)}" }


        }
        if (checkSkipped && skipped.isNotEmpty()){
            message += "\nПропущены выдела в ${skipped.entries.joinToString { "кв ${it.key.numberKv} после выд ${it.value}" }}"
        }

        if (message.isNotBlank()){
            if (message.startsWith("\nПропущены выдела")) confirm("Сохранить?", content = message){
                return true
            } else alert(Alert.AlertType.ERROR, "Ошибка",  message  )
            return false
        }

        return true
    }

    /*
    * if duplicates not found returns 0
    * else returns number of area*/
    private fun List<Int>.containsSkipped(): Int{
        val sorted = this.sorted()
        var num = sorted.last()
        for(i in (sorted.size - 1) downTo 1){
            if(num - sorted[i-1] > 1) {
                return sorted[i-1]
            }
            num = sorted[i-1]
        }
        return 0
    }

    fun calculateAreasForKv(): Map<Int, Double> {
        val map = HashMap<Int, Double>()
        tableData.forEach {
            if (!map.containsKey(it.numberKv)) map[it.numberKv] = 0.0
            map[it.numberKv] = map[it.numberKv]!! + it.area
        }
        return map
    }


}


