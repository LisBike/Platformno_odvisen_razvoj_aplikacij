package com.example.lisbike_pora

class DataModel(  private var nestedList: List<String>?,
        private var itemText: String?,
        private var isExpandable: Boolean = false){

    fun setExpandable(expandable: Boolean) {
        isExpandable = expandable
    }

    fun getNestedList(): List<String>? {
        return nestedList
    }

    fun getItemText(): String? {
        return itemText
    }

    fun isExpandable(): Boolean {
        return isExpandable
    }
}