package com.brokenpc.smframework.shader

import android.opengl.GLES20
import com.brokenpc.smframework.base.types.Vec2

class ProgGeineEffect2 : ProgSprite() {
    private var _uniformAnchor = 0
    private var _uniformProgress = 0

    companion object {
        const val NAME_ANCHOR = "u_anchor"
        const val NAME_PROGRESS = "u_progress"
    }

    override fun complete() {
        super.complete()
        _uniformAnchor = GLES20.glGetUniformLocation(_programId, NAME_ANCHOR)
        _uniformProgress = GLES20.glGetUniformLocation(_programId, NAME_PROGRESS)
    }

    fun setGeineValue(anchor: Vec2, progress: Float) {
        GLES20.glUniform2f(_uniformAnchor, anchor.x, anchor.y)
        GLES20.glUniform1f(_uniformProgress, progress)
    }
}