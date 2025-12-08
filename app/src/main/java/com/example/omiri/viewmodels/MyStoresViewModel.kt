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
    
    // List of available countries from API
    private val _availableCountries = MutableStateFlow<List<String>>(emptyList())
    val availableCountries: StateFlow<List<String>> = _availableCountries.asStateFlow()

    init {
        Log.d(TAG, "MyStoresViewModel initialized")
        loadData()
    }
    
    /**
     * Load initial data
     */
    private fun loadData() {
        viewModelScope.launch {
            try {
                // 1. Fetch ALL stores to determine available countries
                // We call getStores() without country filter
                val result = repository.getStores(country = null)
                
                result.onSuccess { allStores ->
                    // Extract unique countries
                    val countries = allStores.map { it.country }.distinct().sorted()
                    _availableCountries.value = countries
                    Log.d(TAG, "Loaded countries: $countries")
                    
                    // 2. Load preferences after countries are loaded
                    loadSavedPreferences(countries)
                }.onFailure { error ->
                    Log.e(TAG, "Failed to load initial stores for countries", error)
                    _error.value = "Failed to load countries: ${error.message}"
                     // Fallback to basic load if API fails
                    loadSavedPreferences(emptyList())
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading data", e)
                 loadSavedPreferences(emptyList())
            }
        }
    }

    /**
     * Load saved preferences
     */
    private fun loadSavedPreferences(availableCountries: List<String>) {
        viewModelScope.launch {
            try {
                // Load selected country
                var country = userPreferences.selectedCountry.first()
                
                // If no country selected or selected country not in available list (and list not empty),
                // select the first available one (e.g., US)
                if (country.isEmpty() || (availableCountries.isNotEmpty() && !availableCountries.contains(country))) {
                     // Default to US if available, else first one
                     country = if (availableCountries.contains("US")) "US" else availableCountries.firstOrNull() ?: "US"
                     // Save this default
                     userPreferences.saveSelectedCountry(country)
                }
                
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
     * Load stores from API for selected country
     */
    fun loadStores() {
        // ... (existing implementation)
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
    
    // ... (rest of methods: toggleStore, openLocationModal, etc.) remain the same
    
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
            if (!hasMultipleLocations) {
                currentSelected.add(storeId)
            }
        }
        
        _selectedStores.value = currentSelected
        
        viewModelScope.launch {
            userPreferences.saveSelectedStores(currentSelected)
            updateRetailersCache(currentSelected, _availableStores.value)
        }
    }

    // ... (keep rest of class)

    /**
     * Open location modal for a store
     */
    fun openLocationModal(store: StoreListResponse) {
        Log.d(TAG, "Opening location modal for: ${store.retailer} (id: ${store.id})")
        _locationModalStore.value = store
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
            currentLocations.remove(storeId)
            currentSelected.remove(storeId)
            Log.d(TAG, "Deselecting store: $storeId")
        } else {
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

    /**
     * Save all changes (selected stores and their locations) at once
     */
    fun saveChanges(
        newSelectedStores: Set<String>,
        newStoreLocations: Map<String, Set<String>>
    ) {
        Log.d(TAG, "Saving changes: ${newSelectedStores.size} stores, ${newStoreLocations.size} locations map entries")
        
        _selectedStores.value = newSelectedStores
        _storeLocations.value = newStoreLocations
        
        viewModelScope.launch {
            // Save selected stores list
            userPreferences.saveSelectedStores(newSelectedStores)
            
            // Save locations for each store (or clear if not selected)
            // Note: Efficiently only save what changed would be better, but for now save all relevant
            newStoreLocations.forEach { (storeId, locations) ->
                if (newSelectedStores.contains(storeId)) {
                    userPreferences.saveStoreLocations(storeId, locations)
                }
            }
            
            // Update retailer cache string
            updateRetailersCache(newSelectedStores, _availableStores.value)
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
     * Clear all selected stores
     */
    fun clearAllSelections() {
        Log.d(TAG, "Clearing all selections")
        val previouslySelected = _selectedStores.value.toList()
        
        _selectedStores.value = emptySet()
        _storeLocations.value = emptyMap()
        
        viewModelScope.launch {
            userPreferences.saveSelectedStores(emptySet())
            previouslySelected.forEach { storeId ->
                userPreferences.saveStoreLocations(storeId, emptySet())
            }
            updateRetailersCache(emptySet(), _availableStores.value)
        }
    }
    
    /**
     * Get total count of selected stores
     */
    fun getTotalSelectedCount(): Int {
        return _selectedStores.value.size
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
