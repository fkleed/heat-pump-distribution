package service

import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import model.Record
import java.io.OutputStream

class CSVWriterImpl : CSVWriter {
    override fun writeCSV(outputStream: OutputStream, records: List<Record>) = csvWriter().open(outputStream) {
        writeRow(
            "NUTS3Code",
            "BuildingTypeSize",
            "YearOfConstruction",
            "BuildingCount",
            "HPPotentialTotal",
            "HPPotentialAir",
            "HPPotentialProbe",
            "HPPotentialCollector",
            "HPAmountAir",
            "HPAmountProbe",
            "HPAmountCollector"
        )

        for (record in records) {
            writeRow(
                record.nuts3Code,
                record.buildingTypeSize,
                record.yearOfConstruction,
                record.buildingCount,
                record.hpPotentialTotal,
                record.hpPotentialAir,
                record.hpPotentialProbe,
                record.hpPotentialCollector,
                record.hpAmountAir,
                record.hpAmountProbe,
                record.hpAmountCollector
            )
        }
    }
}