package com.forlks.personal_wellness_routine

import android.app.Application
import com.google.android.gms.ads.MobileAds
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class WellFlowApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // 운영 빌드에서만 AdMob 초기화
        // Debug 빌드(BuildConfig.ADS_ENABLED=false)에서는 SDK 자체를 초기화하지 않아
        // 불필요한 네트워크 요청 및 광고 추적을 완전히 차단합니다.
        if (BuildConfig.ADS_ENABLED) {
            MobileAds.initialize(this)
        }
    }
}
