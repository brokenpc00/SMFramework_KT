package com.brokenpc.smframework.shader

import android.opengl.GLES20
import com.brokenpc.smframework.base.texture.Texture
import java.nio.FloatBuffer

class ProgAdjustColor : ProgSprite() {
    private var _uniformBrightness = 0
    private var _uniformContrast = 0
    private var _uniformSaturate = 0
    private var _uniformTemperature = 0

    companion object {
        const val NAME_BRIGHTNESS = "brightness"
        const val NAME_CONTRAST = "contrast"
        const val NAME_SATURATE = "saturate"
        const val NAME_TEMPRATURE = "temperature"
    }

    override fun complete() {
        super.complete()
        _uniformBrightness = GLES20.glGetUniformLocation(_programId, NAME_BRIGHTNESS)
        _uniformContrast = GLES20.glGetUniformLocation(_programId, NAME_CONTRAST)
        _uniformSaturate = GLES20.glGetUniformLocation(_programId, NAME_SATURATE)
        _uniformTemperature = GLES20.glGetUniformLocation(_programId, NAME_TEMPRATURE)
    }

    fun setDrawParam(texture: Texture, modelMatrix: FloatArray, v: FloatBuffer, uv: FloatBuffer, brightness: Float, contrast: Float, saturate: Float, temperaturet: Float): Boolean {
        var temperature = temperaturet
        if (super.setDrawParam(texture, modelMatrix, v, uv)) {
            temperature = if (temperature < 5000f) 0.0004f * (temperature - 5000.0f) else 0.00006f * (temperature - 5000.0f)
            GLES20.glUniform1f(_uniformBrightness, brightness)
            GLES20.glUniform1f(_uniformContrast, contrast)
            GLES20.glUniform1f(_uniformSaturate, saturate)
            GLES20.glUniform1f(_uniformTemperature, temperature)
            return true
        }
        return false
    }
}