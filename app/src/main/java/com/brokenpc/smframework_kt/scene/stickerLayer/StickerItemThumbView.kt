package com.brokenpc.app.scene.stickerLayer

import android.view.MotionEvent
import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.SMView
import com.brokenpc.smframework.base.sprite.Sprite
import com.brokenpc.smframework.base.types.*
import com.brokenpc.smframework.shader.ShaderNode
import com.brokenpc.smframework.util.AppConst
import com.brokenpc.smframework.util.ImageManager.DownloadConfig
import com.brokenpc.smframework.util.ImageManager.IDownloadProtocol
import com.brokenpc.smframework.util.ImageManager.ImageDownloadTask
import com.brokenpc.smframework.util.ImageManager.ImageDownloader
import com.brokenpc.smframework.view.SMImageView
import com.brokenpc.smframework.view.SMRectView
import com.brokenpc.smframework.base.types.FadeIn
import com.brokenpc.smframework.view.LoadingSprite
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.cos
import kotlin.math.sin

class StickerItemThumbView(director: IDirector): SMView(director), IDownloadProtocol {
    private var _shakeAction: ShakeAction? = null
    private var _selectAction: SelectAction? = null
    private var _selected = false

    private var _selectBox: SMRectView? = null
    private val _imageView = SMImageView(director)
    private val _spinner =  LoadingSprite(director)

    private var _thumbDlConfig: DownloadConfig? = null
    private var _imgDlConfig: DownloadConfig? = null
    private var _imagePath: String = ""

    private var _downloadTask: ArrayList<ImageDownloadTask> = ArrayList()

    companion object {
        const val SHAKE_TIME = 1.2f
        const val SHAKE_ANGLE = 8.0f

        @JvmStatic
        fun create(director: IDirector, tag: Int, thumbDlConfig: DownloadConfig, imgDlConfig: DownloadConfig): StickerItemThumbView {
            val view = StickerItemThumbView(director)
            view.setTag(tag)
            view.init()
            view._thumbDlConfig = thumbDlConfig
            view._imgDlConfig = imgDlConfig
            return view
        }
    }

    override fun init(): Boolean {
        if (!super.init()) {
            return false
        }

        _imageView.setAnchorPoint(Vec2.MIDDLE)
        addChild(_imageView)

        _imageView.setTag(_tag)

        _spinner.setAnchorPoint(Vec2.MIDDLE)
        addChild(_spinner)

        _imageView.setPadding(15f)
        _imageView.setScaleType(SMImageView.ScaleType.CENTER_INSIDE)

        return true
    }

    override fun setContentSize(width: Float?, height: Float?) {
        this.setContentSize(Size(width, height))
    }

    override fun setContentSize(size: Size) {
        super.setContentSize(size)

        _imageView.setContentSize(size)
        _imageView.setPosition(size.divide(2f))
        _spinner.setPosition(size.divide(2f))
    }

    override fun onEnter() {
        super.onEnter()

        if (_imageView.getSprite()!=null) {
            _imageView.getSprite()!!.setColor(Color4F.WHITE)
            _imageView.getSprite()!!.setAlpha(1f)
            _spinner.setVisible(false)
        } else {
            _spinner.setVisible(true)
            ImageDownloader.getInstance().loadImageFromResource(this, _imagePath, _tag, _thumbDlConfig)
        }
    }

    override fun cleanup() {
        super.cleanup()

        resetDownload()

        _imageView.setSprite(null)

        _spinner.setAlpha(1f)
        _spinner.stopAllActions()
    }

    fun isSelected(): Boolean {return _selected}

    fun setImagePath(path: String) {
        if (_imagePath!=path) {
            resetDownload()
        }

        _imagePath = path
    }

    fun startShowAction() {
        if (_imageView.getSprite()!=null) {
            if (_shakeAction==null) {
                _shakeAction = createShakeAction(getDirector())
                _shakeAction!!.setTag(AppConst.TAG.USER+1)
            }

            if (getActionByTag(AppConst.TAG.USER+1)!=null) {
                stopAction(_shakeAction!!)
            }

            _shakeAction!!.setValue(0f)
            runAction(_shakeAction!!)
        }
    }

    fun setSelect(select: Boolean, immediate: Boolean) {
        if (_selected==select) return

        val action = getActionByTag(AppConst.TAG.USER+2)
        if (action!=null) {
            stopAction(action)
        }

        if (select && _selectBox==null) {
            _selectBox = SMRectView.create(getDirector())
            _selectBox!!.setColor(Color4F.TEXT_BLACK)
            _selectBox!!.setContentSize(Size(306f, 384f))
            _selectBox!!.setPosition(22f, 45f)
            _imageView.addChild(_selectBox)
        }

        if (immediate) {
            if (_selectBox!=null) {
                _selectBox!!.setVisible(select)
                if (select) {
                    _selectBox!!.setLineWidth(18.0f)
                }
            }
        } else {
            if (_selectAction==null) {
                _selectAction = createSelectAction(getDirector())
                _selectAction!!.setTag(AppConst.TAG.USER+2)
            }

            _selectAction!!.select(select)
            runAction(_selectAction!!)
        }
    }

    fun setFocus() {
        val action = getActionByTag(AppConst.TAG.USER+3)
        if (action!=null) {
            stopAction(action)
        }

        if (_selectBox==null) {
            _selectBox = SMRectView.create(getDirector())
            _selectBox!!.setColor(Color4F.TEXT_BLACK)
            _selectBox!!.setContentSize(Size(306f, 384f))
            _selectBox!!.setPosition(22f, 54f)
            _selectBox!!.setLineWidth(ShaderNode.DEFAULT_ANTI_ALIAS_WIDTH*1.5f)
            _imageView.addChild(_selectBox!!)
        }

        val focusAction = createFocusAction(getDirector())
        focusAction.setTag(AppConst.TAG.USER+3)
        focusAction.setFocusTime(0.1f)
        runAction(focusAction)
    }

    fun getImageView(): SMImageView {return _imageView}

    override fun onStateChangePressToNormal() {
        setAnimOffset(Vec2.ZERO)
    }

    override fun onStateChangeNormalToPress() {
        setAnimOffset(Vec2(0f, 22f))
    }

    fun createShakeAction(director: IDirector): ShakeAction {
        val action = ShakeAction(director)
        action.initWithDuration(0f)
        return action
    }

    class ShakeAction(director: IDirector): ActionInterval(director) {
        private var _delay = 0f
        private var _shakeCount = 0f
        private var _shakeDir = 0
        fun getShakeAngle(t: Float): Float {
            val f = 1f - sin(t* M_PI_2.toFloat())
            return (_shakeDir * SHAKE_ANGLE * f * sin(_shakeCount * sin(t* M_PI_2.toFloat()) * M_PI_2.toFloat()))
        }

        override fun update(dt: Float) {
            if (_target is StickerItemThumbView) {
                val target = _target as StickerItemThumbView

                val imageView = target.getImageView()

                var t = dt
                var time = t * (getDuration() + _delay)
                if (time<_delay) {
                    return
                }

                time -= _delay
                t = time / _duration

                val deg = getShakeAngle(t)
                val hangFactor = 2.2f
                val size = imageView.getContentSize()


                val cx = size.width * 0.5f
                // ToDo anchorPoint를 조정 해 보자.. 조금 위쪽으로.... 0.5f -> 0.3f
                val cy = size.height * 0.5f

                val dx = (cx * cos(toRadians(deg) - M_PI_2)).toFloat()
                val dy = (cy * sin(toRadians(deg) - M_PI_2) * hangFactor).toFloat()

                val x = size.width * 0.5f
                val y = size.height * 0.5f

                imageView.setPosition(x-dx, y+dy+(cy*hangFactor))
                imageView.setRotation((deg/ M_PI).toFloat())
            }
        }

        fun setValue(delay: Float) {
            val duration = SHAKE_TIME + randomFloat(0.0f, 0.8f)
            setDuration(duration+delay)

            _delay = delay

            _shakeCount = randomFloat(6.0f, 10.0f)
            _shakeDir = if (randomFloat(0.0f, 1.0f) > 0.5f) 1 else -1
        }
    }

    fun createSelectAction(director: IDirector): SelectAction {
        val action = SelectAction(director)
        action.initWithDuration(0f)
        return action
    }

    class SelectAction(director: IDirector): DelayBaseAction(director) {
        var _from = 0f
        var _to = 0f
        var _select = false

        override fun onStart() {
            if (_target is StickerItemThumbView) {
                val target = _target as StickerItemThumbView

                _from = target._selectBox!!.getLineWidth()
                _to = if (_select) 18.0f else 0.0f

                target._selectBox!!.setVisible(true)
            }
        }

        override fun onUpdate(t: Float) {
            val lineWidth = interpolation(_from, _to, t)

            val target = _target as StickerItemThumbView
            target._selectBox!!.setLineWidth(lineWidth)
        }

        override fun onEnd() {
            if (!_select) {
                val target = _target as StickerItemThumbView
                target._selectBox!!.setVisible(false)
            }
        }

        fun select(select: Boolean) {
            _select = select

            if (select) {
                setTimeValue(0.15f, 0f)
            } else {
                setTimeValue(0.1f, 0f)
            }
        }
    }

    fun createFocusAction(director: IDirector): FocusAction {
        val action = FocusAction(director)
        action.initWithDuration(0f)
        return action
    }

    class FocusAction(director: IDirector): DelayBaseAction(director) {
        var _focusTime = 0f
        var _from = 0f

        override fun onStart() {
            if (_target is StickerItemThumbView) {
                val target = _target as StickerItemThumbView

                _from = target._selectBox!!.getLineWidth()
                target._selectBox!!.setVisible(true)
            }
        }

        override fun onUpdate(dt: Float) {
            var t = dt
            var time = t * getDuration()
            val lineWidth = if (time<0.15f) {
                t = time / 0.15f
                interpolation(_from, 18.0f, t)
            } else if (time<0.15f-_focusTime) {
                time -= 0.15f
                t = time / _focusTime
                18.0f
            } else {
                time -= 0.15f + _focusTime
                t = time / 0.1f
                interpolation(18f, 0f, t)
            }

            val target = _target as StickerItemThumbView
            target._selectBox!!.setLineWidth(lineWidth)
        }

        override fun onEnd() {
            val target = _target as StickerItemThumbView
            target._selectBox!!.setVisible(false)
        }

        fun setFocusTime(focusTime: Float) {
            _focusTime = focusTime
            setTimeValue(0.15f + focusTime + 0.15f, 0f)
        }
    }

    override fun onImageLoadComplete(sprite: Sprite?, tag: Int, direct: Boolean) {
        if (sprite!=null) {
            _imageView.setSprite(sprite)
            _spinner.setAlpha(0f)
            _spinner.runAction(FadeIn.create(getDirector(), 0.1f))

            startShowAction()
        }
        _spinner.setVisible(false)
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
            val iter = _downloadTask.iterator()
            while (iter.hasNext()) {
                val task = iter.next()
                if (task.isTargetAlive() && task.isRunning()) {
                    task.interrupt()
                }
            }

            _downloadTask.clear()
        }
    }

    override fun removeDownloadTask(task: ImageDownloadTask?) {
        synchronized(_downloadTask) {
            val iter = _downloadTask.iterator()
            while (iter.hasNext()) {
                val t = iter.next()
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
        synchronized(_downloadTask) {
            val iter = _downloadTask.iterator()
            while (iter.hasNext()) {
                val t = iter.next()
                if (!t.isTargetAlive()) {
                    _downloadTask.remove(t)
                } else if (task!=null && t.isRunning() && (t==task || task.getCacheKey().compareTo(t.getCacheKey())==0)) {
                    return false
                }
            }
            if (task==null) return false
            _downloadTask.add(task)
            return true
        }
    }
}