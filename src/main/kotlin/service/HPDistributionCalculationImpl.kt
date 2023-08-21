package service

import model.Record
import java.math.BigDecimal

class HPDistributionCalculationImpl(
    val shareAshp: BigDecimal,
    val shareGshpProbe: BigDecimal,
    val shareGshpCollector: BigDecimal
) :
    HPDistributionCalculation {
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

            val hpAmountAir = if (maxBuildingCountHPPotentialAir > buildingCountWithHPPotential.multiply(shareAshp)) {
                buildingCountWithHPPotential.multiply(shareAshp)
            } else {
                maxBuildingCountHPPotentialAir
            }

            val hpAmountProbe = if (maxBuildingCountHPPotentialProbe > buildingCountWithHPPotential.multiply(shareGshpProbe)) {
                buildingCountWithHPPotential.multiply(shareGshpProbe)
            } else {
                maxBuildingCountHPPotentialProbe
            }

            val hpAmountCollector = if (maxBuildingCountHPPotentialCollector > buildingCountWithHPPotential.multiply(shareGshpCollector)) {
                buildingCountWithHPPotential.multiply(shareGshpCollector)
            } else {
                maxBuildingCountHPPotentialCollector
            }

            val remaining = buildingCountWithHPPotential.subtract(hpAmountAir).subtract(hpAmountProbe).subtract(hpAmountCollector)

            println(remaining)



        }

        return buildingStock2023Until2030
    }
}