# R8 rules for the release build (on top of proguard-android-optimize.txt and the
# consumer rules shipped by AndroidX/Compose).
#
# Kept automatically, no rule needed: everything named in AndroidManifest.xml (MainActivity,
# the five AppWidgetProviders, BadiAgendaWidgetService, WidgetHourlyUpdateReceiver) and the
# resources referenced from the widget layouts/xml.

# Readable crash stack traces in Play Console. The mapping file travels inside the .aab,
# so Play de-obfuscates class/method names; this keeps the line numbers.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Settings are stored in SharedPreferences as enum constant names and read back with
# valueOf() (BadiViewModel, WidgetDataManager). Keep these enums intact so values saved by
# earlier releases keep resolving after an update.
-keep enum com.example.badi.TimeSystemMode { *; }
-keep enum com.example.ui.theme.AppThemeMode { *; }
-keep enum com.example.ui.YearNotationSystem { *; }

# BadiViewModel is an AndroidViewModel built by the default factory, which looks up the
# (Application) constructor reflectively. Lifecycle's consumer rules cover this; kept here
# explicitly so it doesn't hinge on a library version.
-keepclassmembers class * extends androidx.lifecycle.AndroidViewModel {
    <init>(android.app.Application);
}
