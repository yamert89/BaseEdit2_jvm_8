import javafx.beans.property.*
import tornadofx.*

class Area(number: Int, numberKv: Int, area: Double, categoryArea: String, categoryProtection: String, ozu: String, lesb: String, val rawData: RawData) {
    var numberProperty = SimpleIntegerProperty(this, "number", number)
    var number by numberProperty

    var numberKvProperty = SimpleIntegerProperty(this, "numberKv", numberKv)
    var numberKv by numberKvProperty

    var areaProperty = SimpleDoubleProperty(this, "area", area)
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





}

class AreaModel: ItemViewModel<Area>(){
    val number = bind(Area::numberProperty)
    val numberKv = bind(Area::numberKvProperty) as IntegerProperty
    val area = bind(Area::areaProperty) as DoubleProperty
    val categoryArea = bind(Area::categoryAreaProperty) as StringProperty
    val categoryProtection = bind(Area::categoryProtectionProperty) as StringProperty
    val ozu = bind(Area::ozuProperty) as StringProperty
    val lesb = bind(Area::lesbProperty) as StringProperty

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






