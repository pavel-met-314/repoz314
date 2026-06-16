package presentation.screens.admin

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import domain.model.Portfolio
import domain.model.Service
import presentation.ui.ParikmariumTopAppBar
import presentation.ui.RemoteImage
import presentation.viewmodel.PortfolioUiState
import presentation.viewmodel.PortfolioViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPortfolioScreen(
    portfolioViewModel: PortfolioViewModel,
    onBack: () -> Unit
) {
    val portfolio by portfolioViewModel.portfolio.collectAsState()
    val services by portfolioViewModel.services.collectAsState()
    val isLoading by portfolioViewModel.isLoading.collectAsState()
    val uploadState by portfolioViewModel.uploadState.collectAsState()

    var showUploadSheet by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        portfolioViewModel.loadPortfolio()
        portfolioViewModel.loadServices()
    }

    // После успешной загрузки — закрываем шторку
    LaunchedEffect(uploadState) {
        if (uploadState is PortfolioUiState.Success) {
            showUploadSheet = false
            portfolioViewModel.resetUploadState()
        }
    }

    if (showUploadSheet) {
        UploadPhotoSheet(
            services = services,
            uploadState = uploadState,
            onDismiss = {
                showUploadSheet = false
                portfolioViewModel.resetUploadState()
            },
            onUpload = { uri, serviceId, caption ->
                portfolioViewModel.uploadPhoto(uri, serviceId, caption)
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            ParikmariumTopAppBar(title = "Портфолио", onBack = onBack)
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showUploadSheet = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = MaterialTheme.shapes.large
            ) {
                Icon(Icons.Default.Add, contentDescription = "Добавить фото")
            }
        }
    ) { innerPadding ->
        when {
            isLoading -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }

            portfolio.isEmpty() -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Нажмите + чтобы добавить первое фото",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            else -> LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = innerPadding.calculateTopPadding() + 8.dp,
                    bottom = innerPadding.calculateBottomPadding() + 80.dp
                ),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(portfolio) { item ->
                    AdminPortfolioItem(
                        portfolio = item,
                        onDelete = { portfolioViewModel.deletePhoto(item) }
                    )
                }
            }
        }
    }
}

@Composable
private fun AdminPortfolioItem(portfolio: Portfolio, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            RemoteImage(
                url = portfolio.imageUrl,
                contentDescription = portfolio.caption,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp),
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.9f)
            ) {
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Удалить",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            if (portfolio.caption.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .background(Color.Black.copy(alpha = 0.5f))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = portfolio.caption,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        maxLines = 2
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UploadPhotoSheet(
    services: List<Service>,
    uploadState: PortfolioUiState,
    onDismiss: () -> Unit,
    onUpload: (Uri, String?, String) -> Unit
) {
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var caption by remember { mutableStateOf("") }
    var selectedService by remember { mutableStateOf<Service?>(null) }
    var expanded by remember { mutableStateOf(false) }

    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> selectedUri = uri }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Добавить фото") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                // Выбор фото
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(MaterialTheme.shapes.large)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant,
                            shape = MaterialTheme.shapes.large
                        )
                        .clickable { launcher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedUri != null) {
                        val bitmap = remember(selectedUri) {
                            try {
                                val inputStream = context.contentResolver.openInputStream(selectedUri!!)
                                val bmp = android.graphics.BitmapFactory.decodeStream(inputStream)
                                inputStream?.close()
                                bmp?.asImageBitmap()
                            } catch (_: Exception) { null }
                        }
                        if (bitmap != null) {
                            Image(
                                bitmap = bitmap,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize().clip(MaterialTheme.shapes.large)
                            )
                        }
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Выбрать из галереи",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Выбор услуги
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedService?.name ?: "Без привязки к услуге",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Услуга") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Без привязки к услуге") },
                            onClick = { selectedService = null; expanded = false }
                        )
                        services.forEach { service ->
                            DropdownMenuItem(
                                text = { Text(service.name) },
                                onClick = { selectedService = service; expanded = false }
                            )
                        }
                    }
                }

                // Подпись
                OutlinedTextField(
                    value = caption,
                    onValueChange = { caption = it },
                    label = { Text("Подпись (необязательно)") },
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth()
                )

                // Ошибка
                if (uploadState is PortfolioUiState.Error) {
                    Text(
                        text = uploadState.message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    selectedUri?.let { uri ->
                        onUpload(uri, selectedService?.id, caption.trim())
                    }
                },
                enabled = selectedUri != null && uploadState !is PortfolioUiState.Loading
            ) {
                if (uploadState is PortfolioUiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Text("Загрузить")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        }
    )
}
