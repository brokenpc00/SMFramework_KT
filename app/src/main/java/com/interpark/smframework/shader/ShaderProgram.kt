package com.brokenpc.smframework.shader

import android.opengl.GLES20
import com.brokenpc.smframework.IDirector
import java.sql.Ref

abstract class ShaderProgram {

    protected lateinit var _type:ShaderManager.ProgramType
    protected var _programId:Int = -1
    protected var _vertexShaderId:Int = -1
    protected var _fragmentShaderId:Int = -1
    protected var _director:IDirector? = null

    fun getProgramId():Int {return _programId}
    fun getType():ShaderManager.ProgramType {return _type}

    fun linkInterface(director:IDirector, type: ShaderManager.ProgramType, programId: Int) {
        _director = director
        _type = type
        _programId = programId
    }

    fun delete() {
        GLES20.glDeleteProgram(_programId)
    }

    open fun bind() {
        GLES20.glUseProgram(_programId)
    }

    abstract fun complete()
    abstract fun unbind()
    abstract fun setMatrix(matrix: FloatArray)
}