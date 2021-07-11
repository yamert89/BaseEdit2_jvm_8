import javafx.beans.property.*
import roslesinforg.porokhin.areatypes.Area
import roslesinforg.porokhin.areatypes.fields.Field1
import tornadofx.*

class SKLArea(number: Int, numberKv: Int, area: Float, categoryArea: String, categoryProtection: String, ozu: String, lesb: String, val rawData: RawData) : Comparable<SKLArea>{
    val backingArea = Area(kv = numberKv, categoryProtection = categoryProtection.toInt(), column5 = lesb, field1 = Field1(
        number, area, categoryArea.toInt(), 0, ozu.toInt()
    ))

    var numberProperty = SimpleIntegerProperty(this, "number", number)
    var number by numberProperty

    var numberKvProperty = SimpleIntegerProperty(this, "numberKv", numberKv)
    var numberKv by numberKvProperty

    var areaProperty = SimpleFloatProperty(this, "area", area)
    var area by areaProperty

    var categoryAreaProperty = SimpleStringProperty(this, "categoryArea", categoryArea)
    var categoryArea by categoryAreaProperty

    var categoryProtectionProperty = SimpleStringProperty(this, "categoryProtection", categoryProtection)
    var categoryProtection by categoryProtectionProperty

    var ozuProperty = SimpleStringProperty(this, "ozu", ozu)
    var ozu by ozuProperty

    var lesbProperty = SpecStringProperty(this, "lesb", lesb){
        val intV = lesb.toInt()
        lesb.length > 4 || intV < 0 || intV > 9999}
    var lesb by lesbProperty

    override fun compareTo(other: SKLArea): Int {
        return let { it.numberKv * 1000 + it.number }.compareTo(other.let { it.numberKv * 1000 + it.number })
    }

}

class RawData(val admRegion: ByteArray, val data2: ByteArray, val data3: Int, val data4: ByteArray?)

class SpecStringProperty(bean: Any, name: String, value: String, val condition: () -> Boolean) : SimpleStringProperty(bean, name, value){
    override fun setValue(v: String?) {
        if (condition()) {
            throw IllegalStateException("Произошла попытка присвоить недопустимое значение")
        }
        super.setValue(v)
    }
}






