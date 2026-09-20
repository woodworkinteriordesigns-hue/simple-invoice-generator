package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Invoice
import com.example.ui.theme.Red600
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate500

@Composable
fun InvoiceTotalsCard(
    invoice: Invoice,
    onDiscountChange: (Double) -> Unit,
    onTaxRateChange: (Double) -> Unit,
    onNotesChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("invoice_totals_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, Slate200)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Calculate,
                        contentDescription = "Summary",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Total & Payment Notes",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Discounts, taxes, and final payable amount",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate500
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Subtotal Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Subtotal:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Slate500
                )
                Text(
                    text = "${invoice.currency} ${invoice.formattedAmount(invoice.subtotal)}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Discount & Tax inputs
            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = if (invoice.discount == 0.0) "" else if (invoice.discount % 1.0 == 0.0) "${invoice.discount.toLong()}" else "${invoice.discount}",
                    onValueChange = { str ->
                        val disc = str.toDoubleOrNull() ?: 0.0
                        onDiscountChange(disc)
                    },
                    label = { Text("Discount (${invoice.currency})") },
                    placeholder = { Text("0") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("discount_input"),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.width(10.dp))

                OutlinedTextField(
                    value = if (invoice.taxRate == 0.0) "" else if (invoice.taxRate % 1.0 == 0.0) "${invoice.taxRate.toLong()}" else "${invoice.taxRate}",
                    onValueChange = { str ->
                        val rate = str.toDoubleOrNull() ?: 0.0
                        onTaxRateChange(rate)
                    },
                    label = { Text("Tax / VAT (%)") },
                    placeholder = { Text("0%") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("tax_rate_input"),
                    shape = RoundedCornerShape(10.dp)
                )
            }

            if (invoice.discount > 0.0) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Discount Applied:", style = MaterialTheme.typography.bodySmall, color = Red600)
                    Text("- ${invoice.currency} ${invoice.formattedAmount(invoice.discount)}", style = MaterialTheme.typography.bodySmall, color = Red600, fontWeight = FontWeight.Bold)
                }
            }

            if (invoice.taxRate > 0.0) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Tax (${invoice.taxRate}%):", style = MaterialTheme.typography.bodySmall, color = Slate500)
                    Text("+ ${invoice.currency} ${invoice.formattedAmount(invoice.taxAmount)}", style = MaterialTheme.typography.bodySmall, color = Slate500, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Grand Total Card Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(vertical = 14.dp, horizontal = 16.dp)
                    .testTag("grand_total_box")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "TOTAL PAYABLE",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Due Amount",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                            fontSize = 11.sp
                        )
                    }

                    Text(
                        text = "${invoice.currency} ${invoice.formattedAmount(invoice.grandTotal)}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.testTag("grand_total_text")
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = Slate200)
            Spacer(modifier = Modifier.height(14.dp))

            // Notes / Terms
            OutlinedTextField(
                value = invoice.notes,
                onValueChange = onNotesChange,
                label = { Text("Payment Terms & Bank / Payment Instructions") },
                placeholder = { Text("Bank account, JazzCash/EasyPaisa number, or payment terms...") },
                minLines = 2,
                maxLines = 4,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = "Notes",
                        tint = Slate500
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("notes_input"),
                shape = RoundedCornerShape(10.dp)
            )
        }
    }
}
