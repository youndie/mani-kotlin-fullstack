import kotlinx.datetime.*
import ru.workinprogress.feature.transaction.*
import kotlin.test.Test
import kotlin.test.assertEquals

class TransactionOperationsTest {

    @Test
    fun testCreateDates() {
        val results = createDates(LocalDate(2000, 1, 1), LocalDate(2000, 1, 4))
        assertEquals(3, results.size)
    }

    @Test
    fun testDefaultPeriod() {
        val results = listOf(
            Transaction(
                "",
                100.0,
                true,
                LocalDate(2000, 1, 1),
                null,
                Transaction.Period.OneTime,
                "Start"
            )
        ).defaultPeriod()

        assertEquals(
            (Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.monthNumber + 3) % 12,
            results.second.monthNumber
        )

        assertEquals(
            2000,
            results.first.year
        )

        assertEquals(
            2025,
            results.second.year
        )
    }

    @Test
    fun testSimulateSimple() {
        val result = listOf(
            Transaction(
                "",
                100.0,
                true,
                LocalDate(2000, 1, 1),
                null,
                Transaction.Period.OneTime,
                "Start"
            ),
            Transaction(
                "",
                100.0,
                false,
                LocalDate(2000, 1, 5),
                null,
                Transaction.Period.OneTime,
                "Start"
            ),
            Transaction(
                "TAG",
                1000.0,
                true,
                LocalDate(2000, 1, 1),
                LocalDate(2000, 1, 17),
                Transaction.Period.Week,
                "3 Times 1000"
            ),
        ).simulate(LocalDate(2000, 1, 1) to LocalDate(2000, 3, 1))

        assertEquals(5, result.flatMap { it.value }.size)
        assertEquals(1000.0, result[LocalDate(2000, 1, 8)]?.firstOrNull()?.amount)
        assertEquals(3000.0, result.flatMap { it.value }.sumOf { transaction -> transaction.amountSigned })
    }

    @Test
    fun testSimulateRepeats() {
        val result = listOf(
            Transaction(
                "",
                1000.0,
                true,
                LocalDate(2000, 1, 1),
                null,
                Transaction.Period.Month,
                "3 Times 1000"
            ),
        ).simulate(LocalDate(2000, 1, 1) to LocalDate(2000, 3, 1))

        assertEquals(3, result.flatMap { it.value }.size)
        assertEquals(result[LocalDate(2000, 1, 1)]?.firstOrNull()?.amount, 1000.0)
        assertEquals(result[LocalDate(2000, 2, 1)]?.firstOrNull()?.amount, 1000.0)
        assertEquals(result[LocalDate(2000, 3, 1)]?.firstOrNull()?.amount, 1000.0)
    }

    @Test
    fun testChartSimple() {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

        val result = listOf(
            Transaction(
                "",
                100.0,
                true,
                now.minus(1, DateTimeUnit.WEEK),
                null,
                Transaction.Period.OneTime,
                "Start"
            ),
            Transaction(
                "TAG",
                1000.0,
                true,
                now.minus(1, DateTimeUnit.WEEK),
                now.plus(1, DateTimeUnit.WEEK),
                Transaction.Period.Week,
                "3 Times 1000"
            ),
        ).toChartInternal()

        assertEquals(3100.0, result.days.values.last())

    }
}