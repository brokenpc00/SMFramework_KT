package com.brokenpc.smframework.base.types

import com.brokenpc.smframework.IDirector

open class Ref(director: IDirector) {
    protected var _director: IDirector? = director

    open fun Clone() : Ref? {return null}

    open fun getDirector(): IDirector {
        return _director!!
    }
}