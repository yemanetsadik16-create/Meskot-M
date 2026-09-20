package com.example.ui.components

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.data.LiveStreamComment
import com.example.data.LiveStreamSession
import com.example.data.User

/**
 * JavaScript Interface Bridge between Android Kotlin and the Ethiopian Cultural Gifting HTML Engine.
 */
class MeskotLiveGiftsBridge(
    private val onGiftSent: (giftId: Int, giftName: String, giftIcon: String, coins: Int) -> Unit,
    private val onDepositSuccess: (coins: Int, amountEtb: Double, txRef: String) -> Unit,
    private val onCloseLive: () -> Unit,
    private val onSendComment: (String) -> Unit = {},
    private val onSendLike: () -> Unit = {},
    private val onEndLive: () -> Unit = {}
) {
    @JavascriptInterface
    fun sendGift(giftId: Int, giftName: String, giftIcon: String, coins: Int) {
        onGiftSent(giftId, giftName, giftIcon, coins)
    }

    @JavascriptInterface
    fun depositSuccess(coins: Int, amountEtb: Double, txRef: String) {
        onDepositSuccess(coins, amountEtb, txRef)
    }

    @JavascriptInterface
    fun closeLive() {
        onCloseLive()
    }

    @JavascriptInterface
    fun sendComment(text: String) {
        onSendComment(text)
    }

    @JavascriptInterface
    fun sendLike() {
        onSendLike()
    }

    @JavascriptInterface
    fun endLive() {
        onEndLive()
    }
}

/**
 * Generates the full Ethiopian Cultural Gifting HTML engine with interactive 100 cultural gifts,
 * particle effects, animated coin counter, Chapa/Telebirr/CBE payment integration, and native Android bridge.
 */
fun buildLiveGiftsHtml(
    initialCoins: Int,
    userName: String,
    streamerName: String = "Meskot Official",
    viewerCount: String = "52.4k Viewers",
    streamTitle: String = "Meskot Live",
    isHost: Boolean = false,
    likesCount: Int = 0
): String {
    val escapedUserName = userName.replace("'", "\\'").replace("\"", "\\\"")
    val escapedStreamerName = streamerName.replace("'", "\\'").replace("\"", "\\\"")
    val escapedStreamTitle = streamTitle.replace("'", "\\'").replace("\"", "\\\"")

    return """
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
  <title>Meskot - Ethiopian Cultural Gifting Engine</title>
  <script src="https://js.chapa.co/v1/inline.js"></script>
  <style>
    * {
      box-sizing: border-box;
      margin: 0;
      padding: 0;
      font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
      user-select: none;
      -webkit-tap-highlight-color: transparent;
    }

    body {
      background-color: #0b0d0f;
      display: flex;
      justify-content: center;
      align-items: center;
      min-height: 100vh;
      color: #fff;
      overflow: hidden;
    }

    .phone-screen {
      width: 100%;
      height: 100vh;
      max-width: 480px;
      background: linear-gradient(180deg, #0e1511 0%, #171d18 100%);
      position: relative;
      overflow: hidden;
    }

    .live-stream-bg {
      position: absolute;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
      background: radial-gradient(circle at 50% 35%, #1c3527, #0d1a13, #050a07);
      z-index: 1;
    }

    .top-bar {
      position: absolute;
      top: 14px;
      left: 12px;
      right: 12px;
      display: flex;
      justify-content: space-between;
      align-items: center;
      z-index: 10;
    }

    .wallet-group {
      display: flex;
      align-items: center;
      gap: 6px;
    }

    .deposit-btn {
      background: linear-gradient(135deg, #009a44, #007a36);
      color: #fff;
      border: 1px solid rgba(254, 209, 0, 0.6);
      padding: 6px 10px;
      border-radius: 16px;
      font-size: 11px;
      font-weight: 700;
      cursor: pointer;
      box-shadow: 0 2px 10px rgba(0, 154, 68, 0.5);
      transition: transform 0.15s ease, box-shadow 0.15s ease;
    }

    .deposit-btn:hover {
      box-shadow: 0 2px 16px rgba(254, 209, 0, 0.5);
    }

    .deposit-btn:active {
      transform: scale(0.94);
    }

    .close-live-btn {
      background: rgba(0, 0, 0, 0.5);
      color: #fff;
      border: 1px solid rgba(255, 255, 255, 0.2);
      width: 28px;
      height: 28px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      cursor: pointer;
      font-size: 13px;
    }

    .end-broadcast-btn {
      background: linear-gradient(135deg, #ef2b2d, #c41f21);
      color: #fff;
      border: 1px solid rgba(255, 255, 255, 0.3);
      padding: 5px 9px;
      border-radius: 14px;
      font-size: 10.5px;
      font-weight: 700;
      cursor: pointer;
      box-shadow: 0 2px 8px rgba(239, 43, 45, 0.5);
    }

    .live-pulse-badge {
      background: #ef2b2d;
      color: #fff;
      font-size: 8.5px;
      font-weight: 800;
      padding: 1px 5px;
      border-radius: 6px;
      letter-spacing: 0.5px;
      display: inline-block;
      animation: pulseLive 1.5s infinite;
    }

    @keyframes pulseLive {
      0% { opacity: 1; transform: scale(1); }
      50% { opacity: 0.65; transform: scale(1.06); }
      100% { opacity: 1; transform: scale(1); }
    }

    .streamer-info {
      display: flex;
      align-items: center;
      background: rgba(0, 0, 0, 0.55);
      padding: 4px 10px;
      border-radius: 20px;
      backdrop-filter: blur(8px);
      border: 1px solid rgba(255, 215, 0, 0.2);
    }

    .avatar {
      width: 30px;
      height: 30px;
      border-radius: 50%;
      background: linear-gradient(135deg, #009a44, #fed100, #ef2b2d);
      margin-right: 8px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-weight: bold;
      font-size: 10px;
      color: #fff;
      text-shadow: 0 1px 2px #000;
    }

    .coin-badge {
      position: relative;
      overflow: hidden;
      background: linear-gradient(135deg, rgba(70, 55, 10, 0.55), rgba(20, 16, 4, 0.55));
      border: 1px solid rgba(254, 209, 0, 0.55);
      color: #ffe27a;
      padding: 5px 11px;
      border-radius: 20px;
      font-weight: 700;
      font-size: 12px;
      letter-spacing: 0.2px;
      backdrop-filter: blur(8px);
      box-shadow: 0 0 0 1px rgba(0,0,0,0.3) inset, 0 2px 10px rgba(0,0,0,0.4);
    }

    .coin-badge::before {
      content: '';
      position: absolute;
      top: 0;
      left: -60%;
      width: 40%;
      height: 100%;
      background: linear-gradient(120deg, transparent, rgba(255,255,255,0.35), transparent);
      transform: skewX(-20deg);
      animation: badgeSheen 4.5s ease-in-out infinite;
    }

    .coin-badge.coin-pulse {
      animation: coinPulse 0.5s cubic-bezier(0.34, 1.56, 0.64, 1);
    }

    #coin-balance {
      display: inline-block;
    }

    .fullscreen-overlay {
      position: absolute;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
      pointer-events: none;
      z-index: 80;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
    }

    .micro-gift-layer {
      position: absolute;
      bottom: 270px;
      right: 20px;
      width: 100px;
      height: 300px;
      pointer-events: none;
      z-index: 70;
    }

    .chat-container {
      position: absolute;
      bottom: 285px;
      left: 12px;
      width: 250px;
      max-height: 150px;
      overflow-y: hidden;
      display: flex;
      flex-direction: column;
      justify-content: flex-end;
      z-index: 10;
      mask-image: linear-gradient(to bottom, transparent 0%, black 20%);
    }

    .chat-msg {
      background: rgba(0,0,0,0.6);
      padding: 5px 9px;
      border-radius: 12px;
      margin-top: 5px;
      font-size: 11px;
      backdrop-filter: blur(4px);
      animation: fadeIn 0.3s ease-out;
      border-left: 3px solid #009a44;
    }

    .chat-user {
      color: #fed100;
      font-weight: bold;
    }

    .live-action-bar {
      position: absolute;
      bottom: 242px;
      left: 10px;
      right: 10px;
      display: flex;
      align-items: center;
      gap: 7px;
      z-index: 85;
    }

    .chat-input-wrapper {
      flex: 1;
      display: flex;
      align-items: center;
      background: rgba(0, 0, 0, 0.65);
      border: 1px solid rgba(255, 255, 255, 0.22);
      border-radius: 20px;
      padding: 3px 4px 3px 10px;
      backdrop-filter: blur(8px);
    }

    #live-chat-input {
      flex: 1;
      background: transparent;
      border: none;
      outline: none;
      color: #fff;
      font-size: 11px;
    }

    #live-chat-input::placeholder {
      color: rgba(255, 255, 255, 0.55);
    }

    .chat-send-btn {
      background: #009a44;
      color: #fff;
      border: none;
      width: 26px;
      height: 26px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 11px;
      cursor: pointer;
      transition: transform 0.15s ease;
    }

    .chat-send-btn:active {
      transform: scale(0.9);
    }

    .live-heart-btn {
      background: rgba(0, 0, 0, 0.65);
      border: 1px solid rgba(239, 43, 45, 0.6);
      color: #fff;
      border-radius: 20px;
      padding: 4px 10px;
      font-size: 11px;
      font-weight: bold;
      cursor: pointer;
      display: flex;
      align-items: center;
      gap: 3px;
      backdrop-filter: blur(8px);
      transition: transform 0.15s ease;
    }

    .live-heart-btn:active {
      transform: scale(0.92);
    }

    .gift-panel {
      position: absolute;
      bottom: 0;
      left: 0;
      right: 0;
      height: 236px;
      background: rgba(14, 20, 16, 0.96);
      backdrop-filter: blur(16px);
      border-top-left-radius: 20px;
      border-top-right-radius: 20px;
      z-index: 90;
      padding: 10px 8px;
      display: flex;
      flex-direction: column;
      border-top: 2px solid #009a44;
      box-shadow: 0 -5px 25px rgba(0, 0, 0, 0.8);
    }

    .tab-bar {
      display: flex;
      gap: 6px;
      margin-bottom: 8px;
      overflow-x: auto;
      padding-bottom: 4px;
    }

    .tab-bar::-webkit-scrollbar {
      display: none;
    }

    .tab-btn {
      background: rgba(255, 255, 255, 0.08);
      border: 1px solid rgba(255, 215, 0, 0.2);
      color: #aaa;
      padding: 4px 10px;
      border-radius: 14px;
      font-size: 10.5px;
      font-weight: 600;
      cursor: pointer;
      white-space: nowrap;
      transition: all 0.2s;
    }

    .tab-btn.active {
      background: linear-gradient(135deg, #009a44, #007a36);
      color: #fff;
      border-color: #fed100;
      box-shadow: 0 0 10px rgba(0, 154, 68, 0.6);
    }

    .gift-grid {
      display: grid;
      grid-template-columns: repeat(4, 1fr);
      gap: 6px;
      overflow-y: auto;
      padding-right: 4px;
      height: 185px;
    }

    .gift-grid::-webkit-scrollbar {
      width: 4px;
    }

    .gift-grid::-webkit-scrollbar-thumb {
      background: rgba(254, 209, 0, 0.3);
      border-radius: 4px;
    }

    .gift-card {
      background: rgba(255, 255, 255, 0.04);
      border: 1px solid rgba(254, 209, 0, 0.12);
      border-radius: 10px;
      padding: 5px 3px;
      text-align: center;
      cursor: pointer;
      transition: transform 0.1s, background 0.2s;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
    }

    .gift-card:hover {
      border-color: rgba(254, 209, 0, 0.5);
    }

    .gift-card:active {
      transform: scale(0.92);
      background: rgba(0, 154, 68, 0.25);
    }

    .gift-icon {
      font-size: 21px;
      display: block;
      margin-bottom: 2px;
    }

    .gift-name {
      font-size: 9.5px;
      font-weight: 600;
      color: #eee;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
      width: 100%;
    }

    .gift-cost {
      font-size: 9.5px;
      color: #fed100;
      font-weight: bold;
    }

    .deposit-modal-backdrop {
      position: absolute;
      inset: 0;
      background: rgba(4, 8, 5, 0.72);
      backdrop-filter: blur(4px);
      z-index: 200;
      display: flex;
      align-items: flex-end;
      opacity: 0;
      pointer-events: none;
      transition: opacity 0.25s ease;
    }

    .deposit-modal-backdrop.open {
      opacity: 1;
      pointer-events: auto;
    }

    .deposit-sheet {
      width: 100%;
      max-height: 88%;
      background: linear-gradient(180deg, #131a15 0%, #0d1310 100%);
      border-top-left-radius: 22px;
      border-top-right-radius: 22px;
      border-top: 1px solid rgba(254, 209, 0, 0.35);
      box-shadow: 0 -10px 40px rgba(0,0,0,0.7);
      padding: 14px 14px 16px;
      transform: translateY(100%);
      transition: transform 0.32s cubic-bezier(0.16, 1, 0.3, 1);
      overflow-y: auto;
    }

    .deposit-modal-backdrop.open .deposit-sheet {
      transform: translateY(0);
    }

    .deposit-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 10px;
    }

    .deposit-title {
      font-size: 15px;
      font-weight: 800;
      letter-spacing: 0.3px;
    }

    .deposit-close {
      background: rgba(255,255,255,0.08);
      border: none;
      color: #ddd;
      width: 26px;
      height: 26px;
      border-radius: 50%;
      font-size: 14px;
      cursor: pointer;
      line-height: 1;
    }

    .deposit-balance-row {
      display: flex;
      align-items: center;
      justify-content: space-between;
      background: rgba(254, 209, 0, 0.08);
      border: 1px solid rgba(254, 209, 0, 0.25);
      border-radius: 12px;
      padding: 7px 11px;
      margin-bottom: 12px;
      font-size: 11.5px;
      color: #ffe9a8;
    }

    .section-label {
      font-size: 10.5px;
      color: #9db3a3;
      font-weight: 700;
      letter-spacing: 1px;
      text-transform: uppercase;
      margin: 8px 0 6px;
    }

    .package-grid {
      display: grid;
      grid-template-columns: repeat(2, 1fr);
      gap: 7px;
    }

    .package-card {
      position: relative;
      background: rgba(255,255,255,0.04);
      border: 1px solid rgba(254, 209, 0, 0.18);
      border-radius: 11px;
      padding: 8px 6px;
      text-align: center;
      cursor: pointer;
      transition: transform 0.12s ease, border-color 0.12s ease, background 0.12s ease;
    }

    .package-card:active {
      transform: scale(0.96);
    }

    .package-card.selected {
      border-color: #fed100;
      background: rgba(0, 154, 68, 0.18);
      box-shadow: 0 0 0 1px rgba(254,209,0,0.5) inset, 0 0 14px rgba(254,209,0,0.3);
    }

    .package-badge {
      position: absolute;
      top: -7px;
      right: 6px;
      background: linear-gradient(135deg, #ef2b2d, #c41f21);
      color: #fff;
      font-size: 8px;
      font-weight: 800;
      padding: 2px 5px;
      border-radius: 7px;
      letter-spacing: 0.3px;
    }

    .package-coins {
      font-size: 14px;
      font-weight: 800;
      color: #ffe9a8;
    }

    .package-price {
      font-size: 11px;
      color: #aaa;
      margin-top: 2px;
    }

    .package-bonus {
      font-size: 9px;
      color: #4fd67a;
      margin-top: 2px;
      font-weight: 700;
    }

    .method-row {
      display: flex;
      gap: 7px;
      overflow-x: auto;
      padding-bottom: 2px;
    }

    .method-row::-webkit-scrollbar { display: none; }

    .method-card {
      flex: 0 0 auto;
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 3px;
      background: rgba(255,255,255,0.04);
      border: 1px solid rgba(254, 209, 0, 0.18);
      border-radius: 11px;
      padding: 7px 10px;
      min-width: 64px;
      cursor: pointer;
      transition: border-color 0.12s ease, background 0.12s ease;
    }

    .method-card.selected {
      border-color: #fed100;
      background: rgba(0, 154, 68, 0.18);
    }

    .method-icon { font-size: 17px; }
    .method-name { font-size: 9px; color: #ccc; text-align: center; white-space: nowrap; }

    .pay-fields {
      margin-top: 10px;
      display: none;
    }

    .pay-fields.active {
      display: block;
    }

    .pay-input {
      width: 100%;
      background: rgba(255,255,255,0.06);
      border: 1px solid rgba(255,255,255,0.12);
      color: #fff;
      padding: 8px 9px;
      border-radius: 8px;
      font-size: 11.5px;
      margin-bottom: 7px;
      outline: none;
    }

    .pay-input:focus {
      border-color: #fed100;
    }

    .pay-row {
      display: flex;
      gap: 7px;
    }

    .pay-row .pay-input {
      flex: 1;
    }

    .phone-hint {
      font-size: 10px;
      color: #8fa898;
      margin: -2px 0 7px 2px;
    }

    .confirm-deposit-btn {
      width: 100%;
      margin-top: 12px;
      background: linear-gradient(135deg, #fed100, #e0b400);
      color: #0d1310;
      border: none;
      padding: 12px;
      border-radius: 12px;
      font-size: 13.5px;
      font-weight: 800;
      letter-spacing: 0.3px;
      cursor: pointer;
      box-shadow: 0 4px 16px rgba(254, 209, 0, 0.35);
      transition: transform 0.12s ease;
    }

    .confirm-deposit-btn:disabled {
      opacity: 0.5;
      cursor: not-allowed;
    }

    .confirm-deposit-btn:active:not(:disabled) {
      transform: scale(0.97);
    }

    .deposit-note {
      font-size: 9px;
      color: #6e8577;
      text-align: center;
      margin-top: 7px;
      line-height: 1.35;
    }

    .deposit-status {
      display: none;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 24px 8px 8px;
      text-align: center;
    }

    .deposit-status.active {
      display: flex;
    }

    .spinner-ring {
      width: 42px;
      height: 42px;
      border-radius: 50%;
      border: 3px solid rgba(254, 209, 0, 0.2);
      border-top-color: #fed100;
      animation: spin 0.8s linear infinite;
      margin-bottom: 12px;
    }

    @keyframes spin { to { transform: rotate(360deg); } }

    .success-check {
      width: 52px;
      height: 52px;
      border-radius: 50%;
      background: radial-gradient(circle, #1a3d26, #0d1310);
      border: 2px solid #4fd67a;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 24px;
      color: #4fd67a;
      margin-bottom: 10px;
      animation: successPop 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
    }

    @keyframes successPop {
      0% { transform: scale(0); opacity: 0; }
      100% { transform: scale(1); opacity: 1; }
    }

    .gold-foil {
      background: linear-gradient(180deg, #fff6d8 0%, #fed100 35%, #c9941f 60%, #fed100 85%, #fff6d8 100%);
      background-size: 100% 300%;
      -webkit-background-clip: text;
      background-clip: text;
      color: transparent;
      animation: foilShift 3s linear infinite;
    }

    .floating-particle {
      position: absolute;
      bottom: 0;
      right: 20px;
      font-size: 30px;
      filter: drop-shadow(0 0 6px rgba(254, 209, 0, 0.7));
      animation: floatUp 2s cubic-bezier(0.16, 1, 0.3, 1) forwards;
    }

    .particle-spark {
      position: absolute;
      width: 5px;
      height: 5px;
      border-radius: 50%;
      background: radial-gradient(circle, #fff6d8, #fed100 60%, transparent 70%);
      pointer-events: none;
      animation: sparkDrift 1.1s ease-out forwards;
    }

    .mid-tier-banner {
      position: relative;
      overflow: hidden;
      background: linear-gradient(135deg, #0f2b1c 0%, #163d26 45%, #0f2b1c 100%);
      color: #ffe9a8;
      padding: 12px 22px;
      border-radius: 16px;
      text-align: center;
      animation: plaqueEntrance 2.6s cubic-bezier(0.16, 1, 0.3, 1) forwards;
      border: 1px solid rgba(254, 209, 0, 0.55);
      box-shadow: 0 0 0 1px rgba(254,209,0,0.15) inset, 0 10px 30px rgba(0,0,0,0.55), 0 0 28px rgba(254, 209, 0, 0.35);
    }

    .mid-tier-banner::after {
      content: '';
      position: absolute;
      top: 0;
      left: -75%;
      width: 45%;
      height: 100%;
      background: linear-gradient(120deg, transparent, rgba(255, 240, 190, 0.55), transparent);
      transform: skewX(-20deg);
      animation: sheenSweep 1.4s ease-in-out 0.4s 2;
    }

    .mid-tier-name {
      font-weight: 700;
      font-size: 14px;
      letter-spacing: 0.4px;
    }

    .mid-tier-value {
      font-size: 10px;
      color: rgba(255, 233, 168, 0.75);
      letter-spacing: 1px;
      margin-top: 2px;
    }

    .high-tier-overlay {
      width: 100%;
      height: 100%;
      position: relative;
      background: radial-gradient(circle at 50% 42%, rgba(22, 61, 38, 0.55), rgba(6, 11, 8, 0.94) 70%);
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      animation: fullscreenFade 3.4s cubic-bezier(0.4, 0, 0.2, 1) forwards;
      overflow: hidden;
    }

    .ray-burst {
      position: absolute;
      top: 50%;
      left: 50%;
      width: 440px;
      height: 440px;
      margin: -220px 0 0 -220px;
      background: repeating-conic-gradient(from 0deg, rgba(254, 209, 0, 0.16) 0deg 6deg, transparent 6deg 18deg);
      animation: rayRotate 6s linear infinite;
      opacity: 0.8;
    }

    .high-tier-name {
      font-size: 21px;
      font-weight: 800;
      letter-spacing: 0.5px;
      margin-top: 10px;
      text-align: center;
      text-shadow: 0 2px 18px rgba(0, 0, 0, 0.6);
    }

    .high-tier-sub {
      color: rgba(255, 255, 255, 0.75);
      font-size: 11px;
      margin-top: 5px;
      letter-spacing: 1.5px;
    }

    .legendary-takeover {
      width: 100%;
      height: 100%;
      position: relative;
      background: radial-gradient(circle at 50% 45%, rgba(254,209,0,0.22) 0%, rgba(9, 15, 10, 0.96) 78%);
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      animation: legendaryFade 5s cubic-bezier(0.4, 0, 0.2, 1) forwards;
      overflow: hidden;
    }

    .legendary-takeover::before {
      content: '';
      position: absolute;
      inset: 0;
      border: 3px solid transparent;
      background: linear-gradient(#0000, #0000) padding-box,
                  linear-gradient(120deg, #fed100, #fff6d8, #c9941f, #fed100) border-box;
      background-size: 100% 100%, 300% 300%;
      animation: borderShift 3s ease infinite;
      pointer-events: none;
    }

    .legendary-aura {
      position: absolute;
      top: 50%;
      left: 50%;
      width: 320px;
      height: 320px;
      margin: -160px 0 0 -160px;
      border-radius: 50%;
      border: 1px solid rgba(254, 209, 0, 0.4);
      animation: auraExpand 2.4s cubic-bezier(0.16, 1, 0.3, 1) infinite;
    }

    .legendary-aura.delay {
      animation-delay: 0.8s;
    }

    .legendary-eyebrow {
      color: #ffe27a;
      font-size: 11px;
      font-weight: 700;
      letter-spacing: 3px;
      opacity: 0;
      animation: labelReveal 0.6s ease-out 0.5s forwards;
    }

    .legendary-name {
      font-size: 25px;
      font-weight: 800;
      text-align: center;
      padding: 0 12px;
      margin-top: 6px;
      letter-spacing: 0.3px;
      text-shadow: 0 4px 26px rgba(0,0,0,0.7);
    }

    .legendary-value {
      font-size: 16px;
      font-weight: 700;
      color: #ffe27a;
      margin-top: 8px;
      letter-spacing: 1px;
    }

    .confetti-piece {
      position: absolute;
      top: -20px;
      width: 6px;
      height: 11px;
      opacity: 0.9;
      animation: confettiFall linear forwards;
    }

    .hero-graphic {
      font-size: 85px;
      filter: drop-shadow(0 0 25px #fed100);
      animation: heroBounce 2.2s infinite alternate cubic-bezier(0.45, 0, 0.55, 1);
      position: relative;
      z-index: 2;
    }

    .combo-badge {
      position: absolute;
      bottom: 260px;
      right: 14px;
      font-size: 28px;
      font-weight: 900;
      font-style: italic;
      letter-spacing: -0.5px;
      filter: drop-shadow(0 0 8px rgba(239, 43, 45, 0.6)) drop-shadow(0 0 16px rgba(0, 154, 68, 0.5));
      animation: comboPulse 0.32s cubic-bezier(0.34, 1.56, 0.64, 1);
    }

    @keyframes floatUp {
      0% { transform: translateY(0) scale(0.5) rotate(0deg); opacity: 1; }
      55% { transform: translateY(-135px) scale(1.25) rotate(-12deg); opacity: 1; }
      100% { transform: translateY(-260px) scale(1.4) rotate(10deg); opacity: 0; }
    }

    @keyframes sparkDrift {
      0% { transform: translate(0, 0) scale(1); opacity: 1; }
      100% { transform: translate(var(--dx, 20px), var(--dy, -60px)) scale(0); opacity: 0; }
    }

    @keyframes badgeSheen {
      0% { left: -60%; }
      35%, 100% { left: 130%; }
    }

    @keyframes coinPulse {
      0% { transform: scale(1); }
      40% { transform: scale(1.08); }
      100% { transform: scale(1); }
    }

    @keyframes plaqueEntrance {
      0% { transform: scale(0.4) translateY(10px); opacity: 0; }
      12% { transform: scale(1.04) translateY(0); opacity: 1; }
      20% { transform: scale(1) translateY(0); }
      88% { transform: scale(1) translateY(0); opacity: 1; }
      100% { transform: scale(0.92) translateY(-6px); opacity: 0; }
    }

    @keyframes sheenSweep {
      0% { left: -75%; }
      100% { left: 130%; }
    }

    @keyframes fullscreenFade {
      0% { opacity: 0; }
      8% { opacity: 1; }
      92% { opacity: 1; }
      100% { opacity: 0; }
    }

    @keyframes rayRotate {
      0% { transform: rotate(0deg); }
      100% { transform: rotate(360deg); }
    }

    @keyframes legendaryFade {
      0% { opacity: 0; transform: scale(0.85); }
      8% { opacity: 1; transform: scale(1); }
      92% { opacity: 1; transform: scale(1); }
      100% { opacity: 0; transform: scale(1.08); }
    }

    @keyframes borderShift {
      0% { background-position: 0% 0%, 0% 50%; }
      50% { background-position: 0% 0%, 100% 50%; }
      100% { background-position: 0% 0%, 0% 50%; }
    }

    @keyframes auraExpand {
      0% { transform: scale(0.4); opacity: 0.9; }
      100% { transform: scale(1.35); opacity: 0; }
    }

    @keyframes labelReveal {
      0% { opacity: 0; transform: translateY(6px); }
      100% { opacity: 1; transform: translateY(0); }
    }

    @keyframes confettiFall {
      0% { transform: translateY(0) rotate(0deg); opacity: 1; }
      100% { transform: translateY(760px) rotate(540deg); opacity: 0.2; }
    }

    @keyframes foilShift {
      0% { background-position: 0 0; }
      100% { background-position: 0 300%; }
    }

    @keyframes heroBounce {
      0% { transform: translateY(-12px) scale(0.96); }
      100% { transform: translateY(12px) scale(1.07); }
    }

    @keyframes comboPulse {
      0% { transform: scale(1.6) rotate(-4deg); }
      100% { transform: scale(1) rotate(0deg); }
    }

    @keyframes fadeIn {
      from { opacity: 0; transform: translateY(10px); }
      to { opacity: 1; transform: translateY(0); }
    }

    .shake {
      animation: screenShake 0.5s cubic-bezier(0.36, 0.07, 0.19, 0.97);
    }

    @keyframes screenShake {
      0% { transform: translate(0, 0); }
      20% { transform: translate(-4px, 3px); }
      40% { transform: translate(4px, -3px); }
      60% { transform: translate(-2.5px, 2px); }
      80% { transform: translate(2px, -1.5px); }
      100% { transform: translate(0, 0); }
    }
  </style>
</head>
<body>

  <div class="phone-screen" id="screen">
    <div class="live-stream-bg"></div>

    <div class="top-bar">
      <div class="streamer-info">
        <div class="avatar">ETH</div>
        <div>
          <div style="display: flex; align-items: center; gap: 5px;">
            <span class="live-pulse-badge">● LIVE</span>
            <span style="font-size: 11px; font-weight: bold; color: #fff;">$escapedStreamerName</span>
          </div>
          <div style="font-size: 9.5px; color: #ffd966; max-width: 130px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">$escapedStreamTitle</div>
          <div style="display: flex; align-items: center; gap: 5px; font-size: 9px; color: #ccc;">
            <span id="viewer-count-text">$viewerCount</span>
            <span>•</span>
            <span id="likes-count-text">$likesCount ❤️</span>
          </div>
        </div>
      </div>
      <div class="wallet-group">
        <div class="coin-badge"><span id="coin-balance">$initialCoins</span> 🪙</div>
        <button class="deposit-btn" onclick="openDepositModal()">+ Top Up</button>
        ${if (isHost) """<button class="end-broadcast-btn" onclick="handleNativeEndLive()">End</button>""" else """<button class="close-live-btn" onclick="handleNativeClose()">✕</button>"""}
      </div>
    </div>

    <div class="fullscreen-overlay" id="fullscreen-overlay"></div>
    <div class="micro-gift-layer" id="micro-layer"></div>
    <div id="combo-container"></div>

    <div class="chat-container" id="chat-box">
      <div class="chat-msg"><span class="chat-user">System:</span> Welcome to Meskot live streaming!</div>
      <div class="chat-msg"><span class="chat-user">System:</span> Gifting is enabled for all viewers.</div>
    </div>

    <div class="live-action-bar">
      <div class="chat-input-wrapper">
        <input type="text" id="live-chat-input" placeholder="Say something in Live..." maxlength="120" onkeypress="if(event.key==='Enter') submitLiveChat()">
        <button class="chat-send-btn" onclick="submitLiveChat()">➤</button>
      </div>
      <button class="live-heart-btn" onclick="triggerLiveLike()">❤️ <span id="likes-count">$likesCount</span></button>
    </div>

    <div class="gift-panel">
      <div class="tab-bar">
        <button class="tab-btn active" onclick="filterCategory('all', this)">All (100)</button>
        <button class="tab-btn" onclick="filterCategory('micro', this)">Micro (1-99)</button>
        <button class="tab-btn" onclick="filterCategory('mid', this)">Mid (100-999)</button>
        <button class="tab-btn" onclick="filterCategory('high', this)">High (1k-9.9k)</button>
        <button class="tab-btn" onclick="filterCategory('legendary', this)">Legendary (10k+)</button>
      </div>
      <div class="gift-grid" id="gift-grid"></div>
    </div>

    <div class="deposit-modal-backdrop" id="deposit-backdrop" onclick="closeDepositOnBackdrop(event)">
      <div class="deposit-sheet">
        <div id="deposit-form-view">
          <div class="deposit-header">
            <div class="deposit-title">Top Up Coins</div>
            <button class="deposit-close" onclick="closeDepositModal()">✕</button>
          </div>

          <div class="deposit-balance-row">
            <span>Current Balance</span>
            <span id="deposit-current-balance">$initialCoins 🪙</span>
          </div>

          <div class="section-label">Choose a package</div>
          <div class="package-grid" id="package-grid"></div>

          <div class="section-label">Payment method</div>
          <div class="method-row" id="method-row"></div>

          <div class="pay-fields" id="fields-card">
            <input class="pay-input" type="text" placeholder="Card number" maxlength="19" oninput="formatCardNumber(this)">
            <div class="pay-row">
              <input class="pay-input" type="text" placeholder="MM/YY" maxlength="5" oninput="formatExpiry(this)">
              <input class="pay-input" type="text" placeholder="CVC" maxlength="4">
            </div>
            <input class="pay-input" type="text" placeholder="Name on card">
          </div>

          <div class="pay-fields" id="fields-chapa">
            <div class="phone-hint">Chapa checkout supports Telebirr, CBE Birr, M-Pesa, e-Birr and cards directly.</div>
            <div id="chapa-inline-form"></div>
          </div>

          <div class="pay-fields" id="fields-paypal">
            <div class="phone-hint" style="margin-top:6px;">Instant deposit via PayPal funds.</div>
          </div>

          <div class="pay-fields" id="fields-bank">
            <input class="pay-input" type="text" placeholder="Account holder name">
            <input class="pay-input" type="text" placeholder="Account / IBAN number">
          </div>

          <button class="confirm-deposit-btn" id="confirm-deposit-btn" onclick="submitDeposit()" disabled>Select a package</button>
          <div class="deposit-note">Real Chapa & Telebirr payments supported. Demo sandbox mode provides instant test balance.</div>
        </div>

        <div class="deposit-status" id="deposit-processing">
          <div class="spinner-ring"></div>
          <div style="font-size:13px; color:#eee; font-weight:600;">Processing payment…</div>
          <div style="font-size:11px; color:#8fa898; margin-top:4px;">Please don't close this window</div>
        </div>

        <div class="deposit-status" id="deposit-success">
          <div class="success-check">✓</div>
          <div style="font-size:15px; font-weight:800; color:#fff;">Deposit successful</div>
          <div id="deposit-success-amount" style="font-size:12px; color:#ffe27a; margin-top:4px; font-weight:700;"></div>
          <button class="confirm-deposit-btn" style="margin-top:16px;" onclick="closeDepositModal()">Done</button>
        </div>
      </div>
    </div>
  </div>

  <script>
    const GIFTS_CATALOG = [
      // TIER 1: MICRO GIFTS (1 - 99 Coins)
      { id: 1, name: "Jebena Coffee Pot", icon: "☕", coins: 1, tier: "micro" },
      { id: 2, name: "Injera Roll", icon: "🫓", coins: 1, tier: "micro" },
      { id: 3, name: "Berbere Spice Box", icon: "🌶️", coins: 2, tier: "micro" },
      { id: 4, name: "Censer Incense", icon: "💨", coins: 5, tier: "micro" },
      { id: 5, name: "Finiq Coffee Cup", icon: "☕", coins: 5, tier: "micro" },
      { id: 6, name: "Dabo Bread", icon: "🍞", coins: 10, tier: "micro" },
      { id: 7, name: "Shiro Wot Bowl", icon: "🍲", coins: 10, tier: "micro" },
      { id: 8, name: "Talla Horn Cup", icon: "🍺", coins: 10, tier: "micro" },
      { id: 9, name: "Tere Siga Cube", icon: "🥩", coins: 15, tier: "micro" },
      { id: 10, name: "Tej Horn Cup", icon: "📯", coins: 15, tier: "micro" },
      { id: 11, name: "Netela Scarf", icon: "🧣", coins: 20, tier: "micro" },
      { id: 12, name: "Gabi Blanket", icon: "🧣", coins: 20, tier: "micro" },
      { id: 13, name: "Adea Flower", icon: "🌼", coins: 25, tier: "micro" },
      { id: 14, name: "Mini Kebero Drum", icon: "🥁", coins: 25, tier: "micro" },
      { id: 15, name: "Krar String", icon: "🎸", coins: 30, tier: "micro" },
      { id: 16, name: "Masinko Bow", icon: "🎻", coins: 30, tier: "micro" },
      { id: 17, name: "Mini Mesob Basket", icon: "🧺", coins: 35, tier: "micro" },
      { id: 18, name: "Tef Grain Sheaf", icon: "🌾", coins: 35, tier: "micro" },
      { id: 19, name: "Traditional Paper Kite", icon: "🪁", coins: 40, tier: "micro" },
      { id: 20, name: "Roasted Coffee Bean", icon: "🫘", coins: 40, tier: "micro" },
      { id: 21, name: "Koba Leaf Plate", icon: "🍃", coins: 50, tier: "micro" },
      { id: 22, name: "Eucalyptus Sprig", icon: "🌿", coins: 50, tier: "micro" },
      { id: 23, name: "Arefe Foam Cup", icon: "🍺", coins: 55, tier: "micro" },
      { id: 24, name: "Horn Spoon", icon: "🥄", coins: 60, tier: "micro" },
      { id: 25, name: "Siga Wot Stew", icon: "🥘", coins: 65, tier: "micro" },
      { id: 26, name: "Doro Wot Egg", icon: "🥚", coins: 70, tier: "micro" },
      { id: 27, name: "Tere Siga Cleaver", icon: "🔪", coins: 75, tier: "micro" },
      { id: 28, name: "Gomen Stew Dish", icon: "🥬", coins: 80, tier: "micro" },
      { id: 29, name: "Buna Flower Blossom", icon: "🌸", coins: 90, tier: "micro" },
      { id: 30, name: "Meskel Cross Pendant", icon: "✝️", coins: 99, tier: "micro" },

      // TIER 2: MID-TIER GIFTS (100 - 999 Coins)
      { id: 31, name: "Full Mesob Dining Table", icon: "🧺", coins: 100, tier: "mid" },
      { id: 32, name: "Traditional Kebero Drum", icon: "🥁", coins: 120, tier: "mid" },
      { id: 33, name: "Folk Krar Instrument", icon: "🎸", coins: 150, tier: "mid" },
      { id: 34, name: "Master Masinko", icon: "🎻", coins: 180, tier: "mid" },
      { id: 35, name: "Gold Lalibela Cross", icon: "✝️", coins: 200, tier: "mid" },
      { id: 36, name: "Habesha Dress Kemis", icon: "👗", coins: 250, tier: "mid" },
      { id: 37, name: "Tilf Embroidered Vest", icon: "🎽", coins: 300, tier: "mid" },
      { id: 38, name: "Jebena Buna Ceremony", icon: "☕", coins: 350, tier: "mid" },
      { id: 39, name: "Mountain Nyala Antelope", icon: "🦌", coins: 400, tier: "mid" },
      { id: 40, name: "Walia Ibex", icon: "🐐", coins: 450, tier: "mid" },
      { id: 41, name: "Ethiopian Wolf", icon: "🦊", coins: 500, tier: "mid" },
      { id: 42, name: "Gelada Baboon", icon: "🐒", coins: 550, tier: "mid" },
      { id: 43, name: "Gondar Fasil Ghebby", icon: "🏰", coins: 600, tier: "mid" },
      { id: 44, name: "Axum Obelisk Pillar", icon: "🗿", coins: 650, tier: "mid" },
      { id: 45, name: "Lalibela Rock Shrine", icon: "🏛️", coins: 700, tier: "mid" },
      { id: 46, name: "Blue Nile Falls Cascade", icon: "🌊", coins: 750, tier: "mid" },
      { id: 47, name: "Erta Ale Lava Torch", icon: "🌋", coins: 800, tier: "mid" },
      { id: 48, name: "Simien Mountains Peak", icon: "⛰️", coins: 850, tier: "mid" },
      { id: 49, name: "Great Rift Valley Gem", icon: "💎", coins: 900, tier: "mid" },
      { id: 50, name: "Bale Mountains Flora", icon: "🌸", coins: 950, tier: "mid" },
      { id: 51, name: "Harar Jugol Gate", icon: "🚪", coins: 220, tier: "mid" },
      { id: 52, name: "Guji Coffee Sack", icon: "☕", coins: 280, tier: "mid" },
      { id: 53, name: "Yirgacheffe Roast", icon: "☕", coins: 320, tier: "mid" },
      { id: 54, name: "Sidama Gold Roast", icon: "☕", coins: 420, tier: "mid" },
      { id: 55, name: "Queen Sheba Ring", icon: "💍", coins: 520, tier: "mid" },
      { id: 56, name: "Emperor Menelik Sword", icon: "🗡️", coins: 620, tier: "mid" },
      { id: 57, name: "Atse Tewodros Cannon", icon: "💥", coins: 720, tier: "mid" },
      { id: 58, name: "Janoy Regal Cloak", icon: "🧥", coins: 820, tier: "mid" },
      { id: 59, name: "Adwa Victory Shield", icon: "🛡️", coins: 920, tier: "mid" },
      { id: 60, name: "Abyssinian Lion Crown", icon: "🦁", coins: 999, tier: "mid" },

      // TIER 3: HIGH-TIER GIFTS (1,000 - 9,999 Coins)
      { id: 61, name: "Royal Lion of Judah", icon: "🦁", coins: 1000, tier: "high" },
      { id: 62, name: "Church of Saint George", icon: "⛪", coins: 1200, tier: "high" },
      { id: 63, name: "Fasilides Castle Complex", icon: "🏰", coins: 1500, tier: "high" },
      { id: 64, name: "Golden Axum Stela", icon: "🏛️", coins: 2000, tier: "high" },
      { id: 65, name: "Harar Hyena Guardian", icon: "🐺", coins: 2500, tier: "high" },
      { id: 66, name: "Danakil Sulfur Springs", icon: "🌋", coins: 3000, tier: "high" },
      { id: 67, name: "Sof Omar Cave Shrine", icon: "🌌", coins: 3500, tier: "high" },
      { id: 68, name: "Lake Tana Papyrus Boat", icon: "⛵", coins: 4000, tier: "high" },
      { id: 69, name: "Great Ethiopian Run Gold", icon: "🏅", coins: 4500, tier: "high" },
      { id: 70, name: "Lucy Skeleton Legacy", icon: "🦴", coins: 5000, tier: "high" },
      { id: 71, name: "Ark Sanctuary Shrine", icon: "📦", coins: 5500, tier: "high" },
      { id: 72, name: "Erta Ale Fire Shield", icon: "🔥", coins: 6000, tier: "high" },
      { id: 73, name: "Bale Nyala Spirit", icon: "🦌", coins: 6500, tier: "high" },
      { id: 74, name: "Blue Nile Thunder", icon: "⚡", coins: 7000, tier: "high" },
      { id: 75, name: "Eskista Dance Master", icon: "💃", coins: 7500, tier: "high" },
      { id: 76, name: "Lalibela Cross Matrix", icon: "✝️", coins: 8000, tier: "high" },
      { id: 77, name: "Harari Cultural Mansion", icon: "🏠", coins: 8500, tier: "high" },
      { id: 78, name: "Omo Valley Tribal Crown", icon: "👑", coins: 9000, tier: "high" },
      { id: 79, name: "Ras Dashen Summit Flag", icon: "🚩", coins: 9500, tier: "high" },
      { id: 80, name: "Tiya Megalith Monument", icon: "🗿", coins: 9999, tier: "high" },
      { id: 81, name: "Abyssinian Warrior", icon: "🛡️", coins: 1800, tier: "high" },
      { id: 82, name: "Golden Jebena Tower", icon: "☕", coins: 2800, tier: "high" },
      { id: 83, name: "Axum Gold Coins Hoard", icon: "💰", coins: 3800, tier: "high" },
      { id: 84, name: "Coffee Origin Tree", icon: "🌳", coins: 4800, tier: "high" },
      { id: 85, name: "Enkutatash Golden Sun", icon: "☀️", coins: 5800, tier: "high" },

      // TIER 4: LEGENDARY GIFTS (10,000+ Coins)
      { id: 86, name: "Golden Empire of Axum", icon: "🏛️", coins: 10000, tier: "legendary" },
      { id: 87, name: "Monolithic Lalibela Realm", icon: "⛪", coins: 12000, tier: "legendary" },
      { id: 88, name: "Grand Fasil Ghebby Palace", icon: "🏰", coins: 15000, tier: "legendary" },
      { id: 89, name: "Danakil Lava Kingdom", icon: "🌋", coins: 18000, tier: "legendary" },
      { id: 90, name: "Sheba Gold Vault", icon: "🏺", coins: 20000, tier: "legendary" },
      { id: 91, name: "Ark of Covenant Sanctuary", icon: "⛩️", coins: 25000, tier: "legendary" },
      { id: 92, name: "Great Rift Valley Horizon", icon: "🌅", coins: 30000, tier: "legendary" },
      { id: 93, name: "Eternal Nile River Flow", icon: "🌊", coins: 35000, tier: "legendary" },
      { id: 94, name: "Simien Roof of Africa", icon: "🏔️", coins: 40000, tier: "legendary" },
      { id: 95, name: "Demera Meskel Bonfire", icon: "🔥", coins: 45000, tier: "legendary" },
      { id: 96, name: "Enkutatash Sunshine", icon: "🌻", coins: 50000, tier: "legendary" },
      { id: 97, name: "Throne of Lion of Judah", icon: "🦁", coins: 60000, tier: "legendary" },
      { id: 98, name: "Solomonic Royal Crown", icon: "👑", coins: 75000, tier: "legendary" },
      { id: 99, name: "Highlands Coffee Genesis", icon: "☕", coins: 85000, tier: "legendary" },
      { id: 100, name: "Infinite Abyssinian Realm", icon: "🪐", coins: 100000, tier: "legendary" }
    ];

    let userCoins = $initialCoins;
    const currentUserName = '$escapedUserName';
    const giftQueue = [];
    let isQueueProcessing = false;
    let comboCount = 0;
    let comboTimer = null;

    const coinBalanceEl = document.getElementById('coin-balance');
    const giftGridEl = document.getElementById('gift-grid');
    const microLayer = document.getElementById('micro-layer');
    const overlayLayer = document.getElementById('fullscreen-overlay');
    const comboContainer = document.getElementById('combo-container');
    const chatBox = document.getElementById('chat-box');
    const screen = document.getElementById('screen');

    function handleNativeClose() {
      if (window.AndroidBridge && window.AndroidBridge.closeLive) {
        window.AndroidBridge.closeLive();
      }
    }

    function renderGifts(category = 'all') {
      giftGridEl.innerHTML = '';
      const filtered = category === 'all' 
        ? GIFTS_CATALOG 
        : GIFTS_CATALOG.filter(g => g.tier === category);

      filtered.forEach(gift => {
        const card = document.createElement('div');
        card.className = 'gift-card';
        card.onclick = () => sendGift(gift.id);
        card.innerHTML = `
          <span class="gift-icon">${'$'}{gift.icon}</span>
          <div class="gift-name">${'$'}{gift.name}</div>
          <div class="gift-cost">${'$'}{gift.coins.toLocaleString()} 🪙</div>
        `;
        giftGridEl.appendChild(card);
      });
    }

    function filterCategory(category, btnEl) {
      document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
      btnEl.classList.add('active');
      renderGifts(category);
    }

    function sendGift(giftId) {
      const gift = GIFTS_CATALOG.find(g => g.id === giftId);
      if (!gift) return;

      if (userCoins < gift.coins) {
        addChatMessage('System', 'Insufficient 🪙 balance! Tap + Top Up', '#ef2b2d');
        openDepositModal();
        return;
      }

      const fromValue = userCoins;
      userCoins -= gift.coins;
      animateCoinBalance(fromValue, userCoins);

      if (gift.tier === 'micro') {
        handleMicroGift(gift);
      } else {
        giftQueue.push(gift);
        if (!isQueueProcessing) {
          processQueue();
        }
      }

      addChatMessage(currentUserName, `sent ${'$'}{gift.name} (${'$'}{gift.icon})!`, '#fed100');

      if (window.AndroidBridge && window.AndroidBridge.sendGift) {
        window.AndroidBridge.sendGift(gift.id, gift.name, gift.icon, gift.coins);
      }
    }

    function animateCoinBalance(from, to) {
      const duration = 500;
      const start = performance.now();
      function step(now) {
        const t = Math.min(1, (now - start) / duration);
        const eased = 1 - Math.pow(1 - t, 3);
        const value = Math.round(from + (to - from) * eased);
        coinBalanceEl.innerText = value.toLocaleString();
        if (t < 1) requestAnimationFrame(step);
      }
      requestAnimationFrame(step);
      const badge = document.querySelector('.coin-badge');
      badge.classList.remove('coin-pulse');
      void badge.offsetWidth;
      badge.classList.add('coin-pulse');
    }

    function handleMicroGift(gift) {
      const particle = document.createElement('div');
      particle.className = 'floating-particle';
      particle.innerText = gift.icon;
      const rightPos = 10 + Math.random() * 40;
      particle.style.right = rightPos + 'px';
      microLayer.appendChild(particle);

      for (let i = 0; i < 4; i++) {
        const spark = document.createElement('div');
        spark.className = 'particle-spark';
        spark.style.right = (rightPos + (Math.random() * 16 - 8)) + 'px';
        spark.style.bottom = (Math.random() * 30) + 'px';
        spark.style.setProperty('--dx', (Math.random() * 40 - 20) + 'px');
        spark.style.setProperty('--dy', -(60 + Math.random() * 80) + 'px');
        spark.style.animationDelay = (i * 60) + 'ms';
        microLayer.appendChild(spark);
        setTimeout(() => spark.remove(), 1300);
      }

      setTimeout(() => particle.remove(), 2000);

      comboCount++;
      comboContainer.innerHTML = `<div class="combo-badge gold-foil">x${'$'}{comboCount}</div>`;

      clearTimeout(comboTimer);
      comboTimer = setTimeout(() => {
        comboCount = 0;
        comboContainer.innerHTML = '';
      }, 1200);
    }

    async function processQueue() {
      if (giftQueue.length === 0) {
        isQueueProcessing = false;
        return;
      }

      isQueueProcessing = true;
      const currentGift = giftQueue.shift();

      if (currentGift.tier === 'mid') {
        await renderMidTierAnimation(currentGift);
      } else if (currentGift.tier === 'high') {
        await renderHighTierAnimation(currentGift);
      } else if (currentGift.tier === 'legendary') {
        await renderLegendaryAnimation(currentGift);
      }

      processQueue();
    }

    function renderMidTierAnimation(gift) {
      return new Promise((resolve) => {
        screen.classList.add('shake');
        setTimeout(() => screen.classList.remove('shake'), 500);

        const banner = document.createElement('div');
        banner.className = 'mid-tier-banner';
        banner.innerHTML = `
          <div style="font-size: 34px; filter: drop-shadow(0 0 8px rgba(254,209,0,0.5));">${'$'}{gift.icon}</div>
          <div class="mid-tier-name gold-foil">${'$'}{gift.name.toUpperCase()}</div>
          <div class="mid-tier-value">${'$'}{gift.coins.toLocaleString()} 🪙</div>
        `;

        overlayLayer.appendChild(banner);

        setTimeout(() => {
          banner.remove();
          resolve();
        }, 2600);
      });
    }

    function renderHighTierAnimation(gift) {
      return new Promise((resolve) => {
        const overlay = document.createElement('div');
        overlay.className = 'high-tier-overlay';
        overlay.innerHTML = `
          <div class="ray-burst"></div>
          <div class="hero-graphic">${'$'}{gift.icon}</div>
          <h1 class="high-tier-name gold-foil">${'$'}{gift.name.toUpperCase()}</h1>
          <p class="high-tier-sub">HABESHA SPECIAL CULTURAL GIFT</p>
        `;

        overlayLayer.appendChild(overlay);

        setTimeout(() => {
          overlay.remove();
          resolve();
        }, 3400);
      });
    }

    function renderLegendaryAnimation(gift) {
      return new Promise((resolve) => {
        screen.classList.add('shake');
        setTimeout(() => screen.classList.remove('shake'), 900);

        const takeover = document.createElement('div');
        takeover.className = 'legendary-takeover';
        takeover.innerHTML = `
          <div class="legendary-aura"></div>
          <div class="legendary-aura delay"></div>
          <div class="legendary-eyebrow">★ IMPERIAL LEGENDARY GIFT ★</div>
          <div class="hero-graphic" style="font-size: 100px;">${'$'}{gift.icon}</div>
          <h1 class="legendary-name gold-foil">${'$'}{gift.name.toUpperCase()}</h1>
          <p class="legendary-value">${'$'}{gift.coins.toLocaleString()} 🪙</p>
        `;

        overlayLayer.appendChild(takeover);

        const confettiColors = ['#fed100', '#ffe9a8', '#009a44', '#ef2b2d', '#fff6d8'];
        for (let i = 0; i < 26; i++) {
          const piece = document.createElement('div');
          piece.className = 'confetti-piece';
          piece.style.left = Math.random() * 100 + '%';
          piece.style.background = confettiColors[Math.floor(Math.random() * confettiColors.length)];
          piece.style.animationDuration = (2.4 + Math.random() * 1.8) + 's';
          piece.style.animationDelay = (Math.random() * 1.2) + 's';
          piece.style.borderRadius = Math.random() > 0.5 ? '50%' : '2px';
          takeover.appendChild(piece);
        }

        setTimeout(() => {
          takeover.remove();
          resolve();
        }, 5000);
      });
    }

    function addChatMessage(user, text, color = '#fed100') {
      const msg = document.createElement('div');
      msg.className = 'chat-msg';
      msg.innerHTML = `<span class="chat-user" style="color: ${'$'}{color}">${'$'}{user}:</span> ${'$'}{text}`;
      chatBox.appendChild(msg);
      chatBox.scrollTop = chatBox.scrollHeight;
    }

    function submitLiveChat() {
      const input = document.getElementById('live-chat-input');
      if (!input) return;
      const text = input.value.trim();
      if (!text) return;
      if (window.AndroidBridge && window.AndroidBridge.sendComment) {
        window.AndroidBridge.sendComment(text);
      }
      input.value = '';
    }

    function triggerLiveLike() {
      createMicroParticle('❤️');
      const countEl = document.getElementById('likes-count');
      if (countEl) {
        const cur = parseInt(countEl.innerText) || 0;
        countEl.innerText = cur + 1;
      }
      const topLikes = document.getElementById('likes-count-text');
      if (topLikes) {
        const cur = parseInt(topLikes.innerText) || 0;
        topLikes.innerText = (cur + 1) + ' ❤️';
      }
      if (window.AndroidBridge && window.AndroidBridge.sendLike) {
        window.AndroidBridge.sendLike();
      }
    }

    function handleNativeEndLive() {
      if (window.AndroidBridge && window.AndroidBridge.endLive) {
        window.AndroidBridge.endLive();
      }
    }

    window.receiveRemoteMessage = function(user, text, color, type, giftIcon) {
      addChatMessage(user, text, color);
      if (type === 'GIFT' && giftIcon) {
        createMicroParticle(giftIcon);
      } else if (type === 'LIKE') {
        createMicroParticle('❤️');
      }
    };

    window.updateStreamStats = function(viewerStr, likesCount, totalCoins) {
      const vEl = document.getElementById('viewer-count-text');
      if (vEl) vEl.innerText = viewerStr;
      const lEl = document.getElementById('likes-count-text');
      if (lEl) lEl.innerText = likesCount + ' ❤️';
      const cEl = document.getElementById('likes-count');
      if (cEl) cEl.innerText = likesCount;
    };

    renderGifts('all');

    const COIN_PACKAGES = [
      { id: 'p1', coins: 1000, price: '130 ETB', amountETB: 130 },
      { id: 'p2', coins: 5000, price: '580 ETB', bonus: '+5% bonus', amountETB: 580 },
      { id: 'p3', coins: 12000, price: '1,290 ETB', bonus: '+10% bonus', amountETB: 1290 },
      { id: 'p4', coins: 30000, price: '2,970 ETB', bonus: '+15% bonus', tag: 'POPULAR', amountETB: 2970 },
      { id: 'p5', coins: 65000, price: '5,810 ETB', bonus: '+20% bonus', amountETB: 5810 },
      { id: 'p6', coins: 150000, price: '11,620 ETB', bonus: '+25% bonus', tag: 'BEST VALUE', amountETB: 11620 }
    ];

    const CHAPA_PUBLIC_KEY = 'CHAPUBK_TEST-1PW1FKvNMh2tx4k5hHPibEZA4A6GPpRc';

    const PAYMENT_METHODS = [
      { id: 'chapa', name: 'Telebirr/CBE', icon: '📱', fields: 'fields-chapa' },
      { id: 'card', name: 'Card', icon: '💳', fields: 'fields-card' },
      { id: 'paypal', name: 'PayPal', icon: '🅿️', fields: 'fields-paypal' },
      { id: 'bank', name: 'Bank Transfer', icon: '🏛️', fields: 'fields-bank' }
    ];

    let selectedPackage = null;
    let selectedMethod = null;

    function renderPackages() {
      const grid = document.getElementById('package-grid');
      grid.innerHTML = '';
      COIN_PACKAGES.forEach(pkg => {
        const card = document.createElement('div');
        card.className = 'package-card';
        card.dataset.id = pkg.id;
        card.onclick = () => selectPackage(pkg.id);
        card.innerHTML = `
          ${'$'}{pkg.tag ? `<div class="package-badge">${'$'}{pkg.tag}</div>` : ''}
          <div class="package-coins">${'$'}{pkg.coins.toLocaleString()} 🪙</div>
          <div class="package-price">${'$'}{pkg.price}</div>
          ${'$'}{pkg.bonus ? `<div class="package-bonus">${'$'}{pkg.bonus}</div>` : ''}
        `;
        grid.appendChild(card);
      });
    }

    function renderMethods() {
      const row = document.getElementById('method-row');
      row.innerHTML = '';
      PAYMENT_METHODS.forEach(m => {
        const card = document.createElement('div');
        card.className = 'method-card';
        card.dataset.id = m.id;
        card.onclick = () => selectMethod(m.id);
        card.innerHTML = `<span class="method-icon">${'$'}{m.icon}</span><span class="method-name">${'$'}{m.name}</span>`;
        row.appendChild(card);
      });
    }

    function selectPackage(id) {
      selectedPackage = COIN_PACKAGES.find(p => p.id === id);
      document.querySelectorAll('.package-card').forEach(el => {
        el.classList.toggle('selected', el.dataset.id === id);
      });
      updateConfirmButton();
      if (selectedMethod && selectedMethod.id === 'chapa') renderChapaWidget();
    }

    function selectMethod(id) {
      selectedMethod = PAYMENT_METHODS.find(m => m.id === id);
      document.querySelectorAll('.method-card').forEach(el => {
        el.classList.toggle('selected', el.dataset.id === id);
      });
      document.querySelectorAll('.pay-fields').forEach(el => el.classList.remove('active'));
      const target = document.getElementById(selectedMethod.fields);
      if (target) target.classList.add('active');
      updateConfirmButton();
      if (selectedMethod.id === 'chapa') renderChapaWidget();
    }

    function updateConfirmButton() {
      const btn = document.getElementById('confirm-deposit-btn');
      if (!selectedPackage) {
        btn.disabled = true;
        btn.style.display = 'block';
        btn.innerText = 'Select a package';
      } else if (!selectedMethod) {
        btn.disabled = true;
        btn.style.display = 'block';
        btn.innerText = 'Select a payment method';
      } else if (selectedMethod.id === 'chapa') {
        btn.style.display = 'none';
      } else {
        btn.disabled = false;
        btn.style.display = 'block';
        btn.innerText = `Deposit ${'$'}{selectedPackage.coins.toLocaleString()} 🪙 for ${'$'}{selectedPackage.price}`;
      }
    }

    function renderChapaWidget() {
      const container = document.getElementById('chapa-inline-form');
      if (!container || !selectedPackage) return;
      container.innerHTML = '';

      if (typeof ChapaCheckout === 'undefined') {
        container.innerHTML = '<div class="phone-hint">Chapa checkout ready for ETB deposit.</div>';
        return;
      }

      const txRef = 'meskot-' + selectedPackage.id + '-' + Date.now();
      try {
        const chapa = new ChapaCheckout({
          publicKey: CHAPA_PUBLIC_KEY,
          amount: String(selectedPackage.amountETB),
          currency: 'ETB',
          tx_ref: txRef,
          email: 'customer@meskot.et',
          first_name: currentUserName,
          last_name: 'Habesha',
          availablePaymentMethods: ['telebirr', 'cbebirr', 'ebirr', 'mpesa', 'chapa'],
          customizations: {
            buttonText: `Pay ${'$'}{selectedPackage.amountETB} ETB with Chapa`,
            styles: `
              .chapa-pay-button {
                background-color: #fed100;
                color: #0d1310;
                font-weight: 800;
              }
            `
          },
          callbackUrl: 'https://yourdomain.com/api/chapa/callback',
          returnUrl: 'https://yourdomain.com/deposit/success',
          onSuccess: function(data) {
            onSuccessfulDeposit(data && data.tx_ref ? data.tx_ref : txRef);
          }
        });

        chapa.initialize('chapa-inline-form');
      } catch (e) {
        console.error('Chapa init', e);
      }
    }

    function formatCardNumber(input) {
      let digits = input.value.replace(/\D/g, '').slice(0, 16);
      input.value = digits.replace(/(.{4})/g, '$1 ').trim();
    }

    function formatExpiry(input) {
      let digits = input.value.replace(/\D/g, '').slice(0, 4);
      input.value = digits.length > 2 ? digits.slice(0, 2) + '/' + digits.slice(2) : digits;
    }

    function openDepositModal() {
      document.getElementById('deposit-current-balance').innerText = userCoins.toLocaleString() + ' 🪙';
      document.getElementById('deposit-form-view').style.display = 'block';
      document.getElementById('deposit-processing').classList.remove('active');
      document.getElementById('deposit-success').classList.remove('active');
      document.getElementById('chapa-inline-form').innerHTML = '';
      document.getElementById('deposit-backdrop').classList.add('open');
      if (COIN_PACKAGES.length > 0 && !selectedPackage) {
        selectPackage(COIN_PACKAGES[0].id);
      }
      if (PAYMENT_METHODS.length > 0 && !selectedMethod) {
        selectMethod(PAYMENT_METHODS[0].id);
      }
    }

    function closeDepositModal() {
      document.getElementById('deposit-backdrop').classList.remove('open');
    }

    function closeDepositOnBackdrop(evt) {
      if (evt.target.id === 'deposit-backdrop') closeDepositModal();
    }

    function onSuccessfulDeposit(txRef) {
      if (!selectedPackage) return;
      const coinsAdded = selectedPackage.coins;
      const amountEtb = selectedPackage.amountETB;
      const fromValue = userCoins;
      userCoins += coinsAdded;
      animateCoinBalance(fromValue, userCoins);

      addChatMessage('System', `Deposit verified: ${'$'}{coinsAdded.toLocaleString()} 🪙 added!`, '#4fd67a');

      if (window.AndroidBridge && window.AndroidBridge.depositSuccess) {
        window.AndroidBridge.depositSuccess(coinsAdded, amountEtb, txRef);
      }

      document.getElementById('deposit-form-view').style.display = 'none';
      document.getElementById('deposit-processing').classList.remove('active');
      document.getElementById('deposit-success-amount').innerText =
        `${'$'}{coinsAdded.toLocaleString()} 🪙 added • ${'$'}{selectedPackage.price}`;
      document.getElementById('deposit-success').classList.add('active');
    }

    function submitDeposit() {
      if (!selectedPackage || !selectedMethod) return;

      document.getElementById('deposit-form-view').style.display = 'none';
      document.getElementById('deposit-processing').classList.add('active');

      setTimeout(() => {
        onSuccessfulDeposit('tx-' + Date.now());
        selectedPackage = null;
        selectedMethod = null;
      }, 1200);
    }

    renderPackages();
    renderMethods();
  </script>
</body>
</html>
    """.trimIndent()
}

/**
 * Fullscreen Interactive Live Stream & Ethiopian Cultural Gifting Modal connected to Firebase.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun MeskotLiveStreamModal(
    currentUser: User?,
    liveSession: LiveStreamSession? = null,
    liveMessages: List<LiveStreamComment> = emptyList(),
    onDismiss: () -> Unit,
    onSendMessage: (String) -> Unit = {},
    onSendLike: () -> Unit = {},
    onGiftSent: (giftId: Int, giftName: String, giftIcon: String, coinsCost: Int) -> Unit,
    onDepositCompleted: (coins: Int, amountEtb: Double, txRef: String) -> Unit,
    onEndLive: () -> Unit = {}
) {
    var isLoading by remember { mutableStateOf(true) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    val isHost = liveSession?.hostUid == currentUser?.uid
    val initialCoins = currentUser?.starBalance ?: 500
    val userName = currentUser?.displayName ?: "Meskot Member"
    val streamerName = if (isHost) currentUser?.displayName ?: "Broadcaster" else liveSession?.hostName ?: "Meskot Live"
    val viewerCount = liveSession?.formattedViewers ?: "1 Viewer"
    val streamTitle = liveSession?.title ?: "Meskot Live Stream"
    val likesCount = liveSession?.likesCount ?: 0

    val htmlContent = remember(initialCoins, userName, streamerName, isHost) {
        buildLiveGiftsHtml(
            initialCoins = initialCoins,
            userName = userName,
            streamerName = streamerName,
            viewerCount = viewerCount,
            streamTitle = streamTitle,
            isHost = isHost,
            likesCount = likesCount
        )
    }

    // Push new Firestore live messages into the WebView
    LaunchedEffect(liveMessages.size) {
        if (liveMessages.isNotEmpty() && webViewRef != null) {
            val last = liveMessages.last()
            val escapedName = last.senderName.replace("'", "\\'")
            val escapedText = last.text.replace("'", "\\'")
            val color = when (last.type) {
                "GIFT" -> "#fed100"
                "JOIN" -> "#4fd67a"
                "LIKE" -> "#ef2b2d"
                else -> "#ffffff"
            }
            val icon = last.giftIcon ?: ""
            webViewRef?.evaluateJavascript(
                "if(window.receiveRemoteMessage){ window.receiveRemoteMessage('$escapedName', '$escapedText', '$color', '${last.type}', '$icon'); }",
                null
            )
        }
    }

    // Push updated Firestore stream statistics into the WebView
    LaunchedEffect(liveSession?.viewerCount, liveSession?.likesCount, liveSession?.totalCoins) {
        val session = liveSession ?: return@LaunchedEffect
        val vStr = session.formattedViewers
        val likes = session.likesCount
        val coins = session.totalCoins
        webViewRef?.evaluateJavascript(
            "if(window.updateStreamStats){ window.updateStreamStats('$vStr', $likes, $coins); }",
            null
        )
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
                .testTag("meskot_live_stream_modal"),
            color = Color(0xFF0B0D0F)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                AndroidView(
                    factory = { ctx ->
                        WebView(ctx).apply {
                            webViewRef = this
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            settings.loadWithOverviewMode = true
                            settings.useWideViewPort = true
                            settings.databaseEnabled = true
                            settings.allowFileAccess = false
                            setBackgroundColor(android.graphics.Color.parseColor("#0B0D0F"))

                            addJavascriptInterface(
                                MeskotLiveGiftsBridge(
                                    onGiftSent = { id, name, icon, cost ->
                                        post { onGiftSent(id, name, icon, cost) }
                                    },
                                    onDepositSuccess = { coins, etb, ref ->
                                        post { onDepositCompleted(coins, etb, ref) }
                                    },
                                    onCloseLive = {
                                        post { onDismiss() }
                                    },
                                    onSendComment = { text ->
                                        post { onSendMessage(text) }
                                    },
                                    onSendLike = {
                                        post { onSendLike() }
                                    },
                                    onEndLive = {
                                        post { onEndLive() }
                                    }
                                ),
                                "AndroidBridge"
                            )

                            webChromeClient = WebChromeClient()
                            webViewClient = object : WebViewClient() {
                                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                    super.onPageStarted(view, url, favicon)
                                    isLoading = true
                                }

                                override fun onPageFinished(view: WebView?, url: String?) {
                                    super.onPageFinished(view, url)
                                    isLoading = false
                                }
                            }

                            loadDataWithBaseURL(
                                "https://meskot.et/",
                                htmlContent,
                                "text/html",
                                "UTF-8",
                                null
                            )
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Top Close Bar overlay
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.55f))
                            .testTag("close_live_stream_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Live",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Loading Indicator
                AnimatedVisibility(
                    visible = isLoading,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.75f))
                            .padding(20.dp)
                    ) {
                        CircularProgressIndicator(
                            color = Color(0xFFFED100),
                            modifier = Modifier.size(32.dp),
                            strokeWidth = 3.dp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Entering Meskot Live...",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
