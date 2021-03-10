package com.interpark.smframework.util.ImageProcess

import com.brokenpc.smframework.base.SMView
import java.lang.ref.WeakReference

class ImageProcessTask() {

    private var _running: Boolean = false
    private var _tag: Int = 0
    private var _processor: ImageProcessor? = null
    private var _target:WeakReference<ImageProcessProtocol>? = null
    private var _function:WeakReference<ImageProcessFunction>? = null
    private var _this: ImageProcessTask? = null


    companion object {
        @JvmStatic
        fun createTaskForTarget(target: ImageProcessProtocol): ImageProcessTask {
            val task = ImageProcessTask()
            task._target = WeakReference(target)
            task._this = task
            task._processor = ImageProcessor.getInstance()

            return task
        }
    }

    fun init(view: SMView, function: ImageProcessFunction, tag: Int): Boolean {
        _function = WeakReference(function)
        _function?.get()?.setTask(this)
        _tag = tag

        if (_function?.get()?.onPreProcess(view) == true) {
            _processor?.handleState(this, ImageProcessor.State.INIT_SUCCESS)
            return true
        } else {
            _processor?.handleState(this, ImageProcessor.State.INIT_FAILED)
            return false
        }
    }

    fun procImageProcessThread() {
        try {
            do {
                Thread.sleep(1)
                if (!_running) break

                if (_function?.get()?.onProcessInBackground()==true) {
                    Thread.sleep(1)
                    if (!_running) break

                    _processor?.handleState(this, ImageProcessor.State.PROCESS_SUCCESS)
                    return
                }

                Thread.sleep(1)
                if (!_running) break

            } while (false)
        } catch (e: InterruptedException) {

        }

        _processor?.handleState(this, ImageProcessor.State.PROCESS_FAILED)
    }

    fun onProgress(progress: Float) {
        _processor?.handleState(this, ImageProcessor.State.PROGRESS, 0, progress)
    }

    fun interrupt() {
        _running = false
        _function?.get()?.interrupt()
    }

    fun isRunning(): Boolean {return _running}
    fun isTargetAlive(): Boolean {return getTarget()!=null}

    fun getTag(): Int {return _tag}
    fun getTarget(): ImageProcessProtocol? {return _target?.get()}
    fun getProcessFunction(): ImageProcessFunction? {return _function?.get()}

}