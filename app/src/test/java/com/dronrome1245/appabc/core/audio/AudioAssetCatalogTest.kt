package com.dronrome1245.appabc.core.audio

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.dronrome1245.appabc.domain.curriculum.RussianAlphabet
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AudioAssetCatalogTest {
    @Test
    fun mapsAll33RussianLettersIgnoringCaseWithV2First() {
        assertEquals(33, AudioAssetCatalog.mappedLetters().size)
        assertEquals(RussianAlphabet.symbols.map { it.single() }.toSet(), AudioAssetCatalog.mappedLetters())
        RussianAlphabet.symbols.forEach { symbol ->
            val upper = symbol.single()
            val legacy = AudioAssetCatalog.resourceNameForLetter(upper)
            val v2 = AudioAssetCatalog.v2ResourceNameForLetter(upper)

            assertTrue(legacy?.startsWith("sound_letter_") == true)
            assertEquals("${legacy}_v2", v2)
            assertEquals(listOf(v2, legacy), AudioAssetCatalog.resourceCandidatesForLetter(upper))
            assertEquals(legacy, AudioAssetCatalog.resourceNameForLetter(upper.lowercaseChar()))
            assertEquals(v2, AudioAssetCatalog.v2ResourceNameForLetter(upper.lowercaseChar()))
        }
    }

    @Test
    fun all33LegacyMappingsResolveToBundledRawResources() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        RussianAlphabet.symbols.forEach { symbol ->
            val resourceName = AudioAssetCatalog.resourceNameForLetter(symbol.single())
            val resourceId = context.resources.getIdentifier(resourceName, "raw", context.packageName)
            assertNotEquals("Missing raw resource for $symbol: $resourceName", 0, resourceId)
        }
    }

    @Test
    fun allMappedResourceNamesAreAndroidSafeSnakeCase() {
        val names = buildList {
            RussianAlphabet.symbols.forEach { symbol ->
                addAll(AudioAssetCatalog.resourceCandidatesForLetter(symbol.single()))
            }
            add(AudioAssetCatalog.CORRECT_FEEDBACK)
            add(AudioAssetCatalog.INCORRECT_FEEDBACK)
            add(AudioAssetCatalog.LEVEL_COMPLETE)
        }

        names.forEach { resourceName ->
            assertTrue("Invalid Android raw resource name: $resourceName", AudioAssetCatalog.isValidRawResourceName(resourceName))
        }
    }

    @Test
    fun resolverSkipsMissingCandidateAndUsesNextBundledAsset() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val resolver = RawAudioResourceResolver(context)

        val resolved = resolver.resolveFirst(listOf("sound_letter_missing_v2", "sound_letter_a"))

        assertEquals("sound_letter_a", resolved?.resourceName)
        assertNotEquals(0, resolved?.resourceId ?: 0)
    }

    @Test
    fun resolverReturnsNullWhenNoCandidateExists() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val resolver = RawAudioResourceResolver(context)

        assertNull(resolver.resolveFirst(listOf("sound_letter_missing_v2", "INVALID-NAME")))
    }

    @Test
    fun unknownLetterStillUsesTtsFallback() {
        assertNull(AudioAssetCatalog.resourceNameForLetter('A'))
        assertNull(AudioAssetCatalog.v2ResourceNameForLetter('A'))
        assertEquals(emptyList<String>(), AudioAssetCatalog.resourceCandidatesForLetter('A'))
        assertTrue(AudioFallbackPolicy.shouldFallback(null, 0))
        assertTrue(AudioFallbackPolicy.shouldFallback("", 0))
        assertTrue(AudioFallbackPolicy.shouldFallback("sound_letter_a", 0))
        assertFalse(AudioFallbackPolicy.shouldFallback("sound_letter_a", 42))
    }

    @Test
    fun feedbackAndCompletionResourceNamesRemainStable() {
        assertEquals("sound_correct", AudioAssetCatalog.CORRECT_FEEDBACK)
        assertEquals("sound_wrong", AudioAssetCatalog.INCORRECT_FEEDBACK)
        assertEquals("sound_level_complete", AudioAssetCatalog.LEVEL_COMPLETE)
    }
}
