package com.interpark.smframework.view.Sticker

import android.util.Log
import android.view.MotionEvent
import com.brokenpc.smframework.base.types.Vec2
import com.brokenpc.smframework.util.AppConst
import java.lang.Exception
import java.lang.reflect.Method
import kotlin.math.*

class MultiTouchController<T> {
    lateinit var objectCanvas: MultiTouchObjectCanvas<T>
    private var mCurrPt = PointInfo()
    private var mPrevPt = PointInfo()
    private var mInitPt = PointInfo()
    private var mLastUpPt = PointInfo()

    fun getCurrentPoint(): PointInfo {return mCurrPt}

    private var mCurrPtX = 0f
    private var mCurrPtY = 0f
    private var mCurrPtDiam = 0f
    private var mCurrPtWidth = 0f
    private var mCurrPtHeight = 0f
    private var mCurrPtAng = 0f

    private var handleSingleTouchEvents = false
    private var selectedObject:T? = null
    private var prevSelectedObject:T? = null
    private var mCurrXform = PositionAndScale()
    private var mSettleStartTime = 0L
    private var mSettleEndTime = 0L
    private var startPosX = 0f
    private var startPosY = 0f
    private var startAngle = 0.0
    private var startRadius = 0.0
    private var startScaleOverPinchDiam = 0f
    private var startAngleMinusPinchAngle = 0f
    private var startScaleXOverPinchWidth = 0f
    private var startScaleYOverPinchHeight = 0f
    private var mDragOccurred = false

    fun dragOccurred(): Boolean {return mDragOccurred}

    private var mMode = MODE_NOTHING

    companion object {
        const val EVENT_SETTLE_TIME_INTERVAL = 20L
        const val MAX_MULTITOUCH_POS_JUMP_SIZE = 30.0f
        const val MAX_MULTITOUCH_DIM_JUMP_SIZE = 40.0f
        const val MIN_MULTITOUCH_SEPARATION = 10.0f
        const val MAX_TOUCH_POINTS = 20
        const val THRESHOLD = 0.001f
        const val SETTLE_THRESHOLD = 5.0f
        const val DEBUG = false
        const val MODE_NOTHING = 0
        const val MODE_DRAG = 1
        const val MODE_PINCH = 2

        var multiTouchSupported = false
        var succeeded = false

        lateinit var m_getPointerCount:Method
        lateinit var m_getPointerId:Method
        lateinit var m_getPressure:Method
        lateinit var m_getHistoricalX:Method
        lateinit var m_getHistoricalY:Method
        lateinit var m_getHistoricalPressure:Method
        lateinit var m_getX:Method
        lateinit var m_getY:Method

        private var ACTION_POINTER_UP = 6
        private var ACTION_POINTER_INDEX_SHIFT = 8

        private val xVals:FloatArray = FloatArray(MAX_TOUCH_POINTS)
        private val yVals:FloatArray = FloatArray(MAX_TOUCH_POINTS)
        private val pressureVals:FloatArray = FloatArray(MAX_TOUCH_POINTS)
        private val pointerIds: IntArray = IntArray(MAX_TOUCH_POINTS)

        class PointInfo() {
            var numPoints = 0
            val xs:FloatArray = FloatArray(MAX_TOUCH_POINTS)
            val ys:FloatArray = FloatArray(MAX_TOUCH_POINTS)
            val pressures: FloatArray = FloatArray(MAX_TOUCH_POINTS)
            val pointerIds: IntArray = IntArray(MAX_TOUCH_POINTS)
            var xMid = 0f
            var yMid = 0f
            var pressureMid = 0f
            var dx = 0f
            var dy = 0f
            var diameter = 0f
            var diameterSq = 0f
            var angle = 0f
            var _isDown = false
            var _isMultiTouch = false
            var diameterSqIsCalculated = false
            var diameterIsCalculated = false
            var angleIsCalculated = false
            var action = 0
            var eventTime = 0L

            fun set(numPoints: Int, x: FloatArray, y: FloatArray, pressure: FloatArray, pointerIds: IntArray, action: Int, _isDown: Boolean, eventTime: Long) {
//                if (DEBUG) {
//                    val xx = if (numPoints>1) x[1] else x[0]
//                    val yy = if (numPoints>1) y[1] else y[0]
//                    Log.i(
//                        "MultiTouch",
//                        "[[[[[ Got Here 8 - $numPoints ${x[0]} ${y[0]} $xx $yy $action $_isDown}"
//                    )
//                }

                this.eventTime = eventTime
                this.action = action
                this.numPoints = numPoints
                for (i in 0 until numPoints) {
                    this.xs[i] = x[i]
                    this.ys[i] = y[i]
                    this.pressures[i] = pressure[i]
                    this.pointerIds[i] = pointerIds[i]
                }
                this._isDown = _isDown
                this._isMultiTouch = numPoints>=2


                if (_isMultiTouch) {
                    xMid = (x[0] + x[1]) * .5f
                    yMid = (y[0] + y[1]) * .5f
                    pressureMid = (pressure[0] + pressure[1]) * .5f
                    dx = abs(x[1] - x[0])
                    dy = abs(y[1] - y[0])
                } else {
                    xMid = x[0]
                    yMid = y[0]
                    dx = 0.0f
                    dy = 0.0f
                }

                diameterSqIsCalculated = false
                diameterIsCalculated = false
                angleIsCalculated = false
            }

            fun set(other: PointInfo) {
                this.numPoints = other.numPoints
                for (i in 0 until this.numPoints) {
                    this.xs[i] = other.xs[i]
                    this.ys[i] = other.ys[i]
                    this.pressures[i] = other.pressures[i]
                    this.pointerIds[i] = other.pointerIds[i]
                }

                this.xMid = other.xMid
                this.yMid = other.yMid
                this.pressureMid = other.pressureMid
                this.dx = other.dx
                this.dy = other.dy
                this.diameter = other.diameter
                this.diameterSq = other.diameterSq
                this.angle = other.angle
                this._isDown = other._isDown
                this.action = this.action
                this._isMultiTouch = other._isMultiTouch
                this.diameterIsCalculated = other.diameterIsCalculated
                this.diameterSqIsCalculated = other.diameterSqIsCalculated
                this.angleIsCalculated = other.angleIsCalculated
                this.eventTime = other.eventTime
            }

            fun isMultiTouch(): Boolean {return _isMultiTouch}

            fun getMultiTouchWidth(): Float {return if (_isMultiTouch) dx else 0f}

            fun getMultiTouchHeight(): Float {return if (_isMultiTouch) dy else 0f}

            private fun julery_isqrt(v: Int): Int {
                var temp: Int
                var g = 0
                var b = 0x8000
                var bshft = 15
                var vv = v

                do {
                    if (v >= (g.shl(1) + b).shl(bshft--).also { temp = it }) {
                        g += b
                        vv -= temp
                    }
//                b = b.shr(1)
                } while (b.shr(1).also { b = it } > 0)

                return g
            }

            fun getMultiTouchDiameterSq(): Float {
                if (!diameterSqIsCalculated) {
                    diameterSq = if (_isMultiTouch) {dx*dx+dy+dy} else 0f
                    diameterSqIsCalculated = true
                }
                return diameterSq
            }

            fun getMultiTouchDiameter(): Float {
                if (!diameterIsCalculated) {
                    if (!_isMultiTouch) {
                        diameter = 0f
                    } else {
                        val diamSq = getMultiTouchDiameterSq()
                        diameter = if (diameterSq==0f) 0f else {julery_isqrt((256*diamSq).toInt()).toFloat()/16f}

                        if (diameter < dx) diameter = dx
                        if (diameter < dy) diameter = dy
                    }
                    diameterIsCalculated = true
                }
                return diameterSq
            }

            fun getMultiTouchAngle(): Float {
                if (!angleIsCalculated) {
                    angle = if (!_isMultiTouch) { 0f } else { atan2(ys[1]-ys[0], xs[1]-xs[0]) }
                    angleIsCalculated = true
                }
                return angle
            }

            fun getNumTouchPoints(): Int {return numPoints}

            fun getPoint(): Vec2 {return Vec2(getX(), getY())
            }

            fun getX(): Float {return xMid}

            @JvmName("getXs1")
            fun getXs(): FloatArray {return xs}

            fun getY(): Float {return yMid}

            @JvmName("getYs1")
            fun getYs(): FloatArray {return ys}

            fun getDistance(other: PointInfo): Float {
                val deltaX = getX() - other.getX()
                val deltaY = getY() - other.getY()
                return sqrt(deltaX*deltaX + deltaY*deltaY)
            }

            fun getPointerIdArray(): IntArray {return pointerIds}

            fun getPressure(): Float {return pressureMid}

            @JvmName("getPressures1")
            fun getPressures(): FloatArray {return pressures}

            fun isDown(): Boolean {return _isDown}

            @JvmName("getAction1")
            fun getAction(): Int {return action}

            @JvmName("getEventTime1")
            fun getEventTime(): Long {return eventTime}
        }


        class PositionAndScale {
            var xOff = 0f
            var yOff = 0f
            var scale = 1f
            var scaleX = 1f
            var scaleY = 1f
            var angle = 1f
            var updateScale = false
            var updateScaleXY = false
            var updateAngle = false

            fun set(xOff: Float, yOff: Float, updateScale: Boolean, scale: Float, updateScaleXY: Boolean, scaleX: Float, scaleY: Float, updateAngle: Boolean, angle: Float) {
                this.xOff = xOff
                this.yOff = yOff
                this.updateScale = updateScale
                this.scale = if (scale==0f) 1f else scale
                this.updateScaleXY = updateScaleXY
                this.scaleX = if (scaleX==0f) 1f else scaleX
                this.scaleY = if (scaleY==0f) 1f else scaleY
                this.updateAngle = updateAngle
                this.angle = angle
            }

            fun set(xOff: Float, yOff: Float, scale: Float, scaleX: Float, scaleY: Float, angle: Float) {
                this.xOff = xOff
                this.yOff = yOff
                this.scale = if (scale==0f) 1f else scale
                this.scaleX = if (scaleX==0f) 1f else scaleX
                this.scaleY = if (scaleY==0f) 1f else scaleY
                this.angle = angle
            }

            @JvmName("getXOff1")
            fun getXOff(): Float {return xOff}

            @JvmName("getYOff1")
            fun getYOff(): Float {return yOff}

            @JvmName("getScale1")
            fun getScale(): Float {return if (!updateScale) 1f else scale}

            @JvmName("getScaleX1")
            fun getScaleX(): Float {return if (!updateScaleXY) 1f else scaleX}

            @JvmName("getScaleY1")
            fun getScaleY(): Float {return if (!updateScaleXY) 1f else scaleY}

            @JvmName("getAngle1")
            fun getAngle(): Float {return if (!updateAngle) 0f else angle}
        }

        interface MultiTouchObjectCanvas<T> {
            fun getDraggableObjectAtPoint(touchPoint: PointInfo): T?

            fun getPositionAndScale(obj: T?, objPosAndScaleOut: PositionAndScale)

            fun setPositionAndScale(obj: T?, newObjPosAndScale: PositionAndScale, touchPoint: PointInfo): Boolean

            fun selectObject(obj: T?, touchPoint: PointInfo)

            fun doubleClickObject(obj: T?, touchPoint: PointInfo)

            fun touchModeChanged(touchMode: Int, touchPoint: PointInfo)

            fun toWorldPoint(canvasPoint: Vec2): Vec2

            fun toCanvasPoint(worldPoint: Vec2): Vec2
        }
    }


    constructor(objectCanvas: MultiTouchObjectCanvas<T>) {
        this.objectCanvas = objectCanvas
        this.handleSingleTouchEvents = true
    }

    constructor(objectCanvas: MultiTouchObjectCanvas<T>, handleSingleTouchEvents: Boolean) {
        this.objectCanvas = objectCanvas
        this.handleSingleTouchEvents = handleSingleTouchEvents
    }

    init {
        try {
            m_getPointerCount = MotionEvent::class.java.getMethod("getPointerCount")
            m_getPointerId = MotionEvent::class.java.getMethod("getPointerId", Integer.TYPE)
            m_getPressure = MotionEvent::class.java.getMethod("getPressure", Integer.TYPE)
            m_getHistoricalX = MotionEvent::class.java.getMethod("getHistoricalX", Integer.TYPE, Integer.TYPE)
            m_getHistoricalY = MotionEvent::class.java.getMethod("getHistoricalY", Integer.TYPE, Integer.TYPE)
            m_getHistoricalPressure = MotionEvent::class.java.getMethod("getHistoricalPressure", Integer.TYPE, Integer.TYPE)
            m_getX = MotionEvent::class.java.getMethod("getX", Integer.TYPE)
            m_getY = MotionEvent::class.java.getMethod("getY", Integer.TYPE)
            succeeded = true
        } catch (e: Exception) {

        }
        multiTouchSupported = succeeded
        if (multiTouchSupported) {
            ACTION_POINTER_UP = MotionEvent::class.java.getField("ACTION_POINTER_UP").getInt(null)
            ACTION_POINTER_INDEX_SHIFT = MotionEvent::class.java.getField("ACTION_POINTER_INDEX_SHIFT").getInt(null)
        }
    }

    fun extractCurrPtInfo() {
        mCurrPtX = mCurrPt.getX()
        mCurrPtY = mCurrPt.getY()
        mCurrPtDiam = (MIN_MULTITOUCH_SEPARATION * .71f).coerceAtLeast(if (!mCurrXform.updateScale) 0f else mCurrPt.getMultiTouchDiameter())
        mCurrPtWidth = MIN_MULTITOUCH_SEPARATION.coerceAtLeast(if (!mCurrXform.updateScaleXY) 0f else mCurrPt.getMultiTouchWidth())
        mCurrPtHeight = MIN_MULTITOUCH_SEPARATION.coerceAtLeast(if (!mCurrXform.updateScaleXY) 0f else mCurrPt.getMultiTouchHeight())
        mCurrPtAng = if (!mCurrXform.updateAngle) 0f else mCurrPt.getMultiTouchAngle()
    }

    protected fun setHandleSingleTouchEvents(handleSingleTouchEvents: Boolean) {
        this.handleSingleTouchEvents = handleSingleTouchEvents
    }

    protected fun getHandleSingleTouchEvents(): Boolean {return handleSingleTouchEvents}

    fun onTouchEvent(event: MotionEvent): Boolean {
        try {
            val pointCount = if (multiTouchSupported) m_getPointerCount.invoke(event) as Int else 1

            if (mMode== MODE_NOTHING && !handleSingleTouchEvents && pointCount==1) return false

            val action = event.action
            val histLen = (event.historySize / pointCount).toInt()
            for (histIdx in 0..histLen) {
                val processingHist = histIdx < histLen
                if (!multiTouchSupported || pointCount==1) {
                    xVals[0] = if (processingHist) event.getHistoricalX(histIdx) else event.x
                    yVals[0] = if (processingHist) event.getHistoricalY(histIdx) else event.y
                    pressureVals[0] = if (processingHist) event.getHistoricalPressure(histIdx) else event.pressure
                } else {
                    val numPointer = pointCount.coerceAtMost(MAX_TOUCH_POINTS)
                    for (ptrIdx in 0 until numPointer) {
                        val ptrId = m_getPointerId.invoke(event, ptrIdx) as Int
                        pointerIds[ptrIdx] = ptrId

                        xVals[ptrIdx] = if (processingHist) m_getHistoricalX.invoke(event, ptrIdx, histIdx) as Float else m_getX.invoke(event, ptrIdx) as Float
                        yVals[ptrIdx] = if (processingHist) m_getHistoricalY.invoke(event, ptrIdx, histIdx) as Float else m_getY.invoke(event, ptrIdx) as Float
                        pressureVals[ptrIdx] = if (processingHist) m_getHistoricalPressure.invoke(event, ptrIdx, histIdx) as Float else m_getPressure.invoke(event, ptrIdx) as Float
                    }
                }

                decodeTouchEvent(pointCount, xVals, yVals, pressureVals, pointerIds,
                    if (processingHist) MotionEvent.ACTION_MOVE else action,
                    if (processingHist) true else action != MotionEvent.ACTION_UP &&
                            action and (1 shl ACTION_POINTER_INDEX_SHIFT) - 1 != ACTION_POINTER_UP &&
                            action != MotionEvent.ACTION_CANCEL,
                    if (processingHist) event.getHistoricalEventTime(histIdx) else event.eventTime)
            }

            return selectedObject!=null
        } catch (e: Exception) {
            return false
        }
    }

    fun decodeTouchEvent(pointerCount: Int, x: FloatArray, y: FloatArray, pressure: FloatArray, pointerIds: IntArray, action: Int, down: Boolean, eventTime: Long) {
        val tmp = mPrevPt
        mPrevPt = mCurrPt
        mCurrPt = tmp

        mCurrPt.set(pointerCount, x, y, pressure, pointerIds, action, down, eventTime)
        multiTouchController()
    }

    private fun anchorAtThisPositionAndScale() {
        if (selectedObject==null) return

        objectCanvas.getPositionAndScale(selectedObject, mCurrXform)

        val currScaleInv = 1.0f / (if (!mCurrXform.updateScale) 1.0f else if (mCurrXform.scale==0f) 1.0f else mCurrXform.scale)
        extractCurrPtInfo()
        startPosX = (mCurrPtX - mCurrXform.xOff) * currScaleInv
        startPosY = (mCurrPtY - mCurrXform.yOff) * currScaleInv
        startAngle = (mCurrPtAng + atan2(startPosY, -startPosX)).toDouble()
        startRadius = sqrt(startPosX*startPosX + startPosY*startPosY).toDouble()
        startScaleOverPinchDiam = mCurrXform.scale / mCurrPtDiam
        startScaleXOverPinchWidth = mCurrXform.scaleX / mCurrPtWidth
        startScaleYOverPinchHeight = mCurrXform.scaleY / mCurrPtHeight
        startAngleMinusPinchAngle = mCurrXform.angle - mCurrPtAng
    }

    fun performDragOrPinch() {
        if (selectedObject==null) return

        val currScale = if (!mCurrXform.updateScale) 1.0f else if (mCurrXform.scale==0.0f) 1.0f else mCurrXform.scale
        extractCurrPtInfo()

        val diffAngle = startAngle - mCurrPtAng
        val newPosX = (mCurrPtX + startRadius * currScale * cos(diffAngle)).toFloat()
        val newPosY = (mCurrPtY - startRadius * currScale * sin(diffAngle)).toFloat()

        val deltaX = mCurrPt.getX() - mPrevPt.getX()
        val deltaY = mCurrPt.getY() - mPrevPt.getY()

        val newScale = startScaleOverPinchDiam * mCurrPtDiam

        if (!mDragOccurred) {
            if (!pastThreshold(abs(deltaX), abs(deltaY), newScale)) {
                return
            }
        }

        val newScaleX = startScaleXOverPinchWidth * mCurrPtWidth
        val newScaleY = startScaleYOverPinchHeight * mCurrPtHeight
        val newAngle = startAngleMinusPinchAngle + mCurrPtAng

        mCurrXform.set(newPosX, newPosY, newScale, newScaleX, newScaleY, newAngle)

        val success = objectCanvas.setPositionAndScale(selectedObject, mCurrXform, mCurrPt)

        mDragOccurred = true
    }

    fun isPinching(): Boolean {return mMode== MODE_PINCH}

    fun pastThreshold(deltaX: Float, deltaY: Float, newScale: Float): Boolean {
        mDragOccurred = true
        if (deltaX < THRESHOLD && deltaY < THRESHOLD) {
            if (newScale==mCurrXform.scale) {
                mDragOccurred = false
            }
        }

        return mDragOccurred
    }

    fun multiTouchController() {
        when (mMode) {
            MODE_NOTHING -> {
                if (mCurrPt.isDown()) {
                    mInitPt.set(mCurrPt)

                    selectedObject = objectCanvas.getDraggableObjectAtPoint(mCurrPt)

                    if (selectedObject!=null) {
                        mMode = MODE_DRAG
                        objectCanvas.touchModeChanged(mMode, mCurrPt)
                        objectCanvas.selectObject(selectedObject, mCurrPt)
                        anchorAtThisPositionAndScale()

                        mSettleStartTime = mCurrPt.getEventTime()
                        mSettleEndTime = mSettleStartTime + EVENT_SETTLE_TIME_INTERVAL
                    }

                    if (prevSelectedObject!=selectedObject) {
                        prevSelectedObject = null
                    }
                }
            }
            MODE_DRAG -> {
                if (!mCurrPt.isDown()) {
                    mMode = MODE_NOTHING
                    objectCanvas.touchModeChanged(mMode, mCurrPt)

                    var performDoubleTab = false
                    if (prevSelectedObject!=null && prevSelectedObject==selectedObject) {
                        if (mCurrPt.getEventTime()-mLastUpPt.getEventTime() < AppConst.Config.DOUBLE_TAP_TIMEOUT) {
                            val p1 = objectCanvas.toWorldPoint(Vec2(mCurrPt.getPoint()))
                            val p2 = objectCanvas.toWorldPoint(Vec2(mLastUpPt.getPoint()))
                            val dist = p1.distance(p2)

                            if (dist>=0 && dist<AppConst.Config.SCALED_DOUBLE_TAB_SLOPE) {
                                performDoubleTab = true
                            }
                        }
                    }

                    if (performDoubleTab) {
                        prevSelectedObject = null
                        objectCanvas.doubleClickObject(selectedObject, mCurrPt)
                    }

                    prevSelectedObject = selectedObject
                    mLastUpPt.set(mCurrPt)

                    selectedObject = null
                    mDragOccurred = false
                } else if (mCurrPt.isMultiTouch()) {
                    prevSelectedObject = null
                    mMode = MODE_PINCH
                    objectCanvas.touchModeChanged(mMode, mCurrPt)

                    anchorAtThisPositionAndScale()

                    mSettleStartTime = mCurrPt.getEventTime().also { mSettleEndTime = it }
                } else {
                    if (mCurrPt.getEventTime() < mSettleEndTime) {
                        val deltaX = mCurrPt.getX() - mInitPt.getX()
                        val deltaY = mCurrPt.getY() - mInitPt.getY()

                        if (sqrt(deltaX*deltaX + deltaY*deltaY) > EVENT_SETTLE_TIME_INTERVAL) {
                            performDragOrPinch()
                            mSettleEndTime = mSettleStartTime
                        } else {
                            anchorAtThisPositionAndScale()
                        }
                    } else {
                        performDragOrPinch()
                    }
                }
            }
            MODE_PINCH -> {
                prevSelectedObject = null
                if (!mCurrPt.isMultiTouch() || !mCurrPt.isDown()) {
                    if (!mCurrPt.isDown()) {
                        mMode = MODE_NOTHING
                        objectCanvas.touchModeChanged(mMode, mCurrPt)
                        prevSelectedObject = selectedObject
                        selectedObject = null
                        objectCanvas.selectObject(selectedObject, mCurrPt)
                    } else {
                        mMode = MODE_DRAG
                        objectCanvas.touchModeChanged(mMode, mCurrPt)
                        anchorAtThisPositionAndScale()
                        mSettleStartTime = mCurrPt.getEventTime().also { mSettleEndTime = it }
                    }
                } else {
                    if (abs(mCurrPt.getX() - mPrevPt.getX()) > MAX_MULTITOUCH_POS_JUMP_SIZE
                        || abs(mCurrPt.getY() - mPrevPt.getY()) > MAX_MULTITOUCH_POS_JUMP_SIZE
                        || abs(mCurrPt.getMultiTouchWidth() - mPrevPt.getMultiTouchWidth())*0.5f > MAX_MULTITOUCH_DIM_JUMP_SIZE
                        || abs(mCurrPt.getMultiTouchHeight() - mPrevPt.getMultiTouchHeight())*0.5f > MAX_MULTITOUCH_DIM_JUMP_SIZE) {
                        anchorAtThisPositionAndScale()
                        mSettleStartTime = mCurrPt.getEventTime()
                        mSettleEndTime = mSettleStartTime + EVENT_SETTLE_TIME_INTERVAL
                    } else if (mCurrPt.eventTime < mSettleEndTime) {
                        anchorAtThisPositionAndScale()
                    } else {
                        performDragOrPinch()
                    }
                }
            }
        }
    }

    fun getMode(): Int {return mMode}

}