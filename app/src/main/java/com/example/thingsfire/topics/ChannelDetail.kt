package com.example.thingsfire.topics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.thingsfire.DataModels.Channel
import com.example.thingsfire.DataModels.Feed
import com.example.thingsfire.DataModels.FeedX
import com.example.thingsfire.DataModels.labelForField
import com.example.thingsfire.DataModels.valueForField
import com.example.thingsfire.ViewModels.ChannelUiState
import com.example.thingsfire.ViewModels.ChannelViewModel
import com.example.thingsfire.ViewModels.FieldChartUiState
import com.example.thingsfire.ViewModels.FieldChartViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val timestampFormatter = DateTimeFormatter.ofPattern("dd MMM, hh:mm a")

@Composable
fun ChannelDetailScreen(
    channelId: String,
    onBack: () -> Unit,
    onFieldClick: (Int) -> Unit
) {
    val viewModel: ChannelViewModel = viewModel(
        factory = ChannelViewModel.Factory(channelId = channelId)
    )

    ChannelScreenScaffold(
        title = "Channel $channelId",
        onBack = onBack
    ) { modifier ->
        ChannelDetailBody(
            modifier = modifier,
            uiState = viewModel.uiState,
            onRetry = viewModel::refresh,
            onFieldClick = onFieldClick
        )
    }
}

@Composable
fun FieldChartScreen(
    channelId: String,
    fieldNumber: Int,
    onBack: () -> Unit
) {
    val viewModel: FieldChartViewModel = viewModel(
        factory = FieldChartViewModel.Factory(
            channelId = channelId,
            fieldNumber = fieldNumber
        )
    )

    val channelName = viewModel.uiState.fieldFeed?.channel?.labelForField(fieldNumber) ?: "Field $fieldNumber"

    ChannelScreenScaffold(
        title = channelName,
        onBack = onBack
    ) { modifier ->
        FieldChartBody(
            modifier = modifier,
            fieldNumber = fieldNumber,
            uiState = viewModel.uiState,
            onRetry = viewModel::refresh
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChannelScreenScaffold(
    title: String,
    onBack: () -> Unit,
    content: @Composable (Modifier) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text(text = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        content(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        )
    }
}

@Composable
private fun ChannelDetailBody(
    modifier: Modifier,
    uiState: ChannelUiState,
    onRetry: () -> Unit,
    onFieldClick: (Int) -> Unit
) {
    when {
        uiState.isLoading -> LoadingState(modifier)
        uiState.errorMessage != null -> ErrorState(
            modifier = modifier,
            message = uiState.errorMessage,
            onRetry = onRetry
        )
        uiState.channelFeed != null -> {
            val fieldSummaries = remember(uiState.channelFeed) {
                uiState.channelFeed.toFieldSummaries()
            }

            Column(
                modifier = modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ChannelOverviewCard(feed = uiState.channelFeed)

                if (fieldSummaries.isEmpty()) {
                    EmptyState(
                        title = "No field values yet",
                        subtitle = "This channel has no data in the last 7 days."
                    )
                } else {
                    Text(
                        text = "Available Fields",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    fieldSummaries.forEach { summary ->
                        FieldValueCard(
                            summary = summary,
                            onClick = { onFieldClick(summary.fieldNumber) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FieldChartBody(
    modifier: Modifier,
    fieldNumber: Int,
    uiState: FieldChartUiState,
    onRetry: () -> Unit
) {
    when {
        uiState.isLoading -> LoadingState(modifier)
        uiState.errorMessage != null -> ErrorState(
            modifier = modifier,
            message = uiState.errorMessage,
            onRetry = onRetry
        )
        uiState.fieldFeed != null -> {
            val fieldLabel = uiState.fieldFeed.channel.labelForField(fieldNumber)
            val chartPoints = remember(uiState.fieldFeed, fieldNumber) {
                uiState.fieldFeed.feeds.mapNotNull { feedItem ->
                    feedItem.valueForField(fieldNumber)?.toFloatOrNull()?.let { numericValue ->
                        ChartPoint(
                            value = numericValue,
                            valueLabel = feedItem.valueForField(fieldNumber).orEmpty(),
                            createdAt = feedItem.created_at
                        )
                    }
                }
            }
            val recentValues = remember(uiState.fieldFeed, fieldNumber) {
                uiState.fieldFeed.feeds
                    .asReversed()
                    .mapNotNull { feedItem ->
                        feedItem.valueForField(fieldNumber)?.let { value ->
                            FieldReading(
                                value = value,
                                createdAt = feedItem.created_at
                            )
                        }
                    }
                    .take(12)
            }

            Column(
                modifier = modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ChannelFieldHeader(
                    title = fieldLabel,
                    subtitle = "Showing values from the last 7 days"
                )

                if (chartPoints.isNotEmpty()) {
                    ChartSummaryCard(points = chartPoints)
                    SimpleLineChart(
                        points = chartPoints,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                    )
                } else {
                    EmptyState(
                        title = "Chart unavailable",
                        subtitle = "This field has no numeric values to draw yet."
                    )
                }

                if (recentValues.isNotEmpty()) {
                    RecentValuesCard(readings = recentValues)
                }
            }
        }
    }
}

@Composable
private fun ChannelOverviewCard(feed: Feed) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = feed.channel.name ?: "Unnamed Channel",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Channel ID: ${feed.channel.id ?: "-"}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Updated: ${formatTimestamp(feed.channel.updated_at)}",
                style = MaterialTheme.typography.bodyMedium
            )
            feed.channel.description?.takeIf { it.isNotBlank() }?.let { description ->
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun FieldValueCard(
    summary: FieldSummary,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = summary.label,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = summary.latestValue,
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = "Last updated ${formatTimestamp(summary.updatedAt)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
private fun ChannelFieldHeader(
    title: String,
    subtitle: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline
        )
    }
}

@Composable
private fun ChartSummaryCard(points: List<ChartPoint>) {
    val latestPoint = points.last()
    val minValue = points.minOf { it.value }
    val maxValue = points.maxOf { it.value }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            SummaryMetric(label = "Latest", value = latestPoint.valueLabel)
            SummaryMetric(label = "Min", value = minValue.toString())
            SummaryMetric(label = "Max", value = maxValue.toString())
        }
    }
}

@Composable
private fun SummaryMetric(
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.outline
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun SimpleLineChart(
    points: List<ChartPoint>,
    modifier: Modifier = Modifier
) {
    val lineColor = MaterialTheme.colorScheme.primary
    val gridColor = MaterialTheme.colorScheme.outlineVariant

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            if (points.isEmpty()) return@Canvas

            val values = points.map { it.value }
            val minValue = values.minOrNull() ?: return@Canvas
            val maxValue = values.maxOrNull() ?: return@Canvas
            val valueRange = (maxValue - minValue).takeIf { it > 0f } ?: 1f
            val stepX = if (points.size == 1) 0f else size.width / (points.size - 1)

            for (index in 0..3) {
                val y = size.height * index / 3f
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1.dp.toPx()
                )
            }

            val linePath = Path()
            val chartOffsets = points.mapIndexed { index, point ->
                val normalizedValue = (point.value - minValue) / valueRange
                val x = stepX * index
                val y = size.height - (normalizedValue * size.height)
                Offset(x, y)
            }

            chartOffsets.forEachIndexed { index, offset ->
                if (index == 0) {
                    linePath.moveTo(offset.x, offset.y)
                } else {
                    linePath.lineTo(offset.x, offset.y)
                }
            }

            drawPath(
                path = linePath,
                color = lineColor,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )

            chartOffsets.forEach { offset ->
                drawCircle(
                    color = lineColor,
                    radius = 4.dp.toPx(),
                    center = offset
                )
            }

            drawRect(
                color = Color.Transparent,
                topLeft = Offset.Zero,
                size = Size(size.width, size.height)
            )
        }
    }
}

@Composable
private fun RecentValuesCard(readings: List<FieldReading>) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Recent Readings",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            readings.forEachIndexed { index, reading ->
                if (index > 0) {
                    HorizontalDivider()
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = reading.value,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = formatTimestamp(reading.createdAt),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingState(modifier: Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorState(
    modifier: Modifier,
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error
            )
            Button(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}

@Composable
private fun EmptyState(
    title: String,
    subtitle: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

private fun Feed.toFieldSummaries(): List<FieldSummary> {
    return buildList {
        for (fieldNumber in 1..8) {
            val latestReading = feeds.lastOrNull { entry ->
                entry.valueForField(fieldNumber) != null
            } ?: continue

            add(
                FieldSummary(
                    fieldNumber = fieldNumber,
                    label = channel.labelForField(fieldNumber),
                    latestValue = latestReading.valueForField(fieldNumber).orEmpty(),
                    updatedAt = latestReading.created_at
                )
            )
        }
    }
}

private fun formatTimestamp(rawTimestamp: String?): String {
    if (rawTimestamp.isNullOrBlank()) return "-"

    return runCatching {
        Instant.parse(rawTimestamp)
            .atZone(ZoneId.systemDefault())
            .format(timestampFormatter)
    }.getOrElse { rawTimestamp }
}

private data class FieldSummary(
    val fieldNumber: Int,
    val label: String,
    val latestValue: String,
    val updatedAt: String
)

private data class ChartPoint(
    val value: Float,
    val valueLabel: String,
    val createdAt: String
)

private data class FieldReading(
    val value: String,
    val createdAt: String
)
