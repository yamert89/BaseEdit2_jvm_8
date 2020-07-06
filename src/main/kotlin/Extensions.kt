import javafx.util.StringConverter
import tornadofx.find
import java.lang.NumberFormatException
import java.nio.charset.StandardCharsets
const val PAR_LABEL = "1"

val charset = StandardCharsets.US_ASCII

fun <T> T.addZeroes(size: Int): ByteArray{
    var value = if (this !is String) this.toString() else this
    while (value.length < size) value = "0$value"
    return value.toByteArray(charset)

}




/*
object: StringConverter<Double>() {
    override fun toString(`object`: Double?): String {
        return `object`.toString()
    }

    override fun fromString(string: String?): Double {
        try{
            return string?.replace(",", ".")?.toDouble() ?: 0.0
        }catch (e: NumberFormatException){
            tornadofx.error("Ошибка", "Не удалось преобразовать в число")
        }
        return 0.0
    }*/
