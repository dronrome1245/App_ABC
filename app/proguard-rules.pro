# App_ABC release hardening rules.
# AndroidX libraries also ship consumer rules; these explicit rules protect the
# app-owned reflection/annotation boundaries requested for the release build.

# Room database, entities and DAOs.
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-keepclassmembers class * {
    @androidx.room.TypeConverter <methods>;
}

# Compose annotated members that may be reached through framework/runtime paths.
-keepclassmembers class * {
    @androidx.compose.runtime.Composable <methods>;
}

# Preferences DataStore is small in this app and is kept conservatively for the
# persisted settings boundary.
-keep class androidx.datastore.preferences.** { *; }
