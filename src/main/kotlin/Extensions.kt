import java.nio.charset.StandardCharsets

val charset = StandardCharsets.US_ASCII

fun <T> T.addZeroes(size: Int): ByteArray{
    var value = if (this !is String) this.toString() else this
    while (value.length < size) value = "0$value"
    return value.toByteArray(charset)

}