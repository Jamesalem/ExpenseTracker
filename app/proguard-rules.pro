# --- General Android & Kotlin ---
-keepattributes *Annotation*, SourceFile, LineNumberTable
-renamesourcefileattribute SourceFile

# --- Hilt / Dagger ---
-keep class dagger.hilt.android.internal.managers.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$ViewComponentBuilder { *; }

# --- Room ---
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# --- Kotlin Serialization ---
-keepattributes *Annotation*, Signature
-keepclassmembers class ** {
    @kotlinx.serialization.SerialName <fields>;
}

# --- MPAndroidChart ---
-keep class com.github.mikephil.charting.** { *; }
-dontwarn com.github.mikephil.charting.**

# --- Timber ---
-keep class timber.log.** { *; }

# --- MPAndroidChart ---
-dontwarn com.github.mikephil.charting.**
-keep class com.github.mikephil.charting.** { *; }
