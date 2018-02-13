package com.dziankow.offertracker.config

import com.dziankow.offertracker.utils.getAndSaveImage
import java.time.LocalDateTime

data class Offer(
        val name: String,
        val price: Int,
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

) {
    override fun toString(): String {
        return "Offer(name='$name', " +
                "price=${price}zł, " +
                "repoId='$repoId', " +
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
        val name: String,
        val html: String
) {
    override fun toString(): String {
        return "Seller(name='$name')"
    }
}