package com.kota.Bahamut.listPage

class ListStateStore private constructor() {
    private val states: MutableMap<String, ListState> = HashMap()

    fun getState(aBoardName: String?): ListState {
        if (aBoardName == null) {
            return ListState()
        }
        val state = this.states[aBoardName]
        if (state != null) {
            return state
        }
        val state2 = ListState()
        this.states.put(aBoardName, state2)
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
