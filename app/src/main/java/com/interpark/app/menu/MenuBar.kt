package com.interpark.app.menu

import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.SMView
import com.brokenpc.smframework.view.SMImageView

class MenuBar(director: IDirector): SMView(director) {
    enum class DropDown {
        NOTHING,
        UP,
        DOWN
    }

    enum class MenuType {
        NONE,
        DROPDOWN,

        MENU,
        BACK,
        CLOSE,
        DOT,
        CLOSE2,
        ALARM,

        SEARCH,
        CONFIRM,
        DELETE,
        CAMERA,
        ALBUM,
        CART,
        SETTINGS,

        NEXT,
        DONE,
        CANCEL,
        CROP,
        CLEAR
    }

    enum class TextTransition {
        FADE,
        ELASTIC,
        SWIPE
    }

    enum class ButtonTransition {
        FADE,
        ELASTIC,
        SWIPE
    }

    protected var _contentView:SMView? = null
    protected var _dropDownButton:SMImageView? = null

}