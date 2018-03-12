package com.dziankow.offertracker.db.model

import com.dziankow.offertracker.config.Offer
import com.dziankow.offertracker.config.Seller


class Mappers {

    fun mapToSeller(sellerEntity: SellerEntity) =
            Seller(
                    persistenceId = sellerEntity.persistenceId,
                    name = sellerEntity.name,
                    html = sellerEntity.html
            )

    fun mapToSellerEntity(seller: Seller) =
            SellerEntity(
                    persistenceId = seller.persistenceId,
                    name = seller.name,
                    html = seller.html
            )

    fun mapToOfferEntity(offer: Offer) =
            OfferEntity(
                    persistenceId = offer.persistenceId,
                    name = offer.name,
                    offerInRepo = offer.offerInRepo,
                    price = offer.price,
                    repoName = offer.repoName,
                    repoId = offer.repoId,
                    repoLink = offer.repoLink,
                    externalId = offer.externalId,
                    area = offer.area,
                    description = offer.description,
                    repoDate = offer.repoDate,
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
                    persistenceId = offer.persistenceId,
                    name = offer.name,
                    offerInRepo = offer.offerInRepo,
                    price = offer.price,
                    repoName = offer.repoName,
                    repoId = offer.repoId,
                    repoLink = offer.repoLink,
                    externalId = offer.externalId,
                    area = offer.area,
                    description = offer.description,
                    repoDate = offer.repoDate,
                    externalLink = offer.externalLink,
                    parameters = offer.parameters ?: HashMap(),
                    imageUrlList = offer.imageUrlList ?: ArrayList(),
                    html = offer.html,
                    galacticaVirgo = offer.galacticaVirgo,
                    offerDir = offer.offerDir,
                    seller = mapToSeller(offer.seller ?: SellerEntity(name = "Unknown"))
            )
}
