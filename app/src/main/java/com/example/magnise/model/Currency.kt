package com.example.magnise.model

class Currency private constructor(val id: String) {

    companion object {
        private val currencies = mutableMapOf<String, Currency>()

        fun getInstance(id: String): Currency {
            return currencies.getOrPut(id) {
                Currency(id)
            }
        }
    }

}