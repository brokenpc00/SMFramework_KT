package com.brokenpc.smframework.util.ImageManager

import com.brokenpc.smframework.base.types.PERFORM_SEL
import java.util.*
import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import kotlin.collections.ArrayList

class ImageThreadPool {
    constructor(threadCount: Int) {
        _running = true

        for (i in 0 until threadCount) {
            val t:Thread = Thread {
                try {
                    threadFunc()
                } catch (e:InterruptedException) {

                }
            }
            t.start()
            _worker.add(t)
        }
    }

    fun interrupt() {
        _running = false

        synchronized(_cond) {
            _cond.signalAll()
        }
    }

    fun addTask(task: PERFORM_SEL) {
        synchronized(_queue) {
            _queue.add(task)
        }
        synchronized(_cond) {
            _cond.signal()
        }
    }

    @Throws(InterruptedException::class)
    private fun threadFunc() {
        while (true) {
            var task: PERFORM_SEL? = null
            if (!_running) {
                break
            }

            var isEmpty = false

            synchronized(_queue) {
                isEmpty = _queue.isEmpty()
            }

            if (!isEmpty) {
                synchronized(_queue) {
                    task = _queue.poll()
                }
            } else {
                synchronized(_cond) {
                    _cond.await()
                }

                if (!_running) {
                    break
                }
                continue
            }

            task?.performSelector()
        }
    }

    private val _mutex:Lock = ReentrantLock(true)
    private val _cond:Condition = _mutex.newCondition()
    private val _worker:ArrayList<Thread> = ArrayList()
    private val _queue:Queue<PERFORM_SEL> = LinkedList()
    private var _running:Boolean = false
}