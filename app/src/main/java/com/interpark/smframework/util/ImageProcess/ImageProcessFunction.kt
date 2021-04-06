package com.interpark.smframework.util.ImageProcess

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.PixelFormat
import com.brokenpc.smframework.SMDirector
import com.brokenpc.smframework.base.SMView
import com.brokenpc.smframework.base.SceneParams
import com.brokenpc.smframework.base.sprite.BitmapSprite
import com.brokenpc.smframework.base.sprite.Sprite
import com.brokenpc.smframework.base.texture.BitmapTexture
import com.brokenpc.smframework.base.texture.Texture
import com.brokenpc.smframework.base.types.Color4F
import com.brokenpc.smframework.base.types.Size
import com.brokenpc.smframework.base.types.Vec2
import java.lang.Exception
import java.nio.ByteBuffer

open class ImageProcessFunction() {
    protected var _inputSize:Size = Size()
    protected var _inputData:ByteArray? = null
    protected var _inputBpp:Int = 0

    private var _capturedTexture:BitmapTexture? = null
    private var _param:SceneParams? = null
    private var _outputSize:Size = Size()
    private var _outputData:ByteArray? = null
    private var _outputImage:Bitmap? = null
    private var _outputBpp:Int = 0
    private var _clearColor:Color4F = Color4F(0f, 0f, 0f, 0f)
    private var _interrupt:Boolean = false
    private var _isCaptureOnly:Boolean = false
    private var _task:ImageProcessTask? = null


    init {
        _inputData = null
        _outputData = null
        _param = null
        _clearColor = Color4F(Color4F.TRANSPARENT)
        _isCaptureOnly = false
        _outputImage = null
        initParam()
    }

    fun initParam(): SceneParams {
        if (_param==null) {
            _param = SceneParams.create()
        }

        return _param!!
    }

    @Throws(Throwable::class)
    fun finalize() {
        try {
            releaseInputData()
            releaseOutputData()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            println("All done!")
        }
    }

    open fun onPreProcess(view: SMView?): Boolean {
        return if (view!=null) {
            val canvasSize = view.getContentSize()

            val x = canvasSize.width * 0.5f
            val y = canvasSize.height * 0.5f

            startProcess(view, canvasSize, Vec2(x, y), Vec2.MIDDLE, 1f, 1f)
        } else {
            true
        }
    }

    open fun onProcessInBackground(): Boolean {return false}

    fun onPostProcess(): Sprite? {
        var bitmap: Bitmap? = null

        if (getOutputImage()!=null) {
            bitmap = SMView.copyBitmap(getOutputImage())
            releaseOutputData()
        } else if (getOutputData()!=null) {
            val bpp = getOutputBpp()

            var pixelFormat = PixelFormat.RGBA_8888

            if (bpp==3) {
                pixelFormat = PixelFormat.RGB_888
            }

            val size = getOutputSize()
            val data = getOutputData()
            val bmp = BitmapFactory.decodeByteArray(data, 0, data?.size?:0)
            bitmap = bmp?.copy(Bitmap.Config.ARGB_8888, true)
            releaseOutputData()
        }

        if (bitmap!=null) {
            return BitmapSprite.createFromBitmap(SMDirector.getDirector(), "IMGPROC", bitmap)
        }

        return null

    }

    protected fun startProcess(view:SMView, canvasSize: Size, position: Vec2, anchorPoint: Vec2, scaleX: Float, scaleY: Float): Boolean {
        val bmp = view.captureView()

        _capturedTexture = BitmapTexture(SMDirector.getDirector(), "IMGPROC", bmp)

        onCaptureComplete(bmp)

        if (bmp!=null) {
            if (isCaptureOnly()) {
                return true
            }

            _inputSize = Size(bmp.width, bmp.height)
            _inputBpp = 4

            val buffer = ByteBuffer.allocate(bmp.rowBytes*bmp.height)
            bmp.copyPixelsToBuffer(buffer)
            _inputData = ByteArray(bmp.rowBytes*bmp.height)
            buffer.get(_inputData)

            return true
        }

        return false
    }


    open fun onCaptureComplete(bitmap: Bitmap?) {}


    // property method
    fun getCapturedTexture():Texture? {return _capturedTexture}
    fun getParam():SceneParams? {return _param}
    fun setTask(task: ImageProcessTask) {_task = task}
    fun onProgress(progress: Float) {}
    fun setCaptureOnly() {_isCaptureOnly = true}
    fun isCaptureOnly(): Boolean {return _isCaptureOnly}
    fun initOutputBuffer(width: Int, height: Int, bpp: Int) {
        _outputData = ByteArray(width*height*bpp)
        _outputSize = Size(width, height)
        _outputBpp = bpp
    }
    fun setOutputImage(image: Bitmap?) {_outputImage = image}
    fun getOutputSize(): Size {return _outputSize}
    fun getInputSize(): Size {return _inputSize}
    fun getInputBpp(): Int {return _inputBpp}
    fun getOutputBpp(): Int {return _outputBpp}
    fun getOutputImage(): Bitmap? {return _outputImage}
    fun getInputData(): ByteArray? {return _inputData}
    fun getOutputData(): ByteArray? {return _outputData}
    fun getInputDataLength(): Int {return (_inputSize.width*_inputSize.height*_inputBpp).toInt()}
    fun releaseInputData() {_inputData = null}
    fun releaseOutputData() {_outputData = null}
    fun setClearColor(clearColor: Color4F) {_clearColor = clearColor}
    fun interrupt() {_interrupt=true}
    fun isInterrupt(): Boolean {return _interrupt}
    open fun onReadPixelsCommand() {}
}