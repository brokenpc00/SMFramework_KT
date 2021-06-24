package com.interpark.app.scene

import android.util.Log
import android.view.View
import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.SMView
import com.brokenpc.smframework.base.SceneParams
import com.brokenpc.smframework.base.scroller.SMScroller
import com.brokenpc.smframework.base.sprite.BitmapSprite
import com.brokenpc.smframework.base.sprite.Sprite
import com.brokenpc.smframework.base.types.*
import com.brokenpc.smframework.shader.ShaderNode
import com.brokenpc.smframework.util.AppConst
import com.brokenpc.smframework.util.ImageManager.IDownloadProtocol
import com.brokenpc.smframework.util.ImageManager.ImageDownloadTask
import com.brokenpc.smframework.view.*
import com.brokenpc.smframework_kt.scene.AniTestView
import com.interpark.app.menu.MenuBar
import com.interpark.app.scene.stickerLayer.*
import com.interpark.smframework.base.types.ICircularCell
import com.interpark.smframework.util.ImageProcess.ImageProcessProtocol
import com.interpark.smframework.util.ImageProcess.ImageProcessTask
import com.interpark.smframework.view.*
import com.interpark.smframework.view.Sticker.StickerCanvasView
import com.interpark.smframework.view.Sticker.StickerControlView
import com.interpark.smframework.view.Sticker.StickerItem
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.math.abs
import kotlin.math.floor

class ViewDisplayScene(director: IDirector): SMMenuTransitionScene(director), SMPageView.OnPageChangedCallback, StickerCanvasView.StickerCanvasListener, StickerControlView.StickerControlListener, StickerItemView.StickerLayoutListener, ItemListView.OnItemClickListener, IDownloadProtocol, ImageProcessProtocol {
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
    private var _waveBtn: SMButton? = null
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
    private val _pageItemCount = 10
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

    private val _downloadTask:ArrayList<ImageDownloadTask> = ArrayList()
    private val _mutex: Lock = ReentrantLock(true)

    // for animation 1
    private var _testView:AniTestView? = null

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

    private fun makeView() {
        _viewType = _sceneParam?.getInt("VIEW_TYPE")?:0

        when (_viewType) {
            1 -> {
                // zoom view
                zoomDisplay()
            }
            2 -> {
                pageViewDisplay()
            }
            3 -> {
                circularViewDisplay()
            }
            4 -> {
                tableViewDisplay()
            }
            5 -> {
                kenburnDisplay()
            }
            6 -> {
                ringWaveDisplay()
            }
            8 -> {
                stickerDisplay()
            }
            10 -> {
                animation1Display()
            }
            else -> {
                imageDisplay()
            }
        }
    }

    private fun imageDisplay() {
        val s = _contentView.getContentSize()

        val fontSize = 35f
        val padding = 30f
        val buttonSize = AppConst.SIZE.MENUBAR_HEIGHT - padding * 2f

        val bgHeight = buttonSize * 5f

        _mainImageView = SMImageView.create(getDirector(), "images/defaults2.jpg")
        if (_mainImageView==null) return
        _mainImageView!!.setContentSize(Size(s.width, s.height-bgHeight))
        _mainImageView!!.setPosition(Vec2.ZERO)
        _mainImageView!!.setBackgroundColor(1f, 0f, 0f, 0.4f)
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
                if (!_imageButtonGravityType) {
                    _imageButtonGravityType = true
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

        for (btn in scaleBtns) {
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
        RT.setText("RT", fontSize)
        gravityBtns.add(RT)

        posY += buttonSize+30f
        val RC = SMButton.create(getDirector(), 7, SMButton.STYLE.SOLID_ROUNDEDRECT, s.width/3f*2f + 22f, posY + 8f, buttonWidth, buttonSize + 15f)
        RC.setText("RC", fontSize)
        gravityBtns.add(RC)

        posY += buttonSize+30f
        val RB = SMButton.create(getDirector(), 8, SMButton.STYLE.SOLID_ROUNDEDRECT, s.width/3f*2f + 22f, posY + 8f, buttonWidth, buttonSize + 15f)
        RB.setText("RB", fontSize)
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

    private fun zoomDisplay() {
        val s = _contentView.getContentSize()

        _zoomView = SMZoomView.create(getDirector(), 0f, 0f, s.width, s.height)
        _contentView.addChild(_zoomView)

        val imageView = SMImageView.create(getDirector(), "images/bigsize.jpg")
        _zoomView?.setContentView(imageView)
    }

    private fun pageViewDisplay() {
        val s = _contentView.getContentSize()

        for (i in 0 until _pageItemCount) {
            val imgH = SMImageView.create(getDirector(), "images/bigsize.jpg")
            imgH.setContentSize(s.width, s.height/2f)
            imgH.setScaleType(SMImageView.ScaleType.FIT_CENTER)
            imgH.setScissorEnable(true)
            imgH.setBackgroundColor(1f, 1f, 0f, 0.6f)
            imgH.setTag(i)
            _horImages.add(imgH)

            val imgV = SMImageView.create(getDirector(), "images/bigsize.jpg")
            imgV.setContentSize(s.width, s.height/2f)
            imgV.setScaleType(SMImageView.ScaleType.FIT_CENTER)
            imgV.setBackgroundColor(0f, 1f, 1f, 0.6f)
            imgV.setTag(i)
            _verImages.add(imgV)
        }

        _horPageView = SMPageView.Companion.create(getDirector(), SMTableView.Orientation.HORIZONTAL, 0f, 0f, s.width, s.height/2f)
        _horPageView!!.numberOfRowsInSection = object : SMTableView.NumberOfRowsInSection {
            override fun numberOfRowsInSection(section: Int): Int {
                return _horImages.size
            }
        }
        _horPageView!!.cellForRowAtIndexPath = object : SMTableView.CellForRowAtIndexPath {
            override fun cellForRowAtIndexPath(indexPath: IndexPath): SMView {
                return _horImages[indexPath.getIndex()]
            }
        }
        _horPageView!!.setScissorEnable(true)
        _horPageView!!.setOnPageChangedCallback(this)
        _contentView.addChild(_horPageView!!)


        _verPageView = SMPageView.create(getDirector(), SMTableView.Orientation.VERTICAL, 0f, s.height/2f, s.width, s.height/2f)
        _verPageView!!.numberOfRowsInSection = object : SMTableView.NumberOfRowsInSection {
            override fun numberOfRowsInSection(section: Int): Int {
                return _verImages.size
            }
        }
        _verPageView!!.cellForRowAtIndexPath = object : SMTableView.CellForRowAtIndexPath {
            override fun cellForRowAtIndexPath(indexPath: IndexPath): SMView {
                return _verImages[indexPath.getIndex()]
            }
        }
        _verPageView!!.setScissorEnable(true)
        _verPageView!!.setOnPageChangedCallback(this)
        _contentView.addChild(_verPageView)

        layoutPageLabel()
    }

    private fun layoutPageLabel() {
        if (_horLabel==null) {
            _horLabel = SMLabel.create(getDirector(), "", 52f, Color4F(1f, 0f, 0f, 1f))
            _horLabel!!.setAnchorPoint(Vec2.MIDDLE)
            _horLabel!!.setPosition(_contentView.getContentSize().width/2f, _contentView.getContentSize().height/2f - 225f)
            _horLabel!!.setLocalZOrder(999)
            _contentView.addChild(_horLabel)
        }

        if (_verLabel==null) {
            _verLabel = SMLabel.Companion.create(getDirector(), "", 52f, Color4F(1f, 0f, 0f, 1f))
            _verLabel!!.setAnchorPoint(Vec2.MIDDLE)
            _verLabel!!.setPosition(_contentView.getContentSize().width/2f, _contentView.getContentSize().height - 225f)
            _verLabel!!.setLocalZOrder(999)
            addChild(_verLabel)
        }

        val horString = "Horizontal Paging ${_horPageView!!.getCurrentPage()+1} / ${_horImages.size} page"
        _horLabel!!.setText(horString)

        val verString = "Vertical Paging ${_verPageView!!.getCurrentPage()+1} / ${_verImages.size} page"
        _verLabel!!.setText(verString)
    }

    private fun circularViewDisplay() {
        val s = _contentView.getContentSize()

        val pageSize = s.height
        val listViewSize = Size(s.width+PAGER_PADDING*2f, pageSize)
        for (i in 0..4) {
            val cell = CircularImageCellCreate(getDirector(), "images/defaults.jpg")
            cell.setContentSize(Size(s.width, pageSize))
            _circularImages.add(cell)
        }

        _circularOrientation = SMCircularListView.Orientation.HORIZONTAL
        _circularScrollMode = SMScroller.ScrollMode.PAGER
        _isCircular = true

        _circularConfig = SMCircularListView.Config()
        _circularConfig!!.orient = _circularOrientation
        _circularConfig!!.scrollMode = _circularScrollMode
        _circularConfig!!.circular = _isCircular
        _circularConfig!!.cellSize = s.width
        _circularConfig!!.windowSize = s.width + PAGER_PADDING*2
        _circularConfig!!.anchorPosition = PAGER_PADDING
        _circularConfig!!.maxVelocity = 5000f
        _circularConfig!!.minVelocity = 5000f
        _circularConfig!!.preloadPadding = 0f

        _circularListview = SMCircularListView.create(getDirector(), _circularConfig!!)
        _contentView.addChild(_circularListview)
        _circularListview!!.setContentSize(listViewSize)
        _circularListview!!.setPositionX(-PAGER_PADDING)
        _circularListview!!.cellForRowsAtIndex = object : SMCircularListView.CellForRowsAtIndex {
            override fun cellForRowsAtIndex(index: Int): SMView {
                val cell = _circularImages[index]
                cell.setTag(index)
                return cell
            }
        }
        _circularListview!!.numberOfRows = object : SMCircularListView.NumberOfRows {
            override fun numberOfRows(): Int {
                return _circularImages.size
            }
        }
        _circularListview!!.positionCell = object : SMCircularListView.PositionCell {
            override fun positionCell(cell: SMView, position: Float, created: Boolean) {
                cell.setPositionX(position)
            }
        }
        _circularListview!!.pageScrollCallback = object : SMCircularListView.PageScrollCallback {
            override fun pageScrollCallback(pagePosition: Float) {
                layoutCircularLabel(pagePosition)
            }
        }
    }

    fun layoutCircularLabel(pagePosition: Float) {
        if (_circularLabel==null) {
            _circularLabel = SMLabel.Companion.create(getDirector(), "", 67f, Color4F(1f, 0f, 0f, 1f))
            _circularLabel!!.setAnchorPoint(Vec2.MIDDLE)
            _circularLabel!!.setPosition(_contentView.getContentSize().width/2f, _circularListview!!.getContentSize().height-275f)
            _circularLabel!!.setLocalZOrder(999)
            _contentView.addChild(_circularLabel!!)
        }

        val pageNo = (floor(pagePosition+0.5f) % _circularImages.size).toInt()
        val desc = "Circular paging ${pageNo+1}/${_circularImages.size} page"
        _circularLabel!!.setText(desc)
    }

    override fun onPageChangedCallback(view: SMPageView, page: Int) {
        if (_viewType==2) {
            layoutPageLabel()
        } else if (_viewType==4) {
            layoutTableViewLabel()
        }
    }

    private fun CircularImageCellCreate(director: IDirector, assetName: String): CircularImageCell {
        val cell = CircularImageCell(director, assetName)
        if (cell.getContentSize().width==0f && cell.getContentSize().height==0f) {
            if (cell.getSprite()!=null) {
                cell.setContentSize(cell.getSprite()!!.getWidth(), cell.getSprite()!!.getHeight())
            }
        }

        return cell
    }

    class CircularImageCell(director: IDirector, assetName: String): SMImageView(director, assetName), ICircularCell {
        var _index = 0
        var _deleted = false
        var _cellPosition = 0f
        var _aniSrc = 0f
        var _aniDst = 0f
        var _aniIndex = 0
        var _reuseIndentifier = ""

        override fun getCellIndex(): Int {return _index}
        override fun getCellPosition(): Float {return _cellPosition}
        override fun getCellIdentifier(): String {return _reuseIndentifier}
        override fun markDelete() {_deleted = true}
        override fun setCellIndex(index: Int) {_index = index}
        override fun setCellPosition(position: Float) {_cellPosition = position}
        override fun setReuseIdentifier(identifier: String) {_reuseIndentifier = identifier}
        override fun setAniSrc(src: Float) {_aniSrc = src}
        override fun setAniDst(dst: Float) {_aniDst = dst}
        override fun setAniIndex(index: Int) {_aniIndex = index}
        override fun isDeleted(): Boolean {return _deleted}
        override fun getAniSrc(): Float {return _aniSrc}
        override fun getAniDst(): Float {return _aniDst}
        override fun getAniIndex(): Int {return _aniIndex}
    }

    fun tableViewDisplay() {
        val s = _contentView.getContentSize()

        _tableBgViews = ArrayList()
        for (i in 0..4) {
            val bgView = SMView.create(getDirector(), 0, 0f, 0f, s.width, s.height)
            bgView.setBackgroundColor(getRandomColor4F())
            _tableBgViews!!.add(bgView)

            val tableView = SMTableView.createMultiColumn(getDirector(), SMTableView.Orientation.VERTICAL, i+1, 0f, 0f, s.width, s.height)
            bgView.addChild(tableView)

            tableView!!.numberOfRowsInSection = object : SMTableView.NumberOfRowsInSection {
                override fun numberOfRowsInSection(section: Int): Int {
                    return 100
    }
    }
            when (i) {
                0 -> {
                    _tableView1 = tableView
                    tableView.cellForRowAtIndexPath = object : SMTableView.CellForRowAtIndexPath {
                        override fun cellForRowAtIndexPath(indexPath: IndexPath): SMView {
                            val s = _tableView1!!.getContentSize()
                            val cellID = "CELL${indexPath.getIndex()}"
                            var cell = _tableView1!!.dequeueReusableCellWithIdentifier(cellID)
                            if (cell==null) {
                                val height = randomInt(75, 450).toFloat()
                                cell = create(getDirector(), i, 0f, 0f, s.width, height)
                                val r = getRandomColorF()
                                val g = getRandomColorF()
                                val b = getRandomColorF()
                                cell!!.setBackgroundColor(Color4F(r, g, b, 1f))

                                val textColor = Color4F(abs(1-r), abs(1-g), abs(1-b), 1f)
                                val label = SMLabel.create(getDirector(), cellID, 52f, textColor)
                                label.setAnchorPoint(Vec2.MIDDLE)
                                label.setPosition(cell!!.getContentSize().divide(2f))
                                cell!!.addChild(label)
                            }
                            return cell
                        }
                    }
                }
                1 -> {
                    _tableView2 = tableView
                    tableView.cellForRowAtIndexPath = object : SMTableView.CellForRowAtIndexPath {
                        override fun cellForRowAtIndexPath(indexPath: IndexPath): SMView {
                            val s = _tableView2!!.getContentSize()
                            val cellID = "CELL${indexPath.getIndex()}"
                            var cell = _tableView2!!.dequeueReusableCellWithIdentifier(cellID)
                            if (cell==null) {
                                val height = randomInt(75, 450).toFloat()
                                cell = create(getDirector(), 0, 0f, 0f, s.width/2f, height)
                                val r = getRandomColorF()
                                val g = getRandomColorF()
                                val b = getRandomColorF()
                                cell!!.setBackgroundColor(Color4F(r, g, b, 1f))

                                val textColor = Color4F(abs(1-r), abs(1-g), abs(1-b), 1f)
                                val label = SMLabel.create(getDirector(), cellID, 52f, textColor)
                                label.setAnchorPoint(Vec2.MIDDLE)
                                label.setPosition(cell!!.getContentSize().divide(2f))
                                cell!!.addChild(label)
                            }
                            return cell!!
                        }
                    }
                }
                2 -> {
                    _tableView3 = tableView
                    tableView.cellForRowAtIndexPath = object : SMTableView.CellForRowAtIndexPath {
                        override fun cellForRowAtIndexPath(indexPath: IndexPath): SMView {
                            val s = _tableView3!!.getContentSize()
                            val cellID = "CELL${indexPath.getIndex()}"
                            var cell = _tableView3!!.dequeueReusableCellWithIdentifier(cellID)
                            if (cell==null) {
                                val height = randomInt(75, 450).toFloat()
                                cell = create(getDirector(), 0, 0f, 0f, s.width/3f, height)
                                val r = getRandomColorF()
                                val g = getRandomColorF()
                                val b = getRandomColorF()
                                cell!!.setBackgroundColor(Color4F(r, g, b, 1f))

                                val textColor = Color4F(abs(1-r), abs(1-g), abs(1-b), 1f)
                                val label = SMLabel.create(getDirector(), cellID, 52f, textColor)
                                label.setAnchorPoint(Vec2.MIDDLE)
                                label.setPosition(cell!!.getContentSize().divide(2f))
                                cell!!.addChild(label)
                            }
                            return cell!!
                        }
                    }
                }
                3 -> {
                    _tableView4 = tableView
                    tableView.cellForRowAtIndexPath = object : SMTableView.CellForRowAtIndexPath {
                        override fun cellForRowAtIndexPath(indexPath: IndexPath): SMView {
                            val s = _tableView4!!.getContentSize()
                            val cellID = "CELL${indexPath.getIndex()}"
                            var cell = _tableView4!!.dequeueReusableCellWithIdentifier(cellID)
                            if (cell==null) {
                                val height = randomInt(75, 450).toFloat()
                                cell = create(getDirector(), 0, 0f, 0f, s.width/4f, height)
                                val r = getRandomColorF()
                                val g = getRandomColorF()
                                val b = getRandomColorF()
                                cell!!.setBackgroundColor(Color4F(r, g, b, 1f))

                                val textColor = Color4F(abs(1-r), abs(1-g), abs(1-b), 1f)
                                val label = SMLabel.create(getDirector(), cellID, 52f, textColor)
                                label.setAnchorPoint(Vec2.MIDDLE)
                                label.setPosition(cell!!.getContentSize().divide(2f))
                                cell!!.addChild(label)
                            }
                            return cell!!
                        }
                    }
                }
                4 -> {
                    _tableView5 = tableView
                    tableView.cellForRowAtIndexPath = object : SMTableView.CellForRowAtIndexPath {
                        override fun cellForRowAtIndexPath(indexPath: IndexPath): SMView {
                            val s = _tableView5!!.getContentSize()
                            val cellID = "CELL${indexPath.getIndex()}"
                            var cell = _tableView5!!.dequeueReusableCellWithIdentifier(cellID)
                            if (cell == null) {
                                val height = randomInt(75, 450).toFloat()
                                cell = create(getDirector(), 0, 0f, 0f, s.width / 5f, height)
                                val r = getRandomColorF()
                                val g = getRandomColorF()
                                val b = getRandomColorF()
                                cell!!.setBackgroundColor(Color4F(r, g, b, 1f))

                                val textColor = Color4F(abs(1 - r), abs(1 - g), abs(1 - b), 1f)
                                val label = SMLabel.create(getDirector(), cellID, 52f, textColor)
                                label.setAnchorPoint(Vec2.MIDDLE)
                                label.setPosition(cell!!.getContentSize().divide(2f))
                                cell!!.addChild(label)
                            }
                            return cell!!
                        }
                    }
                }
            }
        }

        _tableContainView = SMPageView.Companion.create(getDirector(), SMTableView.Orientation.HORIZONTAL, 0f, 0f, s.width, s.height)
        _tableContainView!!.numberOfRowsInSection = object : SMTableView.NumberOfRowsInSection {
            override fun numberOfRowsInSection(section: Int): Int {
                return _tableBgViews!!.size
            }
        }
        _tableContainView!!.cellForRowAtIndexPath = object : SMTableView.CellForRowAtIndexPath {
            override fun cellForRowAtIndexPath(indexPath: IndexPath): SMView {
                return _tableBgViews!![indexPath.getIndex()]
            }
        }
        _tableContainView!!.setScissorEnable(true)
        _tableContainView!!.setOnPageChangedCallback(this)
        _contentView.addChild(_tableContainView!!)

        layoutTableViewLabel()
    }

    fun layoutTableViewLabel() {
        if (_tableViewLabel==null) {
            _tableViewLabel = SMLabel.Companion.create(getDirector(), "", 90f, Color4F.WHITE)
            _tableViewLabel!!.setAnchorPoint(Vec2.MIDDLE)
            _tableViewLabel!!.setPosition(_contentView.getContentSize().width/2f, _contentView.getContentSize().height - 300f)
            _tableViewLabel!!.setLocalZOrder(999)
            _contentView.addChild(_tableViewLabel)
        }

        val pagNo = _tableContainView!!.getCurrentPage()+1
        val desc = "TableView $pagNo / ${_tableBgViews!!.size} page"
        _tableViewLabel!!.setText(desc)
    }

    fun kenburnDisplay() {
        val s = _contentView.getContentSize()

        val imageList:ArrayList<String> = ArrayList()
        imageList.add("images/ken1.jpg")
        imageList.add("images/ken2.jpg")
        imageList.add("images/ken3.jpg")

        val view = SMKenBurnsView.createWithAssets(getDirector(), imageList)
        view.setContentSize(s)
        view.setBackgroundColor(Color4F.BLACK)
        view.startWithDelay(0.0f)
        _contentView.addChild(view)
    }

    fun ringWaveDisplay() {
        val s = _contentView.getContentSize()

        _buttunRect.set(Rect(60f, s.height-AppConst.SIZE.MENUBAR_HEIGHT-30f, s.width-120f, AppConst.SIZE.MENUBAR_HEIGHT))

        _waveBtn = SMButton.create(getDirector(), 0, SMButton.STYLE.SOLID_ROUNDEDRECT, _buttunRect.origin.x, _buttunRect.origin.y, _buttunRect.size.width, _buttunRect.size.height)
        _waveBtn!!.setShapeCornerRadius(AppConst.SIZE.MENUBAR_HEIGHT/2)
        _waveBtn!!.setOutlineWidth(ShaderNode.DEFAULT_ANTI_ALIAS_WIDTH*2)
        _waveBtn!!.setButtonColor(STATE.NORMAL, Color4F.WHITE)
        _waveBtn!!.setButtonColor(STATE.PRESSED, Color4F.XEEEFF1)
        _waveBtn!!.setOutlineColor(STATE.NORMAL, Color4F.XDBDCDF)
        _waveBtn!!.setOutlineColor(STATE.PRESSED, Color4F.XADAFB3)
        _waveBtn!!.setText("RING FULSE", 82f)
        _waveBtn!!.setTextColor(STATE.NORMAL, Color4F.TEXT_BLACK)
        _waveBtn!!.setTextColor(STATE.PRESSED, Color4F.XADAFB3)
        _contentView.addChild(_waveBtn!!)

        _waveBtn!!.setOnClickListener(object : OnClickListener{
            override fun onClick(view: SMView?) {
                val s = _contentView.getContentSize()
                _ringFlag = !_ringFlag
                if (_ringView!=null) {
                    _contentView.removeChild(_ringView)
                    _ringView = null
                }
                if (_alarmCircle!=null) {
                    _contentView.removeChild(_alarmCircle)
                    _alarmCircle = null
                }

                if (_ringFlag) {
                    _waveBtn!!.setText("RING PULSE2")
                    _ringView = RingWave2.create(getDirector(), 120f, 150f)
                    _ringView!!.setAnchorPoint(Vec2.MIDDLE)
                    _ringView!!.setPosition(s.width/2f, s.height/2f-AppConst.SIZE.MENUBAR_HEIGHT/2f)
                    _ringView!!.setColor(getRandomColor4F())
                    _contentView.addChild(_ringView)

                    val src = Rect(_buttunRect)
                    val dst = Rect(60f, s.height-AppConst.SIZE.MENUBAR_HEIGHT-30f, s.width-120f, AppConst.SIZE.MENUBAR_HEIGHT)
                    val action = ViewTransitionActionCreate(getDirector(), view!!)
                    action.setValue(src, dst, 0.3f, 0.1f)
                    view.runAction(action)
                } else {
                    _waveBtn!!.setText("RING PULSE1")
                    val pulseColor = getRandomColor4F()
                    _alarmCircle = SMSolidCircleView.create(getDirector())

                    _alarmCircle!!.setContentSize(Size(105f, 105f))
                    _alarmCircle!!.setColor(pulseColor)
                    _alarmCircle!!.setAnchorPoint(Vec2.MIDDLE)
                    _alarmCircle!!.setPosition(s.width/2f, s.height/2f - AppConst.SIZE.MENUBAR_HEIGHT/2f)
                    _contentView.addChild(_alarmCircle)

                    _alarmCircle!!.setAlpha(0f)
                    _alarmCircle!!.stopAllActions()

                    val a = TransformAction.create(getDirector())
                    a.toAlpha(1f).setTimeValue(0.2f, 0f)
                    _alarmCircle!!.runAction(a)

                    val size = _alarmCircle!!.getContentSize()
                    RingWave.show(getDirector(), _alarmCircle!!, size.width/2f, size.height/2f, 525f, 0.6f, 0.1f, pulseColor, true)

                    val src = Rect(60f, s.height-AppConst.SIZE.MENUBAR_HEIGHT-30f, s.width-120f, AppConst.SIZE.MENUBAR_HEIGHT)
                    val dst = Rect(_buttunRect)
                    val action = ViewTransitionActionCreate(getDirector(), view!!)
                    action.setValue(src, dst, 0.3f, 0.1f)
                    view.runAction(action)
                }
            }
        })
    }

    fun stickerDisplay() {
        val s = _contentView.getContentSize()

        // zoom layer
        val borderSize = Size(s.width, s.height-AppConst.SIZE.BOTTOM_MENU_HEIGHT)
        _stickerLayer = StickerLayer.create(getDirector(), BitmapSprite.createFromAsset(getDirector(), "images/defaults.jpg", false, null), borderSize)
        _stickerLayer!!.setAnchorPoint(Vec2.MIDDLE)
        _stickerLayer!!.setPosition(s.width/2f, borderSize.height/2f)
        _stickerLayer!!.setStickerListener(this, this)
        _contentView.addChild(_stickerLayer)

        _stickerMenuView = create(getDirector(), 0, 0f, s.height-AppConst.SIZE.BOTTOM_MENU_HEIGHT, s.width, AppConst.SIZE.BOTTOM_MENU_HEIGHT)
        _stickerMenuView!!.setBackgroundColor(Color4F.WHITE)
        _contentView.addChild(_stickerMenuView)
        _stickerMenuView!!.setLocalZOrder(10)

        val bottomUpperLine = create(getDirector(), 0, 0f, 0f, s.width, ShaderNode.DEFAULT_ANTI_ALIAS_WIDTH*1.5f)
        bottomUpperLine.setBackgroundColor(Color4F.XADAFB3)
        _stickerMenuView!!.addChild(bottomUpperLine)
        bottomUpperLine.setLocalZOrder(10)

        _stickerListView = StickerItemListView.create(getDirector())
        _stickerListView!!.setOnItemClickListener(this)
        _stickerMenuView!!.addChild(_stickerListView)

        val action = _stickerListView!!.getActionByTag(AppConst.TAG.USER+1)
        action?.stop()

        _stickerListView!!.setVisible(true)
    }

    override fun onStickerTouch(view: SMView?, action: Int) {

    }

    override fun onStickerSelected(view: SMView?, select: Boolean) {

    }

    override fun onStickerRemoveEnd(view: SMView?) {

    }

    override fun onStickerRemoveBegin(view: SMView?) {

    }

    override fun onStickerMenuClick(sticker: SMView?, menuId: Int) {
        if (sticker is StickerItemView) {
            sticker.prepareRemove()

            _stickerLayer?.startGeineRemove(sticker)
        }
    }

    override fun onStickerDoubleClicked(view: SMView?, worldPoint: Vec2) {

    }

    override fun onStickerLayout(itemView: StickerItemView, sprite: Sprite?, item: StickerItem, colorIndex: Int) {

    }

    override fun onItemClick(sender: ItemListView, view: StickerItemThumbView) {
        runSelectSticker(view.getTag())
    }

    override fun resetDownload() {
        _mutex.withLock {
            for (task in _downloadTask) {
                if (task.isTargetAlive()) {
                    if (task.isRunning()) {
                        task.interrupt()
                    }
                }
            }

            _downloadTask.clear()
        }
    }

    override fun removeDownloadTask(task: ImageDownloadTask?) {
        _mutex.withLock {
            val iter = _downloadTask.iterator()
            while (iter.hasNext()) {
                val t = iter.next()
                if (!t.isTargetAlive()) {
                    _downloadTask.remove(t)
                } else if (task!=null && (t==task || task.getCacheKey().compareTo(t.getCacheKey())==0)) {
                    task.interrupt()
                    _downloadTask.remove(t)
                }
            }
        }
    }

    override fun onImageLoadStart(state: IDownloadProtocol.DownloadStartState) {
        TODO("Not yet implemented")
    }

    override fun onImageLoadComplete(sprite: Sprite?, tag: Int, direct: Boolean) {
        TODO("Not yet implemented")
    }

    override fun onImageCacheComplete(success: Boolean, tag: Int) {
        TODO("Not yet implemented")
    }

    override fun onDataLoadStart() {
        TODO("Not yet implemented")
    }

    override fun onDataLoadComplete(data: ByteArray, size: Int, tag: Int) {
        TODO("Not yet implemented")
    }

    override fun isDownloadRunning(requestPath: String, requestTag: Int): Boolean {
        _mutex.withLock {
            for (t in _downloadTask) {
                if (t.getRequestPath().compareTo(requestPath)==0 && t.getTag()==requestTag) {
                    return true
                }
            }
            return false
        }
    }

    override fun addDownloadTask(task: ImageDownloadTask?): Boolean {
        _mutex.withLock {
            val iter = _downloadTask.iterator()
            while (iter.hasNext()) {
                val t = iter.next()
                if (!t.isTargetAlive()) {
                    _downloadTask.remove(t)
                } else if (task!=null && t.isRunning() && (t==task || task.getCacheKey().compareTo(t.getCacheKey())==0)) {
                    return false
                }
            }

            _downloadTask.add(task!!)
            return true
        }
    }

    override var _imageProcessTask: ArrayList<ImageProcessTask>
        get() = TODO("Not yet implemented")
        set(value) {

        }

    override fun resetImageProcess() {
        val iter = _imageProcessTask.iterator()
        while (iter.hasNext()) {
            val task = iter.next()
            if (task.isRunning()) {
                task.interrupt()
            }
        }

        _imageProcessTask.clear()
    }

    override fun removeImageProcessTask(task: ImageProcessTask) {
        val iter = _imageProcessTask.iterator()
        while (iter.hasNext()) {
            val t = iter.next()
            if (!t.isTargetAlive()) {
                _imageProcessTask.remove(t)
            } else if (t==task) {
                task.interrupt()
                _imageProcessTask.remove(t)
            }
        }
    }

    override fun addImageProcessTask(task: ImageProcessTask): Boolean {
        val iter = _imageProcessTask.iterator()
        while (iter.hasNext()) {
            val t = iter.next()
            if (!t.isTargetAlive()) {
                _imageProcessTask.remove(t)
            } else if (task==t && t.isRunning()) {
                return false
            }
        }
        _imageProcessTask.add(task)
        return true
    }

    private fun runSelectSticker(index: Int): StickerItemView? {
        return runSelectSticker(index, false)
    }

    private fun runSelectSticker(index: Int, fromTemplate: Boolean): StickerItemView? {
        return runSelectSticker(index, fromTemplate, 0)
    }

    private fun runSelectSticker(index: Int, fromTemplate: Boolean, colorIndex: Int): StickerItemView? {
        return runSelectSticker(index, fromTemplate, colorIndex, -1)
    }

    private fun runSelectSticker(index: Int, fromTemplate: Boolean, colorIndex: Int, code: Int): StickerItemView? {
        if (index==0 && code<0) {
            _stickerLayer?.removeAllStickerWithFly()
        } else {
            var sticker: StickerItemView? = null
            val item = _stickerListView!!.getItem(index)
            if (item!=null) {
                sticker = StickerItemView.createWithItem(getDirector(), item, this)
                _stickerLayer?.addSticker(sticker)
            }
            if (sticker!=null) {
                sticker.setPosition(_stickerLayer!!.getContentSize().divide(2f))
                _stickerLayer!!.getCanvas().setSelectedSticker(sticker)
            }
            return sticker
        }

        return null
    }

    fun animation1Display() {
        Log.i("SMFRAMEWORK", "animation1Display")
//        _testView = AniTestView.show(getDirector(), this)
        val s = _contentView.getContentSize()
        _testView = AniTestView.create(getDirector(), 0f, 0f, s.width, s.height, null)
        _contentView.addChild(_testView)
    }

//    override fun onExit() {
//        if (_testView?.getParent()!=null) {
//            _testView?.removeFromParent()
//        }
//        super.onExit()
//    }
}