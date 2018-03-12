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
    abstract fun offerFromPage(html: String, repoLink: String, offerDir: String): Offer
    abstract fun hasNextPage(html: String): Boolean
    abstract fun getNextPageLink(html: String): String

    private fun getOfferList(pageHtml: String, fileNameSuffix: String = ""): List<Offer> {
        val offerList = ArrayList<Offer>()
        // just for debugging
        saveFile(searchFile + fileNameSuffix, pageHtml)
        for (offerLink in getOfferLinksFromPage(pageHtml)) {
            logger.info("Downloading offer: {}", offerLink)
            val offerName = offerLink.substringAfterLast("/")
            val offerHtml = getPage(offerLink, timeout)
            val offerDir = "$dataDir/$offerName"
            // just for debugging
            saveFile("$offerDir/$offerName", offerHtml)
            val offer = offerFromPage(offerHtml, offerLink, offerDir)
            offerList.add(offer)
        }
        return offerList
    }

    fun getOffers(): List<Offer> {
        var searchHtml = getPage(searchUrl, timeout)
        val offerList: MutableList<Offer> = ArrayList<Offer>()

        offerList.addAll(getOfferList(searchHtml))
        var i = 0
        while(hasNextPage(searchHtml)) {
            searchHtml = getPage(getNextPageLink(searchHtml), timeout)
            offerList.addAll(getOfferList(searchHtml, "$i"))
            i++
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
