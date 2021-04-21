package com.brokenpc.smframework.util

import android.os.Process
import android.util.Log
import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.types.PERFORM_SEL
import com.brokenpc.smframework.base.types.Ref
import java.lang.Exception
import java.lang.RuntimeException
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

abstract class SMAsyncTask<Params, Progress, Result>(director: IDirector) {
    private val _director = director

    fun getDirector() : IDirector {return _director}

    private fun postResultIfNotInvoked(result: Result?) {
        val wasTaskInvoked = _taskInvoked.get()
        if (!wasTaskInvoked) {
            postResult(result)
        }
    }

    private var _worker:WorkerRunnable<Params, Result>
    private var _future:FutureTask<Result>

    private var _status:Status = Status.PENDING

    private val _cancelled:AtomicBoolean = AtomicBoolean()
    private val _taskInvoked:AtomicBoolean = AtomicBoolean()




    companion object {
        private val LOG_TAG:String = "AsyncTask"
        private val CPU_COUNT:Int = Runtime.getRuntime().availableProcessors()
        private val CORE_POOL_SIZE:Int = CPU_COUNT + 1
        private val MAXIMUM_POOL_SIZE:Int = CPU_COUNT * 2 + 1
        private val KEEP_ALIVE:Int = 1


        private val _threadFactory: ThreadFactory = object : ThreadFactory {
            private val _count = AtomicInteger(1)
            override fun newThread(r:Runnable):Thread {
                return Thread(r, "AsyncTask #" + _count.getAndIncrement())
            }
        }

        private val _poolWorkQueue:BlockingQueue<Runnable> = LinkedBlockingQueue(128)
        public val THREAD_POOL_EXECUTOR:Executor = ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE.toLong(), TimeUnit.SECONDS, _poolWorkQueue, _threadFactory)

        private val SERIAL_EXECUTOR:Executor = SerialExecutor()
        private var _defaultExecutor:Executor = SERIAL_EXECUTOR

        private class SerialExecutor: Executor {
            val _task = java.util.ArrayDeque<Runnable>()
            var _active:Runnable? = null

            @Synchronized
            override fun execute(r: Runnable) {
                _task.offer(Runnable {
                    try {
                        r.run()
                    } finally {
                        scheduleNext()
                    }
                })
            }

            @Synchronized
            private fun scheduleNext() {
                if ((_task.poll().also { _active=it })!=null) {
                    THREAD_POOL_EXECUTOR.execute(_active)
                }
            }
        }

        @JvmStatic
        open fun init() {}

        @JvmStatic
        fun setDefaultExecutor(exec: Executor) {}

        private abstract class WorkerRunnable<Params, Result> : Callable<Result> {
            var _params: Array<Params?>? = null
        }


        @JvmStatic
        fun execute(runnable: Runnable) {
            _defaultExecutor.execute(runnable)
        }
    }



    enum class Status {
        PENDING,
        RUNNING,
        FINISHED
    }

    open fun postResult(result: Result?): Result? {
        _director.getScheduler().performFunctionInMainThread(object : PERFORM_SEL {
            override fun performSelector() {
                finish(result)
            }
        })
        return result
    }


    private fun finish(result: Result?) {
        if (isCancelled()) {
            onCancelled(result)
        } else {
            onPostExecute(result)
        }

        _status = Status.FINISHED
    }


    fun getStatus():Status {return _status}
    protected abstract fun doInBackground(vararg params: Params?): Result?
    open protected fun onPreExecute() {}
    open protected fun onPostExecute(result: Result?) {}
    open protected fun onProgressUpdate(vararg value: Progress) {}
    open protected fun onCancelled(result: Result?) { onCancelled() }
    open protected fun onCancelled() {}
    fun isCancelled():Boolean {return _cancelled.get()}
    fun cancel(mayInterruptIfRunning:Boolean):Boolean {
        _cancelled.set(true)
        return _future.cancel(mayInterruptIfRunning)
    }

    @Throws(InterruptedException::class, ExecutionException::class)
    fun get():Result {
        return _future.get()
    }

    @Throws(InterruptedException::class, ExecutionException::class)
    fun get(timeout:Long, tunit:TimeUnit):Result {
        return _future.get(timeout, tunit)
    }

    fun execute(vararg params:Params):SMAsyncTask<Params, Progress, Result> {
        return executeOnExecutor(_defaultExecutor, params as Params)
    }

    fun executeOnExecutor(exec: Executor, vararg params: Params
    ): SMAsyncTask<Params, Progress, Result> {
        if (_status != Status.PENDING) {
            when (_status) {
                Status.RUNNING -> throw java.lang.IllegalStateException(
                    "Cannot execute task:"
                            + " the task is already running."
                )
                Status.FINISHED -> throw java.lang.IllegalStateException(
                    "Cannot execute task:"
                            + " the task has already been executed "
                            + "(a task can be executed only once)"
                )
                else -> {
                }
            }
        }
        _status = Status.RUNNING
        onPreExecute()
        _worker._params = params as Array<Params?>
        exec.execute(_future)
        return this
    }

    protected open fun publishProgress(vararg values: Progress) {
        if (!isCancelled()) {
            _director.getScheduler().performFunctionInMainThread(object : PERFORM_SEL {
                override fun performSelector() {
                    onProgressUpdate(*values)
                }
            })
        }
    }

    init {
        _worker = object : WorkerRunnable<Params, Result>() {
            @Throws(Exception::class)
            override fun call():Result? {
                _taskInvoked.set(true)
                Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND)
                return postResult(_params?.let { doInBackground(*it) })
            }
        }
        _future = object : FutureTask<Result>(_worker) {
            override fun done() {
                try {
                    val result = get()
                    postResultIfNotInvoked(get())
                } catch (e: InterruptedException) {
                    Log.w(LOG_TAG, e)
                } catch (e: ExecutionException) {
                    throw RuntimeException(
                        "An error occured while executing doInBackground()",
                        e.cause
                    )
                } catch (e: CancellationException) {
                    postResultIfNotInvoked(null)
                }
            }
        }
    }

}