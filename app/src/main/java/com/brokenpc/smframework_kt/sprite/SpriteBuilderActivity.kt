package com.brokenpc.smframework_kt.sprite

import android.app.Activity
import android.graphics.Bitmap
import android.media.Image
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import com.brokenpc.smframework.ClassHelper
import com.brokenpc.smframework.SMDirector
import com.brokenpc.smframework.SMSurfaceView
import com.brokenpc.smframework_kt.R
import com.brokenpc.smframework_kt.sprite.sspack.ImagePacker
import com.brokenpc.smframework_kt.sprite.sspack.ResInfo

class SpriteBuilderActivity : FragmentActivity(), ImagePacker.OnLogListener {

    lateinit var _text: TextView
    lateinit var _imageView: ImageView
    lateinit var _scroll: ScrollView

    private var _surfaceView: SMSurfaceView? = null
    private var _displayRawWidth:Int = 0
    private var _displayRawHeight:Int = 0


    val SPRITE_INFO: ArrayList<ResInfo> = arrayListOf(
            ResInfo(R.raw.progress_spinner_white),
            ResInfo(R.raw.loading_scissor),
            ResInfo(R.raw.round_box_black),
            ResInfo(R.raw.ab_solid_shadow, ResInfo.Align.TOP or ResInfo.Align.CENTER_HORIZONTAL),
            ResInfo(R.raw.fit_new),

            ResInfo(R.raw.fit_top),
            ResInfo(R.raw.fit_bottom),
            ResInfo(R.raw.fit_dress),
            ResInfo(R.raw.fit_outer),
            ResInfo(R.raw.fit_bag),
            ResInfo(R.raw.fit_acc),
            ResInfo(R.raw.fit_hair),
            ResInfo(R.raw.fit_suit),

            ResInfo(R.raw.fit_top_blur),
            ResInfo(R.raw.fit_bottom_blur),
            ResInfo(R.raw.fit_dress_blur),
            ResInfo(R.raw.fit_outer_blur),
            ResInfo(R.raw.fit_bag_blur),
            ResInfo(R.raw.fit_acc_blur),
            ResInfo(R.raw.fit_hair_blur),

            ResInfo(R.raw.ic_camera_switch),
            ResInfo(R.raw.ic_camera_flash_off),
            ResInfo(R.raw.ic_camera_flash_auto),
            ResInfo(R.raw.ic_camera_flash_on),

            ResInfo(R.raw.ic_floating_camera),
            ResInfo(R.raw.ic_floating_closet),
            ResInfo(R.raw.ic_floating_cutout),
            ResInfo(R.raw.ic_floating_shop),

            ResInfo(R.raw.fituin_f),
            ResInfo(R.raw.fituin_i),
            ResInfo(R.raw.fituin_t),
            ResInfo(R.raw.fituin_u),
            ResInfo(R.raw.fituin_n),

            ResInfo(R.raw.ic_menu_back_seg, ResInfo.Align.LEFT or ResInfo.Align.CENTER_VERTICAL),

            ResInfo(R.raw.ic_cutout_confirm),
            ResInfo(R.raw.ic_cutout_keep),
            ResInfo(R.raw.ic_cutout_remove),
            ResInfo(R.raw.ic_cutout_select),
            ResInfo(R.raw.ic_cutout_undo),

            ResInfo(R.raw.ic_left_model),
            ResInfo(R.raw.ic_left_add),
            ResInfo(R.raw.ic_left_fituin),
            ResInfo(R.raw.ic_left_lookbook),
            ResInfo(R.raw.ic_left_more),
            ResInfo(R.raw.ic_left_setting),
            ResInfo(R.raw.ic_left_shop),

            ResInfo(R.raw.ic_scissors),

            ResInfo(R.raw.profile_gender_women),
            ResInfo(R.raw.profile_gender_men),
            ResInfo(R.raw.profile_gender_girl),
            ResInfo(R.raw.profile_gender_boy),
            ResInfo(R.raw.profile_make_confirm),
            ResInfo(R.raw.profile_make_full),
            ResInfo(R.raw.profile_edittext_empty),

            ResInfo(R.raw.camera_focus_rect),
            ResInfo(R.raw.camera_guide_head),
            ResInfo(R.raw.camera_guide_foot),
            ResInfo(R.raw.camera_guide_chest),

            ResInfo(R.raw.body_shoulder),
            ResInfo(R.raw.body_hip),
            ResInfo(R.raw.body_foot),
            ResInfo(R.raw.body_side_shoulder, ResInfo.Align.RIGHT),
            ResInfo(R.raw.body_side_hip, ResInfo.Align.RIGHT),
            ResInfo(R.raw.body_side_foot, ResInfo.Align.RIGHT),

            ResInfo(R.raw.circle_panel_s),
            ResInfo(R.raw.sticker_ic_close),
            ResInfo(R.raw.sticker_ic_delete),
            ResInfo(R.raw.sticker_ic_more),
            ResInfo(R.raw.sticker_ic_resize),
            ResInfo(R.raw.sticker_ic_rotate),
            ResInfo(R.raw.sticker_ic_size),
            ResInfo(R.raw.sticker_ic_tag),
            ResInfo(R.raw.sticker_ic_add),
            ResInfo(R.raw.sticker_ic_confirm),

            ResInfo(R.raw.shop_heart_unchecked),
            ResInfo(R.raw.shop_heart_checked),
            ResInfo(R.raw.shop_add_closet_thin),
            ResInfo(R.raw.shop_add_closet2),
            ResInfo(R.raw.shop_add_closet),
            ResInfo(R.raw.shop_add_icon_off),
            ResInfo(R.raw.shop_add_icon_on),

            ResInfo(R.raw.shop_icon_woman),
            ResInfo(R.raw.shop_icon_man),
            ResInfo(R.raw.shop_icon_girl),
            ResInfo(R.raw.shop_icon_boy),

            ResInfo(R.raw.style_bullet_clock),
            ResInfo(R.raw.style_bullet_closet),
            ResInfo(R.raw.style_icon_del01),
            ResInfo(R.raw.style_icon_share01),
            ResInfo(R.raw.style_b),

            ResInfo(R.raw.tryon_man_body),
            ResInfo(R.raw.tryon_man_arm, ResInfo.Align.CENTER_HORIZONTAL or ResInfo.Align.TOP),

            ResInfo(R.raw.alert_arrow_white, ResInfo.Align.CENTER_HORIZONTAL or ResInfo.Align.TOP),

            ResInfo(R.raw.alert_close),
            ResInfo(R.raw.top_btn_close),
            ResInfo(R.raw.top_btn_confirm),
            ResInfo(R.raw.btn_check_layer),

            ResInfo(R.raw.ic_share),
            ResInfo(R.raw.ic_save),
            ResInfo(R.raw.ic_model_edit),
            ResInfo(R.raw.ic_model_close),

            ResInfo(R.raw.error_fituin),
            ResInfo(R.raw.error_fituin_feel),
            ResInfo(R.raw.loading_triangle, ResInfo.Align.CENTER_HORIZONTAL or ResInfo.Align.TOP),
            ResInfo(R.raw.intro_scissor),
            ResInfo(R.raw.bullet_stamp),
            ResInfo(R.raw.cn_error_fituin),
            ResInfo(R.raw.cn_fituin_ci),

            ResInfo(R.raw.replay_icon),
            ResInfo(R.raw.ic_action_search),
            ResInfo(R.raw.ic_action_folder),
            ResInfo(R.raw.ic_phone_android),
            ResInfo(R.raw.ic_view_list),
            ResInfo(R.raw.ic_view_module),

            ResInfo(R.raw.edit_ic_tag, ResInfo.Align.CENTER_HORIZONTAL or ResInfo.Align.BOTTOM),
            ResInfo(R.raw.edit_color_ic_brightness),
            ResInfo(R.raw.edit_color_ic_contrast),
            ResInfo(R.raw.edit_color_ic_saturation),
            ResInfo(R.raw.edit_color_ic_temperature),

            ResInfo(R.raw.edit_body_ic_leg),
            ResInfo(R.raw.edit_body_ic_head),
            ResInfo(R.raw.edit_body_ic_waist),
            ResInfo(R.raw.edit_body_ic_slim),

            ResInfo(R.raw.edit_face_ic_eye),
            ResInfo(R.raw.edit_face_ic_chin),

            ResInfo(R.raw.edit_button_reset),
            ResInfo(R.raw.edit_button_ab),
            ResInfo(R.raw.edit_button_fit),
            ResInfo(R.raw.edit_ic_body),
            ResInfo(R.raw.edit_ic_color),
            ResInfo(R.raw.edit_ic_face),
            ResInfo(R.raw.edit_ic_fit),

            ResInfo(R.raw.error_image_file),
            ResInfo(R.raw.new_tag),
            ResInfo(R.raw.fit_balloon, ResInfo.Align.CENTER_HORIZONTAL or ResInfo.Align.BOTTOM),
            ResInfo(R.raw.shop_new, ResInfo.Align.LEFT or ResInfo.Align.TOP),

            ResInfo(R.raw.exp_shade_scissor),

            ResInfo(R.raw.clip_inner, ResInfo.Align.LEFT or ResInfo.Align.TOP),
            ResInfo(R.raw.clip_outer, ResInfo.Align.LEFT or ResInfo.Align.TOP),

            ResInfo(R.raw.ic_wear),
            ResInfo(R.raw.ic_check),
            ResInfo(R.raw.ic_arrow),
    )


    companion object {
        val BASE_TEXTURE_FILE_NAME = "tex_closet"
        const val MAX_WIDTH = 1024
        const val MAX_HEIGHT = 1024
        const val IMAGE_PADDING = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.spritebuilder_main)
        _scroll = findViewById(R.id.scroll)
        _imageView = findViewById(R.id.image)
        _text = findViewById(R.id.text)

        var displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        _displayRawWidth = displayMetrics.widthPixels
        _displayRawHeight = displayMetrics.heightPixels
//        }

        _surfaceView = SMSurfaceView(this)
        addContentView(_surfaceView, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))

        _surfaceView?.getDirector()?.setDisplayRawSize(_displayRawWidth, _displayRawHeight)
//        ClassHelper.init(this)

        val packer = ImagePacker(SMDirector.getDirector(), this, BASE_TEXTURE_FILE_NAME, SPRITE_INFO, true, true, MAX_WIDTH, MAX_HEIGHT, IMAGE_PADDING, this)
        packer.execute()

        Log.i("!!!!!", "======================= FINISH =======================")
    }

    override fun onLog(text: String) {
        _text.append("$text\n")
        _scroll.post {
            _scroll.fullScroll(View.FOCUS_DOWN)
        }
    }

    override fun onBitmap(bitmap: Bitmap) {
        _imageView.setImageBitmap(bitmap)
        _scroll.post {
            _scroll.fullScroll(View.FOCUS_DOWN)
        }
    }
}