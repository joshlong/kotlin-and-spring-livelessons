plugins {
    kotlin("multiplatform") version "1.3.61"
}

repositories {
    mavenCentral()
}

kotlin {
    // For ARM, preset function should be changed to iosArm32() or iosArm64()
    // For Linux, preset function should be changed to e.g. linuxX64()
    // For MacOS, preset function should be changed to e.g. macosX64()
    macosX64("CSVParser") {
        binaries {
            // Comment the next section to generate Kotlin/Native library (KLIB) instead of executable file:
            executable("CSVParserApp") {
                // Change to specify fully qualified name of your application's entry point:
                entryPoint = "sample.csvparser.main"
                runTask?.args("./European_Mammals_Red_List_Nov_2009.csv", "4", "100")
            }
        }
    }
}

// Use the following Gradle tasks to run your application:
// :runCSVParserAppReleaseExecutableCSVParser - without debug symbols
// :runCSVParserAppDebugExecutableCSVParser - with debug symbols
