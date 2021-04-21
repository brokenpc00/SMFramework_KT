package com.interpark.smframework.view

import android.media.Image
import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.SMView
import com.brokenpc.smframework.base.sprite.Sprite
import com.brokenpc.smframework.base.types.*
import com.brokenpc.smframework.util.ImageManager.IDownloadProtocol
import com.brokenpc.smframework.util.ImageManager.ImageDownloadTask
import com.brokenpc.smframework.util.ImageManager.ImageDownloader
import com.brokenpc.smframework.view.SMImageView
import com.brokenpc.smframework.view.SMSolidRectView
import kotlin.math.pow

class SMKenBurnsView(director: IDirector): SMView(director), IDownloadProtocol {

    enum class Mode {
        ASSET,
        URL
    }

    private var _mode = Mode.ASSET
    private var _sequence = 0
    private var _serial = 0
    private var _downloader:ImageDownloader? = null

    private val _imageList:ArrayList<String> = ArrayList()
    var _dimLayer:SMSolidRectView? = null

    private val _downloadTask:ArrayList<ImageDownloadTask> = ArrayList()

    companion object {
        const val PAN_TIME = 8.0f
        const val FADE_TIME = 1.3f
        const val MINUM_RECT_FACTOR = 0.6f
        val DIM_LAYER_COLOR = Color4F(0f, 0f, 0f, 0.6f)

        @JvmStatic
        fun createWithAssets(director: IDirector, assetList:ArrayList<String>): SMKenBurnsView {
            val view = SMKenBurnsView(director)
            view.initWithImageList(Mode.ASSET, assetList)
            return view
        }

        @JvmStatic
        fun createWithURLs(director: IDirector, urlList: ArrayList<String>): SMKenBurnsView {
            val view = SMKenBurnsView(director)
            view.initWithImageList(Mode.URL, urlList)
            return view
        }

        @JvmStatic
        fun getAspectRatio(rect: Size): Float {
            return rect.width/rect.height
        }

        @JvmStatic
        fun truncate(f: Float, d: Int): Float {
            val dShift = 10.toDouble().pow(d.toDouble())
            return (round((f*dShift).toFloat()) / dShift).toFloat()
        }
    }

    fun initWithImageList(mode: Mode, imageList: ArrayList<String>): Boolean {
        if (imageList.isEmpty()) return false

        _mode = mode
        _imageList.addAll(imageList)

        _dimLayer = SMSolidRectView.create(getDirector())
        _dimLayer!!.setContentSize(_contentSize)
        _dimLayer!!.setColor(DIM_LAYER_COLOR)
        addChild(_dimLayer)

        return true
    }

    override fun setContentSize(size: Size) {
        super.setContentSize(size)

        _dimLayer?.setContentSize(size)
    }

    override fun setContentSize(width: Float?, height: Float?) {
        super.setContentSize(width, height)
        _dimLayer?.setContentSize(width, height)
    }

    fun startWithDelay(delay: Float) {
        if (delay<=0) {
            onNextTransition(0f)
        } else {
            if (_mode==Mode.URL) {
                ImageDownloader.getInstance().loadImageFromNetwork(this, _imageList[0], _serial++, ImageDownloader.CACHE_ONLY)
            }
            scheduleOnce(object : SEL_SCHEDULE{
                override fun scheduleSelector(t: Float) {
                    onNextTransition(t)
                }
            }, delay)
        }
    }

    private fun onNextTransition(dt: Float) {
        if (_mode==Mode.URL) {
            ImageDownloader.getInstance().loadImageFromNetwork(this, _imageList[_sequence++], _serial++)
        } else {
            ImageDownloader.getInstance().loadImageFromResource(this, _imageList[_sequence++], _serial++, ImageDownloader.NO_CACHE)
        }

        _sequence %= _imageList.size
    }

    override fun onImageLoadComplete(sprite: Sprite?, tag: Int, direct: Boolean) {
        if (sprite!=null) {
            val imageView = SMImageView.create(getDirector(), sprite)
            imageView.setScaleType(SMImageView.ScaleType.CENTER)
            imageView.setContentSize(sprite.getContentSize())
            imageView.setAnchorPoint(Vec2.MIDDLE)
            addChild(imageView)

            val src = generateRandomRect(imageView.getContentSize())
            val dst = generateRandomRect(imageView.getContentSize())

            src.origin.x += _contentSize.width/2
            src.origin.y += _contentSize.height/2
            dst.origin.x += _contentSize.width/2
            dst.origin.y += _contentSize.height/2

            val action = KenburnsTransitionActionCreate(getDirector())
            action.setValue(imageView, src, dst, PAN_TIME, 0f)
            action.setTag(17)

            runAction(action)
        }

        if (_mode==Mode.URL) {
            val nextSeq = (_sequence+1) % _imageList.size
            ImageDownloader.getInstance().loadImageFromNetwork(this, _imageList[nextSeq], _serial++, ImageDownloader.CACHE_ONLY_DISK_ONLY)
        }

        _scheduler?.performFunctionInMainThread(object : PERFORM_SEL{
            override fun performSelector() {
                scheduleOnce(object : SEL_SCHEDULE {
                    override fun scheduleSelector(t: Float) {
                        onNextTransition(t)
                    }
                }, (PAN_TIME- FADE_TIME-0.5f))
            }
        })
    }

    fun pauseKenburns() {
        onPause()
    }

    fun rusumeKenburs() {
        onResume()
    }

    fun generateRandomRect(imageSize: Size): Rect {
        val ratio1 = getAspectRatio(imageSize)
        val ratio2 = getAspectRatio(_contentSize)

        val maxCorp = Rect(Rect.ZERO)

        if (ratio1>ratio2) {
            val r = (imageSize.height/_contentSize.height) * _contentSize.width
            val b = imageSize.height
            maxCorp.set(0f, 0f, r, b)
        } else {
            val r = imageSize.width
            val b = (imageSize.width/_contentSize.width) * _contentSize.height
            maxCorp.set(0f, 0f, r, b)
        }

        val rnd = truncate(randomFloat(0f, 1f), 2)
        val factor = MINUM_RECT_FACTOR + ((1- MINUM_RECT_FACTOR)*rnd)

        val width = factor * maxCorp.size.width
        val height = factor * maxCorp.size.height

        val diffWidth = imageSize.width - width
        val diffHeight = imageSize.height - height

        val x = if (diffWidth>0f) {randomFloat(0f, diffWidth)} else {0f}
        val y = if (diffHeight>0f) { randomFloat(0f, diffHeight)} else {0f}

        return Rect(x, y, width, height)
    }

    fun KenburnsTransitionActionCreate(director: IDirector): KenburnsTransitionAction {
        val action = KenburnsTransitionAction(director)
        action.initWithDuration(0f)
        return action
    }

    class KenburnsTransitionAction(director: IDirector): DelayBaseAction(director) {
        private var _image:SMImageView? = null
        private val _src = Rect(Rect.ZERO)
        private val _dst = Rect(Rect.ZERO)


        override fun onStart() {

        }

        override fun onUpdate(t: Float) {
            updateTextureRect(t)

            val time = getDuration() * t
            val alpha = (time / FADE_TIME).coerceAtMost(1.0f)
            _image!!.setAlpha(alpha)
            _image!!.setScale(_target!!.getContentSize().width/_image!!.getContentSize().width)
        }

        override fun onEnd() {
            _target?.removeChild(_image)
        }

        fun updateTextureRect(t: Float) {
            val x = interpolation(_src.origin.x, _dst.origin.x, t)
            val y = interpolation(_src.origin.y, _dst.origin.y, t)
            val w = interpolation(_src.size.width, _dst.size.width, t)
            val h = interpolation(_src.size.height, _dst.size.height, t)

            _image?.setAnchorPoint(Vec2.MIDDLE)
            _image?.setContentSize(w, h)
            _image?.setPosition(x, y)
        }

        fun setValue(image: SMImageView?, src: Rect, dst: Rect, duration: Float, delay: Float) {
            setTimeValue(duration, delay)

            _src.set(src)
            _dst.set(dst)

            _image = image
            _image?.setAlpha(0f)
            updateTextureRect(0f)
        }
    }

    override fun onImageCacheComplete(success: Boolean, tag: Int) {

    }

    override fun onImageLoadStart(state: IDownloadProtocol.DownloadStartState) {

    }

    override fun onDataLoadComplete(data: ByteArray, size: Int, tag: Int) {

    }

    override fun onDataLoadStart() {

    }

    override fun resetDownload() {
        synchronized(_downloadTask) {
            for (task in _downloadTask) {
                if (task.isTargetAlive()) {
                    if (task.isRunning()) {
                        task.interrupt()
                    }
                }
            }

            _downloadTask.clear()
        }
    }

    override fun removeDownloadTask(task: ImageDownloadTask?) {
        synchronized(_downloadTask) {
            val itr: ListIterator<ImageDownloadTask> = _downloadTask.listIterator()
            while (itr.hasNext()) {
                val t = itr.next()
                if (!t.isTargetAlive()) {
                    _downloadTask.remove(t)
                } else if (task!=null && (t==task || task.getCacheKey().compareTo(t.getCacheKey())==0)) {
                    task.interrupt()
                    _downloadTask.remove(t)
                }
            }
        }
    }

    override fun isDownloadRunning(requestPath: String, requestTag: Int): Boolean {
        synchronized(_downloadTask) {
            for (t in _downloadTask) {
                if (t.getRequestPath().compareTo(requestPath)==0 && t.getTag()==requestTag) {
                    return true
                }
            }

            return false
        }
    }

    override fun addDownloadTask(task: ImageDownloadTask?): Boolean {
        if (task==null) return false
        synchronized(_downloadTask) {
            val itr = _downloadTask.listIterator()
            while (itr.hasNext()) {
                val t = itr.next()
                if (!t.isTargetAlive()) {
                    _downloadTask.remove(t)
                } else if (t.isRunning() && (t==task || task.getCacheKey().compareTo(t.getCacheKey())==0)) {
                    return false
                }
            }
            _downloadTask.add(task)
            return true
        }
    }

}