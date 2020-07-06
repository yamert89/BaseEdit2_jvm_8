import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.value.ObservableBooleanValue
import javafx.collections.ObservableList
import javafx.scene.control.*
import javafx.scene.layout.Priority
import tornadofx.*
import java.io.File
import java.lang.Exception
import java.lang.IllegalArgumentException
import java.text.DecimalFormat
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.HashMap
import kotlin.error
import kotlin.math.abs

class GenController: Controller() {
    val tableData = emptyList<Area>().toMutableList().asObservable()
    val deletedRows = ArrayDeque<Pair<Int, Area>>()
    var sumAreasForKv :  Map<Int, Double>? = null
    private var filePath = ""
    var fileOpened = SimpleBooleanProperty(false)
    var selected: Area? = null
    var selectedRow: Int = 0
    var selectedCol: TableColumn<Area, *>? = null
    lateinit var tableView: TableView<Area>
    var addButton = Button()
    var delButton = addButton
    var saveButton = addButton
    var menuBar: MenuBar? = null
    var progress = ProgressBar().apply {
        vgrow = Priority.ALWAYS
    }

    fun addArea(item: Area) = tableData.add(selectedRow, item)



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
        fileOpened.value = true
        print("init data done")
    }


    /*
    * return array where 0-index - code, 1-index - number of rows
    * */
    fun executeUtil(param1: Pair<Any?, String>, param2: Pair<Any?, String>, resParam: Pair<Any, String>): Array<Int>{
        println("Записей было ${tableData.size}")

        val filteredData = tableData.filter {
            (if(param1.first != DataTypes.EMPTY && param1.first != null) {
                when(param1.first){
                    DataTypes.KV -> it.numberKv == param1.second.toInt()
                    DataTypes.CATEGORY_AREA -> it.categoryArea == param1.second
                    DataTypes.CATEGORY_PROTECTION -> it.categoryProtection == param1.second
                    DataTypes.OZU -> it.ozu == param1.second
                    DataTypes.LESB -> it.lesb == param1.second
                    else -> throw IllegalArgumentException("invalid param")
                }
            } else true) &&
                    (if(param2.first != "" && param2.first != null) {
                        when(param2.first){
                            DataTypes.KV -> it.numberKv == param2.second.toInt()
                            DataTypes.CATEGORY_AREA -> it.categoryArea == param2.second
                            DataTypes.CATEGORY_PROTECTION -> it.categoryProtection == param2.second
                            DataTypes.OZU -> it.ozu == param2.second
                            DataTypes.LESB -> it.lesb == param2.second
                            else -> throw IllegalArgumentException("invalid param")
                        }
                    } else true)

        }

        println("Изменений ${filteredData}")

        val tempList = HashMap<String, Area>()

        filteredData.forEach { tempList["${it.numberKv}|${it.number}|${it.categoryArea}"] = it }
        if(tempList.isEmpty()) return arrayOf(0)

        try {
            tempList.forEach {
                when(resParam.first){
                    DataTypes.CATEGORY_AREA -> {
                        val intView = resParam.second.toInt()
                        if(resParam.second.length != 4 || (intView < 1101 || intView > 2556)) return arrayOf(-1)
                        it.value.categoryArea = resParam.second
                    }
                    DataTypes.CATEGORY_PROTECTION -> {
                        val contKey = DataTypes.categoryProtection.containsKey(resParam.second)
                        val contValue = DataTypes.categoryProtection.containsValue(resParam.second)
                        if(!contKey && !contValue) return arrayOf(-1)
                        it.value.categoryProtection = if(contKey) DataTypes.categoryProtection[resParam.second] else resParam.second
                    }
                    DataTypes.OZU -> {
                        val conKey = DataTypes.ozu.containsKey(resParam.second)
                        val contValue = DataTypes.ozu.containsValue(resParam.second)
                        if(!conKey && !contValue) return arrayOf(-1)
                        it.value.ozu = if (conKey) DataTypes.ozu[resParam.second] else resParam.second
                    }
                    DataTypes.LESB -> {
                        if (resParam.second.length != 4) return arrayOf(-1)
                        it.value.lesb = resParam.second
                    }
                }
            }
        }catch (e: Exception){

            return arrayOf(-1)
        }

        val indexedMap = HashMap<Int, Area>()

        for (i in 0 until tableData.size){
            val area = tableData[i]
            val key = "${area.numberKv}|${area.number}|${area.categoryArea}"
            if (tempList.containsKey(key)) indexedMap[i] = tempList[key]!!
        }
        indexedMap.forEach { tableData.set(it.key, it.value) }
        println("Записей стало ${tableData.size}")
        return arrayOf(1, tempList.size)

    }

    fun preSaveCheck(): Boolean{
        //tableViewEditModel.commit()
        val checkSkipped = AppPreferences.checkSkipped
        val dublicate = mutableListOf<Area>()
        val catProt = mutableListOf<Area>()
        val skipped = HashMap<Area, Int>()
        val zeroNumber = mutableListOf<Area>()
        val lkWithZero = mutableListOf<Area>()
        val map = mutableMapOf<Int, MutableList<Int>>()
        tableData.forEach {
            if (it.categoryProtection == "-") catProt.add(it)
            val intVal = it.categoryArea.toInt()
            if (intVal in 1108..1207 && it.area == 0.0) lkWithZero.add(it)
            if(it.number == 0) zeroNumber.add(it)
            if (!map.containsKey(it.numberKv)) map[it.numberKv] = mutableListOf()
            map[it.numberKv]!!.add(it.number)
        }
        if (map.isNotEmpty()){
            map.forEach {

                val distinctly= it.value.distinct().sorted()
                if (distinctly.size != it.value.size) {
                    val full = it.value.sorted()
                    for (i in 0 until distinctly.size){
                        if (dublicate.any { el -> el.numberKv == it.key }) break
                        if (full[i] != distinctly[i]) dublicate.add(tableData.first { el -> el.numberKv == it.key && el.number == full[i]})
                    }
                }

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
            message += "\nДубликаты в ${dublicate.joinToString { "кв: ${it.numberKv} выд: ${it.number}"}}"
        }
        if (lkWithZero.isNotEmpty()) message += "\nЛк с нулевой площадью в ${lkWithZero.joinToString { "кв: ${it.numberKv} выд: ${it.number}" }}"
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

    fun openFile(file: File){
        tableData.clear()
        var res = false
        menuBar!!.runAsyncWithProgress(progress = progress) {
            try {
                initData(file)
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


}


