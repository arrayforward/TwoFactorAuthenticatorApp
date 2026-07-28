# Add project specific ProGuard rules here.
-keepattributes *Annotation*

# Tink + Security Crypto
-dontwarn com.google.errorprone.annotations.**
-dontwarn javax.annotation.**
-dontwarn javax.annotation.concurrent.**
-keep class com.google.crypto.tink.** { *; }