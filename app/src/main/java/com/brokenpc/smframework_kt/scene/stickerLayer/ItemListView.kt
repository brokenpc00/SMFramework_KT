package com.brokenpc.app.scene.stickerLayer

import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.SMView
import com.brokenpc.smframework.util.FileUtils
import com.brokenpc.smframework.util.Value
import com.brokenpc.smframework.view.SMTableView

open class ItemListView(director: IDirector) : SMTableView(director), SMView.OnClickListener {
    protected var _initLoaded = false
    protected val _dict = Value.ValueMap()
    protected var _listener: OnItemClickListener? = null
    protected var _resourceRootPath = ""
    protected var _itemSize = 0

    companion object {
        const val ITEMS = "items"
        const val THUMB = "thumb"
        const val NAME = "name"
        const val IMG_EXTEND = ".png"
        const val IMAGE = "image"
        const val LAYOUT = "layout"
        const val PANEL_HEIGHT = 240.0f
        const val CELL_WIDTH = 350.0f
    }

    interface OnItemClickListener {
        fun onItemClick(sender: ItemListView, view: StickerItemThumbView)
    }

    override fun setVisible(visible: Boolean) {

        if (visible!=_visible) {
            if (visible) {
                show()
            } else {
                hide()
            }
        }
        super.setVisible(visible)
    }

    open fun show() {initLoadItemList()}

    open fun hide() {stop()}

    fun setOnItemClickListener(l: OnItemClickListener) {_listener = l}

    fun getResourceRootPath(): String {return _resourceRootPath}

    open fun initLayout(): Boolean {
        setContentSize(getDirector().getWinSize().width, PANEL_HEIGHT)
        setMaxScrollVelocity(10000f)
        setPreloadPaddingSize(200f)

        setBackgroundColor(MakeColor4F(0x767678, 1.0f))

        super.setVisible(false)
        return true
    }


    override fun onClick(view: SMView?) {
        if (view is StickerItemThumbView) {
            _listener?.onItemClick(this, view)
        }
    }

    open fun initLoadItemList(): Boolean {
        if (!_initLoaded) {
            val plist = "$_resourceRootPath$ITEMS.xml"
            val fullPath = FileUtils.getInstance().fullPathForFileName(plist)

            val itemList = Value.ValueList()
            val thumbList = Value.ValueList()

            for (i in 0..20) {
                val map = Value.ValueMap()

                var no = "0"
                no += if (i<10) {
                    "0$i"
                } else {
                    "$i"
                }

                if (i==0) {
                    map[NAME] = Value("styling_no_image")
                } else {
                    map[NAME] = Value(no)
                    map[IMAGE] = Value(no)
                }

                itemList.add(Value(map))
            }

            _dict[ITEMS] = Value(itemList)
            _itemSize = _dict[ITEMS]!!.getList()!!.size
            _initLoaded = true
        }
        return true
    }
}