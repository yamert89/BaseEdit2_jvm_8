import org.gradle.internal.impldep.com.google.common.io.Files
import org.jetbrains.kotlin.cli.common.toBooleanLenient
import kotlin.io.*
import java.util.Properties
import java.io.*
plugins {
    java
    kotlin("jvm") version "1.4.20"
}

group = "BaseEdit2"
version = "1.5.0"
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
    implementation("no.tornado:tornadofx:1.7.20")
    implementation(project(":areatypes2"))
    implementation(project(":areaviews2"))
    implementation(project(":areaselector"))
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

sourceSets{

}
project.configurations.implementation.isCanBeResolved = true
var archieveName = ""

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
        buildVersion = if(version != props.getProperty("appv")) 0 else props.getProperty("v").toInt() + 1
        props.setProperty("v", buildVersion.toString())
        props.setProperty("appv", version.toString())
        val writer = FileWriter(file("$projectDir/buildVersion"))
        props.store(writer, "")
        writer.close()
    }
    jar{
        dependsOn(getByName("changeBuildVersion"))
        archieveName = "BaseEdit2-${project.version}.jar"
        manifest {
            attributes( "Main-Class" to "BaseEdit2AppKt")
        }
        archiveFileName.set(archieveName)
        val c = configurations.implementation.get().files.map{ if(it.isDirectory) it else zipTree(it)}
        val d = project.dependencies
        from(c)
    }
    fun String.prop() = System.getProperty(this)
    val workPlaceConfiguration = "workplaceConfiguration".prop().toBooleanLenient()!!
    var startPath = if (workPlaceConfiguration) {
        "pathJarsWorkplace".prop()
    } else {
        "pathJarsHome".prop()
    }
    startPath = "${startPath}BaseEdit2_main_jar"
    val startFolder = file("${startPath}BaseEdit2_main_jar\\")
    val buildD = "$projectDir/build/libs/"

    val cleanStartDir = register("cleanStartDir"){
        dependsOn(jar)
        startFolder.listFiles()?.filter { it.name.endsWith(".jar") }?.forEach { delete(it.absolutePath) }
    }
    val copyLists = register<Copy>("copyLists"){
        dependsOn(cleanStartDir)
        from(fileTree("${project.parent!!.projectDir}/BaseEdit2_jvm_8/src/main/resources/lists").filter { it.name == "categoryprotectionlong.yml" || it.name == "typesofprotectionlong.yml" })
        into(file("$startPath/lists"))
    }
    val copyB = register<Copy>("copyBuild"){
        dependsOn(cleanStartDir)
        from(file("$buildD/start.cmd"))
        into(startFolder)
    }

    register<Copy>("copy") {
        dependsOn(copyB, copyLists)
        val archieveName = "BaseEdit2-${project.version}.jar"
        val startF = file("$projectDir/build/libs/start.cmd")
        startF.delete()
        startF.createNewFile()
        val writer = FileWriter(startF)
        writer.write("start /B javaw -jar $archieveName")
        writer.flush()
        writer.close()
        println("$buildD/$archieveName")
        from(file("$buildD/$archieveName"))
        into(startFolder)


    }


}