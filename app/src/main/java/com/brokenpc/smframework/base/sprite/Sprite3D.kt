package com.brokenpc.smframework.base.sprite

import android.opengl.GLES20
import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.DrawNode
import com.brokenpc.smframework.base.texture.Texture
import com.brokenpc.smframework.shader.ProgSprite3D
import com.brokenpc.smframework.shader.ShaderManager
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer

open class Sprite3D : DrawNode {

    protected lateinit var _indexb:ShortBuffer
    protected lateinit var _colorb:FloatBuffer
    protected lateinit var _textureb:FloatBuffer
    protected var _numTriangles:Int = 0

    constructor(director:IDirector, vertices:FloatArray, indices:ShortArray, colors:FloatArray) : super(director) {
        _contentSize.width = 1f
        _contentSize.height = 1f
        _cx = 0.5f
        _cy = 0.5f

        setProgramType(ShaderManager.ProgramType.Sprite3D)

        _drawMode = GLES20.GL_TRIANGLES
        _numVertices = vertices.size/3
        _numTriangles = vertices.size/3

        initVertexBuffer(vertices)
        initIndexBuffer(indices)
        initColorBuffer(colors)
    }
    constructor(director: IDirector, vertices: FloatArray, colors: FloatArray, texCoord:FloatArray, texture: Texture, indices: ShortArray) : super(director) {
        _contentSize.width = 1f
        _contentSize.height = 1f
        _cx = 0.5f
        _cy = 0.5f

        setProgramType(ShaderManager.ProgramType.Sprite3D)

        _drawMode = GLES20.GL_TRIANGLES
        _numVertices = vertices.size/3
        _numTriangles = vertices.size/3

        initVertexBuffer(vertices)
        initColorBuffer(colors)
        initTextureBuffer(texCoord)
        initIndexBuffer(indices)
    }

    protected fun initVertexBuffer(vertices: FloatArray) {
        _v = ByteBuffer.allocateDirect(vertices.size*4).order(ByteOrder.nativeOrder()).asFloatBuffer()
        _v!!.put(vertices)
        _v!!.position(0)
    }

    protected fun initIndexBuffer(indices: ShortArray) {
        _indexb = ByteBuffer.allocateDirect(indices.size*2).order(ByteOrder.nativeOrder()).asShortBuffer()
        _indexb.put(indices)
        _indexb.position(0)
    }

    protected fun initColorBuffer(colors: FloatArray) {
        _colorb = ByteBuffer.allocateDirect(colors.size*4).order(ByteOrder.nativeOrder()).asFloatBuffer()
        _colorb.put(colors)
        _colorb.position(0)
    }

    protected fun initTextureBuffer(texCoord: FloatArray) {
        _textureb = ByteBuffer.allocateDirect(texCoord.size*4).order(ByteOrder.nativeOrder()).asFloatBuffer()
        _textureb.put(texCoord)
        _textureb.position(0)
    }

    override fun _draw(modelMatrix: FloatArray) {
        val program = useProgram()
        if (program!=null && _texture!=null) {
            if ((program as ProgSprite3D).setDrawParam(_texture!!, _matrix, _v!!, _textureb, _colorb)) {
                if (_drawMode==GLES20.GL_TRIANGLE_STRIP) {
                    GLES20.glDrawArrays(_drawMode, 0, _numVertices)
                } else {
                    GLES20.glDrawElements(_drawMode, _numTriangles*3, GLES20.GL_UNSIGNED_SHORT, _indexb)
                }
            }
        }
    }
}