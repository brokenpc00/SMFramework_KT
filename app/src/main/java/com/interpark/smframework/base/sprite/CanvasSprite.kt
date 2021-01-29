package com.brokenpc.smframework.base.sprite

import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.texture.CanvasTexture
import com.brokenpc.smframework.base.texture.Texture

class CanvasSprite(director:IDirector, texture: Texture) : Sprite(director, texture.getWidth().toFloat(), texture.getHeight().toFloat(), 0f, 0f, 0, 0, texture) {
    companion object {
        @JvmStatic
        fun createCanvasSprite(director: IDirector, width:Int, height:Int, keyName:String): CanvasSprite {
            return CanvasSprite(director, director.getTextureManager().createCanvasTexture(width, height, keyName))
        }
    }

    fun setRenderTarget(director: IDirector, turnOn:Boolean):Boolean {
        return (getTexture() as CanvasTexture).setRenderTarget(director, turnOn)
    }

    fun clear() {(getTexture() as CanvasTexture).clear()}
}