plugins {
    java
    kotlin("jvm") version "1.3.61"
}

group = "BaseEdit2"
version = "1.1"

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
tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    jar{
        manifest {
            attributes( "Main-Class" to "BaseEdit2AppKt")
        }
        archiveFileName.set("BaseEdit2-${project.version}.jar")
        val c = configurations.implementation.get().files.map{ if(it.isDirectory) it else zipTree(it)}
        val d = project.dependencies
        from(c)


        //from(configurations.compile.get().fileCollection().map{ if(it.isDirectory) it else zipTree(it)})
    }

}