package com.dziankow.offertracker.gratka

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.File
import java.io.FileFilter

internal class GratkaSiteRepoTest {

    val siteRepo = GratkaSiteRepo(baseUrlStr = "https://gratka.pl",
        urlSearchContext = "nieruchomosci/mieszkania?cena-calkowita:min=6000000")

    @Test
    fun `get one offer`() {
        val testFile = "1947452/1947452"
        val offerHtml = File("${siteRepo.dataDir}/$testFile").bufferedReader().use { it.readText() }

        val offer = siteRepo.offerFromPage(offerHtml, "","fake")
        println(offer)
    }

    @Test
    fun `get all offers from data directory`() {
        val htmlList = ArrayList<String>();
        for (file in File(siteRepo.dataDir).listFiles(FileFilter { it.isDirectory })) {
            val offerHtml = File("$file/${file.name}").bufferedReader().use { it.readText() }
            htmlList.add(offerHtml)
        }

        var n = 0
        for (html in htmlList) {
            val offer= siteRepo.offerFromPage(html, "", "fake")
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
    fun `get offer links for page without paging`() {
        val offerSearchHtml = File(siteRepo.searchFile).bufferedReader().use { it.readText() }

        val offerLinks = siteRepo.getOfferLinksFromPage(offerSearchHtml)
        for (offerLink in offerLinks) {
            println(offerLink)
        }

        assertEquals(29, offerLinks.size);
    }

    @Test
    fun `get offer links for page without results`() {
        val offerSearchHtml = File("${siteRepo.dataDir}/search_no_results.html").bufferedReader().use { it.readText() }

        val offerLinks = siteRepo.getOfferLinksFromPage(offerSearchHtml)
        for (offerLink in offerLinks) {
            println(offerLink)
        }

        assertEquals(0, offerLinks.size);
    }

    @Test
    fun `get offer links for first page from search with navigation`() {
        val offerSearchHtml = File("${siteRepo.dataDir}/search_with_pagination.html").bufferedReader().use { it.readText() }

        val offerLinks = siteRepo.getOfferLinksFromPage(offerSearchHtml)
        for (offerLink in offerLinks) {
            println(offerLink)
        }

        assertEquals(32, offerLinks.size);
    }


    @Test
    fun `get nextPage when next page is not available`() {
        val offerSearchHtml = File("${siteRepo.dataDir}/search.html").bufferedReader().use { it.readText() }

        assertFalse(siteRepo.hasNextPage(offerSearchHtml))
    }

    @Test
    fun `get nextPage when search have no results`() {
        val offerSearchHtml = File("${siteRepo.dataDir}/search_no_results.html").bufferedReader().use { it.readText() }

        assertFalse(siteRepo.hasNextPage(offerSearchHtml))
    }

    @Test
    fun `get nextPage when next page is available`() {
        val offerSearchHtml = File("${siteRepo.dataDir}/search_with_pagination.html").bufferedReader().use { it.readText() }

        assertTrue(siteRepo.hasNextPage(offerSearchHtml))
        assertEquals("https://gratka.pl/nieruchomosci/mieszkania?cena-calkowita%3Amin=6000000&device=desktop&page=2",
                siteRepo.getNextPageLink(offerSearchHtml))
    }
}