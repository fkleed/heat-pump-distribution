package model

import java.math.BigDecimal

data class Record(
    val nuts3Code: String,
    val buildingTypeSize: String,
    val yearOfConstruction: String,
    val buildingCount: BigDecimal,
    val hpPotentialTotal: BigDecimal,
    val hpPotentialAir: BigDecimal,
    val hpPotentialProbe: BigDecimal,
    val hpPotentialCollector: BigDecimal,
    var hpAmountAir: BigDecimal? = BigDecimal.ZERO,
    var hpAmountProbe: BigDecimal? = BigDecimal.ZERO,
    var hpAmountCollector: BigDecimal? = BigDecimal.ZERO
)