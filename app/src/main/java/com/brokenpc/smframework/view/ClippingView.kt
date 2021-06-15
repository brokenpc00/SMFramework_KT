package com.brokenpc.smframework.view

import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.SMView
import com.brokenpc.smframework.base.types.Mat4

class ClippingView(director: IDirector) : SMView(director) {

    protected var _stencil: SMView? = null
    protected

    companion object {
        fun create(): ClippingView? {
            return create(null)
        }

        fun create(stencil: SMView?): ClippingView? {
            return null
        }
    }

    fun getStencil(): SMView? {
        return null
    }

    fun setStencil(stencil: SMView?) {

    }

    open fun hasContent(): Boolean {
        return getStencil()!=null
    }

    fun getAlphaThreshold(): Float {
        return 1f
    }

    fun setAlphaThreshold(alphaThreshold: Float) {

    }

    fun isInverted(): Boolean {
        return false
    }

    fun setInverted(inverted: Boolean) {

    }

    override fun onEnter() {
        super.onEnter()
    }

    override fun onEnterTransitionDidFinish() {
        super.onEnterTransitionDidFinish()
    }

    override fun onExitTransitionDidStart() {
        super.onExitTransitionDidStart()
    }

    override fun onExit() {
        super.onExit()
    }

    override fun visit(parentTransform: Mat4, parentFlags: Int) {
        super.visit(parentTransform, parentFlags)
    }

    override fun init(): Boolean {
        return super.init()
    }

    open fun init(stencil: SMView?): Boolean {
        return init()
    }
}