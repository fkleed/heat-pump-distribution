import io.github.oshai.kotlinlogging.KotlinLogging
import service.*
import java.io.FileInputStream
import java.io.FileOutputStream
import java.math.BigDecimal

private val logger = KotlinLogging.logger {}

fun main(args: Array<String>) {
    logger.info { "Starting the application with input file path: ${args[0]}" }

    val csvReader: CSVReader = CSVReaderImpl()

    // Different heat pump shares based on BWP
    val hpDistributionCalculation: HPDistributionCalculation = HPDistributionCalculationImpl(
        shareAshp = BigDecimal("0.8"),
        shareGshpProbe = BigDecimal("0.15"),
        shareGshpCollector = BigDecimal("0.05")
    )

    logger.info { "Calculating the heat pump distribution" }
    val buildingStockWithHPDistribution = hpDistributionCalculation.calculateDistribution(
        csvReader.readCSV(
            FileInputStream(args[0])
        )
    )

    logger.info { "Write results as csv with output file path: ${args[1]}" }
    val csvWriter: CSVWriter = CSVWriterImpl()
    csvWriter.writeCSV(FileOutputStream(args[1]), buildingStockWithHPDistribution)
}