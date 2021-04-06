package com.brokenpc.smframework.shader

import android.content.Context
import android.opengl.GLES20
import android.util.SparseArray
import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.util.IOUtils
import java.io.InputStream
import java.io.InputStreamReader

class ShaderManager {

    private val _shaderSet:SparseArray<Shader?> = SparseArray()
    private val _programSet:SparseArray<ShaderProgram?> = SparseArray()
    private var _activeProgram:ShaderProgram? = null

    companion object {
        var SOURCE_FILE:Int = 0
        var SOURCE_STRING:Int = 1

        @JvmStatic
        fun loadShader(shaderSource:String, shaderType:Int):Int {
            val compiled:IntArray = IntArray(1)
            val shaderId:Int = GLES20.glCreateShader(shaderType)
            GLES20.glShaderSource(shaderId, shaderSource)
            GLES20.glCompileShader(shaderId)
            GLES20.glGetShaderiv(shaderId, GLES20.GL_COMPILE_STATUS, compiled, 0)

            if (compiled[0]==0) {
                return 0
            }

            return shaderId
        }

        @JvmStatic
        fun loadShaderSource(context: Context, fileName:String): String {
            var source:StringBuffer? = null
            var inputStream:InputStream? = null
            var isr:InputStreamReader? = null

            try {
                val BUFFER_SIZE:Int = 1024
                val buffer:CharArray = CharArray(BUFFER_SIZE)
                inputStream = context.assets.open(fileName)
                isr = InputStreamReader(inputStream)
                source = StringBuffer()

                var readCount:Int = 0
                while ((isr.read(buffer, 0, BUFFER_SIZE).also { readCount=it }) != -1) {
                    source.append(buffer, 0, readCount)
                }
            } catch (e:Exception) {
                return ""
            } finally {
                IOUtils.closeSilently(inputStream)
                IOUtils.closeSilently(isr)
            }

            return source.toString()
        }
    }

    public enum class ProgramType(key:Int, vertextShaderSource:String, fragmentShaderSource: String, shaderProgram: ShaderProgram) {
        Sprite(1, "glsl/sprite.vsh", "glsl/sprite.fsh", ProgSprite()),
        SpriteCircle(2, "glsl/sprite.vsh", "glsl/sprite_circle.fsh", ProgSpriteCircle()),
        Primitive(3, "glsl/primitive.vsh", "glsl/primitive.fsh", ProgPrimitive()),
        PrimitiveCircle(4, "glsl/sprite.vsh", "glsl/primitive_circle.fsh", ProgPrimitiveCircle()),
        PrimitiveRing(5, "glsl/sprite.vsh", "glsl/primitive_ring.fsh", ProgPrimitiveRing()),
        PrimitiveSolidRect(6, "glsl/sprite.vsh", "glsl/primitive_solidrect.fsh", ProgPrimitiveSolidRect()),
        PrimitiveTriangle(7, "glsl/sprite.vsh", "glsl/primitive_triangle.fsh", ProgPrimitiveTriangle()),
        Sprite3D(8, "glsl/sprite3d.vsh", "glsl/sprite3d.fsh", ProgSprite3D()),
        CameraPreview(9, "glsl/sprite.vsh", "glsl/yuv2rgb.fsh", ProgCameraPreview()),
        Bilateral(10, "glsl/bilateral_filter.vsh", "glsl/bilateral_filter.fsh", ProgBilateralFilter()),
        GaussianBlur(11, "glsl/gaussian_blur.vsh", "glsl/gaussian_blur.fsh", ProgGaussianBlur()),
        EdgeGlow(12, "glsl/sprite.vsh", "glsl/edge_glow.fsh", ProgEdgeGlow()),
        GeineEffect(13, "glsl/geine_effect.vsh", "glsl/sprite.fsh", ProgGeineEffect()),
        GeineEffect2(14, "glsl/sprite.vsh", "glsl/geine_effect.fsh", ProgGeineEffect2()),
        AdjustColor(15, "glsl/sprite.vsh", "glsl/adjust_color.fsh", ProgAdjustColor()),
        RadialAlpha(16, "glsl/sprite.vsh", "glsl/radial_alpha.fsh", ProgRadialAlpha()),
        ShapeSurface(17, "glsl/sprite.vsh", "glsl/shape_surface.fsh", ProgPrimitiveSolidRect()),
        FishEyeCircle(18, "glsl/sprite.vsh", "glsl/fisheye_circle.fsh", ProgSprite());


        private val _key:Int = key
        private val _vertexShaderSource:String = vertextShaderSource
        private val _fragmentShaderSource:String = fragmentShaderSource
        private val _shaderProgram:ShaderProgram = shaderProgram
        private val _vertexShaderKey:Int = vertextShaderSource.hashCode()
        private val _fragmentShaderKey:Int = fragmentShaderSource.hashCode()


        fun getKey():Int {return _key}
        fun getVertexShaderKey():Int {return _vertexShaderKey}
        fun getFragmentShaderKey():Int {return _fragmentShaderKey}
        fun getVertexShaderSource():String {return _vertexShaderSource}
        fun getFragmentShaderSource():String {return _fragmentShaderSource}
        fun getShaderProgram():ShaderProgram {return _shaderProgram}
    }

    fun getActiveProgram():ShaderProgram? {return _activeProgram}

    fun useProgram(director:IDirector, type:ProgramType): ShaderProgram? {
        if (_activeProgram!=null && _activeProgram!!.getType()==type) return _activeProgram

        var program:ShaderProgram? = _programSet.get(type.getKey())
        if (program==null) {
            program = loadProgram(director, type)
            if (program!=null) {
                _programSet.put(type.getKey(), program)
                program?.complete()
            }
        }
        _activeProgram = program

        return program
    }

    fun setMatrix(matrix:FloatArray?) {
        if (_activeProgram!=null && matrix!=null) {
            _activeProgram!!.setMatrix(matrix)
        }
    }

    fun release(director: IDirector) {
        _activeProgram = null

        var key:Int = 0

        if (director.isGLThread()) {
            for (i in 0 until  _programSet.size()) {
                key = _programSet.keyAt(i)
                val program:ShaderProgram? = _programSet.get(key)
                program?.delete()
            }
        }
        _programSet.clear()

        if (director.isGLThread()) {
            for (i in 0 until _shaderSet.size()) {
                key = _shaderSet.keyAt(i)
                val shader:Shader? = _shaderSet.get(key)
                if (shader!=null) {
                    GLES20.glDeleteShader(shader.getId())
                }
            }
        }
        _shaderSet.clear()
    }

    private fun loadProgram(director: IDirector, type: ProgramType): ShaderProgram? {
        var vertexShaderId:Int = -1
        var fragmentShaderId:Int = -1

        var vertexShader:Shader? = _shaderSet.get(type.getVertexShaderKey())
        if (vertexShader!=null) {
            vertexShaderId = vertexShader.getId()
        } else {
            val source:String = loadShaderSource(director.getContext(), type.getVertexShaderSource())
            vertexShaderId = loadShader(source, GLES20.GL_VERTEX_SHADER)
            if (vertexShaderId==0) {
                return null
            }
            vertexShader = Shader(vertexShaderId)
        }

        var fragmentShader:Shader? = _shaderSet.get(type.getFragmentShaderKey())
        if (fragmentShader!=null) {
            fragmentShaderId = fragmentShader.getId()
        } else {
            val source:String = loadShaderSource(director.getContext(), type.getFragmentShaderSource())
            fragmentShaderId = loadShader(source, GLES20.GL_FRAGMENT_SHADER)
            if (fragmentShaderId==0) {
                return null
            }
            fragmentShader = Shader(fragmentShaderId)
        }

        val programId:Int = GLES20.glCreateProgram()
        if (programId!=0) {
            val program:ShaderProgram = type.getShaderProgram()
            program.linkInterface(director, type, programId)

            GLES20.glAttachShader(programId, vertexShader.getId())
            GLES20.glAttachShader(programId, fragmentShader.getId())
            GLES20.glLinkProgram(programId)

            val link:IntArray = IntArray(1)
            GLES20.glGetProgramiv(programId, GLES20.GL_LINK_STATUS, link, 0)
            if (link[0]<=0) {
                if (vertexShader.getRefCount()<=0) {
                    GLES20.glDeleteShader(vertexShader.getId())
                }
                if (fragmentShader.getRefCount()<=0) {
                    GLES20.glDeleteShader(fragmentShader.getId())
                }

                GLES20.glDeleteProgram(programId)

                return null
            }

            // put into shader cache
            vertexShader.incRef()
            fragmentShader.incRef()
            _shaderSet.put(type.getVertexShaderKey(), vertexShader)
            _shaderSet.put(type.getFragmentShaderKey(), fragmentShader)

            return program
        }

        return null
    }


}