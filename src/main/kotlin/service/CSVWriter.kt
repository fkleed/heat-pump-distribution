package service

import model.Record
import java.io.OutputStream

interface CSVWriter {
    fun writeCSV(outputStream: OutputStream, records: List<Record>)
}