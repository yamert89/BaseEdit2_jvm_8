plugins {
    java
    kotlin("jvm") version "1.3.61"
}

group = "BaseEdit2"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
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
tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    jar{
        manifest {
            attributes( "Main-Class: BaseEdit2AppKt")
        }
        from(sourceSets.names)
        //from(configurations.compile.fileCollection().map { if(it.isDirectory) it else zipTree(it) })
        //from(configurations.compile.get().fileCollection().map{ if(it.isDirectory) it else zipTree(it)})
    }
    register("Post"){

    }
}