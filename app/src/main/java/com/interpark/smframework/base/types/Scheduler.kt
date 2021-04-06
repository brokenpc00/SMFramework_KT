package com.brokenpc.smframework.base.types

import android.util.SparseArray
import com.brokenpc.app.BuildConfig
import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.SMView

open class Scheduler(director: IDirector) : Ref(director) {

    protected var _timeScale:Float = 1.0f
    // priority < 0
    protected var _updatesNegList:ArrayList<tListEntry?>? = ArrayList()
    // prioriity == 0
    protected var _updates0List:ArrayList<tListEntry?>? = ArrayList()
    // priority > 0
    protected var _updatesPosList:ArrayList<tListEntry?>? = ArrayList()

    protected var _hashForUpdates:SparseArray<tHashUpdateEntry>? = SparseArray()

    protected var _updateDeleteVector:ArrayList<tListEntry>? = ArrayList()

    protected var _hashForTimers:SparseArray<tHashTimerEntry>? = SparseArray()

    protected var _currentTarget:tHashTimerEntry? = null

    protected var _currentTargetSalvaged: Boolean = false

    protected var _updateHashLocked: Boolean = false

    protected var _functionsToPerform: ArrayList<PERFORM_SEL> = ArrayList(30)


    companion object {
        val PRIORITY_SYSTEM:Int = Int.MIN_VALUE
        val PRIORITY_NON_SYSTEM_MIN:Int = (PRIORITY_SYSTEM+1)
    }

    protected fun removeHashElement(element: tHashTimerEntry) {
        element.timers?.clear()
        _hashForTimers?.remove(element.target.hashCode())
    }

    open fun schedule(selector: SEL_SCHEDULE, target: Ref, interval: Float, paused: Boolean) {
        schedule(selector, target, interval, (UInt.MAX_VALUE- 1u).toLong(), 0.0f, paused)
    }

    open fun schedule(selector: SEL_SCHEDULE, target: Ref?, interval: Float, repeat: Long, delay:Float, paused:Boolean) {
        if (BuildConfig.DEBUG && target == null) {
            error("Assertion failed")
        }

        var element: tHashTimerEntry? = findTimerElement(target)
        if (element == null) {
            element = tHashTimerEntry()
            element.target = target
            _hashForTimers?.append(target.hashCode(), element)

            element.paused = paused
        } else {
            if (BuildConfig.DEBUG && element.paused != paused) {
                error("Assertion failed")
            }
        }

        if (element.timers==null) {
            element.timers = ArrayList(10)
        } else {
            for (i in 0 until element.timers?.size!!) {
                var timer:TimerTargetSelector? = element.timers?.get(i)
                if (timer!=null && !timer.isExhausted() && selector==timer.getSelector()) {
                    timer.setupTimerWithInterval(interval, repeat, delay)
                    return
                }
            }
        }

        var timer:TimerTargetSelector = TimerTargetSelector(_director!!)
        timer.initWithSelector(this, selector, target!!, interval, repeat, delay)
        element.timers?.add(timer)

        _hashForTimers?.put(target.hashCode(), element)
    }

    fun scheduleUpdate(manager: ActionManager, priority: Int, paused: Boolean) {
        schedulePerFrame(object : SEL_SCHEDULE {
            override fun scheduleSelector(t: Float) {
                manager.update(t)
            }
        }, manager, priority, paused)
    }

    fun scheduleUpdate(target: SMView, priority: Int, paused: Boolean) {
        schedulePerFrame(object : SEL_SCHEDULE{
            override fun scheduleSelector(t: Float) {
                target.update(t)
            }
        }, target, priority, paused)
    }

    open fun unschedule(selector: SEL_SCHEDULE?, target: Ref?) {
        if (target==null || selector==null) {
            return;
        }

        var element: tHashTimerEntry? = findTimerElement(target)
        if (element!=null) {
            for (i in 0 until element?.timers?.size!!) {
                var timer:TimerTargetSelector? = element.timers?.get(i)
                if (timer!=null && selector==timer.getSelector()) {
                    if (timer==element.currentTimer && !timer.isAboared()) {
                        timer.setAborted()
                    }

                    element.timers?.remove(timer)

                    if (element.timerIndex>=i) {
                        element.timerIndex--
                    }

                    if (element.timers?.size==0) {
                        if (_currentTarget==element) {
                            _currentTargetSalvaged = true
                        } else {
                            removeHashElement(element)
                        }
                    }

                    return
                }
            }
        }
    }

    open fun unscheduleUpdate(target: Ref?) {
        if (target==null) {
            return
        }

        var element: tHashUpdateEntry? = findUpdateElement(target)
        if (element!=null) {
            removeUpdateFromHash(element.entry!!)
        }
    }

    protected fun findTimerElement(target:Ref?):tHashTimerEntry? {
        if (_hashForTimers==null) {
            _hashForTimers = SparseArray()
        }
        return target?.hashCode()?.let { _hashForTimers?.get(it) }
    }

    fun unscheduleAllForTarget(target: Ref?) {
        if (target==null) return

        var element: tHashTimerEntry? = findTimerElement(target)
        if (element!=null) {
            if ((element.timers?.contains(element.currentTimer) == true)  && (!element.currentTimer?.isAboared()!!)) {
                element.currentTimer?.setAborted()
            }

            element.timers?.clear()

            if (_currentTarget==element) {
                _currentTargetSalvaged = true
            } else {
                removeHashElement(element)
            }
        }

        unscheduledUpdate(target)
    }

    protected fun findUpdateElement(target: Ref?): tHashUpdateEntry? {
        if (_hashForUpdates==null) {
            _hashForUpdates = SparseArray()
        }
        return target?.hashCode()?.let { _hashForUpdates?.get(it) }
    }

    protected fun schedulePerFrame(selector: SEL_SCHEDULE, target: Ref?, priority: Int, paused: Boolean) {
        var hashElement: tHashUpdateEntry? = findUpdateElement(target)
        if (hashElement!=null) {
            if (hashElement.entry?.priority!=priority) {
                unscheduledUpdate(target)
            } else {
                return
            }
        }

        appendIn(selector, target, priority, paused)
    }

    protected fun appendIn(selector: SEL_SCHEDULE, target: Ref?, priority: Int, paused: Boolean) {
        var listElement: tListEntry = tListEntry()
        listElement.callback = selector
        listElement.target = target
        listElement.paused = paused
        listElement.markedForDeletion = false

        var hashElement: tHashUpdateEntry = tHashUpdateEntry()
        if (priority==0) {
            // append In

            _updates0List?.add(listElement)
            hashElement.list = _updates0List
        } else if (priority<0) {
            // priority negative
            if (_updatesNegList==null) {
                _updatesNegList = ArrayList()
                _updatesNegList!!.add(listElement)
            } else {
                var added:Boolean = false
                for (i in 0 until _updatesNegList?.size!!) {
                    var element:tListEntry = _updatesNegList?.get(i)!!
                    if (priority<element.priority) {
                        _updatesNegList?.add(i, listElement)
                        added = true
                        break
                    }
                }

                if (!added) {
                    _updatesNegList?.add(listElement)
                }
            }

            hashElement.list = _updates0List
        } else {
            // priority > 0
            if (_updatesPosList==null) {
                _updatesPosList = ArrayList()
                _updatesPosList!!.add(listElement)
            } else {
                var added:Boolean = false
                for (i in 0 until _updatesPosList?.size!!) {
                    var element: tListEntry = _updatesPosList?.get(i)!!
                    if (priority<element.priority) {
                        _updatesPosList?.add(i, listElement)
                        added = true
                        break
                    }
                }

                if (!added) {
                    _updatesPosList?.add(listElement)
                }
            }

            hashElement.list = _updatesPosList
        }

        hashElement.target = target
        hashElement.entry = listElement
        _hashForUpdates?.put(hashElement.target.hashCode(), hashElement)
    }

    fun unscheduledUpdate(target: Ref?) {
        if (target==null) {
            return;
        }

        var element:tHashUpdateEntry? = findUpdateElement(target)
        if (element!=null) {
            removeUpdateFromHash(element?.entry!!)
        }
    }

    protected fun removeUpdateFromHash(entry: tListEntry) {
        var element:tHashUpdateEntry? = findUpdateElement(entry.target)
        if (element!=null) {
            element.list?.remove(element.entry)
            if (_updateHashLocked) {
                element.entry?.markedForDeletion = true
                _updateDeleteVector?.add(element?.entry!!)
            } else {
                element.entry = null
            }

            _hashForUpdates?.remove(element.target.hashCode())
        }
    }

    fun unscheduleAll() {unscheduleAllWithMinPriority(PRIORITY_SYSTEM)}

    fun unscheduleAllWithMinPriority(minPriority:Int) {
        var element:tHashTimerEntry? = null
        for (i in 0 until _hashForTimers?.size()!!) {
            element = _hashForTimers?.valueAt(i)
            if (element!=null) {
                unscheduleAllForTarget(element.target)
            }
        }

        var entry:tListEntry? = null
        if (minPriority<0) {
            // negative
            for (i in 0 until _updatesNegList?.size!!) {
                entry = _updatesNegList?.get(i)
                if (entry!=null && entry.priority>=minPriority) {
                    unscheduledUpdate(entry.target)
                }
            }
        }

        if (minPriority<=0) {
            // equal 0
            for (i in 0 until _updates0List?.size!!) {
                entry = _updates0List?.get(i)
                if (entry!=null) {
                    unscheduledUpdate(entry.target)
                }
            }
        }

        // positive
        for (i in 0 until _updatesPosList?.size!!) {
            entry = _updatesPosList?.get(i)
            if (entry!=null && entry.priority>=minPriority) {
                unscheduledUpdate(entry.target)
            }
        }
    }

    fun isScheduled(selector: SEL_SCHEDULE?, target: Ref): Boolean {
        var element: tHashTimerEntry? = findTimerElement(target)
        if (element==null) return false

        if (element.timers==null) return false

        for (i in 0 until element.timers?.size!!) {
            var timer:TimerTargetSelector? = element.timers?.get(i)
            if (timer!=null && !timer.isExhausted() && selector==timer.getSelector()) {
                return true
            }
        }

        return false
    }

    open fun update(t: Float) {
        _updateHashLocked = true
        var dt:Float = t

        if (_timeScale!=1.0f) {
            dt *= _timeScale
        }

        var entry:tListEntry? = null

        // priority negative list update
        for (i in 0 until _updatesNegList?.size!!) {
            entry = _updatesNegList?.get(i)
            if (entry!=null && !entry.paused && !entry.markedForDeletion) {
                entry.callback?.scheduleSelector(dt)
            }
        }

        // priority 0 list update
        for (i in 0 until _updates0List?.size!!) {
            entry = _updates0List?.get(i)
            if (entry!=null && !entry.paused && !entry.markedForDeletion) {
                entry.callback?.scheduleSelector(dt)
            }
        }

        // priority positive list udpate
        for (i in 0 until _updatesPosList?.size!!) {
            entry = _updatesPosList?.get(i)
            if (entry!=null && !entry.paused && !entry.markedForDeletion) {
                entry.callback?.scheduleSelector(dt)
            }
        }


        var hashIndex:Int = 0
        while (hashIndex<_hashForTimers?.size()!!) {
            var elt:tHashTimerEntry = _hashForTimers?.valueAt(hashIndex)!!
            _currentTarget = elt
            _currentTargetSalvaged = false
            if (!_currentTarget?.paused!!) {
                for (t in 0 until elt.timers?.size!!) {
                    elt.timerIndex = t
                    elt.currentTimer = elt.timers?.get(elt.timerIndex)
                    if (BuildConfig.DEBUG && elt.currentTimer?.isAboared()!!) {
                        error("Assertion failed")
                    }

                    elt.currentTimer?.update(dt)
                    elt.currentTimer = null
                }
            }

            hashIndex++

            if (_currentTargetSalvaged && _currentTarget?.timers?.size==0) {
                removeHashElement(_currentTarget!!)
                hashIndex--
            }
        }

        _updateDeleteVector?.clear()
        _updateHashLocked = false
        _currentTarget = null

        if (!_functionsToPerform.isEmpty()!!) {
            synchronized(_functionsToPerform) {
                for (i in 0 until _functionsToPerform.size!!) {
                    var func:PERFORM_SEL = _functionsToPerform.get(i)
                    func.performSelector()
                }

                _functionsToPerform.clear()
            }
        }
    }

    fun getTimeScale():Float {return _timeScale}
    fun setTimeScale(scale:Float) {_timeScale=scale}

    fun performFunctionInMainThread(func: PERFORM_SEL) {
        synchronized(_functionsToPerform) {
            _functionsToPerform.add(func)
        }
    }

    fun removeAllFunctionsToBePerformedInMainThread() {
        synchronized(_functionsToPerform) {
            _functionsToPerform.clear()
        }
    }

    fun pauseTarget(target: Ref) {
        var element:tHashTimerEntry? = findTimerElement(target)
        element?.paused = true
        var elementUpdate:tHashUpdateEntry? = findUpdateElement(target)
        elementUpdate?.entry?.paused = true
    }

    fun resumeTarget(target: Ref) {
        var element: tHashTimerEntry? = findTimerElement(target)
        element?.paused = false

        var elementUpdate: tHashUpdateEntry? = findUpdateElement(target)
        elementUpdate?.entry?.paused = false
    }

    fun isTargetPaused(target: Ref): Boolean {
        var element:tHashTimerEntry? = findTimerElement(target)
        if (element!=null) {
            return element.paused
        }

        var elementUpdate:tHashUpdateEntry? = findUpdateElement(target)
        if (elementUpdate!=null) {
            return elementUpdate.entry?.paused ?: false
        }

        return false
    }
}