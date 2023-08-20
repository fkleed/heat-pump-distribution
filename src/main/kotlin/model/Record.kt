package model

import java.math.BigDecimal

data class Record(
    val nuts3Code: String,
    val buildingTypeSize: String,
    val heatingType: String,
    val yearOfConstruction: String,
    val buildingCount: Int,
    val hpPotentialTotal: BigDecimal,
    val hpPotentialAir: BigDecimal,
    val hpPotentialProbe: BigDecimal,
    val hpPotentialCollector: BigDecimal,
    val hpAmountAir: BigDecimal? = null,
    val hpAmountProbe: BigDecimal? = null,
    val hpAmountCollector: BigDecimal? = null
)