package com.brokenpc.smframework.shader

import android.opengl.GLES20

class BlendFunc(src:Int, dst:Int) {
    var src:Int
    var dst:Int
    init {
        this.src = src
        this.dst = dst
    }

    fun equal(a: BlendFunc): Boolean {
        return src == a.src && dst == a.dst
    }

    fun notequal(a: BlendFunc): Boolean {
        return src != a.src || dst != a.dst
    }

    fun greaterthan(a: BlendFunc): Boolean {
        return src < a.src || src == a.src && dst < a.dst
    }

    companion object {
        val DISABLE = BlendFunc(GLES20.GL_ONE, GLES20.GL_ZERO)
        val ALPHA_PREMULTIPLIED = BlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA)
        val ALPHA_NON_PREMULTIPLIED = BlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)
        val ADDITIVE = BlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE)
    }


}