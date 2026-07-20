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
}
