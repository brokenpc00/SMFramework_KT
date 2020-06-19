package com.interpark.smframework

import android.app.ActivityManager
import android.content.Context
import android.graphics.RectF
import android.util.Size
import androidx.fragment.app.FragmentActivity
import com.android.volley.RequestQueue
import com.interpark.smframework.base.SMScene
import com.interpark.smframework.base.SMView
import com.interpark.smframework.base.types.Scheduler
import com.interpark.smframework.base.sprite.Sprite
import com.interpark.smframework.base.sprite.SpriteSet
import com.interpark.smframework.base.texture.Texture
import com.interpark.smframework.base.texture.TextureManager
import com.interpark.smframework.base.types.Mat4
import com.interpark.smframework.shader.ShaderManager
import com.interpark.smframework.shader.ShaderProgram
import com.interpark.smframework.shader.ShaderManager.ProgramType
import com.interpark.smframework.util.Vec2


public interface IDirector {
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
    public fun getActivity(): FragmentActivity
    public fun getContext(): Context

    // screen size, width, height
    public fun setDisplayRawSize(width: Int, height: Int)
    public fun getDisplayRawWidth(): Int
    public fun getDisplayRawHeight(): Int
    public fun getScreenOrientation(): Int

    // camera
//    public fun getPreviewSurfaceView(): PreviewSurfaceView

    // ScissorTest
    public fun setScissorEnable(enable: Boolean)
    public fun isScissorTestEnabled(): Boolean

    // view property
    public fun getWinSize(): Size
    public fun getWidth(): Int
    public fun getHeight(): Int
    public fun getDeviceWidth(): Int
    public fun getDeviceHeight(): Int
    public fun getDisplayAdjust(): Float
    public fun isGLThread(): Boolean

    public fun getRequestQueue(): RequestQueue
    public fun getActionManager(): ActivityManager
    public fun getScheduler(): Scheduler
    public fun getGlobalTime(): Float

    public fun getFrameBufferId(): Int;
    public fun setFrameBufferId(frameBufferId: Int)
    public fun getFrameBufferSprite(): Sprite

    public fun setTouchEventDispatcherEnable(enable: Boolean)
    public fun getTouchEventDispatcherEnable(): Boolean

    public fun getColor(): FloatArray
    public fun setColor(r: Float, g: Float, b: Float, a: Float)

    public fun bindTexture(texture: Texture): Boolean
    public fun useProgram(type: ProgramType): ShaderProgram

    public fun pushMatrix(type: MATRIX_STACK_TYPE)
    public fun popMatrix(type: MATRIX_STACK_TYPE)
    public fun loadMatrix(type: MATRIX_STACK_TYPE, mat: Mat4)
    public fun getProjectionMatrix(index: Int): Mat4
    public fun getMatrix(type: MATRIX_STACK_TYPE): Mat4
    public fun getFrameBufferMatrix(): Mat4

    public fun getTickCount(): Int



    // primitive
    public fun drawFillRect(x1: Float, y1: Float, width: Float, height: Float)
    public fun drawRect(x1: Float, y1: Float, width: Float, height: Float, lineWidth: Float)
    public fun drawLine(x1: Float, y1: Float, x2: Float, y2: Float, lineWidth: Float)
    public fun drawCircle(x: Float, y: Float, radius: Float)
    public fun drawCircle(x: Float, y: Float, radius: Float, aaWidth: Float)
    public fun drawRing(x: Float, y: Float, radius: Float, thickness: Float)
    public fun drawRing(x: Float, y: Float, radius: Float, thickness: Float, aaWidth: Float)
    public fun drawSolidRect(x: Float, y: Float, width: Float, height: Float, cornerRadius: Float)
    public fun drawSolidRect(x: Float, y: Float, width: Float, height: Float, cornerRadius: Float, aaWidth: Float)



    // scene & layer & menu
    public fun getShaderManager(): ShaderManager
    public fun getSpriteSet(): SpriteSet
    public fun getTextureManager(): TextureManager
    public fun showProgress(show: Boolean, bounds: RectF)
    public fun showUploadProgress(show: Boolean, state: Int, bounds: RectF)

    public fun getTopScene(): SMScene
    public fun setSharedLayer(layerId: SharedLayer, layer: SMView)
    public fun getSharedLayer(layerId: SharedLayer): SMView

    public fun setSideMenuOpenPosition(position: Float)
    public fun getSideMenuState(): SIDE_MENU_STATE

    public fun getRunningScene(): SMScene
    public fun isSendCleanupToScene(): Boolean

    public fun replaceScene(scene: SMScene)
    public fun pushScene(scene: SMScene)
    public fun popScene()
    public fun popToRootScene()
    public fun popToSceneStackLevel(level: Int)
    public fun setNextScene()
    public fun popSceneWithTransition(scene: SMScene)
    public fun runWithScene(scene: SMScene)
    public fun startSceneAnimation()
    public fun stopSceneAnimation()
    public fun getPreviousScene(): SMScene
    public fun getSceneStackCount(): Int

    public fun convertToUI(glPoint: Vec2)

    public fun pause()
    public fun resume()
    public fun isPaused(): Boolean

}