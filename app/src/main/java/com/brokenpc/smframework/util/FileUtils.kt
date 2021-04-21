package com.brokenpc.smframework.util

import android.content.res.AssetManager
import com.brokenpc.smframework.ClassHelper
import java.util.zip.ZipFile
import com.brokenpc.smframework.util.Value.ValueMap
import java.io.*
import java.nio.charset.Charset
import java.util.zip.ZipEntry

class FileUtils {
    companion object {
        private var _instance: FileUtils? = null
        private var ASSETS_FOLDER_NAME: String = "assets/"
        private var ASSETS_FOLDER_NAME_LENGTH: Int = 7
        private var assetsmanager: AssetManager? = null
        private var obbfile: ZipFile? = null
        private var apkprefix: String = "assets/"

        @JvmStatic
        fun getInstance(): FileUtils {
            if (_instance==null) {
                _instance = FileUtils()
            }

            return _instance!!
        }
    }

    protected var _storagePath: String = ""
    protected var _writablePath: String = ""
    protected var _defaultResRootPath: String = ""
    protected var _fullPathCache:HashMap<String, String> = HashMap()
    protected var _originalSearchPaths: ArrayList<String> = ArrayList()
    protected var _searchPathArray: ArrayList<String> = ArrayList()
    protected var _searchResolutionsOrderArray: ArrayList<String> = ArrayList()
    protected var _filenameLookupDict:ValueMap = ValueMap()


    enum class Status {
        OK,
        NotExists,
        OpenFailed,
        ReadFailed,
        NotInitialized,
        TooLarge,
        ObtainSizeFailed
    }

    init {
        init()
    }

    fun init() {
        _defaultResRootPath = ASSETS_FOLDER_NAME
        assetsmanager = ClassHelper.getAssetManager()
        val assetsPath:String = ClassHelper.getAssetsPath()

        if (assetsPath.contains("/obb/", true)) {
            try {
                obbfile = ZipFile(assetsPath)
            } catch (e: IOException) {

            }
        }

        _searchPathArray.add(_defaultResRootPath)
        _searchResolutionsOrderArray.add("")
    }

    fun getWritablePath(): String {
        val dir:StringBuffer = StringBuffer("")

        val tmp:String = ClassHelper.getWritablePath()
        return if (tmp.isNotEmpty()) {
            dir.append(tmp).append("/")
            dir.toString()
        } else { "" }
    }

    fun setSearchPath(searchPath: ArrayList<String>) {
        var existDefaultRootPath: Boolean = false
        _originalSearchPaths = searchPath

        _fullPathCache.clear()
        _searchPathArray.clear()

        for (path in _originalSearchPaths) {
            var prefix:String = ""
            var fullPath:String = ""

            if (!isAbsolutePath(path)) {
                prefix = _defaultResRootPath
            }

            fullPath = prefix + path;

            if (path.isNotEmpty() && path.substring(path.length-1)!="/") {
                fullPath += "/"
            }
            if (!existDefaultRootPath && path.compareTo(_defaultResRootPath)==0) {
                existDefaultRootPath = true
            }

            _searchPathArray.add(fullPath)
        }

        if (!existDefaultRootPath) {
            _searchPathArray.add(_defaultResRootPath)
        }
    }

    fun isDirectoryExistInternal(dirPath:String):Boolean {
        return if (dirPath.isEmpty()) false else ClassHelper.isDirectoryExist(dirPath)
    }

    fun isAbsolutePath(path:String): Boolean {
        return path.substring(0, 1)=="/" || path.contains(_defaultResRootPath)
    }

    fun isDirectoryExist(dirPath: String): Boolean {
        if (isAbsolutePath(dirPath)) return isDirectoryExistInternal(dirPath)

        val cacheDir: String = _fullPathCache[dirPath]!!
        if (cacheDir.isNotEmpty()) isDirectoryExistInternal(cacheDir)

        var fullPath: String = ""
        for (search in _searchPathArray) {
            for (resolution in _searchResolutionsOrderArray) {
                fullPath = fullPathForFileName(search + dirPath + resolution)
                if (isDirectoryExistInternal(fullPath)) {
                    _fullPathCache[dirPath] = fullPath
                    return true
                }
            }
        }
        return false
    }

    fun fullPathForFileName(fileName: String): String {
        if (fileName.isEmpty()) return ""

        if (isAbsolutePath(fileName)) return fileName

        val cacheDir: String = _fullPathCache[fileName]!!
        if (cacheDir.isNotEmpty()) return cacheDir

//        val newFileName:String = getNew

        return ""
    }

    fun getNewFileName(fileName: String): String {
        var newFileName: String = ""

        val value:Value? = _filenameLookupDict[fileName]
        if (value==null) {
            newFileName = fileName
        } else {
            newFileName = value.getString()
        }

        var pos:Int = newFileName.indexOf("../")
        if (pos<=0) {
            // not found or first position
            return newFileName
        }

        val v: ArrayList<String> = ArrayList(3)
        var change: Boolean = false

        var size: Int = newFileName.length
        var idx: Int = 0

        var keepLoop: Boolean = true

        while (keepLoop) {
            pos = newFileName.indexOf("/", idx)
            var tmp:String = ""
            if (pos==-1) {
                tmp = newFileName.substring(idx)
                keepLoop = false
            } else {
                tmp = newFileName.substring(idx, pos+1)
            }

            val t = v.size
            if (t>0 && !v[t-1].contains("../") && (tmp.contains("../") || tmp.contains(".."))) {
                v.removeAt(v.size-1)
                change = true
            } else {
                v.add(tmp)
            }
            idx = pos + 1
        }

        if (change) {
            newFileName = ""
            for (s in v) {
                newFileName += s
            }
        }

        return newFileName
    }

    fun getPathForFileName(fileName: String, resolutionDirectory: String, searchPath: String): String {
        var file = fileName
        var file_path = ""
        val pos = fileName.lastIndexOf("/")
        if (pos!=-1) {
            file_path = fileName.substring(0, pos+1)
            file = fileName.substring(pos+1)
        }

        var path = searchPath
        path += file_path
        path += resolutionDirectory
        path = getFullPathForDirectoryAndFilename(path, file)

        return path
    }

    fun getFullPathForDirectoryAndFilename(dirctory: String, fileName: String): String {
        var ret = dirctory
        if (dirctory.isNotEmpty()) {
            val lastChar = dirctory.substring(dirctory.length-1)
            if (lastChar=="/") {
                ret += "/"
            }
        }

        ret += fileName

        if (!isFileExistInternal(ret)) {
            ret = ""
        }

        return ret
    }

    interface VOID_BOOLEAN_CALLBACK {
        fun func(b: Boolean)
    }

    fun createDirectory(path: String): Boolean {
        if (isDirectoryExist(path)) return true

        var start:Int = 0
        var found:Int = path.indexOf("/\\", start)
        var subPath:String = ""

        val dirs:ArrayList<String> = ArrayList()
        if (found!=-1) {
            while (true) {
                subPath = path.substring(start, found - start+1)
                if (subPath.isNotEmpty()) {
                    dirs.add(subPath)
                }

                start = found+1
                found = path.indexOf("/\\", start)
                if (found==-1) {
                    if (start<path.length) {
                        dirs.add(path.substring(start))
                    }
                    break
                }
            }
        }

        var dir:File? = null
        subPath = ""
        for (a in dirs) {
            subPath += a
            dir = File(subPath)
            if (dir.exists()) {
                return false
            }
            dir.mkdir()
        }
        return true
    }

    fun removeDirectory(dirPath: String): Boolean {
        val dir = File(dirPath)
        return dir.delete()
    }

    fun isFileExistInternal(filePath: String): Boolean {
        if (filePath.isEmpty()) {
            return false
        }

        var bFound = false
        if (filePath.substring(0, 1)!="/") {
            var s:Int = 0

            if (filePath.indexOf(_defaultResRootPath)==0) {
                s += _defaultResRootPath.length
            }

            val obbStr:String? = filePath.substring(s)

            if (obbfile!=null && obbStr!=null && obbStr.isNotEmpty()) {
                bFound = true
            } else if (assetsmanager!=null) {
                try {
                    val Is:InputStream? = assetsmanager!!.open(obbStr!!)
                    if (Is!=null) {
                        bFound = true
                        Is.close()
                    }
                } catch (e: IOException) {

                }
            }
        } else {
            val file:File? = File(filePath)
            if (file!=null && file.exists()) {
                bFound = true
            }
        }

        return bFound
    }

    fun isFileExist(fileName: String): Boolean {
        if (isAbsolutePath(fileName)) {
            return isFileExistInternal(fileName)
        } else {
            val fullPath:String = fullPathForFileName(fileName)
            return fullPath.isNotEmpty()
        }
    }

    fun writeDataToFile(data: ByteArray, fullPath: String): Boolean {
        val file = File(fullPath)
        if (file.canWrite()) {
            try {
                if (file.createNewFile()) {
                    val stream = FileOutputStream(file)
                    stream.write(data)
                    stream.close()
                    return true
                }
            } catch (e: IOException) {

            }
        }
        return false
    }

    fun writeStringToFile(dataStr: String, fullPath: String): Boolean {
        return writeDataToFile(dataStr.toByteArray(), fullPath)
    }

    fun getInternalPathContents(fullPath: String): ByteArray? {
        var relativePath = ""
        val position = fullPath.indexOf(apkprefix)

        if (0==position) {
            relativePath = fullPath.substring(apkprefix.length)
        } else {
            relativePath = fullPath
        }

        if (obbfile!=null) {
            val entry: ZipEntry = obbfile!!.getEntry(relativePath)
            return entry.extra
        }

        if (null== assetsmanager) {
            return null
        }

        try {
            val IS = assetsmanager!!.open(relativePath)
            val data = ByteArray(IS.available())
            IS.read(data)
            IS.close()

            return data
        } catch (e: IOException) {

        }

        return null
    }

    fun getAbsolutePathContents(fileName: String): ByteArray? {
        if (fileName.isEmpty()) return null

        val fs = FileUtils.getInstance()
        val fullPath = fs.fullPathForFileName(fileName)

        if (fullPath.isEmpty()) return null

        val fp = File(fullPath)
        if (!fp.canRead()) return null

        try {
            val fis = FileInputStream(fp)
            val size = fis.available()
            val data = ByteArray(size)
            fis.read(data)
            fis.close()

            return data
        } catch (e: IOException) {

        }

        return null
    }

    fun getContents(fileName: String): ByteArray? {
        if (fileName.isEmpty()) return null

        val fullPath = fullPathForFileName(fileName)

        return if (fullPath.substring(0, 1)=="/") getAbsolutePathContents(fullPath) else getInternalPathContents(fullPath)
    }

    fun getDataFromFile(fullPath: String): ByteArray? {
        return getContents(fullPath)
    }

    fun getStringFromFile(fileName: String): String? {
        val fullPath = FileUtils.getInstance().fullPathForFileName(fileName)
        val fs = File(fullPath)
        try {
            val fis = FileInputStream(fs)
            val data:ByteArray? = getContents(fileName)
            return String(data!!)
        } catch (e: IOException) {

        }

        return null
    }

    fun removeFile(path: String): Boolean {
        val fullPath = FileUtils.getInstance().fullPathForFileName(path)
        val fs = File(fullPath)

        return if (fs.exists()) fs.delete() else return false
    }

    fun renameFile(oldFullPath: String, newFullPath: String): Boolean {
        val src = File(oldFullPath)
        if (!src.exists()) {
            return false
        }

        val dsc = File(newFullPath)
        return src.renameTo(dsc)
    }
}