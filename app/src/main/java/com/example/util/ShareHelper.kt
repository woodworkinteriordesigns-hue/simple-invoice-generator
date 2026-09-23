package com.example.util

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

object ShareHelper {

    fun copyUriToInternalFile(context: Context, sourceUri: Uri): String? {
        return try {
            val logosDir = File(context.filesDir, "logos")
            if (!logosDir.exists()) {
                logosDir.mkdirs()
            }
            val fileName = "logo_${System.currentTimeMillis()}.png"
            val destFile = File(logosDir, fileName)

            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }
            destFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun getFileUri(context: Context, file: File): Uri {
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    fun sharePdf(
        context: Context,
        pdfFile: File,
        invoiceNumber: String,
        clientName: String
    ) {
        try {
            if (!pdfFile.exists()) {
                Toast.makeText(context, "PDF not found. Please regenerate.", Toast.LENGTH_SHORT).show()
                return
            }
            val uri = getFileUri(context, pdfFile)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(
                    Intent.EXTRA_SUBJECT,
                    "Invoice $invoiceNumber for ${clientName.ifBlank { "Client" }}"
                )
                putExtra(
                    Intent.EXTRA_TEXT,
                    "Assalam-o-Alaikum / Hello,\n\nPlease find attached invoice $invoiceNumber for your recent order/service. Thank you for your business!"
                )
                clipData = ClipData.newRawUri("Invoice PDF", uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(intent, "Send Invoice via...").apply {
                clipData = ClipData.newRawUri("Invoice PDF", uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                if (context !is android.app.Activity) {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(context, "Error sharing invoice: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    fun shareOnWhatsApp(
        context: Context,
        pdfFile: File,
        invoiceNumber: String,
        clientName: String
    ) {
        if (!pdfFile.exists()) {
            Toast.makeText(context, "PDF not found. Please regenerate.", Toast.LENGTH_SHORT).show()
            return
        }
        val clientGreeting = if (clientName.isNotBlank()) "Dear $clientName," else "Respected Client,"
        val messageText = "$clientGreeting\n\nPlease find attached your Invoice #$invoiceNumber.\nThank you for doing business with us!\n\n(Generated via Invoice Generator)"
        try {
            val uri = getFileUri(context, pdfFile)
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_TEXT, messageText)
                clipData = ClipData.newRawUri("Invoice PDF", uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                setPackage("com.whatsapp")
                if (context !is android.app.Activity) {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            }
            context.startActivity(sendIntent)
        } catch (_: Exception) {
            // WhatsApp not installed or WhatsApp Business installed -> try WhatsApp Business or chooser
            try {
                val uri = getFileUri(context, pdfFile)
                val businessIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_TEXT, messageText)
                    clipData = ClipData.newRawUri("Invoice PDF", uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    setPackage("com.whatsapp.w4b")
                    if (context !is android.app.Activity) {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                }
                context.startActivity(businessIntent)
            } catch (_: Exception) {
                // Fallback to standard share chooser
                sharePdf(context, pdfFile, invoiceNumber, clientName)
            }
        }
    }

    fun shareViaEmail(
        context: Context,
        pdfFile: File,
        invoiceNumber: String,
        clientName: String,
        clientEmail: String
    ) {
        if (!pdfFile.exists()) {
            Toast.makeText(context, "PDF not found. Please regenerate.", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            val uri = getFileUri(context, pdfFile)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                if (clientEmail.isNotBlank()) {
                    putExtra(Intent.EXTRA_EMAIL, arrayOf(clientEmail))
                }
                putExtra(
                    Intent.EXTRA_SUBJECT,
                    "Invoice $invoiceNumber - ${clientName.ifBlank { "Client" }}"
                )
                putExtra(
                    Intent.EXTRA_TEXT,
                    "Dear ${clientName.ifBlank { "Customer" }},\n\nPlease find attached the invoice $invoiceNumber. Kindly review and let us know if you have any questions.\n\nThank you!"
                )
                putExtra(Intent.EXTRA_STREAM, uri)
                clipData = ClipData.newRawUri("Invoice PDF", uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(intent, "Send Email via...").apply {
                clipData = ClipData.newRawUri("Invoice PDF", uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                if (context !is android.app.Activity) {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            sharePdf(context, pdfFile, invoiceNumber, clientName)
        }
    }

    fun openPdf(context: Context, pdfFile: File) {
        try {
            if (!pdfFile.exists()) {
                Toast.makeText(context, "PDF not found.", Toast.LENGTH_SHORT).show()
                return
            }
            val uri = getFileUri(context, pdfFile)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                clipData = ClipData.newRawUri("Invoice PDF", uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                if (context !is android.app.Activity) {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "No PDF viewer app found. Please install a PDF reader.", Toast.LENGTH_LONG).show()
        }
    }

    fun printPdf(context: Context, pdfFile: File, jobName: String = "Invoice Print") {
        try {
            val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
            if (printManager != null) {
                val printAdapter = object : PrintDocumentAdapter() {
                    override fun onLayout(
                        oldAttributes: PrintAttributes?,
                        newAttributes: PrintAttributes?,
                        cancellationSignal: CancellationSignal?,
                        callback: LayoutResultCallback?,
                        extras: android.os.Bundle?
                    ) {
                        if (cancellationSignal?.isCanceled == true) {
                            callback?.onLayoutCancelled()
                            return
                        }
                        val info = PrintDocumentInfo.Builder("$jobName.pdf")
                            .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                            .setPageCount(1)
                            .build()
                        callback?.onLayoutFinished(info, true)
                    }

                    override fun onWrite(
                        pages: Array<out PageRange>?,
                        destination: ParcelFileDescriptor?,
                        cancellationSignal: CancellationSignal?,
                        callback: WriteResultCallback?
                    ) {
                        try {
                            FileInputStream(pdfFile).use { input ->
                                FileOutputStream(destination?.fileDescriptor).use { output ->
                                    input.copyTo(output)
                                }
                            }
                            callback?.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
                        } catch (e: Exception) {
                            callback?.onWriteFailed(e.message)
                        }
                    }
                }
                printManager.print(jobName, printAdapter, PrintAttributes.Builder().build())
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Printing error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }
}
