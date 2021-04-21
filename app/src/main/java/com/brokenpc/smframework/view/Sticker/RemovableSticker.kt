package com.interpark.smframework.view.Sticker

import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.view.SMImageView

open class RemovableSticker(director: IDirector): SMImageView(director) {

    fun isRemovable(): Boolean {return true}
}