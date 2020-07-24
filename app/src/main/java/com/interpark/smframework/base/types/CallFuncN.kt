package com.interpark.smframework.base.types

import com.interpark.smframework.IDirector

class CallFuncN(director:IDirector) : CallFunc(director) {

    protected var _functionN:PERFORM_SEL_N? = null

    companion object {
        @JvmStatic
        fun create(director: IDirector, funcN: PERFORM_SEL_N):CallFuncN {
            val ret:CallFuncN = CallFuncN(director)
            ret.initWithFunction(funcN)
            return ret
        }
    }

    override fun execute() {
        _functionN?.performSelector(_target)
    }

    override fun Clone(): CallFunc? {
        val funcN:CallFuncN = CallFuncN(getDirector())
        if (_target!=null) {
            funcN.initWithFunction(_functionN)
        }

        return funcN
    }

    private fun initWithFunction(funcN: PERFORM_SEL_N?): Boolean {
        _functionN = funcN
        return true
    }
}