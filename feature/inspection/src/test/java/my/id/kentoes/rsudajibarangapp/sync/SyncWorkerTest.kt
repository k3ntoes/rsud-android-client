package my.id.kentoes.rsudajibarangapp.sync

import org.junit.Assert.assertEquals
import org.junit.Test

class SyncWorkerTest {

    @Test
    fun `WORK_NAME constant is sync_inspection`() {
        assertEquals("sync_inspection", SyncWorker.WORK_NAME)
    }

    @Test
    fun `NOTIFICATION_CHANNEL_ID constant is sync_channel`() {
        assertEquals("sync_channel", SyncWorker.NOTIFICATION_CHANNEL_ID)
    }

    @Test
    fun `NOTIFICATION_ID constant is 1001`() {
        assertEquals(1001, SyncWorker.NOTIFICATION_ID)
    }
}
