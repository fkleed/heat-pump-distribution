import io.github.oshai.kotlinlogging.KotlinLogging
import service.CSVReader
import service.CSVReaderImpl
import java.io.FileInputStream

private val logger = KotlinLogging.logger {}

fun main(args: Array<String>) {
    logger.info { "Starting the application with file path ${args[0]}" }

    val csvReader: CSVReader = CSVReaderImpl()
    val buildingStockWithHPPotential = csvReader.readCSV(
        FileInputStream(args[0])
    )

    logger.info { buildingStockWithHPPotential.first() }
}