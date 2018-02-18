package com.dziankow.offertracker.db

import com.dziankow.offertracker.db.model.EntityWithId
import org.slf4j.LoggerFactory
import javax.persistence.EntityManagerFactory
import javax.persistence.Persistence

internal class PersistenceModels(val persistenceUnitName: String = "file", val fileName: String = "") {

    private val logger = LoggerFactory.getLogger(PersistenceModels::class.java)
    private val entityManagerFactory: EntityManagerFactory =
            Persistence.createEntityManagerFactory(persistenceUnitName,
                    if (fileName.length > 0)
                        mapOf(Pair<String, String>("javax.persistence.jdbc.url", "jdbc:h2:$fileName"))//;TRACE_LEVEL_FILE=4;TRACE_LEVEL_SYSTEM_OUT=4"))
                    else
                        null
            )
    private val entityManager = entityManagerFactory.createEntityManager()


    fun <T : EntityWithId> saveEntity(entity: T): T {
        logger.debug("saveEntity: {}", entity)
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
        logger.debug("listEntities (for {})", clazz.name)
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
        logger.debug("deleteEntity with id: {} (for {})", id, clazz.name)
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