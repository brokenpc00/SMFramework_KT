package com.brokenpc.smframework.util

import java.io.*
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class IOUtils {
    companion object {
        @JvmStatic
        fun closeSilently(c:Closeable?) {
            if (c==null) return
            try {
                c.close()
            } catch (t:Throwable) {
                // Nothing To Do
            }
        }

        @JvmStatic
        fun arrayToBytes(array: ArrayList<Byte>): ByteArray? {

            val bos:ByteArrayOutputStream = ByteArrayOutputStream()

            try {
                val oos:ObjectOutputStream = ObjectOutputStream(bos)
                oos.writeObject(array)
                return bos.toByteArray()
            } catch (e:IOException) {
                return null
            }
        }

        @JvmStatic
        fun bytesToArray(bytes:ByteArray): ArrayList<Byte>? {
            try {
                return toObject(bytes) as ArrayList<Byte>
            } catch (e:IOException) {
                return null
            } catch (e:ClassNotFoundException) {
                return null
            }
        }

        @JvmStatic
        @Throws(IOException::class)
        fun toByteArray(any: Any):ByteArray? {
            var bytes:ByteArray? = null
            var bos:ByteArrayOutputStream? = null
            var oos:ObjectOutputStream? = null

            try {
                bos = ByteArrayOutputStream()
                oos = ObjectOutputStream(bos)
                oos.writeObject(any)
                oos.flush()
                bytes = bos.toByteArray()
            } finally {
                oos?.close()
                bos?.close()
            }

            return bytes
        }

        @JvmStatic
        @Throws(IOException::class, ClassNotFoundException::class)
        fun toObject(bytes: ByteArray): Any? {
            var obj:Any? = null
            var bis:ByteArrayInputStream? = null
            var ois:ObjectInputStream? = null

            try {
                bis = ByteArrayInputStream(bytes)
                ois = ObjectInputStream(bis)
                obj = ois.readObject()
            } finally {
                bis?.close()
                ois?.close()
            }

            return obj
        }

        @JvmStatic
        fun toString(bytes: ByteArray): String {return String(bytes)}

        @JvmStatic
        @Throws(IOException::class)
        fun copy(src:File, dst:File) {
            val inStream:FileInputStream = FileInputStream(src)
            val outStream:FileOutputStream = FileOutputStream(dst)

            inStream.channel.transferTo(0, inStream.channel.size(), outStream.channel)

            inStream.close()
            outStream.close()
        }

        @JvmStatic
        @Throws (IOException::class)
        fun readFile(fileName:String): ByteArray? {
            return readFile(File(fileName))
        }

        @JvmStatic
        @Throws (IOException::class)
        fun readFile(file:File): ByteArray? {
            if (!file.exists()) return null

            var inputStream:FileInputStream? = null

            try {
                inputStream = FileInputStream(file)

                val fileChannel:FileChannel = inputStream.channel
                val mappedByteBuffer: MappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size())

                val dataBytes:ByteArray = ByteArray(mappedByteBuffer.remaining())
                mappedByteBuffer.get(dataBytes)

                return dataBytes
            } finally {
                closeSilently(inputStream)
            }
        }

        @JvmStatic
        @Throws (IOException::class)
        fun writeFile(data:ByteArray, fileName:String) {
            val out:FileOutputStream = FileOutputStream(fileName)
            out.write(data)
            out.close()
        }

        @JvmStatic
        @Throws (IOException::class)
        fun toByteArray(ins:InputStream): ByteArray {
            val out:ByteArrayOutputStream = ByteArrayOutputStream()
            var read:Int = 0
            val buffer:ByteArray = ByteArray(1024)
            while (read!=-1) {
                read = ins.read(buffer)
                if (read!=-1) {
                    out.write(buffer, 0, read)
                }
            }
            out.close()

            return out.toByteArray()
        }

    }

}