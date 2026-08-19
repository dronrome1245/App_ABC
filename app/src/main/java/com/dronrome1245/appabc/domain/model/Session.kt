package com.dronrome1245.appabc.domain.model

import java.time.Instant

/**
 * Represents a training session.
 */
data class Session(
    val id: String,
    val startTime: Instant = Instant.now(),
    val levelId: Int
)
