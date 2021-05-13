package com.interpark.smframework.base.types

import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.SMView
import com.brokenpc.smframework.base.types.*

class Spawn(director: IDirector) : ActionInterval(director) {
    protected var _one:FiniteTimeAction? = null
    protected var _two:FiniteTimeAction? = null

    companion object {
        @JvmStatic
        fun create(director: IDirector, vararg actions: FiniteTimeAction?): Spawn? {
            if (actions.isEmpty()) return null

            var ret:Spawn? = null
            var now: FiniteTimeAction? = null
            var action1: FiniteTimeAction? = null
            action1 = actions[0]
            var prev = action1

            return if (actions.size==1) {
                createWithTwoActions(director, prev, ExtraAction.create(director))
            } else {
                for (i in 1 until actions.size-1) {
                    now = actions[i]
                    prev = createWithTwoActions(director, prev, now)
                }
                prev as Spawn?
            }
        }

        @JvmStatic
        fun createWithTwoActions(director: IDirector, action1: FiniteTimeAction?, action2: FiniteTimeAction?): Spawn? {
            val seq = Spawn(director)
            if (seq.initWithTwoActions(action1, action2)) {
                return seq
            }
            return null
        }

        @JvmStatic
        fun createWithVariableList(director: IDirector, actions: ArrayList<FiniteTimeAction>): Spawn? {
            var ret: Spawn? = null

            if (actions.size==0) return null

            var now: FiniteTimeAction? = null
            var action1: FiniteTimeAction
            var prev: FiniteTimeAction? = actions[0].also { action1 = it }

            var currentIndex: Int
            if (actions.size==1) {
                ret = createWithTwoActions(director, prev, ExtraAction.create(director))
                return ret
            } else {
                for (i in 1 until actions.size-1) {
                    now = actions[i]
                    prev = createWithTwoActions(director, prev, now)
                }

                return prev as Spawn
            }
        }
    }

    protected fun init(arrayOfActions: ArrayList<FiniteTimeAction>): Boolean {
        val count = arrayOfActions.size
        if (count==0) return false

        if (count==1) {
            return initWithTwoActions(arrayOfActions[0], ExtraAction.create(getDirector()))
        }

        var prev:FiniteTimeAction? = arrayOfActions[0]

        for (i in 1 until count-1) {
            prev = createWithTwoActions(getDirector(), prev, arrayOfActions[i])
        }

        return initWithTwoActions(prev, arrayOfActions[count-1])
    }

    protected fun initWithTwoActions(action1: FiniteTimeAction?, action2: FiniteTimeAction?): Boolean {
        var ret = false

        if (action1==null || action2==null) return false

        val d1 = action1.getDuration()
        val d2 = action2.getDuration()

        if (super.initWithDuration(d1.coerceAtLeast(d2))) {
            _one = action1
            _two = action2

            if (d1 > d2) {
                _two = Sequence.createWithTwoActions(getDirector(), action2, DelayTime.create(getDirector(), d1 - d2))
            } else {
                _one = Sequence.createWithTwoActions(getDirector(), action1, DelayTime.create(getDirector(), d2 - d1))
            }

            ret = true
        }

        return ret
    }

    override fun Clone(): ActionInterval? {
        if (_one!=null && _two!=null) {
            return Spawn.createWithTwoActions(getDirector(), _one!!.Clone(), _two!!.Clone())
        } else {
            return null
        }
    }

    override fun startWithTarget(target: SMView?) {
        if (target==null || _one==null || _two==null) return

        super.startWithTarget(target)
        _one!!.startWithTarget(target)
        _two!!.startWithTarget(target)
    }

    override fun stop() {
        _one?.stop()
        _two?.stop()
        super.stop()
    }

    override fun update(dt: Float) {
        _one?.update(dt)
        _two?.update(dt)
    }

    override fun reverse(): ActionInterval? {
        if (_one!=null && _two!=null) {
            return Spawn.createWithTwoActions(getDirector(), _one!!.reverse(), _two!!.reverse())
        }
        return null
    }
}