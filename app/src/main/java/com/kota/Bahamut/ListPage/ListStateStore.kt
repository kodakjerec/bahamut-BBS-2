package com.kota.Bahamut.ListPage

class ListStateStore private constructor() {
    private val _states = mutableMapOf<String, ListState>()

    fun getState(aBoardName: String): ListState {
        return _states[aBoardName] ?: run {
            val state = ListState()
            _states[aBoardName] = state
            state
        }
    }

    companion object {
        @Volatile
        private var _instance: ListStateStore? = null

        fun getInstance(): ListStateStore {
            return _instance ?: synchronized(this) {
                _instance ?: ListStateStore().also { _instance = it }
            }
        }
    }
}
