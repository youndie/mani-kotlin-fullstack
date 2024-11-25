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