package com.dziankow.offertracker.db

import com.dziankow.offertracker.db.model.EntityWithId
import com.dziankow.offertracker.db.model.OfferEntity
import org.slf4j.LoggerFactory
import java.util.*
import javax.persistence.EntityManagerFactory
import javax.persistence.NamedQuery
import javax.persistence.NoResultException
import javax.persistence.Persistence

internal class PersistenceModels(val persistenceUnitName: String = "file", val fileName: String = ""): AutoCloseable {

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

    fun <T : EntityWithId> deleteEntity(persistenceId: Long, clazz: Class<T>) {
        logger.debug("deleteEntity with persistenceId: {} (for {})", persistenceId, clazz.name)
        try {
            entityManager.transaction.begin()
            val offer = entityManager.find(clazz, persistenceId) as T
            entityManager.remove(offer)
            entityManager.transaction.commit()
        } catch (e: Exception) {
            entityManager.transaction.rollback()
            throw e
        }
    }

    fun <T : EntityWithId> deleteEntries(clazz: Class<T>) {
        logger.debug("deleteEntitries (for {})", clazz.name)
        try {
            entityManager.transaction.begin()
            entityManager.createQuery("DELETE FROM ${clazz.name}")
            entityManager.transaction.commit()
        } catch (e: Exception) {
            entityManager.transaction.rollback()
            throw e
        }
    }

    fun <T : EntityWithId> getEntity(persistenceId: Long, clazz: Class<T>): T {
        try {
            entityManager.transaction.begin()
            return entityManager.find(clazz, persistenceId) as T
        } catch (e: Exception) {
            entityManager.transaction.rollback()
            throw e
        } finally {
            if (entityManager.transaction.isActive) {
                entityManager.transaction.commit()
            }
        }
    }

    fun <T : EntityWithId> getEntity(repoId: String, externalId: String, clazz: Class<T>): Optional<T> {
        try {
            entityManager.transaction.begin()
            return Optional.of(entityManager.createQuery(
                    "SELECT o FROM ${clazz.name} o WHERE o.repoId = :repoId AND o.externalId = :externalId ORDER BY o.dateReadFromRepo DESC", clazz)
                    .setParameter("repoId", repoId)
                    .setParameter("externalId", externalId)
                    .setMaxResults(1)
                    .singleResult)
        } catch (e: NoResultException) {
            return Optional.empty()
        } catch (e: Exception) {
            entityManager.transaction.rollback()
            throw e
        } finally {
            if (entityManager.transaction.isActive) {
                entityManager.transaction.commit()
            }
        }
    }

    override fun close() {
        entityManager.close()
        entityManagerFactory.close()
    }
}