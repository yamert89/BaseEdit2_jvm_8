import javafx.beans.property.*
import roslesinforg.porokhin.areatypes.Area
import roslesinforg.porokhin.areatypes.fields.Field1
import tornadofx.*

class SKLArea(number: Int, numberKv: Int, area: Float, categoryArea: String, categoryProtection: Int, ozu: Int, lesb: String, val rawData: RawData) : Comparable<SKLArea>{
    val backingArea = Area(kv = numberKv, categoryProtection = categoryProtection.toInt(), column5 = lesb, field1 = Field1(
        number, area, categoryArea.toInt(), 0, ozu
    ))

    var numberProperty = SimpleIntegerProperty(this, "number", number)
    var number by numberProperty

    var numberKvProperty = SimpleIntegerProperty(this, "numberKv", numberKv)
    var numberKv by numberKvProperty

    var areaProperty = SimpleFloatProperty(this, "area", area)
    var area by areaProperty

    var categoryAreaProperty = SimpleStringProperty(this, "categoryArea", categoryArea)
    var categoryArea by categoryAreaProperty

    var categoryProtectionProperty = SimpleIntegerProperty(this, "categoryProtection", categoryProtection) as Property<Int>
    var categoryProtection by categoryProtectionProperty

    var ozuProperty = SimpleIntegerProperty(this, "ozu", ozu) as Property<Int>
    var ozu by ozuProperty

    var lesbProperty = SpecStringProperty(this, "lesb", lesb){
        val intV = lesb.toInt()
        lesb.length > 4 || intV < 0 || intV > 9999}
    var lesb by lesbProperty

    init {
        numberProperty.onChange { backingArea.field1.number = it }
        areaProperty.onChange { backingArea.field1.area = it }
        categoryProtectionProperty.onChange { backingArea.categoryProtection = it!! }
        ozuProperty.onChange { backingArea.field1.typeOfProtection = it!! }
        lesbProperty.onChange { backingArea.column5 = it!! }
    }

    override fun compareTo(other: SKLArea): Int {
        return let { it.numberKv * 1000 + it.number }.compareTo(other.let { it.numberKv * 1000 + it.number })
    }

    override fun toString(): String {
        return backingArea.id.toString()
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






