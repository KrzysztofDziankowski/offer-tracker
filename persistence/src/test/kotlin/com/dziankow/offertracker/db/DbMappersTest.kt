package com.dziankow.offertracker.db

import com.dziankow.offertracker.config.Offer
import com.dziankow.offertracker.config.Seller
import org.junit.After
import org.junit.Assert
import org.junit.Test
import org.slf4j.LoggerFactory
import java.time.LocalDateTime

class DbMappersTest() {

    private val logger = LoggerFactory.getLogger(DbModelsTest::class.java)
    private val persistence = PersistenceCommonModel("test")

    fun getSimpleOffer(suffix: Int, dateReadFromRepo: LocalDateTime = LocalDateTime.now()): Offer {
        val parameters = HashMap<String, String>()
        parameters.put("key", "value")
        val imageUrlList = arrayListOf<String>("http://link.example.com")
        return Offer(
                name = "offerName-$suffix",
                price = 20000 + suffix,
                repoId = "repoId-$suffix",
                externalId = "externalId-$suffix",
                area = 20.0,
                dateReadFromRepo = dateReadFromRepo,
                description = "description-$suffix",
                externalLink = "",
                parameters = parameters,
                imageUrlList = imageUrlList,
                html = "<html>..$suffix..</html>",
                galacticaVirgo = false,
                offerDir = "dir",
                seller = Seller(name = "sellerName-$suffix", html = "<html>..$suffix..</html>")
        )
    }

    @After
    fun cleanDB() {
        persistence.deleteOffers()
    }

    @Test
    fun offerModelTest() {
        logger.info("Insert offers to DB")
        val offer1 = persistence.saveOffer(getSimpleOffer(1))
        val offer2 = persistence.saveOffer(getSimpleOffer(2))
        Assert.assertNotNull(offer1.persistenceId)
        Assert.assertNotNull(offer2.persistenceId)
        Assert.assertEquals(listOf(offer1, offer2), persistence.listOffers())


        logger.info("Find offer in DB")
        Assert.assertEquals(offer1, persistence.getOffer(offer1.persistenceId ?: 0))
        Assert.assertEquals(offer2, persistence.getOffer(offer2.persistenceId ?: 0))

        logger.info("Delete offer from DB")
        persistence.deleteOffer(offer1.persistenceId ?: 0)
        Assert.assertEquals(listOf(offer2), persistence.listOffers())

        logger.info("Update offer in DB")
        val offer2Updated = persistence.saveOffer(offer2.copy(name = "offerName1Updated"))
        persistence.saveOffer(offer2Updated)
        Assert.assertEquals(listOf(offer2Updated), persistence.listOffers())
    }

    @Test
    fun searchOfferModelTest() {
        logger.info("Insert offers to DB")
        val offer1 = persistence.saveOffer(getSimpleOffer(1, LocalDateTime.now().minusDays(1)))
        val offer2 = persistence.saveOffer(getSimpleOffer(1, LocalDateTime.now().plusDays(1)))
        val offer3 = persistence.saveOffer(getSimpleOffer(1, LocalDateTime.now()))
        Assert.assertEquals(listOf(offer1, offer2, offer3), persistence.listOffers())

        logger.info("Search offer")
        Assert.assertEquals(offer2, persistence.getOffer(offer1.repoId, offer1.externalId).get())
    }

    @Test
    fun searchNoResultOfferModelTest() {
        logger.info("Search offer")
        Assert.assertFalse(persistence.getOffer("", "").isPresent())
    }
}