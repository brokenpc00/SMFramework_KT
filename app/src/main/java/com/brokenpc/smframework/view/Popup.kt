package com.brokenpc.smframework.view

import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.SMView
import com.brokenpc.smframework.util.BackPressable

open class Popup : SMView, BackPressable {
    private var _cancelable = true
    companion object {
        const val POPUP_SHOW_TIMEMILLIS = 0.25f
        const val POPUP_HIDE_TIMEMILLIS = 0.15f
        const val POPUP_DEFAULT_FADEVALUE = 0.6f


    }

    constructor(director: IDirector) : super(director)

    constructor(director: IDirector, id: Int) : super(director) {
        setTag(id)
    }

    constructor(director: IDirector, x: Float, y: Float, width: Float, height: Float) : super(director) {
        setPosition(x, y)
        setContentSize(width, height)
    }

    constructor(director: IDirector, x: Float, y: Float, width: Float, height: Float, anchorX: Float, anchorY: Float) : super(director) {
        setPosition(x, y)
        setContentSize(width, height)
        setAnchorPoint(anchorX, anchorY)
    }

    override fun onBackPressed(): Boolean {
        if (_cancelable) {
            cancel()
        }
        return true
    }

    interface OnDimissListener {
        fun onDismiss(popup: Popup)
    }
    var _dismissListener:OnDimissListener? = null

    fun setOnDismissListener(l: OnDimissListener) {
        _dismissListener = l
    }

    fun dismiss() {
        getDirector().closePopupView(this)
        _dismissListener?.onDismiss(this)
    }

    override fun cancel() {
        dismiss()
    }

}