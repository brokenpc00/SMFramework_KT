package com.brokenpc.smframework

import android.content.Context
import android.content.Intent
import android.graphics.RectF
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.view.MotionEvent
import android.view.OrientationEventListener
import androidx.fragment.app.FragmentActivity
import com.android.volley.BuildConfig
import com.android.volley.RequestQueue
import com.brokenpc.smframework.IDirector.MATRIX_STACK_TYPE
import com.brokenpc.smframework.IDirector.SharedLayer
import com.brokenpc.smframework.base.SMScene
import com.brokenpc.smframework.base.SMView
import com.brokenpc.smframework.base.shape.PrimitiveCircle
import com.brokenpc.smframework.base.shape.PrimitiveLine
import com.brokenpc.smframework.base.shape.PrimitiveRect
import com.brokenpc.smframework.base.shape.PrimitiveSolidRect
import com.brokenpc.smframework.base.sprite.CanvasSprite
import com.brokenpc.smframework.base.sprite.Sprite
import com.brokenpc.smframework.base.sprite.SpriteSet
import com.brokenpc.smframework.base.texture.CanvasTexture
import com.brokenpc.smframework.base.texture.Texture
import com.brokenpc.smframework.base.texture.TextureManager
import com.brokenpc.smframework.base.transition.SwipeBack
import com.brokenpc.smframework.base.transition.SwipeDismiss
import com.brokenpc.smframework.base.transition.TransitionScene
import com.brokenpc.smframework.base.types.*
import com.brokenpc.smframework.shader.ShaderManager
import com.brokenpc.smframework.shader.ShaderNode
import com.brokenpc.smframework.shader.ShaderProgram
import com.brokenpc.smframework.util.AppConst
import com.brokenpc.smframework.util.OpenGlUtils.Companion.getLookAtMatrix
import com.brokenpc.smframework.util.OpenGlUtils.Companion.getPerspectiveMatrix
import com.brokenpc.smframework.view.EdgeSwipeForDismiss
import com.brokenpc.smframework.view.EdgeSwipeLayerForPushBack
import com.brokenpc.smframework.view.EdgeSwipeLayerForSideMenu
import java.util.*
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.collections.ArrayList
import kotlin.math.max
import kotlin.math.tan

class SMDirector : IDirector, GLSurfaceView.Renderer {

    private var _activity:FragmentActivity? = null
    private var _initialized:Boolean = false
    private var _width:Int = 0
    private var _height:Int = 0
    private var _deviceWidth:Int = 0
    private var _deviceHeight:Int = 0
    private var _displayRawWidth:Int = 0
    private var _displayRawHeight:Int = 0
    private var _displayAdjust:Float = 1f
    private var _threadOwner:Thread? = null

    private lateinit var _sharedLayer:ArrayList<SMView?>
    private val _colorBuffer:FloatArray = FloatArray(4)
    private var _frameBufferId:Int = 0
    private var _frameBuffer:CanvasSprite? = null

    private var _spriteSet:SpriteSet? = null
    private var _primFillBox:PrimitiveRect? = null
    private var _primHollowBox:PrimitiveRect? = null
    private var _primLine:PrimitiveLine? = null
    private var _primCircle:PrimitiveCircle? = null
    private var _primSolidRect:PrimitiveSolidRect? = null


    private lateinit var _shaderManager:ShaderManager
    private lateinit var _textureManager:TextureManager
    private val _frameBufferMat:Mat4 = Mat4(Mat4.IDENTITY)
    private val _motionEventQueue:Queue<MotionEvent> = LinkedList()
    private val _sceneStack:Stack<SMScene> = Stack()
    private var _timerUpdate:Boolean = true
    private var _currentTime:Long = 0L
    private lateinit var _requestQueue:RequestQueue
    private var _rootSceneInitialized:Boolean = false
    private lateinit var _orientationListener:OrientationListener
    private var _screenOrientation:Int = 0
    private var _scissorTestEnable:Boolean = false

    private class DelayedRunnable(action: Runnable, startTickCount:Int) {

        var action:Runnable = action
        var startTickCount:Int = startTickCount
    }

    private lateinit var _openGLView:GLSurfaceView
    private var _lastUpdate:Long = 0L
    private var _threadId:Long = 0L
    fun getThreadId():Long {return _threadId}
    private var _invalid:Boolean = true
    private var _lastTouchDownTime:Float = 0f
    private var _touchMotionTarget:SMView? = null
    private val _winSizeInPoints:Size = Size()
    enum class Projection {
        _2D, _3D, CUSTOM
    }
    private var _projection:Projection = Projection._2D
    private var _deltaTime:Float = 0f
    private var _globalTime:Float = 0f
    private var _nextDeltaTimeZero:Boolean = false
    private var _dimLayer:SMView? = null
    private var _menuSwipe: EdgeSwipeLayerForSideMenu? = null
    private var _backSwipe: EdgeSwipeLayerForPushBack? = null
    private var _dismissSwipe: EdgeSwipeForDismiss? = null
    private var _swipeLayer: SMView? = null



    protected var _runnginScene:SMScene? = null
    protected var _nextScene:SMScene? = null
    protected var _sendCleanupToScene:Boolean = false
    protected lateinit var _scheduler:Scheduler
    protected lateinit var _actionManager:ActionManager
    protected val _modelViewMatrixStack:Stack<Mat4> = Stack()
    protected val _projectionMatrixStackList:Vector<Stack<Mat4>> = Vector()
    protected val _textureMatrixStack:Stack<Mat4> = Stack()
    protected var _paused:Boolean = false


    var _touchEventDispather:Boolean = false


    constructor(activity: FragmentActivity, openGLView:GLSurfaceView) {
        _activity = activity
        _scheduler = Scheduler(this)
        _actionManager = ActionManager(this)
        _scheduler.scheduleUpdate(_actionManager, Scheduler.PRIORITY_SYSTEM, false)
        _lastUpdate = System.currentTimeMillis()

        _shaderManager = ShaderManager()
        _textureManager = TextureManager(this)

        _width = BASE_SCREEN_WIDTH
        _timerUpdate = true

        _lastTouchDownTime = getGlobalTime()
        _invalid = false

        _openGLView = openGLView
        _winSizeInPoints.set(_openGLView.width.toFloat(), _openGLView.height.toFloat())

        _orientationListener = OrientationListener(activity)

        _sharedLayer = ArrayList(enumToIntForSharedLayer(SharedLayer.POPUP)+1)
        for (i in 0 until enumToIntForSharedLayer(SharedLayer.POPUP)+1) {
            _sharedLayer.add(null)
        }

        _instance = this
    }

    companion object {
        private var _instance: SMDirector? = null
        fun getDirector(): SMDirector { return _instance!! }

        const val BASE_SCREEN_WIDTH:Int = 1080
        const val MAX_MARIX_BUFFER:Int = 32
        const val SIDE_MENU_ENABLE:Boolean = true

        var _sideMenu:SideMenu? = null

        @JvmStatic
        fun intToEnumForSharedLayer(num: Int): SharedLayer {
            return when (num) {
                1 -> SharedLayer.LEFT_MENU
                2 -> SharedLayer.RIGHT_MENU
                3 -> SharedLayer.BETWEEN_MENU_AND_SCENE
                4 -> SharedLayer.BETWEEN_SCENE_AND_UI
                5 -> SharedLayer.UI
                6 -> SharedLayer.BETWEEN_UI_AND_POPUP
                7 -> SharedLayer.DIM
                8 -> SharedLayer.POPUP
                else -> SharedLayer.BACKGROUND
            }
        }

        @JvmStatic
        fun enumToIntForSharedLayer(layer: SharedLayer): Int {
            return when (layer) {
                SharedLayer.LEFT_MENU -> 1
                SharedLayer.RIGHT_MENU -> 2
                SharedLayer.BETWEEN_MENU_AND_SCENE -> 3
                SharedLayer.BETWEEN_SCENE_AND_UI -> 4
                SharedLayer.UI -> 5
                SharedLayer.BETWEEN_UI_AND_POPUP -> 6
                SharedLayer.DIM -> 7
                SharedLayer.POPUP -> 8
                else -> 0
            }
        }


        @JvmStatic
        fun GLToClipTransform(transformOut:Mat4?) {
            if (transformOut==null) return

            val director = getDirector()
            val projection:Mat4 = director.getMatrix(MATRIX_STACK_TYPE.MATRIX_STACK_PROJECTION)
            val modelview:Mat4 = director.getMatrix(MATRIX_STACK_TYPE.MATRIX_STACK_MODELVIEW)
            transformOut.set(projection.multiplyRet(modelview))
        }
    }

    override fun setSharedLayer(layerId: SharedLayer, layer: SMView?) {

        if (_sharedLayer[enumToIntForSharedLayer(layerId)]!=null) {
            _sharedLayer[enumToIntForSharedLayer(layerId)]!!.onExitTransitionDidStart()
            _sharedLayer[enumToIntForSharedLayer(layerId)]!!.onExit()
            _sharedLayer[enumToIntForSharedLayer(layerId)]!!.cleanup()
        }

        _sharedLayer[enumToIntForSharedLayer(layerId)] = layer
        if (layer==null) { return }

        _sharedLayer[enumToIntForSharedLayer(layerId)]!!.onEnter()
        _sharedLayer[enumToIntForSharedLayer(layerId)]!!.onEnterTransitionDidFinish()
    }

    override fun getSharedLayer(layerId: SharedLayer): SMView? {
        return _sharedLayer[enumToIntForSharedLayer(layerId)]
    }


    override fun getShaderManager():ShaderManager {return _shaderManager!!}
    override fun getActionManager():ActionManager {return _actionManager}
    override fun getScheduler(): Scheduler {return _scheduler}

    override fun getScreenOrientation(): Int {return ((_screenOrientation+45)/90*90)%360}

    inner class OrientationListener(context: Context) : OrientationEventListener(context) {
        fun roundOrientation(orientation:Int):Int {
            return ((orientation + 45) / 90 * 90) % 360
        }

        override fun onOrientationChanged(orientation: Int) {
            if (orientation == ORIENTATION_UNKNOWN) return

            _screenOrientation = orientation
        }
    }

    override fun enableScissorTest(enable: Boolean) {
        _scissorTestEnable = enable
        if (enable) {
            GLES20.glEnable(GLES20.GL_SCISSOR_TEST)
        } else {
            GLES20.glDisable(GLES20.GL_SCISSOR_TEST)
        }
    }

    override fun isScissorTestEnabled(): Boolean {
        return _scissorTestEnable
    }

    override fun getWinSize(): Size {
        return Size(_width, _height)
    }

    override fun setFrameBufferId(frameBufferId: Int) {
        _frameBufferId = frameBufferId
    }

    override fun getMatrix(type:MATRIX_STACK_TYPE):Mat4 {
        return when (type) {
            MATRIX_STACK_TYPE.MATRIX_STACK_MODELVIEW -> _modelViewMatrixStack.peek()
            MATRIX_STACK_TYPE.MATRIX_STACK_PROJECTION -> _projectionMatrixStackList[0].peek()
            MATRIX_STACK_TYPE.MATRIX_STACK_TEXTURE -> _textureMatrixStack.peek()
        }
    }

    override fun showProgress(show: Boolean, bounds: RectF) {

    }

    override fun showUploadProgress(show: Boolean, state: Int, bounds: RectF) {

    }

    override fun setSideMenuOpenPosition(position: Float) {
        setMenuSwipe()
        SideMenu.GetSideMenu().setOpenPosition(position)
    }

    protected fun setMenuSwipe() {
        if (_sideMenu==null) {
            _sideMenu = SideMenu.GetSideMenu()
            _sideMenu!!.setSideMenuListener(null)
        }

        _sideMenu!!.setSwipeLayer(_menuSwipe!!)
        _sideMenu!!._sideMenuUpdateCallback = object : SideMenu.SIDE_MENU_UPDATE_CALLBACK {
            override fun onSideMenuUpdateCallback(
                state: IDirector.SIDE_MENU_STATE,
                position: Float
            ) {
                sideMenuCallback(state, position)
            }
        }
    }

    fun sideMenuCallback(state:IDirector.SIDE_MENU_STATE, position: Float) {
        var f:Float = position / _sideMenu!!.getContentSize().width
        if (f<=0f) {

        } else if (f>1f) {
            f = 1f
        }

        if (f>0f) {
            if (!_dimLayer!!.isVisible()) {
                _dimLayer!!.setVisible(true)
            }

            _dimLayer!!.setContentSize(Size(getWinSize().width-position, getWinSize().height))
            _dimLayer!!.setPositionX(position)
            _dimLayer!!.setAlpha(0.5f*f)
        } else {
            if (_dimLayer!!.isVisible()) {
                _dimLayer!!.setVisible(false)
            }
        }

        var runnsingScene:SMScene? = null
        var inScene:SMScene? = null
        if (getRunningScene() != null) {
            runnsingScene = getRunningScene()
            if (runnsingScene is TransitionScene) {
                runnsingScene.setPositionX(position+getDirector().getWinSize().width/2)
                inScene = runnsingScene.getInScene()
            inScene?.setPositionX(getDirector().getWinSize().width/2)
            return
        }
    }
        runnsingScene?.setPositionX(position+getDirector().getWinSize().width/2)
    }

    fun onEdgeDismissUpdateCallback(state: Int, position: Float) {
        var dismiss: SwipeDismiss? = null

        if (getRunningScene() !is SwipeDismiss) {
            if (position>0) {
                dismiss = SwipeDismiss.create(this, getPreviousScene()!!)
                popSceneWithTransition(dismiss!!)
            }
        } else {
            dismiss = getRunningScene() as SwipeDismiss
            if (!_dismissSwipe!!.isScrollTargeted()) {
                if (position<=0) {
                    dismiss.cancel()
                    _dismissSwipe!!.reset()
                } else if (position>_dismissSwipe!!.getContentSize().height) {
                    dismiss.finish()
                    _dismissSwipe!!.reset()
                } else {
                    // touch disable
                    setTouchEventDispatcherEnable(false)
                }
            }
        }

        if (dismiss!=null) {
            dismiss.getOutScene()!!.setPositionY(position+ getDirector().getWinSize().height/2)
            val progress:Float = (dismiss.getOutScene()!!.getPositionY() - getDirector().getWinSize().height/2) / getDirector().getWinSize().height

            val minusScale:Float = 0.6f*progress
            val newScale:Float = 1.6f - minusScale
            dismiss.getInScene()!!.setScale(newScale)
        }
    }

    override fun getRunningScene(): SMScene? {
        return _runnginScene
    }

    override fun getSideMenuState(): IDirector.SIDE_MENU_STATE {
        setMenuSwipe()
        return SideMenu.GetSideMenu().getState()!!
    }

    override fun runWithScene(scene: SMScene) {
        pushScene(scene)
        startSceneAnimation()
    }


    private fun initMatrixStack() {
        while (!_modelViewMatrixStack.empty()) {
            _modelViewMatrixStack.pop()
        }

        _projectionMatrixStackList.clear()

        while (!_textureMatrixStack.empty()) {
            _textureMatrixStack.pop()
        }

        _modelViewMatrixStack.push(Mat4(Mat4.IDENTITY))
        val projectionMatrixStack:Stack<Mat4> = Stack()
        projectionMatrixStack.push(Mat4(Mat4.IDENTITY))
        _projectionMatrixStackList.add(projectionMatrixStack)
        _textureMatrixStack.push(Mat4(Mat4.IDENTITY))
    }

    fun resetMatrixStack() {initMatrixStack()}

    fun initProjectionMatrixStack(stackCount:Int) {
        _projectionMatrixStackList.clear()
        for (i in 0 until stackCount) {
            val projectionMatrixStack:Stack<Mat4> = Stack()
            projectionMatrixStack.push(Mat4(Mat4.IDENTITY))
            _projectionMatrixStackList.add(projectionMatrixStack)
        }
    }

    fun getProjectionMatrixStackSize():Int {return _projectionMatrixStackList.size}

    fun onBackPressd():Boolean {
        var scene:SMScene? = null
        synchronized(_sceneStack) {
            try {
                scene = getTopScene()
            } catch (e:ArrayIndexOutOfBoundsException) {
                return false
            }
        }

        if (scene!!.isRunning()) {
            val ret:Boolean = scene!!.onBackPressed()
            if (!ret && _sceneStack.size==1) {
                scene!!.onExit()
                scene!!.onEnterTransitionDidFinish()
                finishApplication()
                return false
            }
            return ret
        }

        return false
    }

    fun finishApplication() {
        _activity!!.runOnUiThread {
            _activity!!.finish()
        }
    }

    fun onResume() {
        _orientationListener.enable()
        startSceneAnimation()

        _scheduler.performFunctionInMainThread(object : PERFORM_SEL{
            override fun performSelector() {
                var scene:SMScene? = null

                synchronized(_sceneStack) {
                    try {
                        scene = getTopScene()
                    } catch (e:ArrayIndexOutOfBoundsException) {
                        return
                    }
                }
                _deltaTime = 0f
                setTouchEventDispatcherEnable(true)
                if (scene!!.isInitialized()) {
                    scene!!.onResume()
                }

                for (i in 0 until enumToIntForSharedLayer(SharedLayer.POPUP)+1) {
                    if (_sharedLayer[i]!=null)
                    if (_sharedLayer[i]!!.isInitialized()) {
                        _sharedLayer[i]!!.onResume()
                    }
                }

                _textureManager.onResume()
            }
        })
    }

    fun onPause() {
        _orientationListener.disable()
        stopSceneAnimation()
        var scene:SMScene? = null
        synchronized(_sceneStack) {
            try {
                scene = getTopScene()
            } catch (e:ArrayIndexOutOfBoundsException) {
                return
            }
        }

        scene!!.onPause()
        for (i in 0 until enumToIntForSharedLayer(SharedLayer.POPUP)+1) {
            if (_sharedLayer[i]!=null) {
                _sharedLayer[i]!!.onPause()
            }
        }

        releaseResources()
    }

    fun releaseResources() {
        _shaderManager.release(this)
        _textureManager.onPause()
    }

    override fun isGLThread(): Boolean {
        return Thread.currentThread() == _threadOwner
    }

    override fun onSurfaceCreated(p0: GL10?, p1: EGLConfig?) {
        GLES20.glClearColor(0f, 0f, 0f, 1f)
        GLES20.glDisable(GLES20.GL_DEPTH_TEST)
        setColor(1f, 1f, 1f, 1f)

        _threadOwner = Thread.currentThread()
        setTouchEventDispatcherEnable(true)
        _instance = this
    }

    fun beginProjectionMatrix() {
        _width = BASE_SCREEN_WIDTH
        _height = _deviceHeight * BASE_SCREEN_WIDTH / _deviceWidth
        _displayAdjust = (BASE_SCREEN_WIDTH.toFloat() / _deviceWidth)

        GLES20.glViewport(0, 0, _deviceWidth, _deviceHeight)

        val m1 = FloatArray(16)
        val m2 = FloatArray(16)

        val w = getWidth().toFloat()
        val h = getHeight().toFloat()

        val zNear = 0.01f*1000f
        val zFar = 10000f*1000f
        val fov = 40f
        val ratio = w/h
        val dist:Float = (h/2f/ tan(SMView.toRadians(fov.toDouble())/2f)).toFloat()

        getPerspectiveMatrix(m1, fov, ratio, zNear, zFar)
        getLookAtMatrix(m2, 0f, 0f, dist, 0f, 0f, 0f, 0f, 1f, 0f)
        Matrix.multiplyMM(_frameBufferMat.m, 0, m1, 0, m2, 0)
        Matrix.translateM(_frameBufferMat.m, 0, -getWidth()/2f, getHeight()/2f, 0f)
        Matrix.scaleM(_frameBufferMat.m, 0, 1f, -1f, 1f)

        initMatrixStack()

    }

    override fun popMatrix(type: IDirector.MATRIX_STACK_TYPE) {
        when(type) {
            MATRIX_STACK_TYPE.MATRIX_STACK_MODELVIEW -> {
                val mat:Mat4 = _modelViewMatrixStack.pop()
                _shaderManager.setMatrix(Mat4(mat.m).m)
            }
            MATRIX_STACK_TYPE.MATRIX_STACK_PROJECTION -> {
                _projectionMatrixStackList[0].pop()
            }
            MATRIX_STACK_TYPE.MATRIX_STACK_TEXTURE -> {
                _textureMatrixStack.pop()
            }
        }
    }

    override fun pushMatrix(type: MATRIX_STACK_TYPE) {
        when (type) {
            MATRIX_STACK_TYPE.MATRIX_STACK_MODELVIEW -> {
                val mat:Mat4 = Mat4(_modelViewMatrixStack.peek())
                _modelViewMatrixStack.push(mat)
            }
            MATRIX_STACK_TYPE.MATRIX_STACK_PROJECTION -> {
                _projectionMatrixStackList[0].push(Mat4(_projectionMatrixStackList[0].peek()))
            }
            MATRIX_STACK_TYPE.MATRIX_STACK_TEXTURE -> {
                _textureMatrixStack.push(Mat4(_textureMatrixStack.peek()))
            }
        }
    }

    override fun onSurfaceChanged(p0: GL10?, width: Int, height: Int) {
        _instance = this
        if (width>0 && height>0) {
            _deviceWidth = width
            _deviceHeight = height

            beginProjectionMatrix()

            startTimer()
            setTouchEventDispatcherEnable(true)

            if (SIDE_MENU_ENABLE && _sideMenu==null) {
                _sideMenu = SideMenu.GetSideMenu()
                _sideMenu!!.setSideMenuListener(null)
            }

            if (_dimLayer==null) {
                _dimLayer = SMView.create(this, 0f, 0f, getWidth().toFloat(), getHeight().toFloat())
                _dimLayer!!.setBackgroundColor(Color4F.BLACK)
                _dimLayer!!.setAlpha(0f)
                _dimLayer!!.setVisible(false)
            }

            for (i in 0 until enumToIntForSharedLayer(SharedLayer.POPUP)+1) {
                if (_sharedLayer[i]!=null) {
                    continue
                }

                val layerId = intToEnumForSharedLayer(i)

                if (layerId==SharedLayer.RIGHT_MENU) {
                    // not used.. so.. passed
                    continue
                }

                if (layerId==SharedLayer.LEFT_MENU) {
                    setSharedLayer(layerId, _sideMenu)
                    continue
                }

                if (layerId==SharedLayer.DIM) {
                    setSharedLayer(layerId, _dimLayer)
                    continue
                }

                // new Layer
                setSharedLayer(layerId, SMView(this, 0f, 0f, getWidth().toFloat(), getHeight().toFloat()))
            }

            if (_backSwipe==null) {
                // for swipe back
                _backSwipe = EdgeSwipeLayerForPushBack.create(this, 0, 0f, 0f, getWidth().toFloat(), getHeight().toFloat())
                _backSwipe!!.setSwipeWidth(getWidth().toFloat())
                _backSwipe!!.setEdgeWidth(AppConst.SIZE.EDGE_SWIPE_MENU)
                _backSwipe!!._swipeUpdateCallback = object : EdgeSwipeLayerForPushBack.SWIPTE_BACK_UPDATE_CALLBCK {
                    override fun onSwipeUpdate(a: Int, b: Float) {
                        onEdgeBackUpdateCallback(a, b)
                    }
                }
                _backSwipe!!.onEnter()
                _backSwipe!!.onEnterTransitionDidFinish()
                _backSwipe!!.reset()
            }

            if (_dismissSwipe==null) {
                _dismissSwipe = EdgeSwipeForDismiss.create(this, 0, 0f, 0f, getWidth().toFloat(), getHeight().toFloat())
                _dismissSwipe!!.setSwipeWidth(getHeight().toFloat())
                _dismissSwipe!!.setEdgeWidth(AppConst.SIZE.EDGE_SWIPE_TOP)
                _dismissSwipe!!._swipeUpdateCallback = object : EdgeSwipeForDismiss.SWIPE_DISMISS_UPDATE_CALLBACK {
                    override fun onSwipeUpdate(a: Int, b: Float) {
                        onEdgeDismissUpdateCallback(a, b)
                    }
                }
                _dismissSwipe!!.onEnter()
                _dismissSwipe!!.onEnterTransitionDidFinish()
                _dismissSwipe!!.reset()
            }

            if (_menuSwipe==null) {
                _menuSwipe = EdgeSwipeLayerForSideMenu.create(this, 0, 0f, 0f, getWidth().toFloat(), getHeight().toFloat())
                _menuSwipe!!.setSwipeWidth(_sideMenu?.getContentSize()?.width?:0f)
                _menuSwipe!!.setEdgeWidth(AppConst.SIZE.EDGE_SWIPE_MENU)
                _menuSwipe!!.setBackgroundColor(Color4F(1f, 0f, 0f, 0.4f))
                _menuSwipe!!.setOnClickListener(object : SMView.OnClickListener {
                    override fun onClick(view: SMView?) {
                        if (_sideMenu!=null && _sideMenu!!.getState()==IDirector.SIDE_MENU_STATE.OPEN) {
                            val p1 = _sideMenu!!.getOpenPosition() + getWidth()
                            val p2 = _swipeLayer!!.getLastTouchLocation().x
                            if (p2<p1) {
                                SideMenu.CloseMenu()
                            }
                        } else {
                            SideMenu.OpenMenu(getRunningScene())
                        }
                    }
                })
            }

            setMenuSwipe()

            if (_swipeLayer==null) {
                _swipeLayer = SMView.create(this)
                _swipeLayer!!.addChild(_menuSwipe)
                _swipeLayer!!.onEnter()
                _swipeLayer!!.onEnterTransitionDidFinish()
            }

            if (_frameBuffer==null) {
                _frameBuffer = CanvasSprite.createCanvasSprite(this, _width, _height, "FRAME_BUFFER")
            }
        }
    }

    fun onEdgeBackUpdateCallback(state:Int, position:Float) {
        var backScene:SwipeBack? = null

        if (getRunningScene() !is SwipeBack) {
            if (position>0) {
                backScene = SwipeBack.create(this, getPreviousScene()!!)
                popSceneWithTransition(backScene!!)
            }
        } else {
            backScene = getRunningScene() as SwipeBack?
            if (backScene!=null && !_backSwipe!!.isScrollTargeted()) {
                if (position<=0) {
                    backScene.cancel()
                    _backSwipe?.reset()
                } else if (position>=_backSwipe!!.getContentSize().width) {
                    backScene.finish()
                    _backSwipe?.reset()
                } else {
                    setTouchEventDispatcherEnable(false)
                }
            }
        }

        if (backScene!=null) {
            // for origin
//            backScene.getOutScene()?.setPositionX(position)
//
//            backScene.getInScene()?.setPositionX(0.3f * (-_backSwipe!!.getContentSize().width + position) + getDirector().getWinSize().width/2f)
//            val inScene:SMScene? = backScene.getInScene()
//            if (inScene!=null) {
//                inScene.setPositionX(0.3f * (-_backSwipe!!.getContentSize().width + position))
//            }

            // for scale
            backScene.getOutScene()?.setPositionX(position+getDirector().getWinSize().width/2f)

            val inScene:SMScene? = backScene.getInScene()
            if (inScene!=null) {
                inScene.setPositionX(0.3f * (-_backSwipe!!.getContentSize().width + position) + getDirector().getWinSize().width/2f)
                val progress:Float = backScene.getLastProgress()
                val minusScale = 0.6f * progress
//                inScene.setScale(1.6f-minusScale)
            }
        }
    }

    private fun usedLayer(event:MotionEvent, layer:SMView?, action: Int, ret:Int): Int {
        return if (layer!=null && layer.isVisible() && (action==MotionEvent.ACTION_DOWN || _touchMotionTarget==layer)) {
            layer.dispatchTouchEvent(event, layer, false)
        } else { ret }
    }

    private fun handleTouchEvent() {
        // pop MotionEvent from Queue
        synchronized(_motionEventQueue) {
            while (!_motionEventQueue.isEmpty()) {
                val event = _motionEventQueue.poll()!!
                var nowTime:Float = getGlobalTime()

                val action = event.action
                if (action==MotionEvent.ACTION_DOWN) {
                    _lastTouchDownTime = nowTime
                } else if (action==MotionEvent.ACTION_UP || action==MotionEvent.ACTION_CANCEL) {
                    if (_lastTouchDownTime==nowTime) {
                        nowTime = _lastTouchDownTime + (1.0f / 60.0f)
                    }
                }

                val worldPoint = Vec2(event.getX(0), event.getY(0))
                var touchLayer:SMView? = null
                var newTouchTarget:SMView? = null

                event.setLocation(event.getX(0)*_displayAdjust, event.getY(0)*_displayAdjust)
                var ret = SMView.TOUCH_FALSE

                do {
                    // touch sequence
                    touchLayer = getSharedLayer(SharedLayer.POPUP)
                    ret = usedLayer(event, touchLayer, action, ret)
                    if (ret!=SMView.TOUCH_FALSE) {
                        newTouchTarget = touchLayer
                        break
                    }

                    touchLayer = getSharedLayer(SharedLayer.UI)
                    ret = usedLayer(event, touchLayer, action, ret)
                    if (ret!=SMView.TOUCH_FALSE) {
                        newTouchTarget = touchLayer
                        break
                    }

                    var runningScene:SMScene? = null
                    var type:SMScene.SwipeType = SMScene.SwipeType.NONE
                    var backScene:SwipeBack? = null
                    var dismissScene:SwipeDismiss? = null

                    if (getRunningScene() is TransitionScene) {
                        if (getRunningScene() is SwipeDismiss) {
                            dismissScene = getRunningScene() as SwipeDismiss
                        }

                        if (dismissScene!=null) {
                            type = SMScene.SwipeType.DISMISS
                            runningScene = dismissScene.getOutScene()
                        } else {
                            if (getRunningScene() is SwipeBack) {
                                backScene = getRunningScene() as SwipeBack
                            }
                            if (backScene!=null) {
                                type = SMScene.SwipeType.BACK
                                runningScene = backScene.getOutScene()
                            }
                        }
                    } else {
                        runningScene = getRunningScene()!!
                        type = runningScene.getSwipeType()
                    }

                    if (runningScene!=null) {
                        when (type) {
                            SMScene.SwipeType.MENU -> {
                                if (_swipeLayer!=null && runningScene !is TransitionScene) {
                                    if (!(action==MotionEvent.ACTION_DOWN && _menuSwipe!=null && _menuSwipe!!.isScrollArea(worldPoint) && !runningScene.canSwipe(worldPoint, type))) {
                                        val nRet = _swipeLayer!!.dispatchTouchEvent(event, _swipeLayer!!, false)
                                        if (nRet==SMView.TOUCH_INTERCEPT) {
                                            if (_touchMotionTarget!=null && _touchMotionTarget!= _sideMenu) {
                                                _touchMotionTarget!!.cancelTouchEvent(_touchMotionTarget, event)
                                                _touchMotionTarget = null
                                            }
                                        }
                                    }
                                }
                            }
                            SMScene.SwipeType.BACK -> {
                                if (_backSwipe!=null) {
                                    if (!(action==MotionEvent.ACTION_DOWN && _backSwipe!!.isScrollArea(worldPoint) && !runningScene.canSwipe(worldPoint, type))) {
                                        val nRet:Int = _backSwipe!!.dispatchTouchEvent(event, _backSwipe!!, false)
                                        if (nRet==SMView.TOUCH_INTERCEPT) {
                                            if (_touchMotionTarget!=null && _touchMotionTarget!=_backSwipe) {
                                                _touchMotionTarget!!.cancelTouchEvent(_touchMotionTarget, event)
                                                _touchMotionTarget = null
                                            }
                                        }
                                    }
                                }
                            }
                            SMScene.SwipeType.DISMISS -> {
                                if (_dismissSwipe!=null) {
                                    if (!(action==MotionEvent.ACTION_DOWN && !runningScene.canSwipe(worldPoint, type))) {
                                        val nRet:Int = _dismissSwipe!!.dispatchTouchEvent(event, _dismissSwipe!!, false)
                                        if (nRet==SMView.TOUCH_INTERCEPT) {
                                            if (_touchMotionTarget!=null && _touchMotionTarget!=_dismissSwipe) {
                                                _touchMotionTarget!!.cancelTouchEvent(_touchMotionTarget, event)
                                                _touchMotionTarget = null
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (runningScene!=null) {
                        touchLayer = runningScene.getRootView()
                    }

                    if (_sideMenu==null || _sideMenu!!.getState()==IDirector.SIDE_MENU_STATE.CLOSE) {
                        if (touchLayer!=null && touchLayer.isVisible() && (action==MotionEvent.ACTION_DOWN || _touchMotionTarget==touchLayer)) {
                            ret = touchLayer.dispatchTouchEvent(event, touchLayer, true)
                            newTouchTarget = touchLayer

                            if (ret==SMView.TOUCH_FALSE && action==MotionEvent.ACTION_DOWN) {
                                val point:Vec2 = Vec2(event.getX(0), event.getY(0))
                                if (touchLayer.containsPoint(point)) {
                                    ret = SMView.TOUCH_TRUE
                                }
                            }
                        }

                        if (ret!=SMView.TOUCH_FALSE) {
                            newTouchTarget = touchLayer
                            if (_swipeLayer!=null && ret==SMView.TOUCH_INTERCEPT && _menuSwipe!=null && _menuSwipe!!.isScrollTargeted()) {
                                _swipeLayer!!.cancelTouchEvent(_swipeLayer!!, event)
                            }
                            break
                        }
                    }

                    if (_sideMenu==null || _sideMenu!!.getState()!=IDirector.SIDE_MENU_STATE.CLOSE) {
                        touchLayer = getSharedLayer(SharedLayer.LEFT_MENU)
                        if (touchLayer!=null && touchLayer.isVisible() && (action==MotionEvent.ACTION_DOWN || _touchMotionTarget==touchLayer)) {
                            ret = touchLayer.dispatchTouchEvent(event, touchLayer, false)
                            newTouchTarget = touchLayer
                        }
                        if (ret!=SMView.TOUCH_FALSE) {
                            newTouchTarget = touchLayer
                            break
                        }
                    }
                } while (false)

                if (action==MotionEvent.ACTION_DOWN && newTouchTarget!=null) {
                    _touchMotionTarget = newTouchTarget
                } else if (action==MotionEvent.ACTION_UP) {
                    _touchMotionTarget = null
                }

                try {
                event.recycle()
                } catch (e: Exception) {

                }
            }
        }
    }

    override fun setTouchEventDispatcherEnable(enable: Boolean) {
        _touchEventDispather = enable
    }

    override fun getTouchEventDispatcherEnable():Boolean {return _touchEventDispather}

    fun addTouchEvent(event: MotionEvent) {
        val ev:MotionEvent = MotionEvent.obtain(event)

        synchronized(_motionEventQueue) {
            val point = Vec2(ev.getX(0), ev.getY(0))
            _motionEventQueue.add(ev)
        }


        try {
            Thread.sleep(10)
        } catch (e:InterruptedException) {

        }
    }

    fun setNextDeltaTimeZero(flag:Boolean) {_nextDeltaTimeZero = flag}

    fun startTimer() {
        _timerUpdate = true
        _lastUpdate = System.currentTimeMillis()
        val thread = Thread.currentThread()
        _threadId = thread.id
        _invalid = false
    }

    fun stopTimer() {_timerUpdate = false}

    override fun getGlobalTime():Float {return _globalTime}

    fun calculateDeltaTime() {
        if (_nextDeltaTimeZero) {
            _deltaTime = 0f
            _nextDeltaTimeZero = false
        } else {
            if (_timerUpdate) {
                val time = System.currentTimeMillis()
                _deltaTime = (time - _lastUpdate)/1000.0f
                _currentTime += time - _lastUpdate
                _lastUpdate = time
            }

            _deltaTime = max(0f, _deltaTime)
        }
    }
    override fun onDrawFrame(p0: GL10?) {

        bindTexture(null)

        if (_frameBuffer==null) return

        calculateDeltaTime()
        _globalTime += _deltaTime

        _scheduler.update(_deltaTime)

        if (_nextScene!=null) {
            setNextScene()
        }

        val framBuffer:CanvasTexture = _frameBuffer?.getTexture() as CanvasTexture

        framBuffer.setFrameBuffer(this, true)
        setFrameBufferId(framBuffer.getId())

        GLES20.glClearColor(1f, 1f, 1f, 1f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA)
        GLES20.glDisable(GLES20.GL_CULL_FACE)
        GLES20.glDisable(GLES20.GL_DEPTH_TEST)

        // detect and process touch event
        handleTouchEvent()

        // GL VIEW FRAME UPDATE

        pushMatrix(MATRIX_STACK_TYPE.MATRIX_STACK_MODELVIEW)
        loadMatrix(MATRIX_STACK_TYPE.MATRIX_STACK_MODELVIEW, _frameBufferMat)

        if (!_invalid) {
            for (i in 0 until enumToIntForSharedLayer(SharedLayer.POPUP)+1) {
                val layerId:SharedLayer = intToEnumForSharedLayer(i)

                val drawLayer:SMView? = _sharedLayer[i]
                if (drawLayer!=null && drawLayer.isVisible()) {
                    drawLayer.visit(getMatrix(MATRIX_STACK_TYPE.MATRIX_STACK_MODELVIEW), 0)
                }

                if (layerId==SharedLayer.BETWEEN_MENU_AND_SCENE) {
                    if (_runnginScene!=null) {
                        _runnginScene!!.visit(getMatrix(MATRIX_STACK_TYPE.MATRIX_STACK_MODELVIEW), 0)
                        if (!_rootSceneInitialized) {
                            _rootSceneInitialized = true
                            _runnginScene!!.onEnter()
                            _runnginScene!!.onEnterTransitionDidFinish()
                        }
                    }
                }
            }
        }

        popMatrix(MATRIX_STACK_TYPE.MATRIX_STACK_MODELVIEW)

        framBuffer.setFrameBuffer(this, false)
        setColor(1f, 1f, 1f, 1f)
        GLES20.glViewport(0, 0, getDeviceWidth(), getDeviceHeight())
        loadMatrix(MATRIX_STACK_TYPE.MATRIX_STACK_MODELVIEW, _frameBufferMat)

        val height = getHeight().toFloat()
        _frameBuffer!!.drawScaleXY(0f, height, 1f, -1f)

        try {
            Thread.sleep(1)
        } catch (e:InterruptedException) {

        }

        _initialized = true
    }

    override fun getActivity(): FragmentActivity { return _activity!! }

    override fun getContext(): Context {
        return _activity!!
    }

    override fun getWidth(): Int {
        return _width
    }

    override fun getHeight(): Int {
        return _height
    }

    override fun getDeviceWidth(): Int {
        return _deviceWidth
    }

    override fun getDeviceHeight(): Int {
        return _deviceHeight
    }

    override fun getDisplayAdjust(): Float {
        return _displayAdjust
    }

    override fun getColor(): FloatArray {
        return _colorBuffer
    }

    override fun setColor(r: Float, g: Float, b: Float, a: Float) {
        _colorBuffer[0] = r
        _colorBuffer[1] = g
        _colorBuffer[2] = b
        _colorBuffer[3] = a
    }

    override fun bindTexture(texture: Texture?): Boolean {
        return _textureManager.bindTexture(texture)
    }

    override fun useProgram(type: ShaderManager.ProgramType): ShaderProgram? {
        var program:ShaderProgram? = _shaderManager.getActiveProgram()

        if (program==null || program.getType()!=type) {
            program?.unbind()
            program = _shaderManager.useProgram(this, type)
            if (program==null) return null else program.bind()
        }
        return program
    }

    fun pushProjectionMatrix(index:Int) {
        _projectionMatrixStackList[index].push(Mat4(getProjectionMatrix(index)))
    }

    override fun getProjectionMatrix(index: Int): Mat4 {
        return _projectionMatrixStackList[index].peek()
    }

    fun setViewPort() {
        GLES20.glViewport(0, 0, _winSizeInPoints.width.toInt(), _winSizeInPoints.height.toInt())
    }

    override fun loadMatrix(type: MATRIX_STACK_TYPE, mat: Mat4) {
        when (type) {
            MATRIX_STACK_TYPE.MATRIX_STACK_MODELVIEW -> {
                _modelViewMatrixStack.peek().set(Mat4(mat.m))
                _shaderManager.setMatrix(Mat4(mat.m).m)
            }
            MATRIX_STACK_TYPE.MATRIX_STACK_PROJECTION -> {
                _projectionMatrixStackList[0][_projectionMatrixStackList[0].size-1] = mat
            }
            MATRIX_STACK_TYPE.MATRIX_STACK_TEXTURE -> {
                _textureMatrixStack[_textureMatrixStack.size-1] = mat
            }
        }
    }

    fun loadProjectionMatrix(mat: Mat4, index: Int) {
        _projectionMatrixStackList[index][_projectionMatrixStackList[index].size-1] = mat
    }

    fun multiplyProjectionMatrix(mat: Mat4, index: Int) {
        _projectionMatrixStackList[index][_projectionMatrixStackList[index].size-1] = _projectionMatrixStackList[index].peek().multiplyRet(mat)
    }

    fun multiplyMatrix(type: MATRIX_STACK_TYPE, mat: Mat4) {
        when (type) {
            MATRIX_STACK_TYPE.MATRIX_STACK_MODELVIEW -> {
                _modelViewMatrixStack[_modelViewMatrixStack.size-1] = _modelViewMatrixStack.peek().multiplyRet(mat)
            }
            MATRIX_STACK_TYPE.MATRIX_STACK_PROJECTION -> {
                _projectionMatrixStackList[0][_projectionMatrixStackList[0].size-1] = _projectionMatrixStackList[0].peek().multiplyRet(mat)
            }
            MATRIX_STACK_TYPE.MATRIX_STACK_TEXTURE -> {
                _textureMatrixStack[_textureMatrixStack.size-1] = _textureMatrixStack.peek().multiplyRet(mat)
            }
        }
    }

    fun loadIdentityMatrix(type: MATRIX_STACK_TYPE) {
        val m = Mat4(Mat4.IDENTITY)
        when (type) {
            MATRIX_STACK_TYPE.MATRIX_STACK_MODELVIEW -> {
                _modelViewMatrixStack[_modelViewMatrixStack.size-1] = m
            }
            MATRIX_STACK_TYPE.MATRIX_STACK_PROJECTION -> {
                _projectionMatrixStackList[0][_projectionMatrixStackList[0].size-1] = m
            }
            MATRIX_STACK_TYPE.MATRIX_STACK_TEXTURE -> {
                _textureMatrixStack[_textureMatrixStack.size-1] = m
            }
        }
    }

    fun getZEye():Float {
        return (_winSizeInPoints.height/1.154700538379252f) //(2 * tanf(M_PI/6))
    }

    fun setProjection(projection: Projection) {
        val size = _winSizeInPoints

        if (size.width==0f || size.height==0f) {
            return
        }

        setViewPort()

        when (projection) {
            Projection._2D -> {
                val orthoMatrix:Mat4 = Mat4()
                Mat4.createOrthographicOffCenter(0f, size.width, 0f, size.height, -1024f, 1024f, orthoMatrix)
                loadMatrix(MATRIX_STACK_TYPE.MATRIX_STACK_PROJECTION, orthoMatrix)
                loadIdentityMatrix(MATRIX_STACK_TYPE.MATRIX_STACK_MODELVIEW)
            }
            Projection._3D -> {
                val zEye:Float = this.getZEye()
                val matrixPerspective:Mat4 = Mat4()
                val matrixLookup:Mat4 = Mat4()

                Mat4.createPerspective(60f, size.width/size.height, 10f, zEye+size.height/2f, matrixPerspective)

                val eye:Vec3 = Vec3(size.width/2, size.height/2, zEye)
                val center:Vec3 = Vec3(size.width/2, size.height/2, 0f)
                val up:Vec3 = Vec3(0f, 1f, 0f)
                Mat4.createLookAt(eye, center, up, matrixLookup)
                val proj3d:Mat4 = matrixPerspective.multiplyRet(matrixLookup)

                loadMatrix(MATRIX_STACK_TYPE.MATRIX_STACK_MODELVIEW, proj3d)
                loadIdentityMatrix(MATRIX_STACK_TYPE.MATRIX_STACK_MODELVIEW)
            }
            else -> {

            }
        }

        _projection = projection
    }

    fun popProjectionMatrix(index: Int) {
        _projectionMatrixStackList[index].pop()
    }

    fun loadProjectionIdentityMatrix(index: Int) {
        _projectionMatrixStackList[index][_projectionMatrixStackList[index].size-1] = Mat4(Mat4.IDENTITY)
    }

    override fun getTickCount(): Long {
        return _currentTime
    }

    // primitive

    override fun drawFillRect(x: Float, y: Float, width: Float, height: Float) {
        if (_primFillBox==null) {
            _primFillBox = PrimitiveRect(this, 1f, 1f, 0f, 0f)
        }
        _primFillBox!!.drawScaleXY(x, y, width, height)
    }

    override fun drawRect(x: Float, y: Float, width: Float, height: Float, lineWidth: Float) {
        if (_primHollowBox==null) {
            _primHollowBox = PrimitiveRect(this, 1f, 1f, 0f, 0f, false)
        }
        GLES20.glLineWidth(lineWidth)
        _primHollowBox!!.drawScaleXY(x, y, width, height)
    }

    override fun drawLine(x1: Float, y1: Float, x2: Float, y2: Float, lineWidth: Float) {
        if (_primLine==null) {
            _primLine = PrimitiveLine(this)
        }
        GLES20.glLineWidth(lineWidth)
        _primLine!!.drawLine(x1, y1, x2, y2)
    }

    override fun drawCircle(x: Float, y: Float, radius: Float) {
        drawCircle(x, y, radius, 1.5f)
    }

    override fun drawCircle(x: Float, y: Float, radius: Float, aaWidth: Float) {
        if (_primCircle==null) {
            _primCircle = PrimitiveCircle(this)
        }
        _primCircle!!.drawCircle(x, y, radius, aaWidth)
    }

    override fun drawRing(x: Float, y: Float, radius: Float, thickness: Float) {
        drawRing(x, y, radius, thickness, 1.5f)
    }

    override fun drawRing(x: Float, y: Float, radius: Float, thickness: Float, aaWidth: Float) {
        if (_primCircle==null) {
            _primCircle = PrimitiveCircle(this)
        }
        _primCircle!!.drawRing(x, y, radius, thickness, aaWidth)
    }

    override fun drawSolidRect(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        cornerRadius: Float
    ) {
        drawSolidRect(x, y, width, height, cornerRadius, 1f)
    }

    override fun drawSolidRect(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        cornerRadius: Float,
        aaWidth: Float
    ) {
        if (_primSolidRect==null) {
            _primSolidRect = PrimitiveSolidRect(this)
        }
        _primSolidRect!!.draawRect(x, y, width, height, cornerRadius, aaWidth)
    }

    override fun getSpriteSet(): SpriteSet? {
        return _spriteSet
    }


    override fun getTopScene(): SMScene? {
        synchronized(_sceneStack) {
            val numScene:Int = _sceneStack.size
            if (numScene>0) {
                return _sceneStack.peek()
            }
            return null
        }
    }

    private fun getSecondScene(): SMScene? {
        synchronized(_sceneStack) {
            val numScene:Int = _sceneStack.size
            if (numScene>0) {
                return _sceneStack[numScene-2]
            }
            return null
        }
    }

    override fun getTextureManager(): TextureManager {
        return _textureManager
    }

    override fun setDisplayRawSize(width: Int, height: Int) {
        _displayRawWidth = width
        _displayRawHeight = height

        _deviceWidth = _displayRawWidth
        _deviceHeight = _displayRawHeight

        _width = BASE_SCREEN_WIDTH
        _height = _displayRawHeight * (BASE_SCREEN_WIDTH / _displayRawWidth)

        _displayAdjust = BASE_SCREEN_WIDTH.toFloat() / _displayRawWidth

        ShaderNode.DEFAULT_ANTI_ALIAS_WIDTH = BASE_SCREEN_WIDTH.toFloat()/width

        beginProjectionMatrix()
    }

    override fun getDisplayRawWidth(): Int {
        return _displayRawWidth
    }

    override fun getDisplayRawHeight(): Int {
        return _displayRawHeight
    }

    override fun getRequestQueue(): RequestQueue {
        return _requestQueue
    }

    override fun getFrameBufferId(): Int {
        return _frameBufferId
    }

    override fun getFrameBufferSprite(): Sprite? {
        return _frameBuffer
    }

    override fun getFrameBufferMatrix(): Mat4 {
        return _frameBufferMat
    }

    fun onActivityResult(requestCode:Int, resultCode:Int, data:Intent) {
        _scheduler.performFunctionInMainThread(object : PERFORM_SEL{
            override fun performSelector() {
                synchronized(_sceneStack) {
                    getTopScene()?.onActivityResult(requestCode, resultCode, data)
                }
            }
        })
    }

    override fun isSendCleanupToScene(): Boolean {
        return _sendCleanupToScene
    }

    override fun replaceScene(scene: SMScene) {
        if (_runnginScene==null) {
            runWithScene(scene)
            return
        }

        if (scene==_nextScene) {
            return
        }

        if (_nextScene!=null) {
            if (_nextScene!!.isRunning()) {
                _nextScene!!.onExit()
            }

            _nextScene!!.cleanup()
            _nextScene = null
        }

        val index = _sceneStack.size - 1
        _sendCleanupToScene = true

        _sceneStack[index] = scene
        _nextScene = scene
    }

    override fun pushScene(scene: SMScene) {
        _sendCleanupToScene = false
        _sceneStack.push(scene)
        _nextScene = scene
    }

    override fun popScene() {
        _sceneStack.pop()
        val c:Int = _sceneStack.size
        if (c==0) {
            _sideMenu?.removeFromParent()
            _sideMenu = null
        } else {
            _sendCleanupToScene = true
            _nextScene = _sceneStack[c-1]
        }
    }

    fun end() {
        SideMenu.GetSideMenu().clearMenu()
        _activity?.finish()
    }

    override fun popToRootScene() {
        popToSceneStackLevel(1)
    }

    override fun popToSceneStackLevel(levelt: Int) {
        var c:Int = _sceneStack.size
        var level = levelt
        if (level==0) {
            end()
            return
        }

        if (c in 1..level) {
            level = c - 1
        }

        while (c>level) {
            val current:SMScene = _sceneStack.peek()

            if (current.isRunning()) {
                current.onExit()
            }

            current.cleanup()

            _sceneStack.pop()
            --c
        }

        _nextScene = _sceneStack.peek()
        _sendCleanupToScene = true
    }

    override fun setNextScene() {
        val runningIsTransition:Boolean = _runnginScene is TransitionScene
        val newIsTransition = _nextScene is TransitionScene

        if (!newIsTransition) {
            _runnginScene?.onExitTransitionDidStart()
            _runnginScene?.onExit()

            if (_sendCleanupToScene) {
                _runnginScene?.cleanup()
                _runnginScene?.setState(SMScene.STATE_FINISHING)
            }
        }

        _runnginScene = _nextScene
        _nextScene = null

        if (!runningIsTransition) {
            _runnginScene?.onEnter()
            _runnginScene?.onEnterTransitionDidFinish()
        }
    }

    override fun getPreviousScene(): SMScene? {
        val c = _sceneStack.size
        if (c<=1) return null

        return _sceneStack[c-2]
    }

    override fun popSceneWithTransition(scene: SMScene) {
        if (BuildConfig.DEBUG && _runnginScene==null) {
            error("Assertion Failed")
        }

        _sceneStack.pop()
        val c = _sceneStack.size
        if (c==0) {
            end()
        } else {
            _sendCleanupToScene = true
            _nextScene = scene
        }
    }

    override fun convertToUI(glPoint: Vec2): Vec2 {
        val transform:Mat4 = Mat4()
        GLToClipTransform(transform)

        val clipCoord:Vec4 = Vec4()
        val glCoord:Vec4 = Vec4(glPoint.x, glPoint.y, 0f, 1f)

        transform.transformVector(glCoord, clipCoord)

        clipCoord.x = clipCoord.x / clipCoord.w
        clipCoord.y = clipCoord.y / clipCoord.w
        clipCoord.z = clipCoord.z / clipCoord.w

        val glSize:Size = getWinSize()
        val factor:Float = 1.0f / glCoord.w

        return Vec2(glSize.width * (clipCoord.x * 0.5f + 0.5f) * factor, glSize.height * (-clipCoord.y * 0.5f + 0.5f) * factor)
    }

    override fun getSceneStackCount(): Int {
        return _sceneStack.size
    }

    override fun pause() {
        if (_paused) {
            return
        }
    }

    override fun isPaused(): Boolean {
        return _paused
    }

    override fun resume() {

    }

    override fun startSceneAnimation() {
        startTimer()
    }

    override fun stopSceneAnimation() {
        _invalid = true
    }
}