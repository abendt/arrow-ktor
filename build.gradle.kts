plugins {
    id("module-conventions")

    id("com.google.devtools.ksp") version "2.0.20-1.0.25"

    kotlin("plugin.serialization") version "2.0.21"

    // https://foso.github.io/Ktorfit/
    id("de.jensklingenberg.ktorfit") version "2.1.0"
}

dependencies {
    implementation(platform("io.arrow-kt:arrow-stack:1.2.4"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

    implementation("io.arrow-kt:arrow-core")
    implementation("io.arrow-kt:arrow-fx-coroutines")
    implementation("io.arrow-kt:arrow-optics")
    implementation("io.arrow-kt:arrow-exact:0.1.0")
    ksp("io.arrow-kt:arrow-optics-ksp-plugin:1.2.4")

    val ktorfitVersion = "2.0.0"

    ksp("de.jensklingenberg.ktorfit:ktorfit-ksp:$ktorfitVersion")

    implementation("de.jensklingenberg.ktorfit:ktorfit-lib:$ktorfitVersion")

    testImplementation("io.kotest.extensions:kotest-assertions-arrow:1.4.0")

    implementation(platform("io.ktor:ktor-bom:2.3.12"))

    testImplementation("io.ktor:ktor-client-content-negotiation")
    testImplementation("io.ktor:ktor-serialization-kotlinx-json")

    testImplementation("io.ktor:ktor-client-logging")

    testImplementation("io.kotest.extensions:kotest-extensions-wiremock:3.1.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-debug:1.9.0")
}

spotless {
    kotlin {
        targetExclude("build/generated/**")
    }
}
