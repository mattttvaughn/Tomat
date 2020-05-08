# Daily app day #3: Tomat: Pomodoro Clock

A pomodoro clock with a focus on a e s t h e t i c s. Features adjustable

![Demo picture](./demo/demo.png)

Things I learned:

 - CountDownTimer is not great at counting, likes to skip from 2 to 0
 - I ought to look into using bindService() instead of LocalBroadcastManager
 - Leaving unused resources in the build.gradle increases app size (obvious I guess)
    - presumably proguard would fix this in release builds
 - (Learned but didn't implement because of time):
     - Enums are serializable
     - {Enum}.valueOf({enumInstance}.toString)) appears to be an alternative to serializing
