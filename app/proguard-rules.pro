-optimizations code/*,method/*,field/*,class/*,library/*,library/gson,class/marking/final,class/unboxing/enum,class/merging/vertical,class/merging/horizontal,class/merging/wrapper,field/removal/writeonly,code/removal/advanced,field/marking/private,field/propagation/value,method/marking/private,method/marking/static,method/marking/final,method/marking/synchronized,method/removal/parameter,method/propagation/parameter,code/simplification/advanced,method/propagation/returnvalue,method/inlining/short,method/inlining/unique,method/inlining/tailrecursion,code/merging,code/simplification/variable,code/simplification/arithmetic,code/simplification/cast,code/simplification/field,code/simplification/branch,code/simplification/object,code/simplification/string,code/simplification/math,code/simplification/advanced,code/removal/advanced,code/removal/simple,code/removal/variable,code/removal/exception,code/allocation/variable
-optimizationpasses 10
-allowaccessmodification
-repackageclasses androidx

-keep class **.models.* { *; }
-keep interface **.models.* { *; }

-keep class **.EntryAlbum { *; }
-keep interface **.EntryAlbum { *; }

-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}

-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface retrofit2.Call
-keep,allowobfuscation class retrofit2.Response
-keep,allowobfuscation class kotlin.coroutines.Continuation
-keepclassmembers,allowshrinking,allowobfuscation interface * { @retrofit2.http.* <methods>; }

-keepnames class * implements java.io.Serializable
-keepnames class * implements android.os.Parcelable
-keepnames interface * implements java.io.Serializable
-keepnames interface * implements android.os.Parcelable

-keep class * extends android.webkit.WebChromeClient { *; }
-keep interface * extends android.webkit.WebChromeClient { *; }

-keep class sun.misc.Unsafe.* { *; }
-keep class sun.misc.Unsafe.** { *; }
-keepclasseswithmembernames,includedescriptorclasses class * { native <methods>; }

-keep class net.zetetic.** { *; }
-keep interface net.zetetic.** { *; }
-keep,includedescriptorclasses class net.zetetic.** { *; }
-keep,includedescriptorclasses interface net.zetetic.** { *; }
-keep class net.sqlcipher.** { *; }
-keep interface net.sqlcipher.** { *; }
-keep,includedescriptorclasses class net.sqlcipher.** { *; }
-keep,includedescriptorclasses interface net.sqlcipher.** { *; }

-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

-keepattributes Annotation,Exceptions,LineNumberTable
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepattributes AnnotationDefault

-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-dontwarn javax.annotation.**
-dontwarn kotlin.Unit
-dontwarn retrofit2.KotlinExtensions
-dontwarn android.media.LoudnessCodecController
-dontwarn android.media.LoudnessCodecController$OnLoudnessCodecUpdateListener
-dontwarn sun.misc.Unsafe

-verbose
-printseeds tmp/s/full-seed.txt
-printmapping tmp/m/full-map.txt
-printconfiguration tmp/r/full-r8-config.txt
