package com.interpark.smframework.base.types

import com.interpark.smframework.IDirector

open class CallFunc(director:IDirector) : ActionInstant(director) {
    protected var _function:PERFORM_SEL? = null

    companion object {
        @JvmStatic
        fun create(director: IDirector, func: PERFORM_SEL):CallFunc? {
            val action:CallFunc = CallFunc(director)
            if (action.initWithFunction(func)) {
                return action
            }

            return null
        }
    }

    open fun execute() {
        _function?.performSelector()
    }

    override fun update(dt: Float) {
        super.update(dt)
        execute()
    }

    override fun reverse(): CallFunc? {
        return Clone()
    }

    override fun Clone(): CallFunc? {
        val a:CallFunc = CallFunc(getDirector())
        if (_function!=null) {
            a.initWithFunction(_function!!)
        }

        return a
    }

    private fun initWithFunction(func: PERFORM_SEL):Boolean {
        _function = func
        return true
    }
}