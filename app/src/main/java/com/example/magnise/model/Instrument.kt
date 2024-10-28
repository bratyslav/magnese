package com.example.magnise.model

data class Instrument(
    val id: String,
    val base: Currency,
    val quote: Currency
) {
    override fun toString(): String {
        return "${base.id}/${quote.id}"
    }
}
