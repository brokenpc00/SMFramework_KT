package com.interpark.smframework

import com.interpark.smframework.IDirector.SharedLayer

class SMDirector private constructor() : IDirector {
    companion object {
        private var _instance: SMDirector? = null
        fun getDirector(): SMDirector {
            if (_instance==null)
                _instance= SMDirector()

            return _instance!!
        }

        val BASE_SCREEN_WIDTH: Int = 1080

        fun intToEnumForSharedLayer(num: Int): SharedLayer {
            when (num) {
                1 -> return SharedLayer.LEFT_MENU
                2 -> return SharedLayer.RIGHT_MENU
                3 -> return SharedLayer.BETWEEN_MENU_AND_SCENE
                4 -> return SharedLayer.BETWEEN_SCENE_AND_UI
                5 -> return SharedLayer.UI
                6 -> return SharedLayer.BETWEEN_UI_AND_POPUP
                7 -> return SharedLayer.DIM
                8 -> return SharedLayer.POPUP
            }

            return SharedLayer.BACKGROUND
        }

        fun enumToIntForSharedLayer(layer: SharedLayer): Int {
            when (layer) {
                SharedLayer.BACKGROUND -> return 0
                SharedLayer.LEFT_MENU -> return 1
                SharedLayer.RIGHT_MENU -> return 2
                SharedLayer.BETWEEN_MENU_AND_SCENE -> return 3
//                SharedLayer.POPUP
            }

            return 0
        }



    }


}