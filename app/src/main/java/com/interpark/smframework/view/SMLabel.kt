package com.brokenpc.smframework.view

import android.graphics.Paint.Align
import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.SMView
import com.brokenpc.smframework.base.UIContainerView
import com.brokenpc.smframework.base.sprite.TextSprite
import com.brokenpc.smframework.base.types.Color4F
import com.brokenpc.smframework.base.types.Mat4
import com.brokenpc.smframework.base.types.Vec2

class SMLabel(director:IDirector) : UIContainerView(director) {
    private var _textSprite:TextSprite? = null
    private var _fontSize:Float = 0f
    private var _text:String = ""
    private var _letterConainerView:SMView? = null
    private var _letters:ArrayList<SMLabel>? = null
    private var _align:Align = Align.CENTER
    private var _bold:Boolean = false
    private var _italic:Boolean = false
    private var _strike:Boolean = false
    private var _maxWidth = -1
    private var _maxLines = -1


    companion object {
        @JvmStatic
        fun create(director: IDirector, text: String, fontSize: Float): SMLabel {
            return create(director, text, fontSize, Color4F.BLACK)
        }

        @JvmStatic
        fun create(director: IDirector, text: String, fontSize: Float, fontColor: Color4F): SMLabel {
            return create(director, text, fontSize, fontColor, Align.CENTER)
        }

        @JvmStatic
        fun create(director: IDirector, text: String, fontSize: Float, fontColor: Color4F, align: Align): SMLabel {
            return create(director, text, fontSize, fontColor, align, false, false, false)
        }

        @JvmStatic
        fun create(director: IDirector, text: String, fontSize: Float, fontColor: Color4F, align: Align, bold: Boolean, italic: Boolean, strike: Boolean): SMLabel {
            return create(director, text, fontSize, fontColor, align, bold, italic, strike, -1, 1)
        }

        @JvmStatic
        fun create(director: IDirector, text: String, fontSize: Float, fontColor: Color4F, align: Align, bold: Boolean, italic: Boolean, strike: Boolean, maxWidth: Int, maxLines: Int): SMLabel {
            val label = SMLabel(director)
            label.initWithFont(text, fontSize, fontColor, align, bold, italic, strike, maxWidth, maxLines)
            return label
        }
    }


    fun initWithFont(text:String, fontSize:Float, fontColor:Color4F, align:Align, bold:Boolean, italic:Boolean, strike:Boolean, maxWidth:Int, maxLines:Int) {
        _text = text
        _fontSize = fontSize
        setColor(fontColor)
        _align = align
        _italic = italic
        _strike = strike
        _maxWidth = maxWidth
        _maxLines = maxLines

        makeTextSprite()
    }

    private fun makeTextSprite() {
        releaseGLResources()

        _textSprite = TextSprite.Companion.createTextSprite(getDirector(), _text, _fontSize, _align, _bold, _italic, _strike, _maxWidth, _maxLines)
        setContentSize(_textSprite!!.getWidth(), _textSprite!!.getHeight())
    }

    fun setBold(bold: Boolean) {
        if (_bold==bold) return
        _bold = bold
        makeTextSprite()
    }

    fun setItalic(italic: Boolean) {
        if (_italic==italic) return
        _italic = italic
        makeTextSprite()
    }

    fun setStrike(strike: Boolean) {
        if (_strike==strike) return
        _strike = strike
        makeTextSprite()
    }

    fun setMaxWidth(maxWidth: Int) {
        if (_maxWidth==maxWidth) return
        _maxWidth = maxWidth
        makeTextSprite()
    }

    fun setMaxLines(maxLines: Int) {
        if (_maxLines==maxLines) return
        _maxLines = maxLines
        makeTextSprite()
    }

    fun clearSeparate() {
        if (_letterConainerView!=null) {
            if (_letters!=null) {
                for (label in _letters!!) {
                    label.removeFromParent()
                }
                _letters!!.clear()
                _letters = null
            }

            _letterConainerView!!.removeAllChildren()
            _letterConainerView!!.removeFromParent()
            _letterConainerView = null
        }
    }

    fun makeSeparate() {
        clearSeparate()

        val len = _text.length
        if (len==0) return

        _letterConainerView = create(getDirector())
        _letterConainerView!!.setAnchorPoint(0f, 0f)
        _letterConainerView!!.setPosition(0f, 0f)
        _letterConainerView!!.setContentSize(_contentSize)
        addChild(_letterConainerView!!)

        _letters = ArrayList(len)
        val padding = 4f
        var posX = padding
        for (i in 0 until len) {
            val str:String = _text.substring(i, i+1)
            val letter:SMLabel = SMLabel.create(getDirector(), str, _fontSize, Color4F(_displayedColor))
            letter.setAnchorPoint(Vec2.MIDDLE)
            val letterSize = letter.getContentSize().width - padding*2
            posX += letterSize/2f
            letter.setPosition(posX, _letterConainerView!!.getContentSize().height/2f)
            _letterConainerView!!.addChild(letter)
            posX += letterSize/2f
            _letters!!.add(letter)
        }
    }

    fun getStringLength():Int {return _text.length}

    fun getText():String {return _text}

    fun getSeparateCount():Int {
        return _letters?.size ?: 0
    }

    fun setText(text: String) {
        if (_text==text) return
        _text = text
        makeTextSprite()
    }

    fun getLetter(index:Int):SMLabel? {
        if (_letters==null || index>_letters!!.size-1 || index<0) {
            return null
        }

        return _letters!![index]
    }

    fun setFontColor(color:Color4F) {setColor(color)}

    override fun releaseGLResources() {
        if (_textSprite!=null) {
            _textSprite!!.releaseResources()
            _textSprite = null
        }
    }

    fun testBgColor(color:Color4F) {super.setBackgroundColor(color)}

    override fun setBackgroundColor(color: Color4F) {
        setColor(color)
    }

    override fun draw(m: Mat4, flags: Int) {
        if (_letters!=null && _letters!!.size>0) return

        if (_textSprite==null) return

        setRenderColor()

        _textSprite!!.draw(_textSprite!!.getWidth()/2f, _textSprite!!.getHeight()/2f)
    }
}