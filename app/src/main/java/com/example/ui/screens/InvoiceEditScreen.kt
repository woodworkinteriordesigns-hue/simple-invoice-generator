package com.example.ui.screens

import android.app.Activity
import com.example.util.AdManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.BusinessAddressCard
import com.example.ui.components.ClientAddressCard
import com.example.ui.components.InvoiceItemsEditor
import com.example.ui.components.InvoiceMetaCard
import com.example.ui.components.InvoiceTotalsCard
import com.example.ui.components.LogoUploadCard
import com.example.ui.theme.Slate200
import com.example.ui.viewmodel.AppTab
import com.example.ui.viewmodel.InvoiceViewModel
import com.example.util.ShareHelper

@Composable
fun InvoiceEditScreen(
    viewModel: InvoiceViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val invoice by viewModel.currentInvoice.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .testTag("invoice_edit_scroll_list"),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header banner
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Create Invoice",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Fill addresses, add items, and export instantly",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilledTonalButton(
                            onClick = { viewModel.toggleDarkMode() },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("theme_toggle_btn")
                        ) {
                            Icon(
                                imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = if (isDarkMode) "Switch to Light Mode" else "Switch to Dark Mode",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isDarkMode) "Light" else "Dark",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        OutlinedButton(
                            onClick = { viewModel.createNewInvoice() },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("new_invoice_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "New",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("New", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            // 1. Logo Upload
            item {
                LogoUploadCard(
                    logoPath = invoice.businessLogoPath,
                    onLogoSelected = { uri -> viewModel.updateBusinessLogo(uri) },
                    onRemoveLogo = { viewModel.removeBusinessLogo() }
                )
            }

            // 2. Business Address (Sender)
            item {
                BusinessAddressCard(
                    businessName = invoice.businessName,
                    businessAddress = invoice.businessAddress,
                    businessPhone = invoice.businessPhone,
                    businessEmail = invoice.businessEmail,
                    onNameChange = { viewModel.updateBusinessName(it) },
                    onAddressChange = { viewModel.updateBusinessAddress(it) },
                    onPhoneChange = { viewModel.updateBusinessPhone(it) },
                    onEmailChange = { viewModel.updateBusinessEmail(it) },
                    onSaveAsDefault = { viewModel.saveAsDefaultBusinessProfile() }
                )
            }

            // 3. Client Address ("just address add krain")
            item {
                ClientAddressCard(
                    clientName = invoice.clientName,
                    clientAddress = invoice.clientAddress,
                    clientPhone = invoice.clientPhone,
                    clientEmail = invoice.clientEmail,
                    onNameChange = { viewModel.updateClientName(it) },
                    onAddressChange = { viewModel.updateClientAddress(it) },
                    onPhoneChange = { viewModel.updateClientPhone(it) },
                    onEmailChange = { viewModel.updateClientEmail(it) }
                )
            }

            // 4. Invoice Information (Number, Dates, Currency)
            item {
                InvoiceMetaCard(
                    invoiceNumber = invoice.invoiceNumber,
                    issueDate = invoice.issueDate,
                    dueDate = invoice.dueDate,
                    currency = invoice.currency,
                    onInvoiceNumberChange = { viewModel.updateInvoiceNumber(it) },
                    onIssueDateChange = { viewModel.updateIssueDate(it) },
                    onDueDateChange = { viewModel.updateDueDate(it) },
                    onCurrencyChange = { viewModel.updateCurrency(it) }
                )
            }

            // 5. Items Editor
            item {
                InvoiceItemsEditor(
                    items = invoice.items,
                    currency = invoice.currency,
                    onAddItem = { viewModel.addItem() },
                    onUpdateItem = { idx, item -> viewModel.updateItem(idx, item) },
                    onRemoveItem = { idx -> viewModel.removeItem(idx) }
                )
            }

            // 6. Totals & Notes
            item {
                InvoiceTotalsCard(
                    invoice = invoice,
                    onDiscountChange = { viewModel.updateDiscount(it) },
                    onTaxRateChange = { viewModel.updateTaxRate(it) },
                    onNotesChange = { viewModel.updateNotes(it) }
                )
            }
        }

        // Bottom Sticky Action Bar for 1-Tap PDF & Sharing
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            shadowElevation = 12.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = {
                        viewModel.generatePdfAndSwitchToPreview()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("preview_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = "Preview",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Preview", fontWeight = FontWeight.SemiBold)
                }

                Button(
                    onClick = {
                        val activity = context as? Activity
                        AdManager.showInterstitial(activity) {
                            viewModel.shareCurrentPdf { file, invNo, client ->
                                ShareHelper.shareOnWhatsApp(context, file, invNo, client)
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1.4f)
                        .height(48.dp)
                        .testTag("whatsapp_share_direct_btn"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF25D366),
                        contentColor = Color.White
                    )
                ) {
                    if (isGenerating) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "WhatsApp",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "WhatsApp",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Button(
                    onClick = {
                        val activity = context as? Activity
                        AdManager.showInterstitial(activity) {
                            viewModel.shareCurrentPdf { file, invNo, client ->
                                ShareHelper.sharePdf(context, file, invNo, client)
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1.3f)
                        .height(48.dp)
                        .testTag("share_pdf_main_btn"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Share PDF",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
