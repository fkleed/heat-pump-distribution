package service

import model.Record
import java.io.InputStream

interface CSVReader {
    fun readCSV(inputStream: InputStream): List<Record>
}