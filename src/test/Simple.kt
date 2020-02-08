import java.io.File

fun main() {
    fileTest()
}

fun fileTest(){
    val file = File("${System.getProperty("HOMEPATH")}/baseEdit2.properties")
    print(file.absolutePath)
}