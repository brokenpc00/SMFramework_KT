package com.brokenpc.smframework.base

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.opengl.GLES20
import android.view.MotionEvent
import com.brokenpc.smframework_kt.BuildConfig
import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.sprite.CanvasSprite
import com.brokenpc.smframework.base.types.*
import com.brokenpc.smframework.util.AppConst
import com.brokenpc.smframework.util.OpenGlUtils
import com.brokenpc.smframework.view.SMSolidRectView
import com.brokenpc.smframework.view.ViewConfig
import kotlin.math.*

open class SMView : Ref {

    constructor(director: IDirector) : super(director) {
        _updateFlags = 0L
        _actionManager = _director!!.getActionManager()
        _scheduler = _director!!.getScheduler()
        _scaledDoubleTouchSlope = ViewConfig.getScaledDoubleTouchSlop(director)
    }

    constructor(director: IDirector, x: Float, y: Float, width: Float, height: Float) : super(director) {
        setPosition(x, y)
        setContentSize(width, height)
        _updateFlags = 0L
        _actionManager = _director!!.getActionManager()
        _scheduler = _director!!.getScheduler()
        _scaledDoubleTouchSlope = ViewConfig.getScaledDoubleTouchSlop(director)
    }

    init {
        _parent = null
    }

    enum class Direction {
        UP, LEFT, DOWN, RIGHT
    }

    // for static property
    companion object {
        var _matrix:Matrix = Matrix()

        val FLAGS_TRANSFORM_DIRTY: Long = 1
        val FLAGS_CONTENT_SIZE_DIRTY: Long = FLAGS_TRANSFORM_DIRTY.shl(1)
        val FLAGS_RENDER_AS_3D: Long = FLAGS_TRANSFORM_DIRTY.shl(3)
        val FLAGS_DIRTY_MASK: Long = FLAGS_TRANSFORM_DIRTY.or(FLAGS_CONTENT_SIZE_DIRTY)
        var s_globalOrderOfArrival: Int = 0
        var __attachedNodeCount: Int = 0

        val VISIBLE: Int = android.view.View.VISIBLE
        val INVISIBLE: Int = android.view.View.INVISIBLE

        val DEFAULT_PUSHDOWN_HEIGHT: Float = 10.0f

        val ANIM_TIME_SHOW: Long = 200
        val ANIM_TIME_HIDE: Long = 150

        var VIEWFLAG_POSITION: Long = 1
        var VIEWFLAG_SCALE: Long = VIEWFLAG_POSITION.shl(1)
        var VIEWFLAG_ROTATE: Long = VIEWFLAG_POSITION.shl(2)
        var VIEWFLAG_CONTENT_SIZE: Long = VIEWFLAG_POSITION.shl(3)
        var VIEWFLAG_COLOR: Long = VIEWFLAG_POSITION.shl(4)

        var VIEWFLAG_ANIM_OFFSET: Long = VIEWFLAG_POSITION.shl(5)
        var VIEWFLAG_ANIM_SCALE: Long = VIEWFLAG_POSITION.shl(6)
        var VIEWFLAG_ANIM_ROTATE: Long = VIEWFLAG_POSITION.shl(7)
        var VIEWFLAG_ANIM_CONTENT_SIZE: Long = VIEWFLAG_POSITION.shl(8)
        var VIEWFLAG_ANIM_COLOR: Long = VIEWFLAG_POSITION.shl(9)

        var VIEWFLAG_USER_SHIFT: Long = 8

        var TOUCH_MASK_CLICK: Long = 1
        var TOUCH_MASK_DOUBLECLICK: Long = TOUCH_MASK_CLICK.shl(1)
        var TOUCH_MASK_LONGCLICK: Long = TOUCH_MASK_CLICK.shl(2)
        var TOUCH_MASK_TOUCH: Long = TOUCH_MASK_CLICK.shl(3)

        val COLOR_TOLERANCE:Float = 0.0005f
        val SMOOTH_DIVIDER:Float = 3.0f

//        val pi:Double = Math.PI
        val M_PI:Double = 3.14159265358979323846
        val M_PI_2:Double = M_PI/2
        val M_PI_4:Double = M_PI/4
        val M_1_PI:Double = 1f/M_PI
        val M_2_PI:Double = 2f/M_PI
        val M_PI_X_2:Double = M_PI * 2f
        const val TOUCH_FALSE:Int = 0
        const val TOUCH_TRUE:Int = 1
        const val TOUCH_INTERCEPT:Int = 2

        @JvmStatic
        fun USER_VIEW_FLAG(flagId: Long): Long {
            return (1L).shl((VIEWFLAG_USER_SHIFT+flagId).toInt())
        }

        @JvmStatic
        fun create(director: IDirector): SMView {
            return create(director, 0f, 0f, 0f, 0f)
        }

        @JvmStatic
        fun create(director: IDirector, x: Float, y: Float, width: Float, height: Float):SMView {
            return create(director, 0, x, y, width, height)
        }

        @JvmStatic
        fun create(director: IDirector, tag: Int, x:Float, y:Float, width: Float, height: Float):SMView {
            return create(director, tag, x, y, width, height, 0f, 0f)
        }


        @JvmStatic
        fun create(director: IDirector, tag: Int, x: Float, y: Float, width: Float, height: Float, anchorX: Float, anchorY: Float): SMView {
            val view: SMView = SMView(director)
            view.setPosition(x, y)
            view.setContentSize(Size(width, height))
            view.setAnchorPoint(Vec2(anchorX, anchorY))
            if (view.init()) {
                view.setTag(tag)
            } else {
                view.setTag(-1)
            }

            return view
        }

        // util
        @JvmStatic
        fun MakeColor4F(rgb:Int, alpha:Float):Color4F {
            val r = rgb.and(0xff0000).shr(16)/255.0f
            val g = rgb.and(0x00ff00).shr(8)/255.0f
            val b = rgb.and(0x0000ff)/255.0f

            return Color4F(r, g, b, alpha)
        }

        @JvmStatic
        fun MakeColor4B(rgba:Int):Color4B {
            val r:Int = (rgba.and(0xff000000.toInt()).shr(24)/255.0).toInt()
            val g:Int = (rgba.and(0x00ff0000).shr(16)/255.0f).toInt()
            val b:Int = (rgba.and(0x0000ff00).shr(8)/255.0f).toInt()
            val a:Int = (rgba.and(0x000000ff)/255.0f).toInt()

            return Color4B(r, g, b, a)
        }

        @JvmStatic
        fun interpolation(from:Float, to:Float, t:Float):Float {
            return from + (to-from)*t
        }

        @JvmStatic
        fun smoothInterpolate(from: Float, to: Float, tolerance:Float):InterpolateRet {
            return smoothInterpolate(from, to, tolerance, SMView.SMOOTH_DIVIDER)
        }

        @JvmStatic
        fun smoothInterpolate(from: Float, to: Float, tolerance: Float, smoothDivider:Float):InterpolateRet {
            var retFrom:Float = from
            if (retFrom!=to) {
                retFrom = from + (to-from) / smoothDivider

                if (abs(from-to) < tolerance) {
                    retFrom = to
                    return InterpolateRet(false, retFrom)
                }
                return InterpolateRet(true, retFrom)
            }
            return InterpolateRet(false, retFrom)
        }

        @JvmStatic
        fun smoothInterpolateRotate(from: Float, to: Float, tolerance: Float):Boolean {
            var retFrom:Float = from
            if (retFrom != to) {
                val diff:Float = getShortestAngle(retFrom, to)
                if (abs(diff) < tolerance) {
                    retFrom = to
                    return false
                }
                retFrom += diff / SMOOTH_DIVIDER
                return true
            }
            return false
        }

        @JvmStatic
        fun getShortestAngle(from: Float, to: Float):Float {
            return ((((to - from) % 360) + 540) % 360) - 180
        }

        @JvmStatic
        fun sortNodes(nodes: ArrayList<SMView>) {
            nodes.sortWith(Comparator { a, b -> if (a._localZOrder < b._localZOrder) -1 else if (a._localZOrder > b._localZOrder) 1 else 0 })
        }

        @JvmStatic
        fun getRandomColorB():Int {return randomInt(0, 255)}

        @JvmStatic
        fun getRandomColor4F():Color4F {
            return Color4F(getRandomColorF(), getRandomColorF(), getRandomColorF(), 1f)
        }

        @JvmStatic
        fun getRandomColorF():Float {return randomInt(0, 255).toFloat()/255f}

        @JvmStatic
        fun randomInt(min:Int, max:Int):Int {
            return min + (Math.random()*(max-min)).toInt()
        }

        @JvmStatic
        fun randomFloat(min: Float, max: Float): Float {
            return min + (Math.random()*(max-min)).toFloat()
        }

        @JvmStatic
        fun getDecelateInterpolation(t: Float):Float {
            return (1.0f - (1.0f - t) * (1.0f - t))
        }

        @JvmStatic
        fun getDecelateInterpolation(t: Float, fractor:Float):Float {
            return (1f - Math.pow((1f-t).toDouble(), 2f*fractor.toDouble()).toFloat())
        }

        @JvmStatic
        fun interpolateColor4F(from:Color4F, to:Color4F, t:Float):Color4F {
            val a:Float = interpolation(from.a, to.a, t)
            val r:Float = interpolation(from.r, to.r, t)
            val g:Float = interpolation(from.g, to.g, t)
            val b:Float = interpolation(from.b, to.b, t)

            return Color4F(r, g, b, a)
        }

        @JvmStatic
        fun interpolateColor4B(from: Color4B, to: Color4B, t:Float):Color4B {
            val r:Int = interpolation(from.r.toFloat(), to.r.toFloat(), t).toInt()
            val g:Int = interpolation(from.g.toFloat(), to.g.toFloat(), t).toInt()
            val b:Int = interpolation(from.b.toFloat(), to.b.toFloat(), t).toInt()
            val a:Int = interpolation(from.a.toFloat(), to.a.toFloat(), t).toInt()

            return Color4B(r, g, b, a)
        }

        @JvmStatic
        fun interpolateColor4F(from:Int, to:Int, t:Float):Color4F {
            val a:Float = interpolation(from.and(0xff000000.toInt()).shr(24).toFloat(), to.and(0xff000000.toInt()).shr(24).toFloat(), t)
            val r:Float = interpolation(from.and(0xff0000).shr(16).toFloat(), to.and(0xff0000).shr(16).toFloat(), t)
            val g:Float = interpolation(from.and(0xff00).shr(8).toFloat(), to.and(0xff00).shr(8).toFloat(), t)
            val b:Float = interpolation(from.and(0xff).toFloat(), to.and(0xff).toFloat(), t)

            return Color4F(r, g, b, a)
        }

        @JvmStatic
        fun interpolateColor4F(from:Int, to:Int, t:Float, outvalue:IntArray):Color4F {
            val a:Float = interpolation(from.and(0xff000000.toInt()).shr(24).toFloat(), to.and(0xff000000.toInt()).shr(24).toFloat(), t)
            val r:Float = interpolation(from.and(0xff0000).shr(16).toFloat(), to.and(0xff0000).shr(16).toFloat(), t)
            val g:Float = interpolation(from.and(0xff00).shr(8).toFloat(), to.and(0xff00).shr(8).toFloat(), t)
            val b:Float = interpolation(from.and(0xff).toFloat(), to.and(0xff).toFloat(), t)

            outvalue[0] = (a*0xff).toInt().shl(24)
            outvalue[1] = (r*0xff).toInt().shl(16)
            outvalue[2] = (g*0xff).toInt().shl(8)
            outvalue[3] = (b*0xff).toInt()

            return Color4F(r, g, b, a)
        }

        @JvmStatic
        fun uint32ToColor4F(value:Int): Color4F {
            val a:Float = value.and(0xff000000.toInt()).shr(24).toFloat() / 255f
            val r:Float = value.and(0xff0000).shr(16).toFloat() / 255f
            val g:Float = value.and(0xff00).shr(8).toFloat() / 255f
            val b:Float = value.and(0xff).toFloat() / 255f

            return Color4F(r, g, b, a)
        }

        @JvmStatic
        fun toRadians(degrees:Double):Double {return (degrees*M_PI) / 180f}

        @JvmStatic
        fun toRadians(degrees: Float):Float {return degrees * 0.01745329252f}

        @JvmStatic
        fun toDegrees(radian:Double):Double {return (radian*180f) / M_PI}

        @JvmStatic
        fun toDegrees(radian: Float): Float {return radian * 57.29577951f}

        @JvmStatic
        fun round(value:Float):Int {return (value+0.5f).toInt()}

        @JvmStatic
        fun signum(value: Float):Int {return if (value>=0f) 1 else -1}

        @JvmStatic
        fun shortestAngle(from:Float, to:Float):Float {
            return (((((to-from) % 360) + 540f) % 360f) - 180f )
        }

        @JvmStatic
        fun getDirection(dx:Float, dy:Float):Direction {
            val VERTICAL_WIDE = 100
            // 80
            val HORIZONTAL_WIDE = (180-VERTICAL_WIDE)

            val radians:Float = atan2(dy, dx)
            var degrees:Int = toDegrees(radians.toDouble()).toInt()
            degrees = (degrees % 360) + (if (degrees<0) 360 else 0 )

            // a = 40
            var a:Int = HORIZONTAL_WIDE/2
            // 40 < degrees < 140 == up?
            if (degrees>a && degrees<a+VERTICAL_WIDE) {
                return Direction.UP
            }
            // 140
            a += VERTICAL_WIDE

            // 140 < degrees < 220 = LEFT?
            if (degrees>a && degrees < a+HORIZONTAL_WIDE) {
                return Direction.LEFT
            }
            // 220
            a += HORIZONTAL_WIDE

            // 220 < degrees < 320 = DOWN
            if (degrees>a && degrees < a+VERTICAL_WIDE) {
                return Direction.DOWN
            }

            // 320 ~ 40
            return Direction.RIGHT
        }

        @JvmStatic
        fun copyBitmap(src:Bitmap?):Bitmap? {
            return src?.copy(src.config, true)
        }
    }
    var _pressState:STATE = STATE.NORMAL

    protected var _parent:SMView? = null
    protected var _localZOrder:Int = 0
    protected var _localZOrderAndArrival:Long = 0
    protected var _globalZOrder:Float = 0.0f
    protected var _running:Boolean = false
    protected var _visible:Boolean = true
    protected val TIME_PRESSED_TO_NORMAL: Long = 100
    protected val TIME_NORMAL_TO_PRESSED: Long = 50
    protected val DISABLE_ALPHA_VALUE: Float = 0.5f


    protected var mStateChangeAni: Boolean = false
    protected var mStateChangeTime: Long = 0
    protected var mStateAlpha: Float = 0.0f
    protected var _touchHasFirstClicked: Boolean = false
    protected var _cancelIfTouchOutside: Boolean = false

    protected var _ignoreAnchorPointForPosition:Boolean = false
    protected var _usingNormalizedPosition:Boolean = false
    protected var _normalizedPositionDirty:Boolean = false
    protected var _skewX:Float = 0.0f
    protected var _skewY:Float = 0.0f
    protected var _modelViewTransform:Mat4 = Mat4()
    protected var _transform:Mat4 = Mat4()
    protected var _inverse:Mat4 = Mat4()
    protected var _additionalTransform:ArrayList<Mat4>? = null
    protected var _transformDirty:Boolean = true
    protected var _inverseDirty:Boolean = true
    protected var _additionalTransformDirty:Boolean = false
    protected var _transformUpdated:Boolean = true
    protected var _reorderChildDirty:Boolean = false
    protected var _isTransitionFinished:Boolean = false
    protected var _scissorEnable:Boolean = false
    protected var _isCalledScissorEnabled:Boolean = false
    protected var _children:ArrayList<SMView> = ArrayList()
    protected var _tag:Int = -1
    protected var _name:String = ""
    protected var _hashOfName:Int = 0
    protected var _scheduler:Scheduler? = null
    protected var _actionManager:ActionManager? = null
    protected var _touchTargeted:Boolean = false
    protected var _eventTargetClick:SMView? = null
    protected var _eventTargetLongPress:SMView? = null
    protected var _eventTargetDoubleClick:SMView? = null
    protected var _eventTargetStateChange:SMView? = null
    protected var _displayedAlpha:Float = 1f
    protected var _realAlpha:Float = 1f
    protected var _newAlpha:Float = 1f
    protected var _realPosition: Vec3 = Vec3(0f, 0f, 0f)
    protected var _newPosition: Vec3 = Vec3(0f, 0f, 0f)
    protected var _realScale:Float = 1f
    protected var _newScale:Float = 1f
    protected var _newContentSize: Size = Size(0f, 0f)
    protected var _realRotation: Vec3 = Vec3(0f, 0f, 0f)
    protected var _newRotation: Vec3 = Vec3(0f, 0f, 0f)
    protected var _newColor:Color4F = Color4F(Color4F.TRANSPARENT)
    protected var _animColor:Color4F = Color4F(Color4F.TRANSPARENT)
    protected var _newAnimColor:Color4F = Color4F(Color4F.TRANSPARENT)
    protected var _animOffset:Vec3 = Vec3(0f, 0f, 0f)
    protected var _newAnimOffset:Vec3 = Vec3(0f, 0f, 0f)
    protected var _animScale:Float = 1.0f
    protected var _newAnimScale:Float = 1.0f
    protected var _animRotation:Vec3 = Vec3(0f, 0f, 0f)
    protected var _newAnimRotation:Vec3 = Vec3(0f, 0f, 0f)
    protected var _rotationX:Float = 0.0f
    protected var _rotationY:Float = 0.0f
    protected var _rotationZ_X:Float = 0.0f
    protected var _rotationZ_Y:Float = 0.0f
    protected var _rotationQuat:Quaternion = Quaternion()
    protected var _cascadeColorEnabled:Boolean = false
    protected var _cascadeAlphaEnabled:Boolean = true

    protected var _scaleX:Float = 1.0f
    protected var _scaleY:Float = 1.0f
    protected var _scaleZ:Float = 1.0f

    protected var _position = Vec2(0f, 0f)
    protected var _positionZ:Float = 0f

    protected var _newAnchorPoint:Vec2 = Vec2(0f, 0f)
    protected var _anchorPoint:Vec2 = Vec2(0f, 0f)
    protected var _anchorPointInPoints:Vec2 = Vec2(0f, 0f)
    protected var _normalizedPosition:Vec2 = Vec2(0f, 0f)

    protected var _contentSize:Size = Size(0f, 0f)
    protected var _contentSizeDirty:Boolean = true

    protected var _displayedColor:Color4F = Color4F(Color4F.WHITE)
    protected var _realColor:Color4F = Color4F(Color4F.WHITE)

    protected var _bgColor:Color4F = Color4F(Color4F.TRANSPARENT)

    protected var _bgView:SMView? = null

    protected var _touchMotionTarget:SMView? = null
    protected var _ignoreTouchBounds:Boolean = false

    protected var _updateFlags:Long = 0L
    protected var _startPointCaptured:Boolean = false
    protected var _touchPrevPosition:Vec2 = Vec2(0f, 0f)
    protected var _touchCurrentPosition:Vec2 = Vec2(0f, 0f)
    protected var _touchStartPosition:Vec2 = Vec2(0f, 0f)
    protected var _touchStartTime:Float = 0.0f
    protected var _touchLastPosition:Vec2 = Vec2(0f, 0f)


    private var _touchMask:Long = 0
    private var _smoothFlag:Long = 0
    private var _lastTouchLocation: Vec2 = Vec2(0.0f, 0.0f)
    private var _isEnable: Boolean = true
    private var _scaledDoubleTouchSlope: Float = 0.0f
    private var _enabled:Boolean = true
    private var _initialTouchX: Float = 0.0f
    private var _initialTouchY: Float = 0.0f
    private var _touchEventTime: Long = 0
    private var _isPressed: Boolean = false
    private var _onSmoothUpdateCallback:SEL_SCHEDULE? = null
    private var _onClickValidateCallback:SEL_SCHEDULE? = null
    private var _onLongClickValidateCallback:SEL_SCHEDULE? = null
    private var _id:Int = 0
    private var _targetScissorRect:Rect = Rect()
    private var _scissorRect:Rect? = null
    private var _pivotX:Float = 0f
    private var _pivotY:Float = 0f
    private val _mapPoint:FloatArray = FloatArray(2)



    // life cycle method
    fun SMViewOnEnter() {
        if (!_running) {
            ++__attachedNodeCount
        }

        _isTransitionFinished = false

        for (child:SMView in _children) {
            child.SMViewOnEnter()
        }

        this.onResume()
        _running = true
    }

    open fun onEnter() {
        if (!_running) {
            ++__attachedNodeCount
        }

        _isTransitionFinished = false

        for (child:SMView in _children) {
            child.onEnter()
        }

        this.onResume()
        _running = true
    }

    open fun onEnterTransitionDidFinish() {
        _isTransitionFinished = true

        for (child:SMView in _children) {
            child.onEnterTransitionDidFinish()
        }
    }

    open fun onExitTransitionDidStart() {
        for (child:SMView in _children) {
            child.onExitTransitionDidStart()
        }
    }

    fun SMViewOnExit() {
        if (_running) {
            --__attachedNodeCount
        }

        this.onPause()

        _running = false

        for (child:SMView in _children) {
            child.SMViewOnExit()
        }
    }

    open fun onExit() {
        if (_running) {
            --__attachedNodeCount
        }

        this.onPause()

        _running = false

        for (child:SMView in _children) {
            child.onExit()
        }
    }

    open fun onResume() {
        for (i in 0 until _children.size) {
            _children[i].onResume()
        }

        resumeSchedulerAndActions()
    }

    open fun onPause() {
        for (i in 0 until _children.size) {
            _children[i].onPause()
        }

        pauseSchedulerAndActions()
    }

    fun resumeSchedulerAndActions() {
        _scheduler?.resumeTarget(this)
        _actionManager?.resumeTarget(this)
    }

    fun pauseSchedulerAndActions() {
        _scheduler?.pauseTarget(this)
        _actionManager?.pauseTarget(this)
    }

    open fun cleanup() {
        for (child:SMView in _children) {
            child.cleanup()
        }

        if (_smoothFlag>0) {
            _smoothFlag = 0L
            _newAnimOffset = Vec3(0f, 0f, 0f)
            _animOffset = Vec3(0f, 0f, 0f)
            _newAnimScale = 1f
            _newScale = 1f

            _realPosition.x = _newPosition.x
            _position.x = _newPosition.x
            _realPosition.y = _newPosition.y
            _position.y = _newPosition.y
            _realPosition.z = _newPosition.z
            _positionZ = _newPosition.z

            _scaleX = _newScale
            _scaleY = _newScale
            _scaleZ = _newScale
            _realScale = _newScale

            _transformUpdated = true
        }
    }

    protected fun setTransform(x: Float, y: Float, z: Float, scale: Float, angleX:Float, angleY:Float, angleZ:Float, immediate: Boolean) {
        _newPosition.x = x
        _newPosition.y = y
        _newPosition.z = z
        _newScale = scale
        _newRotation.x = angleX
        _newRotation.y = angleY
        _newRotation.z = angleZ

        if (immediate) {
            _position.x = x
            _position.y = y
            _positionZ = z
            _scaleX = scale
            _scaleY = scale
            _scaleZ = scale
            _rotationX = angleX
            _rotationY = angleY
            _rotationZ_X = angleZ
        }
    }

    fun setCascadeColorEnabled(enable: Boolean) {
        if (_cascadeColorEnabled==enable) return


        _cascadeColorEnabled = enable

        if (_cascadeColorEnabled) {
            updateCascadeColor()
        } else {
            disableCascadeColor()
        }
    }

    fun disableCascadeColor() {
        for (view in _children) {
            view.updateDisplayedColor(Color4F.WHITE)
        }
    }

    open fun updateColor() {}

    fun updateDisplayedColor(parentColor: Color4F) {
        _displayedColor.r = _realColor.r * parentColor.r
        _displayedColor.g = _realColor.g * parentColor.g
        _displayedColor.b = _realColor.b * parentColor.b
        _displayedColor.a = _realColor.a * parentColor.a

        updateColor()

        if (_cascadeColorEnabled) {
            for (view in _children) {
                view.updateDisplayedColor(_displayedColor)
            }
        }
    }

    fun isCascadeColorEnabled():Boolean {return _cascadeColorEnabled}

    fun updateCascadeColor() {
        val parentColor = Color4F(Color4F.WHITE)
        if (_parent!=null && _parent!!.isCascadeColorEnabled()) {
            parentColor.set(_parent!!.getDisplayedColor())
        }

        updateDisplayedColor(parentColor)
    }

    fun setBackgroundAlpha(alpha: Float, changeDurationTime:Float) {
        setBackgroundColor(Color4F(_bgColor.r, _bgColor.g, _bgColor.b, alpha), changeDurationTime)
    }

    fun updateCascadeAlpha() {
        var parentAlpha = 1.0f
        if (_parent!=null && _parent!!.isCascadeAlphaEnabled()) {
            parentAlpha = _parent!!.getDisplayedAlpha()
        }
        updateDisplayedAlpha(parentAlpha)
    }

    fun isCascadeAlphaEnabled():Boolean {return _cascadeAlphaEnabled}
    fun setCascadeAlphaEnable(enable: Boolean) {
        if (_cascadeAlphaEnabled==enable) return

        _cascadeAlphaEnabled = enable
        if (enable) {
            updateCascadeAlpha()
        } else {
            disableCascadeAlpha()
        }
    }

    fun disableCascadeAlpha() {
        _displayedAlpha = _realAlpha
        _displayedColor.a = _realAlpha
        for (view in _children) {
            view.updateDisplayedAlpha(1.0f)
        }
    }

    fun updateDisplayedAlpha(parentAlpha:Float) {
        _displayedAlpha = _realAlpha * parentAlpha
        _displayedColor.a = _realColor.a * parentAlpha

        updateColor()

        if (_cascadeAlphaEnabled) {
            for (view in _children) {
                view.updateDisplayedAlpha(_displayedAlpha)
            }
        }
    }

    open fun init():Boolean {
        _parent = null
        setCascadeAlphaEnable(true)
        return true
    }

    fun setRenderColor() {
        // color of all view
        getDirector().setColor(_displayedColor.r, _displayedColor.g, _displayedColor.b, _displayedColor.a)
    }

    fun getBackgroundColor():Color4F {
        return Color4F(_bgColor)
    }

    open fun setBackgroundColor(r: Float, g: Float, b: Float, a: Float) {
        setBackgroundColor(Color4F(r, g, b, a))
    }

    open fun setBackgroundColor(color:Color4F) {
        if (_bgColor.equal(color)) return

        _bgColor.set(color)

        if (_bgView==null) {
//            if (color.a==0f) return
            _bgView = SMSolidRectView.create(getDirector())
            _bgView!!.setContentSize(_contentSize)
            _bgView!!.setPosition(Vec2.ZERO)

            addChild(_bgView!!, AppConst.ZOrder.BG, "")
        }

        _bgView?.setColor(_bgColor)
        _bgView?.setAlpha(_bgColor.a)
        _bgView?.setVisible(_bgColor.a!=0f)
    }

    open fun setBackgroundColor(color: Color4F, changeDurationTime: Float) {
        var action:Action? = getActionByTag(AppConst.TAG.ACTION_BG_COLOR)
        if (action!=null) {
            stopAction(action)
        }

        if (changeDurationTime>0) {
            action = BGColorTo.create(getDirector(), changeDurationTime, color)
            action.setTag(AppConst.TAG.ACTION_BG_COLOR)
            runAction(action)
        } else {
            setBackgroundColor(color)
        }
    }

    open fun releaseGLResources() {}



    // property method

    fun getAttachedNodeCount():Int {return __attachedNodeCount}
    fun getChildren():ArrayList<SMView> { return _children }
    fun setTag(tag:Int) {_tag = tag}
    fun getTag():Int {return _tag}

    fun getChildrenCount():Int {return _children.size}
    fun getChildCount():Int {return _children.size}

    fun getChild(index:Int):SMView? {
        if (getChildCount()==0) {
            return null
        }

        return _children[index]
    }

    open fun containsPoint(point:Vec2):Boolean {return containsPoint(point.x, point.y)}
    open fun containsPoint(x:Float, y:Float):Boolean {
        return !(x < 0 || y < 0 || x > _contentSize.width || y > _contentSize.height)
    }

    fun setName(name: String) {
        _name = name
        _hashOfName = _name.hashCode()
    }
    fun getName():String {return _name}

    fun setLocalZOrder(z:Int) {
        if (getLocalZOrder()!=z) {
            _setLocalZOrder(z)
            _parent?.reorderChild(this, z)
        }
    }
    fun _setLocalZOrder(z: Int) {
        _localZOrderAndArrival = z.toLong()
        _localZOrder = z
    }
    fun getLocalZOrder():Int {return _localZOrder}

    fun updateOrderOfArrival() {
        _localZOrderAndArrival += ++s_globalOrderOfArrival
    }

    fun reorderChild(child:SMView, z:Int) {
        _reorderChildDirty = true
        child.updateOrderOfArrival()
        child._setLocalZOrder(z)
    }

    fun setGloablZOrder(globalZOrder: Float) {
        _globalZOrder = globalZOrder
    }

    fun isRunning():Boolean {return _running}


    fun getLastTouchLocation(): Vec2 {return Vec2(_lastTouchLocation) }

    fun getScheduler():Scheduler? {return _scheduler}

    fun getActionManager():ActionManager? {return _actionManager}
    fun setActionManager(manager: ActionManager) {
        if (_actionManager!=manager) {
            stopAllActions()
            _actionManager = manager
        }
    }
    fun stopAllActions() {
        _actionManager?.removeallActionsFromTarget(this)
    }

    fun runAction(action:Action):Action {
        _actionManager?.addAction(action, this, !_running)
        return action
    }

    fun stopAction(action: Action) {
        _actionManager?.removeAction(action)
    }

    fun stopActionByTag(tag: Int) {
        if (BuildConfig.DEBUG && tag == Action.INVALID_TAG) {
            error("Assertion failed")
        }

        _actionManager?.removeActionByTag(tag, this)
    }

    fun stopAllActionByTag(tag: Int) {
        if (BuildConfig.DEBUG && tag==Action.INVALID_TAG) {
            error("Assertion failed")
        }

        _actionManager?.removeAllActionsByTag(tag, this)
    }

    fun stopActionsByFlags(flag: Long) {
        if (flag>0) {
            _actionManager?.removeActionsByFlags(flag, this)
        }
    }

    fun getActionByTag(tag: Int):Action? {
        if (BuildConfig.DEBUG && tag==Action.INVALID_TAG) {
            error("Assertion failed")
        }

        return _actionManager?.getActionByTag(tag, this)
    }

    fun getNumberOfRunningActions():Int {
        return _actionManager!!.getNumberOfRunningActionsInTarget(this)
    }

    fun getNumberOfRunningActionsByTag(tag: Int): Int {
        return _actionManager!!.getNumberOfRunningActionsInTargetByTag(this, tag)
    }



    enum class STATE {
        NORMAL, PRESSED, MAX
    }

    fun setIgnoreTouchBounds(ignore:Boolean) {
        _ignoreTouchBounds = ignore
    }

    interface OnTouchListener {
        fun onTouch(view: SMView?, event: MotionEvent): Int
    }
    protected var _onTouchListener: OnTouchListener? = null

    interface OnClickListener {
        fun onClick(view: SMView?)
    }
    protected var _onClickListener: OnClickListener? = null

    interface OnDoubleClickListener {
        fun onDoubleClick(view: SMView?)
    }
    protected var _onDoubleClickListener: OnDoubleClickListener? = null

    interface OnLongClickListener {
        fun onLongClick(view: SMView?)
    }
    private var _onLongClickListener: OnLongClickListener? = null

    // state change
    interface OnStateChangeListener {
        fun onStateChange(view: SMView?, state: STATE?)
    }
    private var _onStateChangeListener: OnStateChangeListener? = null

    // render
    open protected fun onSmoothUpdate(flags:Long, dt:Float) {}

    open protected fun scheduleSmoothUpdate(flag: Long) {
        if (_smoothFlag.and(flag) > 0) {
            return
        }

        _smoothFlag = _smoothFlag.or(flag)

        if (_onSmoothUpdateCallback==null) {
            _onSmoothUpdateCallback = object : SEL_SCHEDULE {
                override fun scheduleSelector(t: Float) {
                    onInternalSmoothUpate(t)
                }
            }
        }

        if (!isScheduled(_onSmoothUpdateCallback)) {
            schedule(_onSmoothUpdateCallback)
        }
    }
    open protected fun unscheduleSmoothUpdate(flag: Long) {
        if (flag==0L) {
            _smoothFlag = 0L
        } else {
            _smoothFlag = _smoothFlag.and(flag.inv())
        }

        if (_smoothFlag==0L) {
            if (_onSmoothUpdateCallback!=null && isScheduled(_onSmoothUpdateCallback)) {
                unschedule(_onSmoothUpdateCallback)
            }
        }
    }

    fun isScheduled(selector: SEL_SCHEDULE?):Boolean {
        return _scheduler?.isScheduled(selector, this) ?: false
    }

    fun scheduleUpdate() {scheduleUpdateWithPriority(0)}
    fun scheduleUpdateWithPriority(priority: Int) {
        _scheduler?.scheduleUpdate(this, priority, !_running)
    }

    fun unscheduleUpdate() {_scheduler?.unscheduledUpdate(this)}

    fun schedule(selector: SEL_SCHEDULE?) {
        schedule(selector, 0.0f, Long.MAX_VALUE-1, 0.0f)
    }

    fun schedule(selector: SEL_SCHEDULE?, interval: Float) {
        schedule(selector, interval, Long.MAX_VALUE-1, 0.0f)
    }

    fun schedule(selector: SEL_SCHEDULE?, interval: Float, repeat: Long, delay: Float) {
        if (BuildConfig.DEBUG && interval<0) {
            error("Assertion failed")
        }

        _scheduler?.schedule(selector!!, this, interval, repeat, delay, !_running)
    }

    fun scheduleOnce(selector: SEL_SCHEDULE?, delay: Float) {
        schedule(selector, 0.0f, 0, delay)
    }

    fun unschedule(selector: SEL_SCHEDULE?) {
        if (selector==null) return
        _scheduler?.unschedule(selector, this)
    }

    fun unscheduleAllCallbacks() {_scheduler?.unscheduleAllForTarget(this)}

    protected fun setTouchMask(mask: Long) {
        _touchMask = _touchMask or mask
    }
    protected fun clearTouchMask(mask: Long) {
        _touchMask = _touchMask and mask.inv()
    }
    protected fun isTouchMask(mask: Long): Boolean {return (_touchMask and mask)!=0L}

    open fun isTouchEnable(): Boolean {return _touchMask>0}

    fun runStateAnimation(): Float {
        var f: Float = 0.0f
        if (mStateChangeAni) {
            val time:Long = _director?.getTickCount()?.toLong()!! - mStateChangeTime
            if (STATE.NORMAL==_pressState) {
                f = time.toFloat()/TIME_PRESSED_TO_NORMAL.toFloat()
            } else {
                f = time.toFloat()/TIME_NORMAL_TO_PRESSED.toFloat()
            }
            if (f>1) {
                f = 1.0f
                mStateChangeAni = false
            }
            if (STATE.NORMAL==_pressState) {
                f = 1f-f
            }
        } else {
            f = if (STATE.NORMAL==_pressState) 0.0f else 1.0f
        }

        return f
    }

    fun setState(state: STATE): Boolean {
        if (_pressState!=state) {
            if (isEnabled()) {
                _pressState = state
                mStateChangeAni = true
                mStateChangeTime = _director?.getTickCount()!!.toLong()
            } else {
                _pressState = state
            }
            return true
        }

        return false
    }


    open fun setVisible(visible: Boolean) {
        if (_visible!=visible) {
            _visible = visible
            if (_visible) {
                _transformUpdated = true
                _transformDirty = true
                _inverseDirty = true
            }
        }
    }
    fun isVisible():Boolean {return _visible}

    fun setEnabled(enable:Boolean) {
        _enabled = enable
    }
    fun isEnabled(): Boolean {return _enabled}


    // touch event
    fun setOnTouchListener(listener: OnTouchListener?) {
        _onTouchListener = listener
        if (listener!=null) {
            setTouchMask(TOUCH_MASK_TOUCH)
        } else {
            clearTouchMask(TOUCH_MASK_TOUCH)
        }
    }

    fun setOnClickListener(listener: OnClickListener?) {
        _onClickListener = listener

        if (listener!=null) {
            setTouchMask(TOUCH_MASK_CLICK)
        } else {
            clearTouchMask(TOUCH_MASK_CLICK)
        }
    }

    fun setOnDoubleClickListener(listener: OnDoubleClickListener?) {
        if (listener==null) {
            if (_onDoubleClickListener!=null) {
                unscheduleClickValidator()
    }
        }
        _onDoubleClickListener = listener

        if (listener!=null) {
            setTouchMask(TOUCH_MASK_DOUBLECLICK)
        } else {
            clearTouchMask(TOUCH_MASK_DOUBLECLICK)
        }
    }

    fun setOnLongClickListener(listener: OnLongClickListener?) {
        if (listener==null) {
            if (_onLongClickListener!=null) {
                unscheduleLongClickValidator()
    }
        }
        _onLongClickListener = listener

        if (listener!=null) {
            setTouchMask(TOUCH_MASK_LONGCLICK)
        } else {
            clearTouchMask(TOUCH_MASK_LONGCLICK)
        }
    }

    fun setOnStateChangeListener(listener: OnStateChangeListener?) {
        _onStateChangeListener = listener
    }

    // member method
    fun setPosition(pos: Vec2) {
        setPosition(pos, true)
    }
    fun setPosition(pos: Vec2, immediate: Boolean) {
        setPosition(pos.x, pos.y, immediate)
    }
    fun setPosition(x: Float, y: Float) {
        setPosition(x, y, true)
    }
    fun setPosition(size:Size) {
        setPosition(size.width, size.height)
    }

    fun setPosition(x: Float, y: Float, immediate: Boolean) {
        setPositionX(x, immediate)
        setPositionY(y, immediate)

        _inverseDirty = true
        _transformDirty = true
        _transformUpdated = true

        _usingNormalizedPosition = true
    }

    fun setPosition(x:Float, y: Float, z: Float, immediate: Boolean) {
        _newPosition.x = x
        _newPosition.y = y
        _newPosition.z = z
        if (immediate) {
            _position.x = x
            _position.y = y
            _positionZ = z
        }
    }


    fun setPositionX(x:Float) {
        setPositionX(x, true)
    }
    fun setPositionX(x: Float, immediate: Boolean) {
        if (immediate) {
            if (_position.x!=x+_animOffset.x) {
                _position.x = x+_animOffset.x

                _newPosition.x = x
                _realPosition.x = x
            }
        } else {
            if (_newPosition.x==x) return

            _newPosition.x = x
            scheduleSmoothUpdate(VIEWFLAG_POSITION)
        }
    }
    fun setPositionY(y: Float) {
        setPositionY(y, true)
    }
    fun setPositionY(y: Float, immediate: Boolean) {
        if (immediate) {
            if (_position.y!=y+_animOffset.y) {
                _position.y = y+_animOffset.y

                _newPosition.y = y
                _realPosition.y = y
            }
        } else {
            if (_newPosition.y==y) return

            _newPosition.y = y
            scheduleSmoothUpdate(VIEWFLAG_POSITION)
        }
    }
    fun setPositionZ(z: Float) {
        setPositionZ(z, true)
    }
    fun setPositionZ(z: Float, immediate: Boolean) {
        if (immediate) {
            if (_positionZ==z) return

            _positionZ = z
            _newPosition.z = z
            _realPosition.z = z

            _inverseDirty = true
            _transformDirty = true
            _transformUpdated = true

        } else {
            if (_newPosition.z==z) return

            _newPosition.z = z
            scheduleSmoothUpdate(VIEWFLAG_POSITION)
        }
    }

    fun setPositionNormalized(position:Vec2) {
        if (_normalizedPosition.equal(position)) return

        _normalizedPosition.set(position)
        _usingNormalizedPosition = true
        _normalizedPositionDirty = true

        _inverseDirty = true
        _transformDirty = true
        _transformUpdated = true
    }

    fun setScissorEnable(enable: Boolean) {
        _isCalledScissorEnabled = true
        _scissorEnable = enable
    }

    fun setPosition3D(pos: Vec3) {
        setPosition3D(pos, true)
    }

    fun setPosition3D(pos:Vec3, immediate:Boolean) {
        if (immediate) {
            _positionZ = pos.z+_animOffset.z
            _position.x = pos.x+_animOffset.x
            _position.y = pos.y+_animOffset.y
        } else {
            setPositionX(pos.x, false)
            setPositionY(pos.y, false)
            setPositionZ(pos.z, false)
        }
    }

    fun setZ(z: Float, immediate: Boolean) {
        _newPosition.z = z
        if (immediate) {
            _positionZ = z
        }
    }

    fun setY(y:Float, immediate: Boolean) {
        _newPosition.y = y
        if (immediate) {
            _position.y = y
        }
    }

    fun setY(y: Float) {
        _newPosition.y = y
        _position.y = y
    }

    fun transform(parentTransform: Mat4):Mat4 {
        return parentTransform.multiplyRet(this.getNodeToParentTransform())
    }

    fun getAnchorPointInPoints():Vec2 {return Vec2(_anchorPointInPoints) }
    fun getAnchorPoint():Vec2 {return Vec2(_anchorPoint) }

    open fun setAnchorPoint(anchorX: Float, anchorY: Float) {
        setAnchorPoint(Vec2(anchorX, anchorY))
    }
    open fun setAnchorPoint(point:Vec2) {
        setAnchorPoint(point, true)
    }
    open fun setAnchorPoint(point: Vec2, immediate: Boolean) {
        if (immediate) {
            if (!point.equal(_anchorPoint)) {
                _anchorPoint.set(point)
                _anchorPointInPoints.set(_contentSize.width*_anchorPoint.x, _contentSize.height*_anchorPoint.y)

                _contentSizeDirty = true
                _inverseDirty = true
                _transformDirty = true
                _transformUpdated = true
            }
            _newAnchorPoint.set(point)
        } else {
            if (!point.equal(_newAnchorPoint)) {
                _newAnchorPoint.set(point)
                scheduleSmoothUpdate(VIEWFLAG_CONTENT_SIZE)
            }
        }
    }

    fun setParent(parent:SMView?) {
        _parent = parent
        _transformUpdated = true
        _inverseDirty = true
        _transformDirty = true
    }

    fun setIgnoreAnchorPointForPosition(newValue:Boolean) {
        if (newValue!=_ignoreAnchorPointForPosition) {
            _ignoreAnchorPointForPosition = newValue
            _transformUpdated = true
            _inverseDirty = true
            _transformDirty = true
        }
    }

    fun processParentFlags(parentTransform:Mat4, parentFlag:Int):Int {
        if (_usingNormalizedPosition) {
            if (parentFlag.and(FLAGS_CONTENT_SIZE_DIRTY.toInt())>0 || _normalizedPositionDirty) {
                val s:Size = _parent!!.getContentSize()
                _position.x = _normalizedPosition.x * s.width
                _position.y = _normalizedPosition.y * s.height

                _transformUpdated = true
                _inverseDirty = true
                _transformDirty = true

                _normalizedPositionDirty = false
            }
        }

        var flags:Int = parentFlag
        flags = flags.or(if (_transformUpdated) FLAGS_TRANSFORM_DIRTY.toInt() else 0)
        flags = flags.or(if (_contentSizeDirty) FLAGS_CONTENT_SIZE_DIRTY.toInt() else 0)

        if (flags.and(FLAGS_DIRTY_MASK.toInt())>0) {
            _modelViewTransform = this.transform(parentTransform)
        }

        _transformUpdated = false
        _contentSizeDirty = false

        return flags
    }

    fun setAnimOffset(pos: Vec2) {setAnimOffset(pos, true)}
    fun setAnimOffset(pos: Vec2, immediate: Boolean) {
        if (_newAnimOffset.x!=pos.x || _newAnimOffset.y!=pos.y) {
            _newAnimOffset.x = pos.x
            _newAnimOffset.y = pos.y

            scheduleSmoothUpdate(VIEWFLAG_ANIM_OFFSET)
        }

        if (immediate) _animOffset.set(_newAnimOffset)
    }

    fun setAnimScale(scale:Float) {setAnimScale(scale, true)}
    fun setAnimScale(scale: Float, immediate: Boolean) {
        if (_newAnimScale!=scale) {
            _newAnimScale = scale
            scheduleSmoothUpdate(VIEWFLAG_ANIM_SCALE)
        }

        if (immediate) _animScale = _newAnimScale
    }

    fun setAnimRotate(rotate: Float) { setAnimRotate(rotate, true) }
    fun setAnimRotate(rotate: Float, immediate: Boolean) {
        if (_newAnimRotation.z!=rotate) {
            _newAnimRotation.z = rotate
            scheduleSmoothUpdate(VIEWFLAG_ANIM_ROTATE)
        }

        if (immediate) _animRotation.set(_newAnimRotation)
    }

    fun setAnimRotate3D(rotate: Vec3) { setAnimRotate3D(rotate, true) }
    fun setAnimRotate3D(rotate: Vec3, immediate: Boolean) {
        if (!_newAnimRotation.equal(rotate)) {
            _newAnimRotation.set(rotate)
            scheduleSmoothUpdate(VIEWFLAG_ANIM_ROTATE)
        }

        if (immediate) _animRotation.set(_newAnimRotation)
    }

    fun setLeft(x: Float) {
        setLeft(x, true)
    }
    fun setRight(x: Float) {
        setRight(x, true)
    }
    fun setTop(y: Float) {
        setTop(y, true)
    }
    fun setBottom(y: Float) {
        setBottom(y, true)
    }

    fun setLeft(x: Float, immediate: Boolean) {
        _newPosition.x = x
        if (immediate) _position.x = _newPosition.x
    }
    fun setRight(x: Float, immediate: Boolean) {
        _newPosition.x = x - _contentSize.width
        if (immediate) _position.x = _newPosition.x
    }
    fun setTop(y: Float, immediate: Boolean) {
        _newPosition.y = y
        if (immediate) _position.y = _newPosition.y
    }
    fun setBottom(y: Float, immediate: Boolean) {
        _newPosition.y = y - _contentSize.height
        if (immediate) _position.y = _newPosition.y
    }

    fun getOriginX():Float {return _position.x - _anchorPointInPoints.x}
    fun getOriginY():Float {return _position.y - _anchorPointInPoints.y}

    fun updateRotationQuat() {
        val halfRadx:Float = toRadians(_rotationX/2.0f)
        val halfRady:Float = toRadians(_rotationY/2.0f)
        val halfRadz:Float = if (_rotationZ_X==_rotationZ_Y) -toRadians(_rotationZ_X/2.0).toFloat() else 0f

        val coshalfRadx:Float = cos(halfRadx)
        val coshalfRady:Float = cos(halfRady)
        val coshalfRadz:Float = cos(halfRadz)

        val sinhalfRadx:Float = sin(halfRadx)
        val sinhalfRady:Float = sin(halfRady)
        val sinhalfRadz:Float = sin(halfRadz)

        _rotationQuat.x = sinhalfRadx * coshalfRady * coshalfRadz - coshalfRadx * sinhalfRady * sinhalfRadz
        _rotationQuat.y = coshalfRadx * sinhalfRady * coshalfRadz + sinhalfRadx * coshalfRady * sinhalfRadz
        _rotationQuat.z = coshalfRadx * coshalfRady * sinhalfRadz - sinhalfRadx * sinhalfRady * coshalfRadz
        _rotationQuat.w = coshalfRadx * coshalfRady * coshalfRadz + sinhalfRadx * sinhalfRady * sinhalfRadz
    }

    fun updateRotation3D() {
        val x:Float = _rotationQuat.x
        val y:Float = _rotationQuat.y
        val z:Float = _rotationQuat.z
        val w:Float = _rotationQuat.w
        _rotationX = atan2(2f * (w * x + y * z), 1f - 2f * (x * x + y * y))
        var sy:Float = 2f * (w * y - z * x)
        sy = Vec2.clampf(sy, -1f, 1f)
        _rotationY = asin(sy)
        _rotationZ_X = atan2(2f*(w*z + x*y), 1f-2f*(y*y + z*z))

        _rotationX = toDegrees(_rotationX.toDouble()).toFloat()
        _rotationY = toDegrees(_rotationY.toDouble()).toFloat()
        _rotationZ_X = -toDegrees(_rotationZ_X.toDouble()).toFloat()
        _rotationZ_Y = _rotationZ_X
    }

    fun setRotationQuat(quat:Quaternion) {
        _rotationQuat.set(quat)
        updateRotation3D()

        _inverseDirty = true
        _transformDirty = true
        _transformUpdated = true
    }
    fun getRotationQuat():Quaternion {return _rotationQuat}

    fun transformMatrixToParent(matrix: FloatArray) {
        transformMatrix(matrix)
        val mat:Mat4 = Mat4(matrix)

        var p:SMView? = getParent()
        while (p!=null) {
            val tmp = Mat4(Mat4.IDENTITY)
            p.transformMatrix(tmp.m)
            mat.multiply(tmp)
            p = p.getParent()
        }

        OpenGlUtils.copyMatrix(matrix, mat.m, 16)
    }

    fun convertToWorldSpace(nodePos:Vec2):Vec2 {
        val tmp:Mat4 = getNodeToWorldTransform()
        val vec3:Vec3 = Vec3(nodePos.x, nodePos.y, 0f)
        val ret:Vec3 = Vec3()
        tmp.transformPoint(vec3, ret)
        return Vec2(ret.x, ret.y)
    }

    fun getNodeToWorldTransform():Mat4 {
        return getNodeToParentTransform(null)
    }

    fun getNodeToParentTransform(ancestor:SMView?):Mat4 {
        val t:Mat4 = Mat4(this.getNodeToParentTransform())

        var p:SMView? = getParent()
        while (p!=null && p!=ancestor) {
            t.multiply(p.getNodeToParentTransform())

            p = p.getParent()
        }

        return t
    }

    fun getViewToWorldTransform():Mat4 { return this.getViewToParentTransform(null) }

    fun getViewToParentTransform(ancestor: SMView?):Mat4 {
        val t:Mat4 = Mat4(this.getViewToParentTransform())

        var p:SMView? = getParent()
        while (p!=null && p!=ancestor) {
            t.multiply(p.getViewToParentTransform())
        }

        return t
    }

    fun getViewToParentTransform():Mat4 {
        if (_transformDirty) {
            var x:Float = _position.x
            var y:Float = _position.y
            var z:Float = _positionZ

            if (_ignoreAnchorPointForPosition) {
                x += _anchorPointInPoints.x
                y += _anchorPointInPoints.y
            }

            val needsSkewMatrix:Boolean = _skewX > 0 || _skewY > 0
            val translation:Mat4 = Mat4()
            Mat4.createTranslation(x, y, z, translation)
            Mat4.createRotation(_rotationQuat, _transform)

            if (_rotationZ_X != _rotationZ_Y) {
                val radiansX:Float = toRadians(_rotationZ_X.toDouble()).toFloat()
                val radiansY:Float = toRadians(_rotationZ_Y.toDouble()).toFloat()
                val cx:Float = cos(radiansX)
                val cy:Float = cos(radiansY)
                val sx:Float = sin(radiansX)
                val sy:Float = sin(radiansY)

                val m0:Float = _transform.m[0]
                val m1:Float = _transform.m[1]
                val m4:Float = _transform.m[4]
                val m5:Float = _transform.m[5]
                val m8:Float = _transform.m[8]
                val m9:Float = _transform.m[9]

                _transform.m[0] = cy * m0 - sx * m1
                _transform.m[4] = cy * m4 - sx * m5
                _transform.m[8] = cy * m8 - sx * m9
                _transform.m[1] = sy * m0 + cx * m1
                _transform.m[5] = sy * m4 + cx * m5
                _transform.m[9] = sy * m8 + cx * m9
            }

            _transform.multiply(translation)

            if (_scaleX!=1f) {
                _transform.m[0] *= _scaleX
                _transform.m[1] *= _scaleX
                _transform.m[2] *= _scaleX
            }
            if (_scaleY!=1f) {
                _transform.m[4] *= _scaleY
                _transform.m[5] *= _scaleY
                _transform.m[6] *= _scaleY
            }
            if (_scaleZ!=1f) {
                _transform.m[8] *= _scaleZ
                _transform.m[9] *= _scaleZ
                _transform.m[10] *= _scaleZ
            }

            if (needsSkewMatrix) {
                val skewMatArray:FloatArray = floatArrayOf(1f, tan(toRadians(_skewY.toDouble())).toFloat(), 0f, 0f,
                                                            tan(toRadians(_skewX.toDouble())).toFloat(), 1f, 0f, 0f,
                                                            0f, 0f, 1f, 0f,
                                                            0f, 0f, 0f, 1f)
                val skewMatrix:Mat4 = Mat4(skewMatArray)
                _transform.multiply(skewMatrix)
            }

            if (!_anchorPointInPoints.isZero()) {
                _transform.m[12] += _transform.m[0] * -_anchorPointInPoints.x + _transform.m[4] * -_anchorPointInPoints.y
                _transform.m[13] += _transform.m[1] * -_anchorPointInPoints.x + _transform.m[5] * -_anchorPointInPoints.y
                _transform.m[14] += _transform.m[2] * -_anchorPointInPoints.x + _transform.m[6] * -_anchorPointInPoints.y
            }
        }

        if (_additionalTransform!=null) {
            if (_transformDirty) _additionalTransform!![1] = _transform

            if (_transformUpdated) _transform = _additionalTransform!![1].multiplyRet(_additionalTransform!![0])
        }

        _transformDirty = false
        _additionalTransformDirty = false

        return _transform
    }

    fun getNodeToParentTransform():Mat4 {
        if (_transformDirty) {
            var x:Float = _position.x
            var y:Float = _position.y
            val z:Float = _positionZ

            if (_ignoreAnchorPointForPosition) {
                x += _anchorPointInPoints.x
                y += _anchorPointInPoints.y
            }

            val needsSkewMatrix:Boolean = _skewX > 0 || _skewY > 0
            val translation:Mat4 = Mat4()
            Mat4.createTranslation(x, y, z, translation)
            Mat4.createRotation(_rotationQuat, _transform)

            if (_rotationZ_X != _rotationZ_Y) {
                val radiansX:Float = toRadians(_rotationZ_X.toDouble()).toFloat()
                val radiansY:Float = toRadians(_rotationZ_Y.toDouble()).toFloat()

                val cx:Float = cos(radiansX)
                val cy:Float = cos(radiansY)
                val sx:Float = sin(radiansX)
                val sy:Float = sin(radiansY)

                val m0:Float = _transform.m[0]
                val m1:Float = _transform.m[1]
                val m4:Float = _transform.m[4]
                val m5:Float = _transform.m[5]
                val m8:Float = _transform.m[8]
                val m9:Float = _transform.m[9]

                _transform.m[0] = cy * m0 - sx * m1
                _transform.m[4] = cy * m4 - sx * m5
                _transform.m[8] = cy * m8 - sx * m9
                _transform.m[1] = sy * m0 + cx * m1
                _transform.m[5] = sy * m4 + cx * m5
                _transform.m[9] = sy * m8 + cx * m9
            }

            _transform.multiply(translation)

            if (_scaleX!=1f) {
                _transform.m[0] *= _scaleX
                _transform.m[1] *= _scaleX
                _transform.m[2] *= _scaleX
            }

            if (_scaleY!=1f) {
                _transform.m[4] *= _scaleY
                _transform.m[5] *= _scaleY
                _transform.m[6] *= _scaleY
            }

            if (_scaleZ!=1f) {
                _transform.m[8] *= _scaleZ
                _transform.m[9] *= _scaleZ
                _transform.m[10] *= _scaleZ
            }

            if (needsSkewMatrix) {
                val skewMatArray:FloatArray = floatArrayOf(1f, tan(toRadians(_skewY.toDouble()).toFloat()), 0f, 0f,
                                                            tan(toRadians(_skewX.toDouble()).toFloat()), 1f, 0f, 0f,
                                                            0f, 0f, 1f, 0f,
                                                            0f, 0f, 0f, 1f)

                _transform.multiply(Mat4(skewMatArray))
            }

            if (!_anchorPointInPoints.isZero()) {
                _transform.m[12] += _transform.m[0] * -_anchorPointInPoints.x + _transform.m[4] * -_anchorPointInPoints.y
                _transform.m[13] += _transform.m[1] * -_anchorPointInPoints.x + _transform.m[5] * -_anchorPointInPoints.y
                _transform.m[14] += _transform.m[2] * -_anchorPointInPoints.x + _transform.m[6] * -_anchorPointInPoints.y
            }
        }

        if (_additionalTransform!=null) {
            if (_transformDirty) _additionalTransform!![1] = _transform

            if (_transformUpdated) _transform = _additionalTransform!![1].multiplyRet(_additionalTransform!![0])
        }

        _transformDirty = false
        _additionalTransformDirty = false

        return _transform
    }

    fun setNodeToParentTransform(additionalTransform: AffineTransform) {
        val tmp:Mat4 = Mat4()
        AffineTransform.CGAffineToGL(additionalTransform, tmp.m)
        setAdditionalTransform(tmp)
    }

    fun setAdditionalTransform(additionalTransform: Mat4?) {
        if (additionalTransform==null) {
            if (_additionalTransform!=null) _transform = _additionalTransform!![1]
            _additionalTransform = null
        } else {
            if (_additionalTransform==null) {
                _additionalTransform = ArrayList<Mat4>(2)
                _additionalTransform!![1] = _transform
            }

            _additionalTransform!![0] = additionalTransform
        }

        _inverseDirty = true
        _additionalTransformDirty = true
        _transformDirty = true
    }

    fun getParentToNodeAffineTransform():AffineTransform {
        val ret:AffineTransform = AffineTransform()

        AffineTransform.GLToCGAffine(getParentToNodeTransform().m, ret)
        return ret
    }

    fun getParentToNodeTransform():Mat4 {
        if (_inverseDirty) {
            _inverse = getNodeToParentTransform().getInversed()
            _inverseDirty = false
        }

        return _inverse
    }

    fun getBoundingBox():Rect {
        val rect:Rect = Rect(0f, 0f, _contentSize.width, _contentSize.height)
        return AffineTransform.RectApplyAffineTransform(rect, getNodeToParentAffineTransform())
    }

    fun getNodeToWorldAffineTransform():AffineTransform {
        return this.getNodeToParentAffineTransform(null)
    }

    fun getNodeToParentAffineTransform():AffineTransform {
        val ret:AffineTransform = AffineTransform()
        AffineTransform.GLToCGAffine(getNodeToParentTransform().m, ret)
        return ret
    }

    fun getNodeToParentAffineTransform(ancestor: SMView?):AffineTransform {
        var t:AffineTransform = AffineTransform(this.getNodeToParentAffineTransform())

        var p:SMView? = getParent()

        while (p!=null && p!=ancestor) {
            t = AffineTransform.AffineTransformConcat(t, p.getNodeToParentAffineTransform())

            p = p.getParent()
        }

        return t
    }

    fun getWorldToNodeTransform():Mat4 {return getNodeToWorldTransform().getInversed()}

    fun convertToNodeSpace(worldPoint:Vec2): Vec2 {
        val tmp:Mat4 = getWorldToNodeTransform()

        val vec3:Vec3 = Vec3(worldPoint.x, worldPoint.y, 0f)
        val ret:Vec3 = Vec3()

        tmp.transformPoint(vec3, ret)
        return Vec2(ret.x, ret.y)
    }

    fun getScreenPosition():Vec2 { return Vec2(getScreenX(), getScreenY()) }

    fun convertCurPosToWorld(curPos: Vec2): Vec2 {
        return Vec2(getScreenX()+curPos.x, getScreenY()+curPos.y)
    }

    fun convertToWorldPos(addPos: Vec2?): Vec2 {
        return if (addPos==null) {
            Vec2(getScreenX()+_anchorPointInPoints.x, getScreenY()+_anchorPointInPoints.y)
        } else {
            Vec2(getScreenX()+_anchorPointInPoints.x+addPos.x, getScreenY()+_anchorPointInPoints.y+addPos.y)
        }
    }

    fun convertToLocalPos(worldPos: Vec2): Vec2 {
        return if (_parent==null) worldPos else {
            Vec2(worldPos.x + _parent!!.getScreenX(), worldPos.y+_parent!!.getScreenY())
        }
    }

    fun getScreenX():Float {
        var x:Float = 0f
        if (_parent!=null ){
            x = _parent!!.getScreenX()
            return x + getOriginX() * _parent!!.getScale()
        }

        return x + getOriginX()
    }

    fun toScreenX(screenPosX:Float):Float {
        if (_parent!=null) {
            return screenPosX - _parent!!.getScreenX()
        }

        return screenPosX
    }

    fun getScreenY():Float {
        var y:Float = 0f
        if (_parent!=null) {
            y = _parent!!.getScreenY()
            return y + getOriginY()*_parent!!.getScale()
        }

        return y + getOriginY()
    }

    fun toScreenY(screenPosY:Float):Float {
        if (_parent!=null) {
            return screenPosY - _parent!!.getScreenY()
        }

        return screenPosY
    }

    fun getScaleX():Float {return _scaleX}
    fun getScaleY():Float {return _scaleY}
    fun getScaleZ():Float {return _scaleZ}

    fun getScale():Float { return _scaleX }

    fun getLeft():Float { return _position.x }
    fun getTop():Float { return _position.y }
    fun getRight():Float { return _position.x + _contentSize.width }
    fun getBottom():Float { return _position.y + _contentSize.height }

    fun setScaleX(scale: Float) {
        if (_scaleX==scale) return

        _scaleX = scale
        _transformDirty = true
        _transformUpdated = true
        _inverseDirty = true
    }

    fun setScaleY(scale: Float) {
        if (_scaleY==scale) return

        _scaleY = scale
        _transformDirty = true
        _transformUpdated = true
        _inverseDirty = true
    }

    fun setScaleZ(scale: Float) {
        if (_scaleZ==scale) return

        _scaleZ = scale
        _transformDirty = true
        _transformUpdated = true
        _inverseDirty = true
    }

    fun setScale(scale: Float) {
        setScale(scale, true)
    }

    fun setScale(scale: Float, immediate: Boolean) {
        if (immediate) {
            _realScale = scale
            _newScale = scale

            _scaleX = scale * _animScale
            _scaleY = _scaleX
            _scaleZ = _scaleY

            _transformDirty = true
            _transformUpdated = true
            _inverseDirty = true
        } else {
            if (_newScale==scale) return

            _newScale = scale
            scheduleSmoothUpdate(VIEWFLAG_SCALE)
        }
    }

    fun getRotationX():Float {return _rotationX}
    fun getRotationY():Float {return _rotationY}
    fun getRotationZ():Float {return _rotationZ_X}

    fun getRotationSkewX():Float {return _rotationZ_X}
    fun getRotationSkewY():Float {return _rotationZ_Y}

    fun getRotation():Float {return _rotationZ_X}
    fun getRotation3D():Vec3 {return Vec3(_rotationX, _rotationY, _rotationZ_X)
    }

    fun setRotationSkewX(rotationX:Float) {
        if (_rotationZ_X==rotationX) return

        _transformDirty = true
        _transformUpdated = true
        _inverseDirty = true

        _rotationZ_X = rotationX
        updateRotationQuat()
    }

    fun setRotationSkewY(rotationY: Float) {
        if (_rotationZ_Y==rotationY) return

        _transformDirty = true
        _transformUpdated = true
        _inverseDirty = true

        _rotationZ_Y = rotationY
        updateRotationQuat()
    }

    fun setRotation(rotate: Float) {setRotation(rotate, true)}
    fun setRotation(rotate: Float, immediate: Boolean) { setRotationZ(rotate, immediate) }
    fun setRotationZ(rotateZ: Float) {setRotationZ(rotateZ, true)}
    fun setRotationZ(rotateZ: Float, immediate: Boolean) {
        if (immediate) {
            _realRotation.z = rotateZ
            _newRotation.z = rotateZ

            val rotation:Float = rotateZ + _animRotation.z

            if (_rotationZ_X!=rotation) {
                _rotationZ_X = rotation
                _rotationZ_Y = rotation

                _transformDirty = true
                _transformUpdated = true
                _inverseDirty = true

                updateRotationQuat()
            }
        } else {
            if (_newRotation.z==rotateZ) return

            _newRotation.z = rotateZ
            scheduleSmoothUpdate(VIEWFLAG_ROTATE)
        }
    }

    fun setRotation3D(rotate: Vec3) { setRotation3D(rotate, true)}
    fun setRotation3D(rotate: Vec3, immediate: Boolean) {
        if (immediate) {
            _realRotation = Vec3(rotate)
            _newRotation = Vec3(rotate)

            val rotate3D:Vec3 = Vec3(_realRotation.x*_animRotation.x, _realRotation.y*_animRotation.y, _realRotation.z*_animRotation.z)
            if (_rotationX==rotate3D.x && _rotationY==rotate3D.y && _rotationZ_X==rotate3D.z) return

            _rotationX = rotate3D.x
            _rotationY = rotate3D.y
            _rotationZ_X = rotate3D.z
            _rotationZ_Y = rotate3D.z
            updateRotationQuat()
        } else {
            if (_newRotation.equal(rotate)) return

            _newRotation = Vec3(rotate)

            scheduleSmoothUpdate(VIEWFLAG_ROTATE)
        }
    }

    open protected fun onStateChangePressToNormal() {}
    open protected fun onStateChangeNormalToPress() {}

    private fun stateChangePressToNormal() {
        if (_pressState==STATE.PRESSED) {
            setState(STATE.NORMAL)

            onStateChangePressToNormal()

            if (_onStateChangeListener!=null) {
                if (_eventTargetStateChange!=null) {
                    _onStateChangeListener?.onStateChange(_eventTargetStateChange, _pressState)
                } else {
                    _onStateChangeListener?.onStateChange(this, _pressState)
                }
            }
        }
    }

    private fun stateChangeNormalToPress() {
        if (_pressState==STATE.NORMAL) {
            setState(STATE.PRESSED);

            onStateChangeNormalToPress()

            if (_onStateChangeListener!=null) {
                if (_eventTargetStateChange!=null) {
                    _onStateChangeListener?.onStateChange(_eventTargetStateChange, _pressState)
                } else {
                    _onStateChangeListener?.onStateChange(this, _pressState)
                }
            }
        }
    }


    fun getColor():Color4F {return Color4F(_realColor)}
    fun getDisplayedColor():Color4F {return Color4F(_displayedColor)}

    open fun setColor(r: Float, g: Float, b: Float, a: Float) {
        setColor(r, g, b, a, true)
    }

    open fun setColor(r:Float, g:Float, b:Float, a:Float, immediate: Boolean) {
        setColor(Color4F(r, g, b, a), immediate)
    }

    open fun setColor(color: Color4F) {
        setColor(color, true)
    }

    open fun setColor(color: Color4F, immediate: Boolean) {
        if (immediate) {
            if (!_realColor.equal(color)) {
                _newColor.set(color)


                _realColor.set(color)
                _displayedColor.set(color)

                setAlpha(color.a)

                updateCascadeColor()

                _contentSizeDirty = true
                _inverseDirty = true
                _transformDirty = true
                _transformUpdated = true
            }
        } else {
            if (!_realColor.equal(color)) {
                _newColor.set(color)
                scheduleSmoothUpdate(VIEWFLAG_COLOR)
            }
        }
    }

    open fun setAlpha(a:Float) {
        _realAlpha = a.also { _displayedAlpha = it }
        _realColor.a = a.also { _displayedColor.a = it }

        updateCascadeAlpha()
    }

    fun changeParent(newParent:SMView?) {
        if (newParent==null || getParent()==newParent) return

        val parent:SMView? = getParent()

        if (parent!=null) {
            parent.removeChild(this, false)
            newParent.addChild(this, getLocalZOrder())
        }
    }

    open fun getParent():SMView? {return _parent}

    fun getPosition3D():Vec3 {return Vec3(_position.x, _position.y, _positionZ) }

    fun getPosition():Vec2 {return Vec2(_position)}

    fun getPositionX():Float {return _position.x}

    fun getPositionY():Float {return _position.y}

    fun getPositionZ():Float {return _positionZ}

    fun getX():Float {return _position.x}
    fun getY():Float {return _position.y}
    fun getZ():Float {return _positionZ}

    fun getNewX():Float {return _newPosition.x}
    fun getNewY():Float {return _newPosition.y}
    fun getNewZ():Float {return _newPosition.z}

    fun getWolrdPosition():Vec2 {return getWolrdPosition(getPosition())}

    fun getWolrdPosition(localPos:Vec2):Vec2 {
        val pos:Vec2 = Vec2(localPos)

        var parent:SMView? = getParent()
        while (parent!=null) {
            pos.addLocal(parent.getAnchorPointInPoints())
            parent = parent.getParent()
        }

        return Vec2(pos)
    }

    fun getSkewX():Float {return _skewX}
    fun getSkewY():Float {return _skewY}

    fun setSkewX(x:Float) {
        if (x==_skewX) return

        _skewX = x
        _transformUpdated = true
        _transformDirty = true
        _inverseDirty = true
    }

    fun setSkewY(y:Float) {
        if (y==_skewY) return

        _skewY = y
        _transformUpdated = true
        _transformDirty = true
        _inverseDirty = true
    }

    open fun removeFromParentAndCleanup(cleanup:Boolean) {
        _parent?.removeChild(this, cleanup)
    }

    open fun removeChild(child: SMView?) {this.removeChild(child, true)}

    open fun removeChild(child: SMView?, cleanup: Boolean) {
        if (_children.isEmpty()) return

        if (child==null) return

        val index:Int = _children.indexOf(child)

        if (index!=-1) {
            detachChild(child, index, cleanup)
        }

        if (child==_bgView) {
            _bgView = null
        } else if (child==_touchMotionTarget) {
            _touchMotionTarget = null
        }
    }

    open fun getChildByTag(tag:Int): SMView? {
        if (BuildConfig.DEBUG && tag==-1) {
            error("Assertion Failed")
        }

        for (child in _children) {
            if (child._tag==tag) {
                return child
            }
        }

        return null
    }

    open fun removeChildByTag(tag: Int) {removeChildByTag(tag, true)}
    open fun removeChildByTag(tag: Int, cleanup: Boolean) {
        val child:SMView? = this.getChildByTag(tag)
        if (child!=null) {
            this.removeChild(child, cleanup)
        }
    }

    open fun getChildByName(name: String): SMView? {
        if (BuildConfig.DEBUG && name.isEmpty()) {
            error("Assertion Failed")
        }

        val hash:Int = name.hashCode()
        for (child in _children) {
            if (child._hashOfName==hash && child._name.equals(name, true)) {
                return child
            }
        }
        return null
    }

    open fun removeChildByName(name: String, cleanup: Boolean) {
        if (BuildConfig.DEBUG && name.isEmpty()) {
            error("Assertion Failed")
        }

        val child:SMView? = this.getChildByName(name)
        if (child!=null) {
            this.removeChild(child, cleanup)
        }
    }

    fun detachChild(child: SMView?, childIndex:Int, doCleanUp:Boolean) {
        // remove really

        if (child==null) return

        if (_running) {
            child.onExitTransitionDidStart()
            child.onExit()
        }

        if (doCleanUp) {
            child.cleanup()
        }

        releaseGLResources()

        child._parent = null
        _children.removeAt(childIndex)
        child.onRemoveFromParent(this)
    }



    fun getBackgroundView():SMView? {return _bgView}
    fun setBackgroundView(bgView:SMView?) {
        if (_bgView!=null) {
            removeChild(_bgView)
        }

        if (bgView!=null) {
            _bgView = bgView
            _bgView?.setContentSize(_contentSize)
            _bgView?.setPosition(Vec2.ZERO)
            _bgView?.setColor(_bgColor)
            _bgView?.setAlpha(_bgColor.a)
            _bgView?.setVisible(_bgColor.a!=0f)

            addChild(_bgView!!, AppConst.ZOrder.BG, "")
        }
    }

    fun setId(id:Int) {_id = id}
    fun getContext():Context {return _director?.getContext()!!}

    open fun setContentSize(size: Size) {
        setContentSize(size, true)
    }
    open fun setContentSize(width:Float?, height: Float?) {
        setContentSize(Size(width, height), true)
    }
    open fun setContentSize(size: Size, immediate:Boolean) {
        if (immediate) {
            if (!size.equal(_contentSize)) {
                _contentSize.set(size)

                _anchorPointInPoints.set(_contentSize.width*_anchorPoint.x, _contentSize.height*_anchorPoint.y)
                _contentSizeDirty = true
                _inverseDirty = true
                _transformDirty = true
                _transformUpdated = true
            }
            _bgView?.setContentSize(size)
            _newContentSize.set(size)
        } else {
            if (!size.equal(_newContentSize)) {
                _newContentSize.set(size)
                scheduleSmoothUpdate(VIEWFLAG_CONTENT_SIZE)
            }
        }
    }
    fun getContentSize():Size {return _contentSize}

    open fun setWidth(width: Float) { _contentSize.width = width}

    open fun setHeight(height: Float) {_contentSize.height = height}

    open fun isInitialized():Boolean {return true}

    open fun addChild(child: SMView?) {
        if (child==null) return
        this.addChild(child, child.getLocalZOrder(), child._name)
    }
    open fun addChild(child: SMView?, zOrder: Int) {
        if (child==null) return
        this.addChild(child, zOrder, child._name)
    }
    open fun addChild(child: SMView?, zOrder: Int, name:String) {
        if (child==null) return
        if (BuildConfig.DEBUG && child.getParent()!=null) {
            error("Assertion Failed... child already added. It can't be added again")
        }

        addChildHelper(child, zOrder, -1, name, false)
    }
    open fun addChild(child: SMView?, zOrder: Int, tag: Int) {
        if (child==null) return
        if (BuildConfig.DEBUG && child.getParent()!=null) {
            error("Assertion Failed... child already added. It can't be added again")
        }

        addChildHelper(child, zOrder, tag, "", true)
    }

    private fun addChildHelper(child: SMView, zOrder: Int, tag: Int, name: String, setTag: Boolean) {
        var ownNode:Boolean = false

        if (BuildConfig.DEBUG && this==child) {
            error("Assertion Failed... child same itself")
        }

        var parent = getParent()
        while (parent != null) {
            if (parent === child) {
                ownNode = true
                break
            }
            parent = parent.getParent()
        }

        if (BuildConfig.DEBUG && ownNode) {
            error("Assertion Failed... can't add child to itself")
        }

        if (_children.isEmpty()) {
            this.childrenAlloc()
        }

        this.insertChild(child, zOrder)

        if (setTag) {
            child.setTag(tag)
        } else {
            child.setName(name)
        }

        child._parent = this

        child.updateOrderOfArrival()

        if (_running) {
            child.onEnter()
            if (_isTransitionFinished) {
                child.onEnterTransitionDidFinish()
            }
        }
    }

    // click 판정시 호출 됨.
    protected open fun performClick(worldPoint: Vec2?) {
        if (_onClickListener != null) {
            if (_eventTargetClick != null) {
                _onClickListener!!.onClick(_eventTargetClick)
            } else {
                _onClickListener!!.onClick(this)
            }
        }
    }

    // doublic click 판정시 호출 됨.
    protected open fun performDoubleClick(worldPoint: Vec2?) {
        if (_onDoubleClickListener != null) {
            if (_eventTargetDoubleClick != null) {
                _onDoubleClickListener!!.onDoubleClick(_eventTargetDoubleClick)
            } else {
                _onDoubleClickListener!!.onDoubleClick(this)
            }
        }
    }

    // lonck lick 판정시 호출 됨.
    protected open fun performLongClick(worldPoint: Vec2?) {
        if (_onLongClickListener != null) {
            if (_eventTargetLongPress != null) {
                _onLongClickListener?.onLongClick(_eventTargetLongPress)
            } else {
                _onLongClickListener?.onLongClick(this)
            }
        }
    }

    protected fun childrenAlloc() {
        _children.ensureCapacity(4)
    }

    protected fun insertChild(child: SMView, z: Int) {
        _transformUpdated = true
        _reorderChildDirty = true
        _children.add(child)
        child._setLocalZOrder(z)
        child.onAddToParent(this)
    }

    fun removeFromParent() {
        val view:SMView? = getParent()
        if (view!=null) {
            if (view._touchMotionTarget==this) {
                view._touchMotionTarget = null
            }
        }
        this.removeFromParentAndCleanup(true)
    }

    open fun removeAllChildren() {this.removeAllChildrenWithCleanup(true)}
    open protected fun onAddToParent(parent:SMView) {}
    open protected fun onRemoveFromParent(parent: SMView) {}

    open fun removeAllChildrenWithCleanup(cleanup: Boolean) {
        if (_bgView!=null) {
            removeChild(_bgView)
        }

        for (child in _children) {
            if (_running) {
                child.onExitTransitionDidStart()
                child.onExit()
            }

            if (cleanup) {
                child.cleanup()
            }

            child.onRemoveFromParent(this)
            child._parent = null
        }

        _children.clear()

        if (_bgView!=null) {
            addChild(_bgView!!, AppConst.ZOrder.BG)
        }

        _touchMotionTarget = null
    }

    fun isContainsChidren(child: SMView):Boolean {
        return _children.contains(child)
    }

    protected fun isSmoothUpdate(flag: Long):Boolean {return _smoothFlag.and(flag) > 0L }

    class InterpolateRet {
        constructor(b:Boolean, f:Float) {
            set(b, f)
        }

        fun set(b:Boolean, f:Float) {
            retB = b
            retF = f
        }

        var retB:Boolean = false
        var retF:Float = 0.0f
    }

    private fun onInternalSmoothUpate(dt: Float) {
        var flags:Long = 0L

        if (isSmoothUpdate(VIEWFLAG_CONTENT_SIZE)) {
            flags = VIEWFLAG_CONTENT_SIZE

            var needUpdate:Boolean = false
            val ret1:InterpolateRet = smoothInterpolate(_anchorPoint.x, _newAnchorPoint.x, AppConst.Config.TOLERANCE_SCALE)
            needUpdate = needUpdate.or(ret1.retB)
            _anchorPoint.x = ret1.retF

            val ret2:InterpolateRet = smoothInterpolate(_anchorPoint.y, _newAnchorPoint.y, AppConst.Config.TOLERANCE_SCALE)
            needUpdate = needUpdate.or(ret2.retB)
            _anchorPoint.y = ret2.retF

            val s:Size = Size(_contentSize)

            val ret3:InterpolateRet = smoothInterpolate(s.width, _newContentSize.width, AppConst.Config.TOLERANCE_POSITION)
            needUpdate = needUpdate.or(ret3.retB)
            s.width = ret3.retF

            val ret4:InterpolateRet = smoothInterpolate(s.height, _newContentSize.height, AppConst.Config.TOLERANCE_POSITION)
            needUpdate = needUpdate.or(ret4.retB)
            s.height = ret4.retF

            setContentSize(s, true)

            _bgView?.setContentSize(s)

            if (!needUpdate) unscheduleSmoothUpdate(VIEWFLAG_CONTENT_SIZE)

            _anchorPointInPoints.set(_contentSize.width*_anchorPoint.x, _contentSize.height*_anchorPoint.y)

            _contentSizeDirty = true
            _transformUpdated = true
            _transformDirty = true
            _inverseDirty = true
        }

        // color
        var animColor:Boolean = false
        if (isSmoothUpdate(VIEWFLAG_ANIM_COLOR)) {
            flags = flags.or(VIEWFLAG_ANIM_COLOR)
            animColor = true

            var needUpdate:Boolean = false

            val ret1:InterpolateRet = smoothInterpolate(_animColor.r, _newAnimColor.r, AppConst.Config.TOLERANCE_COLOR)
            needUpdate = needUpdate.or(ret1.retB)
            _animColor.r = ret1.retF

            val ret2:InterpolateRet = smoothInterpolate(_animColor.g, _newAnimColor.g, AppConst.Config.TOLERANCE_COLOR)
            needUpdate = needUpdate.or(ret2.retB)
            _animColor.g = ret2.retF

            val ret3:InterpolateRet = smoothInterpolate(_animColor.b, _newAnimColor.b, AppConst.Config.TOLERANCE_COLOR)
            needUpdate = needUpdate.or(ret3.retB)
            _animColor.b = ret3.retF

            val ret4:InterpolateRet = smoothInterpolate(_animColor.a, _newAnimColor.a, AppConst.Config.TOLERANCE_COLOR)
            needUpdate = needUpdate.or(ret4.retB)
            _animColor.a = ret4.retF

            if (!needUpdate) unscheduleSmoothUpdate(VIEWFLAG_ANIM_COLOR)
        }


        if (isSmoothUpdate(VIEWFLAG_COLOR) || animColor) {
            flags = flags.or(VIEWFLAG_COLOR)

            var needUpdate:Boolean = false

            val ret1:InterpolateRet = smoothInterpolate(_realColor.r, _newColor.r, AppConst.Config.TOLERANCE_COLOR)
            needUpdate = needUpdate.or(ret1.retB)
            _realColor.r = ret1.retF

            val ret2:InterpolateRet = smoothInterpolate(_realColor.g, _newColor.g, AppConst.Config.TOLERANCE_COLOR)
            needUpdate = needUpdate.or(ret2.retB)
            _realColor.g = ret2.retF

            val ret3:InterpolateRet = smoothInterpolate(_realColor.b, _newColor.b, AppConst.Config.TOLERANCE_COLOR)
            needUpdate = needUpdate.or(ret3.retB)
            _realColor.b = ret3.retF

            val ret4:InterpolateRet = smoothInterpolate(_realColor.a, _newColor.a, AppConst.Config.TOLERANCE_COLOR)
            needUpdate = needUpdate.or(ret4.retB)
            _realColor.a = ret4.retF
            _realAlpha = _realColor.a

            if (!needUpdate && !animColor) unscheduleSmoothUpdate(VIEWFLAG_COLOR)

            _displayedColor.r = _realColor.r + _animColor.r
            _displayedColor.g = _realColor.g + _animColor.g
            _displayedColor.b = _realColor.b + _animColor.b
            setAlpha(_realColor.a + _animColor.a)

            updateCascadeColor()

            _contentSizeDirty = true
            _transformUpdated = true
            _transformDirty = true
            _inverseDirty = true
        }

        // position
        var animOffset:Boolean = false
        if (isSmoothUpdate(VIEWFLAG_ANIM_OFFSET)) {
            flags = flags.or(VIEWFLAG_ANIM_OFFSET)

            animOffset = true

            var needUpdate = false

            val ret1:InterpolateRet = smoothInterpolate(_animOffset.x, _newAnimOffset.x, AppConst.Config.TOLERANCE_POSITION)
            needUpdate = needUpdate.or(ret1.retB)
            _animOffset.x = ret1.retF

            val ret2:InterpolateRet = smoothInterpolate(_animOffset.y, _newAnimOffset.y, AppConst.Config.TOLERANCE_POSITION)
            needUpdate = needUpdate.or(ret2.retB)
            _animOffset.y = ret2.retF

            val ret3:InterpolateRet = smoothInterpolate(_animOffset.z, _newAnimOffset.z, AppConst.Config.TOLERANCE_POSITION)
            needUpdate = needUpdate.or(ret3.retB)
            _animOffset.z = ret3.retF

            if (!needUpdate) unscheduleSmoothUpdate(VIEWFLAG_ANIM_OFFSET)
        }

        if (isSmoothUpdate(VIEWFLAG_POSITION) || animOffset) {
            flags = flags.or(VIEWFLAG_POSITION)

            var needUpdate = false

            val ret1:InterpolateRet = smoothInterpolate(_realPosition.x, _newPosition.x, AppConst.Config.TOLERANCE_POSITION)
            needUpdate = needUpdate.or(ret1.retB)
            _realPosition.x = ret1.retF

            val ret2:InterpolateRet = smoothInterpolate(_realPosition.y, _newPosition.y, AppConst.Config.TOLERANCE_POSITION)
            needUpdate = needUpdate.or(ret2.retB)
            _realPosition.y = ret2.retF

            val ret3:InterpolateRet = smoothInterpolate(_realPosition.z, _newPosition.z, AppConst.Config.TOLERANCE_POSITION)
            needUpdate = needUpdate.or(ret3.retB)
            _realPosition.z = ret3.retF

            if (!needUpdate && !animOffset) unscheduleSmoothUpdate(VIEWFLAG_POSITION)

            _position.x = _realPosition.x + _animOffset.x
            _position.y = _realPosition.y + _animOffset.y
            _positionZ = _realPosition.z + _animOffset.z

            _contentSizeDirty = true
            _transformUpdated = true
            _transformDirty = true
            _inverseDirty = true
        }

        // scale
        var animScale:Boolean = false
        if (isSmoothUpdate(VIEWFLAG_ANIM_SCALE)) {
            flags = flags.or(VIEWFLAG_ANIM_SCALE)

            animScale = true

            var needUpdate = false

            val ret1:InterpolateRet = smoothInterpolate(_animScale, _newAnimScale, AppConst.Config.TOLERANCE_SCALE)
            needUpdate = needUpdate.or(ret1.retB)
            _animScale = ret1.retF

            if (!needUpdate) unscheduleSmoothUpdate(VIEWFLAG_ANIM_SCALE)
        }

        if (isSmoothUpdate(VIEWFLAG_SCALE) || animScale) {
            flags = flags.or(VIEWFLAG_SCALE)

            var needUpdate = false
            val ret1:InterpolateRet = smoothInterpolate(_realScale, _newScale, AppConst.Config.TOLERANCE_SCALE)
            needUpdate = needUpdate.or(ret1.retB)
            _realScale = ret1.retF

            if (!needUpdate && !animScale) unscheduleSmoothUpdate(VIEWFLAG_SCALE)

            _scaleZ = _realScale * _animScale
            _scaleY = _scaleZ
            _scaleX = _scaleY

            _contentSizeDirty = true
            _transformUpdated = true
            _transformDirty = true
            _inverseDirty = true
        }

        // rotate
        var animRotate:Boolean = false
        if (isSmoothUpdate(VIEWFLAG_ANIM_ROTATE)) {
            flags = flags.or(VIEWFLAG_ANIM_ROTATE)

            animRotate = true

            var needUpdate:Boolean = false

            val ret1:InterpolateRet = smoothInterpolate(_animRotation.x, _newAnimRotation.x, AppConst.Config.TOLERANCE_ROTATE)
            needUpdate = needUpdate.or(ret1.retB)
            _animRotation.x = ret1.retF

            val ret2:InterpolateRet = smoothInterpolate(_animRotation.y, _newAnimRotation.y, AppConst.Config.TOLERANCE_ROTATE)
            needUpdate = needUpdate.or(ret2.retB)
            _animRotation.y = ret2.retF

            val ret3:InterpolateRet = smoothInterpolate(_animRotation.z, _newAnimRotation.z, AppConst.Config.TOLERANCE_ROTATE)
            needUpdate = needUpdate.or(ret3.retB)
            _animRotation.z = ret3.retF

            if (!needUpdate) unscheduleSmoothUpdate(VIEWFLAG_ANIM_ROTATE)
        }

        if (isSmoothUpdate(VIEWFLAG_ROTATE) || animRotate) {
            flags = flags.or(VIEWFLAG_ROTATE)

            var needUpdate:Boolean = false

            val ret1:InterpolateRet = smoothInterpolate(_realRotation.x, _newRotation.x, AppConst.Config.TOLERANCE_ROTATE)
            needUpdate = needUpdate.or(ret1.retB)
            _realRotation.x = ret1.retF
            val ret2:InterpolateRet = smoothInterpolate(_realRotation.y, _newRotation.y, AppConst.Config.TOLERANCE_ROTATE)
            needUpdate = needUpdate.or(ret2.retB)
            _realRotation.y = ret2.retF
            val ret3:InterpolateRet = smoothInterpolate(_realRotation.z, _newRotation.z, AppConst.Config.TOLERANCE_ROTATE)
            needUpdate = needUpdate.or(ret3.retB)
            _realRotation.z = ret3.retF

            if (!needUpdate && !animRotate) unscheduleSmoothUpdate(VIEWFLAG_ROTATE)

            _rotationX = _realRotation.x + _animRotation.x
            _rotationY = _realRotation.y + _animRotation.y
            _rotationZ_X = _realRotation.z + _animRotation.z
            _rotationZ_Y = _rotationZ_X

            _contentSizeDirty = true
            _transformUpdated = true
            _transformDirty = true
            _inverseDirty = true
        }

        onSmoothUpdate(flags, dt)
    }

    protected open fun isUpdate(flag: Long): Boolean {
        return _updateFlags and flag > 0
    }

    protected open fun registerUpdate(flag: Long) {
        if (_updateFlags and flag > 0) {
            return
        }
        _updateFlags = _updateFlags or flag
    }

    protected open fun unregisterUpdate(flag: Long) {
        _updateFlags = if (flag == 0L) {
            0
        } else {
            _updateFlags and flag.inv()
        }
    }

    protected open fun onUpdateOnVisit() {}

    open fun cancelTouchEvent(targetView: SMView?, event: MotionEvent) {
        if (targetView != null) {
            if (targetView._touchMotionTarget != null) {
                cancelTouchEvent(targetView._touchMotionTarget, event)
                targetView._touchMotionTarget = null
            } else {
                targetView._touchTargeted = false
                event.action = MotionEvent.ACTION_CANCEL
                targetView.dispatchTouchEvent(event)
                targetView._touchHasFirstClicked = false
                targetView.stateChangePressToNormal()
            }
        }
    }

    open fun cancel() {
        if (_children != null) {
            val numChildCount = getChildCount()
            for (i in 0 until numChildCount) {
                val child = getChild(i)
                if (child!!.isEnabled() && child.isVisible()) {
                    child.cancel()
                }
            }
        }
    }

    open fun update(dt: Float) {}

    fun sortAllChildren() {
        if (_reorderChildDirty) {
            sortNodes(_children)
            _reorderChildDirty = false
        }
    }

    open fun visit() {
        val parentTransform:Mat4 = _director?.getMatrix(IDirector.MATRIX_STACK_TYPE.MATRIX_STACK_MODELVIEW)!!
        visit(parentTransform, 1)
    }

    open fun visit(parentTransform:Mat4, parentFlags:Int) {
        if (!_visible) return

        val flags = parentFlags //processParentFlags(parentTransform, parentFlags)

        _director?.pushMatrix(IDirector.MATRIX_STACK_TYPE.MATRIX_STACK_MODELVIEW)

        val currentMatrix:FloatArray = _director?.getMatrix(IDirector.MATRIX_STACK_TYPE.MATRIX_STACK_MODELVIEW)?.m!!
        transformMatrix(currentMatrix)
        _director?.loadMatrix(IDirector.MATRIX_STACK_TYPE.MATRIX_STACK_MODELVIEW, Mat4(currentMatrix))
//        _director?.loadMatrix(IDirector.MATRIX_STACK_TYPE.MATRIX_STACK_MODELVIEW, _modelViewTransform)


        if (_scissorEnable) {
            enableScissorTest(true)
        }

        // draw first me!!
        if (renderOwn(_modelViewTransform, flags)) {
//            var i:Int =0

            if (_children.size>0) {
                sortAllChildren()

                val size = _children.size
                var i = 0
                while (i<size) {

                    val view = _children[i]
                    if (view._localZOrder<0) {
                        view.visit(_modelViewTransform, flags)
                    } else break

                    i++
                }

                draw(_modelViewTransform, flags)

                val iter = _children.listIterator(i)
                while (iter.hasNext()) {
                    val child = iter.next()
                    child.visit(_modelViewTransform, flags)
                }
            } else {
                draw(_modelViewTransform, flags)
            }
        }

        if (_scissorEnable) {
            enableScissorTest(false)
        }

        _director!!.popMatrix(IDirector.MATRIX_STACK_TYPE.MATRIX_STACK_MODELVIEW)
    }

    open fun dispatchTouchEvent(event: MotionEvent): Int {
        // touch event를 통해 click, doubleclick을 구분한다 long-Press는 update frame에서 상태 값으로 체크한다
        if (!isEnabled()) {
            return SMView.TOUCH_TRUE
        }
        _lastTouchLocation.set(event.x, event.y)

        if (_onTouchListener != null) {
            val ret = _onTouchListener!!.onTouch(this, event)
            if (ret!= TOUCH_FALSE) {
                return ret
            }
        }
        val action = event.action
        if (_touchMotionTarget!=null) {
            if (action==MotionEvent.ACTION_DOWN) {
                _touchMotionTarget = null
            } else {
                val ret = dispatchTouchEvent(event, _touchMotionTarget!!, false)
                if (_touchMotionTarget!=null && _touchMotionTarget!!._touchTargeted && _touchMotionTarget!!.isTouchEnable()) {
                    _touchMotionTarget!!.unscheduleLongClickValidator()

                    val dx = event.x - _touchMotionTarget!!._initialTouchX
                    val dy = event.y - _touchMotionTarget!!._initialTouchY
                    val distance = sqrt(dx * dx + dy * dy.toDouble()).toFloat()

                    if (action==MotionEvent.ACTION_UP) {
                        val gap = (event.eventTime - _touchMotionTarget!!._touchEventTime) / 1000f
                        if (gap < AppConst.Config.TAP_TIMEOUT) {
                            if (_touchMotionTarget!!.isTouchMask(TOUCH_MASK_DOUBLECLICK)) {
                                // enable Double click
                                if (_touchMotionTarget!!._touchHasFirstClicked) {
                                    _touchMotionTarget!!._touchHasFirstClicked = false
                                    if (distance<AppConst.Config.SCALED_DOUBLE_TAB_SLOPE) {
                                        _touchMotionTarget!!.performDoubleClick(_lastTouchLocation)
                                    }
                                    _touchMotionTarget!!.unscheduleClickValidator()
                                } else {
                                    if (distance<AppConst.Config.SCALED_TOUCH_SLOPE) {
                                        // first click
                                        _touchMotionTarget!!._touchTargeted = false
                                    _touchMotionTarget!!._touchHasFirstClicked = true
                                        _touchMotionTarget!!._touchEventTime = event.eventTime
                                        _touchMotionTarget!!.scheduleClickValidator()
                                    } else {
                                        // not first click
                                        _touchMotionTarget!!._touchTargeted = false
                                        _touchMotionTarget!!._touchHasFirstClicked = false
                                        _touchMotionTarget!!.unscheduleClickValidator()
                                }
                                }
                                _touchMotionTarget!!.stateChangePressToNormal()
                            } else {
                                // only single click
                                _touchMotionTarget!!._touchTargeted = false
                                _touchMotionTarget!!._touchHasFirstClicked = false
                                if (ret==TOUCH_FALSE && distance<AppConst.Config.SCALED_TOUCH_SLOPE) {
                                    // just single click
                                    _touchMotionTarget!!.performClick(_lastTouchLocation)
                                }
                                _touchMotionTarget!!.stateChangePressToNormal()
                            }
                        } else {
                            _touchMotionTarget!!._touchTargeted = false
                            _touchMotionTarget!!._touchHasFirstClicked = false
                            _touchMotionTarget!!.unscheduleClickValidator()
                            _touchMotionTarget!!.unscheduleLongClickValidator()
                            _touchMotionTarget!!.stateChangePressToNormal()
                            _touchMotionTarget = null
                        }
                    } else if (action==MotionEvent.ACTION_MOVE) {

                        if (distance > AppConst.Config.SCALED_TOUCH_SLOPE) {
                            _touchMotionTarget!!._touchHasFirstClicked = false
                            _touchMotionTarget!!.unscheduleClickValidator()
                            _touchMotionTarget!!.unscheduleLongClickValidator()
                        }
                    } else if (action==MotionEvent.ACTION_CANCEL) {
                        // cancel touch
                        _touchMotionTarget!!._touchHasFirstClicked = false
                        _touchMotionTarget!!.unscheduleClickValidator()
                        _touchMotionTarget!!.unscheduleLongClickValidator()
                    }
                }

                if (action==MotionEvent.ACTION_CANCEL || action==MotionEvent.ACTION_UP) {
                    _touchMotionTarget = null
                }

                if (ret== TOUCH_INTERCEPT) {
                    return TOUCH_INTERCEPT
                }

                return TOUCH_TRUE
            }
        } else {
            if (action==MotionEvent.ACTION_DOWN) {
                val touchRet = dispatchChildren(event, 0)
                if (touchRet.retB) {
                    return touchRet.retI
                }
            }
        }

        if (isTouchEnable()) {
            return  when (action) {
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    TOUCH_FALSE
                }
                else -> {
                    TOUCH_TRUE
            }
    }
    }

        return TOUCH_FALSE
    }

    class DispatchChildrenRet {
        var retB = false
        var retI: Int = SMView.TOUCH_TRUE

        init {
            retB = false
            retI = SMView.TOUCH_TRUE
        }
    }

    private fun dispatchChildren(event: MotionEvent, touchRet: Int): DispatchChildrenRet {
        val ret = DispatchChildrenRet()
        ret.retI = touchRet
        val numChildCount = getChildCount()
        for (i in numChildCount - 1 downTo 0) {
            val child = getChild(i)
            if (!child!!.isVisible() || !child.isEnabled()) continue
            ret.retI = dispatchTouchEvent(event, child, true)
            if (ret.retI != SMView.TOUCH_FALSE) {
                _touchMotionTarget = child
                if (child._touchMotionTarget == null) {
                    _touchMotionTarget!!._touchTargeted = true
                    _touchMotionTarget!!._touchEventTime = event.eventTime
                    _touchMotionTarget!!.stateChangeNormalToPress()
                    if (!_touchMotionTarget!!._touchHasFirstClicked) {
                        _touchMotionTarget!!._initialTouchX = event.x
                        _touchMotionTarget!!._initialTouchY = event.y
                    } else {
                        _touchMotionTarget!!.unscheduleClickValidator()
                    }
                    _isPressed = false
                    // 이거 문제되면 빼자.
//                    onStateChangePressToNormal()
                    _touchHasFirstClicked = false
                    if (_touchMotionTarget!!.isTouchMask(TOUCH_MASK_LONGCLICK)) {
                        _touchMotionTarget!!.scheduleLongClickValidator()
                    }
                }
                ret.retB = true
                return ret
            }
        }
        ret.retB = false
        return ret
    }

    fun applyMatrix(event: MotionEvent, view: SMView): MotionEvent {
        SMView._matrix.reset()
        SMView._matrix.postTranslate(
            -view.getX() + view._anchorPointInPoints.x,
            -view.getY() + view._anchorPointInPoints.y
        )
        if (view._scaleX != 1.0f || view._scaleY != 1.0f) {
            SMView._matrix.postScale(1 / view._scaleX, 1 / view._scaleY)
        }
        if (view._rotationZ_X != 0f) {
            SMView._matrix.postRotate(-view._rotationZ_X)
            Mat4.createRotation(_rotationQuat, _transform)

            //Mat4::createRotation(_rotationQuat, &_transform);
        }

        val ev = MotionEvent.obtain(event)
        ev.transform(SMView._matrix)
        return ev
    }

    open fun dispatchTouchEvent(event: MotionEvent?, view: SMView, checkBounds: Boolean): Int {
        val ev = applyMatrix(event!!, view)

        val action = ev.action
        val point = Vec2(ev.getX(0), ev.getY(0))
        val isContain = view.containsPoint(point)

        view._touchPrevPosition.set(view._touchCurrentPosition)
        view._touchCurrentPosition.set(point)
        if (!view._startPointCaptured) {
            view._startPointCaptured = true
            view._touchStartPosition.set(_touchCurrentPosition)
            view._touchPrevPosition.set(_touchCurrentPosition)
        }
        if (ev.action == MotionEvent.ACTION_CANCEL || ev.action == MotionEvent.ACTION_UP || ev.action == MotionEvent.ACTION_POINTER_UP) {
            view._startPointCaptured = false
        }
        _lastTouchLocation.set(point.x, point.y)

        if (action == MotionEvent.ACTION_DOWN) {
            view._touchStartPosition.set(point.x, point.y)
            view._touchStartTime = _director!!.getGlobalTime()
        } else {
            view._touchLastPosition.set(point.x, point.y)
        }

        if (view._cancelIfTouchOutside && action == MotionEvent.ACTION_DOWN && !isContain) {
            view.cancel()
        }

        var ret = TOUCH_FALSE
        if (!checkBounds || isContain || view === _touchMotionTarget) {
            ret = view.dispatchTouchEvent(ev)
        } else if (view._ignoreTouchBounds && action == MotionEvent.ACTION_DOWN) {
            val touchRet = view.dispatchChildren(ev, ret)
            ret = touchRet.retI
        }
//        ev.recycle()
        return ret
    }


    open protected fun draw() {draw(_modelViewTransform, 1)}

    open protected fun draw(m:Mat4, flags:Int) {}

    // view transform matrix
    fun transformMatrix(matrix:FloatArray?) {
        if (matrix!=null) {
            if (BuildConfig.DEBUG && matrix.size!=16) {
                error("Assertion Failed... matrix size must be 16")
            }
            // set current x, y, z
            android.opengl.Matrix.translateM(matrix, 0, _position.x, _position.y, _positionZ)

            // scale
            if (_scaleX!=1f || _scaleY!=1f || _scaleZ!=1f) {
                android.opengl.Matrix.scaleM(matrix, 0, _scaleX, _scaleY, _scaleZ)
            }

            // rotate
            if (_rotationX!=0f) {
                android.opengl.Matrix.rotateM(matrix, 0, _rotationX, 1f, 0f, 0f)
            }
            if (_rotationY!=0f) {
                android.opengl.Matrix.rotateM(matrix, 0, _rotationY, 0f, 1f, 0f)
            }
            if (_rotationZ_X!=0f) {
                android.opengl.Matrix.rotateM(matrix, 0, _rotationZ_X, 0f, 0f, 1f)
            }

            if (!_anchorPointInPoints.equal(Vec2.ZERO)) {
                matrix[12] += matrix[0] * -_anchorPointInPoints.x + matrix[4] * -_anchorPointInPoints.y
                matrix[13] += matrix[1] * -_anchorPointInPoints.x + matrix[5] * -_anchorPointInPoints.y
                matrix[14] += matrix[2] * -_anchorPointInPoints.x + matrix[6] * -_anchorPointInPoints.y
            }
        }
    }

    fun enableScissorTest(enable:Boolean) {
        _director?.enableScissorTest(enable)
        if (enable) {
            GLES20.glScissor(_targetScissorRect.origin.x.toInt(), _targetScissorRect.origin.y.toInt(), _targetScissorRect.size.width.toInt(), _targetScissorRect.size.height.toInt())
        }
    }

    fun setScissorRect(rect: Rect) {
        if (_scissorRect==null) {
            _scissorRect = Rect(rect)
        } else {
            _scissorRect?.set(rect)
        }
    }

    fun intersectRectInWindow(rect: Rect, winSize:Size):Boolean {
        val dw:Float = winSize.width
        val dh:Float = winSize.height
        val sw:Float = rect.size.width
        val sh:Float = rect.size.height
        var x:Float = rect.origin.x
        var y:Float = rect.origin.y

        // outside on screen
        if (x+sw<=0 || x>=dw || y+sh<=0 || y>=dh || sw<=0 || sh<=0) return false

        var sx:Float = 0f
        var sy:Float = 0f
        var width:Float = sw
        var height:Float = sh

        if (x<0) {
            sx = -x
            width -= sx
        }
        if (y<0) {
            sy = -y
            height -= sy
        }
        if (x+sw>dw) {
            width -= x+sw-dw
        }
        if (y+sh>dh) {
            height -= y+sh-dh
        }

        if (x<0) x=0f
        if (y<0) y=0f

        rect.set(x, y, width, height)

        return true
    }

    private fun renderOwn(parentTransform:Mat4, parentFlags: Int):Boolean {
        if (_updateFlags>0) {
            onUpdateOnVisit()
        }

        if (_isCalledScissorEnabled) {
            val scale:Float = getScreenScale()
            var x:Float = 0f
            var y:Float = 0f
            var w:Float = 0f
            var h:Float = 0f

            var screenX:Float = 0f
            var screenY:Float = 0f

            val scissorSize:Size = Size(0f, 0f)
            if (_scissorRect!=null) {
                w = _scissorRect?.size?.width!! * scale
                h = _scissorRect?.size?.height!! * scale

                x = getScreenX() + _scissorRect!!.origin.x

                y = _director!!.getWinSize().height - (getScreenY() + h + _scissorRect!!.origin.y)
            } else {
                w = _contentSize.width * scale
                h = _contentSize.height * scale
                x = getScreenX()

                y = _director!!.getWinSize().height - (getScreenY() + h)
            }

            _targetScissorRect.set(x, y, w, h)

            if (!intersectRectInWindow(_targetScissorRect, _director!!.getWinSize())) {
                // don't draw
                return false
            }
        }

        return true
    }

    fun getScreenScale():Float {
//        val scale:Float = 1f
//        if (_parent!=null) {
//            scale = _parent?.getScreenScale()!!
//        }
        val scale:Float = _parent?.getScreenScale() ?: 1f
        return scale * _scaleX
    }

    fun getScreenAngle():Float {return getScreenAngleZ()}

    fun getScreenAngleX():Float {
        val angle:Float = _parent?.getScreenAngleX() ?: 0f
        return angle * _rotationX
    }

    fun getScreenAngleY():Float {
        val angle:Float = _parent?.getScreenAngleY() ?: 0f
        return angle * _rotationY
    }

    fun getScreenAngleZ():Float {
        val angle:Float = _parent?.getScreenAngleZ() ?: 0f
        return angle * _rotationZ_X
    }

    fun getDisplayedAlpha():Float {return _displayedAlpha}
    fun getAlpha():Float {return _realAlpha}

    fun setCancelIfTouchOutside(cancelIfTouchOutside:Boolean) {
        _cancelIfTouchOutside = cancelIfTouchOutside
    }

    fun setPivot(pivotX:Float, pivotY:Float) {
        _pivotX = pivotX
        _pivotY = pivotY
    }

    fun captureView():Bitmap? {
        return captureView(getContentSize(), Vec2(getContentSize().width/2, getContentSize().height/2), Vec2.MIDDLE, 1f, 1f)
    }

    fun captureView(canvasSize:Size, position:Vec2, anchorPoint:Vec2, scaleX:Float, scaleY:Float):Bitmap? {
        val captureKeyName:String = "CAPTURE_VIEW_" + hashCode()
        val canvas:CanvasSprite = CanvasSprite.createCanvasSprite(getDirector(), canvasSize.width.toInt(), canvasSize.height.toInt(), captureKeyName)
        var bitmap:Bitmap? = null

        val oldScale:Float = getScale()
        val oldPos:Vec2 = getPosition()
        val oldAnchor:Vec2 = getAnchorPoint()
        val oldRotate:Float = getRotation()

        if (canvas.setRenderTarget(getDirector(), true)) {
            GLES20.glClearColor(0f, 0f, 0f, 0f)
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
            GLES20.glEnable(GLES20.GL_BLEND)
            GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA)

            setPosition(position)
            setAnchorPoint(anchorPoint)
            setRotation(0f)
            setScaleX(scaleX)
            setScaleY(scaleY)

            getDirector().pushMatrix(IDirector.MATRIX_STACK_TYPE.MATRIX_STACK_MODELVIEW)

            val currentMatrix:FloatArray = getDirector().getMatrix(IDirector.MATRIX_STACK_TYPE.MATRIX_STACK_MODELVIEW).m
            transformMatrix(currentMatrix)
            getDirector().loadMatrix(IDirector.MATRIX_STACK_TYPE.MATRIX_STACK_MODELVIEW, Mat4(currentMatrix))
            visit(Mat4(Mat4.IDENTITY), 1)

            getDirector().popMatrix(IDirector.MATRIX_STACK_TYPE.MATRIX_STACK_MODELVIEW)

            bitmap = Bitmap.createBitmap(canvas.getWidth().toInt(), canvas.getHeight().toInt(), Bitmap.Config.ARGB_8888)
//            ImageProcessing.glGrabPixels(0f, 0f, bitmap, true)

            canvas.setRenderTarget(getDirector(), false)
        }

        canvas.removeTexture()

        setRotation(oldRotate)
        setScale(oldScale)
        setAnchorPoint(oldAnchor)
        setPosition(oldPos)

        return bitmap
    }



    fun ViewTransitionActionCreate(director: IDirector, target:SMView): ViewTransitionAction {
        val action:ViewTransitionAction = ViewTransitionAction(director)
        action.startWithTarget(target)
        action.initWithDuration(0f)
        return action
    }

    open class ViewTransitionAction(director:IDirector) : DelayBaseAction(director) {
        protected var _src:Rect = Rect()
        protected var _dst:Rect = Rect()
        protected var _panTime:Float = 1.5f

        override fun onUpdate(t: Float) {
            updateTextureRect(t)
        }

        fun updateTextureRect(t:Float) {
            val x:Float = SMView.interpolation(_src.origin.x, _dst.origin.x, t)
            val y:Float = SMView.interpolation(_src.origin.y, _dst.origin.y, t)
            val w:Float = SMView.interpolation(_src.size.width, _dst.size.width, t)
            val h:Float = SMView.interpolation(_src.size.height, _dst.size.height, t)

            _target!!.setContentSize(w, h)
            _target!!.setPosition(x, y)
        }

        fun setValue(src:Rect, dst:Rect, duration:Float, delay:Float) {
            setTimeValue(duration, delay)

            _src.set(src)
            _dst.set(dst)

            updateTextureRect(0f)
        }
    }


    fun pointInView(x:Float, y:Float):Boolean {
        if (_scaleX==0f) return false

        _matrix.reset()
        _matrix.postTranslate(-_position.x, -_position.y)
        if (_scaleX!=1f || _scaleY!=1f) {
            _matrix.postScale(1f/_scaleX, 1f/_scaleY)
        }
        if (_rotationZ_X!=0f) {
            _matrix.postRotate(-_rotationZ_X)
        }
        _mapPoint[0] = x
        _mapPoint[1] = y
        _matrix.mapPoints(_mapPoint)

        val ptX:Float = _mapPoint[0]
        val ptY:Float = _mapPoint[1]

        return containsPoint(ptX, ptY)
    }

    fun scheduleClickValidator() {
        if (_onClickValidateCallback==null) {
            _onClickValidateCallback = object : SEL_SCHEDULE {
                override fun scheduleSelector(t: Float) {
                    onClickValidator(t)
                }
            }
        }

        if (!isScheduled(_onClickValidateCallback)) {
            scheduleOnce(_onClickValidateCallback, AppConst.Config.DOUBLE_TAP_TIMEOUT)
        }
    }

    fun unscheduleClickValidator() {
        if (_onClickValidateCallback!=null && isScheduled(_onClickValidateCallback)) {
            unschedule(_onClickValidateCallback)
        }
    }

    fun scheduleLongClickValidator() {
        if (_onLongClickValidateCallback==null) {
            _onLongClickValidateCallback = object : SEL_SCHEDULE {
                override fun scheduleSelector(t: Float) {
                    onLongClickValidator(t)
                }
            }
        }

        if (!isScheduled(_onLongClickValidateCallback)) {
            scheduleOnce(_onLongClickValidateCallback, AppConst.Config.LONG_PRESS_TIMEOUT)
        }
    }

    fun unscheduleLongClickValidator() {
        if (_onLongClickValidateCallback!=null && isScheduled(_onLongClickValidateCallback)) {
            unschedule(_onLongClickValidateCallback)
        }
    }

    fun onClickValidator(dt: Float) {
        if (isTouchMask(TOUCH_MASK_DOUBLECLICK) && _touchHasFirstClicked) {
            _touchHasFirstClicked = false
            _touchTargeted = false
            performClick(_lastTouchLocation)
        }
    }

    fun onLongClickValidator(dt: Float) {
        if (isTouchMask(TOUCH_MASK_LONGCLICK) && !_touchHasFirstClicked) {
            _touchHasFirstClicked = false
            _touchTargeted = false
            stateChangePressToNormal()
            performLongClick(_lastTouchLocation)
        }
    }
}