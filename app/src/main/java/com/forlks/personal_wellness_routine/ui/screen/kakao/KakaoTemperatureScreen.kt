package com.forlks.personal_wellness_routine.ui.screen.kakao

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.forlks.personal_wellness_routine.ui.theme.TempCold
import com.forlks.personal_wellness_routine.ui.theme.TempCool
import com.forlks.personal_wellness_routine.ui.theme.TempNormal
import com.forlks.personal_wellness_routine.ui.theme.TempWarm
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KakaoTemperatureScreen(
    analysisId: Long,
    onNext: (Long) -> Unit,
    onBack: () -> Unit,
    viewModel: KakaoViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(analysisId) {
        viewModel.loadAnalysis(analysisId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🌡 대화 온도계") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "뒤로가기"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        val analysis = uiState.currentAnalysis

        if (analysis == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "분석 데이터를 불러오는 중...",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            return@Scaffold
        }

        val temperature = analysis.temperature
        val temperatureColor = when {
            temperature >= 65f -> TempWarm
            temperature >= 50f -> TempNormal
            temperature >= 35f -> TempCool
            else -> TempCold
        }
        val temperatureLabelText = when {
            temperature >= 65f -> "따뜻함"
            temperature >= 50f -> "보통"
            temperature >= 35f -> "차가움"
            else -> "냉랭함"
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Large temperature display
            Text(
                text = "${temperature.roundToInt()}°",
                fontSize = 72.sp,
                fontWeight = FontWeight.Bold,
                color = temperatureColor
            )
            Text(
                text = temperatureLabelText,
                style = MaterialTheme.typography.titleLarge,
                color = temperatureColor,
                fontWeight = FontWeight.Medium
            )

            // Visual thermometer
            ThermometerCanvas(
                temperature = temperature,
                color = temperatureColor,
                modifier = Modifier
                    .width(60.dp)
                    .height(200.dp)
            )

            // Temperature stages
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "온도 단계",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    val stages = listOf(
                        Triple("냉랭함", "20°", temperature < 35f),
                        Triple("차가움", "35°", temperature in 35f..49.9f),
                        Triple("보통", "50°", temperature in 50f..64.9f),
                        Triple("따뜻함", "65°", temperature >= 65f)
                    )
                    stages.forEach { (label, threshold, isCurrentStage) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isCurrentStage) FontWeight.Bold else FontWeight.Normal,
                                color = if (isCurrentStage) temperatureColor
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = if (isCurrentStage) "← 현재" else threshold,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isCurrentStage) temperatureColor
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontStyle = if (isCurrentStage) FontStyle.Italic else FontStyle.Normal
                            )
                        }
                    }
                }
            }

            // Recommendation card
            val recommendation = when {
                temperature >= 65f -> "💚 긍정적인 대화가 많았어요! '감사 일기' 루틴을 추천드려요"
                temperature >= 50f -> "'명상 5분' 루틴을 추천드려요"
                temperature >= 35f -> "'산책 30분' 루틴을 추천드려요"
                else -> "'친구에게 연락하기' 루틴을 추천드려요"
            }
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = temperatureColor.copy(alpha = 0.1f)
                )
            ) {
                Text(
                    text = recommendation,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Next button
            Button(
                onClick = { onNext(analysisId) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("다음 →")
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ThermometerCanvas(
    temperature: Float,
    color: Color,
    modifier: Modifier = Modifier
) {
    val filledRatio = (temperature / 100f).coerceIn(0f, 1f)
    val tickTemps = listOf(35f, 50f, 65f)
    val grayColor = Color(0xFFBDBDBD)
    val textColor = Color(0xFF666666)

    Canvas(modifier = modifier) {
        val barWidth = size.width * 0.4f
        val barLeft = (size.width - barWidth) / 2f
        val barTop = 0f
        val barBottom = size.height
        val barHeight = barBottom - barTop

        // Background bar
        drawRoundRect(
            color = grayColor,
            topLeft = Offset(barLeft, barTop),
            size = Size(barWidth, barHeight),
            cornerRadius = CornerRadius(barWidth / 2f)
        )

        // Filled portion (from bottom up)
        val filledHeight = barHeight * filledRatio
        val filledTop = barBottom - filledHeight
        if (filledHeight > 0f) {
            drawRoundRect(
                color = color,
                topLeft = Offset(barLeft, filledTop),
                size = Size(barWidth, filledHeight),
                cornerRadius = CornerRadius(barWidth / 2f)
            )
        }

        // Tick marks at 35°, 50°, 65°
        tickTemps.forEach { tickTemp ->
            val tickRatio = (tickTemp / 100f).coerceIn(0f, 1f)
            val tickY = barBottom - barHeight * tickRatio
            drawLine(
                color = textColor,
                start = Offset(barLeft + barWidth, tickY),
                end = Offset(barLeft + barWidth + 8.dp.toPx(), tickY),
                strokeWidth = 1.dp.toPx()
            )
        }
    }
}
