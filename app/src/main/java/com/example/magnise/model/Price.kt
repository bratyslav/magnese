package com.example.magnise.model

import java.math.BigDecimal

data class Price(
    val instrument: Instrument,
    val value: BigDecimal,
    val timestamp: Long // ms
)
