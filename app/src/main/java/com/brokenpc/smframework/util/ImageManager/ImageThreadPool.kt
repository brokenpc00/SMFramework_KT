package com.brokenpc.smframework.util.ImageManager

import com.brokenpc.smframework.base.types.PERFORM_SEL
import java.util.*
import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import kotlin.collections.ArrayList
import kotlin.concurrent.withLock

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

        _mutex.withLock {
            _cond.signalAll()
        }
    }

    fun addTask(task: PERFORM_SEL) {
        _mutex.withLock {
            _queue.add(task)
            _cond.signal()
        }
    }

    @Throws(InterruptedException::class)
    private fun threadFunc() {
        while (true) {
            var task: PERFORM_SEL? = null

            _mutex.lock()

            if (!_running) {
                break
            }

            if (!_queue.isEmpty()) {
                    task = _queue.poll()
            } else {
                    _cond.await()
                if (!_running) {
                    return
                }
                _mutex.unlock()
                continue
            }
            _mutex.unlock()
            task?.performSelector()
        }
    }

    private val _mutex:Lock = ReentrantLock(true)
    private val _cond:Condition = _mutex.newCondition()
    private val _worker:ArrayList<Thread> = ArrayList()
    private val _queue:Queue<PERFORM_SEL> = LinkedList()
    private var _running:Boolean = false
}