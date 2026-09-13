package com.smokingtracker

import org.junit.Test
import org.junit.Assert.*
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.WavyProgressIndicatorDefaults

class ExampleUnitTest {
    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    @Test
    fun addition_isCorrect() {
        println("LinearContainerHeight: ${WavyProgressIndicatorDefaults.LinearContainerHeight}")
        println("LinearDeterminateWavelength: ${WavyProgressIndicatorDefaults.LinearDeterminateWavelength}")
    }

    @Test
    fun testEstimateHistoricalUnlockTimestamp_login() {
        val manager = AchievementsManager()
        val day = 86400000L
        val baseTime = 1700000000000L
        val launches = listOf(
            baseTime,
            baseTime + day,
            baseTime + 2 * day
        )
        val login1Time = manager.estimateHistoricalUnlockTimestamp("login_1", emptyList(), launches)
        assertEquals(baseTime, login1Time)

        val login3Time = manager.estimateHistoricalUnlockTimestamp("login_3", emptyList(), launches)
        assertNotNull(login3Time)
        assertTrue(login3Time!! >= baseTime + 2 * day)
    }

    @Test
    fun testEstimateHistoricalUnlockTimestamp_noSmoke() {
        val manager = AchievementsManager()
        val day = 86400000L
        val t0 = 1700000000000L
        val t1 = t0 + 2 * day
        val entries = listOf(t0, t1)

        val noSmoke1d = manager.estimateHistoricalUnlockTimestamp("nosmoke_1d", entries, emptyList())
        assertEquals(t0 + day, noSmoke1d)
    }

    @Test
    fun testEstimateHistoricalUnlockTimestamp_doubleDamage() {
        val manager = AchievementsManager()
        val t0 = 1700000000000L
        val t1 = t0 + 2 * 60 * 1000L
        val entries = listOf(t0, t1)

        val doubleDamage = manager.estimateHistoricalUnlockTimestamp("secret_double_damage", entries, emptyList())
        assertEquals(t1, doubleDamage)
    }
}
