import java.io.*
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

class FileExecutor {
    private lateinit var raf: RandomAccessFile
    private var globalOffset = 0L
    private val byteList = ArrayList<Byte>()
    private var counter = 0L


    fun parseFile(file: File): List<SKLArea>{
        raf = RandomAccessFile(file, "r")
        val list = mutableListOf<SKLArea>()
        var counter = 0L
        while (raf.filePointer < file.length()){
            val catProt = readCategoryProtection()
            val number = readNumber().toInt()
            val kvNumber = readNumberKv().toInt()
            val area = BigDecimal((readArea().toFloat() * 0.1)).setScale(1, RoundingMode.HALF_UP).toFloat()
            val catArea = readCategoryArea()
            val lesb = readLesb()
            val oz = readOzu()
            val rawData = readRawData()
            list.add(
                SKLArea(number, kvNumber, area, catArea, catProt.toInt(), oz.toInt(), lesb, rawData)
            )
            //println("list add " + ++counter)
            //nextLine()
        }
        raf.close()
        return list
    }

    private fun nextLine(){
        var byte = 0
        while (byte != 10 && byte != -1) byte = raf.read()
        globalOffset = raf.filePointer
    }

    private fun readCategoryProtection() = readToken(0, 6)
    private fun readNumber() = readToken(29, 3)
    private fun readNumberKv() = readToken(6, 4)
    private fun readLesb() = readToken(13, 4)
    private fun readArea() = readToken(32, 5)
    private fun readOzu(): String{
        val s = readToken(42, 3)
        if (s.startsWith("\n")) return "000"
        return s
    }
    private fun readCategoryArea() = readToken(37, 4)



    private fun readToken(offset: Int, size: Int) : String{
        val arr = ByteArray(size)
        raf.seek(offset + globalOffset)
        raf.read(arr, 0, size)
        return arr.toString(charset)
    }

    private fun readRawData(): RawData{
        raf.seek(10 + globalOffset)
        val admRegion = ByteArray(3)
        raf.read(admRegion)
        raf.seek(17 + globalOffset)
        val data2 = ByteArray(12)
        raf.read(data2)
        raf.seek(41 + globalOffset)
        val tempData3 = raf.read()
        val data3 = if(tempData3 == 13) 48 else tempData3
        val s = readToken(42, 3)
        var data4 : ByteArray? = null
        if (s.startsWith("\n")) {
            globalOffset = raf.filePointer - 2
            return RawData(admRegion, data2, data3, data4)
        }
        raf.seek(45 + globalOffset)
        var byte : Byte = 0
        while (byte != 10.toByte() && raf.filePointer != raf.length()){
            byte = raf.readByte()
            byteList.add(byte)
            //println("byte list add")
        }
        byteList.removeAt(byteList.lastIndex)
        data4 = byteList.toByteArray()
        byteList.clear()
        globalOffset = raf.filePointer
        return RawData(admRegion, data2, data3, data4)
    }

   // private fun writeCategoryProtection() = writeToken()


    fun saveFile(file: File, SKLAreas: List<SKLArea>){
        //raf = RandomAccessFile(file, "r")
        val out = ByteArrayOutputStream()
        SKLAreas.forEach {
            with(out){
                write(it.categoryProtection.toString().toByteArray(charset))
                write(it.numberKv.addZeroes(4))
                write(it.rawData.admRegion)
                write(it.lesb.addZeroes(4))
                write(it.rawData.data2)
                write(it.number.addZeroes(3))
                val area = BigDecimal(it.area.toDouble() * 10).round(MathContext.DECIMAL32).setScale(0).toString()
                write(area.addZeroes(5))
                write(it.categoryArea.toByteArray(charset))
                write(it.rawData.data3)
                write(it.ozu.toString().toByteArray(charset))
                if (it.rawData.data4 != null) {
                    write(it.rawData.data4)
                }
                write(10)
            }

        }
        val outPutStream = FileOutputStream(file)
        out.writeTo(outPutStream)
        outPutStream.close()

    }









}