package com.dziankow.offertracker

import com.dziankow.offertracker.config.*
import com.dziankow.offertracker.db.PersistenceCommonModel
import com.dziankow.offertracker.gratka.GratkaSiteRepo
import org.apache.commons.cli.*
import org.slf4j.LoggerFactory
import java.io.File
import kotlin.reflect.KFunction2


class Main(private val config: Config) {
    private fun getPersistenceCommonModel() = PersistenceCommonModel(fileName = config.databaseConfigDto.fileName)

    fun listOffers(verbose: Boolean) {
        println("Listing offers...")
        if (verbose) println(config)

//        val reflections = Reflections("com.dziankow.offertracker")
//        val classes = reflections.getSubTypesOf(SiteRepo::class.java)
//
//        for (clazz in classes) {
//            println(clazz)
//        }

        val persistence = getPersistenceCommonModel()
        try {
            val offersInDb = persistence.listOffers()
            for (offerWithIndex in offersInDb.sortedBy { it.repoId }.withIndex()) {
                printOffer(offerWithIndex, verbose)
            }
            println("Count of offers in DB: ${offersInDb.size}")
        } finally {
            persistence.close()
        }
    }

    fun synchronizeOffers(verbose: Boolean) {
        println("Synchronizing offers...")
        if (verbose) println(config)

        val offerList = ArrayList<Offer>()

        for (siteRepo in config.siteRepos) {
            println("Loads offers for ${siteRepo.name}")
//            offerList.addAll(siteRepo.getOffersWithImages())
            offerList.addAll(siteRepo.getOffers())
        }

        val persistence = getPersistenceCommonModel()
        var countOfUpdatedOffers = 0
        var countOfNewOffers = 0
        try {
            for (offerWithIndex in offerList.sortedBy { it.repoId }.withIndex()) {
                printOffer(offerWithIndex, verbose)
                val dbOffer = persistence.getOffer(offerWithIndex.value.repoId, offerWithIndex.value.externalId)
                val existsInDb = dbOffer.isPresent
                if (existsInDb) {
                    if (offerWithIndex.value.contentEquals(dbOffer.get())) {
                        continue
                    } else {
                        countOfUpdatedOffers++
                    }
                } else {
                    countOfNewOffers++
                }
                val msg = if (existsInDb) "already exists in DB but with different content" else "new offer"
                println("* Save offer save in DB - $msg")
                persistence.saveOffer(offerWithIndex.value)
            }
        } finally {
            persistence.close()
        }
        println("Count of offers in external repo: ${offerList.size}, old offers updated: ${countOfUpdatedOffers}, new offers: ${countOfNewOffers}")
    }

    private fun printOffer(offerWithIndex: IndexedValue<Offer>, verbose: Boolean) {
        val offer = offerWithIndex.value
        val index = offerWithIndex.index + 1
        if (verbose) {
            println("${index}) $offer")
        } else {
            val externalId = if (offer.externalId.length > 0) " (${offer.externalId})" else ""
            println("${index}) ${offer.repoId}${externalId} - ${offer.name}, ${offer.area}m2, ${offer.price}zł")
        }
    }
}

fun getConfig(configDto: ConfigDto): Config {
    val siteRepos = ArrayList<SiteRepo>()
    configDto.siteRepos.forEach {
        it.entries.forEach {
            when (it.key) {
                "gratka" -> {
                    val urlSearchContext = it.value.get("urlSearchContext")
                    if (urlSearchContext != null)
                        siteRepos.add(GratkaSiteRepo(urlSearchContext))
                }
                else -> throw IllegalArgumentException("Unknown siteRepo: ${it.key}")
            }
        }
    }
    return Config(siteRepos, configDto.database)
}

enum class CliActionOptions(val opt: String,
                            val longOpt: String,
                            val hasArg: Boolean,
                            val description: String,
                            val action: KFunction2<Main, @ParameterName(name = "verbose") Boolean, Unit>) {
    SYNCHRONIZE("s",
            "sync",
            false,
            "Synchronize offers from repos with DB.",
            Main::synchronizeOffers),
    LIST("l",
            "list",
            false,
            "List offers from DB.",
            Main::listOffers);

    fun getOption(): Option {
        return Option(opt, longOpt, hasArg, description)
    }
}

enum class CliOptions(val opt: String,
                      val longOpt: String,
                      val hasArg: Boolean,
                      val description: String) {
    CONFIG_FILE("c",
            "config",
            true,
            "Configuration file."),
    VERBOSE("v",
            "verbose",
            false,
            "Verbose output.");

    fun getOption(): Option {
        return Option(opt, longOpt, hasArg, description)
    }
}

fun main(args: Array<String>) {
    val options = Options()
    CliOptions.values().forEach {
        options.addOption(it.getOption())
    }
    CliActionOptions.values().forEach {
        options.addOption(it.getOption())
    }

    val parser: CommandLineParser = DefaultParser();
    val cmd: CommandLine = parser.parse(options, args);

    val configFile = if (cmd.hasOption(CliOptions.CONFIG_FILE.opt)) {
            File(cmd.getOptionValue(CliOptions.CONFIG_FILE.opt))
        } else {
            File(ClassLoader.getSystemResource("config.yaml").toURI())
        }
    if (!configFile.exists()) {
        println("Can not find config file ${configFile.absolutePath}")
    } else {

        val config = getConfig(loadConfig(configFile))

        val main = Main(config)

        CliActionOptions.values().filter { cmd.hasOption(it.opt) }.forEach {
            it.action(main, cmd.hasOption(CliOptions.VERBOSE.opt))
        }
    }
}