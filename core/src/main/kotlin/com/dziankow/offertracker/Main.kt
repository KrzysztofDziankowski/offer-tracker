package com.dziankow.offertracker

import com.dziankow.offertracker.config.*
import com.dziankow.offertracker.db.EntityManagerUtil
import com.dziankow.offertracker.gratka.GratkaSiteRepo
import org.apache.commons.cli.*
import org.slf4j.LoggerFactory
import java.io.File
import kotlin.reflect.KFunction1

class Main(private val config: Config) {
    private val logger = LoggerFactory.getLogger(Main::class.java)

    private fun getEntityManager() = EntityManagerUtil(fileName = config.databaseConfigDto.fileName)
    fun listOffers() {
        logger.info("listOffers")
        logger.info("{}", config)

        getEntityManager()
    }

    fun synchronizeOffers() {
        logger.info("synchronizeOffers")
        logger.info("{}", config)

        val offerList = ArrayList<Offer>()

        for (siteRepo in config.siteRepos) {
            logger.info("Loads offers for {}", siteRepo.name)
            offerList.addAll(siteRepo.getOffersWithImages())
        }

        for (offer in offerList) {
            logger.info("{}", offer)
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
                            val action: KFunction1<Main, Unit>) {
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
            "Configuration file.");

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
            it.action(main)
        }
    }
}