package com.interpark.smframework.base.types

class tHashElement {
    constructor() {

    }

    var actions:ArrayList<Action?> = ArrayList()
    var target:Ref? = null
    var actionIndex:Int = 0
    var currentAction:Action? = null
    var currentActionSalvaged:Boolean = false
    var paused:Boolean = false
}