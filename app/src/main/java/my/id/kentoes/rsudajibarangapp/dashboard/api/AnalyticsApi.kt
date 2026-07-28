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
}
