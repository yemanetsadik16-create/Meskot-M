package com.example.ui.components

import android.annotation.SuppressLint
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.Gold
import com.example.ui.theme.GoldDeep
import com.example.ui.theme.Ink
import com.example.ui.theme.LineBorder
import com.example.ui.theme.MutedText

/**
 * Android JavaScript Interface bridge connecting the HTML Chapa inline checkout with Compose.
 */
class ChapaAndroidBridge(
    private val onSuccess: (txRef: String, amount: String) -> Unit,
    private val onCancel: () -> Unit
) {
    @JavascriptInterface
    fun paymentSuccess(txRef: String, amount: String) {
        onSuccess(txRef, amount)
    }

    @JavascriptInterface
    fun paymentCancel() {
        onCancel()
    }
}

/**
 * Full-screen modal integrating the official Chapa Inline Checkout into Meskot.
 * Powered by public key CHAPUBK_TEST-1PW1FKvNMh2tx4k5hHPibEZA4A6GPpRc and supports
 * Telebirr, CBE Birr, eBirr, M-Pesa, and Cards.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun ChapaPaymentModal(
    amount: Double,
    title: String,
    txRef: String,
    email: String,
    firstName: String,
    lastName: String,
    publicKey: String = "CHAPUBK_TEST-1PW1FKvNMh2tx4k5hHPibEZA4A6GPpRc",
    isLiveMode: Boolean = false,
    onDismiss: () -> Unit,
    onPaymentSuccess: (txRef: String) -> Unit
) {
    var isLoading by remember { mutableStateOf(true) }

    val formattedAmount = if (amount % 1.0 == 0.0) amount.toInt().toString() else "%.2f".format(amount)

    val htmlContent = remember(amount, txRef, email, firstName, lastName, publicKey) {
        """
        <!DOCTYPE html>
        <html>
        <head>
          <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
          <title>Chapa Integration</title>
          <script src="https://js.chapa.co/v1/inline.js"></script>
          <style>
            * { box-sizing: border-box; }
            body {
              font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
              margin: 0;
              padding: 16px;
              background-color: #FFFFFF;
              color: #1A202C;
            }
            .header {
              text-align: center;
              margin-bottom: 12px;
            }
            .header h1 {
              margin: 0 0 4px 0;
              font-size: 24px;
              font-weight: 800;
              color: #1B2A22;
              letter-spacing: -0.5px;
            }
            .header .badge-tag {
              display: inline-block;
              font-size: 11px;
              color: #C48F37;
              font-weight: 700;
              text-transform: uppercase;
              letter-spacing: 0.5px;
            }
            .amount-card {
              background: #FDFBF7;
              border: 1px solid #E2D3B8;
              border-radius: 12px;
              padding: 12px 14px;
              text-align: center;
              margin-bottom: 14px;
            }
            .amount-card .val {
              font-size: 28px;
              font-weight: 800;
              color: #C48F37;
            }
            .amount-card .lbl {
              font-size: 12px;
              color: #64748B;
              margin-top: 2px;
            }
            .methods-row {
              display: flex;
              justify-content: center;
              gap: 6px;
              flex-wrap: wrap;
              margin-top: 8px;
            }
            .method-chip {
              background: #FFFFFF;
              border: 1px solid #E2E8F0;
              border-radius: 6px;
              padding: 3px 8px;
              font-size: 11px;
              font-weight: 600;
              color: #334155;
            }
            #chapa-inline-form {
              min-height: 250px;
              background: #FFFFFF;
              border-radius: 10px;
              margin-top: 4px;
            }
            .chapa-pay-button {
              background-color: #4CAF50 !important;
              color: white !important;
              font-weight: bold !important;
              font-size: 16px !important;
              padding: 12px 20px !important;
              border-radius: 8px !important;
              width: 100% !important;
              border: none !important;
              cursor: pointer !important;
              box-shadow: 0 4px 6px -1px rgba(76, 175, 80, 0.3) !important;
              margin-top: 10px !important;
            }
            .fallback-container {
              margin-top: 20px;
              padding-top: 14px;
              border-top: 1px dashed #E2E8F0;
              text-align: center;
            }
            .fallback-title {
              font-size: 12px;
              color: #64748B;
              margin-bottom: 8px;
            }
            .btn-confirm-payment {
              background-color: #C48F37;
              color: #FFFFFF;
              font-weight: 700;
              font-size: 13px;
              padding: 10px 18px;
              border-radius: 8px;
              border: none;
              width: 100%;
              cursor: pointer;
            }
            .security-foot {
              margin-top: 12px;
              font-size: 11px;
              color: #94A3B8;
              text-align: center;
            }
          </style>
        </head>
        <body>
          <div class="header">
            <h1>Meskot</h1>
            <div class="badge-tag">Chapa Inline Checkout</div>
          </div>

          <div class="amount-card">
            <div class="val">$formattedAmount ETB</div>
            <div class="lbl">$title</div>
            <div class="methods-row">
              <span class="method-chip">📱 Telebirr</span>
              <span class="method-chip">🏦 CBE Birr</span>
              <span class="method-chip">💳 eBirr</span>
              <span class="method-chip">⚡ M-Pesa</span>
              <span class="method-chip">💳 Card</span>
            </div>
          </div>

          <div id="chapa-inline-form"></div>

          <div class="fallback-container">
            <div class="fallback-title">Completed via Telebirr / CBE app?</div>
            <button class="btn-confirm-payment" onclick="notifySuccess('$txRef')">I Have Sent Payment (Verify)</button>
          </div>

          <div class="security-foot">
            🔒 256-Bit SSL Encrypted · Authorized by National Bank of Ethiopia
          </div>

          <script>
            const amount = '$formattedAmount';
            const tx_ref = '$txRef';
            const email = '$email';
            const first_name = '$firstName';
            const last_name = '$lastName';

            function notifySuccess(ref) {
              if (window.AndroidBridge && window.AndroidBridge.paymentSuccess) {
                window.AndroidBridge.paymentSuccess(ref || tx_ref, amount);
              } else {
                window.location.href = 'https://yourdomain.com/success?tx_ref=' + encodeURIComponent(ref || tx_ref);
              }
            }

            function notifyCancel() {
              if (window.AndroidBridge && window.AndroidBridge.paymentCancel) {
                window.AndroidBridge.paymentCancel();
              } else {
                window.location.href = 'https://yourdomain.com/cancel';
              }
            }

            try {
              const chapa = new ChapaCheckout({
                publicKey: '$publicKey',
                amount: amount,
                currency: 'ETB',
                tx_ref: tx_ref,
                email: email,
                first_name: first_name,
                last_name: last_name,
                availablePaymentMethods: ['telebirr', 'cbebirr', 'ebirr', 'mpesa', 'chapa'],
                customizations: {
                  buttonText: 'Pay ' + amount + ' ETB Now',
                  styles: `
                    .chapa-pay-button {
                      background-color: #4CAF50;
                      color: white;
                    }
                  `
                },
                callbackUrl: 'https://yourdomain.com/callback',
                returnUrl: 'https://yourdomain.com/success',
                onSuccess: function(data) {
                  notifySuccess(data && data.tx_ref ? data.tx_ref : tx_ref);
                },
                onClose: function() {
                  notifyCancel();
                }
              });

              chapa.initialize('chapa-inline-form');
            } catch (e) {
              console.error('Chapa init error: ', e);
            }
          </script>
        </body>
        </html>
        """.trimIndent()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .testTag("chapa_payment_modal"),
            color = Color.White
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top App Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        MeskotLogoBadge(size = 32.dp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                          Row(verticalAlignment = Alignment.CenterVertically) {
                              Text(
                                  text = if (isLiveMode) "Chapa Live Pay" else "Chapa Pay",
                                  fontSize = 17.sp,
                                  fontWeight = FontWeight.Bold,
                                  color = Ink
                              )
                              Spacer(modifier = Modifier.width(6.dp))
                              Box(
                                  modifier = Modifier
                                      .clip(RoundedCornerShape(4.dp))
                                      .background(if (isLiveMode) Color(0xFFE8F5E9) else Color(0xFFFEF3C7))
                                      .padding(horizontal = 6.dp, vertical = 2.dp)
                              ) {
                                  Text(
                                      text = if (isLiveMode) "LIVE ETB" else "TEST GATEWAY",
                                      fontSize = 9.sp,
                                      fontWeight = FontWeight.ExtraBold,
                                      color = if (isLiveMode) Color(0xFF2E7D32) else Color(0xFFB45309)
                                  )
                              }
                          }
                          Row(verticalAlignment = Alignment.CenterVertically) {
                              Icon(
                                  imageVector = Icons.Default.Lock,
                                  contentDescription = "Secure",
                                  tint = if (isLiveMode) Color(0xFF2E7D32) else MutedText,
                                  modifier = Modifier.size(10.dp)
                              )
                              Spacer(modifier = Modifier.width(3.dp))
                              Text(
                                  text = if (isLiveMode) "Real Money (ETB) · 256-Bit SSL" else "256-Bit SSL · Sandbox Mode",
                                  fontSize = 11.sp,
                                  color = if (isLiveMode) Color(0xFF2E7D32) else MutedText
                              )
                          }
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFFF1F5F9), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Ink,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(LineBorder)
                )

                // WebView Container
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    AndroidView(
                        factory = { ctx ->
                            WebView(ctx).apply {
                                settings.javaScriptEnabled = true
                                settings.domStorageEnabled = true
                                settings.loadWithOverviewMode = true
                                settings.useWideViewPort = true
                                settings.databaseEnabled = true
                                settings.allowFileAccess = false
                                setBackgroundColor(android.graphics.Color.WHITE)

                                addJavascriptInterface(
                                    ChapaAndroidBridge(
                                        onSuccess = { ref, _ ->
                                            post { onPaymentSuccess(ref) }
                                        },
                                        onCancel = {
                                            post { onDismiss() }
                                        }
                                    ),
                                    "AndroidBridge"
                                )

                                webChromeClient = WebChromeClient()
                                webViewClient = object : WebViewClient() {
                                    override fun onPageFinished(view: WebView?, url: String?) {
                                        super.onPageFinished(view, url)
                                        isLoading = false
                                    }

                                    override fun shouldOverrideUrlLoading(
                                        view: WebView?,
                                        request: WebResourceRequest?
                                    ): Boolean {
                                        val url = request?.url?.toString() ?: ""
                                        if (url.contains("yourdomain.com/success") || url.contains("callback") || url.contains("status=success")) {
                                            val queryRef = request?.url?.getQueryParameter("tx_ref") ?: txRef
                                            onPaymentSuccess(queryRef)
                                            return true
                                        }
                                        if (url.contains("cancel") || url.contains("status=failed")) {
                                            onDismiss()
                                            return true
                                        }
                                        return super.shouldOverrideUrlLoading(view, request)
                                    }
                                }

                                loadDataWithBaseURL("https://yourdomain.com", htmlContent, "text/html", "UTF-8", null)
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    if (isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.White.copy(alpha = 0.85f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(
                                    color = Gold,
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Connecting with Chapa...",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Ink
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
