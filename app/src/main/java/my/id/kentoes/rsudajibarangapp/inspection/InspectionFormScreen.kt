package my.id.kentoes.rsudajibarangapp.inspection

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import my.id.kentoes.rsudajibarangapp.inspection.components.ItemCard
import my.id.kentoes.rsudajibarangapp.inspection.components.createTempPhotoUri
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InspectionFormScreen(
    roomId: Long,
    roomName: String,
    draftId: Long? = null,
    onNavigateBack: () -> Unit,
    viewModel: InspectionFormViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Inisialisasi ViewModel dengan roomId/roomName — dan draftId untuk resume
    LaunchedEffect(roomId, draftId) {
        viewModel.init(roomId, roomName, draftId)
    }

    // Navigasi balik saat draft tersimpan
    LaunchedEffect(uiState.draftSaved) {
        if (uiState.draftSaved) {
            snackbarHostState.showSnackbar("Draf tersimpan")
            viewModel.clearDraftSaved()
            onNavigateBack()
        }
    }

    // Camera permission launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) {
            Toast.makeText(context, "Izin kamera diperlukan untuk mengambil foto", Toast.LENGTH_SHORT).show()
        }
    }

    // Variabel untuk menyimpan itemId yang sedang difoto
    val currentPhotoItemId = remember { mutableLongStateOf(-1L) }

    // Camera capture launcher — single nullable Uri, bukan mutableListOf
    var pendingPhotoUri by remember { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        val uri = pendingPhotoUri
        if (success && uri != null) {
            pendingPhotoUri = null
            // Copy dari URI content ke local file
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val photoDir = File(context.getExternalFilesDir(null), "photos")
                if (!photoDir.exists()) photoDir.mkdirs()
                val photoFile = File(photoDir, "capture_${System.currentTimeMillis()}.jpg")
                inputStream?.use { input ->
                    photoFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                viewModel.addPhoto(currentPhotoItemId.longValue, photoFile.absolutePath)
            } catch (e: Exception) {
                Toast.makeText(context, "Gagal memproses foto: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Form Inspeksi", fontWeight = FontWeight.Bold)
                        Text(
                            roomName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            // Bottom bar with progress + actions
            Column(modifier = Modifier.padding(16.dp)) {
                // Progress bar
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    LinearProgressIndicator(
                        progress = {
                            if (uiState.totalItems > 0) uiState.validItems.toFloat() / uiState.totalItems
                            else 0f
                        },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${uiState.validItems}/${uiState.totalItems}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = viewModel::saveDraft,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Simpan Draf")
                    }
                    Button(
                        onClick = viewModel::submit,
                        enabled = uiState.submitEnabled,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Kirim")
                    }
                }
            }
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.items.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Tidak ada item untuk ruangan ini",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 8.dp,
                    bottom = 160.dp // ruang untuk bottom bar
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Header kategori items
                uiState.groupedItems.forEach { (kategori, items) ->
                    item(key = "header_$kategori") {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Text(
                                text = "$kategori (${items.size})",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(12.dp),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    items(items, key = { "item_${it.itemId}" }) { item ->
                        ItemCard(
                            itemId = item.itemId,
                            nama = item.nama,
                            deskripsi = item.deskripsi,
                            currentScore = item.skor,
                            fotoPaths = item.fotoPaths,
                            currentCatatan = item.catatan,
                            onScoreSelected = { skor ->
                                viewModel.updateScore(item.itemId, skor)
                            },
                            onAddPhoto = {
                                currentPhotoItemId.longValue = item.itemId
                                // Cek permission kamera
                                if (ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.CAMERA
                                    ) == PackageManager.PERMISSION_GRANTED
                                ) {
                                    val uri = createTempPhotoUri(context)
                                    pendingPhotoUri = uri
                                    cameraLauncher.launch(uri)
                                } else {
                                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                }
                            },
                            onDeletePhoto = { path ->
                                viewModel.deletePhoto(item.itemId, path)
                            },
                            onCatatanChanged = { catatan ->
                                viewModel.updateCatatan(item.itemId, catatan)
                            }
                        )
                    }
                }
            }
        }
    }
}


