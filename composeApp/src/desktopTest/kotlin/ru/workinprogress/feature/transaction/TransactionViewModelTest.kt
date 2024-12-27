package ru.workinprogress.feature.transaction

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.*
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.get
import ru.workinprogress.feature.categories.categoriesModule
import ru.workinprogress.feature.categories.data.CategoriesRepository
import ru.workinprogress.feature.category.FakeCategoriesDataSource
import ru.workinprogress.feature.currency.currencyModule
import ru.workinprogress.feature.transaction.data.FakeTransactionsRepository
import ru.workinprogress.feature.transaction.domain.AddTransactionUseCase
import ru.workinprogress.feature.transaction.domain.TransactionRepository
import ru.workinprogress.feature.transaction.ui.AddTransactionViewModel
import ru.workinprogress.mani.today
import kotlin.test.*

class TransactionViewModelTest : KoinTest {

    private var withError = false

    @BeforeTest
    fun setUp() {
        // Start Koin
        startKoin {
            modules(
                categoriesModule, currencyModule, module {
                    single<TransactionRepository> {
                        FakeTransactionsRepository(
                            { withError }, listOf(
                                Transaction(
                                    id = "upcoming",
                                    amount = 500.0,
                                    income = true,
                                    date = today().plus(1, DateTimeUnit.DAY),
                                    until = null,
                                    period = Transaction.Period.OneTime,
                                    comment = ""
                                ), Transaction(
                                    id = "past",
                                    amount = 250.0,
                                    income = true,
                                    date = LocalDate(2000, 1, 1),
                                    until = null,
                                    period = Transaction.Period.OneTime,
                                    comment = ""
                                ), Transaction(
                                    id = "past2",
                                    amount = 250.0,
                                    income = true,
                                    date = LocalDate(2000, 1, 1),
                                    until = null,
                                    period = Transaction.Period.OneTime,
                                    comment = "",
                                )
                            )
                        )
                    }

                    singleOf(::FakeCategoriesDataSource).bind<DataSource<Category>>()

                    factory {
                        AddTransactionViewModel(get(), get(), get(), get(), get(), Dispatchers.Unconfined)
                    }
                    singleOf(::AddTransactionUseCase)
                })
        }

        Dispatchers.setMain(StandardTestDispatcher())
    }

    @Test
    fun simpleTest() = runTest {
        var viewModel: AddTransactionViewModel = get()
        runCurrent()

        assertEquals(
            true, viewModel.observe.value.income
        )


        viewModel.onAmountChanged("100")
        viewModel.onCommentChanged("simpleTest")
        viewModel.onSubmitClicked()

        runCurrent()

        val result = get<TransactionRepository>().dataStateFlow.value.find { transaction ->
            transaction.amount == 100.0
            transaction.comment == "simpleTest"
        }

        assertNotNull(result)
        assertEquals(result.date, today())
    }

    @Test
    fun dateTest() = runTest {
        var viewModel: AddTransactionViewModel = get()
        runCurrent()
        assertFalse(viewModel.observe.value.until.showDatePicker)

        viewModel.onAmountChanged("100")
        viewModel.onCommentChanged("dateTest")
        viewModel.onDateSelected(LocalDate(2000, 1, 2))
        runCurrent()

        assertEquals(Transaction.Period.OneTime, viewModel.observe.value.period)

        assertEquals(
            viewModel.observe.value.futureInformation.toString(), "+100 $ on 01/02/2000"
        )

        viewModel.onPeriodChanged(Transaction.Period.TwoWeek)
        runCurrent()

        assertEquals(Transaction.Period.TwoWeek, viewModel.observe.value.period)

        assertEquals(
            viewModel.observe.value.futureInformation.toString(),
            "+100 $ from 01/02/2000. In 3 month's repeat 7 times, total: +700 $"
        )

        viewModel.onSubmitClicked()

        runCurrent()

        val result = get<TransactionRepository>().dataStateFlow.value.find { transaction ->
            transaction.amount == 100.0
            transaction.comment == "dateTest"
        }

        assertNotNull(result)
        assertEquals(result.date, LocalDate(2000, 1, 2))
    }

    @Test
    fun untilTest() = runTest {
        var viewModel: AddTransactionViewModel = get()
        runCurrent()

        viewModel.onAmountChanged("100")
        viewModel.onCommentChanged("untilTest")
        viewModel.onDateSelected(LocalDate(2000, 1, 2))
        runCurrent()

        viewModel.onPeriodChanged(Transaction.Period.Day)

        assertEquals(
            "+100 \$ from 01/02/2000. In 3 month's repeat 91 times, total: +9100 \$",
            viewModel.observe.value.futureInformation.toString()
        )

        viewModel.onDateUntilSelected(LocalDate(2000, 2, 2))

        assertEquals(
            "+100 \$ from 01/02/2000 to 02/02/2000 repeat 31 times, total: +3100 \$",
            viewModel.observe.value.futureInformation.toString()
        )
    }

    @Test
    fun largePeriodTest() = runTest {
        var viewModel: AddTransactionViewModel = get()
        runCurrent()

        viewModel.onAmountChanged("100")
        viewModel.onCommentChanged("untilTest")
        viewModel.onDateSelected(LocalDate(2000, 1, 2))
        runCurrent()

        viewModel.onPeriodChanged(Transaction.Period.Month)

        assertEquals(
            "+100 \$ from 01/02/2000. In 1 year's repeat 12 times, total: +1200 \$",
            viewModel.observe.value.futureInformation.toString()
        )
    }


    @Test
    fun createCategoryTest() = runTest {
        var viewModel: AddTransactionViewModel = get()
        runCurrent()

        viewModel.onCategoryCreate("New category")
        runCurrent()

        assertNotNull(get<CategoriesRepository>().dataStateFlow.value.find { category ->
            category.name == "New category"
        })
    }


    @Test
    fun createCategoryErrorTest() = runTest {
        var viewModel: AddTransactionViewModel = get()
        runCurrent()

        get<FakeCategoriesDataSource>().withError = true
        viewModel.onCategoryCreate("Error category")
        runCurrent()

        assertNotNull(viewModel.observe.value.errorMessage)
        assertNull(get<CategoriesRepository>().dataStateFlow.value.find { category ->
            category.name == "Error category"
        })

        get<FakeCategoriesDataSource>().withError = false
    }

    @Test
    fun deleteCategoryTest() = runTest {
        var viewModel: AddTransactionViewModel = get()
        runCurrent()
        val category = Category("10000", "to delete")
        get<CategoriesRepository>().create(category)
        runCurrent()
        assertNotNull(
            viewModel.observe.value.categories.find {
                it.name == category.name
            })

        viewModel.onCategoryChanged(category)
        viewModel.onCategoryDelete(category)
        runCurrent()
        assertNull(
            viewModel.observe.value.categories.find {
                it.name == category.name
            })
        assertNotEquals(category, viewModel.observe.value.category)
    }

    @Test
    fun deleteCategoryErrorTest() = runTest {
        var viewModel: AddTransactionViewModel = get()
        runCurrent()
        val category = Category("10000", "to delete")
        get<CategoriesRepository>().create(category)

        get<FakeCategoriesDataSource>().withError = true

        viewModel.onCategoryChanged(category)
        viewModel.onCategoryDelete(category)
        runCurrent()
        assertNotNull(
            viewModel.observe.value.categories.find {
                it.name == category.name
            })
        assertEquals(category, viewModel.observe.value.category)

        get<FakeCategoriesDataSource>().withError = false
    }


    @Test
    fun anotherTest() = runTest {
        var viewModel: AddTransactionViewModel = get()
        runCurrent()

        assertFalse(viewModel.observe.value.valid)
        assertNull(viewModel.observe.value.errorMessage)

        viewModel.onAmountChanged("100")
        viewModel.onCommentChanged("anotherTest")

        assertTrue(viewModel.observe.value.valid)

        withError = true
        viewModel.onSubmitClicked()

        runCurrent()

        val result = get<TransactionRepository>().dataStateFlow.value.find { transaction ->
            transaction.amount == 100.0
            transaction.comment == "anotherTest"
        }

        assertNotNull(viewModel.observe.value.errorMessage)
        assertNull(result)
        withError = false
    }


    @AfterTest
    fun tearDown() {
        stopKoin()
        Dispatchers.resetMain()
    }

}