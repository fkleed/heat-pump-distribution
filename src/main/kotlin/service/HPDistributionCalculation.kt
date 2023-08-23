package service

import model.Record
import java.math.BigDecimal

interface HPDistributionCalculation {
    fun calculateDistribution(
        buildingStockWithHPPotential: List<Record>,
        hpAmount: BigDecimal,
        shareAshp: BigDecimal,
        shareGshpProbe: BigDecimal,
        shareGshpCollector: BigDecimal
    ): List<Record>
}