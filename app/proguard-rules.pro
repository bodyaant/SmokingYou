# Preserve line number information for debugging stack traces
-keepattributes SourceFile,LineNumberTable, *Annotation*, Signature, InnerClasses, EnclosingMethod

# Keep classes and members annotated with @Keep
-keep class * {
    @androidx.annotation.Keep <fields>;
    @androidx.annotation.Keep <methods>;
}
-keep @androidx.annotation.Keep class * { *; }


# Keep Glance SerializedName fields
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Keep AppWidgetProviders
-keep class com.smokingtracker.widget.** { *; }
-keepclassmembers class com.smokingtracker.widget.** { *; }