import javafx.beans.property.BooleanProperty
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleBooleanProperty
import tornadofx.getProperty
import tornadofx.objectProperty
import tornadofx.property
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.util.*
import kotlin.reflect.KProperty

object AppPreferences {
    private val properties = Properties()
    private val file = File("${System.getProperty("user.home")}/baseEdit2.properties")

    val checkSkippedProperty = MyBooleanProperty()
    var checkSkipped by checkSkippedProperty

    val saveBackupsProperty = MyBooleanProperty(true)
    var saveBackups by saveBackupsProperty

    val checkAreasProperty = MyBooleanProperty()
    var checkAreas by checkAreasProperty



    init {
        println("pref loaded")

        if (file.exists()) {
            with(properties){
                load(FileReader(file))
                checkSkipped = getProperty("chSkipped") == "1"
                saveBackups = getProperty("saveBack") == "1"
                checkAreas = getProperty("checkAreas") == "1"
            }

        } else file.createNewFile()


    }


    fun savePreferences(){
        properties["chSkipped"] = if (checkSkipped) "1" else "0"
        properties["saveBack"] = if (saveBackups) "1" else "0"
        properties["checkAreas"] = if (checkAreas) "1" else "0"
        properties.store(FileWriter(file), "")
    }
}

class MyBooleanProperty(initialValue: Boolean = false) : SimpleBooleanProperty(initialValue){ //todo check

    operator fun getValue(appPreferences: AppPreferences, property: KProperty<*>): Boolean {
        return super.get()
    }

    operator fun setValue(appPreferences: AppPreferences, property: KProperty<*>, any: Boolean) {
        super.setValue(any)
    }

}