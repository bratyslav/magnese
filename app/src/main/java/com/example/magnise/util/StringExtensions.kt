package com.example.magnise.util

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

fun String.toTimestampAsIsoOffset() = OffsetDateTime
        .parse(this, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        .toInstant()
        .toEpochMilli()