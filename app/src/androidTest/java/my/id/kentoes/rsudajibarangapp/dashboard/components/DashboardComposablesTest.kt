package my.id.kentoes.rsudajibarangapp.dashboard.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import my.id.kentoes.rsudajibarangapp.core.database.entity.DrafInspeksi
import my.id.kentoes.rsudajibarangapp.dashboard.api.IssueFrequencyOut
import my.id.kentoes.rsudajibarangapp.dashboard.api.RoomScoreOut
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

    // ── RoomScoreCard ──

    @Test
    fun `RoomScoreCard displays room ID and inspection count`() {
        composeTestRule.setContent {
            RoomScoreCard(
                room = RoomScoreOut(roomId = 3, scorePct = 0.85, inspectionCount = 7)
            )
        }

        composeTestRule.onNodeWithText("Ruangan #3").assertIsDisplayed()
        composeTestRule.onNodeWithText("7x inspeksi").assertIsDisplayed()
        composeTestRule.onNodeWithText("85%").assertIsDisplayed()
    }

    @Test
    fun `RoomScoreCard shows warning color for medium score`() {
        composeTestRule.setContent {
            RoomScoreCard(
                room = RoomScoreOut(roomId = 1, scorePct = 0.60, inspectionCount = 3)
            )
        }

        composeTestRule.onNodeWithText("60%").assertIsDisplayed()
        composeTestRule.onNodeWithText("3x inspeksi").assertIsDisplayed()
    }

    @Test
    fun `RoomScoreCard shows error color for low score`() {
        composeTestRule.setContent {
            RoomScoreCard(
                room = RoomScoreOut(roomId = 2, scorePct = 0.30, inspectionCount = 5)
            )
        }

        composeTestRule.onNodeWithText("30%").assertIsDisplayed()
        composeTestRule.onNodeWithText("Ruangan #2").assertIsDisplayed()
    }

    // ── IssueCard ──

    @Test
    fun `IssueCard displays item name and frequency`() {
        composeTestRule.setContent {
            IssueCard(
                issue = IssueFrequencyOut(itemId = 1, itemNameSnapshot = "Meja", scoreZeroCount = 12)
            )
        }

        composeTestRule.onNodeWithText("Meja").assertIsDisplayed()
        composeTestRule.onNodeWithText("12x").assertIsDisplayed()
    }

    @Test
    fun `IssueCard displays zero count correctly`() {
        composeTestRule.setContent {
            IssueCard(
                issue = IssueFrequencyOut(itemId = 2, itemNameSnapshot = "Lantai", scoreZeroCount = 0)
            )
        }

        composeTestRule.onNodeWithText("Lantai").assertIsDisplayed()
        composeTestRule.onNodeWithText("0x").assertIsDisplayed()
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
