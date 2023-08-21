package service

import model.Record

interface HPDistributionCalculation {
    fun calculateDistribution(buildingStockWithHPPotential: List<Record>): List<Record>
}