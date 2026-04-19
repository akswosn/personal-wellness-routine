package com.forlks.personal_wellness_routine.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.forlks.personal_wellness_routine.BuildConfig
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

/** 배너 높이 — AdMob Standard Banner: 320×50dp */
val AD_BANNER_HEIGHT = 50.dp

/**
 * AdMob 배너 광고 컴포넌트
 *
 * - Debug   (ADS_ENABLED=false): 회색 플레이스홀더 영역 표시 (레이아웃 공간 확보)
 * - Release (ADS_ENABLED=true) : 실제 AdView (BANNER 사이즈) 표시
 *
 * 사용처에서 Scaffold contentPadding 하단에 [AD_BANNER_HEIGHT]를 추가해야
 * 콘텐츠가 배너에 가려지지 않습니다.
 */
@Composable
fun AdBannerView(modifier: Modifier = Modifier) {
    if (!BuildConfig.ADS_ENABLED) {
        // Debug: 광고 영역 플레이스홀더 (레이아웃 검증용)
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(AD_BANNER_HEIGHT)
                .background(Color.Gray.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "광고 영역",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
        }
        return
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(AD_BANNER_HEIGHT)
            .background(Color.Black.copy(alpha = 0.03f))  // 미세 구분선 효과
    ) {
        AndroidView(
            modifier = Modifier.fillMaxWidth(),
            factory = { context ->
                AdView(context).apply {
                    setAdSize(AdSize.BANNER)
                    adUnitId = BuildConfig.BANNER_AD_UNIT_ID
                    loadAd(AdRequest.Builder().build())
                }
            }
        )
    }
}
