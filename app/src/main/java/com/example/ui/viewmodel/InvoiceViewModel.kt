package com.example.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.BusinessProfileManager
import com.example.data.local.InvoiceEntity
import com.example.data.model.BusinessProfile
import com.example.data.model.Invoice
import com.example.data.model.InvoiceItem
import com.example.util.InvoicePdfGenerator
import com.example.util.ShareHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

enum class AppTab {
    EDIT,
    PREVIEW,
    HISTORY
}

class InvoiceViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val invoiceDao = db.invoiceDao()
    private val profileManager = BusinessProfileManager(application)

    private val _activeTab = MutableStateFlow(AppTab.EDIT)
    val activeTab: StateFlow<AppTab> = _activeTab.asStateFlow()

    private val _currentInvoice = MutableStateFlow(createInitialInvoice())
    val currentInvoice: StateFlow<Invoice> = _currentInvoice.asStateFlow()

    private val _lastGeneratedPdf = MutableStateFlow<File?>(null)
    val lastGeneratedPdf: StateFlow<File?> = _lastGeneratedPdf.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    val savedInvoices: StateFlow<List<Invoice>> = invoiceDao.getAllInvoices()
        .map { list -> list.map { it.toInvoice() } }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private fun createInitialInvoice(): Invoice {
        val profile = profileManager.getProfile()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = dateFormat.format(Date())

        return Invoice(
            id = UUID.randomUUID().toString(),
            invoiceNumber = "",
            issueDate = today,
            dueDate = "",
            currency = profile.currency.ifBlank { "Rs" },
            businessName = profile.name,
            businessAddress = profile.address,
            businessPhone = profile.phone,
            businessEmail = profile.email,
            businessLogoPath = profile.logoPath,
            clientName = "",
            clientAddress = "",
            clientPhone = "",
            clientEmail = "",
            items = listOf(
                InvoiceItem(
                    id = UUID.randomUUID().toString(),
                    description = "",
                    quantity = 1.0,
                    unitPrice = 0.0
                )
            ),
            discount = 0.0,
            taxRate = 0.0,
            notes = profile.defaultNotes,
            status = "Draft"
        )
    }

    fun setTab(tab: AppTab) {
        _activeTab.value = tab
    }

    fun clearMessage() {
        _userMessage.value = null
    }

    fun createNewInvoice() {
        val newInv = createInitialInvoice()
        _currentInvoice.value = newInv
        _lastGeneratedPdf.value = null
        _activeTab.value = AppTab.EDIT
        _userMessage.value = "New invoice draft created"
    }

    fun loadInvoice(invoice: Invoice) {
        _currentInvoice.value = invoice
        _lastGeneratedPdf.value = null
        _activeTab.value = AppTab.EDIT
    }

    // Business info updates
    fun updateBusinessName(name: String) {
        _currentInvoice.value = _currentInvoice.value.copy(businessName = name)
    }

    fun updateBusinessAddress(address: String) {
        _currentInvoice.value = _currentInvoice.value.copy(businessAddress = address)
    }

    fun updateBusinessPhone(phone: String) {
        _currentInvoice.value = _currentInvoice.value.copy(businessPhone = phone)
    }

    fun updateBusinessEmail(email: String) {
        _currentInvoice.value = _currentInvoice.value.copy(businessEmail = email)
    }

    fun updateBusinessLogo(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            val copiedPath = ShareHelper.copyUriToInternalFile(getApplication(), uri)
            if (copiedPath != null) {
                withContext(Dispatchers.Main) {
                    _currentInvoice.value = _currentInvoice.value.copy(businessLogoPath = copiedPath)
                    // Also save to default profile
                    val currentProfile = profileManager.getProfile()
                    profileManager.saveProfile(currentProfile.copy(logoPath = copiedPath))
                    _userMessage.value = "Business logo uploaded successfully!"
                }
            } else {
                withContext(Dispatchers.Main) {
                    _userMessage.value = "Failed to copy logo image"
                }
            }
        }
    }

    fun removeBusinessLogo() {
        _currentInvoice.value = _currentInvoice.value.copy(businessLogoPath = null)
        val currentProfile = profileManager.getProfile()
        profileManager.saveProfile(currentProfile.copy(logoPath = null))
        _userMessage.value = "Logo removed"
    }

    fun saveAsDefaultBusinessProfile() {
        val inv = _currentInvoice.value
        val profile = BusinessProfile(
            name = inv.businessName,
            address = inv.businessAddress,
            phone = inv.businessPhone,
            email = inv.businessEmail,
            logoPath = inv.businessLogoPath,
            currency = inv.currency,
            defaultNotes = inv.notes
        )
        profileManager.saveProfile(profile)
        _userMessage.value = "Default business profile saved!"
    }

    // Client info updates
    fun updateClientName(name: String) {
        _currentInvoice.value = _currentInvoice.value.copy(clientName = name)
    }

    fun updateClientAddress(address: String) {
        _currentInvoice.value = _currentInvoice.value.copy(clientAddress = address)
    }

    fun updateClientPhone(phone: String) {
        _currentInvoice.value = _currentInvoice.value.copy(clientPhone = phone)
    }

    fun updateClientEmail(email: String) {
        _currentInvoice.value = _currentInvoice.value.copy(clientEmail = email)
    }

    // Invoice meta updates
    fun updateInvoiceNumber(number: String) {
        _currentInvoice.value = _currentInvoice.value.copy(invoiceNumber = number)
    }

    fun updateIssueDate(date: String) {
        _currentInvoice.value = _currentInvoice.value.copy(issueDate = date)
    }

    fun updateDueDate(date: String) {
        _currentInvoice.value = _currentInvoice.value.copy(dueDate = date)
    }

    fun updateCurrency(currency: String) {
        _currentInvoice.value = _currentInvoice.value.copy(currency = currency)
    }

    // Items
    fun addItem() {
        val currentItems = _currentInvoice.value.items.toMutableList()
        currentItems.add(InvoiceItem(description = "", quantity = 1.0, unitPrice = 0.0))
        _currentInvoice.value = _currentInvoice.value.copy(items = currentItems)
    }

    fun updateItem(index: Int, item: InvoiceItem) {
        val currentItems = _currentInvoice.value.items.toMutableList()
        if (index in currentItems.indices) {
            currentItems[index] = item
            _currentInvoice.value = _currentInvoice.value.copy(items = currentItems)
        }
    }

    fun removeItem(index: Int) {
        val currentItems = _currentInvoice.value.items.toMutableList()
        if (currentItems.size > 1 && index in currentItems.indices) {
            currentItems.removeAt(index)
            _currentInvoice.value = _currentInvoice.value.copy(items = currentItems)
        } else if (currentItems.size == 1) {
            // Keep at least one empty item
            currentItems[0] = InvoiceItem()
            _currentInvoice.value = _currentInvoice.value.copy(items = currentItems)
        }
    }

    // Summary & Notes
    fun updateDiscount(discount: Double) {
        _currentInvoice.value = _currentInvoice.value.copy(discount = discount.coerceAtLeast(0.0))
    }

    fun updateTaxRate(taxRate: Double) {
        _currentInvoice.value = _currentInvoice.value.copy(taxRate = taxRate.coerceAtLeast(0.0))
    }

    fun updateNotes(notes: String) {
        _currentInvoice.value = _currentInvoice.value.copy(notes = notes)
    }

    fun updateStatus(status: String) {
        _currentInvoice.value = _currentInvoice.value.copy(status = status)
        saveInvoice()
    }

    fun saveInvoice() {
        viewModelScope.launch(Dispatchers.IO) {
            val entity = InvoiceEntity.fromInvoice(_currentInvoice.value)
            invoiceDao.insertInvoice(entity)
            withContext(Dispatchers.Main) {
                _userMessage.value = "Invoice saved to history"
            }
        }
    }

    fun deleteInvoice(invoiceId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            invoiceDao.deleteInvoiceById(invoiceId)
            withContext(Dispatchers.Main) {
                _userMessage.value = "Invoice deleted"
            }
        }
    }

    fun generatePdfAndSwitchToPreview(onReady: (File) -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            _isGenerating.value = true
            try {
                val file = InvoicePdfGenerator.generatePdf(getApplication(), _currentInvoice.value)
                val entity = InvoiceEntity.fromInvoice(_currentInvoice.value)
                invoiceDao.insertInvoice(entity)

                withContext(Dispatchers.Main) {
                    _lastGeneratedPdf.value = file
                    _isGenerating.value = false
                    _activeTab.value = AppTab.PREVIEW
                    onReady(file)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _isGenerating.value = false
                    _userMessage.value = "PDF generation error: ${e.localizedMessage}"
                }
            }
        }
    }

    fun shareCurrentPdf(shareAction: (File, String, String) -> Unit) {
        val existingPdf = _lastGeneratedPdf.value
        if (existingPdf != null && existingPdf.exists()) {
            shareAction(existingPdf, _currentInvoice.value.invoiceNumber, _currentInvoice.value.clientName)
        } else {
            generatePdfAndSwitchToPreview { pdfFile ->
                shareAction(pdfFile, _currentInvoice.value.invoiceNumber, _currentInvoice.value.clientName)
            }
        }
    }
}
