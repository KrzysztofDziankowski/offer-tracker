package com.dziankow.offertracker.config

import com.dziankow.offertracker.utils.getAndSaveImage
import jdk.nashorn.internal.codegen.OptimisticTypesPersistence
import java.time.LocalDateTime

interface ContentComparable<T> {
    fun contentEquals(other: T): Boolean
}

data class Offer(
        val persistenceId: Long? = null,
        val name: String,
        val offerInRepo: Boolean,
        val price: Int,
        val repoName: String,
        val repoId: String,
        val externalId: String,
        val area: Double,
        val description: String,
        val dateReadFromRepo: LocalDateTime = LocalDateTime.now(),
        val externalLink: String,
        val parameters: Map<String, String>,
        val imageUrlList: List<String>,
        val html: String,
        val galacticaVirgo: Boolean,
        val offerDir: String,
        val seller: Seller

): ContentComparable<Offer> {
    override fun contentEquals(other: Offer): Boolean {
        return name == other.name
                && price == other.price
                && repoId == other.repoId
                && repoName == other.repoName
                && externalId == other.externalId
                && area == area
                && description == other.description
                && externalLink == other.externalLink
//                && imageUrlList == other.imageUrlList
                && html == other.html
                && seller.contentEquals(other.seller)
    }

    override fun toString(): String {
        return "Offer(" +
                "persistenceId='$persistenceId', " +
                "name='$name', " +
                "price=${price}zł, " +
                "repoId='$repoId', " +
                "repoName='$repoName'" +
                "externalId='$externalId', " +
                "area=${area}m2, " +
                "description='${description.substring(0, Math.min(description.length, 10))}...', " +
                "dateReadFromRepo= $dateReadFromRepo, " +
                "externalLink=$externalLink, " +
                "imageUrlList=$imageUrlList, " +
                "galacticaVirgo=$galacticaVirgo, " +
                "offerDir=$offerDir, " +
                "parameters=$parameters, seller=$seller)"
    }

    fun saveImages(timeout: Int) {
        for (imageUrl in imageUrlList) {
            println(imageUrl)
            getAndSaveImage(imageUrl, offerDir, timeout)
        }

    }
}

data class Seller(
        val persistenceId: Long? = null,
        val name: String,
        val html: String
): ContentComparable<Seller> {
    override fun contentEquals(other: Seller): Boolean {
        return name == other.name
    }

    override fun toString(): String {
        return "Seller(persistenceId=$persistenceId, name='$name')"
    }
}