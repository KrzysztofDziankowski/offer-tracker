package com.dziankow.offertracker.db.model

import com.dziankow.offertracker.config.Offer
import com.dziankow.offertracker.config.Seller


class Mappers {

    fun mapToSeller(sellerEntity: SellerEntity) =
            Seller(
                    persistenceId = sellerEntity.id,
                    name = sellerEntity.name,
                    html = sellerEntity.html
            )

    fun mapToSellerEntity(seller: Seller) =
            SellerEntity(
                    id = seller.persistenceId,
                    name = seller.name,
                    html = seller.html
            )

    fun mapToOfferEntity(offer: Offer) =
            OfferEntity(
                    id = offer.persistenceId,
                    name = offer.name,
                    price = offer.price,
                    repoId = offer.repoId,
                    externalId = offer.externalId,
                    area = offer.area,
                    description = offer.description,
                    dateReadFromRepo = offer.dateReadFromRepo,
                    externalLink = offer.externalLink,
                    parameters = offer.parameters ?: HashMap(),
                    imageUrlList = offer.imageUrlList ?: ArrayList(),
                    html = offer.html,
                    galacticaVirgo = offer.galacticaVirgo,
                    offerDir = offer.offerDir,
                    seller = mapToSellerEntity(offer.seller)
            )

    fun mapToOffer(offer: OfferEntity) =
            Offer(
                    persistenceId = offer.id,
                    name = offer.name,
                    price = offer.price,
                    repoId = offer.repoId,
                    externalId = offer.externalId,
                    area = offer.area,
                    description = offer.description,
                    dateReadFromRepo = offer.dateReadFromRepo,
                    externalLink = offer.externalLink,
                    parameters = offer.parameters ?: HashMap(),
                    imageUrlList = offer.imageUrlList ?: ArrayList(),
                    html = offer.html,
                    galacticaVirgo = offer.galacticaVirgo,
                    offerDir = offer.offerDir,
                    seller = mapToSeller(offer.seller ?: SellerEntity(name = "Unknown"))
            )
}
