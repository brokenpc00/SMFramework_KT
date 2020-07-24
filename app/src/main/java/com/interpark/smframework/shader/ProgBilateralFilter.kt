package com.interpark.smframework.shader

import android.opengl.GLES20
import com.interpark.smframework.base.texture.Texture
import java.nio.FloatBuffer

class ProgBilateralFilter : ProgSprite() {
    private var _uniformDistanceNormalizationFactor = 0
    private var _uniformTexelWidth = 0
    private var _uniformTexelHeight = 0
    private var _uniformResolution = 0
    private var _uniformCenter = 0
    private var _uniformRadius = 0
    private var _uniformBorder = 0
    private var _programIndex = 0
    private var _texelSpacingMultiplier = DEFAULT_TEXEL_SPACING_MULTIPLIER
    private var _distanceNotmalizeFactor = DEFAULT_DISTANCE_NORMALIZE_FACTOR
    private var _faceX = 0f
    private var _faceY = 0f
    private var _faceWidth = 0f
    private var _faceHeight = 0f

    companion object {
        const val DEFAULT_TEXEL_SPACING_MULTIPLIER = 3f
        const val DEFAULT_DISTANCE_NORMALIZE_FACTOR = 6f

        const val NAME_DISTANCE_NORMALIZE_FACTOR = "distanceNormalizationFactor"
        const val NAME_TEXEL_WIDTH = "texelWidthOffset"
        const val NAME_TEXEL_HEIGHT = "texelHeightOffset"
        const val NAME_RESOLUTION = "u_resolution"
        const val NAME_CENTER = "u_center"
        const val NAME_RADIUS = "u_radius"
        const val NAME_BORDER = "u_aaWidth"
    }

    override fun complete() {
        super.complete()
        _uniformDistanceNormalizationFactor = GLES20.glGetUniformLocation(_programId, NAME_DISTANCE_NORMALIZE_FACTOR)
        _uniformTexelWidth = GLES20.glGetUniformLocation(_programId, NAME_TEXEL_WIDTH)
        _uniformTexelHeight = GLES20.glGetUniformLocation(_programId, NAME_TEXEL_HEIGHT)
        _uniformResolution = GLES20.glGetUniformLocation(_programId, NAME_RESOLUTION)
        _uniformCenter = GLES20.glGetUniformLocation(_programId, NAME_CENTER)
        _uniformRadius = GLES20.glGetUniformLocation(_programId, NAME_RADIUS)
        _uniformBorder = GLES20.glGetUniformLocation(_programId, NAME_BORDER)
    }

    override fun setDrawParam(texture: Texture, modelMatrix: FloatArray, v: FloatBuffer, uv: FloatBuffer): Boolean {
        if (super.setDrawParam(texture, modelMatrix, v, uv)) {
            if (_programIndex == 0) {
                GLES20.glUniform1f(_uniformTexelWidth, 0f)
                GLES20.glUniform1f(_uniformTexelHeight, _texelSpacingMultiplier / texture.getHeight()
                )
            } else {
                GLES20.glUniform1f(_uniformTexelWidth, _texelSpacingMultiplier / texture.getWidth())
                GLES20.glUniform1f(_uniformTexelHeight, 0f)
            }
            GLES20.glUniform1f(_uniformDistanceNormalizationFactor, _distanceNotmalizeFactor)
            GLES20.glUniform2f(_uniformResolution, texture.getWidth().toFloat(), texture.getHeight().toFloat())
            GLES20.glUniform2f(_uniformCenter, _faceX + texture.getWidth() / 2, _faceY + texture.getHeight() / 2)
            GLES20.glUniform2f(_uniformRadius, _faceWidth, _faceHeight * 1.2f)
            GLES20.glUniform1f(_uniformBorder, _faceWidth / 2f)
            return true
        }
        return false
    }

    fun setProgramIndex(index: Int) {
        _programIndex = index
    }

    fun setTexelSpacingMultiplier(texelSpacingMultiplier: Float) {
        _texelSpacingMultiplier = texelSpacingMultiplier
    }

    fun setDistanceNotmalizeFactor(distanceNotmalizeFactor: Float) {
        _distanceNotmalizeFactor = distanceNotmalizeFactor
    }

    fun setFaceDetectionValue(faceX: Float, faceY: Float, faceWidth: Float, faceHeight: Float) {
        _faceX = faceX
        _faceY = faceY
        _faceWidth = faceWidth
        _faceHeight = faceHeight
    }
}