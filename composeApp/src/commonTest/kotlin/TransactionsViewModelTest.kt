import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.*
import kotlinx.datetime.LocalDate
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.get
import ru.workinprogress.feature.currency.Currency
import ru.workinprogress.feature.currency.GetCurrentCurrencyUseCase
import ru.workinprogress.feature.currency.data.CurrentCurrencyRepository
import ru.workinprogress.feature.transaction.Transaction
import ru.workinprogress.feature.transaction.data.TransactionRepository
import ru.workinprogress.feature.transaction.domain.DeleteTransactionsUseCase
import ru.workinprogress.feature.transaction.domain.GetTransactionsUseCase
import ru.workinprogress.feature.transaction.ui.TransactionsViewModel
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue

val currencyRepository = object : CurrentCurrencyRepository {
    override var currency = Currency.Usd
}

private fun testModule(withError: Boolean = false) = module {
    single<TransactionRepository> { FakeTransactionsRepository(withError) }
    single<GetTransactionsUseCase> { GetTransactionsUseCase(get()) }
    single<GetCurrentCurrencyUseCase> { GetCurrentCurrencyUseCase(get()) }
    single<CurrentCurrencyRepository> { currencyRepository }
    single<DeleteTransactionsUseCase> { DeleteTransactionsUseCase(get()) }
    single<TransactionsViewModel> { TransactionsViewModel(get(), get(), get()) }
}

@OptIn(ExperimentalCoroutinesApi::class)
class TransactionsViewModelTest : KoinTest {

    private lateinit var viewModel: TransactionsViewModel

    @BeforeTest
    fun setUp() {
        // Start Koin
        startKoin {
            modules(testModule(false))
        }
        viewModel = get()
        Dispatchers.setMain(StandardTestDispatcher())
    }

    //
    @Test
    fun testLoadTransactions() = runTest {
        while (viewModel.observe.value.loading) {
            runCurrent()
        }

        assertTrue(viewModel.observe.value.errorMessage == null)
        assertTrue(
            viewModel.observe.value.data.isNotEmpty(),
            "Expected transactions to be fetched but got an empty list."
        )
    }
//
//    @Test
//    fun testDeleteSelectedTransactions() = runTest {
//        while (viewModel.observe.value.loading) {
//            runCurrent()
//        }
//
//        viewModel.onTransactionSelected(TransactionUiItem(toDelete, currencyRepository.currency))
//        assertTrue(
//            TransactionUiItem(
//                toDelete,
//                currencyRepository.currency
//            ) in viewModel.observe.value.selectedTransactions
//        )
//
//        viewModel.onDeleteClicked()
//        runCurrent()
////        assertTrue(viewModel.observe.value.selectedTransactions.isEmpty())
//        while (viewModel.observe.value.selectedTransactions.isEmpty().not()) {
//            runCurrent()
//        }
//        runCurrent()
//
//        assertTrue(viewModel.observe.value.data.flatMap { it.value }.none { it.id == toDelete.id })
//    }


    @AfterTest
    fun tearDown() {
        stopKoin()
        Dispatchers.resetMain()
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class TransactionsViewModelErrorTest : KoinTest {

    private lateinit var viewModel: TransactionsViewModel

    @BeforeTest
    fun setUp() {
        // Start Koin
        startKoin {
            modules(testModule(true))
        }
        viewModel = get()
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @Test
    fun testLoadTransactionsFailed() = runTest {
        while (viewModel.observe.value.loading) {
            runCurrent()
        }

        assertTrue(viewModel.observe.value.errorMessage == "fake")
        assertTrue(
            viewModel.observe.value.data.isEmpty(),
            "Expected an empty list due to API failure but got ${viewModel.observe.value.data.size} items."
        )
    }

    @AfterTest
    fun tearDown() {
        stopKoin()
        Dispatchers.resetMain()
    }
}

val toDelete = Transaction(
    "toDelete", 500.0, true, LocalDate(2000, 1, 1), null, Transaction.Period.OneTime, ""
)

private class FakeTransactionsRepository(private val shouldCrash: Boolean = false) :
    TransactionRepository {
    private val data = MutableStateFlow(emptyList<Transaction>())

    override val dataStateFlow: StateFlow<List<Transaction>> = data

    override suspend fun load() {
        if (shouldCrash) throw RuntimeException("fake")
        data.value = listOf(
            Transaction(
                "", 500.0, true, LocalDate(2000, 1, 1), null, Transaction.Period.OneTime, ""
            ), toDelete
        )
    }

    override fun getById(transactionId: String): Transaction {
        return data.value.first { it.id == transactionId }
    }

    override suspend fun create(params: Transaction): Boolean {
        data.value += params
        return true
    }

    override suspend fun update(params: Transaction): Boolean {
        data.value = data.value - getById(params.id) + params
        return true
    }

    override suspend fun delete(transactionId: String): Boolean {
        data.value -= getById(transactionId)
        return true
    }

    override fun reset() {
        data.value = emptyList()
    }
}
