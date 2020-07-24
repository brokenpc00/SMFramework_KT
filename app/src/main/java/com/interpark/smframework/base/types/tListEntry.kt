package com.interpark.smframework.base.types

class tListEntry {
    constructor() {
        callback = null
        target = null
        priority = 0
        paused = false
        markedForDeletion = false
    }

    var callback:SEL_SCHEDULE? = null
    var target:Ref? = null
    var priority:Int = 0
    var paused:Boolean = false
    var markedForDeletion: Boolean = false
}