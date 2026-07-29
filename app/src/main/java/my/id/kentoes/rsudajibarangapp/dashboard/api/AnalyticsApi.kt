package my.id.kentoes.rsudajibarangapp.dashboard.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Query

@Serializable
data class RoomScoreOut(
    @SerialName("room_id")
    val roomId: Int,
    @SerialName("score_pct")
    val scorePct: Double = 0.0,
    @SerialName("inspection_count")
    val inspectionCount: Int = 0
)

@Serializable
data class IssueFrequencyOut(
    @SerialName("item_id")
    val itemId: Int,
    @SerialName("item_name_snapshot")
    val itemNameSnapshot: String,
    @SerialName("score_zero_count")
    val scoreZeroCount: Int
)

@Serializable
data class DashboardDto(
    @SerialName("pending_count")
    val pendingCount: Int = 0,
    @SerialName("total_rooms")
    val totalRooms: Int = 0,
    @SerialName("monthly_inspection_count")
    val monthlyInspectionCount: Int = 0,
    @SerialName("avg_score_pct")
    val avgScorePct: Double = 0.0
)

interface AnalyticsApi {

    @GET("analytics/lowest-rooms")
    suspend fun getLowestRooms(
        @Query("year_month") yearMonth: String? = null,
        @Query("limit") limit: Int = 3
    ): List<RoomScoreOut>

    @GET("analytics/top-issues")
    suspend fun getTopIssues(
        @Query("year_month") yearMonth: String? = null,
        @Query("limit") limit: Int = 10
    ): List<IssueFrequencyOut>

    @GET("analytics/dashboard")
    suspend fun getDashboard(
        @Query("year_month") yearMonth: String? = null
    ): DashboardDto
}
