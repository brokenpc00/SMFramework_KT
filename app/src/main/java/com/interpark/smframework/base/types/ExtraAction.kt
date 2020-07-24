package com.interpark.smframework.base.types

import com.interpark.smframework.IDirector

class ExtraAction(director: IDirector) : FiniteTimeAction(director) {

    companion object {
        @JvmStatic
        fun create(director: IDirector): ExtraAction {
            return ExtraAction(director)
        }
    }

    override fun Clone(): FiniteTimeAction? {
        return ExtraAction.create(getDirector())
    }

    override fun reverse(): FiniteTimeAction? {
        return ExtraAction.create(getDirector())
    }

    override fun update(dt: Float) {
        // nothing to do
    }

    override fun step(dt: Float) {
        // nothing to do
    }

}