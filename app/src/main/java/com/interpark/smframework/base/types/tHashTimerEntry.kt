package com.brokenpc.smframework.base.types

class tHashTimerEntry {
    constructor() {
        timers = ArrayList()
        target = null
        timerIndex = 0
        currentTimer = null
        paused = false
    }

    var timers:ArrayList<TimerTargetSelector>? = null
    var target:Ref? = null
    open var timerIndex:Int = 0
    var currentTimer: Timer? = null
    var paused:Boolean = false

}