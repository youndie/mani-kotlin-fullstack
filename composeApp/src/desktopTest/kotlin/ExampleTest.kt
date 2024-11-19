import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test
import org.koin.compose.KoinApplication
import org.koin.core.KoinApplication
import ru.workinprogress.feature.auth.ui.LoginComponent
import ru.workinprogress.mani.ManiApp
import ru.workinprogress.mani.appModules
import ru.workinprogress.mani.theme.AppTheme

//class ExampleTest {
//    @get:Rule
//    val rule = createComposeRule()
//
//    @Test
//    fun myTest() {
//        // Declares a mock UI to demonstrate API calls
//        //
//        // Replace with your own declarations to test the code in your project
//        rule.setContent {
//            KoinApplication(
//                application = {
//                    modules(appModules)
//                }) {
//                LoginComponent { }
//            }
//        }
//
//        // Tests the declared UI with assertions and actions of the JUnit-based testing API
//        rule.onNodeWithTag("appname").assertTextEquals("Mani")
////        rule.onNodeWithTag("button").performClick()
////        rule.onNodeWithTag("text").assertTextEquals("Compose")
//    }
//}