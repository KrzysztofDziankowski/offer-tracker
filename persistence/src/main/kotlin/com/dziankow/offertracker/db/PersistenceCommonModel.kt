package com.dziankow.offertracker.db

import com.dziankow.offertracker.config.Offer
import com.dziankow.offertracker.db.model.Mappers
import com.dziankow.offertracker.db.model.OfferEntity

class PersistenceCommonModel(val persistenceUnitName: String = "file", val fileName: String = "") {

    private val persistence = com.dziankow.offertracker.db.Persistence(persistenceUnitName, fileName)
    private val mapper = Mappers()


    fun saveOffer(offer: Offer): Offer {
        return mapper.mapToOffer(persistence.saveEntity(mapper.mapToOfferEntity(offer)))
    }

    fun listOffers(): List<Offer> {
        return persistence.listEntities(OfferEntity::class.java).map { mapper.mapToOffer(it) }
    }

    fun deleteOffer(id: Long) {
        persistence.deleteEntity(id, OfferEntity::class.java)
    }
}