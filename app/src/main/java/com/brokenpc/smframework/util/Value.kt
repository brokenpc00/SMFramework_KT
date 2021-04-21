package com.brokenpc.smframework.util

import com.brokenpc.smframework.downloader.DownloadRunnable

class Value {

    companion object {
    }

    class ValueList : ArrayList<Value> {
        constructor() : super()
        constructor(list: ValueList) : super(list)
    }

    class ValueMap : HashMap<String, Value> {
        constructor() : super()
        constructor(map: ValueMap) : super(map)
    }

    class ValueMapInKey : HashMap<Int, Value> {
        constructor() : super()
        constructor(intKeyMap: ValueMapInKey) : super(intKeyMap)
    }

    class Field() {
        init {
            clear()
        }
        fun set(f:Field) {
            byteVal = f.byteVal
            intVal = f.intVal
            longVal = f.longVal
            floatVale = f.floatVale
            doubleVal = f.doubleVal
            booleanVal = f.booleanVal
            stringVal = f.stringVal
            listVal = f.listVal
            mapVal = f.mapVal
            intKeyVal = f.intKeyVal
        }
        fun clear() {
            byteVal = '\u0000'
            intVal = 0
            longVal = 0L
            floatVale = 0f
            doubleVal = 0.0
            booleanVal = false
            stringVal = ""
            listVal = null
            mapVal = null
            intKeyVal = null
        }

        var byteVal:Char = '\u0000'
        var intVal:Int = 0
        var longVal:Long = 0L
        var floatVale:Float = 0f
        var doubleVal:Double = 0.0
        var booleanVal:Boolean = false
        var stringVal:String = ""
        var listVal:ValueList? = null
        var mapVal:ValueMap? = null
        var intKeyVal:ValueMapInKey? = null
    }

    enum class Type {
        NONE,
        BYTE,
        INTEGER,
        FLOAT,
        LONG,
        DOUBLE,
        BOOLEAN,
        STRING,
        LIST,
        MAP,
        INT_KEY_MAP
    }

    protected val _field:Field = Field()
    private var _type:Type = Type.NONE

    constructor() {
        _type = Type.NONE
        _field.clear()
    }
    constructor(v: Char) {
        _type = Type.BYTE
        _field.byteVal = v
    }
    constructor(v: Int) {
        _type = Type.INTEGER
        _field.intVal = v
    }
    constructor(v: Long) {
        _type = Type.LONG
        _field.longVal = v
    }
    constructor(v: Float) {
        _type = Type.FLOAT
        _field.floatVale = v
    }
    constructor(v: Double) {
        _type = Type.DOUBLE
        _field.doubleVal = v
    }
    constructor(v: Boolean) {
        _type = Type.BOOLEAN
        _field.booleanVal = v
    }
    constructor(v: String) {
        _type = Type.STRING
        _field.stringVal = v
    }
    constructor(v: ValueList) {
        _type = Type.LIST
        _field.listVal = v
    }
    constructor(v: ValueMap) {
        _type = Type.MAP
        _field.mapVal = v
    }
    constructor(v: ValueMapInKey) {
        _type = Type.INT_KEY_MAP
        _field.intKeyVal
    }
    constructor(other: Value) {
        _field.set(other._field)
    }
    constructor(other: Value, move: Boolean) {
        _field.set(other._field)
        if (move) {
            other._field.clear()
            other._type = Type.NONE
        }
    }

    fun set(v: Char): Value {
        reset(Type.BYTE)._field.byteVal = v
        return this
    }
    fun set(v: Int): Value {
        reset(Type.INTEGER)._field.intVal = v
        return this
    }
    fun set(v: Long): Value {
        reset(Type.LONG)._field.longVal = v
        return this
    }
    fun set(v: Float): Value {
        reset(Type.FLOAT)._field.floatVale = v
        return this
    }
    fun set(v: Double): Value {
        reset(Type.DOUBLE)._field.doubleVal = v
        return this
    }
    fun set(v: Boolean): Value {
        reset(Type.BOOLEAN)._field.booleanVal = v
        return this
    }
    fun set(v: String): Value {
        reset(Type.STRING)._field.stringVal = v
        return this
    }
    fun set(v: ValueList): Value {
        reset(Type.LIST)._field.listVal = v
        return this
    }
    fun set(v: ValueMap): Value {
        reset(Type.MAP)._field.mapVal = v
        return this
    }
    fun set(v: ValueMapInKey): Value {
        reset(Type.INT_KEY_MAP)._field.intKeyVal = v
        return this
    }
    fun set(other: Value): Value {
        if (!equal(other)) {
            reset(other._type)

            when (other._type) {
                Type.BYTE -> _field.byteVal = other._field.byteVal
                Type.INTEGER -> _field.intVal = other._field.intVal
                Type.LONG -> _field.longVal = other._field.longVal
                Type.FLOAT -> _field.floatVale = other._field.floatVale
                Type.DOUBLE -> _field.doubleVal = other._field.doubleVal
                Type.BOOLEAN -> _field.booleanVal = other._field.booleanVal
                Type.STRING -> _field.stringVal = other._field.stringVal
                Type.LIST -> _field.listVal = if (other._field.listVal!=null) ValueList(other._field.listVal!!) else null
                Type.MAP -> _field.mapVal = if (other._field.mapVal!=null) ValueMap(other._field.mapVal!!) else null
                Type.INT_KEY_MAP -> _field.intKeyVal = if (other._field.intKeyVal!=null) ValueMapInKey(other._field.intKeyVal!!) else null
            }
        }
        return this
    }

    fun reset(type:Type): Value {
        return  this
    }

    fun equal(v: Value): Boolean {
        if (this==v) return true

        if (v._type!=v._type) return false

        if (this.isNull()) return true

        return when (_type) {
            Type.BYTE -> v._field.byteVal==_field.byteVal
            Type.INTEGER -> v._field.intVal==_field.intVal
            Type.LONG -> v._field.longVal==_field.longVal
            Type.FLOAT -> v._field.floatVale==_field.floatVale
            Type.DOUBLE -> v._field.doubleVal==_field.doubleVal
            Type.BOOLEAN -> v._field.booleanVal==_field.booleanVal
            Type.STRING -> v._field.stringVal==_field.stringVal
            Type.LIST -> {
                if (_field.listVal==null || v._field.listVal==null) return false

                val v1 = _field.listVal!!
                val v2 = v._field.listVal!!
                val size = v1.size
                if (size==v2.size) {
                    for (i in 0 until size) {
                        if (!v1[i].equal(v2[i])) return false
                    }
                    return true
                }
                false
            }
            Type.MAP -> {
                if (_field.mapVal==null || v._field.mapVal==null) return false

                val m1 = _field.mapVal!!
                val m2 = v._field.mapVal!!

                if (m1.keys.size!=m2.keys.size) return false

                val keys:Set<String> = m1.keys
                for (key in keys) {
                    val value: Value? = m2[key]
                    if (value==null || !value.equal(m1[key]!!)) return false
                }
                true
            }
            Type.INT_KEY_MAP -> {
                if (_field.intKeyVal==null || v._field.intKeyVal==null) return false

                val m1:ValueMapInKey = _field.intKeyVal!!
                val m2:ValueMapInKey = v._field.intKeyVal!!

                if (m1.keys.size!=m2.keys.size) return false

                val keys:Set<Int> = m1.keys
                for (key in keys) {
                    val value:Value? = m2[key]
                    if (value==null || !value.equal(m1[key]!!)) return false
                }
                true
            }
            else -> false
        }
    }

    fun isNull():Boolean {return _type==Type.NONE}

    fun getType():Type {return _type}

    fun clear() {
        when (_type) {
            Type.BYTE -> _field.byteVal = '\u0000'
            Type.INTEGER -> _field.intVal = 0
            Type.LONG -> _field.longVal = 0L
            Type.FLOAT -> _field.floatVale = 0f
            Type.DOUBLE -> _field.doubleVal = 0.0
            Type.BOOLEAN -> _field.booleanVal = false
            Type.STRING -> _field.stringVal = ""
            Type.LIST -> _field.listVal = null
            Type.MAP -> _field.mapVal = null
            Type.INT_KEY_MAP -> _field.intKeyVal = null
            else -> {}
        }
    }

    fun getByte(): Char {return _field.byteVal}
    fun getInt(): Int {return _field.intVal}
    fun getLong(): Long {return _field.longVal}
    fun getFloat(): Float {return _field.floatVale}
    fun getDouble(): Double {return _field.doubleVal}
    fun getBoolean(): Boolean {return _field.booleanVal}
    fun getString(): String {return _field.stringVal}
    fun getList(): ValueList? {return _field.listVal}
    fun getMap(): ValueMap? {return _field.mapVal}
    fun getMapInKey(): ValueMapInKey? {return _field.intKeyVal}
}