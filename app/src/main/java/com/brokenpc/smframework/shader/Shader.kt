package com.brokenpc.smframework.shader

class Shader(shaderID:Int) {
    private var _shaderId:Int = shaderID
    private var _refCount:Int = 0
    fun incRef() {_refCount++}
    fun decRef():Boolean {return (--_refCount>=0)}
    fun getRefCount():Int {return _refCount}
    fun getId():Int {return _shaderId}
}