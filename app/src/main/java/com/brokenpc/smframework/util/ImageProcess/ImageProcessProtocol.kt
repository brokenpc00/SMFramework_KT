package com.brokenpc.smframework.util.ImageProcess

import com.brokenpc.smframework.base.SceneParams
import com.brokenpc.smframework.base.sprite.Sprite
import com.brokenpc.smframework.base.texture.Texture
import com.brokenpc.smframework.base.types.Size

interface ImageProcessProtocol {
    fun onImageProcessComplete(tag: Int, success: Boolean, sprite: Sprite?, params: SceneParams?) {}
    fun onImageCaptureComplete(tag: Int, texture: Texture?, data: ByteArray?, size: Size, bpp: Int) {}
    fun onImageProcessProgress(tag: Int, progress: Float) {}
    fun resetImageProcess() {}
    fun removeImageProcessTask(task: ImageProcessTask) {}
    fun addImageProcessTask(task: ImageProcessTask): Boolean {return false}

    var _imageProcessTask: ArrayList<ImageProcessTask>
}