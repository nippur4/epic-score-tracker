# kotlinx.serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class **$$serializer { *; }
-keepclasseswithmembers class com.epichypernova.scoretracker.data.model.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.epichypernova.scoretracker.**$$serializer { *; }
-keepclassmembers class com.epichypernova.scoretracker.data.model.** {
    *** Companion;
}
