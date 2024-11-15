# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-repackageclasses ''
-allowaccessmodification
-optimizations !code/simplification/arithmetic
#,!field/*,!class/merging/*

-keepattributes *Annotation*
-keepattributes Signature
-keepattributes EnclosingMethod
-keep class com.google.gson.stream.** { *; }

-dontwarn android.support.**
-dontwarn javax.management.**
-dontwarn org.apache.**
-dontwarn org.slf4j.**
-dontwarn java.lang.management.**

-keep,allowobfuscation class model.** { *; }
-keep,allowobfuscation class model.base.** { *; }
-keep,allowobfuscation class model.enum.** { *; }
-keep,allowobfuscation class network.model.** { *; }
-keep class com.app.development.winter.model.** { *; }
-keep class com.app.development.winter.shared.network.** { *; }
-keep class com.app.development.winter.shared.base.**{*;}

## Retrolambda specific rules ##
# as per official recommendation: https://github.com/evant/gradle-retrolambda#proguard
-dontwarn java.lang.invoke.*

-dontwarn sun.misc.**

-dontwarn rx.**
##############

-dontwarn com.boingo.**
-dontwarn com.google.android.maps.**
-keep class javax.**  { *; }
-keep class org.apache.**  { *; }
-keep class org.slf4j.** { *; }
-keep class twitter4j.**  { *; }
-keep class java.lang.management.**  { *; }
-keep class com.google.code.**  { *; }
-keep class oauth.signpost.**  { *; }
-keep class com.github.saiakhil90.** { *; }
-keep class de.jkeylockmanager.** {*;}
-keep public class com.espian.flyin.**
-keep public class org.jsoup.**
-keep public class org.openid4java.**
-keep class org.brickred.** { *; }
-dontwarn org.brickred.**



-keep class com.google.gson.** {
   *;
}

-dontwarn java.beans.**

-dontwarn com.fasterxml.jackson.**
-dontwarn com.damnhandy.uri.template.jackson.**

-keep public class android.util.FloatMath { *; }

-keep class com.google.firebase.** { *; }

-keep public class com.google.android.gms.* { public *; }
-keep class com.google.android.gms.** { *; }
-keep class com.google.android.gms.ads.** { *; }
-keep class com.google.android.gms.ads.identifier.** { *; }
-dontwarn okio.**
-keep class org.sqlite.**  { *; }

-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
    public static *** w(...);
    public static *** e(...);
}

-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Retrofit does reflection on generic parameters. InnerClasses is required to use Signature and
# EnclosingMethod is required to use InnerClasses.
-keepattributes Signature, InnerClasses, EnclosingMethod

# Retrofit does reflection on method and parameter annotations.
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations

# Retain service method parameters when optimizing.
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# Application classes that will be serialized/deserialized over Gson
-keep class com.google.gson.examples.android.model.** { <fields>; }

# Prevent R8 from leaving Data object members always null
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}
#-------------------------RETROFIT-------------------------------------------
# Platform calls Class.forName on types which do not exist on Android to determine platform.
-dontnote retrofit2.Platform
# Retain generic type information for use by reflection by converters and adapters.
-keepattributes Signature
# Retain declared checked exceptions for use by a Proxy instance.
-keepattributes Exceptions

-dontwarn com.squareup.okhttp3.**
-keep class com.squareup.okhttp3.* { *;}
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# https://github.com/square/okhttp/blob/339732e3a1b78be5d792860109047f68a011b5eb/okhttp/src/jvmMain/resources/META-INF/proguard/okhttp3.pro#L11-L14
-dontwarn okhttp3.internal.platform.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# TODO: Waiting for new retrofit release to remove these rules
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

##---------------Begin: proguard configuration for Gson  ----------
# Gson uses generic type information stored in a class file when working with fields. Proguard
# removes such information by default, so configure it to keep all of it.
-keepattributes Signature

# For using GSON @Expose annotation
-keepattributes *Annotation*

# Gson specific classes
-dontwarn sun.misc.**
#-keep class com.google.gson.stream.** { *; }

# Application classes that will be serialized/deserialized over Gson
-keep class com.google.gson.examples.android.model.** { <fields>; }

# Prevent proguard from stripping interface information from TypeAdapter, TypeAdapterFactory,
# JsonSerializer, JsonDeserializer instances (so they can be used in @JsonAdapter)
-keep class * extends com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Prevent R8 from leaving Data object members always null
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}

# Retain generic signatures of TypeToken and its subclasses with R8 version 3.0 and higher.
-keep,allowobfuscation,allowshrinking class com.google.gson.reflect.TypeToken
-keep,allowobfuscation,allowshrinking class * extends com.google.gson.reflect.TypeToken



#-------------------------GLIDE-------------------------------------------
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule {
 <init>(...);
}
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}
-keep class com.bumptech.glide.load.data.ParcelFileDescriptorRewinder$InternalRewinder {
  *** rewind();
}

#-------------------------FACEBOOK AUTH-------------------------------------------
-keepnames class com.facebook.FacebookActivity
-keepnames class com.facebook.CustomTabActivity
-keep class com.facebook.login.Login

##-------------------------Google AUTH-------------------------------------------
-keep class com.google.android.apps.authenticator.** {*;}
-keep class com.google.android.gms.auth.api.identity.** {*;}
-keep class com.google.googlesignin.** { *; }
-keepnames class com.google.googlesignin.* { *; }
-keep class com.google.android.gms.auth.** { *; }
-dontwarn com.google.android.gms.**
-keep class com.google.** {*;}

#-------------------------BRANCH-------------------------------------------
# In case you are using Facebook SDK to support deep linking through Facebook ads, please make sure to keep the Facebook SDK classes in proguard```bash
# -keep class com.facebook.applinks.** { *; }
# -keepclassmembers class com.facebook.applinks.** { *; }
#-keep class com.facebook.FacebookSdk { *; }

-keepattributes SourceFile,LineNumberTable        # Keep file names and line numbers.
-keep public class * extends java.lang.Exception  # Optional: Keep custom exceptions.

#---------------------Lottie-----------------------------------------------
-dontwarn com.airbnb.lottie.**
-keep class com.airbnb.lottie.** {*;}

-keep class kotlinx.coroutines.android.AndroidExceptionPreHandler
-keep class kotlinx.coroutines.android.AndroidDispatcherFactory
-keep class kotlinx.coroutines.**
-dontwarn kotlinx.coroutines.debug.**

#---------------Coroutines ---------------------------------------------
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepnames class kotlinx.coroutines.android.AndroidExceptionPreHandler {}
-keepnames class kotlinx.coroutines.android.AndroidDispatcherFactory {}

-keep class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keep class kotlinx.coroutines.CoroutineExceptionHandler {}
-keep class kotlinx.coroutines.android.AndroidExceptionPreHandler {}
-keep class kotlinx.coroutines.android.AndroidDispatcherFactory {}

# Most of volatile fields are updated with AFU and should not be mangled
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

-keepattributes *Annotation*
-keepclassmembers enum androidx.lifecycle.Lifecycle$Event {
    <fields>;
}
-keep !interface * implements androidx.lifecycle.LifecycleObserver {
}
-keep class * implements androidx.lifecycle.GeneratedAdapter {
    <init>(...);
}
-keepclassmembers class ** {
    @androidx.lifecycle.OnLifecycleEvent *;
}
# this rule is need to work properly when legendary is compiled with api 28, see b/142778206
# Also this rule prevents registerIn from being inlined.
-keepclassmembers class androidx.lifecycle.ReportFragment$LifecycleCallbacks { *; }

##--------------------TAPJOY-------------------------------------------
-keep class com.tapjoy.** { *; }
-keep class com.moat.** { *; }
-keepattributes JavascriptInterface
-keepattributes *Annotation*
-keep class * extends java.util.ListResourceBundle {
protected Object[][] getContents();
}
-keep public class com.google.android.gms.common.internal.safeparcel.SafeParcelable {
public static final *** NULL;
}
-keepnames @com.google.android.gms.common.annotation.KeepName class *
-keepclassmembernames class * {
@com.google.android.gms.common.annotation.KeepName *;
}
-keepnames class * implements android.os.Parcelable {
public static final ** CREATOR;
}
-keep class com.google.android.gms.ads.identifier.** { *; }
-dontwarn com.tapjoy.**
-keep class io.fiverocks.** { *; }

##------------------------MINTEGRAL---------------------------------------
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.mbridge.** {*; }
-keep interface com.mbridge.** {*; }
-keep interface androidx.** { *; }
-keep class androidx.** { *; }
-keep public class * extends androidx.** { *; }
-dontwarn com.mbridge.**
-keep class **.R$* { public static final int mbridge*; }

##------------------------PANGLE---------------------------------------
 -keep class com.bytedance.sdk.** { *; }
 -keep class com.pgl.sys.ces.* {*;}

-keep class com.adjust.sdk.** { *; }
-keep class com.google.android.gms.common.ConnectionResult {
    int SUCCESS;
}
-keep class com.google.android.gms.ads.identifier.AdvertisingIdClient {
    com.google.android.gms.ads.identifier.AdvertisingIdClient$Info getAdvertisingIdInfo(android.content.Context);
}
-keep class com.google.android.gms.ads.identifier.AdvertisingIdClient$Info {
    java.lang.String getId();
    boolean isLimitAdTrackingEnabled();
}
-keep public class com.android.installreferrer.** { *; }

##-----------------------Ayet----------------------------------------
-keep class com.ayetstudios.publishersdk.messages.** {*;}
-keep public class com.ayetstudios.publishersdk.AyetSdk
-keepclassmembers class com.ayetstudios.publishersdk.AyetSdk {
   public *;
}
-keep public interface com.ayetstudios.publishersdk.interfaces.UserBalanceCallback { *; }
-keep public interface com.ayetstudios.publishersdk.interfaces.DeductUserBalanceCallback { *; }

-keep class com.ayetstudios.publishersdk.models.VastTagReqData { *; }
#---------------------PollFish------------------------------------------
-dontwarn com.pollfish.**
-keep class com.pollfish.** { *; }

##---------------------------------------------------------------
-keep class theoremreach.com.** { *; }
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.mbridge.** {*; }
-keep interface com.mbridge.** {*; }
-keep interface androidx.** { *; }
-keep class androidx.** { *; }
-keep public class * extends androidx.** { *; }
-dontwarn com.mbridge.**
-keep class **.R$* { public static final int mbridge*; }

##----Adscend----------------------------------------------------
-dontwarn java.nio.file.*
-dontwarn com.squareup.okhttp.**
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement

-keepattributes Signature
-keepattributes Exceptions

-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.stream.** { *; }
-keep class com.adscendmedia.sdk.rest.model.** { *; }
-keep class com.adscendmedia.sdk.rest.response.** { *; }

##------Rapido-Reach--------------------------------------------------
-keep class rapidoreach.com.** { *; }

##-----Theorem-Reach--------------------------------------------------
 -keep class theoremreach.com.** { *; }
##---PollFish---------------------------------------------------------
-dontwarn com.pollfish.**
-keep class com.pollfish.** { *; }
##---TapResearch---------------------------------------------------------
-keep class com.tapr.** { *; }
-keep interface com.tapr.** { *; }
-keep class * extends androidx.databinding.DataBinderMapper { *; }

-keepclassmembers class * extends androidx.datastore.preferences.protobuf.GeneratedMessageLite {<fields>;}

-keep class androidx.startup.AppInitializer
-keep class * extends androidx.startup.Initializer

-keepnames class * extends androidx.startup.Initializer
# These Proguard rules ensures that ComponentInitializers are are neither shrunk nor obfuscated,
# and are a part of the primary dex file. This is because they are discovered and instantiated
# during application startup.
-keep class * extends androidx.startup.Initializer {
    # Keep the public no-argument constructor while allowing other methods to be optimized.
    <init>();
}
-keep class com.github.mikephil.charting.** { *; }
-dontobfuscate
