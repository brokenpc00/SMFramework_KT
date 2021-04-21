package com.brokenpc.smframework.base.types

import android.util.SparseArray
import androidx.core.util.valueIterator
import com.brokenpc.smframework_kt.BuildConfig
import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.SMView

open class ActionManager(director: IDirector) : Ref(director) {

    companion object {

    }

    protected var _targets:SparseArray<tHashElement> = SparseArray()
    protected var _currentTarget:tHashElement? = null
    protected var _currentTargetSalvaged:Boolean = false

    protected fun findHashElement(target: Ref): tHashElement? {
        return _targets.get(target.hashCode())
    }

    fun addAction(action: Action?, target: Ref?, paused: Boolean) {
        if (action==null || target==null) return

        var element = findHashElement(target)

        if (element == null) {
            element = tHashElement()
            element.paused = paused
            element.target = target
            element.actions = ArrayList()
            _targets.remove(target.hashCode())
        }

        if (BuildConfig.DEBUG && element.actions.contains(action)) {
            error("Assertion failed")
        }

        element.actionIndex = element.actions.size
        element.actions.add(action)

        _targets.put(target.hashCode(), element)

        action.startWithTarget(target as SMView)
    }

    fun removeAllActions() {
        for (i in 0 until _targets.size()) {
            val element = _targets[i]
            if (element.target!=null) {
                removeallActionsFromTarget(element.target)
            }
        }
        _targets.clear()
    }

    fun removeallActionsFromTarget(target: Ref?) {

        if (target==null) return

        val element = _targets.get(target.hashCode())
        if (element!=null) {
            if (element.actions.contains(element.currentAction)) {
                element.currentActionSalvaged = true
            }

            element.actions.clear()
            if (_currentTarget==element) {
                _currentTargetSalvaged = true
            } else {
                deleteHashElement(element)
            }
        }
    }

    fun deleteHashElement(element: tHashElement) {
        element.actions.clear()
        _targets.remove(element.target.hashCode())
    }

    fun removeAction(action: Action?) {
        if (action==null) return

        val target = action.getOriginTarget()!!
        val element = findHashElement(target)
        if (element!=null) {
            val index = element.actions.indexOf(action)
            if (index!=-1) {
                removeActionAtIndex(index, element)
            }
        }
    }

    fun removeActionAtIndex(index:Int, element:tHashElement):tHashElement {
        val action = element.actions[index]
        if (action!=null && action==element.currentAction) {
            element.currentActionSalvaged = true
        }

        element.actions.removeAt(index)

        if (element.actionIndex >= index) {
            element.actionIndex--
        }

        if (element.actions.size==0) {
            if (_currentTarget==element) {
                _currentTargetSalvaged = true
            } else {
                deleteHashElement(element)
            }
        }

        return element
    }

    fun removeActionByTag(tag: Int, target: Ref?) {
        if (BuildConfig.DEBUG && tag == Action.INVALID_TAG) {
            error("Invalid Tag")
        }

        if (target==null) {
            return
        }

        val element = findHashElement(target)
        if (element!=null) {
            val limit = element.actions.size
            for (i in 0 until limit) {
                if (element.actions[i]?.getTag()==tag && element.actions[i]?.getOriginTarget()==target) {
                    removeActionAtIndex(i, element)
                    break
                }
            }
        }
    }

    fun removeAllActionsByTag(tag: Int, target: Ref?) {
        if (BuildConfig.DEBUG && tag == Action.INVALID_TAG) {
            error("Assertion failed")
        }

        if (target==null) {
            return
        }

        val element = findHashElement(target)

        if (element!=null) {
            var limit = element.actions.size
            var i = 0
            while (i<limit) {
                var action = element.actions[i]
                if (action?.getTag()==tag && action?.getOriginTarget()==target) {
                    removeActionAtIndex(i, element)
                    --limit
                } else {
                    ++i
                }
            }
        }
    }

    fun removeActionsByFlags(flags: Long, target: Ref?) {
        if (flags == 0L) {
            return
        }

        if (target==null) {
            return
        }

        val element:tHashElement? = findHashElement(target)
        if (element!=null) {
            var limit:Int = element.actions.size
            var i = 0
            while (i<limit) {
                var action = element.actions[i]

                if (action?.getFlags()?.and(flags)!=0L && action?.getOriginTarget()==target) {
                    removeActionAtIndex(i, element)
                    --limit
                } else {
                    ++i
                }
            }
        }
    }

    fun getActionByTag(tag: Int, target: Ref): Action? {
        if (tag==Action.INVALID_TAG) {
            return null
        }

        val element = findHashElement(target)
        if (element!=null) {
            val limit = element.actions.size
            for (i in 0 until limit) {
                if (element.actions[i]?.getTag()==tag) {
                    return element.actions[i]
                }
            }
        }

        return null
    }

    fun getNumberOfRunningActionsInTarget(target: Ref): Int {
        val element = findHashElement(target)
        return element?.actions?.size ?: 0
    }

    fun getNumberOfRunningActions(): Int {
        var count = 0
        for (i in 0 until _targets.size()) {
            val element = _targets[i]
            count += element.actions.size
        }

        return count
    }

    fun getNumberOfRunningActionsInTargetByTag(target: Ref, tag: Int): Int {
        if (tag==Action.INVALID_TAG) return 0

        val element = findHashElement(target)
        if (element==null || element.actions.size==0) return 0

        var count = 0
        for (i in 0 until element.actions.size) {
            val action = element.actions[i]
            if (action?.getTag()==tag) {
                ++count
            }
        }

        return count
    }

    fun pauseTarget(target: Ref) {
        val element:tHashElement? = findHashElement(target)
        if (element!=null) element.paused = true
    }

    fun resumeTarget(target: Ref) {
        val element = findHashElement(target)
        if (element!=null) element.paused = false
    }

    fun pauseAllRunningActions():ArrayList<Ref?> {
        val idsWidthActions:ArrayList<Ref?> = ArrayList()
        for (i in 0 until _targets.size()) {
            val element = _targets.valueAt(i)
            if (!element.paused) {
                element.paused = true
                idsWidthActions.add(element.target)
            }
        }

        return idsWidthActions
    }

    fun resumeTargets(targetsToResume:ArrayList<Ref?>) {
        for (view:Ref? in targetsToResume) {
            if (view!=null) {
                this.resumeTarget(view)
            }
        }
    }

    fun update(dt:Float) {

        val values = _targets.valueIterator()
        while (values.hasNext()) {
            val elt = values.next()
            _currentTarget = elt
            _currentTargetSalvaged= false

            if (!_currentTarget?.paused!!) {
                _currentTarget!!.actionIndex = 0
                while (_currentTarget!!.actionIndex < _currentTarget!!.actions.size) {
                    _currentTarget!!.currentAction =
                        _currentTarget!!.actions[_currentTarget!!.actionIndex]
                    if (_currentTarget!!.currentAction == null) continue

                    _currentTarget!!.currentActionSalvaged = false
                    _currentTarget!!.currentAction!!.step(dt)

                    if (_currentTarget!!.currentAction!!.isDone()) {
                        _currentTarget!!.currentAction!!.stop()
                        val action = _currentTarget!!.currentAction!!
                        _currentTarget!!.currentAction = null
                        removeAction(action)
                    }

                    _currentTarget!!.currentAction = null
                    _currentTarget!!.actionIndex++
                }

                if (_currentTargetSalvaged && _currentTarget!!.actions.size == 0) {
                    deleteHashElement(_currentTarget!!)
                } else if (_currentTargetSalvaged && _currentTarget!!.target != null) {
                    deleteHashElement(_currentTarget!!)
                }
            }
        }
        _currentTarget = null
    }
}