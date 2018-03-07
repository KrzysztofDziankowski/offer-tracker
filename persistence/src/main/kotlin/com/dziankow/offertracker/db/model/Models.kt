package com.dziankow.offertracker.db.model

import java.time.LocalDateTime
import javax.persistence.*

interface EntityWithId {
        val persistenceId: Long?
}

@Entity
data class OfferEntity(
        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        override val persistenceId: Long? = null,
        val name: String = "",
        val offerInRepo: Boolean = false,
        val price: Int = 0,
        val repoName: String = "",
        val repoId: String = "",
        val externalId: String = "",
        val area: Double = 0.0,
        @Lob
        val description: String = "",
        val dateReadFromRepo: LocalDateTime = LocalDateTime.now(),
        val externalLink: String = "",
        @ElementCollection
        val parameters: Map<String, String>? = null,
        @ElementCollection
        val imageUrlList: List<String>? = null,
        @Lob
        val html: String = "",
        val galacticaVirgo: Boolean = false,
        val offerDir: String = "",
        @OneToOne(cascade = arrayOf(CascadeType.ALL))
        val seller: SellerEntity? = null
): EntityWithId

@Entity
data class SellerEntity(
        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        override val persistenceId: Long? = null,
        val name: String = "",
        @Lob
        val html: String = ""
): EntityWithId
