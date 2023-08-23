package service

import model.Record
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class HPDistributionCalculationImplTest {

    private val hpDistributionCalculationImpl: HPDistributionCalculation = HPDistributionCalculationImpl()


    @Test
    fun `calculates the correct distribution of heat pumps`() {
        val distributedHPs = hpDistributionCalculationImpl.calculateDistribution(
            records(),
            BigDecimal("6000000"),
            BigDecimal("0.8"),
            BigDecimal("0.15"),
            BigDecimal("0.05"),
        )

        Assertions.assertEquals(distributedHPs.size, 2)
        Assertions.assertEquals(5, distributedHPs[0].hpAmountAir?.toInt())
        Assertions.assertEquals(0, distributedHPs[0].hpAmountProbe?.toInt())
        Assertions.assertEquals(0, distributedHPs[0].hpAmountCollector?.toInt())

        Assertions.assertEquals(0, distributedHPs[1].hpAmountAir?.toInt())
        Assertions.assertEquals(5, distributedHPs[1].hpAmountProbe?.toInt())
        Assertions.assertEquals(0, distributedHPs[1].hpAmountCollector?.toInt())
    }

    @Test
    fun `distributes no heat pumps when hpAmount is zero`() {
        val distributedHPs = hpDistributionCalculationImpl.calculateDistribution(
            records(),
            BigDecimal.ZERO,
            BigDecimal("0.8"),
            BigDecimal("0.15"),
            BigDecimal("0.05"),
        )

        Assertions.assertEquals(distributedHPs.size, 2)
        Assertions.assertEquals(0, distributedHPs[0].hpAmountAir?.toInt())
        Assertions.assertEquals(0, distributedHPs[0].hpAmountProbe?.toInt())
        Assertions.assertEquals(0, distributedHPs[0].hpAmountCollector?.toInt())

        Assertions.assertEquals(0, distributedHPs[1].hpAmountAir?.toInt())
        Assertions.assertEquals(0, distributedHPs[1].hpAmountProbe?.toInt())
        Assertions.assertEquals(0, distributedHPs[1].hpAmountCollector?.toInt())
    }

    @Test
    fun `distributes no heat pumps when heat pump shared is zero`() {
        val distributedHPs = hpDistributionCalculationImpl.calculateDistribution(
            records(),
            BigDecimal("6000000"),
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            BigDecimal.ZERO,
        )

        Assertions.assertEquals(distributedHPs.size, 2)
        Assertions.assertEquals(0, distributedHPs[0].hpAmountAir?.toInt())
        Assertions.assertEquals(0, distributedHPs[0].hpAmountProbe?.toInt())
        Assertions.assertEquals(0, distributedHPs[0].hpAmountCollector?.toInt())

        Assertions.assertEquals(0, distributedHPs[1].hpAmountAir?.toInt())
        Assertions.assertEquals(0, distributedHPs[1].hpAmountProbe?.toInt())
        Assertions.assertEquals(0, distributedHPs[1].hpAmountCollector?.toInt())
    }

    companion object {
        @JvmStatic
        fun records() = listOf(
            Record(
                "123",
                "ABC",
                "ABC",
                "ABC",
                BigDecimal("10.0"),
                BigDecimal("0.5"),
                BigDecimal("0.5"),
                BigDecimal.ZERO,
                BigDecimal.ZERO
            ),
            Record(
                "321",
                "ABC",
                "ABC",
                "ABC",
                BigDecimal("10.0"),
                BigDecimal("0.5"),
                BigDecimal.ZERO,
                BigDecimal("0.5"),
                BigDecimal.ZERO
            ),
        )
    }
}