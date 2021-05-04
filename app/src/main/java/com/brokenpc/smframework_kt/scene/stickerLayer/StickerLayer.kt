package com.interpark.app.scene.stickerLayer

import android.view.MotionEvent
import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.SMView
import com.brokenpc.smframework.base.sprite.GridSprite
import com.brokenpc.smframework.base.sprite.Sprite
import com.brokenpc.smframework.base.types.Color4F
import com.brokenpc.smframework.base.types.Size
import com.brokenpc.smframework.base.types.Vec2
import com.brokenpc.smframework.shader.ShaderNode
import com.brokenpc.smframework.view.SMImageView
import com.brokenpc.smframework.view.SMRectView
import com.interpark.smframework.view.SMZoomView
import com.interpark.smframework.view.Sticker.StickerCanvasView
import com.interpark.smframework.view.Sticker.StickerControlView
import kotlin.math.atan2

class StickerLayer(director: IDirector) : SMView(director), StickerCanvasView.StickerCanvasListener, StickerControlView.StickerControlListener {
    private val _contentView = create(director)
    private val _zoomView = SMZoomView.create(director)
    private val _controlView = StickerControlView.create(director)
    private val _canvasView = StickerCanvasView.create(director)
    private val _bgImageView = SMImageView.create(director)
    private var _canvasListener: StickerCanvasView.StickerCanvasListener? = null
    private var _controlListener: StickerControlView.StickerControlListener? = null
    private var _gridSprite: GridSprite? = null

    companion object {
        @JvmStatic
        fun create(director: IDirector, contentSize: Size): StickerLayer {
            val layer = StickerLayer(director)
            layer._contentSize.set(contentSize)
            layer.init()
            return layer
        }

        @JvmStatic
        fun create(director: IDirector, sprite: Sprite?, contentSize: Size): StickerLayer {
            val layer = StickerLayer(director)
            layer._contentSize.set(contentSize)
            layer.initWithSprite(sprite)
            return layer
        }
    }

    override fun init(): Boolean {
        if (!super.init()) {
            return false
        }

        val s = getContentSize()

        _zoomView.setContentSize(s)
        _zoomView.setPadding(30.0f)
        addChild(_zoomView)

        _controlView.setContentSize(s)
        _controlView.setStickerControlListener(this)
        addChild(_controlView)

        _contentView.setBackgroundColor(Color4F(1f, 1f, 1f, 0.6f))
        if (_gridSprite!=null) {
            _contentView.setContentSize(_gridSprite!!.getContentSize())
        } else {
            _contentView.setContentSize(s)
        }
        _zoomView.setContentView(_contentView)

        _bgImageView.setAnchorPoint(Vec2.MIDDLE)
        if (_gridSprite!=null) {
            _bgImageView.setSprite(_gridSprite)
            _bgImageView.setContentSize(_gridSprite!!.getContentSize())
            _bgImageView.setPosition(_contentView.getContentSize().divide(2f))
        } else {
            _bgImageView.setContentSize(s)
            _bgImageView.setPosition(s.divide(2f))
        }
        // ToDo... delete this after check.
        _bgImageView.setBackgroundColor(1f, 0f, 0f, 0.4f)

        _contentView.addChild(_bgImageView)

        val rect = SMRectView.create(getDirector())
        if (_gridSprite!=null) {
            rect.setContentSize(_gridSprite!!.getContentSize())
        } else {
            rect.setContentSize(s)
        }
        rect.setLineWidth(ShaderNode.DEFAULT_ANTI_ALIAS_WIDTH*3f)
        rect.setColor(Color4F.XDBDCDF)
        _bgImageView.addChild(rect)

        _canvasView.setContentSize(s)
        _canvasView.setAnchorPoint(Vec2.MIDDLE)
        _canvasView.setPosition(_contentView.getContentSize().divide(2f))
        _canvasView.setStickerCanvasListener(this)
        _canvasView.addChild(_canvasView)

        return true
    }

    fun initWithSprite(sprite: Sprite?): Boolean {
        if (sprite!=null) {
            _gridSprite = GridSprite.create(getDirector(), sprite)
        }

        return init()
    }

    fun setStickerListener(canvasListener: StickerCanvasView.StickerCanvasListener, controlListener: StickerControlView.StickerControlListener) {
        _canvasListener = canvasListener
        _controlListener = controlListener
    }

    fun startGeineRemove(view: SMView?) {
        if (view is StickerItemView) {
            val sticker = view as StickerItemView

            if (sticker.getSprite()==null) return

            val sprite = sticker.getSprite() as Sprite

            _controlView.startGeineRemove(view)
            _canvasView.removeChildWithGenieAction(sticker, sprite, Vec2(0f, 1f), 0.5f, 0.01f)
        }
    }

    fun addSticker(sticker: SMView?) {
        if (sticker!=null) {
            _canvasView.addChild(sticker)
        }
    }

    fun addStickerAboveAt(sticker: SMView?, aboveAt: SMView?) {
        if (sticker!=null && aboveAt!=null) {
            _canvasView.addChild(sticker)
            reorderStickerAboveAt(sticker, aboveAt)
        }
    }

    fun reorderStickerAboveAt(sticker: SMView, aboveAt: SMView?) {
        if (aboveAt!=null) {
            _canvasView.aboveView(sticker, aboveAt)
        } else {
            _canvasView.sendChildToBack(sticker)
        }
    }

    fun removeSticker(sticker: SMView?) {
        if (sticker!=null) {
            _canvasView.removeChild(sticker)
        }
    }

    fun removeStickerWithFadeOut(sticker: SMView?, duration: Float, delay: Float) {
        if (sticker!=null) {
            _canvasView.removeChildWithFadeOut(sticker, duration, delay)
        }
    }

    fun removeAllStickerWithFly() {
        val children = _canvasView.getChildren()
        val pt1 = Vec2(_contentSize.divide(2f))

        for (view in children) {
            if (view !is StickerItemView) {
                continue
            }

            val sticker = view as StickerItemView
            val pt2 = sticker.getPosition()

            val radians = atan2(pt2.y-pt1.y, pt2.x-pt1.x)
            var degrees = toDegrees(radians)

            if (degrees>90 && degrees<120) {
                degrees += randomFloat(0f, 0.3f) * 100f
            }
            if (degrees<90 && degrees>60) {
                degrees -= randomFloat(0f, 0.3f) * 100f
            }

            _canvasView.removeChildWithFly(sticker, degrees, randomFloat(0.7f, 0.8f)*10000f)
        }
    }

    fun removeAllSticker() {
        val children = _contentView.getChildren()

        val size = children.size
        for (i in size-1 downTo 0) {
            if (children[i] is StickerItemView) {
                val sticker = children[i] as StickerItemView
                removeSticker(sticker)
            }
        }
    }

    fun getBgImageView(): SMImageView {return _bgImageView}

    fun getCanvas(): StickerCanvasView {return _canvasView}

    fun getControl(): StickerControlView {return _controlView}

    fun getZoomView(): SMZoomView {return _zoomView}

    fun getContentView(): SMView {return _contentView}

    fun setZoomStatus(panX: Float, panY: Float, zoomScale: Float, duration: Float) {
        _zoomView.setZoomWithAnimation(panX, panY, zoomScale, duration)
    }

    fun cancelTouch() {cancel()}

    override fun containsPoint(point: Vec2): Boolean {
        return true
    }

    override fun containsPoint(x: Float, y: Float): Boolean {
        return true
    }

    override fun dispatchTouchEvent(event: MotionEvent?, view: SMView, checkBounds: Boolean): Int {
        return super.dispatchTouchEvent(event, view, false)
    }

    override fun onStickerDoubleClicked(view: SMView?, worldPoint: Vec2) {
        _zoomView.performDoubleClick(worldPoint)

        _canvasListener?.onStickerDoubleClicked(view, worldPoint)
    }

    override fun onStickerMenuClick(sticker: SMView?, menuId: Int) {
        _controlListener?.onStickerMenuClick(sticker, menuId)
    }

    override fun onStickerRemoveBegin(view: SMView?) {
        if (view!=null && view==_canvasView.getSelectedSticker()) {
            _controlView.linkStickerView(null)
        }

        _canvasListener?.onStickerRemoveBegin(view)
    }

    override fun onStickerRemoveEnd(view: SMView?) {
        if (view!=null && view==_canvasView.getSelectedSticker()) {
            _controlView.linkStickerView(null)
        }

        _canvasListener?.onStickerRemoveEnd(view)
    }

    override fun onStickerSelected(view: SMView?, select: Boolean) {
        _controlView.linkStickerView(if (select) {view} else {null})

        _canvasListener?.onStickerSelected(view, select)
    }

    override fun onStickerTouch(view: SMView?, action: Int) {
        _canvasListener?.onStickerTouch(view, action)
    }
}