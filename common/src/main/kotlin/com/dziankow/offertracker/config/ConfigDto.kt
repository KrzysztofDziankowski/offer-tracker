package com.dziankow.offertracker.config

data class ConfigDto(val siteRepos: List<Map<String, Map<String, String>>>,
                     val database: DatabaseConfigDto)

data class DatabaseConfigDto(val fileName: String)
