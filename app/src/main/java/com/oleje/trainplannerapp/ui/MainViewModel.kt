package com.oleje.trainplannerapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oleje.trainplannerapp.domain.BonusDiscountStrategy
import com.oleje.trainplannerapp.domain.CertificateDiscount
import com.oleje.trainplannerapp.domain.CitizenCategory
import com.oleje.trainplannerapp.domain.InvalidTicketDataException
import com.oleje.trainplannerapp.domain.NamedTravelTicket
import com.oleje.trainplannerapp.domain.PromoCodeDiscount
import com.oleje.trainplannerapp.domain.RouteNotFoundException
import com.oleje.trainplannerapp.domain.RoutePlanner
import com.oleje.trainplannerapp.domain.StreamableTicket
import com.oleje.trainplannerapp.domain.TicketSorter
import com.oleje.trainplannerapp.domain.TravelTicket
import com.oleje.trainplannerapp.repository.TicketRepository
import com.oleje.trainplannerapp.repository.TicketRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.util.Comparator

class MainViewModel(
    private val repository: TicketRepository = TicketRepositoryImpl.getInstance(),
    private val routePlanner: RoutePlanner = RoutePlanner()
) : ViewModel() {

    private val _catalogTickets = MutableStateFlow<List<TravelTicket>>(emptyList())
    val catalogTickets: StateFlow<List<TravelTicket>> = _catalogTickets.asStateFlow()

    private val _purchasedTickets = MutableStateFlow<List<NamedTravelTicket>>(emptyList())
    val purchasedTickets: StateFlow<List<NamedTravelTicket>> = _purchasedTickets.asStateFlow()

    private val _userBonuses = MutableStateFlow<List<BonusDiscountStrategy>>(emptyList())
    val userBonuses: StateFlow<List<BonusDiscountStrategy>> = _userBonuses.asStateFlow()

    private val _currentUserCategory = MutableStateFlow(CitizenCategory.REGULAR)
    val currentUserCategory: StateFlow<CitizenCategory> = _currentUserCategory.asStateFlow()

    private val _searchOrigin = MutableStateFlow("Львів")
    val searchOrigin: StateFlow<String> = _searchOrigin.asStateFlow()

    private val _searchDestination = MutableStateFlow("Одеса-Головна")
    val searchDestination: StateFlow<String> = _searchDestination.asStateFlow()

    private val _optimizationMode = MutableStateFlow(RoutePlanner.OptimizationMode.CHEAPEST)
    val optimizationMode: StateFlow<RoutePlanner.OptimizationMode> = _optimizationMode.asStateFlow()

    private val _searchResultPlans = MutableStateFlow<List<RoutePlanner.PersonalTravelPlan>>(emptyList())
    val searchResultPlans: StateFlow<List<RoutePlanner.PersonalTravelPlan>> = _searchResultPlans.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    init {
        refreshData()
    }

    fun refreshData() {
        _catalogTickets.value = repository.catalogTickets
        _purchasedTickets.value = repository.purchasedTickets
        _userBonuses.value = repository.userBonuses
        _currentUserCategory.value = repository.currentUserCategory
    }

    fun updateSearchOrigin(origin: String) {
        _searchOrigin.value = origin
    }

    fun updateSearchDestination(destination: String) {
        _searchDestination.value = destination
    }

    fun swapSearchStations() {
        val temp = _searchOrigin.value
        _searchOrigin.value = _searchDestination.value
        _searchDestination.value = temp
    }

    fun setOptimizationMode(mode: RoutePlanner.OptimizationMode) {
        _optimizationMode.value = mode
    }

    fun setCurrentUserCategory(category: CitizenCategory) {
        repository.currentUserCategory = category
        _currentUserCategory.value = category
        refreshData()
    }

    fun searchRoutes() {
        viewModelScope.launch {
            _isSearching.value = true
            _errorMessage.value = null
            try {
                val plans = routePlanner.findOptimalPlans(
                    repository.catalogTickets,
                    _searchOrigin.value,
                    _searchDestination.value,
                    360, // Earliest departure 06:00 AM
                    _currentUserCategory.value,
                    repository.userBonuses,
                    _optimizationMode.value,
                    3 // Top-3
                )
                _searchResultPlans.value = plans
            } catch (e: RouteNotFoundException) {
                _searchResultPlans.value = emptyList()
                _errorMessage.value = e.message
            } catch (e: Exception) {
                _searchResultPlans.value = emptyList()
                _errorMessage.value = "Помилка пошуку маршруту: ${e.message}"
            } finally {
                _isSearching.value = false
            }
        }
    }

    fun purchasePlan(plan: RoutePlanner.PersonalTravelPlan, passengerFullName: String) {
        viewModelScope.launch {
            try {
                repository.purchasePlan(plan, "user_${System.currentTimeMillis()}", passengerFullName)
                _successMessage.value = "План поїздки успішно придбано для $passengerFullName!"
                refreshData()
            } catch (e: InvalidTicketDataException) {
                _errorMessage.value = e.message
            } catch (e: Exception) {
                _errorMessage.value = "Помилка купівлі: ${e.message}"
            }
        }
    }

    fun changeTicketOwner(ticketId: String, newOwnerFullName: String) {
        viewModelScope.launch {
            try {
                repository.transferTicketOwnership(ticketId, "user_${System.currentTimeMillis()}", newOwnerFullName)
                _successMessage.value = "Власника квитка успішно змінено на $newOwnerFullName!"
                refreshData()
            } catch (e: InvalidTicketDataException) {
                _errorMessage.value = e.message
            } catch (e: Exception) {
                _errorMessage.value = "Помилка зміни власника: ${e.message}"
            }
        }
    }

    fun sortCatalogWithCustomMergeSort(comparator: Comparator<TravelTicket>) {
        val current = repository.catalogTickets
        // Call Java custom recursive MergeSort static method
        val sorted = TicketSorter.mergeSort(current, comparator)
        _catalogTickets.value = sorted
    }

    fun addCatalogTicket(ticket: TravelTicket) {
        viewModelScope.launch {
            try {
                repository.addCatalogTicket(ticket)
                _successMessage.value = "Новий рейс додано до каталогу!"
                refreshData()
            } catch (e: InvalidTicketDataException) {
                _errorMessage.value = e.message
            }
        }
    }

    fun updateCatalogTicket(ticket: TravelTicket) {
        viewModelScope.launch {
            try {
                repository.updateCatalogTicket(ticket)
                _successMessage.value = "Параметри рейсу оновлено!"
                refreshData()
            } catch (e: InvalidTicketDataException) {
                _errorMessage.value = e.message
            }
        }
    }

    fun deleteCatalogTicket(ticketId: String) {
        repository.deleteCatalogTicket(ticketId)
        _successMessage.value = "Рейс видалено з каталогу!"
        refreshData()
    }

    fun addCertificateDiscount(amountUah: Double) {
        if (amountUah <= 0) {
            _errorMessage.value = "Сума сертифіката має бути більшою за 0 грн!"
            return
        }
        val bonus = CertificateDiscount(amountUah)
        repository.addBonusDiscount(bonus)
        _successMessage.value = "Сертифікат -${amountUah.toInt()} грн додано до гаманця!"
        refreshData()
    }

    fun addPromoCodeDiscount(percentage: Double) {
        if (percentage <= 0 || percentage > 100) {
            _errorMessage.value = "Відсоток промокоду має бути від 1% до 100%!"
            return
        }
        val bonus = PromoCodeDiscount(percentage)
        repository.addBonusDiscount(bonus)
        _successMessage.value = "Промокод -${percentage.toInt()}% додано до гаманця!"
        refreshData()
    }

    fun removeBonusDiscount(bonusId: String) {
        repository.removeBonusDiscount(bonusId)
        _successMessage.value = "Бонус видалено з гаманця!"
        refreshData()
    }

    fun importFromTxtFile(file: File) {
        viewModelScope.launch {
            try {
                val imported = repository.importTicketsFromTxt(file)
                _successMessage.value = "Успішно імпортовано ${imported.size} квитків з файлу!"
                refreshData()
            } catch (e: Exception) {
                _errorMessage.value = "Помилка імпорту з файлу: ${e.message}"
            }
        }
    }

    fun exportToTxtFile(file: File, ticketsToExport: List<StreamableTicket>) {
        viewModelScope.launch {
            try {
                repository.exportTicketsToTxt(file, ticketsToExport)
                _successMessage.value = "Успішно експортовано ${ticketsToExport.size} квитків у файл ${file.name}!"
            } catch (e: Exception) {
                _errorMessage.value = "Помилка експорту у файл: ${e.message}"
            }
        }
    }

    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }
}
