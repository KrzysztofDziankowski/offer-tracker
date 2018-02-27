package com.dziankow.offertracker.gratka

import android.util.Patterns
import com.dziankow.offertracker.config.Offer
import com.dziankow.offertracker.config.Seller
import com.dziankow.offertracker.config.SiteRepo
import org.jsoup.Jsoup
import java.net.URL
import java.util.regex.Pattern

class GratkaSiteRepo(override var urlSearchContext: String = ""): SiteRepo("gratka", URL("http://dom.gratka.pl"), urlSearchContext) {
    override fun offerFromPage(html: String, offerDir: String): Offer {
        val doc = Jsoup.parse(html)

        val sellerHtml = doc.getElementById("dane-kontaktowe").html()
        val sellerName = doc.getElementById("dane-kontaktowe").getElementsByClass("nazwaFirmy").text()

        val seller = Seller(name = sellerName, html = sellerHtml)

        val offerDoc = doc.getElementById("dane-podstawowe")
        val offerDescription = offerDoc.getElementsByClass("opis").first().html()
        val offerHtml = offerDoc.html()
        val offerName = doc.getElementById("karta-naglowek").getElementsByTag("h1").first().text()
        val offerPrice = doc.getElementsByClass("cenaGlowna").first().getElementsByTag("b").first().text().replace(" ", "").toInt()

        val offerParameters = HashMap<String, String>()
        offerDoc.getElementsByClass("label").forEach {
            val valueElement = it.parent().getElementsByClass("wartosc").first()
            if (valueElement != null) {
                offerParameters.put(it.text(), valueElement.text())
            }
        }
        val offerNumber = offerParameters.get("Numer oferty") ?: ""
        val offerArea = (offerParameters.get("Powierzchnia") ?: "").replace(" ", "").replace(",", ".").substringBefore("m").toDouble()

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
        doc.getElementById("zakladki-galeria")?.getElementById("slider")?.getElementsByTag("img")?.forEach {
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
        return doc.getElementsByClass("ogloszenie").map {
            val link = it.getElementsByTag("a").first()
            val href = link.attr("href")
            "${baseUrl.toExternalForm()}${href}"
        }.toList()
    }


}
