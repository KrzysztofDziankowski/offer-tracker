package com.dziankow.offertracker.db

import com.dziankow.offertracker.config.Offer
import com.dziankow.offertracker.db.model.Mappers
import com.dziankow.offertracker.db.model.OfferEntity
import java.util.*

class PersistenceCommonModel(val persistenceUnitName: String = "file", val fileName: String = ""): AutoCloseable {

    private val persistence = com.dziankow.offertracker.db.PersistenceModels(persistenceUnitName, fileName)
    private val mapper = Mappers()


    fun saveOffer(offer: Offer): Offer {
        return mapper.mapToOffer(persistence.saveEntity(mapper.mapToOfferEntity(offer)))
    }

    fun listOffers(): List<Offer> {
        return persistence.listEntities(OfferEntity::class.java).map { mapper.mapToOffer(it) }
    }

    fun deleteOffer(persistenceId: Long) {
        persistence.deleteEntity(persistenceId, OfferEntity::class.java)
    }
    fun deleteOffers() {
        persistence.deleteEntries(OfferEntity::class.java)
    }

    fun getOffer(persistenceId: Long): Offer {
        return mapper.mapToOffer(persistence.getEntity(persistenceId, OfferEntity::class.java))
    }

    fun getOffer(repoId: String, externalId: String): Optional<Offer> {
        val offerEntityOpt = persistence.getEntity(repoId, externalId, OfferEntity::class.java)
        return if (offerEntityOpt.isPresent) {
            Optional.of(mapper.mapToOffer(offerEntityOpt.get()))
        } else {
            Optional.empty<Offer>()
        }
    }

    override fun close() {
        persistence.close()
    }
}