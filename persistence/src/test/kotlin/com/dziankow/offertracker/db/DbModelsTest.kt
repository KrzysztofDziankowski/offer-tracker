package com.dziankow.offertracker.db

import com.dziankow.offertracker.db.model.OfferEntity
import com.dziankow.offertracker.db.model.SellerEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.slf4j.LoggerFactory


class DbModelsTest() {

    private val logger = LoggerFactory.getLogger(DbModelsTest::class.java)
    private val persistence = PersistenceModels("test")

    fun getSimpleOfferEntity(offerName: String, sellerName: String): OfferEntity {
        val parameters = HashMap<String, String>()
        parameters.put("key", "value")
        val imageUrlList = arrayListOf<String>("http://link.example.com")
        return OfferEntity(
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
                seller = SellerEntity(name = sellerName, html = "<html>...</html>")
        )
    }

    @Test
    fun offerModelTest() {
        logger.info("Insert offers to DB")
        val offer1 = persistence.saveEntity(getSimpleOfferEntity("offerName1", "sellerName1"))
        val offer2 = persistence.saveEntity(getSimpleOfferEntity("offerName2", "sellerName2"))
        assertNotNull(offer1.id)
        assertNotNull(offer2.id)
        assertEquals(listOf(offer1, offer2), persistence.listEntities(OfferEntity::class.java))

        logger.info("Delete offer from DB")
        persistence.deleteEntity(offer1.id ?: 0, OfferEntity::class.java)
        assertEquals(listOf(offer2), persistence.listEntities(OfferEntity::class.java))

        logger.info("Update offer in DB")
        val offer2Updated = persistence.saveEntity(offer2.copy(name = "offerName1Updated"))
        assertEquals(listOf(offer2), persistence.listEntities(OfferEntity::class.java))
    }
}