package my.id.kentoes.rsudajibarangapp.inspection.ui

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

/** Menampilkan Snackbar setiap kali [error] berubah menjadi non-null. */
@Composable
fun ErrorSnackbarEffect(
    error: String?,
    snackbarHostState: SnackbarHostState
) {
    LaunchedEffect(error) {
        error?.let { snackbarHostState.showSnackbar(it) }
    }
}
