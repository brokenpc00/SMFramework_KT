package com.interpark.app.scene

import android.view.View
import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.SMView
import com.brokenpc.smframework.base.SceneParams
import com.brokenpc.smframework.base.scroller.SMScroller
import com.brokenpc.smframework.base.types.Color4F
import com.brokenpc.smframework.base.types.Rect
import com.brokenpc.smframework.base.types.Size
import com.brokenpc.smframework.base.types.Vec2
import com.brokenpc.smframework.shader.ShaderNode
import com.brokenpc.smframework.util.AppConst
import com.brokenpc.smframework.view.*
import com.interpark.app.menu.MenuBar
import com.interpark.app.scene.stickerLayer.StickerItemListView
import com.interpark.app.scene.stickerLayer.StickerLayer
import com.interpark.smframework.view.RingWave2
import com.interpark.smframework.view.SMCircularListView
import com.interpark.smframework.view.SMPageView
import com.interpark.smframework.view.SMZoomView

class ViewDisplayScene(director: IDirector): SMMenuTransitionScene(director), SMPageView.OnPageChangedCallback {
    private lateinit var _contentView: SMView
    private var _imageButtonGravityType = false
    // for image
    private var _mainImageView: SMImageView? = null
    private var _viewType = 0
    // for sticker
    private var _stickerMenuView: SMView? = null
    private var _stickerLayer: StickerLayer? = null
    private var _stickerListView: StickerItemListView? = null
    // for ring wave
    private var _ringView: RingWave2? = null
    private var _alarmCircle: SMSolidCircleView? = null
    private var _ringFlag = false
    private var _buttunRect = Rect(Rect.ZERO)
    // for table View
    private var _tableContainView: SMPageView? = null
    private var _tableBgViews:ArrayList<SMView>? = null
    private var _tableView1: SMTableView? = null
    private var _tableView2: SMTableView? = null
    private var _tableView3: SMTableView? = null
    private var _tableView4: SMTableView? = null
    private var _tableView5: SMTableView? = null
    private var _tableViewLabel: SMLabel? = null
    // for circular view
    private val PAGER_PADDING = 300f
    private var _circularConfig: SMCircularListView.Config? = null
    private var _circularListview: SMCircularListView? = null
    private var _circularOrientation: SMCircularListView.Orientation = SMCircularListView.Orientation.HORIZONTAL
    private var _circularScrollMode: SMScroller.ScrollMode = SMScroller.ScrollMode.PAGER
    private var _isCircular = false

    private val _circularImages: ArrayList<CircularImageCell> = ArrayList()
    private var _circularLabel: SMLabel? = null

    // for pager
    private var _horPageView: SMPageView? = null
    private var _verPageView: SMPageView? = null
    private var _horLabel: SMLabel? = null
    private var _verLabel: SMLabel? = null
    private var _pageItemCount = 10
    private var _currentHorPage = 0
    private var _currentVerPage = 0
    private val _horImages: ArrayList<SMImageView> = ArrayList()
    private val _verImages: ArrayList<SMImageView> = ArrayList()

    // for zoom
    private var _zoomView: SMZoomView? = null

    // for Image view
    private var _scaleButton: SMButton?= null
    private var _gravityButton: SMButton? = null
    private var _scaleBG: SMView? = null
    private var _gravitiBG: SMView? = null



    companion object {
        @JvmStatic
        fun create(director: IDirector, meubar: MenuBar): ViewDisplayScene {
            return create(director, meubar, null)
        }
        @JvmStatic
        fun create(director: IDirector, meubar: MenuBar, params: SceneParams?): ViewDisplayScene {
            val scene = ViewDisplayScene(director)
            scene.initWithParams(meubar, params)
            return scene
        }
    }

    override fun onMenuBarClick(view: SMView?): Boolean {
        val type = MenuBar.intToMenuType(view?.getTag()?:0)
        when (type) {
            MenuBar.MenuType.BACK, MenuBar.MenuType.CLOSE -> {
                finishScene()
                return true
            }
            else -> {
                return false
            }
        }
    }

    protected fun initWithParams(menuBar: MenuBar, params: SceneParams?): Boolean {
        super.initWithMenuBar(menuBar, SwipeType.DISMISS)

        _sceneParam = params
        getRootView().setBackgroundColor(Color4F.XEEEFF1)
        setMenuBarTitle(_sceneParam?.getString("MENU_NAME")?:"VIEW")

        val s = getDirector().getWinSize()
        _contentView = create(getDirector(), 0, 0f, AppConst.SIZE.MENUBAR_HEIGHT, s.width, s.height-AppConst.SIZE.MENUBAR_HEIGHT)
        _contentView.setAnchorPoint(Vec2.MIDDLE)
        _contentView.setPosition(s.width/2f, s.height/2f+AppConst.SIZE.MENUBAR_HEIGHT/2f)
        _contentView.setBackgroundColor(Color4F.WHITE)
        addChild(_contentView)

        makeView()
        return true
    }

    fun makeView() {
        _viewType = _sceneParam?.getInt("VIEW_TYPE")?:0

        when (_viewType) {
            else -> {
                imageDisplay()
            }
        }
    }

    fun imageDisplay() {
        val s = _contentView.getContentSize()

        val fontSize = 35f
        val padding = 30f
        val buttonSize = AppConst.SIZE.MENUBAR_HEIGHT - padding * 2f

        val bgHeight = buttonSize * 5

        _mainImageView = SMImageView.create(getDirector(), "images/defaults2.jpg")
        if (_mainImageView==null) return
        _mainImageView!!.setContentSize(Size(s.width, s.height-bgHeight))
        _mainImageView!!.setPosition(Vec2.ZERO)
        _mainImageView!!.setScaleType(SMImageView.ScaleType.CENTER)
        _contentView.addChild(_mainImageView)

        val menuBg = SMView.create(getDirector(), 0f, s.height-bgHeight, s.width, bgHeight)
        menuBg.setBackgroundColor(Color4F.XEEEFF1)
        _contentView.addChild(menuBg)

        _scaleButton = SMButton.Companion.create(getDirector(),0, SMButton.STYLE.SOLID_RECT, 0f, 0f, s.width/2f, buttonSize)
        _gravityButton = SMButton.Companion.create(getDirector(), 0, SMButton.STYLE.SOLID_RECT, s.width/2, 0f, s.width/2f, buttonSize)
        menuBg.addChild(_scaleButton)
        menuBg.addChild(_gravityButton)

        _scaleButton!!.setText("SCALE TYPE", 52.5f)
        _gravityButton!!.setText("GRAVITY TYPE", 52.5f)

        _scaleButton!!.setOnClickListener(object : OnClickListener{
            override fun onClick(view: SMView?) {
                if (_imageButtonGravityType) {
                    _imageButtonGravityType = false
                    setGravityButtonState()
                }
            }
        })
        _gravityButton!!.setOnClickListener(object : OnClickListener{
            override fun onClick(view: SMView?) {
                if (_imageButtonGravityType) {
                    _imageButtonGravityType = false
                    setGravityButtonState()
                }
            }
        })
        _imageButtonGravityType = false

        _scaleBG = create(getDirector(), 0, 0f, buttonSize, s.width, bgHeight-buttonSize)
        _scaleBG!!.setBackgroundColor(Color4F.WHITE)
        menuBg.addChild(_scaleBG)
        _gravitiBG = create(getDirector(), 0, 0f, buttonSize, s.width, bgHeight-buttonSize)
        _gravitiBG!!.setBackgroundColor(Color4F.WHITE)
        menuBg.addChild(_gravitiBG)

        setGravityButtonState()

        var posY = 15f
        var buttonWidth = s.width/2f - 45f

        val scaleBtns: ArrayList<SMButton> = ArrayList()

        val centerBtn = SMButton.create(getDirector(), 0, SMButton.STYLE.SOLID_ROUNDEDRECT, 22f, posY+7f, buttonWidth, buttonSize-15f)
        centerBtn.setText("CENTER", fontSize)
        scaleBtns.add(centerBtn)

        posY += buttonSize+30f
        val centerInsideBtn = SMButton.create(getDirector(), 1, SMButton.STYLE.SOLID_ROUNDEDRECT, 22f, posY+7f, buttonWidth, buttonSize-15f)
        centerInsideBtn.setText("CENTER INSIDE", fontSize)
        scaleBtns.add(centerInsideBtn)

        posY += buttonSize+30f
        val centerCropButton = SMButton.create(getDirector(), 2, SMButton.STYLE.SOLID_ROUNDEDRECT, 22f, posY+7f, buttonWidth, buttonSize-15f)
        centerCropButton.setText("CENTER CROP", fontSize)
        scaleBtns.add(centerCropButton)

        posY = 15f
        val fitXYBtn = SMButton.create(getDirector(), 3, SMButton.STYLE.SOLID_ROUNDEDRECT, s.width/2f+22f, posY+7f, buttonWidth, buttonSize-15f)
        fitXYBtn.setText("FIT XY", fontSize)
        scaleBtns.add(fitXYBtn)

        posY += buttonSize+30f
        val fitCenterBtn = SMButton.create(getDirector(), 4, SMButton.STYLE.SOLID_ROUNDEDRECT, s.width/2f+22f, posY+7f, buttonWidth, buttonSize-15f)
        fitCenterBtn.setText("FIT CENTER", fontSize)
        scaleBtns.add(fitCenterBtn)

        for (i in 0 until scaleBtns.size) {
            val btn = scaleBtns[i]
            btn.setButtonColor(STATE.NORMAL, Color4F.WHITE)
            btn.setButtonColor(STATE.PRESSED, Color4F.XEEEFF1)

            btn.setOutlineColor(STATE.NORMAL, Color4F.XADAFB3)
            btn.setOutlineColor(STATE.PRESSED, Color4F.WHITE)

            btn.setOutlineWidth(ShaderNode.DEFAULT_ANTI_ALIAS_WIDTH*1.5f)
            btn.setShapeCornerRadius(buttonSize/2f)

            btn.setTextColor(STATE.NORMAL, Color4F.XADAFB3)
            btn.setTextColor(STATE.PRESSED, Color4F.WHITE)

            btn.setOnClickListener(object : OnClickListener {
                override fun onClick(view: SMView?) {
                    when (view!!.getTag()) {
                        1 -> {
                            _mainImageView!!.setScaleType(SMImageView.ScaleType.CENTER_INSIDE)
                        }
                        2 -> {
                            _mainImageView!!.setScaleType(SMImageView.ScaleType.CENTER_CROP)
                        }
                        3 -> {
                            _mainImageView!!.setScaleType(SMImageView.ScaleType.FIT_XY)
                        }
                        4 -> {
                            _mainImageView!!.setScaleType(SMImageView.ScaleType.FIT_CENTER)
                        }
                        else -> {
                            _mainImageView!!.setScaleType(SMImageView.ScaleType.CENTER)
                        }
                    }
                }
            })
            _scaleBG?.addChild(btn)
        }

        posY = 15f
        buttonWidth = s.width/3f-45f

        val gravityBtns: ArrayList<SMButton> = ArrayList()
        val LT = SMButton.create(getDirector(), 0, SMButton.STYLE.SOLID_ROUNDEDRECT, 22f, posY + 8f, buttonWidth, buttonSize + 15f)
        LT.setText("LT", fontSize)
        gravityBtns.add(LT)

        posY += buttonSize+30f
        val LC = SMButton.create(getDirector(), 1, SMButton.STYLE.SOLID_ROUNDEDRECT, 22f, posY + 8f, buttonWidth, buttonSize + 15f)
        LC.setText("LC", fontSize)
        gravityBtns.add(LC)

        posY += buttonSize+30f
        val LB = SMButton.create(getDirector(), 2, SMButton.STYLE.SOLID_ROUNDEDRECT, 22f, posY + 8f, buttonWidth, buttonSize + 15f)
        LB.setText("LB", fontSize)
        gravityBtns.add(LB)

        posY = 15f
        val CT = SMButton.create(getDirector(), 3, SMButton.STYLE.SOLID_ROUNDEDRECT, s.width/3f + 22f, posY + 8f, buttonWidth, buttonSize + 15f)
        CT.setText("CT", fontSize)
        gravityBtns.add(CT)

        posY += buttonSize+30f
        val CC = SMButton.create(getDirector(), 4, SMButton.STYLE.SOLID_ROUNDEDRECT, s.width/3f + 22f, posY + 8f, buttonWidth, buttonSize + 15f)
        CC.setText("CC", fontSize)
        gravityBtns.add(CC)

        posY += buttonSize+30f
        val CB = SMButton.create(getDirector(), 5, SMButton.STYLE.SOLID_ROUNDEDRECT, s.width/3f + 22f, posY + 8f, buttonWidth, buttonSize + 15f)
        CB.setText("CB", fontSize)
        gravityBtns.add(CB)

        posY = 15f
        val RT = SMButton.create(getDirector(), 6, SMButton.STYLE.SOLID_ROUNDEDRECT, s.width/3f*2f + 22f, posY + 8f, buttonWidth, buttonSize + 15f)
        RT.setText("RT")
        gravityBtns.add(RT)

        posY += buttonSize+30f
        val RC = SMButton.create(getDirector(), 7, SMButton.STYLE.SOLID_ROUNDEDRECT, s.width/3f*2f + 22f, posY + 8f, buttonWidth, buttonSize + 15f)
        RC.setText("RC")
        gravityBtns.add(RC)

        posY += buttonSize+30f
        val RB = SMButton.create(getDirector(), 8, SMButton.STYLE.SOLID_ROUNDEDRECT, s.width/3f*2f + 22f, posY + 8f, buttonWidth, buttonSize + 15f)
        RB.setText("RB")
        gravityBtns.add(RB)

        for (i in 0 until gravityBtns.size) {
            val btn = gravityBtns[i]

            btn.setButtonColor(STATE.NORMAL, Color4F.WHITE)
            btn.setButtonColor(STATE.PRESSED, Color4F.XEEEFF1)

            btn.setOutlineColor(STATE.NORMAL, Color4F.XADAFB3)
            btn.setOutlineColor(STATE.PRESSED, Color4F.WHITE)

            btn.setOutlineWidth(ShaderNode.DEFAULT_ANTI_ALIAS_WIDTH*1.5f)
            btn.setShapeCornerRadius(buttonSize/2f)

            btn.setTextColor(STATE.NORMAL, Color4F.XADAFB3)
            btn.setTextColor(STATE.PRESSED, Color4F.WHITE)

            btn.setOnClickListener(object : OnClickListener {
                override fun onClick(view: SMView?) {
                    when (view!!.getTag()) {
                        1 -> {
                            _mainImageView!!.setGravity(SMImageView.GRAVITY_LEFT or SMImageView.GRAVITY_CENTER_VERTICAL)
                        }
                        2 -> {
                            _mainImageView!!.setGravity(SMImageView.GRAVITY_LEFT or SMImageView.GRAVITY_BOTTOM)
                        }
                        3 -> {
                            _mainImageView!!.setGravity(SMImageView.GRAVITY_CENTER_HORIZONTAL or SMImageView.GRAVITY_TOP)
                        }
                        4 -> {
                            _mainImageView!!.setGravity(SMImageView.GRAVITY_CENTER_HORIZONTAL or SMImageView.GRAVITY_CENTER_VERTICAL)
                        }
                        5 -> {
                            _mainImageView!!.setGravity(SMImageView.GRAVITY_CENTER_HORIZONTAL or SMImageView.GRAVITY_BOTTOM)
                        }
                        6 -> {
                            _mainImageView!!.setGravity(SMImageView.GRAVITY_RIGHT or SMImageView.GRAVITY_TOP)
                        }
                        7 -> {
                            _mainImageView!!.setGravity(SMImageView.GRAVITY_RIGHT or SMImageView.GRAVITY_CENTER_VERTICAL)
                        }
                        8 -> {
                            _mainImageView!!.setGravity(SMImageView.GRAVITY_RIGHT or SMImageView.GRAVITY_BOTTOM)
                        }
                        else -> {
                            _mainImageView!!.setGravity(SMImageView.GRAVITY_LEFT or SMImageView.GRAVITY_TOP)
                        }
                    }
                }
            })
            _gravitiBG?.addChild(btn)
        }
    }

    fun setGravityButtonState() {
        if (_scaleButton!=null && _gravityButton!=null) {
            _mainImageView!!.setScaleType(SMImageView.ScaleType.CENTER)
            _mainImageView!!.setGravity(SMImageView.GRAVITY_CENTER_HORIZONTAL or SMImageView.GRAVITY_CENTER_VERTICAL)
            if (_imageButtonGravityType) {
                _gravityButton!!.setButtonColor(STATE.NORMAL, Color4F.WHITE)
                _gravityButton!!.setButtonColor(STATE.PRESSED, Color4F.WHITE)
                _gravityButton!!.setTextColor(STATE.NORMAL, Color4F.TEXT_BLACK)
                _gravityButton!!.setTextColor(STATE.PRESSED, Color4F.TEXT_BLACK)

                _scaleButton!!.setButtonColor(STATE.NORMAL, Color4F.XEEEFF1)
                _scaleButton!!.setButtonColor(STATE.PRESSED, Color4F.WHITE)
                _scaleButton!!.setTextColor(STATE.NORMAL, Color4F.XADAFB3)
                _scaleButton!!.setTextColor(STATE.PRESSED, Color4F.XDBDCDF)

                _scaleBG!!.setVisible(false)
                _gravitiBG!!.setVisible(true)
            } else {
                _scaleButton!!.setButtonColor(STATE.NORMAL, Color4F.WHITE)
                _scaleButton!!.setButtonColor(STATE.PRESSED, Color4F.WHITE)
                _scaleButton!!.setTextColor(STATE.NORMAL, Color4F.TEXT_BLACK)
                _scaleButton!!.setTextColor(STATE.PRESSED, Color4F.TEXT_BLACK)

                _gravityButton!!.setButtonColor(STATE.NORMAL, Color4F.XEEEFF1)
                _gravityButton!!.setButtonColor(STATE.PRESSED, Color4F.WHITE)
                _gravityButton!!.setTextColor(STATE.NORMAL, Color4F.XADAFB3)
                _gravityButton!!.setTextColor(STATE.PRESSED, Color4F.XDBDCDF)

                _scaleBG!!.setVisible(true)
                _gravitiBG!!.setVisible(false)
            }
        }
    }

    override fun onPageChangedCallback(view: SMPageView, page: Int) {

    }

    class CircularImageCell(director: IDirector): SMImageView(director) {

    }
}