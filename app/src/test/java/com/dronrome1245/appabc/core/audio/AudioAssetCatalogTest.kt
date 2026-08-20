package com.dronrome1245.appabc.core.audio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AudioAssetCatalogTest {
    @Test
    fun mapsAllCurriculumV2LettersIgnoringCase() {
        val expected = mapOf(
            'А' to "sound_letter_a",
            'М' to "sound_letter_m",
            'О' to "sound_letter_o",
            'У' to "sound_letter_u",
            'С' to "sound_letter_s",
            'Н' to "sound_letter_n"
        )
        expected.forEach { (letter, resourceName) ->
            assertEquals(resourceName, AudioAssetCatalog.resourceNameForLetter(letter))
            assertEquals(resourceName, AudioAssetCatalog.resourceNameForLetter(letter.lowercaseChar()))
        }
    }

    @Test
    fun unknownLetterHasNoLocalAssetMapping() {
        assertNull(AudioAssetCatalog.resourceNameForLetter('Б'))
    }

    @Test
    fun feedbackAndCompletionResourceNamesMatchBundledAssets() {
        assertEquals("sound_correct", AudioAssetCatalog.CORRECT_FEEDBACK)
        assertEquals("sound_wrong", AudioAssetCatalog.INCORRECT_FEEDBACK)
        assertEquals("sound_level_complete", AudioAssetCatalog.LEVEL_COMPLETE)
    }

    @Test
    fun fallbackPolicyUsesTtsForMissingMappingOrMissingAndroidResource() {
        assertTrue(AudioFallbackPolicy.shouldFallback(null, 0))
        assertTrue(AudioFallbackPolicy.shouldFallback("", 0))
        assertTrue(AudioFallbackPolicy.shouldFallback("sound_letter_a", 0))
        assertFalse(AudioFallbackPolicy.shouldFallback("sound_letter_a", 42))
    }
}
