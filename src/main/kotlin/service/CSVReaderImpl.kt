package service

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import model.Record
import java.io.InputStream
import java.math.BigDecimal

class CSVReaderImpl : CSVReader {
    override fun readCSV(inputStream: InputStream): List<Record> = csvReader().open(inputStream) {
        readAllWithHeaderAsSequence().map {
            Record(
                it["NUTS3Code"]!!.trim(),
                it["BuildingTypeSize"]!!.trim(),
                it["HeatingType"]!!.trim(),
                it["YearOfConstruction"]!!.trim(),
                BigDecimal(it["BuildingCount"]),
                BigDecimal(it["HPPotentialTotal"]),
                BigDecimal(it["HPPotentialAir"]),
                BigDecimal(it["HPPotentialProbe"]),
                BigDecimal(it["HPPotentialCollector"])
            )
        }.toList()
    }
}