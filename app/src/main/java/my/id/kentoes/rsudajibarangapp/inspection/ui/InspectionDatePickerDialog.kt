package my.id.kentoes.rsudajibarangapp.inspection.ui

import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable

/** Dialog pemilih tanggal untuk filter riwayat inspeksi. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InspectionDatePickerDialog(
    filterDate: String?,
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val initialMillis = filterDate?.let { parseDateToMillis(it) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialMillis
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                datePickerState.selectedDateMillis?.let { millis ->
                    onDateSelected(formatMillisToDate(millis))
                }
                onDismiss()
            }) {
                Text("Pilih")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}
