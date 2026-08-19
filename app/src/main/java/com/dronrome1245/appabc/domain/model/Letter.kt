package com.dronrome1245.appabc.domain.model

/**
 * Represents a Russian letter with its visual symbol and spoken name.
 */
data class Letter(
    val symbol: String,
    val spokenName: String,
    val levelIntroduced: Int
)
