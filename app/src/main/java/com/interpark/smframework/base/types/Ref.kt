package com.brokenpc.smframework.base.types

import com.brokenpc.smframework.IDirector

open class Ref(director: IDirector) : Cloneable {
    protected var _director: IDirector? = director


    open fun getDirector(): IDirector {
        return _director!!
    }
}