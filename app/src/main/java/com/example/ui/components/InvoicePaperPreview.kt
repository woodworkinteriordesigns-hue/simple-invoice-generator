package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Invoice
import com.example.ui.theme.Blue600
import com.example.ui.theme.Navy900
import com.example.ui.theme.Red600
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import java.io.File

@Composable
fun InvoicePaperPreview(
    invoice: Invoice,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("invoice_paper_preview"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Slate200),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Top Accent Ribbon
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .background(Color(0xFF1E3A8A))
            )

            Column(modifier = Modifier.padding(20.dp)) {
                // Header: Logo & Business Info + INVOICE details
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    // Left: Logo & Business Details
                    Column(modifier = Modifier.weight(1.3f)) {
                        if (!invoice.businessLogoPath.isNullOrBlank() && File(invoice.businessLogoPath).exists()) {
                            AsyncImage(
                                model = File(invoice.businessLogoPath),
                                contentDescription = "Business Logo",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .size(width = 90.dp, height = 50.dp)
                                    .clip(RoundedCornerShape(4.dp))
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                        }

                        Text(
                            text = invoice.businessName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Navy900
                        )

                        invoice.businessAddress.lines().take(3).forEach { line ->
                            if (line.isNotBlank()) {
                                Text(
                                    text = line.trim(),
                                    fontSize = 10.sp,
                                    color = Slate500,
                                    lineHeight = 14.sp
                                )
                            }
                        }

                        val contactInfo = listOfNotNull(
                            invoice.businessPhone.takeIf { it.isNotBlank() },
                            invoice.businessEmail.takeIf { it.isNotBlank() }
                        ).joinToString(" • ")
                        if (contactInfo.isNotBlank()) {
                            Text(
                                text = contactInfo,
                                fontSize = 10.sp,
                                color = Slate500
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Right: "INVOICE" Title & Meta
                    Column(
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier.weight(0.9f)
                    ) {
                        Text(
                            text = "INVOICE",
                            fontWeight = FontWeight.Black,
                            fontSize = 22.sp,
                            color = Color(0xFF1E3A8A)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = invoice.invoiceNumber,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Navy900
                        )
                        if (invoice.issueDate.isNotBlank()) {
                            Text(
                                text = "Date: ${invoice.issueDate}",
                                fontSize = 10.sp,
                                color = Slate500
                            )
                        }
                        if (invoice.dueDate.isNotBlank()) {
                            Text(
                                text = "Due: ${invoice.dueDate}",
                                fontSize = 10.sp,
                                color = Slate500
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Slate200)
                Spacer(modifier = Modifier.height(14.dp))

                // BILL TO Section (Address add krain)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Slate100)
                        .border(1.dp, Slate200, RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            text = "BILL TO (CLIENT DETAILS):",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Blue600,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = invoice.clientName,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Navy900
                        )
                        invoice.clientAddress.lines().forEach { line ->
                            if (line.isNotBlank()) {
                                Text(
                                    text = line.trim(),
                                    fontSize = 10.5.sp,
                                    color = Color(0xFF334155),
                                    lineHeight = 14.sp
                                )
                            }
                        }
                        val clientContact = listOfNotNull(
                            invoice.clientPhone.takeIf { it.isNotBlank() },
                            invoice.clientEmail.takeIf { it.isNotBlank() }
                        ).joinToString(" • ")
                        if (clientContact.isNotBlank()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = clientContact,
                                fontSize = 10.sp,
                                color = Slate500
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Line Items Table Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF1E3A8A))
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ITEM / SERVICE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.weight(2f)
                    )
                    Text(
                        text = "QTY",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(0.7f)
                    )
                    Text(
                        text = "PRICE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.End,
                        modifier = Modifier.weight(1.1f)
                    )
                    Text(
                        text = "TOTAL",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.End,
                        modifier = Modifier.weight(1.2f)
                    )
                }

                // Line Items Rows
                invoice.items.forEachIndexed { index, item ->
                    val isAlt = index % 2 == 1
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (isAlt) Slate100 else Color.White)
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = item.description.ifBlank { "Item #${index + 1}" },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Navy900,
                            modifier = Modifier.weight(2f)
                        )
                        Text(
                            text = if (item.quantity % 1.0 == 0.0) "${item.quantity.toInt()}" else "${item.quantity}",
                            fontSize = 11.sp,
                            color = Color(0xFF334155),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(0.7f)
                        )
                        Text(
                            text = "${invoice.currency} ${invoice.formattedAmount(item.unitPrice)}",
                            fontSize = 11.sp,
                            color = Color(0xFF334155),
                            textAlign = TextAlign.End,
                            modifier = Modifier.weight(1.1f)
                        )
                        Text(
                            text = "${invoice.currency} ${invoice.formattedAmount(item.total)}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Navy900,
                            textAlign = TextAlign.End,
                            modifier = Modifier.weight(1.2f)
                        )
                    }
                    HorizontalDivider(color = Slate200, thickness = 0.5.dp)
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Summary & Notes Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    // Notes on left
                    Column(modifier = Modifier.weight(1.2f)) {
                        Text(
                            text = "TERMS & PAYMENT:",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate500,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = invoice.notes.ifBlank { "Payment due on receipt." },
                            fontSize = 10.sp,
                            color = Slate500,
                            lineHeight = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Calculations on right
                    Column(
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier.weight(1.3f)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Subtotal:", fontSize = 11.sp, color = Slate500)
                            Text(
                                "${invoice.currency} ${invoice.formattedAmount(invoice.subtotal)}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Navy900
                            )
                        }

                        if (invoice.discount > 0.0) {
                            Spacer(modifier = Modifier.height(3.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Discount:", fontSize = 10.sp, color = Red600)
                                Text(
                                    "- ${invoice.currency} ${invoice.formattedAmount(invoice.discount)}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Red600
                                )
                            }
                        }

                        if (invoice.taxRate > 0.0) {
                            Spacer(modifier = Modifier.height(3.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Tax (${invoice.taxRate}%):", fontSize = 10.sp, color = Slate500)
                                Text(
                                    "${invoice.currency} ${invoice.formattedAmount(invoice.taxAmount)}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Navy900
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Total Due Highlight Box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF1E3A8A))
                                .padding(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "TOTAL DUE:",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "${invoice.currency} ${invoice.formattedAmount(invoice.grandTotal)}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                // Footer clean end
            }
        }
    }
}
