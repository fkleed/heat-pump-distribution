import io.github.oshai.kotlinlogging.KotlinLogging
import service.CSVReader
import service.CSVReaderImpl
import service.HPDistributionCalculation
import service.HPDistributionCalculationImpl
import java.io.FileInputStream
import java.math.BigDecimal

private val logger = KotlinLogging.logger {}

fun main(args: Array<String>) {
    logger.info { "Starting the application with file path ${args[0]}" }

    val csvReader: CSVReader = CSVReaderImpl()

    // Different heat pump shares based on BWP
    val hpDistributionCalculation: HPDistributionCalculation = HPDistributionCalculationImpl(
        shareAshp = BigDecimal("0.8"),
        shareGshpProbe = BigDecimal("0.15"),
        shareGshpCollector = BigDecimal("0.05")
    )

    hpDistributionCalculation.calculateDistribution(
        csvReader.readCSV(
            FileInputStream(args[0])
        )
    )
}