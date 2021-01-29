package com.brokenpc.smframework.view

import android.view.View
import android.view.ViewConfiguration
import com.brokenpc.smframework.IDirector
import kotlin.properties.Delegates

class ViewConfig {
    companion object {
        const val SCROLL_TOLERANCE:Float = 10f
        const val MIN_VELOCITY:Float = 100f
        const val ACTIONBAR_HEIGHT:Int = 100
        const val ACTIONBAR_TAB_HEIGHT:Int = 96
        const val SIDEMENU_WIDTH:Int = 614
        const val SIDEMENU_GRAB_AREA:Int = 15
        private const val MAX_VELOCITY:Float = 10000f

        const val TAP_TIMEOUT:Long = 300L
        val DOUBLE_TAP_TIMEOUT:Long = ViewConfiguration.getDoubleTapTimeout().toLong()
        val LONG_PRESS_TIMEOUT:Long = ViewConfiguration.getLongPressTimeout().toLong()

        @JvmStatic
        fun getScaledMaximumFlingVelocity(): Float {
            return MAX_VELOCITY
        }

        @JvmStatic
        fun getScaledTouchSlop(director:IDirector):Float {
            return ViewConfiguration.get(director.getContext()).scaledTouchSlop.toFloat()
        }

        @JvmStatic
        fun getScaledDoubleTouchSlop(director: IDirector):Float {
            return ViewConfiguration.get(director.getContext()).scaledDoubleTapSlop.toFloat()
        }
    }


}