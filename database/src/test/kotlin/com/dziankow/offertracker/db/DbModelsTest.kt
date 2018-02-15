package com.dziankow.offertracker.db

import com.dziankow.offertracker.db.model.Offer
import com.dziankow.offertracker.db.model.Seller
import org.junit.Assert.assertEquals
import org.junit.Test
import org.slf4j.LoggerFactory


class DbModelsTest() {

    private val logger = LoggerFactory.getLogger(DbModelsTest::class.java)
    private val entityManagerUtil: EntityManagerUtil = EntityManagerUtil("test")

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

        logger.info("After Sucessfully insertion ")
        val offer1 = entityManagerUtil.saveEntity(getSimpleOffer("offerName1", "sellerName1"))
        val offer2 = entityManagerUtil.saveEntity(getSimpleOffer("offerName2", "sellerName2"))
        assertEquals(listOf(offer1, offer2), entityManagerUtil.listEntities(Offer::class.java))

        logger.info("After Sucessfully deletion ")
        entityManagerUtil.deleteEntity(offer1.id, Offer::class.java)
        assertEquals(listOf(offer2), entityManagerUtil.listEntities(Offer::class.java))

        logger.info("After Sucessfully modification ")
        offer2.name = "updated"
        entityManagerUtil.saveEntity(offer2)
        assertEquals(listOf(offer2), entityManagerUtil.listEntities(Offer::class.java))
    }
}