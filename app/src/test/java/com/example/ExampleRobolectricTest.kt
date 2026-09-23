package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.Invoice
import com.example.data.model.InvoiceItem
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("BillCraft", appName)
    }

    @Test
    fun `calculate invoice totals correctly`() {
        val items = listOf(
            InvoiceItem(description = "Item 1", quantity = 2.0, unitPrice = 1000.0),
            InvoiceItem(description = "Item 2", quantity = 3.0, unitPrice = 500.0)
        )
        val invoice = Invoice(
            items = items,
            discount = 500.0,
            taxRate = 10.0
        )

        // Subtotal = (2 * 1000) + (3 * 500) = 3500.0
        assertEquals(3500.0, invoice.subtotal, 0.001)

        // Subtotal after discount = 3000.0
        // Tax 10% on 3000 = 300.0
        assertEquals(300.0, invoice.taxAmount, 0.001)

        // Grand total = 3000 + 300 = 3300.0
        assertEquals(3300.0, invoice.grandTotal, 0.001)
    }
}
