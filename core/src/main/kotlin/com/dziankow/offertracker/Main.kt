package com.dziankow.offertracker

import com.dziankow.offertracker.config.Config
import com.dziankow.offertracker.config.ConfigDto
import com.dziankow.offertracker.config.Offer
import com.dziankow.offertracker.config.SiteRepo
import com.dziankow.offertracker.gratka.GratkaSiteRepo
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

fun getConfig(configDto: ConfigDto): Config {
    val siteRepos = ArrayList<SiteRepo>()
    configDto.siteRepos.forEach {
        it.entries.forEach {
            when(it.key) {
                "gratka" -> {
                    val urlSearchContext = it.value.get("urlSearchContext")
                    if (urlSearchContext != null)
                        siteRepos.add(GratkaSiteRepo(urlSearchContext))
                }
                else -> throw IllegalArgumentException("Unknown siteRepo: ${it.key}")
            }
        }
    }
    return Config(siteRepos)
}


fun loadConfig(fileName: String): Config {
    return getConfig(loadFromFile(File(fileName).toPath(), ConfigDto::class.java))
}

fun <T> loadFromFile(path: Path, clazz: Class<T>): T {
    val mapper = ObjectMapper(YAMLFactory()) // Enable YAML parsing
    mapper.registerModule(KotlinModule()) // Enable Kotlin support

    return Files.newBufferedReader(path).use {
        mapper.readValue(it, clazz)
    }
}

fun main(args: Array<String>) {
    val config = loadConfig(ClassLoader.getSystemResource("config.yaml").path)

    println(config)

    val offerList = ArrayList<Offer>()

    for (siteRepo in config.siteRepos) {
        println("Loads offers for ${siteRepo.name}")
        offerList.addAll(siteRepo.getOffersWithImages())
    }

    for (offer in offerList) {
        println(offer)
    }
}
