package my.id.kentoes.rsudajibarangapp.core.model

import org.junit.Assert.assertEquals
import org.junit.Test

class StatusDisplayTest {

    @Test
    fun `inspectionStatusLabel maps server statuses to Indonesian`() {
        assertEquals("Menunggu Review", "PENDING".inspectionStatusLabel())
        assertEquals("Disetujui", "APPROVED".inspectionStatusLabel())
        assertEquals("Ditolak", "REJECTED".inspectionStatusLabel())
    }

    @Test
    fun `inspectionStatusLabel falls back to raw value for unknown status`() {
        assertEquals("CUSTOM", "CUSTOM".inspectionStatusLabel())
    }
}
