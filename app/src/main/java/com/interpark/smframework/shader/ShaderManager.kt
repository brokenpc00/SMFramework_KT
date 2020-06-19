package com.interpark.smframework.shader

class ShaderManager {

    public enum class ProgramType {
        Sprite(1, "glsl/sprite.vsh", ProgSprite()) {

        };

        var key: Int = 0
        var vertexShader: String = ""
        var fragmentShader: String = ""
        lateinit var program: ShaderProgram


        constructor(key: Int, vertexShader: String, program: ShaderProgram) {

        }
    }

}