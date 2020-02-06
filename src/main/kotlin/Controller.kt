import javafx.collections.ObservableList
import javafx.scene.control.Alert
import tornadofx.Controller
import tornadofx.alert
import tornadofx.asObservable
import tornadofx.confirm
import java.io.File
import java.lang.IllegalArgumentException
import java.nio.file.Files
import java.nio.file.Paths
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class GenController: Controller() {
    val tableData = emptyList<Area>().toMutableList().asObservable()
    private var filePath = ""
    private val dataTypes = DataTypes()


    fun getData(): ObservableList<Area> {

        return tableData
    }

    fun save(path: String?){
        /*var name = "$filePath.bak"
        while (Files.exists(Paths.get(name))) name+="1"*/
        if (path == null){
            File(filePath).renameTo(File("${filePath}_${LocalTime.now().format(DateTimeFormatter.ofPattern("HH_mm_ss"))}.bak"))
        }else filePath = path
        FileExecutor().saveFile(File(filePath), tableData)
    }

    fun initData(file: File){
        tableData.addAll(FileExecutor().parseFile(file))
        filePath = file.absolutePath
        print("init data done")
    }

    fun executeUtil(param1: Pair<Any?, String>, param2: Pair<Any?, String>, resParam: Pair<Any, String>){
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

        filteredData.forEach {
            when(resParam.first){
                dataTypes.CATEGORY_AREA -> it.categoryArea = resParam.second
                dataTypes.CATEGORY_PROTECTION -> it.categoryProtection = resParam.second
                dataTypes.OZU -> it.ozu = resParam.second
                dataTypes.LESB -> it.lesb = resParam.second
            }
        }
    }

    fun preSaveCheck(): Boolean{
        //tableViewEditModel.commit()
        val dublicate = mutableListOf<Area>()
        val catProt = mutableListOf<Area>()
        val skipped = mutableListOf<Area>()
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
                if(!skipped.contains(tableData.first { el -> el.numberKv == it.key})){
                    if(it.value.containsSkipped()) skipped.add(tableData.first { el -> el.numberKv == it.key})
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
        if (skipped.isNotEmpty()){
            message += "\nПропущены выдела в кв ${skipped.joinToString { it.numberKv.toString() }}"
        }

        if (message.isNotBlank()){
            if (message.startsWith("\nПропущены выдела")) confirm("Сохранить?", content = message){
                return true
            } else alert(Alert.AlertType.ERROR, "Ошибка",  message  )
            return false
        }

        return true
    }

    private fun List<Int>.containsSkipped(): Boolean{

        val sorted = this.sorted()
        var num = sorted.last()
        for(i in (sorted.size - 1) downTo 1){
            if(num - sorted[i-1] != 1) {
                return true
            }
            num = sorted[i-1]
        }
        return false
    }


}


