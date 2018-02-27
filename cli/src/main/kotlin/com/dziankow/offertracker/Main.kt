package com.dziankow.offertracker

import com.dziankow.offertracker.config.*
import com.dziankow.offertracker.db.PersistenceCommonModel
//import com.dziankow.offertracker.gratka.GratkaSiteRepo
import org.apache.commons.cli.*
import org.reflections.Reflections
import org.slf4j.LoggerFactory
import java.io.File
import kotlin.reflect.KFunction2
import org.reflections.vfs.Vfs.getFile
import java.net.URLClassLoader




class Main(private val configFile: File) {
    private val config = getConfig(loadConfig(configFile))

    private fun getPersistenceCommonModel() = PersistenceCommonModel(fileName = config.databaseConfigDto.fileName)

    fun listOffers(verbose: Boolean) {
        println("Listing offers...")
        if (verbose) println(config)

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
            println("Loads offers for ${siteRepo.repoName}")
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
    fun getSiteRepoMap(): Map<String, Class<out SiteRepo>> {
        val reflections = Reflections("com.dziankow.offertracker")
        val classes = reflections.getSubTypesOf(SiteRepo::class.java)
        return classes.associateBy { it.getConstructor().newInstance().repoName }
    }

    fun test(verbose: Boolean) {
        println("test")
        if (verbose) println(config)

    }
    fun getConfig(configDto: ConfigDto): Config {
        val siteRepoMap= getSiteRepoMap()
        val siteRepos = ArrayList<SiteRepo>()
        configDto.siteRepos.forEach {
            it.entries.forEach { entry ->
                if (siteRepoMap.containsKey(entry.key)) {
                    val siteRepo = siteRepoMap.get(entry.key)?.getConstructor()?.newInstance() ?:
                        throw IllegalArgumentException("Cannot create class for ${entry.key}")
                    entry.value.keys.forEach {
                        val methodName = "set${it[0].toUpperCase()}${it.subSequence(1, it.length)}"
                        siteRepoMap.get(entry.key)?.getMethod(methodName, String::class.java)?.invoke(siteRepo, entry.value.get(it) ?: "")
                    }
                    siteRepos.add(siteRepo)
                } else {
                    throw IllegalArgumentException("Unknown siteRepo: ${entry.key}")
                }
            }
        }
        return Config(siteRepos, configDto.database)
    }
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
            Main::listOffers),
    TEST("t",
            "test",
            false,
            "Test",
            Main::test);

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
        val main = Main(configFile)

        CliActionOptions.values().filter { cmd.hasOption(it.opt) }.forEach {
            it.action(main, cmd.hasOption(CliOptions.VERBOSE.opt))
        }
    }
}