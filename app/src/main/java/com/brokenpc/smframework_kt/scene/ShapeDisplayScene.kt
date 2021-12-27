package com.brokenpc.app.scene

import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.SMView
import com.brokenpc.smframework.base.SceneParams
import com.brokenpc.smframework.base.types.Color4F
import com.brokenpc.smframework.base.types.Size
import com.brokenpc.smframework.base.types.Vec2
import com.brokenpc.smframework.util.AppConst
import com.brokenpc.smframework.view.*
import com.brokenpc.app.menu.MenuBar
import com.brokenpc.smframework.view.SMRoundLine
import com.brokenpc.smframework.view.SMSlider
import com.brokenpc.smframework.view.SMTriangleView

class ShapeDisplayScene(director: IDirector): SMMenuTransitionScene(director), SMSlider.OnSliderListener {

    private lateinit var _contentView:SMView
    private lateinit var _shapeBG:SMView
    private lateinit var _slideBG:SMView
    private lateinit var _scaleSlider:SMSlider
    private lateinit var _rotateSlider:SMSlider
    private lateinit var _moveXSlider:SMSlider
    private lateinit var _moveYSlider:SMSlider
    private lateinit var _colorRSlider:SMSlider
    private lateinit var _colorGSlider:SMSlider
    private lateinit var _colorBSlider:SMSlider
    private lateinit var _colorASlider:SMSlider

    private var _shapeScale = 1f
    private var _shapeRotate = 0f
    private var _shapePosX = 0f
    private var _shapePosY = 0f
    private var _shapeColor = Color4F(Color4F.BLACK)
    private var _shape:SMShapeView? = null
    private var _triangle:SMTriangleView? = null
    private var _shapeType = 0

    companion object {

        const val LABEL_TITLE_FONT_SIZE = 50f

        @JvmStatic
        fun create(director: IDirector, menuBar: MenuBar): ShapeDisplayScene {
            return create(director, menuBar, null)
        }

        @JvmStatic
        fun create(director: IDirector, menuBar: MenuBar, params: SceneParams?): ShapeDisplayScene {
            val scene = ShapeDisplayScene(director)
            scene.initWithParams(menuBar, params)
            return scene
        }
    }

    fun initWithParams(menuBar: MenuBar, params: SceneParams?): Boolean {
        super.initWithMenuBar(menuBar, SwipeType.DISMISS)
        _sceneParam = params
        getRootView().setBackgroundColor(Color4F.XEEEFF1)
        setMenuBarTitle(_sceneParam?.getString("MENU_NAME")?:"SUB_VIEW2")

        val s = getDirector().getWinSize()
        _contentView = create(getDirector(), 0, 0f, AppConst.SIZE.MENUBAR_HEIGHT, s.width, s.height-AppConst.SIZE.MENUBAR_HEIGHT)
        _contentView.setBackgroundColor(Color4F.WHITE)
        addChild(_contentView)

        makeShape()
        return true
    }

    private fun makeShape() {
        _shapeType = _sceneParam?.getInt("SHAPE_TYPE")?:0
        val s = _contentView.getContentSize()

        val bgViewHeight = AppConst.SIZE.MENUBAR_HEIGHT*4f

        _shapeBG = create(getDirector(), 0f, 0f, s.width, s.height-bgViewHeight)
        _shapeBG.setBackgroundColor(Color4F.XEEEFF1)
        _contentView.addChild(_shapeBG)

        _slideBG = create(getDirector(), 0f, s.height - bgViewHeight, s.width, bgViewHeight)
        _slideBG.setBackgroundColor(Color4F.XEEEFF1)
        _contentView.addChild(_slideBG)

        _shapeColor.r = getRandomColorF()
        _shapeColor.g = getRandomColorF()
        _shapeColor.b = getRandomColorF()
        _shapeColor.a = 1f

        val scale = SMLabel.create(getDirector(), "Scale", LABEL_TITLE_FONT_SIZE)
        val rotate = SMLabel.create(getDirector(), "Rotate", LABEL_TITLE_FONT_SIZE)
        val moveX = SMLabel.create(getDirector(), "Move H", LABEL_TITLE_FONT_SIZE)
        val moveY = SMLabel.create(getDirector(), "Move V", LABEL_TITLE_FONT_SIZE)
        val colorR = SMLabel.create(getDirector(), "Red", LABEL_TITLE_FONT_SIZE)
        val colorG = SMLabel.create(getDirector(), "Green", LABEL_TITLE_FONT_SIZE)
        val colorB = SMLabel.create(getDirector(), "Blue", LABEL_TITLE_FONT_SIZE)
        val alpha = SMLabel.create(getDirector(), "Alpha", LABEL_TITLE_FONT_SIZE)

        scale.setAnchorPoint(Vec2.LEFT_TOP)
        rotate.setAnchorPoint(Vec2.LEFT_TOP)
        moveX.setAnchorPoint(Vec2.LEFT_TOP)
        moveY.setAnchorPoint(Vec2.LEFT_TOP)
        colorR.setAnchorPoint(Vec2.LEFT_TOP)
        colorG.setAnchorPoint(Vec2.LEFT_TOP)
        colorB.setAnchorPoint(Vec2.LEFT_TOP)
        alpha.setAnchorPoint(Vec2.LEFT_TOP)

        var posY = 0f
        var posX = 20f
        scale.setPosition(posX, posY)
        posY += AppConst.SIZE.MENUBAR_HEIGHT
        rotate.setPosition(posX, posY)
        posY += AppConst.SIZE.MENUBAR_HEIGHT
        moveX.setPosition(posX, posY)
        posY += AppConst.SIZE.MENUBAR_HEIGHT
        moveY.setPosition(posX, posY)

        posY = 0f
        posX = s.width/2f + 20f
        colorR.setPosition(posX, posY)
        posY += AppConst.SIZE.MENUBAR_HEIGHT
        colorG.setPosition(posX, posY)
        posY += AppConst.SIZE.MENUBAR_HEIGHT
        colorB.setPosition(posX, posY)
        posY += AppConst.SIZE.MENUBAR_HEIGHT
        alpha.setPosition(posX, posY)

        _slideBG.addChild(scale)
        _slideBG.addChild(rotate)
        _slideBG.addChild(moveX)
        _slideBG.addChild(moveY)

        _slideBG.addChild(colorR)
        _slideBG.addChild(colorG)
        _slideBG.addChild(colorB)
        _slideBG.addChild(alpha)


        posY = 40f
        posX = 20f

        _scaleSlider = SMSlider.create(getDirector(), SMSlider.Type.ZERO_TO_ONE, SMSlider.LIGHT)
        _scaleSlider.setContentSize(Size(s.width/2f-40f, AppConst.SIZE.MENUBAR_HEIGHT-20f))
        _scaleSlider.setPosition(posX, posY)
        _scaleSlider.setOnSliderListener(this)
        _slideBG.addChild(_scaleSlider)
        posY += AppConst.SIZE.MENUBAR_HEIGHT

        _rotateSlider = SMSlider.create(getDirector(), SMSlider.Type.ZERO_TO_ONE, SMSlider.LIGHT)
        _rotateSlider.setContentSize(Size(s.width/2f-40f, AppConst.SIZE.MENUBAR_HEIGHT-20f))
        _rotateSlider.setPosition(posX, posY)
        _rotateSlider.setOnSliderListener(this)
        _slideBG.addChild(_rotateSlider)
        posY += AppConst.SIZE.MENUBAR_HEIGHT

        _moveXSlider = SMSlider.create(getDirector(), SMSlider.Type.ZERO_TO_ONE, SMSlider.LIGHT)
        _moveXSlider.setContentSize(Size(s.width/2f-40f, AppConst.SIZE.MENUBAR_HEIGHT-20f))
        _moveXSlider.setPosition(posX, posY)
        _moveXSlider.setOnSliderListener(this)
        _slideBG.addChild(_moveXSlider)
        posY += AppConst.SIZE.MENUBAR_HEIGHT

        _moveYSlider = SMSlider.create(getDirector(), SMSlider.Type.ZERO_TO_ONE, SMSlider.LIGHT)
        _moveYSlider.setContentSize(Size(s.width/2f-40f, AppConst.SIZE.MENUBAR_HEIGHT-20f))
        _moveYSlider.setPosition(posX, posY)
        _moveYSlider.setOnSliderListener(this)
        _slideBG.addChild(_moveYSlider)

        posY = 40f
        posX = s.width/2f + 20f

        _colorRSlider = SMSlider.create(getDirector(), SMSlider.Type.ZERO_TO_ONE, SMSlider.LIGHT)
        _colorRSlider.setContentSize(Size(s.width/2f-40f, AppConst.SIZE.MENUBAR_HEIGHT-20f))
        _colorRSlider.setPosition(posX, posY)
        _colorRSlider.setOnSliderListener(this)
        _slideBG.addChild(_colorRSlider)
        posY += AppConst.SIZE.MENUBAR_HEIGHT

        _colorGSlider = SMSlider.create(getDirector(), SMSlider.Type.ZERO_TO_ONE, SMSlider.LIGHT)
        _colorGSlider.setContentSize(Size(s.width/2f-40f, AppConst.SIZE.MENUBAR_HEIGHT-20f))
        _colorGSlider.setPosition(posX, posY)
        _colorGSlider.setOnSliderListener(this)
        _slideBG.addChild(_colorGSlider)
        posY += AppConst.SIZE.MENUBAR_HEIGHT

        _colorBSlider = SMSlider.create(getDirector(), SMSlider.Type.ZERO_TO_ONE, SMSlider.LIGHT)
        _colorBSlider.setContentSize(Size(s.width/2f-40f, AppConst.SIZE.MENUBAR_HEIGHT-20f))
        _colorBSlider.setPosition(posX, posY)
        _colorBSlider.setOnSliderListener(this)
        _slideBG.addChild(_colorBSlider)
        posY += AppConst.SIZE.MENUBAR_HEIGHT

        _colorASlider = SMSlider.create(getDirector(), SMSlider.Type.ZERO_TO_ONE, SMSlider.LIGHT)
        _colorASlider.setContentSize(Size(s.width/2f-40f, AppConst.SIZE.MENUBAR_HEIGHT-20f))
        _colorASlider.setPosition(posX, posY)
        _colorASlider.setOnSliderListener(this)
        _slideBG.addChild(_colorASlider)

        when (_shapeType) {
            0 -> {
                // dot
                val dot = SMSolidCircleView.create(getDirector())
                dot.setContentSize(80f, 80f)
                dot.setColor(_shapeColor)
                _shape = dot
            }
            1 -> {
                // line
                val line = SMRoundLine.create(getDirector())
                line.line(320f, s.height/2f, s.width-640f, s.height/2f)
                line.setLineWidth(1f)
                line.setColor(_shapeColor)
                _shape = line
            }
            2 -> {
                // rect
                val rect = SMRectView.create(getDirector())
                rect.setContentSize(100f, 100f)
                rect.setLineWidth(8f)
                rect.setColor(_shapeColor)
                _shape = rect
            }
            3 -> {
                // rounded-rect
                val rect = SMRoundRectView.create(getDirector())
                rect.setContentSize(100f, 100f)
                rect.setLineWidth(8f)
                rect.setCornerRadius(20f)
                rect.setColor(_shapeColor)
                _shape = rect
            }
            4 -> {
                // circle
                val circle = SMCircleView.create(getDirector())
                circle.setContentSize(100f, 100f)
                circle.setLineWidth(8f)
                circle.setColor(_shapeColor)
                _shape = circle
            }
            5 -> {
                // solid - rect
                val rect = SMSolidRectView.create(getDirector())
                rect.setContentSize(100f, 100f)
                rect.setColor(_shapeColor)
                _shape = rect
            }
            6 -> {
                // solid - rounded-rect
                val rect = SMSolidRoundRectView.create(getDirector())
                rect.setContentSize(100f, 100f)
                rect.setCornerRadius(20f)
                rect.setColor(_shapeColor)
                _shape = rect
            }
            7 -> {
                // solid - circle
                val circle = SMSolidCircleView.create(getDirector())
                circle.setContentSize(100f, 100f)
                circle.setColor(_shapeColor)
                _shape = circle
            }
            else -> {
                // solid - triangle
                val tri = SMShapeView(getDirector())
                tri.setContentSize(100f, 100f)


                _triangle = SMTriangleView.create(getDirector(), 100f, 100f)
                _triangle?.setColor(_shapeColor)
                _triangle?.setTriangle(Vec2(50f, 0f), Vec2(0f, 100f), Vec2(100f, 100f))
                tri.addChild(_triangle)
                _shape = tri
            }
        }

        if (_shape!=null) {
            _shape?.setAnchorPoint(Vec2.MIDDLE)
            _shape?.setPosition(_shapeBG.getContentSize().width/2f, _shapeBG.getContentSize().height/2f)
            _shapeBG.addChild(_shape)
        }
    }

    private fun layoutShape() {
        if (_shape!=null) {
            if (_shapeType>7) {
                // triangle
                _triangle?.setColor(_shapeColor, false)
            } else {
                _shape?.setColor(_shapeColor, false)
            }

            _shape?.setScale(_shapeScale, false)
            _shape?.setPosition(_shapeBG.getContentSize().width/2f+_shapePosX, _shapeBG.getContentSize().height/2f+_shapePosY, false)
            _shape?.setRotation(_shapeRotate, false)
        }
    }

    override fun onSliderValueChanged(slider: SMSlider, value: Float) {
        if (slider==_scaleSlider) {
            var scale = value * 20f
            if (scale<1f) scale = 1f
            _shapeScale = scale
        } else if (slider==_rotateSlider) {
            _shapeRotate = 360.0f * value
//            _shapeRotate = (-180f + 360.0f * value) + 360f
        } else if (slider==_colorRSlider) {
            _shapeColor.r = value
        } else if (slider==_colorGSlider) {
            _shapeColor.g = value
        } else if (slider==_colorBSlider) {
            _shapeColor.b = value
        } else if (slider==_colorASlider) {
            _shapeColor.a = value
        } else if (slider==_moveXSlider) {
            _shapePosX = _shapeBG.getContentSize().width/2f * value
        } else if (slider==_moveYSlider) {
            _shapePosY = _shapeBG.getContentSize().height/2f * value
        }

        layoutShape()
    }

    override fun onSliderValueChanged(slider: SMSlider, minValue: Float, maxValue: Float) {

    }

    override fun onMenuBarClick(view: SMView?): Boolean {
        return when (MenuBar.intToMenuType(view?.getTag()?:0)) {
            MenuBar.MenuType.BACK, MenuBar.MenuType.CLOSE -> {
                finishScene()
                true
            }
            else -> {
                false
            }
        }
    }
}