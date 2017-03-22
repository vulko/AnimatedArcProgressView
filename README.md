# Animated Progress Views

Hello and welcome.
This is my first open source project that is targeting fellow developers that are looking for something working and simple to animate waiting progress rather than using android default one.


Usage
======
This view is simple as it is and the use of it can't be any simpler. You don't need to call any extra methods for it to work.

Usage is very simple. Just add this to your *layout.xml*:
```xml
        <com.kvolkov.animatedprogressviews.AnimatedArcIndefiniteProgressView
            android:id="@+id/progress0"
            android:layout_gravity="center"
            android:layout_width="match_parent"
            android:layout_height="300dp"
            android:layout_margin="50dp"
            app:defaultColor="@color/colorAccent"
            app:arcCount="10"
            app:arcSpacing="10.0"
            app:arcStrokeWidth="30.0"
            app:progressAnimation="ProgressAnimationType.RACE_CONDITION"/>
```

Might need to provide this for container layout to resolve *app* scheme. Or use custom scheme instead, if you are using *app* scheme already.
```
  xmlns:app="http://schemas.android.com/apk/libs/com.kvolkov.animatedprogressviews.AnimatedArcIndefiniteProgressView"
```

Any view size is respected. There are several parameters to experiment with:
 - **defaultColor** Main color of the view. Opacity is supported.
 - **arcCount** Number of arcs to be drawn. So far algorithm is a bit tricky for some of setups. Try to compile and run demo app to play with settings to see how it works. Basically in case this number increases arcs are added from edges in direction of center. *This is done to support usage of this view when only **arcCount = 1** so this view might look the same as one from Android SDK :)*
 - **arcSpacing** Spacing between arcs.
 - **arcStrokeWidth** Width or stroke, or thickness of arcs.
 - **progressAnimation** Animation type. Should be one of these:
    ```xml
            <enum name="ProgressAnimationType.OPACITY_ANIMATION_TEST_STUB" value="-1" />
            <enum name="ProgressAnimationType.RACE_CONDITION" value="0" />
            <enum name="ProgressAnimationType.SWIRLY" value="1" />
            <enum name="ProgressAnimationType.WHIRPOOL" value="2" />
            <enum name="ProgressAnimationType.HYPERLOOP" value="3" />
            <enum name="ProgressAnimationType.METRONOME_1" value="4" />
            <enum name="ProgressAnimationType.METRONOME_2" value="5" />
            <enum name="ProgressAnimationType.METRONOME_3" value="6" />
            <enum name="ProgressAnimationType.METRONOME_4" value="7" />
            <enum name="ProgressAnimationType.BUTTERFLY_KNIFE" value="8" />
            <enum name="ProgressAnimationType.RAINBOW" value="9" />
            <enum name="ProgressAnimationType.GOTCHA" value="10" />
    ```

  **Note** so far some other API's are already provided by the view itself, but not supported by custom attributes yet. Please stay tuned with this project, cause there will be some more feature development, and also the plan is to add some other animated views that look different from this one, but still are cool :)


**Demo App sample video**
======
Please take a look at a video of *demo app* working. TODO: upload some screenshots :)
https://youtu.be/AVs89qdmeOE


**Import to your project**
======
You can download an **aar** here: https://dl.bintray.com/vulko/AnimatedArcProgressView/com/kvolkov/animatedprogressviews/library/1.0/

Or use Gradle:
```gradle
repositories {
    jcenter()
}

dependencies {
    classpath ('com.kvolkov.animatedprogressviews:library:1.0') {
        // this in case you already have these import's in your project, otherwise skip it
        exclude group: 'com.android.support', module: 'appcompat-v7'
        exclude group: 'com.android.support', module: 'support-annotations'
    }
}
```


ProGuard
------
Please let me know if you bump in any issues with library and proguard. So far the only dependency is appcompat, so try to exclude it to avoid any proguard issues.


Contributing
------
If you are interested in contributing to this repository, please don't hesitate to contact me.


Author
------
Kirill Volkov


License
------
Apache 2.0
