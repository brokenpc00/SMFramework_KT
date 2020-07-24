package com.interpark.smframework.base.types

import com.interpark.smframework.IDirector

open class Ref : Cloneable {
    constructor(director: IDirector) {
        _director = director
    }


    open fun getDirector(): IDirector {
        return _director!!
    }

    protected var _director: IDirector? = director
}