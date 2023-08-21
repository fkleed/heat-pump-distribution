package service

import io.github.oshai.kotlinlogging.KotlinLogging
import model.Record
import java.math.BigDecimal

class HPDistributionCalculationImpl(
    val shareAshp: BigDecimal,
    val shareGshpProbe: BigDecimal,
    val shareGshpCollector: BigDecimal
) :
    HPDistributionCalculation {

    private val logger = KotlinLogging.logger {}
    private val hpToDistribute = BigDecimal("6000000")
    override fun calculateDistribution(buildingStockWithHPPotential: List<Record>): List<Record> {
        // Split buildingStockWithHPPotential to perform calculations for different yearOfConstruction
        val buildingStock2023Until2030 =
            buildingStockWithHPPotential.partition { it.yearOfConstruction == "2023 - 2030" }.first
        val buildingStock2012Until2022 =
            buildingStockWithHPPotential.partition { it.yearOfConstruction == "2012 - 2022" }.first
        val buildingStockBeginUntil2011 =
            buildingStockWithHPPotential.partition { it.yearOfConstruction != "2012 - 2022" && it.yearOfConstruction != "2023 - 2030" }.first

        // Perform the different distribution calculations for the different yearOfConstruction
        val buildingStock2023Until2030HPDistribution =
            calculateHPDistributionForBuildingStock2023Until2030(buildingStock2023Until2030)

        return buildingStockWithHPPotential
    }

    private fun calculateHPDistributionForBuildingStock2023Until2030(buildingStock2023Until2030: List<Record>): List<Record> {
        buildingStock2023Until2030.forEach {
            val buildingCountWithHPPotential = it.buildingCount.multiply(it.hpPotentialTotal)
            val maxBuildingCountHPPotentialAir = it.buildingCount.multiply(it.hpPotentialAir)
            val maxBuildingCountHPPotentialProbe = it.buildingCount.multiply(it.hpPotentialProbe)
            val maxBuildingCountHPPotentialCollector = it.buildingCount.multiply(it.hpPotentialCollector)

            var hpAmountAir = if (maxBuildingCountHPPotentialAir > buildingCountWithHPPotential.multiply(shareAshp)) {
                buildingCountWithHPPotential.multiply(shareAshp)
            } else {
                maxBuildingCountHPPotentialAir
            }

            var hpAmountProbe =
                if (maxBuildingCountHPPotentialProbe > buildingCountWithHPPotential.multiply(shareGshpProbe)) {
                    buildingCountWithHPPotential.multiply(shareGshpProbe)
                } else {
                    maxBuildingCountHPPotentialProbe
                }

            var hpAmountCollector =
                if (maxBuildingCountHPPotentialCollector > buildingCountWithHPPotential.multiply(shareGshpCollector)) {
                    buildingCountWithHPPotential.multiply(shareGshpCollector)
                } else {
                    maxBuildingCountHPPotentialCollector
                }

            var remaining =
                buildingCountWithHPPotential.subtract(hpAmountAir).subtract(hpAmountProbe).subtract(hpAmountCollector)

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

                    remaining = buildingCountWithHPPotential.subtract(hpAmountAir).subtract(hpAmountProbe)
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

                    remaining = buildingCountWithHPPotential.subtract(hpAmountAir).subtract(hpAmountProbe)
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

                    remaining = buildingCountWithHPPotential.subtract(hpAmountAir).subtract(hpAmountProbe)
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

                remaining = buildingCountWithHPPotential.subtract(hpAmountAir).subtract(hpAmountProbe)
                    .subtract(hpAmountCollector)

                if (remaining > BigDecimal.ZERO) {
                    logger.info { "There is still a remaining of $remaining that wont be assigned" }
                }

            }

            it.hpAmountAir = hpAmountAir
            it.hpAmountProbe = hpAmountProbe
            it.hpAmountCollector = hpAmountCollector
        }

        return buildingStock2023Until2030
    }
}