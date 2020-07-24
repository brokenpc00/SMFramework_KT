package com.interpark.smframework.base.texture

import android.content.Context
import android.graphics.*
import android.graphics.Paint.Align
import android.opengl.GLES20
import android.opengl.GLUtils
import android.text.Html
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.text.HtmlCompat
import com.interpark.smframework.IDirector
import com.interpark.smframework.util.TextTextureUtil
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

class TextTexture : Texture {
    private var _text: String = ""
    private var _fontSize = 0f
    private var _align: Align = Align.CENTER
    private var _bold = false
    private var _italic = false
    private var _strikeThru = false
    private var _bounds:  Rect = Rect(0, 0, 0, 0)
    private var _cx = 0f
    private var _cy = 0f
    private var _maxWidth = 0
    private var _maxLines = 0
    private var _lineCount = 0
    private var _spaceMult = 1f
    private var _isHtml = false

    private var _sLineCount:IntArray = IntArray(1)

    companion object {
        const val TEXT_PADDING:Int = 4
        const val MAX_TEXT_WIDTH:Int = 1024-(TEXT_PADDING*2)
        private var SPACING_ADD = 0.0f
        private val _textPaint = TextPaint()
        private val _textCanvas = Canvas()
        private val _lineCount:IntArray = IntArray(1)
    }

    constructor(director:IDirector, key:String, text:String, fontSize:Float, align: Paint.Align, bold:Boolean, italic:Boolean, strike:Boolean) : super(director, key, false, null) {
        _text = text
        _fontSize = fontSize
        _align = align
        _bold = bold
        _italic = italic
        _strikeThru = strike
        _maxWidth = -1
        _maxLines = 1
        _lineCount = 0
        _isHtml = false
        initTextureDimen(director.getContext())
    }

    constructor(director:IDirector, key:String, text:String, fontSize:Float, align: Paint.Align, bold:Boolean, italic:Boolean, strike:Boolean, maxWidth:Int, maxLine:Int) : super(director, key, false, null) {
        _text = text
        _fontSize = fontSize
        _align = align
        _bold = bold
        _italic = italic
        _strikeThru = strike
        _maxWidth = maxWidth
        _maxLines = maxLine
        _lineCount = 0
        _isHtml = false
        initTextureDimen(director.getContext())
    }

    constructor(director:IDirector, key:String, text:String, fontSize:Float, align: Paint.Align, bold:Boolean, italic:Boolean, strike:Boolean, isHtml:Boolean) : super(director, key, false, null) {
        _text = text
        _fontSize = fontSize
        _align = align
        _bold = bold
        _italic = italic
        _strikeThru = strike
        _maxWidth = -1
        _maxLines = 1
        _lineCount = 0
        _isHtml = isHtml
        initTextureDimen(director.getContext())
    }

    fun getBounds():Rect {return _bounds}
    fun getCX():Float {return _cx}
    fun getCY():Float {return _cy}
    fun getLineCount():Int {return _lineCount}

    override fun loadTexture(director: IDirector, bitmap: Bitmap?): Boolean {
        val bitmap:Bitmap? = loadTextureBitmap(director.getContext())
        if (bitmap!=null) {
            GLES20.glGenTextures(1, _textureId, 0)
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, _textureId[0])

            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)

            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)
            bitmap.recycle()
            return true
        }

        return false
    }

    private fun initPaint() {
        _textPaint.typeface = Typeface.DEFAULT
        _textPaint.color = 0xFFFFFFFF.toInt()
        _textPaint.style = Paint.Style.FILL
        _textPaint.isAntiAlias = false
        _textPaint.textAlign = _align
        _textPaint.textSize = _fontSize
        _textPaint.isFakeBoldText = _bold
        _textPaint.textSkewX = if (_italic) -0.2f else 0f
        _textPaint.isStrikeThruText = _strikeThru
    }

    override fun initTextureDimen(context: Context) {
        initPaint()

        if (_maxWidth > 0 && !_isHtml) {
            _text = TextTextureUtil.getDivideString(_textPaint, _text, _maxWidth, _maxLines, _sLineCount) ?: ""
            _spaceMult = if (_sLineCount[0]>1) 1.5f else 1f
        }

        val layout:StaticLayout = if (_isHtml) {
            val html = HtmlCompat.fromHtml(_text, HtmlCompat.FROM_HTML_MODE_LEGACY)

            StaticLayout.Builder.obtain(html, 0, html.length, _textPaint, MAX_TEXT_WIDTH).build()
        } else {
            StaticLayout.Builder.obtain(_text, 0, _text.length, _textPaint, MAX_TEXT_WIDTH).build()
        }

        var l:Float = Int.MAX_VALUE.toFloat()
        var t:Float = Int.MAX_VALUE.toFloat()
        var r = 0f
        var b = 0f
        val lineCount:Int = layout.lineCount
        for (i in 0 until lineCount) {
            l = min(layout.getLineLeft(i), l)
            t = min(layout.getLineTop(i).toFloat(), t)
            r = max(layout.getLineRight(i), r)
            b = max(layout.getLineBottom(i).toFloat(), b)
        }

        val ll:Int = max(floor(l).toInt(), 0)
        val tt:Int = max(floor(t).toInt(), 0)
        val rr:Int = min(ceil(r).toInt(), MAX_TEXT_WIDTH) + TEXT_PADDING*2
        val bb:Int = min(ceil(b).toInt(), MAX_TEXT_WIDTH) + TEXT_PADDING*2

        _bounds.set(ll, tt, rr, bb)

        _width = _bounds.width()
        _originalWidth = _bounds.width()
        _height = _bounds.height()
        _originalHeight = _bounds.height()

        _cx = ll + _width/2f

        val b1:Float = layout.getLineBaseline(0).toFloat()
        val b2:Float = layout.getLineBaseline(lineCount-1).toFloat()
        _cy = tt + TEXT_PADDING + b1 + (b2-b1)/2f

        _lineCount = lineCount
    }

    override fun loadTextureBitmap(context: Context): Bitmap {
        val bitmap: Bitmap = Bitmap.createBitmap(_width, _height, Bitmap.Config.ARGB_8888)

        initPaint()

        _textCanvas.setBitmap(bitmap)
        _textCanvas.save()

        when(_align) {
            Align.LEFT -> _textCanvas.translate((_bounds.left+ TEXT_PADDING).toFloat(), TEXT_PADDING.toFloat())
            Align.RIGHT -> _textCanvas.translate((_bounds.right- TEXT_PADDING).toFloat(), TEXT_PADDING.toFloat())
            else -> _textCanvas.translate(_cx, TEXT_PADDING.toFloat())
        }

        val layout:StaticLayout = if (_isHtml) {
            val html = HtmlCompat.fromHtml(_text, HtmlCompat.FROM_HTML_MODE_LEGACY)
            StaticLayout.Builder.obtain(html, 0, html.length, _textPaint, MAX_TEXT_WIDTH).build()
        } else {
            StaticLayout.Builder.obtain(_text, 0, _text.length, _textPaint, MAX_TEXT_WIDTH).build()
        }

        layout.draw(_textCanvas)
        _textCanvas.restore()

        return bitmap
    }

    fun getText():String {return _text}

    fun updateText(director: IDirector, text: String?):Boolean {
        if (text==null || text.isEmpty() || text==_text) {
            return false
        }

        _text = text

        deleteTexture(director.isGLThread())
        initTextureDimen(director.getContext())
        if (director.isGLThread()) {
            loadTexture(director, null)
        }

        return true
    }
}