package com.brokenpc.app.scene

import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.SMScene
import com.brokenpc.smframework.base.SMView
import com.brokenpc.smframework.base.SceneParams
import com.brokenpc.smframework.base.types.IndexPath
import com.brokenpc.smframework.view.SMTableView

class HelloBrokenpcScene(director:IDirector) : SMScene(director), SMTableView.CellForRowAtIndexPath, SMTableView.NumberOfRowsInSection, SMView.OnClickListener {
    private var _mainScene:HelloBrokenpcScene? = null
    private lateinit var _tableView:SMTableView

    companion object {
        @JvmStatic
        fun create(director: IDirector, params: SceneParams, type: SwipeType): HelloBrokenpcScene {
            val scene = HelloBrokenpcScene(director)
            scene.initWithSceneParams(params, type)
            return scene
        }
    }

    override fun init(): Boolean {
        return super.init()
    }

    override fun cellForRowAtIndexPath(indexPath: IndexPath): SMView {
        TODO("Not yet implemented")
    }

    override fun numberOfRowsInSection(section: Int): Int {
        TODO("Not yet implemented")
    }

    override fun onClick(view: SMView?) {
        TODO("Not yet implemented")
    }


}