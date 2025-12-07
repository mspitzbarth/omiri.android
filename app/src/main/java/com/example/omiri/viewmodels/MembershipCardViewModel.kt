package com.example.omiri.viewmodels

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.omiri.data.local.UserPreferences
import com.example.omiri.data.models.MembershipCard
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class MembershipCardViewModel(application: Application) : AndroidViewModel(application) {
    private val userPreferences = UserPreferences(application)
    
    private val _cards = MutableStateFlow<List<MembershipCard>>(emptyList())
    val cards: StateFlow<List<MembershipCard>> = _cards.asStateFlow()
    
    init {
        loadCards()
    }
    
    fun loadCards() {
        viewModelScope.launch {
            userPreferences.membershipCards.collect { savedCards ->
                _cards.value = savedCards
            }
        }
    }
    
    fun addCard(name: String, number: String, storeId: String?, imageBitmap: Bitmap?, imageUri: Uri?) {
        viewModelScope.launch {
            val imagePath = when {
                imageBitmap != null -> saveImageToInternalStorage(imageBitmap)
                imageUri != null -> saveImageFromUri(imageUri)
                else -> null
            }
            
            val newCard = MembershipCard(
                name = name,
                cardNumber = number,
                storeId = storeId,
                imagePath = imagePath
            )
            
            val updatedList = _cards.value + newCard
            userPreferences.saveMembershipCards(updatedList)
        }
    }
    
    fun deleteCard(cardId: String) {
        viewModelScope.launch {
            val cardToDelete = _cards.value.find { it.id == cardId }
            cardToDelete?.imagePath?.let { path ->
                val file = File(path)
                if (file.exists()) {
                    file.delete()
                }
            }
            
            val updatedList = _cards.value.filter { it.id != cardId }
            userPreferences.saveMembershipCards(updatedList)
        }
    }
    
    private fun saveImageToInternalStorage(bitmap: Bitmap): String {
        val filename = "card_${UUID.randomUUID()}.jpg"
        val file = File(getApplication<Application>().filesDir, filename)
        
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }
        
        return file.absolutePath
    }

    private fun saveImageFromUri(uri: Uri): String? {
        return try {
            val filename = "card_${UUID.randomUUID()}.jpg"
            val file = File(getApplication<Application>().filesDir, filename)
            
            getApplication<Application>().contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(file).use { out ->
                    input.copyTo(out)
                }
            }
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
