package com.vulkanalia.aranews.ui

import android.content.Context
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vulkanalia.aranews.data.NewsItem
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsScreen(viewModel: NewsViewModel) {
    val newsList by viewModel.newsList.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val lastUpdate by viewModel.lastUpdate.collectAsState()
    val context = LocalContext.current

    var showInfoDialog by remember { mutableStateOf(false) }
    var selectedNews by remember { mutableStateOf<NewsItem?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Noticias Admon. Local Aragón",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        lastUpdate?.let {
                            Text(
                                text = "Actualizado: $it",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 10.sp
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { showInfoDialog = true }) {
                        Icon(Icons.Default.Info, contentDescription = "Información")
                    }
                    IconButton(onClick = { viewModel.refreshNews() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refrescar")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            SearchBar(
                query = searchQuery,
                onQueryChange = { viewModel.onSearchQueryChange(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            if (isRefreshing && newsList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (newsList.isEmpty()) {
                EmptyState(query = searchQuery)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(newsList) { news ->
                        NewsCard(
                            news = news,
                            searchQuery = searchQuery,
                            onCardClick = { selectedNews = news },
                            onUrlClick = { url -> openUrl(context, url) }
                        )
                    }
                }
            }
        }
    }

    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            title = { Text("Información") },
            text = {
                Text("Datos obtenidos a partir de los datos abiertos de Aragón Open Data sobre 'Noticias provenientes de entidades públicas de administración local de Aragón'")
            },
            confirmButton = {
                TextButton(onClick = { showInfoDialog = false }) {
                    Text("Cerrar")
                }
            }
        )
    }

    selectedNews?.let { news ->
        AlertDialog(
            onDismissRequest = { selectedNews = null },
            title = {
                Text(
                    text = buildAnnotatedStringWithHighlight(news.titulo ?: "Sin título", searchQuery),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        text = buildAnnotatedStringWithHighlight(news.noticia ?: "", searchQuery),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Justify
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Publicado: ${formatDate(news.fGrabacion)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedNews = null }) {
                    Text("Cerrar")
                }
            }
        )
    }
}

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier,
        placeholder = { Text("Buscar noticias...") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Clear, contentDescription = "Limpiar")
                }
            }
        },
        singleLine = true,
        shape = MaterialTheme.shapes.medium,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline
        )
    )
}

@Composable
fun NewsCard(
    news: NewsItem,
    searchQuery: String,
    onCardClick: () -> Unit,
    onUrlClick: (String) -> Unit
) {
    val hasUrl = !news.url.isNullOrBlank()
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = buildAnnotatedStringWithHighlight(news.titulo ?: "Sin título", searchQuery),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                if (hasUrl) {
                    IconButton(
                        onClick = { news.url?.let { onUrlClick(it) } },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.Link,
                            contentDescription = "Abrir enlace",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = buildAnnotatedStringWithHighlight(news.noticia ?: "", searchQuery),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 6,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Justify
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Publicado: ${formatDate(news.fGrabacion)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
fun EmptyState(query: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Info,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.outline
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = if (query.isEmpty()) "No hay noticias disponibles" else "No se encontraron resultados para \"$query\"",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.outline,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
fun buildAnnotatedStringWithHighlight(text: String, query: String): androidx.compose.ui.text.AnnotatedString {
    if (query.isEmpty() || !text.contains(query, ignoreCase = true)) {
        return buildAnnotatedString { append(text) }
    }

    val annotatedString = buildAnnotatedString {
        var currentIndex = 0
        val lowerText = text.lowercase()
        val lowerQuery = query.lowercase()

        while (currentIndex < text.length) {
            val nextIndex = lowerText.indexOf(lowerQuery, currentIndex)
            if (nextIndex == -1) {
                append(text.substring(currentIndex))
                break
            } else {
                append(text.substring(currentIndex, nextIndex))
                withStyle(style = SpanStyle(background = Color.Yellow.copy(alpha = 0.5f), fontWeight = FontWeight.ExtraBold)) {
                    append(text.substring(nextIndex, nextIndex + query.length))
                }
                currentIndex = nextIndex + query.length
            }
        }
    }
    return annotatedString
}

fun formatDate(dateString: String?): String {
    if (dateString == null) return "Fecha desconocida"
    return try {
        val inputFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
        val outputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val date = LocalDateTime.parse(dateString, inputFormatter)
        date.format(outputFormatter)
    } catch (e: Exception) {
        dateString
    }
}

fun openUrl(context: Context, url: String) {
    try {
        val intent = CustomTabsIntent.Builder().build()
        intent.launchUrl(context, Uri.parse(url))
    } catch (e: Exception) {
        // Fallback to generic intent if Custom Tabs fails
    }
}
