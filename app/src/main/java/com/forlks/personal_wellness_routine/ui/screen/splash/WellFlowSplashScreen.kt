package com.forlks.personal_wellness_routine.ui.screen.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * 전체 화면 Compose 애니메이션 스플래시
 *
 * 시퀀스 (총 ~2700ms):
 *  100ms  — 배경 빛 서서히 등장
 *  350ms  — 물방울이 위에서 낙하 (Spring 바운스)
 *  750ms  — 충돌 → 3개 리플 파동 + 파티클 상승
 *  950ms  — 앱 이름 페이드 인
 * 2700ms  — onDone() 호출
 */
@Composable
fun WellFlowSplashScreen(onDone: () -> Unit) {

    // ── 색상 팔레트 ──────────────────────────────────────────────────────────
    val colorBg     = Color(0xFF0D1A0F)
    val colorMid    = Color(0xFF2A5035)
    val colorSage   = Color(0xFF5E8B63)
    val colorSageSoft = Color(0xFF8FB48F)
    val colorDrop   = Color(0xFFF0FAF0)

    // ── 페이즈 타이밍 ─────────────────────────────────────────────────────────
    var phase by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        delay(100);  phase = 1   // 배경 빛
        delay(250);  phase = 2   // 물방울 낙하
        delay(400);  phase = 3   // 리플 + 파티클
        delay(200);  phase = 4   // 텍스트
        delay(1800)
        onDone()                 // 총 ≈ 2750ms
    }

    // ── 애니메이션 값 ─────────────────────────────────────────────────────────

    // 배경 글로우 알파
    val glowAlpha by animateFloatAsState(
        targetValue = if (phase >= 1) 1f else 0f,
        animationSpec = tween(700),
        label = "glow"
    )

    // 물방울 Y 오프셋: -1.4f(화면 위) → 0f(중심)
    val dropOffset by animateFloatAsState(
        targetValue = if (phase >= 2) 0f else -1.4f,
        animationSpec = spring(dampingRatio = 0.52f, stiffness = 210f),
        label = "drop"
    )

    // 리플 3개 (타이밍 엇갈림)
    val r1 by animateFloatAsState(
        targetValue = if (phase >= 3) 1f else 0f,
        animationSpec = tween(850, easing = FastOutSlowInEasing), label = "r1"
    )
    val r2 by animateFloatAsState(
        targetValue = if (phase >= 3) 1f else 0f,
        animationSpec = tween(1050, delayMillis = 90, easing = FastOutSlowInEasing), label = "r2"
    )
    val r3 by animateFloatAsState(
        targetValue = if (phase >= 3) 1f else 0f,
        animationSpec = tween(1250, delayMillis = 180, easing = FastOutSlowInEasing), label = "r3"
    )

    // 파티클 상승 (0 → 1 = 최상단)
    val particles by animateFloatAsState(
        targetValue = if (phase >= 3) 1f else 0f,
        animationSpec = tween(1700, easing = LinearEasing), label = "parts"
    )

    // 텍스트 알파 + 위로 슬라이드
    val textAlpha by animateFloatAsState(
        targetValue = if (phase >= 4) 1f else 0f,
        animationSpec = tween(700), label = "ta"
    )
    val textSlide by animateFloatAsState(
        targetValue = if (phase >= 4) 0f else 22f,
        animationSpec = tween(700, easing = FastOutSlowInEasing), label = "ts"
    )

    // 미세 숨쉬기 글로우 (루프)
    val breathe by rememberInfiniteTransition(label = "br").animateFloat(
        initialValue = 0.78f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "bv"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorBg)
    ) {
        // ── 메인 Canvas ───────────────────────────────────────────────────────
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx   = size.width / 2f
            val baseCy = size.height * 0.40f
            val dropCy = baseCy + dropOffset * size.height * 0.42f

            // ① 방사형 배경 글로우
            drawRect(
                brush = Brush.radialGradient(
                    colorStops = arrayOf(
                        0.00f to colorMid.copy(alpha = 0.88f * glowAlpha * breathe),
                        0.35f to Color(0xFF1A3520).copy(alpha = 0.55f * glowAlpha),
                        0.70f to Color(0xFF111D13).copy(alpha = 0.25f * glowAlpha),
                        1.00f to colorBg.copy(alpha = 0f)
                    ),
                    center = Offset(cx, baseCy),
                    radius = size.width * 1.05f
                )
            )

            // ② 대각선 빛줄기 (속도감)
            val sa = glowAlpha * 0.095f
            drawLine(Color.White.copy(alpha = sa * 1.6f),
                Offset(-size.width * 0.05f, size.height * 0.68f),
                Offset(size.width * 0.78f, -size.height * 0.04f),
                strokeWidth = size.width * 0.20f)
            drawLine(Color.White.copy(alpha = sa * 0.8f),
                Offset(size.width * 0.18f, size.height * 1.0f),
                Offset(size.width * 1.05f, size.height * 0.18f),
                strokeWidth = size.width * 0.11f)
            drawLine(Color.White.copy(alpha = sa * 0.4f),
                Offset(size.width * 0.52f, size.height * 1.0f),
                Offset(size.width * 1.15f, size.height * 0.52f),
                strokeWidth = size.width * 0.06f)

            // ③ 에너지 오빗 링 (배경 장식)
            val ringAlpha = glowAlpha * 0.22f * breathe
            drawCircle(colorSage.copy(alpha = ringAlpha),
                radius = size.width * 0.46f, center = Offset(cx, baseCy),
                style = Stroke(width = 1.2f))
            drawCircle(colorSage.copy(alpha = ringAlpha * 0.6f),
                radius = size.width * 0.33f, center = Offset(cx, baseCy),
                style = Stroke(width = 0.8f))

            // ④ 리플 파동 3개
            val maxR = size.width * 0.60f
            listOf(r1 to 0.55f, r2 to 0.40f, r3 to 0.28f).forEach { (prog, opMul) ->
                if (prog > 0f) {
                    drawCircle(
                        color  = colorSage.copy(alpha = (1f - prog) * opMul),
                        radius = maxR * prog,
                        center = Offset(cx, dropCy),
                        style  = Stroke(width = 5f * (1f - prog) + 1f)
                    )
                }
            }

            // ⑤ 상승 파티클 8개
            if (particles > 0f) {
                data class P(val dx: Float, val sp: Float, val r: Float, val op: Float)
                listOf(
                    P(-0.32f, 1.00f, 6.0f, 0.80f),
                    P(-0.14f, 0.80f, 4.0f, 0.65f),
                    P( 0.09f, 0.94f, 5.0f, 0.75f),
                    P( 0.28f, 0.85f, 3.5f, 0.60f),
                    P(-0.22f, 0.62f, 3.0f, 0.50f),
                    P( 0.40f, 0.72f, 4.5f, 0.68f),
                    P(-0.07f, 1.12f, 2.5f, 0.44f),
                    P( 0.18f, 0.55f, 3.0f, 0.54f)
                ).forEach { p ->
                    val pp = (particles * p.sp).coerceIn(0f, 1f)
                    val pY = dropCy - pp * size.height * 0.44f
                    val pA = (1f - pp * 0.85f) * particles * p.op
                    if (pA > 0f) {
                        drawCircle(
                            color  = colorDrop.copy(alpha = pA),
                            radius = p.r,
                            center = Offset(cx + p.dx * size.width * 0.55f, pY)
                        )
                    }
                }
            }

            // ⑥ 물방울 (teardrop — 위가 뾰족)
            val dropR = size.width * 0.118f
            val tipY  = dropCy - dropR * 1.80f
            val botY  = dropCy + dropR * 0.72f

            val dropPath = Path().apply {
                moveTo(cx, tipY)
                cubicTo(cx + dropR * 0.42f, tipY + dropR * 0.55f,
                    cx + dropR, dropCy - dropR * 0.18f,
                    cx + dropR, dropCy)
                cubicTo(cx + dropR, botY,
                    cx - dropR, botY,
                    cx - dropR, dropCy)
                cubicTo(cx - dropR, dropCy - dropR * 0.18f,
                    cx - dropR * 0.42f, tipY + dropR * 0.55f,
                    cx, tipY)
                close()
            }
            drawPath(
                dropPath,
                brush = Brush.verticalGradient(
                    colors = listOf(Color.White, Color(0xFFD0EDD0), colorSage),
                    startY = tipY, endY = botY
                )
            )
            // 내부 반사 하이라이트
            drawLine(
                Color.White.copy(alpha = 0.48f),
                start  = Offset(cx - dropR * 0.36f, tipY + dropR * 0.40f),
                end    = Offset(cx - dropR * 0.28f, dropCy - dropR * 0.08f),
                strokeWidth = dropR * 0.24f,
                cap = StrokeCap.Round
            )
            // 뾰족한 끝 작은 반사 점
            drawCircle(
                Color.White.copy(alpha = 0.72f),
                radius = dropR * 0.17f,
                center = Offset(cx + dropR * 0.20f, tipY + dropR * 0.28f)
            )
        }

        // ── 텍스트 (아래 영역) ─────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 90.dp)
                .offset(y = textSlide.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "마음흐름",
                color = Color.White.copy(alpha = textAlpha),
                fontSize = 28.sp,
                fontWeight = FontWeight.Light,
                letterSpacing = 7.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "WellFlow",
                color = colorSageSoft.copy(alpha = textAlpha * 0.82f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                letterSpacing = 4.sp
            )
        }
    }
}
