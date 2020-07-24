package com.interpark.smframework.base.types

import com.interpark.smframework.IDirector
import com.interpark.smframework.base.SMView

open class Sequence(director:IDirector) : ActionInterval(director) {
    protected val _actions:ArrayList<FiniteTimeAction?> = ArrayList(2)
    protected var _split:Float = 0f
    protected var _last:Int = 0

    init {
        _actions[0] = null
        _actions[1] = null
    }


    companion object {

        @JvmStatic
        fun create(director: IDirector, vararg actions: FiniteTimeAction?): Sequence? {
            val action1:FiniteTimeAction = actions[0]!!
            var prev:FiniteTimeAction? = action1

            return if (actions.size==1) createWithTwoActions(director, prev, ExtraAction.create(director)) else {
                for (i in 0 until actions.size-1) {
                    val now:FiniteTimeAction? = actions[i]
                    prev = createWithTwoActions(director, prev, now)
                }
                (prev as Sequence)
            }
        }

        @JvmStatic
        fun create(director: IDirector, actions: ArrayList<FiniteTimeAction>): Sequence? {
            val action1:FiniteTimeAction = actions[0]
            var prev:FiniteTimeAction? = action1

            var currentIndex:Int = 0
            return if (actions.size==1) createWithTwoActions(director, prev, ExtraAction.create(director)) else {
                for (i in 0 until  actions.size-1) {
                    val now:FiniteTimeAction? = actions[i]
                    prev = createWithTwoActions(director, prev, now)
                }
                (prev as Sequence)
            }
        }

        @JvmStatic
        fun createWithTwoActions(director: IDirector, action1: FiniteTimeAction?, action2: FiniteTimeAction?):Sequence? {
            val seq:Sequence = Sequence(director)
            if (seq.initWithTwoActions(action1, action2)) {
                return seq
            }
            return null
        }

    }

    override fun Clone(): ActionInterval? {
        return if (_actions[0]!=null && _actions[1]!=null) Sequence.create(getDirector(), _actions[0]!!, _actions[1]!!) else null
    }

    override fun reverse(): ActionInterval? {
        return if (_actions[0]!=null && _actions[1]!=null) Sequence.createWithTwoActions(getDirector(), _actions[1], _actions[0]) else null
    }

    override fun startWithTarget(target: SMView?) {
        if (target==null) return

        if (_actions[0]==null || _actions[1]==null) return

        if (_duration> EPSILON) _split = if (_actions[0]!!.getDuration() > EPSILON) _actions[0]!!.getDuration()/_duration else 0f

        super.startWithTarget(target)
        _last = -1
    }

    override fun stop() {
        if (_last!=-1 && _actions[_last]!=null) _actions[_last]!!.stop()

        super.stop()
    }

    override fun update(t: Float) {
        var found:Int
        var new_t:Float

        if (t<_split) {
            found = 0
            new_t = if (_split!=0f) t /_split else t
        } else {
            found = 1
            new_t = if (_split==1f) 1f else (t-_split) / (1f-_split)
        }

        if (found==1) {
            if (_last==-1) {
                _actions[0]!!.startWithTarget(_target)
                _actions[0]!!.update(1f)
                _actions[0]!!.stop()
            } else if (_last==0) {
                _actions[0]!!.update(1f)
                _actions[0]!!.stop()
            }
        } else if (found==0 && _last==1) {
            _actions[1]!!.update(0f)
            _actions[1]!!.stop()
        }

        if (found==_last && _actions[found]!!.isDone()) return

        if (found!=_last) _actions[found]!!.startWithTarget(_target)

        _actions[found]!!.update(new_t)
        _last = found
    }

    protected fun initWithTwoActions(action1: FiniteTimeAction?, action2: FiniteTimeAction?):Boolean {
        if (action1==null || action2==null) return false

        val d:Float = action1.getDuration()+action2.getDuration()
        super.initWithDuration(d)

        _actions[0] = action1
        _actions[1] = action2

        return true
    }
}