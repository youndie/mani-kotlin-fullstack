import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import ru.workinprogress.feature.transaction.Transaction
import ru.workinprogress.feature.transaction.amountSigned
import ru.workinprogress.feature.transaction.createDates
import ru.workinprogress.feature.transaction.defaultPeriod
import ru.workinprogress.feature.transaction.defaultPeriodAppend
import ru.workinprogress.feature.transaction.findZeroEvents
import ru.workinprogress.feature.transaction.simulate
import ru.workinprogress.feature.transaction.toChartInternal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

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
            (Clock.System.now()
                .toLocalDateTime(TimeZone.currentSystemDefault()).date.monthNumber + 3) % 12,
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
        assertEquals(
            3000.0,
            result.flatMap { it.value }.sumOf { transaction -> transaction.amountSigned })
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
        ).simulate(LocalDate(2000, 1, 1) to defaultPeriodAppend(LocalDate(2000, 1, 1)))

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

    @Test
    fun testZeroEventsNone() {
        val (positive, negative) = listOf(
            Transaction(
                "",
                100.0,
                true,
                LocalDate(2000, 1, 2),
                null,
                Transaction.Period.OneTime,
                ""
            ), Transaction(
                "",
                100.0,
                true,
                LocalDate(2000, 1, 3),
                null,
                Transaction.Period.OneTime,
                ""
            ),
            Transaction(
                "",
                100.0,
                false,
                LocalDate(2000, 1, 3),
                null,
                Transaction.Period.OneTime,
                ""
            ),
            Transaction(
                "",
                100.0,
                true,
                LocalDate(2000, 2, 3),
                null,
                Transaction.Period.OneTime,
                ""
            )
        ).simulate(
            LocalDate(2000, 1, 1)
                    to LocalDate(2000, 3, 1)
        ).findZeroEvents()

        assertTrue(positive == null)
        assertTrue(negative == null)
    }

    @Test
    fun testZeroEventsPositive() {
        val targetDate = LocalDate(2000, 1, 4)
        val (positive, negative) = listOf(
            Transaction(
                "",
                100.0,
                false,
                LocalDate(2000, 1, 2),
                null,
                Transaction.Period.OneTime,
                ""
            ), Transaction(
                "",
                100.0,
                true,
                LocalDate(2000, 1, 3),
                null,
                Transaction.Period.OneTime,
                ""
            ),
            Transaction(
                "",
                100.0,
                true,
                targetDate,
                null,
                Transaction.Period.OneTime,
                ""
            ),
            Transaction(
                "",
                100.0,
                true,
                LocalDate(2000, 2, 3),
                null,
                Transaction.Period.OneTime,
                ""
            )
        ).simulate(
            LocalDate(2000, 1, 1)
                    to LocalDate(2000, 3, 1)
        ).findZeroEvents()

        assertEquals(positive, targetDate)
        assertTrue(negative == null)
    }

    @Test
    fun testZeroEventsNegative() {
        val targetDate = LocalDate(2000, 1, 5)
        val (positive, negative) = listOf(
            Transaction(
                "",
                100.0,
                true,
                LocalDate(2000, 1, 2),
                null,
                Transaction.Period.OneTime,
                ""
            ), Transaction(
                "",
                100.0,
                true,
                LocalDate(2000, 1, 3),
                null,
                Transaction.Period.OneTime,
                ""
            ),
            Transaction(
                "",
                100.0,
                false,
                LocalDate(2000, 1, 4),
                null,
                Transaction.Period.OneTime,
                ""
            ),
            Transaction(
                "",
                200.0,
                false,
                targetDate,
                null,
                Transaction.Period.OneTime,
                ""
            ),
            Transaction(
                "",
                100.0,
                true,
                LocalDate(2000, 1, 1),
                null,
                Transaction.Period.OneTime,
                ""
            )
        ).simulate(
            LocalDate(2000, 1, 1)
                    to LocalDate(2000, 3, 1)
        ).findZeroEvents()

        assertEquals(negative, targetDate)
        assertTrue(positive == null)
    }

    @Test
    fun testZeroEvents() {
        val targetPositive = LocalDate(2000, 2, 3)
        val targetNegative = LocalDate(2000, 1, 5)

        val (positive, negative) = listOf(
            Transaction(
                "",
                100.0,
                true,
                LocalDate(2000, 1, 2),
                null,
                Transaction.Period.OneTime,
                ""
            ), Transaction(
                "",
                100.0,
                true,
                LocalDate(2000, 1, 3),
                null,
                Transaction.Period.OneTime,
                ""
            ),
            Transaction(
                "",
                100.0,
                false,
                LocalDate(2000, 1, 4),
                null,
                Transaction.Period.OneTime,
                ""
            ),
            Transaction(
                "",
                100.0,
                false,
                targetNegative,
                null,
                Transaction.Period.OneTime,
                ""
            ),
            Transaction(
                "",
                1000.0,
                true,
                targetPositive,
                null,
                Transaction.Period.OneTime,
                ""
            )
        ).simulate(
            LocalDate(2000, 1, 1)
                    to LocalDate(2000, 3, 1)
        ).findZeroEvents()

        assertEquals(targetPositive, positive)
        assertEquals(targetNegative, negative)
    }
}