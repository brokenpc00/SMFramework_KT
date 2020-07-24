package com.interpark.smframework.base.types

class tHashUpdateEntry {

    constructor() {
        list = ArrayList()
        entry = null
        target = null
        callback = null
    }

    var list:ArrayList<tListEntry?>? = ArrayList()
    var entry: tListEntry? = null
    var target: Ref? = null
    var callback: SEL_SCHEDULE? = null
}