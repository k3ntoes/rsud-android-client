package my.id.kentoes.rsudajibarangapp.dashboard.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import my.id.kentoes.rsudajibarangapp.core.database.entity.DrafInspeksi
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DashboardComposablesTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // ── StatCard ──

    @Test
    fun `StatCard displays icon label value and color`() {
        composeTestRule.setContent {
            StatCard(
                icon = Icons.Default.CheckCircle,
                label = "Total Item",
                value = "42",
                color = Color(0xFF388E3C)
            )
        }

        composeTestRule.onNodeWithText("42").assertIsDisplayed()
        composeTestRule.onNodeWithText("Total Item").assertIsDisplayed()
    }

    // ── RecentDraftCard ──

    @Test
    fun `RecentDraftCard displays room ID and date`() {
        composeTestRule.setContent {
            RecentDraftCard(
                draft = DrafInspeksi(
                    id = 1, roomId = 5,
                    localTimestamp = "2026-07-28T10:00:00Z",
                    status = "DRAFT"
                )
            )
        }

        composeTestRule.onNodeWithText("Ruangan #5").assertIsDisplayed()
        composeTestRule.onNodeWithText("2026-07-28").assertIsDisplayed()
    }

    @Test
    fun `RecentDraftCard shows PENDING_SYNC status`() {
        composeTestRule.setContent {
            RecentDraftCard(
                draft = DrafInspeksi(
                    id = 2, roomId = 10,
                    localTimestamp = "2026-07-29T08:00:00Z",
                    status = "PENDING_SYNC"
                )
            )
        }

        composeTestRule.onNodeWithText("Ruangan #10").assertIsDisplayed()
    }
}
