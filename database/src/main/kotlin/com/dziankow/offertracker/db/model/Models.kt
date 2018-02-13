package com.dziankow.offertracker.db.model

import java.time.LocalDateTime
import javax.persistence.*

@Entity
data class Offer(
        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        val id: Long = 0,
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
)

@Entity
data class Seller(
        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        val id: Long = 0,
        val name: String = "",
        val html: String = ""
)


@Entity
@Table(name = "STUDENT")
data class Student(
        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        @Column(name = "STUDENTID")
        var studentId: Long = 0,
        @Column(name = "STUDENTNAME")
        var studentName: String? = null
)
