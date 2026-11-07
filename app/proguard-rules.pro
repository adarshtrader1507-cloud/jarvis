# Keep Room entities/DAOs and Retrofit/Gson model classes — reflection-based libs break silently
# under R8 without these, which is exactly the kind of "silent fake behavior" we're avoiding.
-keep class com.jarvis.assistant.data.local.entity.** { *; }
-keep class com.jarvis.assistant.data.remote.** { *; }
-keepattributes Signature, *Annotation*
-dontwarn okhttp3.**
-dontwarn retrofit2.**
