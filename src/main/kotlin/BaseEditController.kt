import javafx.beans.property.SimpleBooleanProperty
import javafx.collections.ObservableList
import javafx.scene.control.*
import javafx.scene.layout.Priority
import roslesinforg.porokhin.areatypes.GeneralTypes
import roslesinforg.porokhin.areatypes.Kv
import roslesinforg.porokhin.areaviews.StrictAreaController
import tornadofx.*
import java.io.File
import java.lang.IllegalArgumentException
import java.text.DecimalFormat
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.Exception
import kotlin.collections.HashMap

class GenController: Controller(), StrictAreaController {
    private val outputSize: Int = 25
    val tableData = emptyList<SKLArea>().toMutableList().asObservable()
    val deletedRows = ArrayDeque<Pair<Int, SKLArea>>()
    var sumAreasForKv :  Map<Int, Double>? = null
    private var filePath = ""
    var fileOpened = SimpleBooleanProperty(false)
    var selected: SKLArea? = null
    var selectedRow: Int = 0
    var selectedCol: TableColumn<SKLArea, *>? = null
    lateinit var tableView: TableView<SKLArea>
    var addButton = Button()
    var delButton = addButton
    var saveButton = addButton
    var menuBar: MenuBar? = null
    var progress = ProgressBar().apply {
        vgrow = Priority.ALWAYS
    }
    private var strictView: BE2StrictView? = null

    override val startSq = mutableListOf<Kv>().toObservable()

    override fun <T : View> setStrictView(view: T) {
        strictView = view as BE2StrictView
    }

    override fun error(text: String) {
        println(text)
        //TODO("Not yet implemented")
    }

    fun addArea(item: SKLArea) {
        tableData.add(selectedRow, item)
        findKv(item.numberKv).areas.add(selectedRow, item.backingArea)
    }

    fun delArea(){
        val item = tableData[selectedRow]
        deletedRows.add(selectedRow to item)
        tableData.removeAt(selectedRow)
        tableView.selectionModel.select(selectedRow + 1, selectedCol)
        findKv(item.numberKv).areas.remove(item.backingArea)
    }

    fun getData(): ObservableList<SKLArea> {
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
    * return array where 0-index - code, 1-index - number of rows //todo replace with Param of GeoBaseEditor
    * */
    fun executeUtil(param1: Pair<Any?, String>, param2: Pair<Any?, String>, resParam: Pair<Any, String>): Array<Int>{
        println("Записей было ${tableData.size}")

        val filteredData = tableData.filter {
            (if(param1.first != DataTypes.EMPTY && param1.first != null) {
                when(param1.first){
                    DataTypes.KV -> it.numberKv == param1.second.toInt()
                    DataTypes.CATEGORY_AREA -> it.categoryArea == param1.second
                    DataTypes.CATEGORY_PROTECTION -> it.categoryProtection.toString() == param1.second
                    DataTypes.OZU -> it.ozu.toString() == param1.second
                    DataTypes.LESB -> it.lesb == param1.second
                    else -> throw IllegalArgumentException("invalid param")
                }
            } else true) &&
                    (if(param2.first != "" && param2.first != null) {
                        when(param2.first){
                            DataTypes.KV -> it.numberKv == param2.second.toInt()
                            DataTypes.CATEGORY_AREA -> it.categoryArea == param2.second
                            DataTypes.CATEGORY_PROTECTION -> it.categoryProtection.toString() == param2.second
                            DataTypes.OZU -> it.ozu.toString() == param2.second
                            DataTypes.LESB -> it.lesb == param2.second
                            else -> throw IllegalArgumentException("invalid param")
                        }
                    } else true)

        }

        println("Изменений ${filteredData}")

        val tempList = HashMap<String, SKLArea>()

        filteredData.forEach { tempList["${it.numberKv}|${it.number}|${it.categoryArea}"] = it }
        if(tempList.isEmpty()) return arrayOf(0)

        try {
            tempList.forEach { item ->
                when(resParam.first){
                    DataTypes.CATEGORY_AREA -> {
                        val intView = resParam.second.toInt()
                        if(resParam.second.length != 4 || (intView < 1101 || intView > 2556)) return arrayOf(-1)
                        item.value.categoryArea = resParam.second
                    }
                    DataTypes.CATEGORY_PROTECTION -> {
                        try {
                            item.value.categoryProtection = GeneralTypes.categoryProtection.entries.find { it.value == resParam.second }!!.key
                        }catch (e: Exception){
                            e.printStackTrace()
                            return arrayOf(-1)
                        }

                    }
                    DataTypes.OZU -> {
                        try {
                            item.value.ozu = GeneralTypes.typesOfProtection.entries.find { it.value == resParam.second }!!.key
                        }catch (e: Exception){
                            e.printStackTrace()
                            return arrayOf(-1)
                        }
                    }
                    DataTypes.LESB -> {
                        if (resParam.second.length != 4) return arrayOf(-1)
                        item.value.lesb = resParam.second
                    }
                }
            }
        }catch (e: Exception){

            return arrayOf(-1)
        }

        val indexedMap = HashMap<Int, SKLArea>()

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
        var truncated = false
        val checkSkip = AppPreferences.checkSkipped
        val checkDuplicate = mutableListOf<SKLArea>()
        val checkCatProt = mutableListOf<SKLArea>()
        val checkSkipped = HashMap<SKLArea, Int>()
        val checkZeroNumber = mutableListOf<SKLArea>()
        val checkLkWithZero = mutableListOf<SKLArea>()
        val checkZeroAreas = mutableListOf<SKLArea>()
        val map = mutableMapOf<Int, MutableList<Int>>()
        tableData.forEach {
            if (it.categoryProtection == 0) checkCatProt.add(it)
            val intVal = it.categoryArea.toInt()
            if (intVal in 1108..1207 && it.area == 0f) checkLkWithZero.add(it)
            if (it.area == 0f && it.categoryProtection != 400000) checkZeroAreas.add(it)
            if(it.number == 0) checkZeroNumber.add(it)
            if (!map.containsKey(it.numberKv)) map[it.numberKv] = mutableListOf()
            map[it.numberKv]!!.add(it.number)
        }
        if (map.isNotEmpty()){
            map.forEach {

                val distinctly= it.value.distinct().sorted()
                if (distinctly.size != it.value.size) {
                    val full = it.value.sorted()
                    for (i in distinctly.indices){
                        if (checkDuplicate.any { el -> el.numberKv == it.key }) break
                        if (full[i] != distinctly[i]) checkDuplicate.add(tableData.first { el -> el.numberKv == it.key && el.number == full[i]})
                    }
                }

                if (!checkSkip) return@forEach
                if(!checkSkipped.contains(tableData.first { el -> el.numberKv == it.key})){
                    val skippedNumber = it.value.containsSkipped()
                    if( skippedNumber != 0) checkSkipped[tableData.first { el -> el.numberKv == it.key}] = skippedNumber
                }

            }
        }
        val sortedCheckSkipped =  TreeMap<SKLArea, Int>().apply {
            if (checkSkipped.size > outputSize){
                truncated = true
                val it = checkSkipped.iterator()
                var idx = 0
                while(it.hasNext() && idx++ < 25) with(it.next()){ put(key, value)}
            }else putAll(checkSkipped)

        }

        fun MutableList<SKLArea>.truncate(): MutableList<SKLArea>{
            if (size > outputSize){
                truncated = true
                return subList(0, outputSize - 1)
            }
            return this
        }

        var message = ""

        if (checkCatProt.size > 0){
            message += "Категория защитности не проставлена в ${checkCatProt.truncate().joinToString(", "){"кв: ${it.numberKv} выд: ${it.number}"}}"
        }
        if (checkZeroNumber.size > 0){
            message += "\nНомер выдела не проставлен в кв ${checkZeroNumber.truncate().joinToString(", "){ it.numberKv.toString()}}"
        }
        if (checkDuplicate.size > 0){
            message += "\nДубликаты в ${checkDuplicate.truncate().joinToString { "кв: ${it.numberKv} выд: ${it.number}"}}"
        }
        if (checkLkWithZero.isNotEmpty()) message += "\nЛк с нулевой площадью в ${checkLkWithZero.truncate().joinToString { "кв: ${it.numberKv} выд: ${it.number}" }}"
        if (sumAreasForKv != null && AppPreferences.checkAreas){
            val resMap = HashMap<Int, Double>()
            val check = calculateAreasForKv()
            check.forEach{
                val diff = it.value - sumAreasForKv!![it.key]!!
                if(diff > 0.08 || diff < -0.08) resMap[it.key] = diff
            }
            if (resMap.isNotEmpty()) message += "\nНе совпадают площади в " + resMap.entries.joinToString { "кв: ${it.key} на ${DecimalFormat("####.#").format(it.value)}" }
            if (checkZeroAreas.isNotEmpty()) message += "\nНулевые площади в ${checkZeroAreas.truncate().joinToString { "\nкв: ${it.numberKv}, выд: ${it.number}" }}"
        }
        if (checkSkip && checkSkipped.isNotEmpty()){
            message += "\nПропущены выдела в ${sortedCheckSkipped.entries.joinToString { "кв ${it.key.numberKv} после выд ${it.value}" }}"
        }

        if (message.isNotBlank()){
            if (truncated) message += "\n..."
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

                tableData.map { it.backingArea }.groupBy{ it.kv }.map { it.key to it.value }.forEach {
                    startSq.add(Kv(it.first, it.second.toMutableList()))
                }
            }catch (e: Exception){
                e.printStackTrace()
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

    fun updateStrictView() = strictView?.update()


}