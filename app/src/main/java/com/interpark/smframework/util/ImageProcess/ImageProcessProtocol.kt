package com.interpark.smframework.util.ImageProcess

import com.brokenpc.smframework.base.SceneParams
import com.brokenpc.smframework.base.sprite.Sprite
import com.brokenpc.smframework.base.texture.Texture
import com.brokenpc.smframework.base.types.Size

interface ImageProcessProtocol {
    open fun onImageProcessComplete(tag: Int, success: Boolean, sprite: Sprite?, params: SceneParams?) {}
    open fun onImageCaptureComplete(tag: Int, texture: Texture?, data: ByteArray?, size: Size, bpp: Int) {}
    open fun onImageProcessProgress(tag: Int, progress: Float) {}
    open fun resetImageProcess() {}
    open fun removeImageProcessTask(task: ImageProcessTask) {}
    open fun addImageProcessTask(task: ImageProcessTask): Boolean {return false}

    public var _imageProcessTask: ArrayList<ImageProcessTask>
}