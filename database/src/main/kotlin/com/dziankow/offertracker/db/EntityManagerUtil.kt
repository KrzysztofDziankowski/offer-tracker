package com.dziankow.offertracker.db

import javax.persistence.EntityManagerFactory
import javax.persistence.Persistence


val entityManagerFactory: EntityManagerFactory = Persistence.createEntityManagerFactory("file")

fun getEntityManager() = entityManagerFactory.createEntityManager()
