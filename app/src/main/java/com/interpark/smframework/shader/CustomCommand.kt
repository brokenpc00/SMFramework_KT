package com.interpark.smframework.shader

class CustomCommand() : RenderCommand() {
    var customCommandFunc: CustomCommandFunc? = null

    init {
        _type = Type.CUSTOM_COMMAND
    }

    override fun init(globalZOrder: Float, modelViewTransform: FloatArray, flags: Long) {
        _globalOrder = globalZOrder
        super.init(_globalOrder, modelViewTransform, flags)
    }

    fun init(globalOrder: Float) {
        _globalOrder = globalOrder
    }

    fun execute() {
        if (customCommandFunc != null) {
            customCommandFunc!!.func()
        }
    }

    fun isTranslucent(): Boolean {
        return true
    }

    interface CustomCommandFunc {
        fun func()
    }

}