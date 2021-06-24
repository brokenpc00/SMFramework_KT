package com.interpark.smframework.util

import android.util.Log
import com.brokenpc.smframework.util.AppUtil
import com.brokenpc.smframework.util.FileUtils
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.lang.Exception
import java.nio.channels.FileChannel
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock

class FileManager() {

    companion object {
        const val SUCCESS = 0
        const val FAILED = -1

        const val DATA_ROOT_PATH = "Data/"
        const val IMAGE_ROOT = "Images/"
        const val SPRITE_BUILDER = "SpriteBuilder/"
        const val DOC_ROOT = "Doc/"
        const val SNAPSHOT_ROOT = "SnapShot/"
        const val ZIP_ROOT = "Zip/"
        const val XML_ROOT = "XML/"
        const val PRELOAD_ROOT = "Preload/"
        const val DB_ROOT = "DB/"
        const val EPUB_DOWN_ROOT = "InterparkEBook/Downloads/"
        const val EPUB_EXTRACT_ROOT = "InterparkEBook/Extract/"


        private var _instance:FileManager? = null
        public fun getInstance(): FileManager {
            if (_instance==null) {
                _instance = FileManager()
            }

            return _instance!!
        }

        @Throws (Throwable::class)
        @JvmStatic
        fun copyFile(sourceFile: File, destFile: File) {
            if (!destFile.parentFile.exists()) {
                destFile.parentFile.mkdir()
            }

            if (!destFile.exists()) {
                destFile.createNewFile()
            }

            var source:FileChannel? = null
            var destination: FileChannel? = null

            try {
                source = FileInputStream(sourceFile).channel
                destination = FileOutputStream(destFile).channel
            } finally {
                source?.close()
                destination?.close()
            }
        }
    }

    enum class FileType {
        Image,
        SpriteBuilder,
        Doc,
        SnapShot,
        ZIP,
        XML,
        Preload,
        DB,
        EPUB_DOWN,
        EPUB_EXTRACT
    }

    init {
        val fileUtils = FileUtils.getInstance()
        val rootPath = fileUtils.getWritablePath() + DATA_ROOT_PATH

        if (!fileUtils.isDirectoryExist(rootPath)) {
            fileUtils.createDirectory(rootPath)
        }

        getFullPath(FileType.Image)
        getFullPath(FileType.SpriteBuilder)
        getFullPath(FileType.Doc)
        getFullPath(FileType.SnapShot)
        getFullPath(FileType.ZIP)
        getFullPath(FileType.XML)
        getFullPath(FileType.Preload)
        getFullPath(FileType.DB)
        getFullPath(FileType.EPUB_DOWN)
        getFullPath(FileType.EPUB_EXTRACT)
    }

    fun getFullFilePath(type: FileType, fileName: String): String {
        return getFullPath(type) + fileName
    }

    fun getLocalFilePath(type: FileType, fileName: String): String {
        return getLocalPath(type) + fileName
    }

    fun getFullPath(type: FileType): String {
        val fileUtils = FileUtils.getInstance()
        val dir = fileUtils.getWritablePath() + getLocalPath(type)

        if (!fileUtils.isDirectoryExist(dir)) {
            if (fileUtils.isFileExist(dir)) {
                fileUtils.removeDirectory(dir)
            }

            fileUtils.createDirectory(dir)
        }

        return dir
    }

    fun getLocalPath(type: FileType): String {
        return DATA_ROOT_PATH + when (type) {
            FileType.Image -> IMAGE_ROOT
            FileType.SpriteBuilder -> SPRITE_BUILDER
            FileType.Doc -> DOC_ROOT
            FileType.SnapShot -> SNAPSHOT_ROOT
            FileType.ZIP -> ZIP_ROOT
            FileType.XML -> XML_ROOT
            FileType.Preload -> PRELOAD_ROOT
            FileType.DB -> DB_ROOT
            FileType.EPUB_DOWN -> EPUB_DOWN_ROOT
            FileType.EPUB_EXTRACT -> EPUB_EXTRACT_ROOT
        }
    }

    fun isFileExist(type: FileType, fileName: String): Boolean {
        return FileUtils.getInstance().isFileExist(getFullFilePath(type, fileName))
    }

    fun writeToFile(type: FileType, fileName: String, buffer: ByteArray?, bufSize: Int): Boolean {
        _mutex.lock()

        val fullPath = getFullFilePath(type, fileName)

        if (buffer==null || bufSize==0) return false

        val bSuccess = FileUtils.getInstance().writeDataToFile(buffer, fullPath)

        _mutex.unlock()

        return bSuccess
    }

    fun loadFromFile(type: FileType, fileName: String, error: IntArray): ByteArray? {
        val fileUtils = FileUtils.getInstance()

        val fullPath = getFullFilePath(type, fileName)

        error[0] = FAILED

        if (fileUtils.isFileExist(fullPath)) {
            error[0] = SUCCESS
            return fileUtils.getDataFromFile(fullPath)
        }

        return null
    }

    fun loadStringFromFile(type: FileType, fileName: String, error: IntArray): String {
        val fileUtils = FileUtils.getInstance()
        val fullPath = getFullFilePath(type, fileName)

        error[0] = FAILED
        if (fileUtils.isFileExist(fullPath)) {
            error[0] = SUCCESS
            return fileUtils.getStringFromFile(fullPath) ?: ""
        }

        return ""
    }

    fun removeFile(type: FileType, fileName: String): Boolean {
        return FileUtils.getInstance().removeFile(getFullFilePath(type, fileName))
    }

    fun renameFile(type: FileType, oldName: String, newName: String): Boolean {
        return FileUtils.getInstance().renameFile(
            getFullFilePath(type, oldName), getFullFilePath(
                type,
                newName
            )
        )
    }

    fun clearCache(type: FileType) {
        val fileList = getFileList(type)
        val pathName = getFullPath(type)
        val fs = FileUtils.getInstance()

        var numRemoved = 0

        for (i in fileList.indices) {
            val name = fileList[i]
            if (name.isNotEmpty()) {
                val fileFullPath = pathName + name
                if (fs.removeFile(fileFullPath)) {
                    numRemoved++
                }
            }
        }

        Log.i("FileManager", "[[[[[ file removed : $numRemoved files...")
    }

    fun createSaveFileName(): String {
        return createSaveFileName("", "")
    }

    fun createSaveFileName(prefix: String): String {
        return createSaveFileName(prefix, "")
    }

    fun createSaveFileName(prefix: String, postfix: String): String {
        val timeformat = AppUtil.getSaveFileTimeFormat()
        val rnd = Math.random() % 1000.0f
        return prefix + "_" + timeformat + "_" + rnd + "_" + postfix
    }

    fun getFilePath(fullPath: String): String {
        val pos = fullPath.lastIndexOf("/")
        return if (pos==-1) "" else fullPath.substring(0, pos)
    }

    fun getFileName(fullPath: String): String {
        return fullPath.substring(fullPath.lastIndexOf("/")+1)
    }

    fun copyFileOrDirectory(srcDir: String, dstDir: String) {
        try {
            val src = File(srcDir)
            val dst = File(dstDir, src.name)
            if (src.isDirectory) {
                val files = src.list()!!
                for (i in files.indices) {
                    val src1 = File(src, files[i]).path
                    val dst1 = dst.path
                    copyFileOrDirectory(src1, dst1)
                }
            } else {
                copyFile(src, dst)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun deleteFile(file: String): Boolean {
        val fs = FileUtils.getInstance()
        return fs.removeFile(file)
    }

    fun createFolder(path: String): Boolean {
        val fs = FileUtils.getInstance()
        return fs.createDirectory(path)
    }

    fun listFolder(path: String, entries: ArrayList<String>, directoryOnly: Boolean): Int {
        val fs = FileUtils.getInstance()

        if (!fs.isDirectoryExist(path)) {
            return entries.size
        }

        val dir = File(path)

        for (file in dir.listFiles()) {
            if (file.isDirectory) {
                entries.add(file.name)
            }
        }

        return entries.size
    }

    private val _mutex:Lock = ReentrantLock(true)

    fun getFileList(type: FileType): ArrayList<String> {
        val pathName = getFullPath(type)

        val fileList = ArrayList<String>()
        val fs = FileUtils.getInstance()
        if (!fs.isDirectoryExist(pathName)) {
            return fileList
        }

        val dir = File(pathName)
        val files = dir.listFiles()
        for (file in files) {
            fileList.add(file.name)
        }

        return fileList
    }
}