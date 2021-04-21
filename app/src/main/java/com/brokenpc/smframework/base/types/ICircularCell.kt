package com.interpark.smframework.base.types

interface ICircularCell {
    fun getCellIndex(): Int
    fun getCellPosition(): Float
    fun getCellIdentifier(): String
    fun markDelete()
    fun setCellIndex(index: Int)
    fun setCellPosition(position: Float)
    fun setReuseIdentifier(identifier: String)

    fun setAniSrc(src: Float)
    fun setAniDst(dst: Float)
    fun setAniIndex(index: Int)

    fun isDeleted(): Boolean
    fun getAniSrc(): Float
    fun getAniDst(): Float
    fun getAniIndex(): Int
}