# ============================================================
#  WellFlow ProGuard 설정
# ============================================================

# ── 스택 트레이스 가독성 유지 ────────────────────────────────────────────────
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ── Kotlin ───────────────────────────────────────────────────────────────────
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**
-keepclassmembers class **$WhenMappings { <fields>; }

# ── Hilt ─────────────────────────────────────────────────────────────────────
-keep class dagger.hilt.** { *; }
-keep @dagger.hilt.android.lifecycle.HiltViewModel class * { *; }
-keepclasseswithmembers class * {
    @dagger.hilt.android.lifecycle.HiltViewModel <init>(...);
}
-keep class * extends dagger.hilt.android.internal.managers.** { *; }

# ── Room ─────────────────────────────────────────────────────────────────────
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-keep class * extends androidx.room.migration.Migration { *; }
-keepclassmembers class * extends androidx.room.RoomDatabase {
    abstract *** *Dao();
}

# ── Coroutines ───────────────────────────────────────────────────────────────
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

# ── Gson (Drive 백업 직렬화 대비) ─────────────────────────────────────────────
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory { *; }
-keep class * implements com.google.gson.JsonSerializer { *; }
-keep class * implements com.google.gson.JsonDeserializer { *; }

# ── 도메인 모델 (Gson 직렬화 대상) ───────────────────────────────────────────
-keep class com.forlks.personal_wellness_routine.domain.model.** { *; }
-keep class com.forlks.personal_wellness_routine.data.db.entity.** { *; }

# ── Credential Manager / Google Sign-In ─────────────────────────────────────
-keep class com.google.android.libraries.identity.googleid.** { *; }
-keep class androidx.credentials.** { *; }
-dontwarn androidx.credentials.**

# ── Firebase ─────────────────────────────────────────────────────────────────
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

# ── AdMob ────────────────────────────────────────────────────────────────────
-keep public class com.google.android.gms.ads.** { public *; }
-dontwarn com.google.android.gms.ads.**

# ── Compose ──────────────────────────────────────────────────────────────────
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# ── Navigation ───────────────────────────────────────────────────────────────
-keepnames class androidx.navigation.** { *; }

# ── DataStore ────────────────────────────────────────────────────────────────
-keep class androidx.datastore.** { *; }
-dontwarn androidx.datastore.**

# ── Lottie ───────────────────────────────────────────────────────────────────
-dontwarn com.airbnb.lottie.**
