package com.dziankow.offertracker.config

import com.dziankow.offertracker.utils.getPage
import com.dziankow.offertracker.utils.loadFromFile
import com.dziankow.offertracker.utils.saveFile
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.slf4j.LoggerFactory
import java.io.File
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path

data class Config(val siteRepos: Collection<SiteRepo>, val databaseConfigDto: DatabaseConfigDto)

fun loadConfig(file: File): ConfigDto {
    return loadFromFile(file.toPath(), ConfigDto::class.java)
}

abstract class SiteRepo(val repoName: String,
                        open var baseUrl: URL,
                        open var urlSearchContext: String,
                        val timeout: Int = 30000) {

    private val logger = LoggerFactory.getLogger(SiteRepo::class.java)

    val searchUrl: String
        get() {return "${baseUrl.toExternalForm()}/${urlSearchContext}"}
    val searchFile: String
        get() {return "${dataDir}/search.html"}
    val dataDir: String
        get() {return "data/${repoName}"}

    abstract fun getOfferLinksFromPage(html: String): List<String>
    abstract fun offerFromPage(html: String, offerDir: String): Offer

    fun getOffers(): List<Offer> {
        val offerList = ArrayList<Offer>()
        val searchHtml = getPage(searchUrl, timeout)
        // just for debugging
        saveFile(searchFile, searchHtml)
        for (offerLink in getOfferLinksFromPage(searchHtml)) {
            logger.info("Downloading offer: {}", offerLink)
            val offerName = offerLink.substringAfterLast("/")
            val offerHtml = getPage(offerLink, timeout)
            val offerDir = "$dataDir/$offerName"
            // just for debugging
            saveFile("$offerDir/$offerName", offerHtml)
            val offer = offerFromPage(offerHtml, offerDir)
            offerList.add(offer)
        }
        return offerList;
    }

    fun getOffersWithImages(): List<Offer> {
        val offerList = getOffers()
        offerList.forEach { it.saveImages(timeout) }
        return offerList
    }

    override fun toString(): String {
        return "SiteRepo(name='$repoName', baseUrl=$baseUrl, urlSearchContext='$urlSearchContext', timeout=$timeout)"
    }
}
