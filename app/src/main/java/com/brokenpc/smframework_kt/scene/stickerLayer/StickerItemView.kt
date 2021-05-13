package com.interpark.app.scene.stickerLayer

import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.SMView
import com.brokenpc.smframework.base.SceneParams
import com.brokenpc.smframework.base.sprite.BitmapSprite
import com.brokenpc.smframework.base.sprite.Sprite
import com.brokenpc.smframework.base.texture.Texture
import com.brokenpc.smframework.base.types.Color4F
import com.brokenpc.smframework.base.types.Size
import com.brokenpc.smframework.base.types.Vec2
import com.brokenpc.smframework.util.ImageManager.ImageDownloader
import com.interpark.smframework.util.ImageProcess.ImageProcessProtocol
import com.interpark.smframework.util.ImageProcess.ImageProcessTask
import com.interpark.smframework.util.ImageProcess.ImageProcessor
import com.interpark.smframework.util.ImageProcess.ImgPrcSimpleCapture
import com.interpark.smframework.view.Sticker.Sticker
import com.interpark.smframework.view.Sticker.StickerItem

class StickerItemView(director: IDirector): Sticker(director), ImageProcessProtocol {
    override var _imageProcessTask: ArrayList<ImageProcessTask> = ArrayList()
    private var _layout: SMView? = null
    private var _item: StickerItem? = null
    private var _isBlack = false
    private var _alpha = 1.0f
    private var _listener: StickerLayoutListener? = null
    private var _colorSelectable = false

    companion object {
        @JvmStatic
        fun createWithItem(director: IDirector, item: StickerItem, l: StickerLayoutListener): StickerItemView {
            val view = StickerItemView(director)
            view.initWithStickerItem(item, l)
            return view
        }
    }

    init {
        setControlType(ControlType.DELETE)
    }

    fun initWithStickerItem(item: StickerItem, l: StickerLayoutListener): Boolean {
        _item = item
        _listener = l

        setAnchorPoint(Vec2.MIDDLE)

        _isBlack = false

        val path = "${_item!!._rootPath}image/${_item!!._imageArrary[0]}.png"

        val sprite = BitmapSprite.createFromAsset(getDirector(), path, false, null)
        if (sprite!=null) {
            super.onImageLoadComplete(sprite, 0, true)
        }

        // ToDo delete color for test
//        setBackgroundColor(Color4F(1f, 0f, 0f, 0.4f))

        return true
    }

    fun isBlack(): Boolean {return _isBlack}

    fun setAlphaValue(alpha: Float) {
        val a = 0.0f.coerceAtLeast(1.0f.coerceAtMost(alpha))
        setAlpha(0.1f + 0.9f+a)
        _alpha = a
    }

    fun getAlphaValue(): Float {return _alpha}

    interface StickerLayoutListener {
        fun onStickerLayout(itemView: StickerItemView, sprite: Sprite?, item: StickerItem, colorIndex: Int)
    }

    override fun onImageLoadComplete(sprite: Sprite?, tag: Int, direct: Boolean) {
        super.onImageLoadComplete(sprite, tag, direct)

        if (getSprite()!=null) {
            if (tag==0 && isBlack()) {
                getSprite()!!.setColor(Color4F.BLACK)
            }

            if (_item!!._layout>0 && _listener!=null) {
                _listener?.onStickerLayout(this, getSprite() as Sprite, _item!!, if (isBlack()) 1 else 0)
            }
        }
    }

    fun setBlack() {
        if (isBlack()) return

        if (_item==null) return

        _isBlack = true


        if (_item!!._imageArrary.size>=2) {
            resetDownload()
            val path = "${_item!!._rootPath}sticker/${_item!!._imageArrary[1]}.png"
            ImageDownloader.getInstance().loadImageFromResource(this, path, 1, ImageDownloader.NO_DISK)
        } else {
            setColor(Color4F.BLACK)
            getSprite()?.setColor(Color4F.BLACK)

            if (_item!!._layout>0 && getSprite()!=null) {
                _listener?.onStickerLayout(this, getSprite() as Sprite, _item!!, 1)
            }
        }
    }

    fun setWhite() {
        if (!_isBlack) return

        if (_item==null) return

        _isBlack = false

        if (_item!!._imageArrary.size>=2) {
            resetDownload()
            val path = "${_item!!._rootPath}sticker/${_item!!._imageArrary[0]}.png"
            ImageDownloader.getInstance().loadImageFromResource(this, path, 0, ImageDownloader.NO_DISK)
        } else {
            setColor(Color4F.WHITE)
            getSprite()?.setColor(Color4F.WHITE)

            if (_item!!._layout>0 && getSprite()!=null) {
                _listener?.onStickerLayout(this, getSprite() as Sprite, _item!!, 0)
            }
        }
    }

    override fun onImageProcessComplete(
        tag: Int,
        success: Boolean,
        sprite: Sprite?,
        params: SceneParams?
    ) {
        clearLayout()
        setSprite(sprite, true)
        setColor(Color4F.WHITE)
    }

    override fun onImageCaptureComplete(
        tag: Int,
        texture: Texture?,
        data: ByteArray?,
        size: Size,
        bpp: Int
    ) {
        // Nothing To Do
    }

    fun clearLayout() {
        if (_layout!=null) {
            removeChild(_layout)
            _layout = null
        }
    }

    fun setLayout(view: SMView) {
        clearLayout()
        view.setCascadeAlphaEnable(true)
        addChild(view)
        _layout = view
    }

    fun isColorSelectable(): Boolean {return _colorSelectable}

    fun setColorSelectable(colorSelectable: Boolean) {_colorSelectable=colorSelectable}

    fun prepareRemove() {
        if (_layout!=null) {
            ImageProcessor.getInstance().executeImageProcess(this, this, ImgPrcSimpleCapture(), 0)
        }
    }

    override fun onImageProcessProgress(tag: Int, progress: Float) {
        //
    }

    override fun resetImageProcess() {
        val iter = _imageProcessTask.iterator()
        while (iter.hasNext()) {
            val task = iter.next()
            if (task.isRunning()) {
                task.interrupt()
            }
        }

        _imageProcessTask.clear()
    }

    override fun removeImageProcessTask(task: ImageProcessTask) {
        val iter = _imageProcessTask.iterator()
        while (iter.hasNext()) {
            val iterTask = iter.next()
            if (!iterTask.isTargetAlive()) {
                _imageProcessTask.remove(iterTask)
            } else if (task==iterTask) {
                task.interrupt()
                _imageProcessTask.remove(iterTask)
            }
        }
    }

    override fun addImageProcessTask(task: ImageProcessTask): Boolean {
        val iter = _imageProcessTask.iterator()
        while (iter.hasNext()) {
            val iterTask = iter.next()
            if (!iterTask.isTargetAlive()) {
                _imageProcessTask.remove(iterTask)
            } else if (task==iterTask && iterTask.isRunning()) {
                return false
            }
        }

        _imageProcessTask.add(task)
        return true
    }
}