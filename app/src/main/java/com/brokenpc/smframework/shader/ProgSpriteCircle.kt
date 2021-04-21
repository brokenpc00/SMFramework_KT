package com.brokenpc.smframework.shader

import android.opengl.GLES20
import com.brokenpc.smframework.base.texture.Texture
import java.nio.FloatBuffer

open class ProgSpriteCircle : ProgSprite() {
    private var _uniformTextureCenter:Int = -1
    private var _uniformAspectRatio:Int = -1
    private var _uniformRadius:Int = -1
    private var _uniformAAWidth:Int = -1

    companion object {
        const val NAME_RADIUS:String = "radius"
        const val NAME_AAWIDTH:String = "aaWidth"
        const val NAME_ASPECT_RATIO:String = "aspectRatio"
        const val NAME_TEXTURE_CENTER:String = "textureCenter"

    }

    override fun complete() {
        super.complete()
        _uniformTextureCenter = GLES20.glGetUniformLocation(_programId, NAME_TEXTURE_CENTER)
        _uniformAspectRatio = GLES20.glGetUniformLocation(_programId, NAME_ASPECT_RATIO)
        _uniformRadius = GLES20.glGetUniformLocation(_programId, NAME_RADIUS)
        _uniformAAWidth = GLES20.glGetUniformLocation(_programId, NAME_AAWIDTH)
    }

    fun setDrawParam(texture: Texture, modelMatrix: FloatArray, v: FloatBuffer, uv:FloatBuffer, cx:Float, cy:Float, radius:Float, aaWidth:Float): Boolean {
        if (super.setDrawParam(texture, modelMatrix, v, uv)) {
            GLES20.glUniform2f(_uniformTextureCenter, cx, cy)
            if (texture.getWidth() < texture.getHeight()) {
                GLES20.glUniform2f(_uniformAspectRatio, (texture.getWidth()/texture.getHeight()).toFloat(), 1f)
            } else {
                GLES20.glUniform2f(_uniformAspectRatio, 1f, (texture.getHeight()/texture.getWidth()).toFloat())
            }

            GLES20.glUniform1f(_uniformRadius, radius)
            GLES20.glUniform1f(_uniformAAWidth, aaWidth)
            return true
        }

        return false
    }
}