package com.interpark.smframework.base.types

import android.util.SparseArray
import com.interpark.app.BuildConfig
import com.interpark.smframework.IDirector
import com.interpark.smframework.base.SMView
import kotlin.math.sign

open class ActionManager(director: IDirector) : Ref(director) {

    companion object {

    }

    protected var _targets:SparseArray<tHashElement> = SparseArray()
    protected var _currentTarget:tHashElement? = null
    protected var _currentTargetSalvaged:Boolean = false

    protected fun findHashElement(target: Ref): tHashElement? {
        return _targets.get(target.hashCode())
    }

    fun addAction(action: Action, target: Ref, paused: Boolean) {
        var element: tHashElement? = findHashElement(target)
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
            val element:tHashElement = _targets[i]
            val target:Ref? = element.target
            if (target!=null) {
                removeallActionsFromTarget(target)
            }
        }
        _targets.clear()
    }

    fun removeallActionsFromTarget(target: Ref) {
        val element:tHashElement? = _targets.get(target.hashCode())
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

    fun removeAction(action: Action) {
        val target: Ref = action.getOriginTarget()!!
        val element: tHashElement? = findHashElement(target)
        if (element!=null) {
            val index:Int = element.actions.indexOf(action)
            if (index!=-1) {
                removeActionAtIndex(index, element)
            }
        }
    }

    fun removeActionAtIndex(index:Int, element:tHashElement):tHashElement {
        val action:Action? = element.actions[index]
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

        val element:tHashElement? = findHashElement(target)
        if (element!=null) {
            val limit:Int = element.actions.size
            for (i in 0 until limit) {
                val action:Action? = element.actions[i]
                if (action?.getTag()==tag && action.getOriginTarget() ==target) {
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

        val element:tHashElement? = findHashElement(target)

        if (element!=null) {
            var limit:Int = element.actions.size
            var i:Int = 0
            while (i<limit) {
                var action:Action? = element.actions[i]
                if (action?.getTag()==tag && action.getOriginTarget()==target) {
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
            var i:Int = 0
            while (i<limit) {
                var action:Action? = element.actions[i]

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

        val element:tHashElement? = findHashElement(target)
        if (element!=null) {
            val limit:Int = element.actions.size
            for (i in 0 until limit) {
                val action:Action? = element.actions.get(i)
                if (action?.getTag()==tag) {
                    return action
                }
            }
        }

        return null
    }

    fun getNumberOfRunningActionsInTarget(target: Ref): Int {
        val element:tHashElement? = findHashElement(target)
        return element?.actions?.size ?: 0
    }

    fun getNumberOfRunningActions(): Int {
        var count: Int = 0
        for (i in 0 until _targets.size()) {
            val element:tHashElement = _targets[i]
            count += element.actions.size
        }

        return count
    }

    fun getNumberOfRunningActionsInTargetByTag(target: Ref, tag: Int): Int {
        if (tag==Action.INVALID_TAG) return 0

        val element:tHashElement? = findHashElement(target)
        if (element==null || element.actions.size==0) return 0

        var count:Int = 0
        for (i in 0 until element.actions.size) {
            val action:Action? = element.actions[i]
            if (action?.getTag()==tag) {
                ++count
            }
        }

        return count
    }

    fun pauseTarget(target: Ref) {
        val element:tHashElement? = findHashElement(target)
        element?.paused = true
    }

    fun resumeTarget(target: Ref) {
        val element:tHashElement? = findHashElement(target)
        element?.paused = false
    }

    fun pauseAllRunningActions():ArrayList<Ref?> {
        val idsWidthActions:ArrayList<Ref?> = ArrayList()
        for (i in 0 until _targets.size()) {
            val element:tHashElement = _targets.valueAt(i)
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
        for (i in 0 until _targets.size()) {
            val elt:tHashElement = _targets.valueAt(i)
            _currentTarget = elt
            _currentTargetSalvaged= false

            if (!_currentTarget?.paused!!) {
                for (i in 0 until _currentTarget?.actions?.size!!) {
                    _currentTarget?.currentAction = _currentTarget?.actions?.get(i)
                    if (_currentTarget?.currentAction==null) {
                        continue
                    }

                    _currentTarget?.currentActionSalvaged = false

                    _currentTarget?.currentAction?.step(dt)

                    if (_currentTarget?.currentAction?.isDone()!!) {
                        _currentTarget?.currentAction?.stop()
                        val action:Action = _currentTarget?.currentAction!!
                        removeAction(action)
                    }

                    _currentTarget?.currentAction = null
                }
            }

            if (_currentTargetSalvaged && _currentTarget?.actions?.size==0) {
                deleteHashElement(_currentTarget!!)
            } else if (_currentTargetSalvaged && _currentTarget?.target!=null) {
                deleteHashElement(_currentTarget!!)
            }
        }

        _currentTarget = null
    }
}