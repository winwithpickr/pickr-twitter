plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    `maven-publish`
}

group   = "com.winwithpickr"
version = "0.4.0"

repositories {
    mavenCentral()
    maven {
        name = "GitHubPackages"
        url = uri("https://maven.pkg.github.com/winwithpickr/*")
        credentials {
            username = System.getenv("GITHUB_ACTOR") ?: "winwithpickr"
            password = System.getenv("GITHUB_TOKEN") ?: ""
        }
    }
}

kotlin {
    jvm()
    js(IR) {
        moduleName = "pickr"
        browser {
            webpackTask {
                mainOutputFileName = "pickr-parser.js"
            }
        }
        binaries.executable()
    }

    sourceSets {
        commonMain.dependencies {
            api(libs.pickr.engine)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.coroutines.core)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmTest.dependencies {
            implementation(libs.mockk)
        }
    }
}

tasks.register<Copy>("assembleNpm") {
    group = "build"
    description = "Assemble npm package into build/npm-package/"
    dependsOn("jsBrowserProductionWebpack")
    from(layout.buildDirectory.file("kotlin-webpack/js/productionExecutable/pickr-parser.js")) {
        into("lib")
    }
    from(layout.projectDirectory.dir("packages/npm")) {
        include("package.json")
    }
    into(layout.buildDirectory.dir("npm-package"))
}

publishing {
    publications.withType<MavenPublication> {
        pom {
            name.set("pickr-twitter")
            description.set("Twitter/X integration for the pickr verifiable selection engine")
            url.set("https://github.com/winwithpickr/pickr-twitter")
            licenses {
                license {
                    name.set("MIT License")
                    url.set("https://opensource.org/licenses/MIT")
                }
            }
        }
    }
}
