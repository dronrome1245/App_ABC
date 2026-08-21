package com.dronrome1245.appabc.domain.curriculum

/** Canonical visual order of the 33 Russian uppercase letters for reporting/UI. */
object RussianAlphabet {
    val symbols: List<String> = "АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ".map { it.toString() }

    init {
        check(symbols.size == 33) { "Russian alphabet must contain exactly 33 letters" }
    }
}
