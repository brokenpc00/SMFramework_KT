package com.brokenpc.smframework.base.transition

import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.SMView
import com.brokenpc.smframework.base.types.Action
import com.brokenpc.smframework.base.types.ActionInterval
import com.brokenpc.smframework.util.AppConst
import com.brokenpc.smframework.view.SMButton
import java.nio.channels.FileLock

class StateTransitionAction(director:IDirector) : ActionInterval(director) {
    private var _toState:SMView.STATE = SMView.STATE.NORMAL

    companion object {
        @JvmStatic
        fun create(director: IDirector, toState:SMView.STATE):StateTransitionAction {
            val action = StateTransitionAction(director)
            if (action.initWithDuration(0f)) {
                action._toState = toState
            }

            return action
        }
    }

    override fun startWithTarget(target: SMView?) {
        super.startWithTarget(target)

        var tag:Int = if (_toState==SMView.STATE.PRESSED) { AppConst.TAG.ACTION_VIEW_STATE_CHANGE_PRESS_TO_NORMAL } else {AppConst.TAG.ACTION_VIEW_STATE_CHANGE_NORMAL_TO_PRESS}

        if (target!!.getActionByTag(tag)!=null) {
            var action:ActionInterval = target!!.getActionByTag(tag) as ActionInterval
            target.stopAction(action)
            val run: Float = action.getElapsed() / action.getDuration()
            if (run<1) {
                _firstTick = false
                _elapsed = getDuration() * (1-run)
            }
        }
    }

    override fun update(t: Float) {
        if (_target!=null) {
            if (_target is SMButton) {
                val button = _target as SMButton
                button.onUpdateStateTransition(_toState, if (_toState==SMView.STATE.PRESSED) {t} else {1-t})
            }
        }
    }
}