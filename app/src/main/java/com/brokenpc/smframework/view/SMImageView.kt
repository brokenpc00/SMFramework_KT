package com.brokenpc.smframework.view

import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.DrawNode
import com.brokenpc.smframework.base.UIContainerView
import com.brokenpc.smframework.base.sprite.BitmapSprite
import com.brokenpc.smframework.base.sprite.Sprite
import com.brokenpc.smframework.base.texture.Texture
import com.brokenpc.smframework.base.types.Mat4
import com.brokenpc.smframework.base.types.Rect
import com.brokenpc.smframework.base.types.Size
import com.brokenpc.smframework.base.types.Vec2
import com.brokenpc.smframework.util.AppConst
import com.brokenpc.smframework.util.ImageManager.IDownloadProtocol
import com.brokenpc.smframework.util.ImageManager.ImageDownloadTask
import com.brokenpc.smframework.util.ImageManager.ImageDownloader

open class SMImageView : UIContainerView, IDownloadProtocol {

    protected var _sprite:DrawNode? = null
    protected var _iconVisible:Boolean = true
    private var _scaleType:ScaleType = ScaleType.FIT_CENTER
    private var _gravity: Int = 0
    private var _maxAreaRatio: Float = 0f
    private var _isDownloadImageView: Boolean = false
    private var _imageScale: Float = 1f
    private var _spriteScaleX: Float = 1f
    private var _spriteScaleY: Float = 1f
    private var _spritePosition: Vec2 = Vec2(Vec2.ZERO)
    private var _imageRect: Rect = Rect(Rect.ZERO)
    private var _clipping: Boolean = false

    private val _realSpritePosition = Vec2(Vec2.ZERO)
    private val _newSpritePosition = Vec2(Vec2.ZERO)
    private val _animSpritePosition = Vec2(Vec2.ZERO)
    private val _animSpriteSize = Vec2(Vec2.ZERO)

    private val _realSpriteSize = Size(Size.ZERO)
    private val _newSpriteSize = Size(Size.ZERO)
    private val _newAnimSpritePosition = Size(Size.ZERO)
    private val _newAnimSpriteSize = Size(Size.ZERO)

    private val _downloadTask = ArrayList<ImageDownloadTask>()

    companion object {
        const val GRAVITY_LEFT = 1
        const val GRAVITY_RIGHT = GRAVITY_LEFT.shl(1)
        const val GRAVITY_CENTER_HORIZONTAL = GRAVITY_LEFT.or(GRAVITY_RIGHT)
        const val GRAVITY_TOP = GRAVITY_LEFT.shl(2)
        const val GRAVITY_BOTTOM = GRAVITY_RIGHT.shl(3)
        const val GRAVITY_CENTER_VERTICAL = GRAVITY_TOP.or(GRAVITY_BOTTOM)
        const val FLAG_CONTENT_SIZE = 1L
        const val ACTION_TAG_SHOW = AppConst.TAG.USER+1
        const val ACTION_TAG_DIM = AppConst.TAG.USER+2

        @JvmStatic
        fun create(director: IDirector): SMImageView {
            val imageView = SMImageView(director)
            imageView.init()
            return imageView
        }

        @JvmStatic
        fun create(director: IDirector, assetName: String): SMImageView {
            return create(director, assetName, false)
        }

        @JvmStatic
        fun create(director: IDirector, assetName: String, isNetwork: Boolean): SMImageView {
            val imageView = SMImageView(director, assetName, isNetwork)
            if (!isNetwork) {
                if (imageView.getContentSize().width==0f && imageView.getContentSize().height==0f) {
                    imageView.setContentSize(
                        imageView.getSprite()?.getWidth(),
                        imageView.getSprite()?.getHeight()
                    )
                }
            }

            return imageView
        }

        @JvmStatic
        fun created(
            director: IDirector,
            assetName: String,
            x: Float,
            y: Float,
            width: Float,
            height: Float
        ): SMImageView {
            val view = SMImageView(director, assetName)
            view.setContentSize(width, height)
            view.setPosition(x, y)
            view.setAnchorPoint(Vec2.ZERO)
            return view
        }

        @JvmStatic
        fun create(director: IDirector, sprite: Sprite): SMImageView {
            val view = SMImageView(director, sprite)
            view.setContentSize(Size.ZERO)
            view.setPosition(Vec2.ZERO)
            view.setAnchorPoint(Vec2.ZERO)
            return view
        }

        @JvmStatic
        fun create(
            director: IDirector,
            sprite: Sprite,
            x: Float,
            y: Float,
            width: Float,
            height: Float
        ): SMImageView {
            val view = SMImageView(director, sprite)
            view.setContentSize(width, height)
            view.setPosition(x, y)
            view.setAnchorPoint(Vec2.ZERO)
            return view
        }
    }

    enum class ScaleType {
        CENTER,
        CENTER_INSIDE,
        CENTER_CROP,
        FIT_XY,
        FIT_CENTER
    }

    constructor(director: IDirector) : super(director) {

    }
    constructor(director: IDirector, sprite: Sprite?) : super(director) {
        setSprite(sprite)
    }
    constructor(director: IDirector, assetName: String) : super(director) {
        val sprite = BitmapSprite.createFromAsset(getDirector(), assetName, true, null)
        setSprite(sprite)
    }
    constructor(director: IDirector, assetName: String, isNetwork: Boolean) : super(director) {
        if (isNetwork) {
            ImageDownloader.getInstance().loadImageFromNetwork(
                this,
                assetName,
                0,
                ImageDownloader.DEFAULT
            )
        } else {
            val sprite = BitmapSprite.createFromAsset(getDirector(), assetName, true, null)
            setSprite(sprite)
        }
    }
    constructor(director: IDirector, sprite: DrawNode?) : super(director) {
        setSprite(sprite)
    }
    constructor(director: IDirector, texture: Texture) : super(director) {
        val sprite = Sprite(director, texture, 0f, 0f)
        setSprite(sprite)
    }
    constructor(director: IDirector, x: Float, y: Float, width: Float, height: Float) : super(
        director
    ) {
        setPosition(x, y)
        setContentSize(width, height)
    }
    constructor(
        director: IDirector,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        anchorX: Float,
        anchorY: Float
    ) : super(director) {
        setPosition(x, y)
        setContentSize(width, height)
        setAnchorPoint(anchorX, anchorY)
    }

    fun updateData() {
        _imageRect = Rect(Vec2.ZERO, Size(_sprite?.getWidth(), _sprite?.getHeight()))
        computeContentSize()
    }

    fun setSprite(sprite: DrawNode?) {
        setSprite(sprite, true)
    }

    fun setSprite(sprite: DrawNode?, fitSize: Boolean) {
        if (_sprite!=sprite) {
            _sprite?.releaseResources()

            _sprite = sprite

            if (_sprite!=null) {
                _imageRect = Rect(Vec2.ZERO, _sprite!!.getContentSize())

                registerUpdate(FLAG_CONTENT_SIZE)
            }
        }

        if (fitSize && _sprite!=null) {
            _imageScale = 1f
            setContentSize(_sprite!!.getContentSize())
        }

        _onSpriteSetCallback?.onSpriteSetCallback(this, _sprite)
    }

    interface OnSpriteSetCallback {
        fun onSpriteSetCallback(view: SMImageView, sprite: DrawNode?)
    }
    var _onSpriteSetCallback: OnSpriteSetCallback? = null
    fun setOnSpriteSetCallback(callback: OnSpriteSetCallback) {_onSpriteSetCallback = callback}

    interface OnSpriteLoadedCallback {
        fun onSpriteLoadedCallback(view: SMImageView, sprite: DrawNode?): DrawNode
    }
    var _onSpriteLoadedCallback: OnSpriteLoadedCallback? = null
    fun setOnSpriteLoadedCallback(callback: OnSpriteLoadedCallback) {_onSpriteLoadedCallback=callback}

    override fun setContentSize(size: Size) {
        super.setContentSize(size)
        registerUpdate(FLAG_CONTENT_SIZE)
    }

    override fun setContentSize(width: Float?, height: Float?) {
        super.setContentSize(width, height)
        registerUpdate(FLAG_CONTENT_SIZE)
    }

    override fun setContentSize(size: Size, immediate: Boolean) {
        super.setContentSize(size, immediate)
        registerUpdate(FLAG_CONTENT_SIZE)
    }

    fun getSprite(): DrawNode? {return _sprite}

    fun fitSpriteBounds() {
        if (_sprite!=null) {
            setPosition(getX(), getY())
            setContentSize(_sprite?.getWidth(), _sprite?.getHeight())
        }
    }

    fun setScaleType(scaleType: ScaleType) {
        if (_scaleType!=scaleType) {
            _scaleType = scaleType
            registerUpdate(FLAG_CONTENT_SIZE)
        }
    }

    override fun onUpdateOnVisit() {
        if (isUpdate(FLAG_CONTENT_SIZE)) {
            computeContentSize()
            unregisterUpdate(FLAG_CONTENT_SIZE)
        }
    }

    override fun draw(m: Mat4, flags: Int) {
        if (_sprite!=null) {
            drawImage(
                _spritePosition.x,
                _spritePosition.y,
                _spriteScaleX * _imageScale,
                _spriteScaleY * _imageScale
            )
        }
    }

    protected fun drawImage(x: Float, y: Float, scaleX: Float, scaleY: Float) {
        setRenderColor()
        _sprite?.drawScaleXY(x, y, scaleX, scaleY)
    }

    fun computeContentSize() {
        if (_sprite==null) return

        val vsize = Size(_uiContainer.getContentSize())

        if (vsize.width<=0 || vsize.height<=0) return

        val ssize = _imageRect.size

        if (ssize.width<=0 || ssize.height<=0) return

        var scaleX = 1f
        var scaleY = 1f

        when (_scaleType) {
            ScaleType.CENTER -> {
                scaleX = 1f
                scaleY = 1f
            }
            ScaleType.CENTER_INSIDE -> {
                scaleX = (vsize.width / ssize.width).coerceAtMost(vsize.height / ssize.height).coerceAtMost(1f).also { scaleY = it }
            }
            ScaleType.CENTER_CROP -> {
                scaleX = (vsize.width / ssize.width).coerceAtLeast(vsize.height / ssize.height).also { scaleY = it }
            }
            ScaleType.FIT_XY -> {
                scaleX = vsize.width / ssize.width
                scaleY = vsize.height / ssize.height
            }
            else -> {
                scaleX = (vsize.width/ssize.width).coerceAtMost(vsize.height / ssize.height).also { scaleY = it }
            }
        }

        val sw = ssize.width * scaleX
        val sh = ssize.height * scaleY

        val origin = Vec2(vsize.width / 2f - sw / 2f, vsize.height / 2f - sh / 2f)

        if (_gravity>0) {
            if (_gravity.and(GRAVITY_CENTER_HORIZONTAL)>0) {
                if (_gravity.and(GRAVITY_LEFT)>0 && _gravity.and(GRAVITY_RIGHT)==0) {
                    // attach left
                    origin.x = 0f
                } else if (_gravity.and(GRAVITY_RIGHT)>0 && _gravity.and(GRAVITY_LEFT)==0) {
                    // attach right
                    origin.x = vsize.width - sw
                }
            }

            if (_gravity.and(GRAVITY_CENTER_VERTICAL)>0) {
                if (_gravity.and(GRAVITY_TOP)>0 && _gravity.and(GRAVITY_BOTTOM)==0) {
                    // attach top
                    origin.y = 0f
                } else if (_gravity.and(GRAVITY_BOTTOM)>0 && _gravity.and(GRAVITY_TOP)==0) {
                    // attach bottom
                    origin.y = vsize.height - sh
                }
            }
        }

        // clipping... 나중에 sprite의 texture rect을 설정할 수 있으면 clipping한다.

        if (_clipping && (sw > vsize.width || sh > vsize.height)) {

        } else {

        }


        val w = _imageRect.size.width * scaleX
        val h = _imageRect.size.height * scaleY

        val x = origin.x
        val y = origin.y

        if (_maxAreaRatio>0 && _maxAreaRatio<1) {
            val ratio = (w*h) / (_contentSize.width*_contentSize.height)
            if (ratio>_maxAreaRatio) {
                val newScale = _maxAreaRatio / ratio
                scaleX *= newScale
                scaleY *= newScale
            }
        }

        _spritePosition.set(x, y)
        _spriteScaleX = scaleX
        _spriteScaleY = scaleY
    }

    fun getContentsScaleX(): Float {return _spriteScaleX}
    fun getContentsScaleY(): Float {return _spriteScaleY}

    fun convertContentsXtoViewX(x: Float): Float {return x*_spriteScaleX + _spritePosition.x}
    fun convertContentsYtoViewY(y: Float): Float {return y*_spriteScaleY + _spritePosition.y}

    fun convertContentsScaleXtoViewScaleX(scaleX: Float): Float {return scaleX*_spriteScaleX}
    fun convertContentsScaleYtoViewScaleY(scaleY: Float): Float {return scaleY*_spriteScaleY}

    fun convertViewXtoContentsX(x: Float): Float {return (x-_spritePosition.x)/_spriteScaleX}
    fun convertViewYtoContentsY(y: Float): Float {return (y-_spritePosition.y)/_spriteScaleY}

    fun convertViewScaleXtoContentsScaleX(scaleX: Float): Float {return scaleX/_spriteScaleX}
    fun convertViewScaleYtoContentsScaleY(scaleY: Float): Float {return scaleY/_spriteScaleY}

    fun setImageScale(imageScale: Float) {_imageScale=imageScale}

    override fun onImageLoadComplete(sprite: Sprite?, tag: Int, direct: Boolean) {
        if (sprite!=null) {
            setSprite(sprite)
            if (getContentSize().width==0f && getContentSize().height==0f) {
                setContentSize(getSprite()?.getWidth(), getSprite()?.getHeight())
            }
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
        if (task==null) return
        synchronized(_downloadTask) {
            val iter = _downloadTask.listIterator()
            while (iter.hasNext()) {
                val t = iter.next()
                if (!t.isTargetAlive()) {
                    _downloadTask.remove(t)
                } else if (t == task || task.getCacheKey().compareTo(t.getCacheKey())==0) {
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
            val iter = _downloadTask.listIterator()
            while (iter.hasNext()) {
                val t = iter.next()
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

    fun setMaxAreaRatio(ratio: Float) {_maxAreaRatio = ratio}

    fun setGravity(gravity: Int) {setGravity(gravity, true)}
    fun setGravity(gravity: Int, immediate: Boolean) {
        _gravity = gravity
        registerUpdate(FLAG_CONTENT_SIZE)
    }

    protected fun setClipping(clipping: Boolean) {
        if (_clipping==clipping) return

        _clipping=clipping
        if (_sprite!=null) {
            registerUpdate(FLAG_CONTENT_SIZE)
        }
    }

}