@file:OptIn(ExperimentalCoroutinesApi::class)

package ru.workinprogress.feature.main

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.get
import ru.workinprogress.feature.auth.data.TokenRepository
import ru.workinprogress.feature.auth.data.TokenRepositoryCommon
import ru.workinprogress.feature.auth.data.TokenStorage
import ru.workinprogress.feature.auth.data.TokenStorageImpl
import ru.workinprogress.feature.auth.domain.LogoutUseCase
import ru.workinprogress.feature.categories.data.CategoriesRepository
import ru.workinprogress.feature.categories.domain.GetCategoriesUseCase
import ru.workinprogress.feature.category.FakeCategoriesDataSource
import ru.workinprogress.feature.currency.Currency
import ru.workinprogress.feature.currency.GetCurrentCurrencyUseCase
import ru.workinprogress.feature.currency.data.CurrentCurrencyRepository
import ru.workinprogress.feature.transaction.*
import ru.workinprogress.feature.transaction.data.FakeTransactionsRepository
import ru.workinprogress.feature.transaction.domain.DeleteTransactionsUseCase
import ru.workinprogress.feature.transaction.domain.GetTransactionsUseCase
import ru.workinprogress.feature.transaction.domain.TransactionRepository
import ru.workinprogress.feature.transaction.ui.ru.workinprogress.feature.transaction.testCurrencyRepository
import ru.workinprogress.mani.today
import kotlin.test.*


class MainViewModelTest : KoinTest {
    private var shouldReturnError = false
    private val targetCategory = Category("100", "Target")

    @BeforeTest
    fun setUp() {
        startKoin {
            modules(testModule({ shouldReturnError }))
        }
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @Test
    fun testLoadTransactionsSuccess() = runTest {
        val viewModel: MainViewModel = get()
        assertEquals(MainViewModel.loadingItems, viewModel.observe.value.transactions)
        while (viewModel.observe.value.loading) {
            runCurrent()
        }
        assertTrue(!viewModel.observe.value.loading)
        assertTrue(viewModel.observe.value.transactions.isNotEmpty())
        assertTrue(viewModel.observe.value.transactions.all { entry -> entry.key >= today() })

        get<TransactionRepository>().reset()
    }

    @Test
    fun testLoadTransactionsError() = runTest {
        shouldReturnError = true

        val viewModel: MainViewModel = get()
        assertEquals(MainViewModel.loadingItems, viewModel.observe.value.transactions)

        runCurrent()

        assertTrue(!viewModel.observe.value.loading)
        assertTrue(viewModel.observe.value.transactions.isEmpty())
        assertTrue(viewModel.observe.value.errorMessage != null)

        get<TransactionRepository>().reset()
    }

    @Test
    fun testTransactionFilters() = runTest {
        val viewModel: MainViewModel = get()

        runCurrent()

        viewModel.onUpcomingToggle(false)

        runCurrent()
        assertTrue(viewModel.observe.value.transactions.none { entry -> entry.key >= today() })

        viewModel.onCategorySelected(targetCategory)
        runCurrent()
        assertTrue(
            viewModel.observe.value.transactions
                .flatMap { it.value }
                .all { item ->
                    item.category.id == targetCategory.id
                })

        get<TransactionRepository>().reset()
    }

    @Test
    fun testTransactionSelected() = runTest {
        val viewModel: MainViewModel = get()
        while (viewModel.observe.value.loading) {
            runCurrent()
        }

        val firstTransaction = viewModel.observe.value.transactions.entries.first().value.first()

        viewModel.onTransactionSelected(firstTransaction)
        runCurrent()
        assertNotNull(
            viewModel.observe.value.selectedTransactions.find { item -> item.id == firstTransaction.id }
        )

        viewModel.onTransactionSelected(firstTransaction)
        runCurrent()
        assertNull(
            viewModel.observe.value.selectedTransactions.find { item -> item.id == firstTransaction.id }
        )

        get<TransactionRepository>().reset()
    }

    @Test
    fun testTransactionDelete() = runTest {
        val viewModel: MainViewModel = get()

        while (viewModel.observe.value.loading) {
            runCurrent()
        }

        val firstTransaction = viewModel.observe.value.transactions.entries.first().value.first()

        viewModel.onTransactionSelected(firstTransaction)
        viewModel.onShowDeleteDialogClicked()
        runCurrent()
        assertTrue(viewModel.observe.value.showDeleteDialog)

        viewModel.onDismissDeleteDialog()
        runCurrent()
        assertFalse(viewModel.observe.value.showDeleteDialog)

        viewModel.onShowDeleteDialogClicked()
        runCurrent()
        assertTrue(viewModel.observe.value.showDeleteDialog)
        viewModel.onDeleteClicked()

        runCurrent()
        assertFalse(viewModel.observe.value.showDeleteDialog)
        assertTrue(viewModel.observe.value.selectedTransactions.isEmpty())

        assertNull(
            viewModel.observe.value.transactions.flatMap {
                it.value
            }.find { item -> item.id == firstTransaction.id }
        )

        assertNull(
            viewModel.observe.value.selectedTransactions.find { item -> item.id == firstTransaction.id }
        )

        get<TransactionRepository>().reset()
    }

    @Test
    fun testCloseContextMenu() = runTest {
        val viewModel: MainViewModel = get()

        while (viewModel.observe.value.loading) {
            runCurrent()
        }

        viewModel.onCategorySelected(targetCategory)
        viewModel.onContextMenuClosed()
        assertTrue(viewModel.observe.value.selectedTransactions.isEmpty())
    }

    @Test
    fun testLogout() = runTest {
        val viewModel: MainViewModel = get()

        while (viewModel.observe.value.loading) {
            runCurrent()
        }

        viewModel.onProfileClicked()
        assertTrue(viewModel.observe.value.showProfile)

        viewModel.onLogoutClicked()
        runCurrent()
        assertTrue(viewModel.observe.value.transactions.isEmpty())
    }

    @Test
    fun testProfile() = runTest {
        val viewModel: MainViewModel = get()

        while (viewModel.observe.value.loading) {
            runCurrent()
        }

        viewModel.onProfileClicked()
        assertTrue(viewModel.observe.value.showProfile)
        viewModel.onProfileDismiss()
        assertFalse(viewModel.observe.value.showProfile)
    }

    @Test
    fun testFutureInfoSimple() {
        val start = LocalDate(2000, 1, 1)
        val transactions = listOf(Transaction("0", 100.0, true, start, null, Transaction.Period.OneTime, ""))
        val today = start.plus(1, DateTimeUnit.DAY)

        val futureInformation = MainViewModel.buildFutureInformation(
            transactions.simulate(start, defaultPeriodAppend(start)),
            Currency.Usd,
            today
        )

        assertEquals(
            "balance: +100 \$\n" +
                    "today balance change: 0 \$\n" +
                    "in month: +100 \$, in next month: 0 \$\n" +
                    "no zero events", futureInformation.toString()
        )
    }


    @Test
    fun testFutureInfoTodayBalanceChange() {
        val start = LocalDate(2000, 1, 1)

        val transactions = listOf(
            Transaction("0", 100.0, true, start, null, Transaction.Period.OneTime, ""),
            Transaction("0", 50.0, true, start.plus(1, DateTimeUnit.DAY), null, Transaction.Period.OneTime, "")
        )
        val today = start.plus(1, DateTimeUnit.DAY)

        val futureInformation = MainViewModel.buildFutureInformation(
            transactions.simulate(start, defaultPeriodAppend(start)),
            Currency.Usd,
            today
        )

        assertEquals(
            "balance: +150 \$\n" +
                    "today balance change: +50 \$\n" +
                    "in month: +150 \$, in next month: 0 \$\n" +
                    "no zero events", futureInformation.toString()
        )
    }

    @Test
    fun testFutureInfoNegative() {
        val start = LocalDate(2000, 1, 1)
        val transactions = listOf(
            Transaction("0", 100.0, true, start, null, Transaction.Period.OneTime, ""),
            Transaction("0", 200.0, false, start.plus(5, DateTimeUnit.DAY), null, Transaction.Period.OneTime, "")

        )
        val today = start.plus(1, DateTimeUnit.DAY)

        val futureInformation = MainViewModel.buildFutureInformation(
            transactions.simulate(start, defaultPeriodAppend(start)),
            Currency.Usd,
            today
        )

        assertEquals(
            "balance: +100 \$\n" +
                    "today balance change: 0 \$\n" +
                    "next transaction 06 Jan 2000: −200 \$\n" +
                    "in month: −100 \$, in next month: 0 \$\n" +
                    "balance will become negative: 06 Jan 2000", futureInformation.toString()
        )
    }

    @Test
    fun testFutureInfoPositive() {
        val start = LocalDate(2000, 1, 1)
        val transactions = listOf(
            Transaction("0", 100.0, false, start, null, Transaction.Period.OneTime, ""),
            Transaction("0", 200.0, true, start.plus(5, DateTimeUnit.DAY), null, Transaction.Period.OneTime, "")

        )
        val today = start.plus(1, DateTimeUnit.DAY)

        val futureInformation = MainViewModel.buildFutureInformation(
            transactions.simulate(start, defaultPeriodAppend(start)),
            Currency.Usd,
            today
        )
        assertEquals(
            "balance: −100 \$\n" +
                    "today balance change: 0 \$\n" +
                    "next transaction 06 Jan 2000: +200 \$\n" +
                    "in month: +100 \$, in next month: 0 \$\n" +
                    "balance will become positive: 06 Jan 2000", futureInformation.toString()
        )
    }

    @Test
    fun testFutureInfoNextMonth() {
        val start = LocalDate(2000, 1, 1)
        val transactions = listOf(
            Transaction("0", 100.0, true, start, null, Transaction.Period.OneTime, ""),
            Transaction("0", 200.0, false, start.plus(1, DateTimeUnit.MONTH), null, Transaction.Period.OneTime, "")

        )
        val today = start.plus(1, DateTimeUnit.DAY)

        val futureInformation = MainViewModel.buildFutureInformation(
            transactions.simulate(start, defaultPeriodAppend(start)),
            Currency.Usd,
            today
        )

        assertEquals(
            "balance: +100 \$\n" +
                    "today balance change: 0 \$\n" +
                    "next transaction 01 Feb 2000: −200 \$\n" +
                    "in month: +100 \$, in next month: −200 \$\n" +
                    "balance will become negative: 01 Feb 2000", futureInformation.toString()
        )
    }


    @AfterTest
    fun tearDown() {
        stopKoin()
        Dispatchers.resetMain()
    }

    private fun testModule(withError: () -> Boolean) = module {
        single<TransactionRepository> {
            FakeTransactionsRepository(
                withError,
                listOf(
                    Transaction(
                        id = "upcoming",
                        amount = 500.0,
                        income = true,
                        date = today().plus(1, DateTimeUnit.DAY),
                        until = null,
                        period = Transaction.Period.OneTime,
                        comment = ""
                    ),
                    Transaction(
                        id = "past",
                        amount = 250.0,
                        income = true,
                        date = LocalDate(2000, 1, 1),
                        until = null,
                        period = Transaction.Period.OneTime,
                        comment = ""
                    ),
                    Transaction(
                        id = "past2",
                        amount = 250.0,
                        income = true,
                        date = LocalDate(2000, 1, 1),
                        until = null,
                        period = Transaction.Period.OneTime,
                        comment = "",
                        category = targetCategory
                    )
                )
            )
        }
        single<GetTransactionsUseCase> { GetTransactionsUseCase(get()) }
        single<GetCurrentCurrencyUseCase> { GetCurrentCurrencyUseCase(get()) }
        single<CurrentCurrencyRepository> { testCurrencyRepository }
        single<DeleteTransactionsUseCase> { DeleteTransactionsUseCase(get()) }
        single<GetCategoriesUseCase> { GetCategoriesUseCase(get()) }
        factory<MainViewModel> { MainViewModel(get(), get(), get(), get(), get()) }

        singleOf(::TokenRepositoryCommon).bind<TokenRepository>()
        singleOf(::TokenStorageImpl).bind<TokenStorage>()
        singleOf(::LogoutUseCase)
        singleOf(::FakeCategoriesDataSource).bind<DataSource<Category>>()
        singleOf(::CategoriesRepository)
    }

}