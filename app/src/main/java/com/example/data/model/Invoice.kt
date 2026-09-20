package com.example.data.model

import java.util.UUID

data class InvoiceItem(
    val id: String = UUID.randomUUID().toString(),
    val description: String = "",
    val quantity: Double = 1.0,
    val unitPrice: Double = 0.0
) {
    val total: Double
        get() = quantity * unitPrice
}

data class BusinessProfile(
    val name: String = "",
    val address: String = "",
    val phone: String = "",
    val email: String = "",
    val logoPath: String? = null,
    val currency: String = "Rs",
    val defaultNotes: String = ""
)

data class Invoice(
    val id: String = UUID.randomUUID().toString(),
    val invoiceNumber: String = "",
    val issueDate: String = "",
    val dueDate: String = "",
    val currency: String = "Rs",
    // Business (Sender) info
    val businessName: String = "",
    val businessAddress: String = "",
    val businessPhone: String = "",
    val businessEmail: String = "",
    val businessLogoPath: String? = null,
    // Client (Recipient) info
    val clientName: String = "",
    val clientAddress: String = "",
    val clientPhone: String = "",
    val clientEmail: String = "",
    // Line items
    val items: List<InvoiceItem> = listOf(
        InvoiceItem(description = "", quantity = 1.0, unitPrice = 0.0)
    ),
    // Financials
    val discount: Double = 0.0,
    val taxRate: Double = 0.0, // percentage e.g. 5.0 for 5%
    val notes: String = "",
    val status: String = "Draft", // Draft, Sent, Paid
    val createdAt: Long = System.currentTimeMillis()
) {
    val subtotal: Double
        get() = items.sumOf { it.total }

    val taxAmount: Double
        get() = ((subtotal - discount).coerceAtLeast(0.0)) * (taxRate / 100.0)

    val grandTotal: Double
        get() = ((subtotal - discount).coerceAtLeast(0.0)) + taxAmount

    fun formattedAmount(amount: Double): String {
        return if (amount % 1.0 == 0.0) {
            String.format("%,.0f", amount)
        } else {
            String.format("%,.2f", amount)
        }
    }
}
