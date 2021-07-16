import org.junit.Assert
import org.junit.Test

class Ariphmetics {


    @Test
    fun d(){
        var d1 = 2.1
        var d2 = 2.2
        var diff = d1 - d2
        Assert.assertEquals(true, diff > 0.08 || diff < -0.08)
        d1 = 159.2
        d2 = 159.5
        diff = d1 - d2
        Assert.assertEquals(true, diff > 0.08 || diff < -0.08)
        d1 = 17.3
        d2 = 17.3
        diff = d1 - d2
        Assert.assertEquals(false, diff > 0.08 || diff < -0.08)
        d1 = 133.1
        d2 = 133.2
        diff = d1 - d2
        Assert.assertEquals(true, diff > 0.08 || diff < -0.08)
        d1 = 189.5
        d2 = 189.5
        diff = d1 - d2
        Assert.assertEquals(false, diff > 0.08 || diff < -0.08)
        d1 = 111.9
        d2 = 115.9
        diff = d1 - d2
        Assert.assertEquals(true, diff > 0.08 || diff < -0.08)
        d1 = 2.1
        d2 = 2.1
        diff = d1 - d2
        Assert.assertEquals(false, diff > 0.08 || diff < -0.08)
        d1 = 117.1
        d2 = 117.0
        diff = d1 - d2
        Assert.assertEquals(true, diff > 0.08 || diff < -0.08)
    }
}