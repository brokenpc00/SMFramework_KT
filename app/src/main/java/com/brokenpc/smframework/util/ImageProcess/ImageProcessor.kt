package com.interpark.smframework.util.ImageProcess

import com.brokenpc.smframework.SMDirector
import com.brokenpc.smframework.base.SMView
import com.brokenpc.smframework.base.sprite.BitmapSprite
import com.brokenpc.smframework.base.types.PERFORM_SEL
import com.brokenpc.smframework.base.types.Size
import com.brokenpc.smframework.util.ImageManager.ImageThreadPool
import java.lang.Exception
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock

class ImageProcessor() {

    private val _mutex_process: Lock = ReentrantLock(true)
    private val _processThreadPool:ImageThreadPool = ImageThreadPool(1)

    enum class State {
        INIT_SUCCESS,
        INIT_FAILED,

        PROCESS_SUCCESS,
        PROCESS_FAILED,

        PROGRESS
    }

    companion object {
        private var _processInstance:ImageProcessor? = null

        @JvmStatic
        fun getInstance():ImageProcessor {
            if (_processInstance==null) {
                _processInstance = ImageProcessor()
            }

            return _processInstance!!
        }
    }

    @Throws(Throwable::class)
    fun finalize() {
        try {
            _processThreadPool.interrupt()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            println("All done!")
        }
    }

    fun executeImageProcess(target: ImageProcessProtocol, view: SMView, function: ImageProcessFunction) {
        executeImageProcess(target, view, function, 0)
    }
    fun executeImageProcess(target: ImageProcessProtocol, view: SMView, function: ImageProcessFunction, tag: Int) {
        val task = ImageProcessTask.createTaskForTarget(target)
        task.init(view, function, tag)
    }

    fun cancelImageProcess(target: ImageProcessProtocol?) {
        target?.resetImageProcess()
    }

    fun handleState(task: ImageProcessTask, state: State) {
        handleState(task, state, 0)
    }
    fun handleState(task: ImageProcessTask, state: State, intParam: Int) {
        handleState(task, state, intParam, 0f)
    }
    fun handleState(task: ImageProcessTask, state: State, intParam: Int, floatParam: Float) {
        when (state) {
            State.INIT_SUCCESS -> {
                if (task.isTargetAlive()) {
                    if (task.getProcessFunction()?.isCaptureOnly()==true) {
                        var sprite: BitmapSprite? = null
                        val capturedTexture = task.getProcessFunction()?.getCapturedTexture()
                        if (capturedTexture!=null) {
                            sprite = BitmapSprite.createFromTexture(SMDirector.getDirector(), capturedTexture)
                        }
                        task.getTarget()?.onImageProcessComplete(task.getTag(), true, sprite, task.getProcessFunction()?.getParam())
                        task.getTarget()?.removeImageProcessTask(task)
                    } else {
                        task.getTarget()?.onImageCaptureComplete(
                            task.getTag(),
                            task.getProcessFunction()?.getCapturedTexture(),
                            task.getProcessFunction()?.getInputData(),
                            task.getProcessFunction()?.getInputSize()?: Size(Size.ZERO),
                            task.getProcessFunction()?.getInputBpp()?:0
                        )
                    }
                }

                _processThreadPool.addTask(object : PERFORM_SEL {
                    override fun performSelector() {
                        task.procImageProcessThread()
                    }
                })
            }
            State.INIT_FAILED -> {
                if (task.isTargetAlive()) {
                    task.getTarget()?.onImageProcessComplete(task.getTag(), false, null, null)
                    task.getTarget()?.removeImageProcessTask(task)
                }
            }
            State.PROCESS_FAILED -> {
                _mutex_process.lock()
                try {
                    if (task.isTargetAlive()) {
                        SMDirector.getDirector().getScheduler().performFunctionInMainThread(object : PERFORM_SEL {
                            override fun performSelector() {
                                if (task.isTargetAlive()) {
                                    task.getTarget()?.onImageProcessComplete(task.getTag(), false, null, null)
                                    task.getTarget()?.removeImageProcessTask(task)
                                }
                            }
                        })
                    }
                } catch (e: Exception) {

                } finally {
                    _mutex_process.unlock()
                }
            }
            State.PROCESS_SUCCESS -> {
                _mutex_process.lock()
                try {
                    if (task.isTargetAlive()) {
                        SMDirector.getDirector().getScheduler().performFunctionInMainThread(object : PERFORM_SEL {
                            override fun performSelector() {
                                if (task.isTargetAlive()) {
                                    task.getTarget()?.onImageProcessComplete(task.getTag(), true, task.getProcessFunction()?.onPostProcess(), task.getProcessFunction()?.getParam())
                                    task.getTarget()?.removeImageProcessTask(task)
                                }
                            }
                        })
                    }
                } catch (e: Exception) {

                } finally {
                    _mutex_process.unlock()
                }
            }
            State.PROGRESS -> {
                _mutex_process.lock()
                try {
                    if (task.isTargetAlive()) {
                        SMDirector.getDirector().getScheduler().performFunctionInMainThread(object : PERFORM_SEL {
                            override fun performSelector() {
                                if (task.isTargetAlive()) {
                                    task.getTarget()?.onImageProcessProgress(task.getTag(), floatParam)
                                }
                            }
                        })
                    }
                } catch (e: Exception) {

                } finally {
                    _mutex_process.unlock()
                }
            }
        }
    }
}