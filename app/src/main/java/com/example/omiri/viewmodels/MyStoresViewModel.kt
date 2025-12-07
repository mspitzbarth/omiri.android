package com.example.omiri.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.omiri.data.api.models.StoreListResponse
import com.example.omiri.data.api.models.StoreResponse
import com.example.omiri.data.local.UserPreferences
import com.example.omiri.data.repository.StoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * ViewModel for My Stores screen
 */
class MyStoresViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository = StoreRepository()
    private val userPreferences = UserPreferences(application)
    
    // UI State
    private val _selectedCountry = MutableStateFlow(UserPreferences.DEFAULT_COUNTRY)
    val selectedCountry: StateFlow<String> = _selectedCountry.asStateFlow()
    
    private val _availableStores = MutableStateFlow<List<StoreListResponse>>(emptyList())
    val availableStores: StateFlow<List<StoreListResponse>> = _availableStores.asStateFlow()
    
    private val _selectedStores = MutableStateFlow<Set<String>>(emptySet())
    val selectedStores: StateFlow<Set<String>> = _selectedStores.asStateFlow()
    
    private val _storeLocations = MutableStateFlow<Map<String, Set<String>>>(emptyMap())
    val storeLocations: StateFlow<Map<String, Set<String>>> = _storeLocations.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    // For location modal
    private val _locationModalStore = MutableStateFlow<StoreListResponse?>(null)
    val locationModalStore: StateFlow<StoreListResponse?> = _locationModalStore.asStateFlow()
    
    private val _availableLocations = MutableStateFlow<List<StoreResponse>>(emptyList())
    val availableLocations: StateFlow<List<StoreResponse>> = _availableLocations.asStateFlow()
    
    private val _isLoadingLocations = MutableStateFlow(false)
    val isLoadingLocations: StateFlow<Boolean> = _isLoadingLocations.asStateFlow()
    
    init {
        Log.d(TAG, "MyStoresViewModel initialized")
        loadSavedPreferences()
    }
    
    /**
     * Load saved preferences
     */
    private fun loadSavedPreferences() {
        viewModelScope.launch {
            try {
                // Load selected country
                val country = userPreferences.selectedCountry.first()
                _selectedCountry.value = country
                Log.d(TAG, "Loaded country: $country")
                
                // Load selected stores
                val stores = userPreferences.selectedStores.first()
                _selectedStores.value = stores
                Log.d(TAG, "Loaded ${stores.size} selected stores")
                
                // Load locations for each selected store
                val locationsMap = mutableMapOf<String, Set<String>>()
                stores.forEach { storeId ->
                    val locations = userPreferences.getStoreLocations(storeId).first()
                    if (locations.isNotEmpty()) {
                        locationsMap[storeId] = locations
                    }
                }
                _storeLocations.value = locationsMap
                Log.d(TAG, "Loaded locations for ${locationsMap.size} stores")
                
                // Load stores for current country
                loadStores()
            } catch (e: Exception) {
                Log.e(TAG, "Error loading preferences", e)
                _error.value = "Failed to load preferences: ${e.message}"
            }
        }
    }
    
    /**
     * Select a country
     */
    fun selectCountry(country: String) {
        if (country == _selectedCountry.value) return
        
        Log.d(TAG, "Selecting country: $country")
        _selectedCountry.value = country
        
        viewModelScope.launch {
            try {
                userPreferences.saveSelectedCountry(country)
                loadStores()
            } catch (e: Exception) {
                Log.e(TAG, "Error saving country", e)
                _error.value = "Failed to save country: ${e.message}"
            }
        }
    }
    
    /**
     * Load stores from API
     */
    fun loadStores() {
        Log.d(TAG, "Loading stores for country: ${_selectedCountry.value}")
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val result = repository.getStores(country = _selectedCountry.value)
                
                result.onSuccess { stores ->
                    _availableStores.value = stores
                    Log.d(TAG, "Loaded ${stores.size} stores")
                }.onFailure { error ->
                    val errorMsg = "Failed to load stores: ${error.message}"
                    Log.e(TAG, errorMsg, error)
                    _error.value = errorMsg
                }
            } catch (e: Exception) {
                val errorMsg = "Failed to load stores: ${e.message}"
                Log.e(TAG, errorMsg, e)
                _error.value = errorMsg
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Toggle store selection
     */
    fun toggleStore(storeId: String, hasMultipleLocations: Boolean) {
        val currentSelected = _selectedStores.value.toMutableSet()
        
        if (currentSelected.contains(storeId)) {
            // Deselect store
            currentSelected.remove(storeId)
            // Remove locations for this store
            val currentLocations = _storeLocations.value.toMutableMap()
            currentLocations.remove(storeId)
            _storeLocations.value = currentLocations
            
            viewModelScope.launch {
                userPreferences.saveStoreLocations(storeId, emptySet())
            }
        } else {
            // No limit on number of stores
            // For multi-location stores, the 5 zipcode limit is enforced in the location modal
            // For single-location stores, just add them directly
            if (!hasMultipleLocations) {
                currentSelected.add(storeId)
            }
            // If hasMultipleLocations, the modal will open and handle zipcode selection
        }
        
        _selectedStores.value = currentSelected
        
        viewModelScope.launch {
            userPreferences.saveSelectedStores(currentSelected)
            updateRetailersCache(currentSelected, _availableStores.value)
        }
    }
    
    /**
     * Open location modal for a store
     */
    fun openLocationModal(store: StoreListResponse) {
        Log.d(TAG, "Opening location modal for: ${store.retailer} (id: ${store.id})")
        _locationModalStore.value = store
        // Use the retailer field directly for the API call
        // The API expects the retailer name (e.g., "ALDI USA") which will be URL-encoded automatically
        loadStoreLocations(store.retailer, store.country)
    }
    
    /**
     * Close location modal
     */
    fun closeLocationModal() {
        _locationModalStore.value = null
        _availableLocations.value = emptyList()
    }
    
    /**
     * Load locations for a specific store
     */
    private fun loadStoreLocations(retailer: String, country: String) {
        viewModelScope.launch {
            _isLoadingLocations.value = true
            
            try {
                val result = repository.getStoresByRetailer(retailer, country)
                
                result.onSuccess { locations ->
                    _availableLocations.value = locations
                    Log.d(TAG, "Loaded ${locations.size} locations for $retailer")
                }.onFailure { error ->
                    Log.e(TAG, "Failed to load locations", error)
                    _error.value = "Failed to load locations: ${error.message}"
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load locations", e)
                _error.value = "Failed to load locations: ${e.message}"
            } finally {
                _isLoadingLocations.value = false
            }
        }
    }
    
    /**
     * Save selected locations for a store
     */
    fun saveStoreLocations(storeId: String, zipcodes: Set<String>) {
        Log.d(TAG, "Saving ${zipcodes.size} locations for store: $storeId")
        
        val currentLocations = _storeLocations.value.toMutableMap()
        val currentSelected = _selectedStores.value.toMutableSet()
        
        if (zipcodes.isEmpty()) {
            // If no zipcodes selected, deselect the store entirely
            currentLocations.remove(storeId)
            currentSelected.remove(storeId)
            Log.d(TAG, "Deselecting store: $storeId")
        } else {
            // Save locations and add store to selected stores
            currentLocations[storeId] = zipcodes
            if (!currentSelected.contains(storeId)) {
                currentSelected.add(storeId)
            }
        }
        
        _storeLocations.value = currentLocations
        _selectedStores.value = currentSelected
        
        viewModelScope.launch {
            userPreferences.saveStoreLocations(storeId, zipcodes)
            userPreferences.saveSelectedStores(currentSelected)
            updateRetailersCache(currentSelected, _availableStores.value)
        }
        
        closeLocationModal()
    }
    
    private suspend fun updateRetailersCache(selectedIds: Set<String>, allStores: List<StoreListResponse>) {
        try {
            val country = _selectedCountry.value
            val selectedForCountry = selectedIds.filter { it.endsWith("_$country", ignoreCase = true) }
            
            if (selectedForCountry.isNotEmpty() && allStores.isNotEmpty()) {
                val retailers = selectedForCountry.mapNotNull { id ->
                    allStores.find { it.id == id }?.retailer
                }.distinct().joinToString(",")
                
                userPreferences.saveCachedRetailersString(retailers)
                Log.d(TAG, "Updated cached retailers: $retailers")
            } else if (selectedForCountry.isEmpty()) {
                userPreferences.saveCachedRetailersString("")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update retailers cache", e)
        }
    }
    
    /**
     * Get total count of selected zipcodes across all stores
     */
    fun getTotalSelectedCount(): Int {
        var count = 0
        _selectedStores.value.forEach { storeId ->
            val locations = _storeLocations.value[storeId]
            count += if (locations != null && locations.isNotEmpty()) {
                locations.size  // Count each zipcode
            } else {
                1  // Single location store counts as 1
            }
        }
        return count
    }
    
    /**
     * Get selected location count for a specific store
     */
    fun getStoreLocationCount(storeId: String): Int {
        return _storeLocations.value[storeId]?.size ?: 0
    }
    
    /**
     * Check if a store is selected
     */
    fun isStoreSelected(storeId: String): Boolean {
        return _selectedStores.value.contains(storeId)
    }
    
    /**
     * Clear error message
     */
    fun clearError() {
        _error.value = null
    }
    
    companion object {
        private const val TAG = "MyStoresViewModel"
    }
}
