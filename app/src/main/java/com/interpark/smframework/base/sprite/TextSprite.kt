package com.interpark.smframework.base.sprite

import android.graphics.Paint
import com.interpark.smframework.IDirector
import com.interpark.smframework.base.texture.TextTexture

class TextSprite(director:IDirector, texture:TextTexture, cx:Float, cy:Float) : Sprite(director, texture.getBounds().width().toFloat(), texture.getBounds().height().toFloat(), texture.getBounds().width()/2f, texture.getBounds().height()/2f, texture.getBounds().left, texture.getBounds().top, texture) {
    companion object {
        @JvmStatic
        fun createTextSprite(director: IDirector, text: String, fontSize: Float): TextSprite {
            return createTextSprite(director, text, fontSize, Paint.Align.CENTER, false, false, false)
        }

        @JvmStatic
        fun createTextSprite(director: IDirector, text: String, fontSize: Float, align: Paint.Align, bold: Boolean, italic: Boolean, strikeThru: Boolean): TextSprite {
            return createTextSprite(director, text, fontSize, align, bold, italic, strikeThru, -1, 1)
        }

        @JvmStatic
        fun createTextSprite(director: IDirector, text: String, fontSize: Float, align:Paint.Align, bold: Boolean, italic: Boolean, strikeThru: Boolean, maxWidth:Int, maxLines:Int):TextSprite {
            val texture:TextTexture? = director.getTextureManager().createTextureFromString(text, fontSize, align, bold, italic, strikeThru, maxWidth, maxLines) as TextTexture
            return TextSprite(director, texture!!, texture.getWidth()/2f, texture.getHeight()/2f)
        }

        @JvmStatic
        fun createHtmlSprite(director: IDirector, text: String, fontSize: Float, bold: Boolean, italic: Boolean, strikeThru: Boolean): TextSprite {
            val texture:TextTexture? = director.getTextureManager().createTextureFromHtmlString(text, fontSize, bold, italic, strikeThru) as TextTexture
            return TextSprite(director, texture!!, texture.getWidth()/2f, texture.getHeight()/2f)
        }
    }

    fun getText():String {
        var retText:String = ""
        val texture:TextTexture? = getTexture() as TextTexture
        if (texture!=null) {
            retText = texture.getText()
        } else {
            retText = ""
        }

        return retText
    }

    fun setText(text:String) {
        if ((getTexture() as TextTexture).updateText(_director!!, text)) {
            val texture: TextTexture = getTexture() as TextTexture

            this._tx = texture.getBounds().left.toFloat()
            this._ty = texture.getBounds().top.toFloat()
            this._tw = texture.getWidth().toFloat()
            this._th = texture.getHeight().toFloat()

            initRect(_director!!, texture.getBounds().width().toFloat(), texture.getBounds().height().toFloat(), texture.getBounds().width()/2f, texture.getBounds().height()/2f)
        }
    }

    fun getLineCount():Int {return (getTexture() as TextTexture).getLineCount()}
}