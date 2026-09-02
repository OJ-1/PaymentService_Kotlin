package com.ojsolutions

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class SanityTest {

    // Sanity test - purely to check if the Test Framework is working

    @Nested
    @DisplayName("Feature: Automated test infrastructure")
    inner class AutomatedTestInfrastructure {

        @Test
        @DisplayName("Scenario: The automated test framework executes successfully")
        fun testFrameworkExecutesSuccessfully() {

            // Given
            val firstValue = 1
            val secondValue = 1

            // When
            val result = firstValue + secondValue

            // Then
            assertEquals(2, result)
        }
    }
}