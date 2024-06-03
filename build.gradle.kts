plugins {
    id("module-conventions")

    id("com.google.devtools.ksp") version "1.9.23-1.0.20"

    kotlin("plugin.serialization") version "1.9.23"

    // https://foso.github.io/Ktorfit/
    id("de.jensklingenberg.ktorfit") version "1.13.0"
}

dependencies {
    implementation(platform("io.arrow-kt:arrow-stack:1.2.4"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    implementation("io.arrow-kt:arrow-core")
    implementation("io.arrow-kt:arrow-fx-coroutines")
    implementation("io.arrow-kt:arrow-optics")
    implementation("io.arrow-kt:arrow-exact:0.1.0")
    ksp("io.arrow-kt:arrow-optics-ksp-plugin:1.2.4")

    val ktorfitVersion = "1.13.0"

    ksp("de.jensklingenberg.ktorfit:ktorfit-ksp:$ktorfitVersion")

    implementation("de.jensklingenberg.ktorfit:ktorfit-lib:$ktorfitVersion")

    testImplementation("io.kotest.extensions:kotest-assertions-arrow:1.4.0")

    implementation(platform("io.ktor:ktor-bom:2.3.10"))

    testImplementation("io.ktor:ktor-client-content-negotiation")
    testImplementation("io.ktor:ktor-serialization-kotlinx-json")

    testImplementation("io.ktor:ktor-client-logging")

    testImplementation("io.kotest.extensions:kotest-extensions-wiremock:3.0.1")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-debug:1.8.1")
}

spotless {
    kotlin {
        targetExclude("build/generated/**")
    }
}
