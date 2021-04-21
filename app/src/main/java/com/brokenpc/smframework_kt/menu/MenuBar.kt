package com.interpark.app.menu

import android.graphics.Paint
import android.view.MotionEvent
import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.SMView
import com.brokenpc.smframework.base.types.*
import com.brokenpc.smframework.util.AppConst
import com.brokenpc.smframework.util.tweenfunc
import com.brokenpc.smframework.view.SMButton
import com.brokenpc.smframework.view.SMImageView
import com.brokenpc.smframework.view.SMLabel
import com.brokenpc.smframework.view.SMSolidCircleView
import com.interpark.smframework.view.RingWave
import com.interpark.smframework.view.SMRoundLine
import com.interpark.smframework.view.SMToastBar

class MenuBar(director: IDirector): SMView(director), SMView.OnClickListener {

    private var _showAction = false
    private var _show = false
    private var _up = false
    private var _from = 0f
    private var _to = 0f
    private lateinit var _menuBar:MenuBar
    private val sDotMenu:ArrayList<DotPosition> = ArrayList(4)
    private val sDotClose:ArrayList<DotPosition> = ArrayList(4)
    private val sDotBack:ArrayList<DotPosition> = ArrayList(4)
    private val sDotDot:ArrayList<DotPosition> = ArrayList(4)

    inner class DotPosition {
        var _from = Vec2(Vec2.ZERO)
        var _to = Vec2(Vec2.ZERO)
        var _diameter = 0f

        constructor(pos: DotPosition) {
            _from.set(pos._from)
            _to.set(pos._to)
            _diameter = pos._diameter
        }

        constructor(from: Vec2, to: Vec2, diameter: Float) {
            _from.set(from)
            _to.set(to)
            _diameter = diameter
        }
    }

    enum class DropDown {
        NOTHING,
        UP,
        DOWN
    }

    enum class MenuType {
        NONE,
        DROPDOWN,

        MENU,
        BACK,
        CLOSE,
        DOT,
        CLOSE2,
        ALARM,

        SEARCH,
        CONFIRM,
        DELETE,
        CAMERA,
        ALBUM,
        CART,
        SETTINGS,

        NEXT,
        DONE,
        CANCEL,
        CROP,
        CLEAR
    }

    enum class TextTransition {
        FADE,
        ELASTIC,
        SWIPE
    }

    enum class ButtonTransition {
        FADE,
        ELASTIC,
        SWIPE
    }

    companion object {


        @JvmStatic
        fun menuTypeToInt(type: MenuType): Int {
            return when(type) {
                MenuType.DROPDOWN -> 0x100
                MenuType.MENU -> 0x1000
                MenuType.BACK -> 0x1001
                MenuType.CLOSE -> 0x1002
                MenuType.DOT -> 0x1003
                MenuType.CLOSE2 -> 0x1004
                MenuType.ALARM -> 0x1005
                MenuType.SEARCH -> 0x2000
                MenuType.CONFIRM -> 0x2001
                MenuType.DELETE -> 0x2002
                MenuType.CAMERA -> 0x2003
                MenuType.ALBUM -> 0x2004
                MenuType.CART -> 0x2005
                MenuType.SETTINGS -> 0x2006
                MenuType.NEXT -> 0x3000
                MenuType.DONE -> 0x3001
                MenuType.CANCEL -> 0x3002
                MenuType.CROP -> 0x3003
                MenuType.CLEAR -> 0x3004
                else -> 0
            }
        }

        @JvmStatic
        fun intToMenuType(value: Int): MenuType {
            return when (value) {
                 0x100 -> MenuType.DROPDOWN
                 0x1000 -> MenuType.MENU
                 0x1001 -> MenuType.BACK
                 0x1002 -> MenuType.CLOSE
                 0x1003 -> MenuType.DOT
                 0x1004 -> MenuType.CLOSE2
                 0x1005 -> MenuType.ALARM
                 0x2000 -> MenuType.SEARCH
                 0x2001 -> MenuType.CONFIRM
                 0x2002 -> MenuType.DELETE
                 0x2003 -> MenuType.CAMERA
                 0x2004 -> MenuType.ALBUM
                 0x2005 -> MenuType.CART
                 0x2006 -> MenuType.SETTINGS
                 0x3000 -> MenuType.NEXT
                 0x3001 -> MenuType.DONE
                 0x3002 -> MenuType.CANCEL
                 0x3003 -> MenuType.CROP
                 0x3004 -> MenuType.CLEAR
                else -> MenuType.NONE
            }
        }

        @JvmStatic
        fun create(director: IDirector): MenuBar {
            val menu = MenuBar(director)
            menu.init()
            return menu
        }
    }

    protected var _contentView: SMView? = null
    protected var _dropDownButton: SMImageView? = null
    protected lateinit var _textContainer: TextContainer

    protected lateinit var _buttonContainer: SMView
    protected lateinit var _mainButton: SMButton
    protected var _menuButtons: ArrayList<ArrayList<SMButton?>> = ArrayList(2)
    protected var _menuLine: ArrayList<SMRoundLine> = ArrayList(4)
    protected var _menuCircle: ArrayList<SMSolidCircleView> = ArrayList(4)
    protected var _menuImage: SMImageView? = null
    protected var _alarmCircle: SMSolidCircleView? = null
    protected var _menuTransform: MenuTransform? = null
    protected var _textTransform: TextTransform? = null
    protected var _colorTransform: ColorTransform? = null
    protected var _menuButtonType: MenuType = MenuType.NONE
    protected var _listener: MenuBarListener? = null
    protected var _colorSet = ColorSet(ColorSet.NONE)
    protected var _activeColorSet = ColorSet(ColorSet.NONE)
    protected var _textLabel: ArrayList<SMLabel?> = ArrayList(2)
    protected var _textString:String = ""
    protected var _textIndex = 0
    protected var _buttonIndex= 0
    protected var _dropdown = DropDown.NOTHING
    protected var _dropdownAction: DropwDownAction? = null
    protected var _textTransType: TextTransition? = null
    protected var _buttonTransType: ButtonTransition? = null
    protected var _overlapChild: SMView? = null
    protected var _toast: SMToastBar? = null
    protected var _newAlarm: Boolean = false
    private val MenuButtonCenter = Vec2(AppConst.SIZE.TOP_MENU_BUTTONE_SIZE/2f, AppConst.SIZE.TOP_MENU_BUTTONE_SIZE/2f)


    init {
        sDotMenu.add(DotPosition(Vec2(-26f, -26f), Vec2(-26f, -26f), AppConst.SIZE.DOT_DIAMETER))
        sDotMenu.add(DotPosition(Vec2(26f, 26f), Vec2(26f, 26f), AppConst.SIZE.DOT_DIAMETER))
        sDotMenu.add(DotPosition(Vec2(-26f, 26f), Vec2(-26f, 26f), AppConst.SIZE.DOT_DIAMETER))
        sDotMenu.add(DotPosition(Vec2(26f, -26f), Vec2(26f, -26f), AppConst.SIZE.DOT_DIAMETER))

        sDotClose.add(DotPosition(Vec2(-40f, -40f), Vec2.ZERO, AppConst.SIZE.LINE_DIAMETER))
        sDotClose.add(DotPosition(Vec2(40f, 40f), Vec2.ZERO, AppConst.SIZE.LINE_DIAMETER))
        sDotClose.add(DotPosition(Vec2(-40f, 40f), Vec2.ZERO, AppConst.SIZE.LINE_DIAMETER))
        sDotClose.add(DotPosition(Vec2(40f, -40f), Vec2.ZERO, AppConst.SIZE.LINE_DIAMETER))

        sDotBack.add(DotPosition(Vec2(-32f, -32f), Vec2.ZERO, AppConst.SIZE.LINE_DIAMETER))
        sDotBack.add(DotPosition(Vec2(32f, 32f), Vec2.ZERO, AppConst.SIZE.LINE_DIAMETER))
        sDotBack.add(DotPosition(Vec2(-32f, 32f), Vec2(-32f, -32f), AppConst.SIZE.LINE_DIAMETER))
        sDotBack.add(DotPosition(Vec2(32f, -32f), Vec2(-32f, -32f), AppConst.SIZE.LINE_DIAMETER))

        sDotDot.add(DotPosition(Vec2.ZERO, Vec2.ZERO, AppConst.SIZE.LINE_DIAMETER))
        sDotDot.add(DotPosition(Vec2.ZERO, Vec2.ZERO, AppConst.SIZE.LINE_DIAMETER))
        sDotDot.add(DotPosition(Vec2.ZERO, Vec2.ZERO, AppConst.SIZE.LINE_DIAMETER))
        sDotDot.add(DotPosition(Vec2.ZERO, Vec2.ZERO, AppConst.SIZE.LINE_DIAMETER))
    }

    override fun init(): Boolean {
        super.init()

        val s = getDirector().getWinSize()

        setContentSize(s.width, AppConst.SIZE.MENUBAR_HEIGHT)
        setAnchorPoint(Vec2.ZERO)
        setPosition(Vec2.ZERO)

        _textContainer = TextContainerCreate(getDirector())
        _textContainer.setTag(menuTypeToInt(MenuType.DROPDOWN))
        _textContainer.setContentSize(0f, AppConst.SIZE.MENUBAR_HEIGHT)
        _textContainer.setAnchorPoint(Vec2.MIDDLE)
        _textContainer.setPosition(s.width/2f, AppConst.SIZE.MENUBAR_HEIGHT/2f)
        addChild(_textContainer)

        for (i in 0 until 2) {
            _textLabel.add(null)
            val arr:ArrayList<SMButton?> = ArrayList()
            for (j in 0 until 2) {
                arr.add(null)
            }
            _menuButtons.add(arr)
        }

        _mainButton = SMButton.create(getDirector(), menuTypeToInt(MenuType.MENU), SMButton.STYLE.DEFAULT, 5f+AppConst.SIZE.TOP_MENU_BUTTONE_SIZE/2f, 5f+AppConst.SIZE.TOP_MENU_BUTTONE_SIZE/2f, AppConst.SIZE.TOP_MENU_BUTTONE_SIZE, AppConst.SIZE.TOP_MENU_BUTTONE_SIZE)
        _mainButton.setAnchorPoint(Vec2.MIDDLE)
        _mainButton.setButtonColor(STATE.NORMAL, MakeColor4F(0x222222, 1f))
        _mainButton.setButtonColor(STATE.PRESSED, MakeColor4F(0xadafb3, 1f))

        _buttonContainer = SMView.create(getDirector())
        _buttonContainer.setContentSize(AppConst.SIZE.TOP_MENU_BUTTONE_SIZE, AppConst.SIZE.TOP_MENU_BUTTONE_SIZE)
        _buttonContainer.setCascadeColorEnabled(true)
        _buttonContainer.setCascadeAlphaEnable(true)

        for (i in 0 until 4) {
            _menuLine.add(SMRoundLine.create(getDirector()))
            _menuLine[i].setAnchorPoint(Vec2.MIDDLE)
            _menuCircle.add(SMSolidCircleView.create(getDirector()))
            _buttonContainer.addChild(_menuLine[i])
            _buttonContainer.addChild(_menuCircle[i])

            _menuCircle[i].setPosition(sDotMenu[i]._from.add(MenuButtonCenter))
            _menuCircle[i].setContentSize(AppConst.SIZE.DOT_DIAMETER, AppConst.SIZE.DOT_DIAMETER)
            _menuCircle[i].setAnchorPoint(Vec2.MIDDLE)

        }

        _mainButton.setButton(STATE.NORMAL, _buttonContainer)
        _mainButton.setOnClickListener(this)
        _mainButton.setPushDownScale(0.9f)
        _mainButton.setPushDownOffset(Vec2(0f, -3f))
        addChild(_mainButton)

        return true
    }

    interface MenuBarListener {
        fun func1(view: SMView): Boolean    // onMenuBarClick
        fun func2() // onMenuBarTouch
    }

    fun setMenuButtonType(menuButtonType: MenuType, immediate: Boolean) {
        setMenuButtonType(menuButtonType, immediate, false)
    }

    fun setMenuButtonType(menuButtonType: MenuType, immediate: Boolean, swipe: Boolean) {
        if (_menuButtonType==menuButtonType) {
            _menuTransform?.setMenuType(_menuButtonType, menuButtonType, 0f)

            return
        }

        var to: ArrayList<DotPosition>

        when (menuButtonType) {
            MenuType.MENU -> {
                to = sDotMenu
                _mainButton.setTag(menuTypeToInt(MenuType.MENU))
            }
            MenuType.CLOSE, MenuType.CLOSE2 -> {
                to = sDotClose
                _mainButton.setTag(menuTypeToInt(MenuType.CLOSE))
            }
            MenuType.BACK -> {
                to = sDotBack
                _mainButton.setTag(menuTypeToInt(MenuType.BACK))
            }
            MenuType.ALARM -> {
                if (_menuImage==null) {
                    _menuImage = SMImageView.create(getDirector(), "images/ic_titlebar_notice.png")
                    _menuImage!!.setPosition(_buttonContainer.getContentSize().width/2f, _buttonContainer.getContentSize().height/2f)
                    _menuImage!!.setColor(Color4F.TEXT_BLACK)
                    _buttonContainer.addChild(_menuImage!!)
                }
                to = ArrayList(0)
                _mainButton.setTag(menuTypeToInt(MenuType.ALARM))

                _newAlarm = false
                showAlarmBadge()
            }
            else -> {
                return
            }
        }

        val action = getActionByTag(AppConst.TAG.ACTION_MENUBAR_MENU)
        if (action!=null) {
            stopAction(action)
        }

        if (_menuButtonType==MenuType.NONE || immediate) {
            if (menuButtonType==MenuType.ALARM) {
                for (i in 0 until 4) {
                    _menuLine[i].setVisible(false)
                    _menuCircle[i].setVisible(false)
                }

                _menuImage?.setVisible(true)
                _menuImage?.setScale(1f)
                showAlarmBadge()
            } else {
                for (i in 0 until 4) {
                    _menuLine[i].setLineWidth(to[i]._diameter)
                    _menuLine[i].line(to[i]._from.add(MenuButtonCenter), to[i]._to.add(MenuButtonCenter))

                    _menuLine[i].setVisible(menuButtonType!=MenuType.MENU)
                    _menuCircle[i].setVisible(menuButtonType==MenuType.MENU)

                    if (menuButtonType==MenuType.MENU) {
                        showAlarmBadge()
                    }
                }
            }

            val angle = when (menuButtonType) {
                MenuType.CLOSE -> 180f
                MenuType.CLOSE2 -> 90f
                MenuType.BACK -> 315f
                else -> 0f
            }

            _buttonContainer.setRotation(angle)
            _menuButtonType = menuButtonType
        }

        if (_menuTransform==null) {
            _menuTransform = MenuTransformCreate(getDirector())
            _menuTransform?.setMenuBar(this)
            _menuTransform?.setTag(AppConst.TAG.ACTION_MENUBAR_MENU)
        }

        _menuTransform?.setMenuType(_menuButtonType, menuButtonType, 0.45f)
        if (!swipe) {
            runAction(_menuTransform!!)
        }

        _menuButtonType = menuButtonType
    }

    fun getMenuButtonType(): MenuType {return _menuButtonType}

    fun setColorSet(colorSet: ColorSet, immediate: Boolean) {
        if (_colorSet.equal(colorSet)) return

        val action = getActionByTag(AppConst.TAG.ACTION_BG_COLOR)
        if (action!=null) {
            stopAction(action)
        }

        if (immediate) {
            _colorSet.set(colorSet)
            _activeColorSet.set(colorSet)

            applyColorSet(colorSet)
        } else {
            if (_colorTransform==null) {
                _colorTransform = ColorTransformCreate(getDirector())
                _colorTransform?.setTag(AppConst.TAG.ACTION_MENUBAR_COLOR)
            }

            _colorTransform?.setColorSet(colorSet)
            runAction(_colorTransform!!)
        }
    }

    fun setText(textString: String, immediate: Boolean) {
        setText(textString, immediate, false)
    }

    fun setText(textString: String, immediate: Boolean, dropdown: Boolean) {
        if (_textString==textString) return

        _textString = textString
        _textIndex = 1 - _textIndex

        if (_textLabel[_textIndex]==null) {
            _textLabel[_textIndex] = SMLabel.create(getDirector(), "", 80f, Color4F.TEXT_BLACK, Paint.Align.CENTER, true)
            _textLabel[_textIndex]?.setAnchorPoint(Vec2.MIDDLE)
            _textLabel[_textIndex]?.setCascadeAlphaEnable(true)
            _textContainer.stub[_textIndex].addChild(_textLabel[_textIndex]!!)
        }

        _textLabel[_textIndex]?.setText(textString)
        _textLabel[_textIndex]?.setColor(_activeColorSet.TEXT)
        _textLabel[_textIndex]?.setVisible(false)

        updateTextPosition(dropdown)

        val action = getActionByTag(AppConst.TAG.ACTION_MENUBAR_TEXT)
        if (action!=null) {
            stopAction(action)
        }

        if (immediate) {
            _textLabel[_textIndex]?.setVisible(true)
            if (_textLabel[1-_textIndex]!=null) {
                _textLabel[1-_textIndex]?.setVisible(false)
            }
            return
        }

        if (_textTransform==null) {
            _textTransform = TextTransformCreate(getDirector())
            _textTransform?.setTag(AppConst.TAG.ACTION_MENUBAR_TEXT)
            _textTransform?.setMenuBar(this)
        }

        if (_textTransType==TextTransition.ELASTIC) {
            _textTransform?.makeTextSeparate()
            _textTransform?.setElasticType()
        } else {
            _textTransform?.setFadeType()
        }

        // for elastic test
//        if (_textTransType==TextTransition.SWIPE) {
//            _textTransform?.makeTextSeparate()
//            _textTransform?.setElasticType()
//        }

        _textTransform?.setTextIndex(_textIndex)

        if (_textTransType!=TextTransition.SWIPE) {
            runAction(_textTransform!!)
        }
    }

    fun getText(): String {return _textString}

    fun setTextTransitionType(type: TextTransition) {_textTransType = type}

    fun setButtonTransitionType(type: ButtonTransition) {_buttonTransType = type}

    fun setTextWithDropDown(textString: String, immediate: Boolean) {
        setText(textString, immediate, true)
    }

    fun setOneButton(buttonType: MenuType, immediate: Boolean) {
        setOneButton(buttonType, immediate, false)
    }

    fun setOneButton(buttonType: MenuType, immediate: Boolean, swipe: Boolean) {
        setTwoButton(buttonType, MenuType.NONE, immediate, swipe)
    }

    fun setTwoButton(buttonType1: MenuType, buttonType2: MenuType, immediate: Boolean) {
        setTwoButton(buttonType1, buttonType2, immediate, false)
    }

    fun setTwoButton(buttonType1: MenuType, buttonType2: MenuType, imm: Boolean, swipe: Boolean) {
        var delay = 0f

        var immediate = imm

        val types:ArrayList<MenuType> = arrayListOf(buttonType1, buttonType2)

        if (!swipe) {
            for (i in 0 until 2) {
                val button = _menuButtons[_buttonIndex][i]
                if (button!=null && button.isVisible()) {
                    if (immediate) {
                        button.setVisible(false)
                    } else {
                        val action = getActionByTag(AppConst.TAG.ACTION_MENUBAR_BUTTON)

                        if (action!=null) {
                            stopAction(action)
                        }

                        if (_buttonTransType==ButtonTransition.ELASTIC) {
                            val buttonAction = ButtonActionCreate(getDirector())
                            buttonAction.setTag(AppConst.TAG.ACTION_MENUBAR_BUTTON)
                            buttonAction.setHide(this, delay)
                            button.runAction(buttonAction)
                        } else {
                            val buttonFadeAction = ButtonFadeActionCreate(getDirector())
                            buttonFadeAction.setTag(AppConst.TAG.ACTION_MENUBAR_BUTTON)
                            buttonFadeAction.setHide(this, delay)
                            button.runAction(buttonFadeAction)
                        }

                        delay += AppConst.Config.ACTION_BUTTON_DELAY
                    }
                }
            }

            if (delay>0) {
                delay += 0.2f
            }
        } else {
            immediate = true
        }

        _buttonIndex = 1 - _buttonIndex

        var x = _contentSize.width - 20f
        var index = 0

        for (i in 0 until 2) {
            if (types[i]==MenuType.NONE) continue

            var button = _menuButtons[_buttonIndex][i]
            if (button==null) {
                button = SMButton.create(getDirector(), menuTypeToInt(MenuType.NONE), SMButton.STYLE.DEFAULT, 0f, 0f, AppConst.SIZE.TOP_MENU_BUTTON_HEIGHT, AppConst.SIZE.TOP_MENU_BUTTON_HEIGHT, 0.5f, 0.5f)
                button.setIconColor(STATE.NORMAL, _activeColorSet.NORMAL)
                button.setIconColor(STATE.PRESSED, _activeColorSet.PRESS)
                button.setOnClickListener(this)
                button.setPushDownScale(0.9f)
                button.setPushDownOffset(Vec2(0f, -3f))
                addChild(button)
                _menuButtons[_buttonIndex][index] = button
            }

            var icon:SMView? = null
            var textIcon = false
            if (button.getTag()!= menuTypeToInt(types[i])) {
                button.setTag(menuTypeToInt(types[i]))

                when (types[i]) {
                    MenuType.NEXT -> {
                        icon = SMLabel.create(getDirector(), "NEXT", 38f)
                        icon.setAnchorPoint(Vec2.MIDDLE)
                        textIcon = true
                    }
                    MenuType.DONE -> {
                        icon = SMLabel.create(getDirector(), "DONE", 38f)
                        icon.setAnchorPoint(Vec2.MIDDLE)
                        textIcon = true
                    }
                    MenuType.CANCEL -> {
                        icon = SMLabel.create(getDirector(), "CANCEL", 38f)
                        icon.setAnchorPoint(Vec2.MIDDLE)
                        textIcon = true
                    }
                    else -> {
                        icon = SMLabel.create(getDirector(), "Whatever", 38f)
                        icon.setAnchorPoint(Vec2.MIDDLE)
                        textIcon = true
                    }
                }
                button.setIcon(STATE.NORMAL, icon)
            }
            if (icon!=null) {
                var width: Int = 0
                if (textIcon) {
                    width = icon.getContentSize().width.toInt()
                    x -= 20f
                }

                x -= width/2f
                button.setPosition(x, AppConst.SIZE.MENUBAR_HEIGHT/2f)
                x -= width/2f + 20f
            }

            if (immediate) {
                button.setScale(1f)
                button.setVisible(true)
            } else {
                val action = getActionByTag(AppConst.TAG.ACTION_MENUBAR_BUTTON)
                if (action!=null) {
                    stopAction(action)
                }

                if (_buttonTransType==ButtonTransition.ELASTIC) {
                    button.setScale(0f)
                    button.setAlpha(1f)
                    val buttonAction = ButtonActionCreate(getDirector())
                    buttonAction.setTag(AppConst.TAG.ACTION_MENUBAR_BUTTON)
                    buttonAction.setShow(this, delay)
                    button.runAction(buttonAction)
                } else {
                    button.setAlpha(0f)
                    button.setScale(1f)
                    val buttonFadeAction = ButtonFadeActionCreate(getDirector())
                    buttonFadeAction.setTag(AppConst.TAG.ACTION_MENUBAR_BUTTON)
                    buttonFadeAction.setShow(this, 0.1f)
                    button.runAction(buttonFadeAction)
                }

                delay += AppConst.Config.ACTION_BUTTON_DELAY
            }

            index++
        }
    }

    fun setDropDown(dropDown: DropDown, immediate: Boolean) {
        setDropDown(dropDown, immediate, 0f)
    }

    fun setDropDown(dropDown: DropDown, immediate: Boolean, delay: Float) {
        if (dropDown==_dropdown) return

        if (dropDown == DropDown.NOTHING) {
            _textContainer.setOnClickListener(null)
        } else {
            _textContainer.setOnClickListener(this)
        }

        if (_dropDownButton!=null) {
            val action = _dropDownButton!!.getActionByTag(AppConst.TAG.ACTION_MENUBAR_DROPDOWN)
            if (action!=null) {
                stopAction(action)
            }
        }

        if (immediate) {
            _dropdown = dropDown
            if (dropDown==DropDown.NOTHING) {
                removeChild(_dropDownButton)
                _dropDownButton = null
            } else {
                if (_dropDownButton==null) {
                    _dropDownButton = SMImageView.create(getDirector(), "images/arrow_bottom.png")
                    _dropDownButton!!.setColor(_activeColorSet.TEXT)
                    addChild(_dropDownButton!!)
                }
                if (dropDown==DropDown.UP) {
                    _dropDownButton!!.setRotation(180f)
                } else {
                    _dropDownButton!!.setRotation(0f)
                }
                _dropDownButton!!.setScale(1f)
                _dropDownButton!!.setVisible(true)
            }

            updateTextPosition(dropDown != DropDown.NOTHING)
            return
        }

        if (_dropdownAction==null) {
            _dropdownAction = DropDownActionCreate(getDirector())
            _dropdownAction!!.setTag(AppConst.TAG.ACTION_MENUBAR_DROPDOWN)
        }

        var created = false
        if (_dropDownButton==null) {
            _dropDownButton = SMImageView.create(getDirector(), "images/arrow_bottom.png")
            _dropDownButton!!.setColor(_activeColorSet.TEXT)
            _dropDownButton!!.setVisible(false)
            _dropDownButton!!.setScale(0f)
            addChild(_dropDownButton!!)
            updateTextPosition(true)
            created = true
        }

        if (dropDown == DropDown.UP) {
            if (created) {
                _dropDownButton!!.setRotation(100f)
                _dropdownAction?.setShow(this, delay)
                _dropDownButton!!.runAction(_dropdownAction!!)
            } else {
                _dropdownAction?.setUp(this, delay)
                _dropDownButton!!.runAction(_dropdownAction!!)
            }
        } else if (dropDown == DropDown.DOWN) {
            if (created) {
                _dropDownButton!!.setRotation(0f)
                _dropdownAction?.setShow(this, delay)
                _dropDownButton!!.runAction(_dropdownAction!!)
            } else {
                _dropdownAction?.setDown(this, delay)
                _dropDownButton!!.runAction(_dropdownAction!!)
            }
        } else {
            _dropdownAction?.setHide(this, delay)
            _dropDownButton!!.runAction(_dropdownAction!!)
        }

        _dropdown = dropDown
    }

    fun showButton(show: Boolean, immediate: Boolean) {
        for (i in 0 until 2) {
            val button = _menuButtons[_buttonIndex][i]
            if (button!=null && button.getTag()!= menuTypeToInt(MenuType.NONE)) {
                if (immediate) {
                    if (show) {
                        button.setScale(1f)
                        button.setVisible(true)
                    } else {
                        button.setScale(0f)
                        button.setVisible(false)
                    }
                } else {
                    val action = button.getActionByTag(AppConst.TAG.ACTION_MENUBAR_BUTTON)
                    if (action!=null) {
                        button.stopAction(action)
                    }
                    val buttonAction = ButtonActionCreate(getDirector())
                    buttonAction.setTag(AppConst.TAG.ACTION_MENUBAR_BUTTON)
                    if (show) {
                        buttonAction.setShow(this, 0f)
                    } else {
                        buttonAction.setHide(this, 0f)
                    }
                    button.runAction(buttonAction)
                }
            }
        }

        val button = _mainButton

        if (immediate) {
            if (show) {
                button.setScale(1f)
                button.setVisible(true)
            } else {
                button.setScale(0f)
                button.setVisible(false)
            }
        } else {
            val action = button.getActionByTag(AppConst.TAG.ACTION_MENUBAR_BUTTON)
            if (action!=null) {
                button.stopAction(action)
            }

            if (_buttonTransType==ButtonTransition.ELASTIC) {
                val buttonAction = ButtonActionCreate(getDirector())
                buttonAction.setTag(AppConst.TAG.ACTION_MENUBAR_BUTTON)
                if (show) {
                    buttonAction.setShow(this, 0f)
                } else {
                    buttonAction.setHide(this, 0f)
                }
                button.runAction(buttonAction)
            } else {
                val buttonFadeAction = ButtonFadeActionCreate(getDirector())
                buttonFadeAction.setTag(AppConst.TAG.ACTION_MENUBAR_BUTTON)
                if (show) {
                    buttonFadeAction.setShow(this, 0f)
                } else {
                    buttonFadeAction.setHide(this, 0f)
                }
                button.runAction(buttonFadeAction)
            }
        }
    }

    fun showActionButtonWithDelay(show: Boolean, delay: Float) {
        for (i in 0 until 2) {
            val button = _menuButtons[_buttonIndex][i]
            if (button!=null && button.getTag()!= menuTypeToInt(MenuType.NONE)) {
                val action = button.getActionByTag(AppConst.TAG.ACTION_MENUBAR_BUTTON)
                if (action!=null) {
                    button.stopAction(action)
                }

                val buttonAction = ButtonActionCreate(getDirector())
                buttonAction.setTag(AppConst.TAG.ACTION_MENUBAR_BUTTON)
                if (show) {
                    buttonAction.setShow(this, delay)
                } else {
                    buttonAction.setHide(this, delay)
                }
                button.runAction(buttonAction)
            }
        }
    }

    fun showMenuButton(show: Boolean, immediate: Boolean) {
        if (_mainButton!=null) {
            val button = _mainButton
            if (immediate) {
                if (show) {
                    button.setScale(1f)
                    button.setVisible(true)
                } else {
                    button.setScale(0f)
                    button.setVisible(false)
                }
            } else {
                val action = button.getActionByTag(AppConst.TAG.ACTION_MENUBAR_BUTTON)
                if (action!=null) {
                    button.stopAction(action)
                }
                val buttonAction = ButtonActionCreate(getDirector())
                buttonAction.setTag(AppConst.TAG.ACTION_MENUBAR_BUTTON)
                if (show) {
                    buttonAction.setShow(this, 0f)
                } else {
                    buttonAction.setHide(this, 0f)
                }
                button.runAction(buttonAction)
            }
        }
    }

    fun getDropDownState():DropDown {return _dropdown}

    fun setMenuBarListener(l: MenuBarListener?) {_listener = l}

    override fun dispatchTouchEvent(event: MotionEvent): Int {
        super.dispatchTouchEvent(event)

        val action = event.action
        if (action==MotionEvent.ACTION_DOWN) {
            _listener?.func2()
        }

        return TOUCH_TRUE
    }

    fun setTextOffsetY(textOffsetY: Float) {
        _textContainer?.setPositionY(AppConst.SIZE.MENUBAR_HEIGHT/2f+textOffsetY)
    }

    fun setOverlapChild(child: SMView?) {_overlapChild=child}

    fun getOverlapChild():SMView? {return _overlapChild}

    override fun containsPoint(x: Float, y: Float): Boolean {
        if (_overlapChild!=null) {
            val worldPtr = convertToWorldSpace(Vec2(x, y))
            val pt = _overlapChild?.convertToNodeSpace(worldPtr) ?: Vec2(Vec2.ZERO)
            if (_overlapChild?.containsPoint(pt)==true) {
                return false
            }
        }

        return super.containsPoint(x, y)
    }

    fun getMenuBarListener(): MenuBarListener? {return _listener}

    fun getButtonByType(type: MenuType): SMView? {
        for (i in 0 until 2) {
            val button = _menuButtons[_buttonIndex][i]
            if (button!=null && button.getTag()== menuTypeToInt(type)) {
                return button
            }
        }

        return null
    }

    fun getButtonTypes(): ArrayList<MenuType> {
        val buttonTypes:ArrayList<MenuType> = ArrayList()
        for (i in 0 until 2) {
            val button = _menuButtons[_buttonIndex][i]
            if (button!=null && button.isVisible()) {
                buttonTypes.add(intToMenuType(button.getTag()))
            }
        }
        return buttonTypes
    }

    fun onSwipeStart() {
        if (_textTransType==TextTransition.SWIPE && _textTransform!=null && _menuTransform!=null) {
            _textTransform?.onStart()
            _menuTransform?.onStart()
        }
    }

    fun onSwipeUpdate(t: Float) {
        if (_textTransType==TextTransition.SWIPE && _textTransform!=null && _menuTransform!=null) {
            _textTransform?.onUpdate(t)
            _menuTransform?.onUpdate(t)

            for (i in 0 until 2) {
                var button = _menuButtons[1-_buttonIndex][i]
                if (button!=null && button.isVisible()) {
                    button.setAlpha(1f-t)
                }
                button = _menuButtons[_buttonIndex][i]
                if (button!=null && button.isVisible()) {
                    button.setAlpha(t)
                }
            }
        }
    }

    fun onSwipeComplete() {
        if (_textTransType==TextTransition.SWIPE && _textTransform!=null && _menuTransform!=null) {
            _textTransform?.onEnd()
            _menuTransform?.onEnd()

            for (i in 0 until 2) {
                var button = _menuButtons[1-_buttonIndex][i]
                if (button!=null && button.isVisible()) {
                    button.setVisible(false)
                }
                button = _menuButtons[_buttonIndex][i]
                if (button!=null && button.isVisible()) {
                    button.setAlpha(1f)
                }
            }
        }
    }

    fun onSwipeCancel() {
        if (_textTransType==TextTransition.SWIPE && _textTransform!=null && _menuTransform!=null) {
            _textTransform?.onCancel()
            _textIndex = 1-_textIndex
            _textString = _textLabel[_textIndex]?.getText()?:""

            _menuButtonType = _menuTransform!!._fromType
            _menuTransform?.onCancel()

            _buttonIndex = 1 - _buttonIndex
            for (i in 0 until 2) {
                var button = _menuButtons[1 - _buttonIndex][i]
                if (button!=null && button.isVisible()) {
                    button.setVisible(false)
                }

                button = _menuButtons[_buttonIndex][i]
                if (button!=null && button.isVisible()) {
                    button.setAlpha(1f)
                }
            }
        }
    }

    fun showToast(message: String, color: Color4F, duration: Float) {
        if (_toast==null) {
            _toast = SMToastBar.create(getDirector(), object : SMToastBar.ToastBarCallback{
                override fun onToastBarHide(bar: SMToastBar) {
                    onToastHideComplete(bar)
                }
            })
            addChild(_toast!!, -100)
        }

        _toast?.setMessage(message, color, duration)
    }

    private fun applyColorSet(colorSet: ColorSet) {
        setBackgroundColor(colorSet.BG)

        _textLabel[0]?.setColor(colorSet.TEXT)
        _textLabel[1]?.setColor(colorSet.TEXT)
        _dropDownButton?.setColor(colorSet.TEXT)

        _mainButton.setButtonColor(STATE.NORMAL, colorSet.NORMAL)
        _mainButton.setButtonColor(STATE.PRESSED, colorSet.PRESS)

        for (i in 0 until 2) {
            for (j in 0 until 2) {
                val button = _menuButtons[i][j]
                button?.setIconColor(STATE.NORMAL, colorSet.NORMAL)
                button?.setIconColor(STATE.PRESSED, colorSet.PRESS)
            }
        }
    }

    override fun onClick(view: SMView?) {
        if (view==null) return

        if (_mainButton.getActionByTag(AppConst.TAG.USER+1)!=null) {
            // in transform action
            return
        }

        if (_listener!=null) {
            if (_listener!!.func1(view)) {
                //
                return
            }
        }

        when (intToMenuType(view.getTag())) {
            MenuType.MENU -> {

            }
            else -> {

            }
        }
    }

    private fun updateTextPosition(dropdown: Boolean) {
        if (_textLabel[_textIndex]==null) return

        var containerWidth = _textLabel[_textIndex]!!.getContentSize().width

        if (dropdown) {
            containerWidth += 16f + 36f
        }

        val centerPt = Vec2(containerWidth/2f, AppConst.SIZE.MENUBAR_HEIGHT/2f)
        _textContainer.setContentSize(Size(containerWidth, AppConst.SIZE.MENUBAR_HEIGHT))
        _textContainer.stub!![0].setPosition(centerPt)
        _textContainer.stub!![1].setPosition(centerPt)

        _textLabel[_textIndex]!!.setPositionX(if (dropdown) -34f else 0f)

        if (_dropDownButton!=null && dropdown) {
            _dropDownButton?.setPosition((_contentSize.width+containerWidth/2)/2f - 18f, AppConst.SIZE.MENUBAR_HEIGHT/2f)
        }
    }

    private fun onToastHideComplete(toast: SMToastBar) {
        if (_toast!=null) {
            removeChild(_toast)
            _toast = null
        }
    }

    private fun showAlarmBadge() {
        showAlarmBadge(false)
    }

    private fun showAlarmBadge(effect: Boolean) {
        if (!_newAlarm) return

        if (_alarmCircle==null) {
            if (_menuImage!=null) {
                _alarmCircle = SMSolidCircleView.create(getDirector())
                _alarmCircle!!.setContentSize(Size(12f, 12f))
                _alarmCircle!!.setColor(Color4F.ALARM_BADGE_RED)
                _alarmCircle!!.setAnchorPoint(Vec2.MIDDLE)
                _alarmCircle!!.setPosition(73f, 73f)
                _menuImage!!.addChild(_alarmCircle!!)
            }
        }
        if (_alarmCircle!=null) {
            _alarmCircle!!.setAlpha(0f)
            _alarmCircle!!.stopAllActions()
            val a = TransformAction.create(getDirector())
            a.toAlpha(0f).setTimeValue(0.2f, 0f)
            _alarmCircle!!.runAction(a)

            if (effect) {
                val size = _alarmCircle?.getContentSize() ?: Size(Size.ZERO)
                RingWave.show(getDirector(), _alarmCircle, size.width/2f, size.height/2f, 50f, 0.4f, 0.1f, Color4F.ALARM_BADGE_RED)
            }
        }
    }


    fun TextContainerCreate(director: IDirector):TextContainer {
        val text = TextContainer(director)
        text.init()
        return text
    }

    inner class TextContainer(director: IDirector) : SMView(director) {
        public var stub: ArrayList<SMView> = ArrayList(2)

        override fun init(): Boolean {
            stub.add(SMView(getDirector()))
            stub[0].setAnchorPoint(Vec2.MIDDLE)
            stub[0].setCascadeAlphaEnable(true)

            stub.add(SMView(getDirector()))
            stub[1].setAnchorPoint(Vec2.MIDDLE)
            stub[1].setCascadeAlphaEnable(true)

            addChild(stub[0])
            addChild(stub[1])

            return true
        }

        override fun onStateChangeNormalToPress(event: MotionEvent) {
            setAnimOffset(Vec2(0f, -2f))
        }

        override fun onStateChangePressToNormal(event: MotionEvent) {
            setAnimOffset(Vec2.ZERO)
        }
    }

    fun MenuTransformCreate(director: IDirector): MenuTransform {
        val action = MenuTransform(director)
        action.initWithDuration(0f)
        return action
    }

    inner class MenuTransform(director: IDirector): DelayBaseAction(director) {
        var isFirstMenu = true
        var _from:ArrayList<Vec2> = ArrayList(4)
        var _to:ArrayList<Vec2> = ArrayList(4)
        var _diameter:FloatArray = FloatArray(4)
        var _fromAngle = 0f
        var _toAngle = 0f
        var _dst:ArrayList<DotPosition>? = null
        var _menuButtonType = MenuType.NONE
        var _fromType = MenuType.MENU
        lateinit var _menuBar:MenuBar

        fun setMenuBar(menuBar: MenuBar) {_menuBar = menuBar}

        override fun initWithDuration(d: Float): Boolean {

            for (i in 0..3) {
                _from.add(Vec2(Vec2.ZERO))
                _to.add(Vec2(Vec2.ZERO))
            }

            return super.initWithDuration(d)
        }

        override fun onStart() {
            var lineWidth = 1f

            if (_from.size==0) {
                for (i in 0..3) {
                    _from.add(Vec2(Vec2.ZERO))
                }
            }
            if (_to.size==0) {
                for (i in 0..3) {
                    _to.add(Vec2(Vec2.ZERO))
                }
            }

            for (i in 0 until 4) {
                val line = _menuBar._menuLine[i]
                _from[i].set(line.getFromPosition())
                _to[i].set(line.getToPosition())

                if (_menuButtonType==MenuType.MENU && isFirstMenu) {
                    _diameter[i] = 1f
                } else {
                    _diameter[i] = line.getLineWidth()
                }

                _menuBar._menuCircle[i].setVisible(false)
                _menuBar._menuLine[i].setVisible(true)
            }

            if (_menuButtonType==MenuType.MENU && isFirstMenu) {
                isFirstMenu = false
            }

            _fromAngle = _menuBar._buttonContainer.getRotation() ?: 0f

            when (_menuButtonType) {
                MenuType.CLOSE -> {
                    _toAngle = 180f
                }
                MenuType.CLOSE2 -> {
                    _toAngle = 360f // 180 + 90 + 90
                }
                MenuType.BACK -> {
                    if (_fromType!=MenuType.BACK) {
                        _toAngle = 315f // 180 + 90 + 45
                    } else {
                        // on rotate back to back
                        _toAngle = _fromAngle
                    }
                }
                else -> {
                    _toAngle = 0f
                }
            }

            if (_menuButtonType==MenuType.BACK) {
                val diff = _fromAngle % 90f

                // make 180 degree : bottom -> left bottom -> left
                if (_fromType==_menuButtonType) {
                    _fromAngle = _toAngle - 45 - (90 + diff)
                }
            }
            if (_fromType==MenuType.BACK) {
                if (_fromType!=_menuButtonType) {
                    _fromAngle = SMView.getShortestAngle(0f, _fromAngle)
                    _toAngle = 90f
                }
            }
            if (_toAngle<_fromAngle) {
                _fromAngle -= 360f
            }
        }

        override fun onUpdate(dt: Float) {
            val t = tweenfunc.cubicEaseOut(dt)

            for (i in 0 until 4) {
                val line = _menuBar._menuLine[i]


                val x1 = interpolation(_from[i].x, _dst!![i]._from.x+AppConst.SIZE.TOP_MENU_BUTTONE_SIZE/2f, t)
                val y1 = interpolation(_from[i].y, _dst!![i]._from.y+AppConst.SIZE.TOP_MENU_BUTTONE_SIZE/2f, t)

                val x2 = interpolation(_to[i].x, _dst!![i]._to.x+AppConst.SIZE.TOP_MENU_BUTTONE_SIZE/2f, t)
                val y2 = interpolation(_to[i].y, _dst!![i]._to.y+AppConst.SIZE.TOP_MENU_BUTTONE_SIZE/2f, t)

                val angle = interpolation(_fromAngle, _toAngle, t)

                var diameter = 0f
                if (_menuButtonType==MenuType.CLOSE || _menuButtonType==MenuType.CLOSE2) {
                    var tt = t * 1.5f
                    if (tt>1f) {
                        tt = 1f
                    }
                    diameter = interpolation(_diameter[i], _dst!![i]._diameter, tt)
                } else if (_menuButtonType==MenuType.MENU) {
                    var tt = t - 0.5f
                    if (tt<0f) {
                        tt = 0f
                    }
                    tt /= 0.5f
                    diameter = interpolation(_diameter[i], _dst!![i]._diameter, tt)
                } else {
                    diameter = interpolation(_diameter[i], _dst!![i]._diameter, t)
                }

                line.setLineWidth(diameter)
                line.line(x1, y1, x2, y2)
                _menuBar._buttonContainer.setRotation(angle)

                // alarm button count
                if (_fromType==MenuType.ALARM) {
                    line.setAlpha(t)
                } else if (_menuButtonType==MenuType.ALARM) {
                    line.setAlpha(1f-t)
                }
            }

            // alarm button count
            if (_fromType==MenuType.ALARM) {
                _menuBar._menuImage?.setAlpha(1f-t)
            } else if (_menuButtonType==MenuType.ALARM) {
                _menuBar._menuImage?.setAlpha(t)
            }
        }

        override fun onEnd() {
            for (i in 0 until 4) {
                _menuBar._menuLine[i].setVisible(_menuButtonType!=MenuType.MENU)
                _menuBar._menuCircle[i].setVisible(_menuButtonType==MenuType.MENU)
            }
            if (_menuButtonType==MenuType.MENU) {
                _menuBar.showAlarmBadge()
            }

            _toAngle = when(_menuButtonType) {
                MenuType.CLOSE -> 180f
                MenuType.CLOSE2 -> 90f
                MenuType.BACK -> 315f
                else -> 0f
            }

            _menuBar._buttonContainer.setRotation(_toAngle)
        }

        fun onCancel() {
            for (i in 0 until 4) {
                _menuBar._menuLine[i].setVisible(_fromType!=MenuType.MENU)
                _menuBar._menuCircle[i].setVisible(_fromType==MenuType.MENU)
            }
            if (_fromType==MenuType.MENU) {
                _menuBar.showAlarmBadge()
            }

            _toAngle = when(_fromType) {
                MenuType.CLOSE -> 180f
                MenuType.CLOSE2 -> 90f
                MenuType.BACK -> 315f
                else -> 0f
            }

            if (_fromAngle!=_toAngle) {
                _menuBar._buttonContainer.setRotation(_fromAngle)
            }
            _menuBar._mainButton.setTag(menuTypeToInt(_fromType))
        }

        fun setMenuType(fromMenuType: MenuType, toMenuType: MenuType, duration: Float) {
            setTimeValue(duration, 0f)

            _fromType = fromMenuType
            _menuButtonType = toMenuType

            _dst = when (toMenuType) {
                MenuType.MENU -> sDotMenu
                MenuType.CLOSE, MenuType.CLOSE2 -> sDotClose
                MenuType.BACK -> sDotBack
                else -> sDotDot
            }
        }
    }

    class ColorSet {
        var BG = Color4F(Color4F.WHITE)
        var TEXT = MakeColor4F(0x222222, 1f)
        var NORMAL = MakeColor4F(0x222222, 1f)
        var PRESS = MakeColor4F(0xadafb3, 1f)

        companion object {
            val WHITE = ColorSet()
            val WHITE_TRANSLUCENT = ColorSet(Color4F(1f, 1f, 1f, 0.7f), MakeColor4F(0x222222, 1f), MakeColor4F(0x222222, 1f), MakeColor4F(0xadafb3, 1f))
            val BLACK = ColorSet(MakeColor4F(0x222222, 1f), Color4F.WHITE, Color4F.WHITE, MakeColor4F(0xadafb3, 1f))
            val NONE = ColorSet(Color4F.WHITE, Color4F.WHITE, Color4F.WHITE, Color4F.WHITE)
            val TRANSLUCENT = ColorSet(Color4F(1f, 1f, 1f, 0f), MakeColor4F(0x222222, 1f), MakeColor4F(0x222222, 1f), MakeColor4F(0xadafb3, 1f))
        }

        constructor() {}

        constructor(colorSet: ColorSet) {
            BG.set(colorSet.BG)
            TEXT.set(colorSet.TEXT)
            NORMAL.set(colorSet.NORMAL)
            PRESS.set(colorSet.PRESS)
        }

        constructor(bg: Color4F, text: Color4F, normal: Color4F, press: Color4F) {
            BG.set(bg)
            TEXT.set(text)
            NORMAL.set(normal)
            PRESS.set(press)
        }

        fun equal(set: ColorSet): Boolean {
            return BG.equal(set.BG) && TEXT.equal(set.TEXT) && NORMAL.equal(set.NORMAL) && PRESS.equal(set.PRESS)
        }

        fun set(colorSet: ColorSet): ColorSet {
            if (equals(colorSet)) return this

            this.BG.set(colorSet.BG)
            this.TEXT.set(colorSet.TEXT)
            this.NORMAL.set(colorSet.NORMAL)
            this.PRESS.set(colorSet.PRESS)

            return this
        }
    }

    fun TextTransformCreate(director: IDirector): TextTransform {
        val action = TextTransform(director)
        action.initWithDuration(0f)
        return action
    }

    inner class TextTransform(director: IDirector): DelayBaseAction(director) {

        var _fadeType: Boolean = false
        var _toIndex: Int = 0
        var _gap: Float = 0f
        var _menuBar: MenuBar? = null

        override fun onStart() {
            if (_fadeType) {
                _menuBar?._textLabel!![1-_toIndex]?.setVisible(true)
                _menuBar?._textLabel!![_toIndex]?.setVisible(true)
            } else {
                _menuBar?._textLabel!![_toIndex]?.setVisible(true)
                _menuBar?._textLabel!![_toIndex]?.setAlpha(1f)
            }
        }

        override fun onUpdate(dt: Float) {
            if (_fadeType) {
                _menuBar?._textLabel!![1-_toIndex]?.setAlpha(1f-dt)
                _menuBar?._textLabel!![_toIndex]?.setAlpha(dt)
            } else {
                var t = tweenfunc.cubicEaseOut(dt)
                if (t<0f) t = 0f
                if (t>1f) t = 1f

                // out text... 0.4 ~ 0.8
                var outValue = 0f
                var outStart = 0.4f
                var outEnd = 0.8f
                if (t in outStart..outEnd) {
                    outValue = t - outStart
                    if (outValue>0f) {
                        outValue /= 0.4f
                    }
                }

                if (t>outEnd) outValue = 1f

                // in text... 0.6 ~ 1.0
                var inValue = 0f
                var inStart = 0.5f
                var inEnd = 0.9f
                if (t>=inStart && t<inEnd) {
                    inValue = t - inStart
                    if (inValue>0f) {
                        inValue /= 0.4f
                    }
                }
                if (t>inEnd) inValue = 1f

                if (t>=outStart) {
                    // out text label stride moving
                    var label = _menuBar?._textLabel!![1-_toIndex]
                    if (label!=null) {
                        if (label.getSeparateCount()>0) {
                            for (i in 0 until label.getSeparateCount()) {
                                val letter = label.getLetter(i)
                                if (letter!=null) {
                                    val tt = outValue - i*(AppConst.Config.TEXT_TRANS_DELAY/2f)
                                    if (tt>0f) {
                                        var f = tt / (AppConst.Config.TEXT_TRANS_DURATION/2f)
                                        if (f>1f) f = 1f

                                        f = 1f - f
                                        letter.setScale(f)
                                        letter.setAlpha(f)
                                    }
                                }
                            }
                        } else {
                            label.setAlpha(1f-t)
                        }
                    }

                    label = _menuBar!!._textLabel[_toIndex]
                    if (label!=null) {
                        if (label.getSeparateCount()>0) {
                            for (i in 0 until label.getSeparateCount()) {
                                val letter = label.getLetter(i)
                                if (letter!=null) {
                                    val tt = inValue - i * (AppConst.Config.TEXT_TRANS_DELAY)
                                    if (tt>0) {
                                        label.setVisible(true)
                                        var f = tt / AppConst.Config.TEXT_TRANS_DURATION
                                        if (f>1) f = 1f

                                        val newScale = 0.5f * 0.5f*f
                                        letter.setScale(newScale)
                                        letter.setAlpha(t)
                                    }
                                }
                            }
                        } else {
                            label.setAlpha(t)
                        }
                    }
                } else {
                    // animate not yet... so... wait...

                    // just out text...
                    var label = _menuBar?._textLabel!![1-_toIndex]
                    if (label!=null) {
                        if (label.getSeparateCount()>0) {
                            for (i in 0 until label.getSeparateCount()) {
                                val letter = label.getLetter(i)
                                letter?.setScale(1f)
                                letter?.setAlpha(1f)
                            }
                        } else {
                            label.setAlpha(1f)
                        }
                    }

                    label = _menuBar?._textLabel!![_toIndex]
                    if (label!=null) {
                        if (label.getSeparateCount()>0) {
                            for (i in 0 until label.getSeparateCount()) {
                                val letter = label.getLetter(i)
                                letter?.setScale(0f)
                                letter?.setAlpha(0f)
                            }
                        } else {
                            label.setAlpha(0f)
                        }
                    }
                }
            }
        }

        override fun onEnd() {
            val label = _menuBar?._textLabel!![1-_toIndex]
            if (label!=null) {
                if (label.getSeparateCount()>0) {
                    label.setVisible(false)
                    for (i in 0 until label.getSeparateCount()) {
                        val letter = label.getLetter(i)
                        letter?.setScale(1f)
                        letter?.setAlpha(1f)
                    }
                } else {
                    label.setVisible(true)
                }
            }

            _menuBar?._textLabel!![1-_toIndex]?.clearSeparate()
            _menuBar?._textLabel!![_toIndex]?.clearSeparate()

            _menuBar?._textLabel!![1-_toIndex]?.setAlpha(0f)
            _menuBar?._textLabel!![_toIndex]?.setAlpha(1f)
        }

        fun onCancel() {
            // inText hidden
            val label = _menuBar?._textLabel!![_toIndex]
            if (label!=null) {
                if (label.getSeparateCount()>0) {
                    label.setVisible(false)
                    for (i in 0 until label.getSeparateCount()) {
                        val letter = label.getLetter(i)
                        letter?.setScale(0f)
                        letter?.setAlpha(0f)
                    }
                } else {
                    label.setVisible(true)
                }
            }

            _menuBar?._textLabel!![1-_toIndex]?.clearSeparate()
            _menuBar?._textLabel!![_toIndex]?.clearSeparate()
        }

        fun setFadeType() {
            _fadeType = true
        }

        fun setElasticType() {
            _fadeType = false
        }

        fun setMenuBar(menuBar: MenuBar?) {
            _menuBar = menuBar
        }

        fun makeTextSeparate() {
            _menuBar?._textLabel!![1-_toIndex]?.makeSeparate()
            _menuBar?._textLabel!![_toIndex]?.makeSeparate()
        }

        fun setTextIndex(textIndex: Int) {
            var duration = 0f
            _gap = 0f

            if (_fadeType) {
                duration = 0.25f
            } else {
                // out label
                var label = _menuBar?._textLabel!![1-textIndex]
                if (label!=null) {
                    val len = label.getStringLength()
                    _gap = AppConst.Config.TEXT_TRANS_DURATION + AppConst.Config.TEXT_TRANS_DELAY * len
                }


                label = _menuBar?._textLabel!![textIndex]
                if (label!=null) {
                    if (label.getSeparateCount()>0) {
                        for (i in 0 until label.getSeparateCount()) {
                            val letter = label.getLetter(i)
                            letter?.setScale(0f)
//                            letter?.setAlpha(0f)
                        }
                        duration = AppConst.Config.TEXT_TRANS_DURATION + AppConst.Config.TEXT_TRANS_DELAY * label.getSeparateCount() + 0.1f
                    } else {
                        label.setVisible(false)
                    }
                }

                duration = duration.coerceAtLeast(AppConst.Config.TEXT_TRANS_MOVE_DURATION)
                duration += _gap + 0.1f
            }

            setTimeValue(duration, 0.1f)
            _toIndex = textIndex
        }
    }

    fun ButtonActionCreate(director: IDirector):ButtonAction {
        val action = ButtonAction(director)
        action.initWithDuration(0f)
        return action
    }


    inner class ButtonAction(director: IDirector): DelayBaseAction(director) {
        protected var _show = false
        protected var _from = 0f
        protected var _to = 0f
        protected var _menuBar:MenuBar? = null
        override fun onStart() {
            _from = _target?.getScale()?:1f
            _to = if (_show) { 1f } else { 0f }
            _target?.setVisible(true)

            if (_target is SMButton) {
                val button = _target as SMButton
                button.setIconColor(STATE.NORMAL, _menuBar!!._activeColorSet.NORMAL)
                button.setIconColor(STATE.PRESSED, _menuBar!!._activeColorSet.PRESS)
            }
        }

        override fun onUpdate(t: Float) {
            val tt = tweenfunc.cubicEaseOut(t)
            val scale = interpolation(_from, _to, tt)
            _target?.setScale(scale)
        }

        override fun onEnd() {
            if (!_show) {
                _target?.setVisible(false)
            }
        }

        fun setShow(menuBar: MenuBar?, delay: Float) {
            _menuBar = menuBar
            setTimeValue(0.25f, delay)
            _show = true
        }

        fun setHide(menuBar: MenuBar?, delay: Float) {
            _menuBar = menuBar
            setTimeValue(0.25f, delay)
            _show = false
        }
    }

    fun ButtonFadeActionCreate(director: IDirector): ButtonFadeAction {
        val action = ButtonFadeAction(director)
        action.initWithDuration(0f)
        return action
    }

    inner class ButtonFadeAction(director: IDirector): DelayBaseAction(director) {
        protected var _show = false
        protected var _from = 0f
        protected var _to = 0f
        protected var _menuBar:MenuBar? = null

        override fun onStart() {
            _from = _target?.getAlpha()?:1f
            _to = if (_show) { 1.0f } else { 0.0f }

            _target?.setVisible(true)
            if (_target is SMButton) {
                val button = _target as SMButton
                button.setIconColor(STATE.NORMAL, _menuBar!!._activeColorSet.NORMAL)
                button.setIconColor(STATE.PRESSED, _menuBar!!._activeColorSet.PRESS)
            }
        }

        override fun onUpdate(t: Float) {
            val alpha = interpolation(_from, _to, t)
            _target?.setAlpha(alpha)
        }

        override fun onEnd() {
            if (!_show) {
                _target?.setVisible(false)
            }
        }

        fun setShow(menuBar: MenuBar?, delay: Float) {
            _menuBar = menuBar
            setTimeValue(0.25f, delay)
            _show = true
        }

        fun setHide(menuBar: MenuBar?, delay: Float) {
            _menuBar = menuBar
            setTimeValue(0.25f, delay)
            _show = false
        }
    }

    fun DropDownActionCreate(director: IDirector): DropwDownAction {
        val action = DropwDownAction(director)
        action.initWithDuration(0f)
        return action
    }

    inner class DropwDownAction(director: IDirector): DelayBaseAction(director) {
        protected var _showAction = false
        protected var _show = false
        protected var _from = 0f
        protected var _to = 0f
        protected var _up = false
        protected var _menuBar:MenuBar? = null

        override fun onStart() {
            if (_showAction) {
                _from = _target?.getScale()?:1f
                _to = if (_show) { 1f } else { 0f }
                _target?.setVisible(true)
            } else {
                _from = _target?.getRotation()?:0f
                _to = if (_up) {180f} else {360f}
                _target?.setVisible(true)
            }
        }

        override fun onUpdate(t: Float) {
            if (_showAction) {
                val tt = tweenfunc.cubicEaseOut(t)
                val scale = interpolation(_from, _to, tt)
                _target?.setScale(scale)
            } else {
                val tt = tweenfunc.backEaseOut(t)
                val rotate = interpolation(_from, _to, tt)
                _target?.setRotation(rotate)
            }
        }

        override fun onEnd() {
            if (_showAction) {
                if (!_show) {
                    _target?.removeFromParent()
                    _menuBar?._dropDownButton = null
                    _menuBar?.updateTextPosition(false)
                }
            } else {
                if (!_up) {
                    _target?.setRotationSkewY(0f)
                }
            }
        }

        fun setShow(menuBar: MenuBar?, delay: Float) {
            setTimeValue(0.25f, delay)
            _showAction = true
            _show = true
        }

        fun setHide(menuBar: MenuBar?, delay: Float) {
            setTimeValue(0.25f, delay)
            _showAction = true
            _show = false
        }

        fun setUp(menuBar: MenuBar?, delay: Float) {
            _showAction = false
            _menuBar = menuBar
            setTimeValue(0.5f, delay)
            _up = true
        }

        fun setDown(menuBar: MenuBar?, delay: Float) {
            _showAction = false
            _menuBar = menuBar
            setTimeValue(0.5f, delay)
            _up = false
        }
    }

    fun ColorTransformCreate(director: IDirector): ColorTransform {
        val action = ColorTransform(director)
        action.initWithDuration(0f)
        return action
    }

    inner class ColorTransform(director: IDirector): DelayBaseAction(director) {
        var _from = ColorSet(ColorSet.NONE)
        var _to = ColorSet(ColorSet.NONE)

        override fun onStart() {
            if (_target is MenuBar) {
                val bar = _target as MenuBar
                _from = bar._activeColorSet
                bar._colorSet = _to
            }
        }

        override fun onUpdate(t: Float) {
            val bar = _target as MenuBar
            bar._activeColorSet.BG = interpolateColor4F(_from.BG, _to.BG, t)
            bar._activeColorSet.TEXT = interpolateColor4F(_from.TEXT, _to.TEXT, t)
            bar._activeColorSet.NORMAL = interpolateColor4F(_from.NORMAL, _to.NORMAL, t)
            bar._activeColorSet.PRESS = interpolateColor4F(_from.PRESS, _to.PRESS, t)

            bar.applyColorSet(bar._activeColorSet)
        }

        fun setColorSet(toColorSet: ColorSet) {
            setTimeValue(0.25f, 1f)

            _to = toColorSet
        }
    }

}