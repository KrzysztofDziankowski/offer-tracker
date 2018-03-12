package com.dziankow.offertracker.gratka

import android.util.Patterns
import com.dziankow.offertracker.config.Offer
import com.dziankow.offertracker.config.Seller
import com.dziankow.offertracker.config.SiteRepo
import com.dziankow.offertracker.config.exception.ElementNotFoundException
import org.jsoup.Jsoup
import java.net.URL
import java.util.regex.Pattern

class GratkaSiteRepo(override var urlSearchContext: String = "", var baseUrlStr: String = "https://gratka.pl"):
        SiteRepo("gratka", URL(baseUrlStr), urlSearchContext) {

    private val offerIdPattern = Pattern.compile("offerId: ([0-9]*)")
    private val companyPattern = Pattern.compile("\"company\": \"([^\"]*)\"")

    override fun hasNextPage(html: String): Boolean {
        val doc = Jsoup.parse(html)
        return doc?.getElementsByClass("pagination__nextPage")?.first() != null
    }

    override fun getNextPageLink(html: String): String {
        val doc = Jsoup.parse(html)
        val href = doc?.getElementsByClass("pagination__nextPage")?.first()
                ?.getElementsByTag("a")?.first()?.attr("href")
                ?: throw ElementNotFoundException()
        return "${baseUrl.toExternalForm()}${href}"
    }

    override fun offerFromPage(html: String, repoLink: String, offerDir: String): Offer {
        val doc = Jsoup.parse(html)

        val sellerHtml = doc?.getElementById("contact-container")?.html()
                ?: throw ElementNotFoundException()
        val sellerName = doc?.getElementById("leftColumn")?.select("script")
                ?.map { val matcher = companyPattern.matcher(it.html())
                    if (matcher.find()) {
                        matcher.group(1)
                    } else {
                        null
                    }
                }?.filter { it != null }?.firstOrNull() ?: "UNKNOWN"

        val seller = Seller(name = sellerName, html = sellerHtml)

        val offerDoc = doc?.getElementById("rightColumn")
                ?: throw ElementNotFoundException()
        val offerDescription = doc.getElementsByClass("description__container").first()?.html()
                ?: throw ElementNotFoundException()
        val offerHtml = offerDoc?.html()
                ?: throw ElementNotFoundException()
        val offerName = offerDoc?.getElementsByClass("sticker__title")?.first()?.text()
                ?: throw ElementNotFoundException()
        val offerPriceTmp = offerDoc.getElementsByClass("sticker__value")?.first()
                ?.text()
                ?.replace(" ", "")
                ?: throw ElementNotFoundException()
        val offerPrice = offerPriceTmp.substring(0, offerPriceTmp.indexOf(",")).toInt()

        val offerParameters = offerDoc?.getElementsByClass("parameters__container")?.first()
                ?.getElementsByClass("parameters__rolled")?.first()
                ?.getElementsByTag("li")
                ?.filter{
                    val valueHasText = it.getElementsByClass("parameters__value")?.first()?.hasText() ?: false
                    val keyHasText = it.children()?.first()?.hasText() ?: false
                    valueHasText && keyHasText
                }?.map {
            val valueText = it.getElementsByClass("parameters__value")?.first()?.text() ?: ""
            val keyText = it.children()?.first()?.text() ?: ""
            keyText to valueText
        }?.toMap() ?: throw ElementNotFoundException()


        val offerNumber = doc?.getElementById("leftColumn")?.select("script")
                ?.map { val matcher = offerIdPattern.matcher(it.html())
                    if (matcher.find()) {
                        matcher.group(1)
                    } else {
                        null
                    }
                }?.filter { it != null }?.first() ?: throw ElementNotFoundException()
        val offerArea = (offerParameters.get("Powierzchnia w m2") ?: throw ElementNotFoundException())
                .toDouble()

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
                offerInRepo = true,
                price = offerPrice,
                repoName = repoName,
                repoId = offerNumber,
                repoLink = repoLink,
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
        return doc?.getElementById("leftColumn")?.getElementsByClass("teaser")?.map {
            val href = it.attr("href")
            "${href}"
        }?.toList() ?: ArrayList()
    }
}
