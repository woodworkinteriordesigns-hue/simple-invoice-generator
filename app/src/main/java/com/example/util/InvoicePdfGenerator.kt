package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.example.data.model.Invoice
import java.io.File
import java.io.FileOutputStream

object InvoicePdfGenerator {

    private const val PAGE_WIDTH = 595 // A4 standard point width
    private const val PAGE_HEIGHT = 842 // A4 standard point height
    private const val MARGIN = 36f

    fun generatePdf(context: Context, invoice: Invoice): File {
        val invoicesDir = File(context.cacheDir, "invoices")
        if (!invoicesDir.exists()) {
            invoicesDir.mkdirs()
        }

        val sanitizedNumber = invoice.invoiceNumber.replace(Regex("[^a-zA-Z0-9._-]"), "_")
        val outputFile = File(invoicesDir, "Invoice_${sanitizedNumber}.pdf")

        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = document.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        drawInvoice(canvas, invoice)

        document.finishPage(page)

        FileOutputStream(outputFile).use { out ->
            document.writeTo(out)
        }
        document.close()

        return outputFile
    }

    private fun drawInvoice(canvas: Canvas, invoice: Invoice) {
        // Paints
        val paintBg = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), PAGE_HEIGHT.toFloat(), paintBg)

        // Accent top bar
        val paintTopBar = Paint().apply {
            color = Color.parseColor("#1E3A8A") // Deep navy/blue
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 8f, paintTopBar)

        var currentY = 40f

        // Draw Business Logo if available
        var logoWidth = 0f
        var logoBottom = currentY
        if (!invoice.businessLogoPath.isNullOrBlank()) {
            try {
                val logoFile = File(invoice.businessLogoPath)
                if (logoFile.exists()) {
                    val originalBitmap = BitmapFactory.decodeFile(logoFile.absolutePath)
                    if (originalBitmap != null) {
                        val maxW = 90f
                        val maxH = 65f
                        val ratio = (originalBitmap.width.toFloat() / originalBitmap.height.toFloat()).coerceAtLeast(0.1f)
                        val targetW: Float
                        val targetH: Float
                        if (ratio > (maxW / maxH)) {
                            targetW = maxW
                            targetH = maxW / ratio
                        } else {
                            targetH = maxH
                            targetW = maxH * ratio
                        }
                        val scaled = Bitmap.createScaledBitmap(
                            originalBitmap,
                            targetW.toInt().coerceAtLeast(1),
                            targetH.toInt().coerceAtLeast(1),
                            true
                        )
                        canvas.drawBitmap(scaled, MARGIN, currentY, null)
                        logoWidth = targetW + 16f
                        logoBottom = currentY + targetH
                    }
                }
            } catch (_: Exception) {}
        }

        val businessTextX = if (logoWidth > 0f) MARGIN + logoWidth else MARGIN

        // Business Name
        val paintTitle = Paint().apply {
            color = Color.parseColor("#0F172A")
            textSize = 15f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val bName = invoice.businessName
        if (bName.isNotBlank()) {
            canvas.drawText(bName, businessTextX, currentY + 14f, paintTitle)
        }

        // Business Address & Contact
        val paintSub = Paint().apply {
            color = Color.parseColor("#475569")
            textSize = 8.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }

        var bY = currentY + 28f
        val addressLines = invoice.businessAddress.lines()
        for (line in addressLines.take(3)) {
            if (line.isNotBlank()) {
                canvas.drawText(line.trim(), businessTextX, bY, paintSub)
                bY += 12f
            }
        }
        val contactLine = listOfNotNull(
            invoice.businessPhone.takeIf { it.isNotBlank() },
            invoice.businessEmail.takeIf { it.isNotBlank() }
        ).joinToString(" | ")
        if (contactLine.isNotBlank()) {
            canvas.drawText(contactLine, businessTextX, bY, paintSub)
            bY += 12f
        }

        // Right side: INVOICE title & meta details
        val paintInvoiceTitle = Paint().apply {
            color = Color.parseColor("#1E3A8A")
            textSize = 24f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
            isAntiAlias = true
        }
        canvas.drawText("INVOICE", PAGE_WIDTH - MARGIN, currentY + 20f, paintInvoiceTitle)

        val paintMetaLabel = Paint().apply {
            color = Color.parseColor("#64748B")
            textSize = 9f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textAlign = Paint.Align.RIGHT
            isAntiAlias = true
        }
        val paintMetaValue = Paint().apply {
            color = Color.parseColor("#0F172A")
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
            isAntiAlias = true
        }

        var metaY = currentY + 36f
        canvas.drawText("Invoice #: ${invoice.invoiceNumber}", PAGE_WIDTH - MARGIN, metaY, paintMetaValue)
        metaY += 14f
        if (invoice.issueDate.isNotBlank()) {
            canvas.drawText("Date: ${invoice.issueDate}", PAGE_WIDTH - MARGIN, metaY, paintMetaLabel)
            metaY += 13f
        }
        if (invoice.dueDate.isNotBlank()) {
            canvas.drawText("Due Date: ${invoice.dueDate}", PAGE_WIDTH - MARGIN, metaY, paintMetaLabel)
            metaY += 13f
        }

        currentY = maxOf(bY, logoBottom, metaY) + 18f

        // Separator line
        val paintDivider = Paint().apply {
            color = Color.parseColor("#E2E8F0")
            strokeWidth = 1f
        }
        canvas.drawLine(MARGIN, currentY, PAGE_WIDTH - MARGIN, currentY, paintDivider)
        currentY += 16f

        // BILL TO section (Address Box)
        val billToBoxHeight = 68f
        val paintBoxBg = Paint().apply {
            color = Color.parseColor("#F8FAFC")
            style = Paint.Style.FILL
        }
        val paintBoxBorder = Paint().apply {
            color = Color.parseColor("#E2E8F0")
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }
        val boxRect = RectF(MARGIN, currentY, PAGE_WIDTH - MARGIN, currentY + billToBoxHeight)
        canvas.drawRoundRect(boxRect, 6f, 6f, paintBoxBg)
        canvas.drawRoundRect(boxRect, 6f, 6f, paintBoxBorder)

        val paintBillToLabel = Paint().apply {
            color = Color.parseColor("#3B82F6")
            textSize = 8f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("BILL TO (CLIENT DETAILS):", MARGIN + 14f, currentY + 16f, paintBillToLabel)

        val paintClientName = Paint().apply {
            color = Color.parseColor("#0F172A")
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val cName = invoice.clientName
        if (cName.isNotBlank()) {
            canvas.drawText(cName, MARGIN + 14f, currentY + 32f, paintClientName)
        }

        val paintClientDetails = Paint().apply {
            color = Color.parseColor("#475569")
            textSize = 9f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }
        val clientAddressFirstLine = invoice.clientAddress.lines().firstOrNull { it.isNotBlank() } ?: ""
        if (clientAddressFirstLine.isNotBlank()) {
            canvas.drawText(clientAddressFirstLine, MARGIN + 14f, currentY + 46f, paintClientDetails)
        }

        val clientContact = listOfNotNull(
            invoice.clientPhone.takeIf { it.isNotBlank() },
            invoice.clientEmail.takeIf { it.isNotBlank() }
        ).joinToString(" • ")
        if (clientContact.isNotBlank()) {
            canvas.drawText(clientContact, MARGIN + 14f, currentY + 59f, paintClientDetails)
        }

        currentY += billToBoxHeight + 20f

        // Table Header
        val tableHeaderHeight = 24f
        val paintTableHeaderBg = Paint().apply {
            color = Color.parseColor("#1E3A8A")
            style = Paint.Style.FILL
        }
        val headerRect = RectF(MARGIN, currentY, PAGE_WIDTH - MARGIN, currentY + tableHeaderHeight)
        canvas.drawRoundRect(headerRect, 4f, 4f, paintTableHeaderBg)

        val paintHeaderCol = Paint().apply {
            color = Color.WHITE
            textSize = 9f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val colDescX = MARGIN + 12f
        val colQtyX = 330f
        val colPriceX = 420f
        val colTotalX = PAGE_WIDTH - MARGIN - 12f

        canvas.drawText("ITEM DESCRIPTION", colDescX, currentY + 16f, paintHeaderCol)

        paintHeaderCol.textAlign = Paint.Align.RIGHT
        canvas.drawText("QTY", colQtyX, currentY + 16f, paintHeaderCol)
        canvas.drawText("UNIT PRICE", colPriceX, currentY + 16f, paintHeaderCol)
        canvas.drawText("TOTAL", colTotalX, currentY + 16f, paintHeaderCol)

        currentY += tableHeaderHeight + 2f

        // Table Rows
        val paintRowText = Paint().apply {
            color = Color.parseColor("#1E293B")
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }
        val paintRowAltBg = Paint().apply {
            color = Color.parseColor("#F8FAFC")
            style = Paint.Style.FILL
        }

        val rowHeight = 26f
        for ((index, item) in invoice.items.withIndex()) {
            if (index % 2 == 1) {
                canvas.drawRect(MARGIN, currentY, PAGE_WIDTH - MARGIN, currentY + rowHeight, paintRowAltBg)
            }
            // Bottom line
            canvas.drawLine(MARGIN, currentY + rowHeight, PAGE_WIDTH - MARGIN, currentY + rowHeight, paintDivider)

            // Item desc
            paintRowText.textAlign = Paint.Align.LEFT
            val desc = if (item.description.isNotBlank()) item.description else "Item ${index + 1}"
            // Truncate if too long
            val truncatedDesc = if (desc.length > 45) desc.substring(0, 42) + "..." else desc
            canvas.drawText(truncatedDesc, colDescX, currentY + 17f, paintRowText)

            // Qty
            paintRowText.textAlign = Paint.Align.RIGHT
            val qtyText = if (item.quantity % 1.0 == 0.0) "${item.quantity.toInt()}" else "${item.quantity}"
            canvas.drawText(qtyText, colQtyX, currentY + 17f, paintRowText)

            // Unit price
            val priceText = "${invoice.currency} ${invoice.formattedAmount(item.unitPrice)}"
            canvas.drawText(priceText, colPriceX, currentY + 17f, paintRowText)

            // Total
            val totalText = "${invoice.currency} ${invoice.formattedAmount(item.total)}"
            canvas.drawText(totalText, colTotalX, currentY + 17f, paintRowText)

            currentY += rowHeight
        }

        currentY += 16f

        // Totals & Notes Section
        val summaryWidth = 220f
        val summaryX = PAGE_WIDTH - MARGIN - summaryWidth
        var summaryY = currentY

        val paintSummaryLabel = Paint().apply {
            color = Color.parseColor("#64748B")
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textAlign = Paint.Align.LEFT
            isAntiAlias = true
        }
        val paintSummaryValue = Paint().apply {
            color = Color.parseColor("#0F172A")
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
            isAntiAlias = true
        }

        // Subtotal
        canvas.drawText("Subtotal:", summaryX, summaryY, paintSummaryLabel)
        canvas.drawText("${invoice.currency} ${invoice.formattedAmount(invoice.subtotal)}", PAGE_WIDTH - MARGIN - 10f, summaryY, paintSummaryValue)
        summaryY += 16f

        // Discount if any
        if (invoice.discount > 0.0) {
            canvas.drawText("Discount:", summaryX, summaryY, paintSummaryLabel)
            val paintDiscountVal = Paint(paintSummaryValue).apply { color = Color.parseColor("#DC2626") }
            canvas.drawText("- ${invoice.currency} ${invoice.formattedAmount(invoice.discount)}", PAGE_WIDTH - MARGIN - 10f, summaryY, paintDiscountVal)
            summaryY += 16f
        }

        // Tax if any
        if (invoice.taxRate > 0.0) {
            canvas.drawText("Tax (${invoice.taxRate}%):", summaryX, summaryY, paintSummaryLabel)
            canvas.drawText("${invoice.currency} ${invoice.formattedAmount(invoice.taxAmount)}", PAGE_WIDTH - MARGIN - 10f, summaryY, paintSummaryValue)
            summaryY += 16f
        }

        // Grand Total Box
        summaryY += 4f
        val totalBoxHeight = 32f
        val paintTotalBox = Paint().apply {
            color = Color.parseColor("#1E3A8A")
            style = Paint.Style.FILL
        }
        val totalRect = RectF(summaryX - 10f, summaryY, PAGE_WIDTH - MARGIN, summaryY + totalBoxHeight)
        canvas.drawRoundRect(totalRect, 6f, 6f, paintTotalBox)

        val paintGrandTotalLabel = Paint().apply {
            color = Color.WHITE
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.LEFT
            isAntiAlias = true
        }
        val paintGrandTotalValue = Paint().apply {
            color = Color.WHITE
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
            isAntiAlias = true
        }
        canvas.drawText("TOTAL DUE:", summaryX, summaryY + 20f, paintGrandTotalLabel)
        canvas.drawText("${invoice.currency} ${invoice.formattedAmount(invoice.grandTotal)}", PAGE_WIDTH - MARGIN - 12f, summaryY + 20f, paintGrandTotalValue)

        // Notes on Left Side
        val notesWidth = summaryX - MARGIN - 30f
        var notesY = currentY
        val paintNotesHeader = Paint().apply {
            color = Color.parseColor("#334155")
            textSize = 9f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("TERMS & PAYMENT NOTES:", MARGIN, notesY, paintNotesHeader)
        notesY += 14f

        val paintNotesBody = Paint().apply {
            color = Color.parseColor("#64748B")
            textSize = 8.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }

        for (line in invoice.notes.lines().take(5)) {
            if (line.isNotBlank()) {
                val safeLine = if (line.length > 55) line.substring(0, 52) + "..." else line
                canvas.drawText(safeLine, MARGIN, notesY, paintNotesBody)
                notesY += 12f
            }
        }

        // Bottom Footer clean end
    }
}
