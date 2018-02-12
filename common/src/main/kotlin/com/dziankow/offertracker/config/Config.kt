package com.dziankow.offertracker.config

import com.dziankow.offertracker.utils.getPage
import com.dziankow.offertracker.utils.saveFile
import java.net.URL

data class Config(val siteRepos: Collection<SiteRepo>)

abstract class SiteRepo(val name: String,
                        val baseUrl: URL,
                        val urlSearchContext: String,
                        val timeout: Int = 30000) {

    val searchUrl: String
        get() {return "${baseUrl.toExternalForm()}/${urlSearchContext}"}
    val searchFile: String
        get() {return "${dataDir}/search.html"}
    val dataDir: String
        get() {return "data/${name}"}

    abstract fun getOfferLinksFromPage(html: String): List<String>
    abstract fun offerFromPage(html: String, offerDir: String): Offer

    fun getOffers(): List<Offer> {
        val offerList = ArrayList<Offer>()
        val searchHtml = getPage(searchUrl, timeout)
        // just for debugging
        saveFile(searchFile, searchHtml)
        for (offerLink in getOfferLinksFromPage(searchHtml)) {
            println(offerLink)
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
        return "SiteRepo(name='$name', baseUrl=$baseUrl, urlSearchContext='$urlSearchContext', timeout=$timeout)"
    }
}
