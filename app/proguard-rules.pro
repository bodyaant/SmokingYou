# Preserve line number information and generic signatures for debugging and reflection
-keepattributes SourceFile,LineNumberTable,*Annotation*,Signature,InnerClasses,EnclosingMethod

# Keep classes and members annotated with @Keep
-keep class * {
    @androidx.annotation.Keep <fields>;
    @androidx.annotation.Keep <methods>;
    @androidx.annotation.Keep <init>(...);
}
-keep @androidx.annotation.Keep class * { *; }

# Keep classes and constructors with Gson @SerializedName fields (R8 full mode safe)
-keepclasseswithmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
    <init>(...);
}

# Explicitly keep GitHub API and Backup models for Gson reflection
-keep class com.smokingtracker.data.manager.GitHubRelease { *; }
-keep class com.smokingtracker.data.manager.GitHubAsset { *; }
-keep class com.smokingtracker.BackupManager$BackupData { *; }
-keep class com.smokingtracker.BackupManager$BackupEntry { *; }

# Keep Gson TypeToken subclasses and their generic type parameters
-keep class * extends com.google.gson.reflect.TypeToken
-keepclassmembers class * extends com.google.gson.reflect.TypeToken { *; }

# Keep Room Database and Entity classes
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class * { *; }

# Keep Enums resolved dynamically by name via valueOf(String)
-keep class com.smokingtracker.data.preferences.ThemePreference { *; }
-keep class com.smokingtracker.data.preferences.FontPreset { *; }
-keep class com.smokingtracker.data.preferences.ColorPreset { *; }
-keep class com.smokingtracker.data.preferences.AppIconPreset { *; }
-keep class com.smokingtracker.data.TriggerType { *; }

# Keep AppWidgetProviders
-keep class com.smokingtracker.widget.QuickAddWidgetProvider { <init>(); }
-keep class com.smokingtracker.widget.TimerWidgetProvider { <init>(); }
