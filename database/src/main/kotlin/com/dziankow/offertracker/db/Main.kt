package com.dziankow.offertracker.db

import com.dziankow.offertracker.db.model.Offer
import com.dziankow.offertracker.db.model.Seller

fun main(args: Array<String>) {
    val parameters = HashMap<String, String>()
    parameters.put("key", "value")
    val imageUrlList = arrayListOf<String>("http://link.example.com")
    val offer = Offer(
            name = "name",
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
            seller = Seller(name = "name", html = "<html>...</html>")
    )

    println("After Sucessfully insertion ")
    val offer1 = saveEntity(offer)
    val offer2 = saveEntity(offer)
    listOffers()

    println("After Sucessfully deletion ")
    deleteOffer(offer1.id)
    listOffers()

    println("After Sucessfully modification ")
    offer2.name = "updated"
    saveEntity(offer2)
    listOffers()
}

val entityManager = getEntityManager()


fun <T> saveEntity(entity: T): T {
    try {
        entityManager.transaction.begin()
        val entityDb: T = entityManager.merge(entity)
        entityManager.transaction.commit()
        return entityDb
    } catch (e: Exception) {
        entityManager.transaction.rollback()
        throw IllegalArgumentException()
    }
}

fun listOffers() {
    try {
        entityManager.transaction.begin()
        val entities = entityManager.createQuery("from Offer").resultList
        val iterator = entities.iterator()
        while (iterator.hasNext()) {
            println(iterator.next())
        }
        entityManager.transaction.commit()
    } catch (e: Exception) {
        entityManager.transaction.rollback()
    }

}

//fun <T> updateEntity(id: Long, entity: T) {
//    try {
//        entityManager.transaction.begin()
//        val student = entityManager.find(Student::class.java, studentId) as Student
//        student.studentName = studentName
//        entityManager.transaction.commit()
//    } catch (e: Exception) {
//        entityManager.transaction.rollback()
//    }
//
//}

fun deleteOffer(id: Long) {
    try {
        entityManager.transaction.begin()
        val offer = entityManager.find(Offer::class.java, id) as Offer
        entityManager.remove(offer)
        entityManager.transaction.commit()
    } catch (e: Exception) {
        entityManager.transaction.rollback()
        throw IllegalArgumentException()
    }
}
