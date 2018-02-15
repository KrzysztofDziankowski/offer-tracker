package com.dziankow.offertracker.db.model

import java.time.LocalDateTime
import javax.persistence.*

interface EntityWithId {
        val id: Long
}

@Entity
data class Offer(
        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        override val id: Long = 0,
        var name: String = "",
        val price: Int = 0,
        val repoId: String = "",
        val externalId: String = "",
        val area: Double = 0.0,
        val description: String = "",
        val dateReadFromRepo: LocalDateTime = LocalDateTime.now(),
        val externalLink: String = "",
        @ElementCollection
        val parameters: Map<String, String>? = null,
        @ElementCollection
        val imageUrlList: List<String>? = null,
        val html: String = "",
        val galacticaVirgo: Boolean = false,
        val offerDir: String = "",
        @OneToOne(cascade = arrayOf(CascadeType.ALL))
        val seller: Seller? = null
): EntityWithId

@Entity
data class Seller(
        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        override val id: Long = 0,
        val name: String = "",
        val html: String = ""
): EntityWithId
