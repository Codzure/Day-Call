package com.codzuregroup.daycall.ui.vibes

import com.google.common.truth.Truth
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
class VibesViewModelTest {

    private lateinit var viewModel: VibesViewModel

    @Before
    fun setUp() {
        viewModel = VibesViewModel()
    }

    @After
    fun tearDown() {
        // Clean up if needed
    }

    @Test
    fun `initial state should have vibes loaded`() = runTest {
        val uiState = viewModel.uiState.value
        
        Truth.assertThat(uiState.vibes).isNotEmpty()
        Truth.assertThat(uiState.vibes.size).isEqualTo(5)
        Truth.assertThat(uiState.isLoading).isFalse()
        Truth.assertThat(uiState.error).isNull()
    }

    @Test
    fun `selectVibe should update selected vibe`() = runTest {
        val chillVibe = VibeDefaults.availableVibes.first { it.id == "chill" }
        
        viewModel.handleEvent(VibesEvent.SelectVibe(chillVibe))
        
        val uiState = viewModel.uiState.value
        Truth.assertThat(uiState.selectedVibe).isEqualTo(chillVibe)
        Truth.assertThat(uiState.vibes.find { it.id == "chill" }?.isSelected).isTrue()
        Truth.assertThat(uiState.vibes.find { it.id == "hustle" }?.isSelected).isFalse()
    }

    @Test
    fun `unlockVibe should unlock the specified vibe`() = runTest {
        val lockedVibe = VibeDefaults.availableVibes.first().copy(isUnlocked = false)
        viewModel = VibesViewModel()
        
        viewModel.handleEvent(VibesEvent.UnlockVibe(lockedVibe.id))
        
        val uiState = viewModel.uiState.value
        val unlockedVibe = uiState.vibes.find { it.id == lockedVibe.id }
        Truth.assertThat(unlockedVibe?.isUnlocked).isTrue()
    }

    @Test
    fun `getSelectedVibe should return current selected vibe`() = runTest {
        val hustleVibe = VibeDefaults.availableVibes.first { it.id == "hustle" }
        viewModel.handleEvent(VibesEvent.SelectVibe(hustleVibe))
        
        val selectedVibe = viewModel.getSelectedVibe()
        Truth.assertThat(selectedVibe).isEqualTo(hustleVibe)
    }

    @Test
    fun `isVibeSelected should return correct selection state`() = runTest {
        val cosmicVibe = VibeDefaults.availableVibes.first { it.id == "cosmic" }
        viewModel.handleEvent(VibesEvent.SelectVibe(cosmicVibe))
        
        Truth.assertThat(viewModel.isVibeSelected("cosmic")).isTrue()
        Truth.assertThat(viewModel.isVibeSelected("chill")).isFalse()
    }

    @Test
    fun `refreshVibes should reload vibes`() = runTest {
        viewModel.handleEvent(VibesEvent.RefreshVibes)
        
        val uiState = viewModel.uiState.value
        Truth.assertThat(uiState.vibes).isNotEmpty()
        Truth.assertThat(uiState.isLoading).isFalse()
    }
} 