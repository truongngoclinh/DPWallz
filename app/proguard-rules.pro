# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in E:\sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

-keep class * implements com.bumptech.glide.module.GlideModule
#-keepresourcexmlelements manifest/application/meta-data@value=GlideModule

#-keep class * extends android.support.design.widget.CoordinatorLayout@Behavior
#
#-keepclassmembers class * extends android.support.design.widget.CoordinatorLayout@Behavior {
# public <init>(android.content.Context, android.util.AttributeSet);
#}

#-keep class * extends android.support.design.widget.AppBarLayout.ScrollingViewBehavior
#
#-keepclassmembers class * extends android.support.design.widget.AppBarLayout.ScrollingViewBehavior {
# public <init>(android.content.Context, android.util.AttributeSet);
#}
#

-keep class * extends android.support.design.widget.CoordinatorLayout.Behavior { *; }
-keep class * extends android.support.design.widget.ViewOffsetBehavior { *; }
-keep class * extends android.support.design.widget.AppBarLayout.ScrollingViewBehavior { *; }
-keep class com.dpanic.dpwallz.util.ConstrainedScrollBehavior
-keepclassmembers class com.dpanic.dpwallz.util.ConstrainedScrollBehavior

-keepclassmembers class * implements android.os.Parcelable {
   public static final android.os.Parcelable$Creator *;
}


# for DexGuard only
#-keepresourcexmlelements manifest/application/meta-data@value=GlideModule

-dontwarn okio.**

-dontwarn sun.misc.Unsafe
-dontwarn org.w3c.dom.bootstrap.DOMImplementationRegistry

-keepattributes *Annotation*
-keepclassmembers class ** {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }

-keep class rx.schedulers.Schedulers {
    public static <methods>;
}
-keep class rx.schedulers.ImmediateScheduler {
    public <methods>;
}
-keep class rx.schedulers.TestScheduler {
    public <methods>;
}
-keep class rx.schedulers.Schedulers {
    public static ** test();
}

-keepclassmembers class rx.internal.util.unsafe.*ArrayQueue*Field* {
    long producerIndex;
    long consumerIndex;
}
-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueProducerNodeRef {
    long producerNode;
    long consumerNode;
}

-dontwarn android.support.**
-keep class android.support.v7.widget.SearchView { *; }

-keep public class org.jsoup.** {
public *;
}