package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.data.remote.GeminiBusinessService
import com.example.ui.components.ResearchHeader
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

    @get:Rule val composeTestRule = createComposeRule()

    @Test
    fun bizintel_header_screenshot() {
        val research = GeminiBusinessService().generateComprehensiveBusinessReport("Stripe")

        composeTestRule.setContent {
            MyApplicationTheme {
                ResearchHeader(
                    research = research,
                    isBookmarked = false,
                    isGeneratingPdf = false,
                    onToggleBookmark = {},
                    onGeneratePdf = {},
                    onOpenArena = {}
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
    }
}
