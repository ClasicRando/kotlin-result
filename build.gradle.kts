plugins {
    kotlin("jvm") version "1.9.22"
}

group = "com.github.clasicrando"
version = "0.1"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(11)
    compilerOptions.optIn.add("kotlin.contracts.ExperimentalContracts")
}
