package com.dziankow.offertracker.gratka

import android.util.Patterns
import com.dziankow.offertracker.config.Offer
import com.dziankow.offertracker.config.Seller
import com.dziankow.offertracker.config.SiteRepo
import com.dziankow.offertracker.config.exception.ElementNotFoundException
import org.jsoup.Jsoup
import java.net.URL
import java.util.regex.Pattern

class GratkaSiteRepo(override var urlSearchContext: String = ""): SiteRepo("gratka", URL("http://dom.gratka.pl"), urlSearchContext) {
    override fun hasNextPage(html: String): Boolean {
        val doc = Jsoup.parse(html)
        return doc?.getElementsByClass("stronaNastepna")?.first() != null
    }

    override fun getNextPageLink(html: String): String {
        val doc = Jsoup.parse(html)
        val href = doc?.getElementsByClass("stronaNastepna")?.first()
                ?.getElementsByTag("a")?.first()?.attr("href")
                ?: throw ElementNotFoundException()
        val rssLink = doc?.getElementsByTag("link")
                ?.attr("rel", "alternate")
                ?.eachAttr("href")
                ?.filter { it.startsWith(baseUrl.toExternalForm()) && it.contains("/rss/") }
                ?.first()
                ?: throw ElementNotFoundException()
        val urlContext = rssLink.subSequence(baseUrl.toExternalForm().length, rssLink.indexOf("/rss/") + 1)
        return "${baseUrl.toExternalForm()}${urlContext}${href}"
    }

    override fun offerFromPage(html: String, offerDir: String): Offer {
        val doc = Jsoup.parse(html)

        val sellerHtml = doc?.getElementById("dane-kontaktowe")?.html()
                ?: throw ElementNotFoundException()
        val sellerName = doc?.getElementById("dane-kontaktowe")
                ?.getElementsByClass("nazwaFirmy")?.text()
                ?: throw ElementNotFoundException()

        val seller = Seller(name = sellerName, html = sellerHtml)

        val offerDoc = doc?.getElementById("dane-podstawowe")
                ?: throw ElementNotFoundException()
        val offerDescription = offerDoc.getElementsByClass("opis").first()?.html()
                ?: throw ElementNotFoundException()
        val offerHtml = offerDoc.html()
                ?: throw ElementNotFoundException()
        val offerName = doc.getElementById("karta-naglowek").getElementsByTag("h1").first()?.text()
                ?: throw ElementNotFoundException()
        val offerPrice = doc.getElementsByClass("cenaGlowna")?.first()
                ?.getElementsByTag("b")?.first()?.text()?.replace(" ", "")?.toInt()
                ?: throw ElementNotFoundException()

        val offerParameters = HashMap<String, String>()
        offerDoc.getElementsByClass("label").forEach {
            val valueElement = it.parent().getElementsByClass("wartosc").first()
            if (valueElement != null) {
                offerParameters.put(it.text(), valueElement.text())
            }
        }
        val offerNumber = offerParameters.get("Numer oferty")
                ?: throw ElementNotFoundException()
        val offerArea = (offerParameters.get("Powierzchnia") ?: throw ElementNotFoundException())
                .replace(" ", "")
                .replace(",", ".")
                .substringBefore("m").toDouble()

        var offerExternalLink = ""
        val externalLinkIdx = offerDescription.lastIndexOf("::LINK DO STRONY")
        if (externalLinkIdx > 0) {
            val matcher = Patterns.WEB_URL.matcher(offerDescription.substring(externalLinkIdx))
            if (matcher.find()) {
                offerExternalLink = matcher.group()
            }
        }

        val galacticaVirgo = offerDescription.indexOf("Galactica Virgo") != 0

        val offerExternalId = when(sellerName) {
            "Biuro Zamiany i Sprzedaży Nieruchomości-Twój Dom" -> {
                val pattern = Pattern.compile("oferta w biurze pod nr:([A-Za-z/0-9]*)")
                val matcher = pattern.matcher(offerDescription)
                if (matcher.find()) {
                    matcher.group(1)
                } else {
                    ""
                }
            }
            "Filar Nieruchomości" -> {
                val pattern = Pattern.compile("Indeks oferty : ([0-9]*)")
                val matcher = pattern.matcher(offerDescription)
                if (matcher.find()) {
                    matcher.group(1)
                } else {
                    ""
                }
            }
            else -> if (offerExternalLink.length > 0) {
                val pattern = Pattern.compile("repoId=([0-9]*)")
                val matcher = pattern.matcher(offerDescription)
                if (matcher.find()) {
                    matcher.group(1)
                } else {
                    ""
                }
            } else {
                ""
            }
        }

        val imageUrlList = ArrayList<String>()
        doc?.getElementById("zakladki-galeria")?.getElementById("slider")?.getElementsByTag("img")?.forEach {
            val imgLink = it.attr("rel")
            if (imgLink.length > 0) {
                imageUrlList.add(imgLink)
            } else {
                val imgSrcLink= it.attr("src")
                if (imgSrcLink.length > 0) {
                    imageUrlList.add(imgSrcLink)
                }
            }
        }

        val offer = Offer(
                name = offerName,
                price = offerPrice,
                repoName = repoName,
                repoId = offerNumber,
                externalId = offerExternalId,
                description = offerDescription,
                parameters = offerParameters,
                externalLink = offerExternalLink,
                area = offerArea,
                galacticaVirgo = galacticaVirgo,
                html = offerHtml,
                imageUrlList = imageUrlList,
                offerDir = offerDir,
                seller = seller)
        return offer
    }

    override fun getOfferLinksFromPage(html: String): List<String> {
        val doc = Jsoup.parse(html)
        return doc?.getElementsByClass("ogloszenie")?.map {
            val link = it.getElementsByTag("a").first()
            val href = link.attr("href")
            "${baseUrl.toExternalForm()}${href}"
        }?.toList() ?: throw ElementNotFoundException()
    }
}
