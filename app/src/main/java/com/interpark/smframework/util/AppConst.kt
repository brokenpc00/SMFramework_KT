package com.brokenpc.smframework.util

class AppConst {

    object SceneTransitionTime {
        const val NORMAL = 0.3f
        const val FAST = 0.2f
        const val SLOW = 0.5f
        const val DEFAULT_DELAY_TIME = 0.1f
    }

    object ZOrder {
        const val USER = 0
        const val BG = Int.MIN_VALUE + 1
        const val BUTTON_NORMAL = Int.MIN_VALUE + 100
        const val BUTTON_PRESSED = BUTTON_NORMAL + 1
        const val BUTTON_TEXT = BUTTON_PRESSED + 1
        const val BUTTON_ICON_NORMAL = BUTTON_TEXT + 1
        const val BUTTON_ICON_PRESSED = BUTTON_ICON_NORMAL + 1
    }

    object DEFAULT_VALUE {
        const val FONT_SIZE = 12.0f
        const val LINE_WIDTH = 2.0f
    }

    object SIZE {
        const val EDGE_SWIPE_MENU = 80.0f
        const val EDGE_SWIPE_TOP = 130.0f
        const val LEFT_SIDE_MENU_WIDTH = 550.0f
        const val TOP_MENU_HEIGHT = 130.0f
        const val TOP_MENU_BUTTON_HEIGHT = 120.0f
        const val MENUBAR_HEIGHT = 130.0f
        const val BOTTOM_MENU_HEIGHT = 160.0f
        const val DOT_DIAMETER = 20.0f
        const val LINE_DIAMETER = 5.0f
        const val TOP_MENU_BUTTONE_SIZE = 120.0f
    }

    object TAG {
        const val USER = 0x10000
        const val ACTION_MENUBAR_MENU = USER + 1
        const val ACTION_MENUBAR_COLOR = USER + 2
        const val ACTION_MENUBAR_TEXT = USER + 3
        const val ACTION_MENUBAR_BUTTON = USER + 4
        const val ACTION_MENUBAR_DROPDOWN = USER + 5
        const val SYSTEM = 0x10000
        const val ACTION_VIEW_SHOW = SYSTEM + 1
        const val ACTION_VIEW_HIDE = SYSTEM + 2
        const val ACTION_BG_COLOR = SYSTEM + 3
        const val ACTION_VIEW_STATE_CHANGE_PRESS_TO_NORMAL = SYSTEM + 4
        const val ACTION_VIEW_STATE_CHANGE_NORMAL_TO_PRESS = SYSTEM + 5
        const val ACTION_VIEW_STATE_CHANGE_DELAY = SYSTEM + 6
        const val ACTION_ZOOM = SYSTEM + 7
        const val ACTION_STICKER_REMOVE = SYSTEM + 10
        const val ACTION_LIST_ITEM_DEFAULT = SYSTEM + 100
        const val ACTION_LIST_HIDE_REFRESH = SYSTEM + 101
        const val ACTION_LIST_JUMP = SYSTEM + 102
        const val ACTION_PROGRESS1 = SYSTEM + 103
        const val ACTION_PROGRESS2 = SYSTEM + 104
        const val ACTION_PROGRESS3 = SYSTEM + 105
    }

    object Config {
        const val DEFAULT_FONT_SIZE = 12f
        const val TAP_TIMEOUT = 0.5f
        const val DOUBLE_TAP_TIMEOUT = 0.3f
        const val LONG_PRESS_TIMEOUT = 0.5f
        const val SCALED_TOUCH_SLOPE = 100.0f
        const val SCALED_DOUBLE_TAB_SLOPE = 100.0f
        const val SMOOTH_DIVIDER = 3.0f
        const val TOLERANCE_POSITION = 0.01f
        const val TOLERANCE_ROTATE = 0.01f
        const val TOLERANCE_SCALE = 0.005f
        const val TOLERANCE_COLOR = 0.0005f
        const val TOLERANCE_ALPHA = 0.0005f
        const val MIN_VELOCITY = 1000.0f
        const val MAX_VELOCITY = 30000.0f
        const val SCROLL_TOLERANCE = 10.0f
        const val SCROLL_HORIZONTAL_TOLERANCE = 20.0f
        const val BUTTON_PUSHDOWN_PIXELS = -10.0f
        const val BUTTON_PUSHDOWN_SCALE = 0.9f
        const val BUTTON_STATE_CHANGE_PRESS_TO_NORMAL_TIME = 0.25f
        const val BUTTON_STATE_CHANGE_NORMAL_TO_PRESS_TIME = 0.15f
        const val ZOOM_SHORT_TIME = 0.1f
        const val ZOOM_NORMAL_TIME = 0.30f
        const val LIST_HIDE_REFRESH_TIME = 0.1f
        const val TEXT_TRANS_DELAY = 0.05f
        const val TEXT_TRANS_DURATION = 0.17f
        const val TEXT_TRANS_MOVE_DURATION = 0.6f
        const val ACTION_BUTTON_DELAY = 0.1f
    }
}