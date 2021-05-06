package com.interpark.app.scene.stickerLayer

import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.SMView
import com.brokenpc.smframework.base.types.Color4F
import com.brokenpc.smframework.base.types.IndexPath
import com.brokenpc.smframework.base.types.Vec2
import com.brokenpc.smframework.util.ImageManager.ImageDownloader
import com.brokenpc.smframework.view.SMLabel
import com.interpark.smframework.view.Sticker.StickerItem

class StickerItemListView(director: IDirector) : ItemListView(director) {
    protected val _items:ArrayList<StickerItem> = ArrayList()
    init {
        _resourceRootPath = "sticker/"
    }

    companion object {
        @JvmStatic
        fun create(director: IDirector): StickerItemListView {
            val listView = StickerItemListView(director)
            listView.initWithOrientAndColumns(Orientation.HORIZONTAL, 1)
            listView.initLayout()
            return listView
        }
    }

    override fun show() {
        super.show()

        val cells = getVisibleCells()
        for (cell in cells) {
            if (cell is StickerItemThumbView) {
                val thumb = cell as StickerItemThumbView
                // shake it..
                thumb.startShowAction()
            }
        }
    }

    fun findItem(name: String): StickerItem? {
        if (name.isEmpty()) {
            return null
        }

        initLoadItemList()

        val array = _dict[ITEMS]!!.getList()
        if (array!=null) {
            var index = 0
            while (_itemSize!=0) {
                if (_items.size>index) {
                    if (_items[index]._decoded) {
                        if (_items[index]._name.compareTo(name)==0) {
                            return _items[index]
                        }
                    } else {
                        if (array.size>index) {
                            val m = array[index].getMap()
                            if (m!=null && name.compareTo(m[NAME]!!.getString())==0) {
                                setStickerItem(_items[index], index)
                                return _items[index]
                            }
                        }
                    }
                }
                index++
            }
        }

        return null
    }

    fun getItem(index: Int): StickerItem? {
        if (index<0) return null

        initLoadItemList()

        if (index>=_itemSize) return null

        if (_items[index]._decoded) {
            return _items[index]
        }

        setStickerItem(_items[index], index)

        return _items[index]
    }

    override fun initLayout(): Boolean {
        super.initLayout()

        cellForRowAtIndexPath = object : CellForRowAtIndexPath {
            override fun cellForRowAtIndexPath(indexPath: IndexPath): SMView {
                return getView(indexPath)
            }
        }

        numberOfRowsInSection = object : NumberOfRowsInSection {
            override fun numberOfRowsInSection(section: Int): Int {
                return getItemCount(section)
            }
        }

        setScrollMarginSize(22f, 22f)
        hintFixedCellSize(CELL_WIDTH)

        return true
    }

    override fun initLoadItemList(): Boolean {
        if (!_initLoaded) {
            if (super.initLoadItemList()) {
                val items = _dict[ITEMS]!!.getList()
                if (items!=null) {
                    for (i in 0 until items.size) {
                        val item = StickerItem()
                        item._decoded = false
                        _items.add(item)
                    }
                }
            }
        }

        return _initLoaded
    }

    fun setStickerItem(item: StickerItem, index: Int): Boolean {
        item._index = index
        item._decoded = true
        item._rootPath = _resourceRootPath

        val m = _dict[ITEMS]!!.getList()!![index].getMap()!!
        if (m[NAME]!=null) {
            item._name = m[NAME]!!.getString()
        } else {
            item._name = ""
        }

        if (m[IMAGE]!=null) {
            val str = m[IMAGE]!!.getString()
            item._imageArrary.add(str)
        }

        item._layout = -1

        return true
    }

    protected fun getView(indexPath: IndexPath): SMView {
        val index = indexPath.getIndex()
        var thumb:StickerItemThumbView? = if (index==0) {
            val view = dequeueReusableCellWithIdentifier("NOIMAGE")
            if (view is StickerItemThumbView) view else null
        } else {
            val view = dequeueReusableCellWithIdentifier("STICKER")
            if (view is StickerItemThumbView) view else null
        }

        if (thumb==null) {
            thumb = StickerItemThumbView.create(getDirector(), index, ImageDownloader.NO_DISK, ImageDownloader.NO_CACHE)
            thumb.setContentSize(CELL_WIDTH, PANEL_HEIGHT)
            thumb.setOnClickListener(this)

            if (index==0) {
                val text = SMLabel.create(getDirector(), "CLEAR\nALL", 45f)
                text.setColor(Color4F.TEXT_BLACK)
                text.setAnchorPoint(Vec2.MIDDLE)
                text.setPosition(thumb.getImageView().getContentSize().divide(2f))
                thumb.addChild(text)
            }
        }
        thumb.setTag(index)

        val m = _dict[ITEMS]!!.getList()!![index].getMap()!!
        thumb.setTag(index)
        val thumbPath = "$_resourceRootPath$THUMB/${m[NAME]!!.getString()}$IMG_EXTEND"
        thumb.setImagePath(thumbPath)
        return thumb
    }

    protected fun getItemCount(section: Int): Int {return _itemSize}
}