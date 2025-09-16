package com.kota.Bahamut.ListPage

class ListStateStore private constructor() {
    private val _states: MutableMap<String?, ListState?> = HashMap<Any?, Any?>()

    fun getState(aBoardName: String?): ListState {
        val state = this._states.get(aBoardName)
        if (state != null) {
            return state
        }
        val state2 = ListState()
        this._states.put(aBoardName, state2)
        return state2
    }

    companion object {
        private var _instance: ListStateStore? = null
        @JvmStatic
        val instance: ListStateStore
            get() {
                if (_instance == null) {
                    _instance = ListStateStore()
                }
                return _instance!!
            }
    }
}
