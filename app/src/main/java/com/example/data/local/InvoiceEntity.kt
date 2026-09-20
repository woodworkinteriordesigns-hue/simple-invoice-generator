package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.example.data.model.Invoice
import com.example.data.model.InvoiceItem
import org.json.JSONArray
import org.json.JSONObject

@Entity(tableName = "invoices")
data class InvoiceEntity(
    @PrimaryKey val id: String,
    val invoiceNumber: String,
    val issueDate: String,
    val dueDate: String,
    val currency: String,
    val businessName: String,
    val businessAddress: String,
    val businessPhone: String,
    val businessEmail: String,
    val businessLogoPath: String?,
    val clientName: String,
    val clientAddress: String,
    val clientPhone: String,
    val clientEmail: String,
    val itemsJson: String,
    val discount: Double,
    val taxRate: Double,
    val notes: String,
    val status: String,
    val createdAt: Long
) {
    fun toInvoice(): Invoice {
        val itemsList = mutableListOf<InvoiceItem>()
        try {
            val jsonArray = JSONArray(itemsJson)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                itemsList.add(
                    InvoiceItem(
                        id = obj.optString("id"),
                        description = obj.optString("description"),
                        quantity = obj.optDouble("quantity", 1.0),
                        unitPrice = obj.optDouble("unitPrice", 0.0)
                    )
                )
            }
        } catch (_: Exception) {}

        return Invoice(
            id = id,
            invoiceNumber = invoiceNumber,
            issueDate = issueDate,
            dueDate = dueDate,
            currency = currency,
            businessName = businessName,
            businessAddress = businessAddress,
            businessPhone = businessPhone,
            businessEmail = businessEmail,
            businessLogoPath = businessLogoPath,
            clientName = clientName,
            clientAddress = clientAddress,
            clientPhone = clientPhone,
            clientEmail = clientEmail,
            items = if (itemsList.isEmpty()) listOf(InvoiceItem()) else itemsList,
            discount = discount,
            taxRate = taxRate,
            notes = notes,
            status = status,
            createdAt = createdAt
        )
    }

    companion object {
        fun fromInvoice(invoice: Invoice): InvoiceEntity {
            val jsonArray = JSONArray()
            for (item in invoice.items) {
                val obj = JSONObject().apply {
                    put("id", item.id)
                    put("description", item.description)
                    put("quantity", item.quantity)
                    put("unitPrice", item.unitPrice)
                }
                jsonArray.put(obj)
            }
            return InvoiceEntity(
                id = invoice.id,
                invoiceNumber = invoice.invoiceNumber,
                issueDate = invoice.issueDate,
                dueDate = invoice.dueDate,
                currency = invoice.currency,
                businessName = invoice.businessName,
                businessAddress = invoice.businessAddress,
                businessPhone = invoice.businessPhone,
                businessEmail = invoice.businessEmail,
                businessLogoPath = invoice.businessLogoPath,
                clientName = invoice.clientName,
                clientAddress = invoice.clientAddress,
                clientPhone = invoice.clientPhone,
                clientEmail = invoice.clientEmail,
                itemsJson = jsonArray.toString(),
                discount = invoice.discount,
                taxRate = invoice.taxRate,
                notes = invoice.notes,
                status = invoice.status,
                createdAt = invoice.createdAt
            )
        }
    }
}
