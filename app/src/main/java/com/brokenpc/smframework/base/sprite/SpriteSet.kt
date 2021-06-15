package com.brokenpc.smframework.base.sprite

import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.texture.Texture

abstract class SpriteSet(director: IDirector) {
    abstract fun getInfos(): ArrayList<SpriteInfo>
    abstract fun getTextureResIds(): IntArray
    val _director: IDirector = director
    var _sprites: ArrayList<Sprite>
    var _textures: ArrayList<Texture>

    class SpriteInfo(id: Int, tx: Int, ty: Int, w: Int, h: Int, cx: Float, cy: Float) {
        var id: Int = id
        var tx: Int = tx
        var ty: Int = ty
        var w: Int = w
        var h: Int = h
        var cx: Float = cx
        var cy: Float = cy
    }

    init {
        val numTextures = getTextureResIds().size
        _textures = ArrayList(numTextures)
        for (i in 0 until numTextures) {
            val texture = _director.getTextureManager().createTextureFromResource(getTextureResIds()[i])
            _textures.add(texture)
        }

        val infos = getInfos()
        val numSprites = infos.size
        _sprites = ArrayList(numSprites)
        for (i in 0 until numSprites) {
            val spriteInfo = infos[i]
            val sprite = Sprite(_director, (spriteInfo.w+1).toFloat(), (spriteInfo.h+1).toFloat(), spriteInfo.cx, spriteInfo.cy, spriteInfo.tx, spriteInfo.ty, _textures[spriteInfo.id])
            _sprites.add(sprite)
        }
    }

    fun getNumSprite(): Int {return getInfos().size}

    fun get(spriteId: Int): Sprite? {
        try {
            return _sprites[spriteId]
        } catch (e: IndexOutOfBoundsException) {
            return null
        }
    }
}