package com.brokenpc.smframework.util.ImageManager

class PhoneAlbum() {
    private var id: Int = -1
    private var bucketId: Long = -1
    private var albumIndex: Int = -1
    private var name: String = ""
    private var coverUri: String = ""   // 애표 이미지 uri
    private var photoCount: Int = -1
    private var albumPhotos:ArrayList<PhonePhoto> = ArrayList()

    fun getId(): Int { return id}

    fun setId(id:Int) {this.id = id}

    fun getBucketId(): Long {return bucketId}

    fun setBucketId(bucketId: Long) {this.bucketId = bucketId}

    fun getAlbumIndex(): Int { return albumIndex}

    fun setAlbumIndex(index: Int) {this.albumIndex = index}

    fun getName(): String {return name}

    fun setName(name: String) {this.name = name}

    fun getCoverUri(): String {return coverUri}

    fun setCoverUri(uri: String) {this.coverUri = uri}

    fun getPhotoCount(): Int {return photoCount}

    fun setPhotoCount(count: Int) {this.photoCount = count}

    fun getAlbumPhotos(): ArrayList<PhonePhoto> {
        return albumPhotos
    }

    fun setAlbumPhotos(array: ArrayList<PhonePhoto>) {
        this.albumPhotos.clear()
        albumPhotos.addAll(array)
    }
}