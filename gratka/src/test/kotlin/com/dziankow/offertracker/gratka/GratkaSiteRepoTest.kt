package com.dziankow.offertracker.gratka

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.File
import java.io.FileFilter

internal class GratkaSiteRepoTest {

    val siteRepo = GratkaSiteRepo("mieszkania-sprzedam/lista/kujawsko-pomorskie,,4000000,co.html")

    @Before
    fun initialize() {

    }

    @Test
    fun getOffer() {
        val testFile = "397-73180823-kujawskopomorskie-bydgoszcz-centrum-grottgera.html/397-73180823-kujawskopomorskie-bydgoszcz-centrum-grottgera.html"
        val offerHtml = File("${siteRepo.dataDir}/$testFile").bufferedReader().use { it.readText() }

        val offer = siteRepo.offerFromPage(offerHtml, "fake")
        println(offer)
    }

    @Test
    fun getOffers() {
        val htmlList = ArrayList<String>();
        for (file in File(siteRepo.dataDir).listFiles(FileFilter { it.isDirectory })) {
            val offerHtml = File("$file/${file.name}").bufferedReader().use { it.readText() }
            htmlList.add(offerHtml)
        }

        var n = 0
        for (html in htmlList) {
            val offer= siteRepo.offerFromPage(html, "fake")
            if (offer.externalId.length == 0) {
                println(offer)
                println(offer.description)
                println()
                n++
            }
        }
        println(n)
    }

    @Test
    fun getOfferLinks() {
        val offerSearchHtml = File(siteRepo.searchFile).bufferedReader().use { it.readText() }

        val offerLinks = siteRepo.getOfferLinksFromPage(offerSearchHtml)
        for (offerLink in offerLinks) {
            println(offerLink)
        }

        assertEquals(5, offerLinks.size);
    }

    @Test
    fun getOfferLinks2() {
        val offerSearchHtml = File("${siteRepo.dataDir}/search_with_pagination.html").bufferedReader().use { it.readText() }

        val offerLinks = siteRepo.getOfferLinksFromPage(offerSearchHtml)
        for (offerLink in offerLinks) {
            println(offerLink)
        }

        assertEquals(40, offerLinks.size);
    }


    @Test
    fun getNextPage() {
        val offerSearchHtml = File("${siteRepo.dataDir}/search.html").bufferedReader().use { it.readText() }

        assertFalse(siteRepo.hasNextPage(offerSearchHtml))
    }
    @Test
    fun getNextPage2() {
        val offerSearchHtml = File("${siteRepo.dataDir}/search_with_pagination.html").bufferedReader().use { it.readText() }

        assertTrue(siteRepo.hasNextPage(offerSearchHtml))
        assertEquals("http://dom.gratka.pl/mieszkania-sprzedam/lista/kujawsko-pomorskie,,40,2,li,s.html",
                siteRepo.getNextPageLink(offerSearchHtml, "/mieszkania-sprzedam/lista/"))
    }
}