package com.dziankow.offertracker.db

import com.dziankow.offertracker.db.model.EntityWithId
import javax.persistence.EntityManagerFactory
import javax.persistence.Persistence

class EntityManagerUtil (val persistenceUnitName: String = "file") {

    private val entityManagerFactory: EntityManagerFactory =
            Persistence.createEntityManagerFactory(persistenceUnitName)
    private val entityManager = entityManagerFactory.createEntityManager()


    fun <T : EntityWithId> saveEntity(entity: T): T {
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

    fun <T : EntityWithId> listEntities(clazz: Class<T>): List<T> {
        try {
            entityManager.transaction.begin()
            return entityManager.createQuery("from ${clazz.name}").resultList as List<T>
        } catch (e: Exception) {
            entityManager.transaction.rollback()
            throw e
        } finally {
            if (entityManager.transaction.isActive) {
                entityManager.transaction.commit()
            }
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

    fun <T : EntityWithId> deleteEntity(id: Long, clazz: Class<T>) {
        try {
            entityManager.transaction.begin()
            val offer = entityManager.find(clazz, id) as T
            entityManager.remove(offer)
            entityManager.transaction.commit()
        } catch (e: Exception) {
            entityManager.transaction.rollback()
            throw e
        }
    }

}