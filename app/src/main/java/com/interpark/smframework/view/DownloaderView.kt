package com.brokenpc.smframework.view

import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.SMView
import com.brokenpc.smframework.base.UIContainerView
import com.brokenpc.smframework.base.sprite.Sprite
import com.brokenpc.smframework.base.types.Mat4
import com.brokenpc.smframework.downloader.ImageManager
import com.brokenpc.smframework.downloader.ImageTask

class DownloaderView(director:IDirector) : UIContainerView(director) {
    private var _status = ImageManager.TASK_NONE
    private var _sprite:Sprite? = null
    private var _cacheFlag:Boolean = false
    private var _isDrawn:Boolean = false
    private var _mediaType:Int = 0
    private var _imagePath:String? = null
    private var _diskCachePath:String? = null
    private var _downloadThread:ImageTask? =  null

    private var _siblingView:ArrayList<DownloaderView>? = null

    private var _imageWidth:Int = 0
    private var _imageHeight:Int = 0
    private var _imageDegrees:Int = 0

    fun getMediaType():Int {return _mediaType}
    fun getImagePath():String? {return _imagePath}
    fun getDiskCachPath():String? {return _diskCachePath}

    private fun releaseResource() {
        if (_siblingView!=null) {
            for (view in _siblingView!!) {
                view.releaseResource()
            }

            _siblingView?.clear()
            _siblingView = null
        }
        setImagePath(0, null, false, null, 0, 0, 0)
        setSprite(null)
        _downloadThread = null
    }

    override fun onRemoveFromParent(parent: SMView) {
        super.onRemoveFromParent(parent)
        releaseResource()
    }

    override fun draw(m: Mat4, flags: Int) {
        super.draw(m, flags)

        if (!_isDrawn && _imagePath!=null) {
            _downloadThread = ImageManager.startDownload(getDirector(), this, _cacheFlag, _imageWidth, _imageHeight, _imageDegrees)

            _isDrawn = true
        }
    }

    fun setImagePath(mediaType:Int, imagePath:String?, cacheFlag:Boolean, diskCachePath:String?, width:Int, height:Int, degrees:Int) {
        _status = ImageManager.TASK_NONE

        if (_imagePath!=null && _imagePath!! != imagePath) {
            ImageManager.removeDownload(_downloadThread, _imagePath!!)
        }

        if (imagePath!=null) {
            if (_imagePath!=imagePath) {
                ImageManager.removeDownload(_downloadThread, imagePath)
            } else {
                return
            }
        } else if (_imagePath!=null) {
            ImageManager.removeDownload(_downloadThread, _imagePath)
        }

        _imagePath = imagePath
        _diskCachePath = diskCachePath

        _mediaType = mediaType
        _imageWidth = width
        _imageHeight = height
        _imageDegrees = degrees

        if (_isDrawn && imagePath!=null) {
            _cacheFlag = cacheFlag
            _downloadThread = ImageManager.startDownload(getDirector(), this, cacheFlag, width, height, degrees)
        }
    }

    fun clearSibling() {
        if (_siblingView==null || _siblingView!!.isEmpty()) {
            return
        }

        val numSiblings = _siblingView!!.size
        for (i in numSiblings-1 .. 0 step -1) {
            val view:DownloaderView? = _siblingView!![i]
            view?.setImagePath(0, null, false, null, 0, 0, 0)
            _siblingView!!.removeAt(i)
        }
        _siblingView!!.clear()
    }

    fun setSiblingImagePath(index:Int, mediaType:Int, imagePath:String?, cacheFlag:Boolean, diskCachePath:String?, width:Int, height:Int, degrees:Int) {
        if (imagePath==null) return

        if (_siblingView==null) {

            _siblingView = ArrayList()
        }

        var view:DownloaderView? = null
        if (_siblingView!!.size>index) {
            view = _siblingView!![index]
        } else {
            view = DownloaderView(getDirector())
            _siblingView!!.add(view)
        }

        view.setImagePath(mediaType, imagePath, cacheFlag, diskCachePath, width, height, degrees)
    }

    fun setStatus(status:Int) {
        _status = status

    }

    fun setSprite(sprite:Sprite?) {
        if (sprite!=null) {
            if (_sprite!=null && _sprite!!.getTexture()!=sprite.getTexture()) {
                _sprite!!.removeTexture()
            }
        } else {
            _sprite?.removeTexture()
        }
        _sprite = sprite
    }

    fun getSprite():Sprite? {return _sprite}

    fun isSiblingViewComplete():Boolean {
        if (_siblingView==null || _siblingView!!.isEmpty()) {
            return false
        }

        val numView = _siblingView!!.size
        for (i in 0 until numView) {
            val view:DownloaderView? = _siblingView!![i]
            if (view!=null && view._status!=ImageManager.TASK_COMPLETE) {
                return false
            }
        }

        return true
    }

    fun getSiblinSpriteCount():Int {
        return _siblingView?.size ?: 0
    }

    fun getSiblinSprite(index:Int):Sprite? {
        return if (_siblingView!=null && _siblingView!!.isNotEmpty()) _siblingView!![index]._sprite else null
    }
}