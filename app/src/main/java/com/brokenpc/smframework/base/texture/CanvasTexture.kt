package com.brokenpc.smframework.base.texture

import android.content.Context
import android.graphics.Bitmap
import android.opengl.GLES20
import android.opengl.Matrix
import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.types.Mat4

class CanvasTexture(director: IDirector, key:String, width:Int, height:Int) : Texture(director, key, false, null) {

    private var _renderTargetEnabled:Boolean = false

    private var _storedFrameBufferId:Int = 0

    companion object {
        private var _matrix:FloatArray = FloatArray(16)
    }

    init {
        _width = width.also { _originalWidth = it }
        _height = height.also { _originalHeight = it }
    }

    fun setRenderTarget(director: IDirector, turnOn:Boolean):Boolean {
        _renderTargetEnabled = true

        if (turnOn) {
            _storedFrameBufferId = director.getFrameBufferId()

            if (director.bindTexture(this)) {
                if (_textureId[0]== NO_TEXTURE) {
                    loadTexture(director, null)
                }
                GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, getId())
                GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, _textureId[0], 0)

                GLES20.glViewport(0, 0, _width, _height)

                val matrix:FloatArray = setProjectionMatrix()

                director.pushMatrix(IDirector.MATRIX_STACK_TYPE.MATRIX_STACK_MODELVIEW)
                director.loadMatrix(IDirector.MATRIX_STACK_TYPE.MATRIX_STACK_MODELVIEW, Mat4(matrix))

                _renderTargetEnabled = true
            }
        } else {
            if (_renderTargetEnabled) {
                _renderTargetEnabled = false
                director.setFrameBufferId(_storedFrameBufferId)
                GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, _storedFrameBufferId)

                director.getTextureManager().bindTexture(null)

                GLES20.glViewport(0, 0, director.getWidth(), director.getHeight())
                director.popMatrix(IDirector.MATRIX_STACK_TYPE.MATRIX_STACK_MODELVIEW)

            }
        }

        return _renderTargetEnabled
    }

    private fun setProjectionMatrix():FloatArray {
        Matrix.orthoM(_matrix, 0, 0f, _width.toFloat(), _height.toFloat(), 0f, -1000f, 1000f)
        Matrix.translateM(_matrix, 0, 0f, _height.toFloat(), 0f)
        Matrix.scaleM(_matrix, 0, 1f, -1f, 1f)

        return _matrix
    }

    override fun loadTexture(director: IDirector, bitmap: Bitmap?): Boolean {
        GLES20.glGenTextures(1, _textureId, 0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, _textureId[0])

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)

        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, _width, _height, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null)

        return true
    }

    override fun initTextureDimen(context: Context) {

    }

    override fun loadTextureBitmap(context: Context): Bitmap? {
        return null
    }

    fun isRenderTargerEnables():Boolean {return _renderTargetEnabled}

    fun clear() {
        if (_renderTargetEnabled) {
            GLES20.glClearColor(0f, 0f, 0f, 0f)
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        }
    }

    fun setFrameBuffer(director: IDirector, turnOn: Boolean):Boolean {
        if (turnOn) {
            if (director.bindTexture(this)) {
                if (_textureId[0]== NO_TEXTURE) {
                    loadTexture(director, null)
                }

                GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, getId())
                GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, _textureId[0], 0)

                GLES20.glViewport(0, 0, _width, _height)
                director.loadMatrix(IDirector.MATRIX_STACK_TYPE.MATRIX_STACK_MODELVIEW, director.getFrameBufferMatrix())

                _renderTargetEnabled = true
            }
        } else {
            if (_renderTargetEnabled) {
                _renderTargetEnabled = false
                GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0)
            }
        }

        return _renderTargetEnabled
    }
}