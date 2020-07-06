import org.gradle.internal.impldep.com.google.common.io.Files
import kotlin.io.*
import java.util.Properties
import java.io.*
plugins {
    java
    kotlin("jvm") version "1.3.61"
}

group = "BaseEdit2"
version = "1.4.3"
var buildVersion = 0


repositories {
    mavenCentral()
}
java{
    sourceCompatibility = org.gradle.api.JavaVersion.VERSION_1_8
    targetCompatibility = org.gradle.api.JavaVersion.VERSION_1_8
}


dependencies {
    implementation(kotlin("stdlib-jdk8"))
    testCompile("junit", "junit", "4.12")
    implementation("no.tornado:tornadofx:1.7.19")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

sourceSets{

}
project.configurations.implementation.isCanBeResolved = true
var archieveName = ""
val startFolder = file("C:\\Users\\porohin\\Desktop\\BaseEdit2_main_jar\\")
tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    register("changeBuildVersion"){
        outputs.upToDateWhen{false}
        dependsOn(compileKotlin)
        val props = Properties()
        val reader = FileReader(file("$projectDir/buildVersion"))
        props.load(reader)
        reader.close()
        buildVersion = props.getProperty("v").toInt() + 1
        props.setProperty("v", buildVersion.toString())
        val writer = FileWriter(file("$projectDir/buildVersion"))
        props.store(writer, "")
        writer.close()
    }
    jar{
        dependsOn(getByName("changeBuildVersion"))
        archieveName = "BaseEdit2-${project.version}.b${buildVersion}.jar"
        manifest {
            attributes( "Main-Class" to "BaseEdit2AppKt")
        }
        archiveFileName.set(archieveName)
        val c = configurations.implementation.get().files.map{ if(it.isDirectory) it else zipTree(it)}
        val d = project.dependencies
        from(c)
    }

    register("cleanStartDir"){
        dependsOn(jar)
        startFolder.listFiles()?.filter { it.name.endsWith(".jar") }?.forEach { delete(it.absolutePath) }
    }

    register<Copy>("copy") {
        dependsOn(getByName("cleanStartDir"))
        val archieveName = "BaseEdit2-${project.version}.b${buildVersion}.jar"
        val startF = file("$projectDir/build/libs/start.cmd")
        startF.delete()
        startF.createNewFile()
        val writer = FileWriter(startF)
        writer.write("start /B javaw -jar $archieveName")
        writer.flush()
        writer.close()
        val buildD = "$projectDir/build/libs/"
        from(file("$buildD/$archieveName"))
        into(startFolder)
        from(file("$buildD/start.cmd"))
        into(startFolder)

    }


}