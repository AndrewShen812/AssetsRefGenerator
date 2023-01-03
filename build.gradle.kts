plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.6.0"
}

version = "1.2.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("net.coobird:thumbnailator:0.4.17")
}
// Configure Gradle IntelliJ Plugin - read more: https://github.com/JetBrains/gradle-intellij-plugin
intellij {
    version.set("2021.3")
    type.set("IC") // Target IDE Platform

    plugins.set(
        listOf(
            //因为要生成 dart 文件，需要使用到 dart 插件中的类，所以这里要引入 dart 插件
            "Dart:213.5744.122", //https://plugins.jetbrains.com/plugin/6351-dart/versions
            "io.flutter:63.2.4",
            "org.jetbrains.plugins.yaml:213.5744.121", //https://plugins.jetbrains.com/plugin/13126-yaml/versions
        )
    )
}

tasks {

    buildSearchableOptions {
        enabled = false
    }

    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "11"
        targetCompatibility = "11"
    }

    patchPluginXml {
        sinceBuild.set("193.5233.102")
    }

    signPlugin {
        certificateChain.set(System.getenv("PLUGIN_CERTIFICATE_CHAIN").trimIndent())
        privateKey.set(System.getenv("PLUGIN_PRIVATE_KEY").trimIndent())
        password.set(System.getenv("PLUGIN_PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PLUGIN_PUBLISH_TOKEN"))
    }
}
