package com.brokenpc.smframework.base.types

class IndexPath {
    private var _section:Int = 0
    private var _column:Int = 0
    private var _index:Int = 0

    constructor() {
        set(0, 0, 0)
    }

    constructor(indexPath: IndexPath) {
        set(indexPath)
    }

    constructor(index: Int) {
        set(0, index, 0)
    }

    constructor(section: Int, index: Int) {
        set(section, 0, index)
    }

    constructor(section: Int, column: Int, index: Int) {
        set(section, column, index)
    }

    fun set(indexPath: IndexPath) {
        set(indexPath._section, indexPath._column, indexPath._index)
    }

    fun set(section: Int, column:Int, index:Int) {
        _column = column
        _index = index
        _section = section
    }

    fun getSection(): Int {
        return _section
    }

    fun getColumn(): Int {
        return _column
    }

    fun getIndex(): Int {
        return _index
    }

    fun eqaul(rhs: IndexPath): Boolean {
        return _index == rhs._index && _section == rhs._section
    }

    fun notequal(rhs: IndexPath): Boolean {
        return _index != rhs._index || _section != rhs._section
    }

    fun lessequal(rhs: IndexPath): Boolean {
        return _index <= rhs._index && _section == rhs._section
    }

    fun greaterequal(rhs: IndexPath): Boolean {
        return _index >= rhs._index && _section == rhs._section
    }

    fun lessthan(rhs: IndexPath): Boolean {
        return _index < rhs._index && _section == rhs._section
    }

    fun greaterthan(rhs: IndexPath): Boolean {
        return _index > rhs._index && _section == rhs._section
    }

    fun inc(): IndexPath? {
        ++_index
        return this
    }

    fun dec(): IndexPath? {
        --_index
        return this
    }

    fun add(value: Int): IndexPath? {
        _index += value
        return this
    }

    fun min(value: Int): IndexPath? {
        _index -= value
        return this
    }
}