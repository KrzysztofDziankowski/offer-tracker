package com.dziankow.offertracker.db

import com.dziankow.offertracker.db.model.OfferEntity
import com.dziankow.offertracker.db.model.SellerEntity
import org.junit.After
import org.junit.Assert.*
import org.junit.Test
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import javax.persistence.NoResultException


class DbModelsTest() {

    private val logger = LoggerFactory.getLogger(DbModelsTest::class.java)
    private val persistence = PersistenceModels("test")

    fun getSimpleOfferEntity(suffix: Int, dateReadFromRepo: LocalDateTime = LocalDateTime.now()): OfferEntity {
        val parameters = HashMap<String, String>()
        parameters.put("key", "value")
        val imageUrlList = arrayListOf<String>("http://link.example.com")
        return OfferEntity(
                name = "offerName-$suffix",
                price = 20000 + suffix,
                repoName = "gratka",
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
                seller = SellerEntity(name = "sellerName-$suffix", html = "<html>..$suffix..</html>")
        )
    }

    @After
    fun cleanDB() {
        persistence.deleteEntries(OfferEntity::class.java)
    }

    @Test
    fun offerModelTest() {
        logger.info("Insert offers to DB")
        val offer1 = persistence.saveEntity(getSimpleOfferEntity(1))
        val offer2 = persistence.saveEntity(getSimpleOfferEntity(2))
        assertNotNull(offer1.persistenceId)
        assertNotNull(offer2.persistenceId)
        assertEquals(listOf(offer1, offer2), persistence.listEntities(OfferEntity::class.java))

        logger.info("Find offer in DB")
        assertEquals(offer1, persistence.getEntity(offer1.persistenceId?: 0, OfferEntity::class.java))
        assertEquals(offer2, persistence.getEntity(offer2.persistenceId?: 0, OfferEntity::class.java))

        logger.info("Delete offer from DB")
        persistence.deleteEntity(offer1.persistenceId ?: 0, OfferEntity::class.java)
        assertEquals(listOf(offer2), persistence.listEntities(OfferEntity::class.java))

        logger.info("Update offer in DB")
        val offer2Updated = persistence.saveEntity(offer2.copy(name = "offerName1Updated"))
        assertEquals(listOf(offer2Updated), persistence.listEntities(OfferEntity::class.java))
    }

    @Test
    fun searchOfferModelTest() {
        logger.info("Insert offers to DB")
        val offer1 = persistence.saveEntity(getSimpleOfferEntity(1, LocalDateTime.now().minusDays(1)))
        val offer2 = persistence.saveEntity(getSimpleOfferEntity(1, LocalDateTime.now().plusDays(1)))
        val offer3 = persistence.saveEntity(getSimpleOfferEntity(1, LocalDateTime.now()))
        assertEquals(listOf(offer1, offer2, offer3), persistence.listEntities(OfferEntity::class.java))

        logger.info("Search offer")
        assertEquals(offer2, persistence.getEntity(offer1.repoId, offer1.externalId, OfferEntity::class.java).get())
    }

    @Test
    fun searchNoResultOfferModelTest() {
        logger.info("Search offer")
        assertFalse(persistence.getEntity("", "", OfferEntity::class.java).isPresent())
    }
}