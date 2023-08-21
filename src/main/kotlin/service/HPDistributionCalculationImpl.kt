package service

import io.github.oshai.kotlinlogging.KotlinLogging
import model.Record
import java.math.BigDecimal
import java.math.RoundingMode

class HPDistributionCalculationImpl(
    val shareAshp: BigDecimal,
    val shareGshpProbe: BigDecimal,
    val shareGshpCollector: BigDecimal
) :
    HPDistributionCalculation {

    private val logger = KotlinLogging.logger {}
    private var hpToDistribute = BigDecimal("6000000")
    override fun calculateDistribution(buildingStockWithHPPotential: List<Record>): List<Record> {
        // Split buildingStockWithHPPotential to perform calculations for different yearOfConstruction
        val buildingStock2023Until2030 =
            buildingStockWithHPPotential.partition { it.yearOfConstruction == "2023 - 2030" }.first
        val buildingStock2012Until2022 =
            buildingStockWithHPPotential.partition { it.yearOfConstruction == "2012 - 2022" }.first
        val buildingStockBeginUntil2011 =
            buildingStockWithHPPotential.partition { it.yearOfConstruction != "2012 - 2022" && it.yearOfConstruction != "2023 - 2030" }.first

        // Perform the different distribution calculations for the different yearOfConstruction
        // Where it is possible heat pumps are used in buildings with year of construction 2023 - 2030
        val buildingStock2023Until2030HPDistribution =
            calculateHPDistributionForBuildingStock2023Until2030(buildingStock2023Until2030)

        hpToDistribute = hpToDistribute.subtract(calculateHPSum(buildingStock2023Until2030HPDistribution))

        // In buildings with year of construction 2012 - 2022 a maximum of 500000 heat pumps are used
        val buildingStock2012Until2022HPDistribution = if (hpToDistribute > BigDecimal("500000")) {
            calculateHPDistributionForBuildingStockDefault(buildingStock2012Until2022, BigDecimal("500000"))
        } else {
            calculateHPDistributionForBuildingStockDefault(buildingStock2012Until2022, hpToDistribute)
        }

        hpToDistribute = hpToDistribute.subtract(calculateHPSum(buildingStock2012Until2022HPDistribution))

        return buildingStockWithHPPotential
    }

    private fun calculateHPDistributionForBuildingStock2023Until2030(buildingStock2023Until2030: List<Record>): List<Record> {
        buildingStock2023Until2030.forEach {
            val hpPotential = it.buildingCount.multiply(it.hpPotentialTotal)

            val recordWithHPDistribution = calculateHPDistributionForRecord(it, hpPotential)

            it.hpAmountAir = recordWithHPDistribution.hpAmountAir
            it.hpAmountProbe = recordWithHPDistribution.hpAmountProbe
            it.hpAmountCollector = recordWithHPDistribution.hpAmountCollector
        }

        return buildingStock2023Until2030
    }

    private fun calculateHPDistributionForBuildingStockDefault(
        buildingStock2012Until2022: List<Record>,
        totalHPAmount: BigDecimal
    ): List<Record> {

        // Calculate the max possible amount of heat pumps to have a reference value
        val maxHpPotential = calculateMaxHPPotential(buildingStock2012Until2022)

        buildingStock2012Until2022.forEach {
            val hpPotential =
                it.buildingCount.multiply(it.hpPotentialTotal).divide(maxHpPotential, 12, RoundingMode.HALF_UP)
                    .multiply(totalHPAmount).min(it.buildingCount.multiply(it.hpPotentialTotal))

            val recordWithHPDistribution = calculateHPDistributionForRecord(it, hpPotential)

            it.hpAmountAir = recordWithHPDistribution.hpAmountAir
            it.hpAmountProbe = recordWithHPDistribution.hpAmountProbe
            it.hpAmountCollector = recordWithHPDistribution.hpAmountCollector
        }

        return buildingStock2012Until2022
    }

    private fun calculateHPDistributionForRecord(record: Record, hpPotential: BigDecimal): Record {
        val maxBuildingCountHPPotentialAir = record.buildingCount.multiply(record.hpPotentialAir)
        val maxBuildingCountHPPotentialProbe = record.buildingCount.multiply(record.hpPotentialProbe)
        val maxBuildingCountHPPotentialCollector = record.buildingCount.multiply(record.hpPotentialCollector)

        var hpAmountAir = if (maxBuildingCountHPPotentialAir > hpPotential.multiply(shareAshp)) {
            hpPotential.multiply(shareAshp)
        } else {
            maxBuildingCountHPPotentialAir
        }

        var hpAmountProbe =
            if (maxBuildingCountHPPotentialProbe > hpPotential.multiply(shareGshpProbe)) {
                hpPotential.multiply(shareGshpProbe)
            } else {
                maxBuildingCountHPPotentialProbe
            }

        var hpAmountCollector =
            if (maxBuildingCountHPPotentialCollector > hpPotential.multiply(shareGshpCollector)) {
                hpPotential.multiply(shareGshpCollector)
            } else {
                maxBuildingCountHPPotentialCollector
            }

        var remaining =
            hpPotential.subtract(hpAmountAir).subtract(hpAmountProbe).subtract(hpAmountCollector)

        if (remaining > BigDecimal.ZERO) {

            if (hpAmountAir < maxBuildingCountHPPotentialAir && hpAmountProbe == maxBuildingCountHPPotentialProbe && hpAmountCollector == maxBuildingCountHPPotentialCollector) {
                hpAmountAir = hpAmountAir.add(remaining.min(maxBuildingCountHPPotentialAir.subtract(hpAmountAir)))
            }

            if (hpAmountAir == maxBuildingCountHPPotentialAir && hpAmountProbe < maxBuildingCountHPPotentialProbe && hpAmountCollector == maxBuildingCountHPPotentialCollector) {
                hpAmountProbe =
                    hpAmountProbe.add(remaining.min(maxBuildingCountHPPotentialProbe.subtract(hpAmountProbe)))
            }

            if (hpAmountAir == maxBuildingCountHPPotentialAir && hpAmountProbe == maxBuildingCountHPPotentialProbe && hpAmountCollector < maxBuildingCountHPPotentialCollector) {
                hpAmountCollector = hpAmountCollector.add(
                    remaining.min(
                        maxBuildingCountHPPotentialCollector.subtract(hpAmountCollector)
                    )
                )
            }

            if (hpAmountAir < maxBuildingCountHPPotentialAir && hpAmountProbe < maxBuildingCountHPPotentialProbe && hpAmountCollector == maxBuildingCountHPPotentialCollector) {

                hpAmountProbe = hpAmountProbe.add(
                    remaining.min(maxBuildingCountHPPotentialProbe.subtract(hpAmountProbe))
                )

                remaining = hpPotential.subtract(hpAmountAir).subtract(hpAmountProbe)
                    .subtract(hpAmountCollector)

                if (remaining > BigDecimal.ZERO) {
                    hpAmountAir =
                        hpAmountAir.add(remaining.min(maxBuildingCountHPPotentialAir.subtract(hpAmountAir)))
                }

            }

            if (hpAmountAir < maxBuildingCountHPPotentialAir && hpAmountProbe == maxBuildingCountHPPotentialProbe && hpAmountCollector < maxBuildingCountHPPotentialCollector) {

                hpAmountCollector = hpAmountCollector.add(
                    remaining.min(maxBuildingCountHPPotentialCollector.subtract(hpAmountCollector))
                )

                remaining = hpPotential.subtract(hpAmountAir).subtract(hpAmountProbe)
                    .subtract(hpAmountCollector)

                if (remaining > BigDecimal.ZERO) {
                    hpAmountAir =
                        hpAmountAir.add(remaining.min(maxBuildingCountHPPotentialAir.subtract(hpAmountAir)))
                }

            }

            if (hpAmountAir == maxBuildingCountHPPotentialAir && hpAmountProbe < maxBuildingCountHPPotentialProbe && hpAmountCollector < maxBuildingCountHPPotentialCollector) {

                hpAmountProbe = hpAmountProbe.add(
                    remaining.multiply(shareGshpProbe.divide(shareGshpProbe.add(shareGshpCollector)))
                        .min(maxBuildingCountHPPotentialProbe.subtract(hpAmountProbe))
                )

                hpAmountCollector = hpAmountCollector.add(
                    remaining.multiply(shareGshpCollector.divide(shareGshpProbe.add(shareGshpCollector)))
                        .min(maxBuildingCountHPPotentialCollector.subtract(hpAmountCollector))
                )

                remaining = hpPotential.subtract(hpAmountAir).subtract(hpAmountProbe)
                    .subtract(hpAmountCollector)

                if (remaining > BigDecimal.ZERO && hpAmountProbe == maxBuildingCountHPPotentialProbe && hpAmountCollector < maxBuildingCountHPPotentialCollector) {
                    hpAmountCollector = hpAmountCollector.add(
                        remaining.min(maxBuildingCountHPPotentialCollector.subtract(hpAmountCollector))
                    )
                }

                if (remaining > BigDecimal.ZERO && hpAmountProbe < maxBuildingCountHPPotentialProbe && hpAmountCollector == maxBuildingCountHPPotentialCollector) {
                    hpAmountProbe = hpAmountProbe.add(
                        remaining.min(maxBuildingCountHPPotentialProbe.subtract(hpAmountProbe))
                    )
                }
            }

            remaining = hpPotential.subtract(hpAmountAir).subtract(hpAmountProbe)
                .subtract(hpAmountCollector)

            if (remaining > BigDecimal.ZERO) {
                logger.info { "There is still a remaining of $remaining that wont be assigned" }
            }

        }

        record.hpAmountAir = hpAmountAir
        record.hpAmountProbe = hpAmountProbe
        record.hpAmountCollector = hpAmountCollector

        return record
    }


    private fun calculateHPSum(buildingStock: List<Record>): BigDecimal {
        var heatPumpSum = BigDecimal.ZERO

        for (record in buildingStock) {
            heatPumpSum = heatPumpSum.add(record.hpAmountAir).add(record.hpAmountProbe).add(record.hpAmountCollector)
        }

        return heatPumpSum
    }

    private fun calculateMaxHPPotential(buildingStock: List<Record>): BigDecimal {
        var heatPumpPotential = BigDecimal.ZERO

        for (record in buildingStock) {
            heatPumpPotential = heatPumpPotential.add(record.buildingCount.multiply(record.hpPotentialTotal))
        }

        return heatPumpPotential
    }
}