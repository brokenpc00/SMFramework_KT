package com.brokenpc.smframework.base.types

import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.SMView

open class Action(director: IDirector) : Ref(director) {

    companion object {
        val INVALID_TAG:Int = -1
    }

    protected var _originalTarget:SMView? = null
    protected var _target:SMView? = null
    protected var _tag:Int = INVALID_TAG
    protected var _flags:Long = 0


    open fun Clone():Action? {return null}
    open fun reverse():Action? {return null}
    open fun isDone():Boolean {return true}
    open fun startWithTarget(target: SMView?) {
        _originalTarget = target
        _target = target
    }
    open fun stop() {_target=null}
    open fun step(dt:Float) {}
    open fun update(dt: Float) {}

    open fun getTarget():SMView? {return _target}
    open fun setTarget(target: SMView) {_target=target}
    open fun getOriginTarget():Ref? {return _originalTarget}

    open fun getTag():Int {return _tag}
    open fun setTag(tag:Int) {_tag=tag}

    open fun getFlags():Long {return _flags}
    open fun setFlags(flags: Long) {_flags=flags}
}