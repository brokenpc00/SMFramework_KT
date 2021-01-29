package com.brokenpc.smframework.shader

import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.SMView
import com.brokenpc.smframework.base.types.Color4F
import com.brokenpc.smframework.base.types.Mat4

class ShaderNode(director:IDirector) : SMView(director) {
    enum class Quadrant {
        ALL,  // 0
        LEFT_HALF,  // 1
        RIGHT_HALF,  // 2
        TOP_HALF,  // 3
        BOTTOM_HALF,  // 4
        LEFT_TOP,  // 5
        LEFT_BOTTOM,  // 6
        RIGHT_TOP,  // 7
        RIGHT_BOTTOM // 8
    }

    companion object {
        var DEFAULT_ANTI_ALIAS_WIDTH:Float = 1.5f
    }


    override fun draw(m: Mat4, flags: Int) {}

    fun setColor4F(color: Color4F?) {}

    protected var _customCommand: CustomCommand? = null
    protected var _uniformColor = 0
    protected var _blendFunc: BlendFunc? = null
    var _quadrant = 0
}