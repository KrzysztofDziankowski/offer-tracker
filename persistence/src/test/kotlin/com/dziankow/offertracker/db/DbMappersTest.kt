package com.dziankow.offertracker.db

import com.dziankow.offertracker.config.Offer
import com.dziankow.offertracker.config.Seller
import org.junit.Assert
import org.junit.Test
import org.slf4j.LoggerFactory

class DbMappersTest() {

    private val logger = LoggerFactory.getLogger(DbModelsTest::class.java)
    private val persistence = PersistenceCommonModel("test")

    fun getSimpleOffer(offerName: String, sellerName: String): Offer {
        val parameters = HashMap<String, String>()
        parameters.put("key", "value")
        val imageUrlList = arrayListOf<String>("http://link.example.com")
        return Offer(
                name = offerName,
                price = 20000,
                repoId = "repoId",
                externalId = "externalId",
                area = 20.0,
                description = "description",
                externalLink = "",
                parameters = parameters,
                imageUrlList = imageUrlList,
                html = "<html>...</html>",
                galacticaVirgo = false,
                offerDir = "dir",
                seller = Seller(name = sellerName, html = "<html>...</html>")
        )
    }

    @Test
    fun offerModelTest() {
        logger.info("Insert offers to DB")
        val offer1 = persistence.saveOffer(getSimpleOffer("offerName1", "sellerName1"))
        val offer2 = persistence.saveOffer(getSimpleOffer("offerName2", "sellerName2"))
        Assert.assertNotNull(offer1.persistenceId)
        Assert.assertNotNull(offer2.persistenceId)
        Assert.assertEquals(listOf(offer1, offer2), persistence.listOffers())

        logger.info("Delete offer from DB")
        persistence.deleteOffer(offer1.persistenceId ?: 0)
        Assert.assertEquals(listOf(offer2), persistence.listOffers())

        logger.info("Update offer in DB")
        val offer2Updated = persistence.saveOffer(offer2.copy(name = "offerName1Updated"))
        persistence.saveOffer(offer2Updated)
        Assert.assertEquals(listOf(offer2Updated), persistence.listOffers())
    }
}