package com.brokenpc.smframework

import android.content.Context
import android.graphics.RectF
import androidx.fragment.app.FragmentActivity
import com.android.volley.RequestQueue
import com.brokenpc.smframework.base.SMScene
import com.brokenpc.smframework.base.SMView
import com.brokenpc.smframework.base.sprite.Sprite
import com.brokenpc.smframework.base.sprite.SpriteSet
import com.brokenpc.smframework.base.texture.Texture
import com.brokenpc.smframework.base.texture.TextureManager
import com.brokenpc.smframework.base.types.*
import com.brokenpc.smframework.shader.ShaderManager
import com.brokenpc.smframework.shader.ShaderManager.ProgramType
import com.brokenpc.smframework.shader.ShaderProgram


interface IDirector {
    public enum class SIDE_MENU_STATE {
        CLOSE, OPEN, MOVING,
    }

    public enum class SharedLayer {
        BACKGROUND,
        LEFT_MENU,
        RIGHT_MENU,
        BETWEEN_MENU_AND_SCENE,
        // scene
        BETWEEN_SCENE_AND_UI,
        UI,
        BETWEEN_UI_AND_POPUP,
        DIM,
        POPUP,
    }

    public enum class MATRIX_STACK_TYPE {
        MATRIX_STACK_MODELVIEW,
        MATRIX_STACK_PROJECTION,
        MATRIX_STACK_TEXTURE
    }

    // activity & context
    fun getActivity(): FragmentActivity
    fun getContext(): Context

    // screen size, width, height
    fun setDisplayRawSize(width: Int, height: Int)
    fun getDisplayRawWidth(): Int
    fun getDisplayRawHeight(): Int
    fun getScreenOrientation(): Int

    // camera
//    fun getPreviewSurfaceView(): PreviewSurfaceView

    // ScissorTest
    fun enableScissorTest(enable: Boolean)
    fun isScissorTestEnabled(): Boolean

    // view property
    fun getWinSize(): Size
    fun getWidth(): Int
    fun getHeight(): Int
    fun getDeviceWidth(): Int
    fun getDeviceHeight(): Int
    fun getDisplayAdjust(): Float
    fun isGLThread(): Boolean

    fun getRequestQueue(): RequestQueue
    fun getActionManager(): ActionManager
    fun getScheduler(): Scheduler
    fun getGlobalTime(): Float

    fun getFrameBufferId(): Int;
    fun setFrameBufferId(frameBufferId: Int)
    fun getFrameBufferSprite(): Sprite?

    fun setTouchEventDispatcherEnable(enable: Boolean)
    fun getTouchEventDispatcherEnable(): Boolean

    fun getColor(): FloatArray
    fun setColor(r: Float, g: Float, b: Float, a: Float)

    fun bindTexture(texture: Texture?): Boolean
    fun useProgram(type: ProgramType): ShaderProgram?

    fun pushMatrix(type: MATRIX_STACK_TYPE)
    fun popMatrix(type: MATRIX_STACK_TYPE)
    fun loadMatrix(type: MATRIX_STACK_TYPE, mat: Mat4)
    fun getProjectionMatrix(index: Int): Mat4
    fun getMatrix(type: MATRIX_STACK_TYPE): Mat4
    fun getFrameBufferMatrix(): Mat4
//
    fun getTickCount(): Long
//
//
//
    // primitive
    fun drawFillRect(x: Float, y: Float, width: Float, height: Float)
    fun drawRect(x: Float, y: Float, width: Float, height: Float, lineWidth: Float)
    fun drawLine(x1: Float, y1: Float, x2: Float, y2: Float, lineWidth: Float)
    fun drawCircle(x: Float, y: Float, radius: Float)
    fun drawCircle(x: Float, y: Float, radius: Float, aaWidth: Float)
    fun drawRing(x: Float, y: Float, radius: Float, thickness: Float)
    fun drawRing(x: Float, y: Float, radius: Float, thickness: Float, aaWidth: Float)
    fun drawSolidRect(x: Float, y: Float, width: Float, height: Float, cornerRadius: Float)
    fun drawSolidRect(x: Float, y: Float, width: Float, height: Float, cornerRadius: Float, aaWidth: Float)



    // scene & layer & menu
    fun getShaderManager(): ShaderManager
    fun getSpriteSet(): SpriteSet?
    fun getTextureManager(): TextureManager
    fun showProgress(show: Boolean, bounds: RectF)
    fun showUploadProgress(show: Boolean, state: Int, bounds: RectF)

    fun getTopScene(): SMScene?
    fun setSharedLayer(layerId: SharedLayer, layer: SMView?)
    fun getSharedLayer(layerId: SharedLayer): SMView?

    fun setSideMenuOpenPosition(position: Float)
    fun getSideMenuState(): SIDE_MENU_STATE

    fun getRunningScene(): SMScene?
    fun isSendCleanupToScene(): Boolean

    fun replaceScene(scene: SMScene)
    fun pushScene(scene: SMScene)
    fun popScene()
    fun popToRootScene()
    fun popToSceneStackLevel(level: Int)
    fun setNextScene()
    fun popSceneWithTransition(scene: SMScene)
    fun runWithScene(scene: SMScene)
    fun startSceneAnimation()
    fun stopSceneAnimation()
    fun getPreviousScene(): SMScene?
    fun getSceneStackCount(): Int

    fun convertToUI(glPoint: Vec2): Vec2

    fun pause()
    fun resume()
    fun isPaused(): Boolean

}