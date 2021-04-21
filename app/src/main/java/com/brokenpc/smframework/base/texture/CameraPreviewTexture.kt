package com.brokenpc.smframework.base.texture

import android.content.Context
import android.graphics.Bitmap
import android.opengl.GLES20
import android.opengl.Matrix
import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.types.Mat4
import java.nio.ByteBuffer

class CameraPreviewTexture(director:IDirector, key: String, width: Int, height: Int) : Texture(director, key, false, null) {
    private var _renderTargetEnable:Boolean = false
    private var _utilMatrix:FloatArray = FloatArray(16)
    private var _frameBufferId:IntArray? = null
    private var _YTextureId:IntArray? = null
    private var _UVTextureID:IntArray? = null
    private var _storedFrameBufferId:Int = 0

    init {
        _width = width
        _height = height
        _originalWidth = width
        _originalHeight = height
    }

    fun onResume() {
        if (_frameBufferId!=null) {
            _frameBufferId!![0] = NO_TEXTURE
        }

        if (_YTextureId!=null) {
            _YTextureId!![0] = NO_TEXTURE
        }
        if (_UVTextureID!=null) {
            _UVTextureID!![0] = NO_TEXTURE
        }
    }

    fun setRenderTarget(turnOn:Boolean):Boolean {
        _renderTargetEnable = true
        if (turnOn) {
            _storedFrameBufferId = _director.getFrameBufferId()
            if (_frameBufferId==null) {
                _frameBufferId = IntArray(1)
                _frameBufferId!![0] = NO_TEXTURE
            }
            if (_frameBufferId!![0]== NO_TEXTURE) {
                GLES20.glGenFramebuffers(1, _frameBufferId, 0)
            }

            if (_textureId[0]== NO_TEXTURE) {
                loadTexture(_director, null)
            }

            if (_director.bindTexture(this)) {
                GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, _frameBufferId!![0])
                GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, _textureId[0], 0)

                GLES20.glViewport(0, 0, _width, _height)

                Matrix.orthoM(_utilMatrix, 0, 0f, _width.toFloat(), _height.toFloat(), 0f, -1000f, 1000f)
                Matrix.translateM(_utilMatrix, 0, 0f, _height.toFloat(), 0f)
                Matrix.scaleM(_utilMatrix, 0, 1f, -1f, 1f)

                _director.pushMatrix(IDirector.MATRIX_STACK_TYPE.MATRIX_STACK_MODELVIEW)
                _director.loadMatrix(IDirector.MATRIX_STACK_TYPE.MATRIX_STACK_MODELVIEW, Mat4(_utilMatrix))

                _renderTargetEnable = true
            }
        } else {
            if (_renderTargetEnable) {
                _renderTargetEnable = false
                GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, _storedFrameBufferId)
                _director.getTextureManager().bindTexture(null)

                GLES20.glViewport(0, 0, _director.getWidth(), _director.getHeight())
                _director.popMatrix(IDirector.MATRIX_STACK_TYPE.MATRIX_STACK_MODELVIEW)
            }
        }

        return _renderTargetEnable
    }

    override fun deleteTexture(isGLThread: Boolean) {
        super.deleteTexture(isGLThread)

        if (_frameBufferId!=null) {
            if (_frameBufferId!![0] != NO_TEXTURE) {
                if (isGLThread) {
                    GLES20.glDeleteFramebuffers(1, _frameBufferId, 0)
                }
            }
            _frameBufferId = null
        }

        if (_YTextureId!=null) {
            if (_YTextureId!![0] != NO_TEXTURE) {
                if (isGLThread) {
                    GLES20.glDeleteFramebuffers(1, _YTextureId, 0)
                }
            }
            _YTextureId = null
        }

        if (_UVTextureID!=null) {
            if (_UVTextureID!![0] != NO_TEXTURE) {
                if (isGLThread) {
                    GLES20.glDeleteFramebuffers(1, _UVTextureID, 0)
                }
            }
        }
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

    fun setupPreviewBuffer(prevWidth:Int, prevHeight:Int, yBuffer:ByteBuffer, uvBuffer:ByteBuffer) {
        if (_YTextureId==null) {
            _YTextureId = IntArray(1)
            _YTextureId!![0] = NO_TEXTURE
        }
        if (_UVTextureID==null) {
            _UVTextureID = IntArray(1)
            _UVTextureID!![0] = NO_TEXTURE
        }

        if (_YTextureId!![0] == NO_TEXTURE) {
            GLES20.glGenTextures(1, _YTextureId, 0)
        }
        if (_UVTextureID!![0] == NO_TEXTURE) {
            GLES20.glGenTextures(1, _UVTextureID, 0)
        }

        // Y TEXTURE
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, _YTextureId!![0])
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_LUMINANCE, prevWidth, prevHeight, 0, GLES20.GL_LUMINANCE, GLES20.GL_UNSIGNED_BYTE, yBuffer)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)

        // UV TEXTURE
        GLES20.glActiveTexture(GLES20.GL_TEXTURE1)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, _UVTextureID!![0])
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_LUMINANCE_ALPHA, prevWidth/2, prevHeight/2, 0, GLES20.GL_LUMINANCE_ALPHA, GLES20.GL_UNSIGNED_BYTE, uvBuffer)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)
    }
}