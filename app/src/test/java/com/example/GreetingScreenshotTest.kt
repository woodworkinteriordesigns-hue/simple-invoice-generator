package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.data.model.Invoice
import com.example.data.model.InvoiceItem
import com.example.ui.components.InvoicePaperPreview
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun invoice_preview_screenshot() {
        val sampleInvoice = Invoice(
            invoiceNumber = "INV-2026-01",
            issueDate = "2026-09-20",
            dueDate = "2026-10-05",
            currency = "Rs",
            businessName = "Woodwork & Interior Designs",
            businessAddress = "Main Boulevard, Phase 5, DHA\nLahore, Pakistan",
            businessPhone = "+92 300 9876543",
            businessEmail = "info@woodworkinteriors.com",
            clientName = "Apex Studio & Co.",
            clientAddress = "Plot 42, Blue Area, Islamabad",
            clientPhone = "+92 321 5554321",
            clientEmail = "orders@apexstudio.com",
            items = listOf(
                InvoiceItem(description = "Bespoke Walnut Conference Desk", quantity = 1.0, unitPrice = 85000.0),
                InvoiceItem(description = "Ergonomic Highback Swivel Chairs", quantity = 8.0, unitPrice = 12000.0)
            ),
            discount = 10000.0,
            taxRate = 5.0,
            notes = "Payment due within 15 days.\nThank you for choosing Woodwork & Interior Designs!"
        )

        composeTestRule.setContent {
            MyApplicationTheme {
                InvoicePaperPreview(invoice = sampleInvoice)
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
    }
}
