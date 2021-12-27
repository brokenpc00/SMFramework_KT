package com.brokenpc.smframework.util.ImageManager

class PhonePhoto() {
    private var id: Int = -1
    private var photoIndex: Int = -1
    private var albumName: String = ""
    private var photoUri: String = ""
    private var orientation: Int = 0 // portrait default

    fun getId(): Int {return id}

    fun setId(id: Int) {this.id = id}

    fun getPhotoIndex(): Int {return photoIndex}

    fun setPhotoIndex(index: Int) {this.photoIndex = index}

    fun getAlbumName(): String {return albumName}

    fun setAlbumName(name: String) {this.albumName = name}

    fun getPhotoUri(): String {return photoUri}

    fun setPhotoUri(uri: String) {this.photoUri = uri}

    fun getOrientation(): Int {return orientation}

    fun setOrientation(orientation: Int) {this.orientation = orientation}
}