import * as admin from "firebase-admin";
import { onCall, onRequest, HttpsError } from "firebase-functions/v2/https";
import {
  WalletDocument,
  StudioAnalyticsDocument,
  TransactionDocument,
  AdCampaignDocument,
  DepositETBRequest,
  DepositETBResponse,
  WithdrawETBRequest,
  WithdrawETBResponse,
  SupportCreatorRequest,
  SupportCreatorResponse,
  PurchaseStarsRequest,
  PurchaseStarsResponse,
  SendStarGiftRequest,
  SendStarGiftResponse,
  BoostPostRequest,
  BoostPostResponse,
  CreateAdCampaignRequest,
  CreateAdCampaignResponse,
} from "./types";

// Initialize Firebase Admin SDK
if (!admin.apps.length) {
  admin.initializeApp();
}

const db = admin.firestore();

// Financial constants per Meskot Partner Studio Specification
const STAR_ETB_RATE = 1.50; // 1 Star = 1.50 ETB
const STAR_PLATFORM_FEE_PERCENT = 0.20; // 20% platform cut on virtual gifts
const STAR_CREATOR_SHARE_PERCENT = 0.80; // 80% net to creator

/**
 * Currency arithmetic helper: round down/up to strict 2 decimal places.
 * Avoids IEEE-754 floating-point inaccuracies (e.g. 0.1 + 0.2 = 0.30000000000000004).
 */
function roundCurrency(val: number): number {
  return Math.round((val + Number.EPSILON) * 100) / 100;
}

/**
 * Ensures user has an active wallet document initialized inside a transaction.
 */
function getOrCreateWallet(
  walletSnap: FirebaseFirestore.DocumentSnapshot,
  userId: string
): WalletDocument {
  if (walletSnap.exists) {
    const data = walletSnap.data() as Partial<WalletDocument>;
    return {
      chapaBalance: roundCurrency(Number(data.chapaBalance) || 0),
      grossRevenue: roundCurrency(Number(data.grossRevenue) || 0),
      platformFee: roundCurrency(Number(data.platformFee) || 0),
      virtualStars: Math.max(0, Math.floor(Number(data.virtualStars) || 0)),
      updatedAt: data.updatedAt || admin.firestore.Timestamp.now(),
    };
  }

  // Initial fresh state
  return {
    chapaBalance: 0,
    grossRevenue: 0,
    platformFee: 0,
    virtualStars: 0,
    updatedAt: admin.firestore.Timestamp.now(),
  };
}

// ============================================================================
// 1. DEPOSIT FUNDS (depositETB)
// ============================================================================
/**
 * Atomically deposits real funds into a user's wallet.
 * Callable by verified client sessions or internal processing.
 */
export const depositETB = onCall<DepositETBRequest, Promise<DepositETBResponse>>(
  { cors: true },
  async (request) => {
    const userId = request.auth?.uid;
    if (!userId) {
      throw new HttpsError("unauthenticated", "Authentication required to deposit funds.");
    }

    const rawAmount = request.data.amountETB;
    const txRef = request.data.txRef;

    if (!rawAmount || typeof rawAmount !== "number" || rawAmount <= 0) {
      throw new HttpsError("invalid-argument", "A positive deposit amount in ETB is required.");
    }

    const depositAmount = roundCurrency(rawAmount);
    if (depositAmount < 5) {
      throw new HttpsError("invalid-argument", "Minimum deposit is 5.00 ETB.");
    }

    const walletRef = db.collection("wallets").doc(userId);
    const txRefDoc = db.collection("transactions").doc();

    return await db.runTransaction(async (transaction) => {
      const walletSnap = await transaction.get(walletRef);
      const currentWallet = getOrCreateWallet(walletSnap, userId);

      const updatedBalance = roundCurrency(currentWallet.chapaBalance + depositAmount);
      const now = admin.firestore.Timestamp.now();

      // Write updated wallet
      transaction.set(
        walletRef,
        {
          chapaBalance: updatedBalance,
          grossRevenue: currentWallet.grossRevenue,
          platformFee: currentWallet.platformFee,
          virtualStars: currentWallet.virtualStars,
          updatedAt: now,
        },
        { merge: true }
      );

      // Write transaction ledger entry
      const txDoc: TransactionDocument = {
        transactionId: txRefDoc.id,
        userId,
        type: "DEPOSIT",
        amountETB: depositAmount,
        stars: 0,
        feeETB: 0,
        referenceId: txRef || `DEP-${Date.now()}`,
        status: "COMPLETED",
        notes: `Deposited ${depositAmount} ETB via ${request.data.paymentMethod || "CHAPA"}`,
        timestamp: now,
      };
      transaction.set(txRefDoc, txDoc);

      return {
        success: true,
        transactionId: txRefDoc.id,
        newBalanceETB: updatedBalance,
        amountDepositedETB: depositAmount,
      };
    });
  }
);

// ============================================================================
// 2. CASH OUT / WITHDRAW (withdrawETB)
// ============================================================================
/**
 * Executes a payout debit transaction. Verifies actual live balance and
 * prevents negative balances or overdrafts.
 */
export const withdrawETB = onCall<WithdrawETBRequest, Promise<WithdrawETBResponse>>(
  { cors: true },
  async (request) => {
    const userId = request.auth?.uid;
    if (!userId) {
      throw new HttpsError("unauthenticated", "Authentication required to withdraw funds.");
    }

    const rawWithdraw = request.data.withdrawAmountETB;
    if (!rawWithdraw || typeof rawWithdraw !== "number" || rawWithdraw <= 0) {
      throw new HttpsError("invalid-argument", "A valid withdrawal amount is required.");
    }

    const withdrawAmount = roundCurrency(rawWithdraw);
    if (withdrawAmount < 50) {
      throw new HttpsError("invalid-argument", "Minimum withdrawal is 50.00 ETB.");
    }

    const { accountNumber, accountHolderName, bankCode } = request.data;
    if (!accountNumber || !accountHolderName) {
      throw new HttpsError("invalid-argument", "Valid bank account details are required.");
    }

    const walletRef = db.collection("wallets").doc(userId);
    const txRefDoc = db.collection("transactions").doc();

    return await db.runTransaction(async (transaction) => {
      const walletSnap = await transaction.get(walletRef);
      if (!walletSnap.exists) {
        throw new HttpsError("failed-precondition", "Wallet not initialized or zero balance.");
      }

      const wallet = getOrCreateWallet(walletSnap, userId);

      // Strict Live Balance Audit Check
      if (wallet.chapaBalance < withdrawAmount) {
        throw new HttpsError(
          "failed-precondition",
          `Insufficient funds. Available: ${wallet.chapaBalance.toFixed(2)} ETB, Requested: ${withdrawAmount.toFixed(2)} ETB.`
        );
      }

      const newBalance = roundCurrency(wallet.chapaBalance - withdrawAmount);
      const now = admin.firestore.Timestamp.now();

      // Deduct exact live balance
      transaction.update(walletRef, {
        chapaBalance: newBalance,
        updatedAt: now,
      });

      // Record transaction
      const txDoc: TransactionDocument = {
        transactionId: txRefDoc.id,
        userId,
        type: "WITHDRAWAL",
        amountETB: withdrawAmount,
        stars: 0,
        feeETB: 0,
        referenceId: `WD-${bankCode || "BANK"}-${Date.now()}`,
        status: "COMPLETED",
        notes: `Payout to ${accountHolderName} (${accountNumber}, Bank: ${bankCode || "Default"})`,
        timestamp: now,
      };
      transaction.set(txRefDoc, txDoc);

      return {
        success: true,
        transactionId: txRefDoc.id,
        withdrawnAmountETB: withdrawAmount,
        newBalanceETB: newBalance,
      };
    });
  }
);

// ============================================================================
// 3. SUPPORT CREATOR / CHAPA TIP (supportCreator)
// ============================================================================
/**
 * Real-money tip transfer from user to creator.
 * 100% of the ETB goes directly to the creator (0% platform cut).
 */
export const supportCreator = onCall<SupportCreatorRequest, Promise<SupportCreatorResponse>>(
  { cors: true },
  async (request) => {
    const senderId = request.auth?.uid;
    if (!senderId) {
      throw new HttpsError("unauthenticated", "Authentication required to support creator.");
    }

    const { creatorId, amountETB, message } = request.data;
    if (!creatorId || typeof creatorId !== "string") {
      throw new HttpsError("invalid-argument", "Creator ID is required.");
    }
    if (creatorId === senderId) {
      throw new HttpsError("invalid-argument", "Cannot send a tip to your own wallet.");
    }

    if (!amountETB || typeof amountETB !== "number" || amountETB <= 0) {
      throw new HttpsError("invalid-argument", "A valid tip amount is required.");
    }

    const tipAmount = roundCurrency(amountETB);
    const senderWalletRef = db.collection("wallets").doc(senderId);
    const creatorWalletRef = db.collection("wallets").doc(creatorId);
    const creatorAnalyticsRef = db.collection("studio_analytics").doc(creatorId);
    const txRefDoc = db.collection("transactions").doc();

    return await db.runTransaction(async (transaction) => {
      // 1. Read sender wallet
      const senderSnap = await transaction.get(senderWalletRef);
      if (!senderSnap.exists) {
        throw new HttpsError("failed-precondition", "Sender wallet not found.");
      }
      const senderWallet = getOrCreateWallet(senderSnap, senderId);

      if (senderWallet.chapaBalance < tipAmount) {
        throw new HttpsError(
          "failed-precondition",
          `Insufficient funds. Current balance: ${senderWallet.chapaBalance.toFixed(2)} ETB, Tip amount: ${tipAmount.toFixed(2)} ETB.`
        );
      }

      // 2. Read creator wallet & analytics
      const creatorSnap = await transaction.get(creatorWalletRef);
      const creatorWallet = getOrCreateWallet(creatorSnap, creatorId);

      const analyticsSnap = await transaction.get(creatorAnalyticsRef);
      const analyticsData = (analyticsSnap.data() || {}) as Partial<StudioAnalyticsDocument>;
      const currentDaily = Number(analyticsData.dailyApproximateEarnings) || 0;
      const current7Days = Number(analyticsData.last7DaysEarnings) || 0;

      const now = admin.firestore.Timestamp.now();

      // Real Calculations
      const senderNewBalance = roundCurrency(senderWallet.chapaBalance - tipAmount);
      const creatorNewBalance = roundCurrency(creatorWallet.chapaBalance + tipAmount);
      const creatorNewGross = roundCurrency(creatorWallet.grossRevenue + tipAmount);
      const updatedDailyEarnings = roundCurrency(currentDaily + tipAmount);
      const updated7DaysEarnings = roundCurrency(current7Days + tipAmount);

      // Debit sender
      transaction.update(senderWalletRef, {
        chapaBalance: senderNewBalance,
        updatedAt: now,
      });

      // Credit creator 100%
      transaction.set(
        creatorWalletRef,
        {
          chapaBalance: creatorNewBalance,
          grossRevenue: creatorNewGross,
          platformFee: creatorWallet.platformFee,
          virtualStars: creatorWallet.virtualStars,
          updatedAt: now,
        },
        { merge: true }
      );

      // Update creator studio analytics
      transaction.set(
        creatorAnalyticsRef,
        {
          dailyApproximateEarnings: updatedDailyEarnings,
          last7DaysEarnings: updated7DaysEarnings,
          updatedAt: now,
        },
        { merge: true }
      );

      // Record transaction
      const txDoc: TransactionDocument = {
        transactionId: txRefDoc.id,
        userId: senderId,
        recipientId: creatorId,
        type: "SUPPORT",
        amountETB: tipAmount,
        stars: 0,
        feeETB: 0,
        status: "COMPLETED",
        notes: message ? `Tip with message: ${message}` : "Creator Tip Support",
        timestamp: now,
      };
      transaction.set(txRefDoc, txDoc);

      return {
        success: true,
        transactionId: txRefDoc.id,
        amountSentETB: tipAmount,
        senderNewBalanceETB: senderNewBalance,
      };
    });
  }
);

// ============================================================================
// 4. BUY VIRTUAL STARS (purchaseStars)
// ============================================================================
/**
 * Converts live ETB balance to virtual stars.
 * Real Cost Formula: totalStars * 1.50 ETB.
 */
export const purchaseStars = onCall<PurchaseStarsRequest, Promise<PurchaseStarsResponse>>(
  { cors: true },
  async (request) => {
    const userId = request.auth?.uid;
    if (!userId) {
      throw new HttpsError("unauthenticated", "Authentication required to buy stars.");
    }

    const starCount = Math.floor(Number(request.data.totalStars));
    if (!starCount || starCount <= 0) {
      throw new HttpsError("invalid-argument", "Positive integer star count required.");
    }

    // Exact cost calculation: totalStars * 1.50 ETB
    const totalCost = roundCurrency(starCount * STAR_ETB_RATE);

    const walletRef = db.collection("wallets").doc(userId);
    const txRefDoc = db.collection("transactions").doc();

    return await db.runTransaction(async (transaction) => {
      const walletSnap = await transaction.get(walletRef);
      if (!walletSnap.exists) {
        throw new HttpsError("failed-precondition", "Wallet not found.");
      }
      const wallet = getOrCreateWallet(walletSnap, userId);

      if (wallet.chapaBalance < totalCost) {
        throw new HttpsError(
          "failed-precondition",
          `Insufficient funds to purchase ${starCount} stars. Cost: ${totalCost.toFixed(2)} ETB, Balance: ${wallet.chapaBalance.toFixed(2)} ETB.`
        );
      }

      const now = admin.firestore.Timestamp.now();
      const updatedBalance = roundCurrency(wallet.chapaBalance - totalCost);
      const updatedStars = wallet.virtualStars + starCount;

      transaction.update(walletRef, {
        chapaBalance: updatedBalance,
        virtualStars: updatedStars,
        updatedAt: now,
      });

      const txDoc: TransactionDocument = {
        transactionId: txRefDoc.id,
        userId,
        type: "STARS_PURCHASE",
        amountETB: totalCost,
        stars: starCount,
        feeETB: 0,
        status: "COMPLETED",
        notes: `Purchased ${starCount} stars at ${STAR_ETB_RATE} ETB/star`,
        timestamp: now,
      };
      transaction.set(txRefDoc, txDoc);

      return {
        success: true,
        transactionId: txRefDoc.id,
        totalStarsPurchased: starCount,
        totalCostETB: totalCost,
        newStarBalance: updatedStars,
        newChapaBalanceETB: updatedBalance,
      };
    });
  }
);

// ============================================================================
// 5. SEND VIRTUAL STARS / GIFTING (sendStarGift)
// ============================================================================
/**
 * Sends virtual stars from user to creator.
 * - Deducts star count from sender.
 * - Credits creator's star balance.
 * - Real gross monetary value: giftStarCount * 1.50 ETB.
 * - Platform Fee: 20%.
 * - Net Creator Earnings: 80% credited to creator gross revenue and studio analytics.
 */
export const sendStarGift = onCall<SendStarGiftRequest, Promise<SendStarGiftResponse>>(
  { cors: true },
  async (request) => {
    const senderId = request.auth?.uid;
    if (!senderId) {
      throw new HttpsError("unauthenticated", "Authentication required to gift stars.");
    }

    const { creatorId, postId } = request.data;
    const giftCount = Math.floor(Number(request.data.giftStarCount));

    if (!creatorId || typeof creatorId !== "string") {
      throw new HttpsError("invalid-argument", "Creator ID is required.");
    }
    if (creatorId === senderId) {
      throw new HttpsError("invalid-argument", "Cannot send a star gift to yourself.");
    }
    if (!giftCount || giftCount <= 0) {
      throw new HttpsError("invalid-argument", "Positive integer star count required.");
    }

    // Mathematical breakdown of the financial value
    const grossValue = roundCurrency(giftCount * STAR_ETB_RATE);
    const platformFee = roundCurrency(grossValue * STAR_PLATFORM_FEE_PERCENT);
    const creatorNet = roundCurrency(grossValue * STAR_CREATOR_SHARE_PERCENT);

    const senderWalletRef = db.collection("wallets").doc(senderId);
    const creatorWalletRef = db.collection("wallets").doc(creatorId);
    const creatorAnalyticsRef = db.collection("studio_analytics").doc(creatorId);
    const txRefDoc = db.collection("transactions").doc();

    return await db.runTransaction(async (transaction) => {
      // 1. Check sender stars
      const senderSnap = await transaction.get(senderWalletRef);
      if (!senderSnap.exists) {
        throw new HttpsError("failed-precondition", "Sender wallet not found.");
      }
      const senderWallet = getOrCreateWallet(senderSnap, senderId);

      if (senderWallet.virtualStars < giftCount) {
        throw new HttpsError(
          "failed-precondition",
          `Insufficient stars. Available: ${senderWallet.virtualStars}, Required: ${giftCount}.`
        );
      }

      // 2. Read creator wallet and analytics
      const creatorSnap = await transaction.get(creatorWalletRef);
      const creatorWallet = getOrCreateWallet(creatorSnap, creatorId);

      const analyticsSnap = await transaction.get(creatorAnalyticsRef);
      const analyticsData = (analyticsSnap.data() || {}) as Partial<StudioAnalyticsDocument>;
      const currentDaily = Number(analyticsData.dailyApproximateEarnings) || 0;
      const current7Days = Number(analyticsData.last7DaysEarnings) || 0;

      const now = admin.firestore.Timestamp.now();

      // Updates
      const senderRemainingStars = senderWallet.virtualStars - giftCount;
      const creatorNewStars = creatorWallet.virtualStars + giftCount;
      const creatorNewGrossRevenue = roundCurrency(creatorWallet.grossRevenue + creatorNet);
      const creatorNewPlatformFee = roundCurrency(creatorWallet.platformFee + platformFee);
      const newDailyEarnings = roundCurrency(currentDaily + creatorNet);
      const new7DaysEarnings = roundCurrency(current7Days + creatorNet);

      // Debit sender stars
      transaction.update(senderWalletRef, {
        virtualStars: senderRemainingStars,
        updatedAt: now,
      });

      // Credit creator stars & revenue
      transaction.set(
        creatorWalletRef,
        {
          chapaBalance: creatorWallet.chapaBalance,
          grossRevenue: creatorNewGrossRevenue,
          platformFee: creatorNewPlatformFee,
          virtualStars: creatorNewStars,
          updatedAt: now,
        },
        { merge: true }
      );

      // Increment creator analytics
      transaction.set(
        creatorAnalyticsRef,
        {
          dailyApproximateEarnings: newDailyEarnings,
          last7DaysEarnings: new7DaysEarnings,
          updatedAt: now,
        },
        { merge: true }
      );

      // Record transaction
      const txDoc: TransactionDocument = {
        transactionId: txRefDoc.id,
        userId: senderId,
        recipientId: creatorId,
        type: "STARS_GIFT",
        amountETB: grossValue,
        stars: giftCount,
        feeETB: platformFee,
        status: "COMPLETED",
        notes: `Gifted ${giftCount} stars (Gross: ${grossValue} ETB, Net to creator: ${creatorNet} ETB, Fee: ${platformFee} ETB)${postId ? ` on post ${postId}` : ""}`,
        timestamp: now,
      };
      transaction.set(txRefDoc, txDoc);

      return {
        success: true,
        transactionId: txRefDoc.id,
        starsGifted: giftCount,
        grossValueETB: grossValue,
        platformFeeETB: platformFee,
        creatorNetETB: creatorNet,
        senderRemainingStars,
      };
    });
  }
);

// ============================================================================
// 6. BOOST POST (boostPost)
// ============================================================================
/**
 * Deducts live ETB funds to promote a post for the selected duration.
 * Cost Formula: dailyBudgetETB * durationInDays.
 */
export const boostPost = onCall<BoostPostRequest, Promise<BoostPostResponse>>(
  { cors: true },
  async (request) => {
    const userId = request.auth?.uid;
    if (!userId) {
      throw new HttpsError("unauthenticated", "Authentication required to boost post.");
    }

    const { postId, dailyBudgetETB, durationInDays } = request.data;
    if (!postId || typeof postId !== "string") {
      throw new HttpsError("invalid-argument", "Post ID is required.");
    }

    const dailyBudget = roundCurrency(Number(dailyBudgetETB) || 0);
    const duration = Math.floor(Number(durationInDays) || 0);

    if (dailyBudget < 10) {
      throw new HttpsError("invalid-argument", "Minimum daily budget for boost is 10.00 ETB.");
    }
    if (duration < 1 || duration > 30) {
      throw new HttpsError("invalid-argument", "Duration must be between 1 and 30 days.");
    }

    const totalCost = roundCurrency(dailyBudget * duration);
    const walletRef = db.collection("wallets").doc(userId);
    const postRef = db.collection("posts").doc(postId);
    const txRefDoc = db.collection("transactions").doc();

    return await db.runTransaction(async (transaction) => {
      // 1. Verify user wallet balance
      const walletSnap = await transaction.get(walletRef);
      if (!walletSnap.exists) {
        throw new HttpsError("failed-precondition", "User wallet not found.");
      }
      const wallet = getOrCreateWallet(walletSnap, userId);

      if (wallet.chapaBalance < totalCost) {
        throw new HttpsError(
          "failed-precondition",
          `Insufficient funds to boost post. Cost: ${totalCost.toFixed(2)} ETB, Balance: ${wallet.chapaBalance.toFixed(2)} ETB.`
        );
      }

      // 2. Verify post existence
      const postSnap = await transaction.get(postRef);
      if (!postSnap.exists) {
        throw new HttpsError("not-found", "Target post does not exist.");
      }

      const now = admin.firestore.Timestamp.now();
      const expiryDate = new Date(Date.now() + duration * 24 * 60 * 60 * 1000);
      const newBalance = roundCurrency(wallet.chapaBalance - totalCost);

      // Debit wallet
      transaction.update(walletRef, {
        chapaBalance: newBalance,
        updatedAt: now,
      });

      // Update post boosting state
      transaction.update(postRef, {
        isBoosted: true,
        boostDailyBudget: dailyBudget,
        boostTotalCost: totalCost,
        boostExpiresAt: admin.firestore.Timestamp.fromDate(expiryDate),
        updatedAt: now,
      });

      // Record transaction
      const txDoc: TransactionDocument = {
        transactionId: txRefDoc.id,
        userId,
        type: "POST_BOOST",
        amountETB: totalCost,
        stars: 0,
        feeETB: totalCost,
        referenceId: postId,
        status: "COMPLETED",
        notes: `Boosted post ${postId} for ${duration} days @ ${dailyBudget} ETB/day`,
        timestamp: now,
      };
      transaction.set(txRefDoc, txDoc);

      return {
        success: true,
        transactionId: txRefDoc.id,
        totalCostETB: totalCost,
        newBalanceETB: newBalance,
        boostExpiryDate: expiryDate.toISOString(),
      };
    });
  }
);

// ============================================================================
// 7. CREATE AD CAMPAIGN (createAdCampaign)
// ============================================================================
/**
 * Creates an ad campaign in `ad_campaigns/{campaignId}` with ACTIVE status
 * and deducts the total budget atomically from the live user balance.
 * Budget Formula: dailyBudgetETB * durationInDays.
 */
export const createAdCampaign = onCall<CreateAdCampaignRequest, Promise<CreateAdCampaignResponse>>(
  { cors: true },
  async (request) => {
    const userId = request.auth?.uid;
    if (!userId) {
      throw new HttpsError("unauthenticated", "Authentication required to create ad campaign.");
    }

    const {
      campaignName,
      objective,
      dailyBudgetETB,
      durationInDays,
      adHeadline,
      primaryText,
      callToAction,
      destinationUrl,
    } = request.data;

    if (!campaignName || !objective || !adHeadline || !primaryText) {
      throw new HttpsError("invalid-argument", "All campaign creative fields and objective are required.");
    }

    const dailyBudget = roundCurrency(Number(dailyBudgetETB) || 0);
    const duration = Math.floor(Number(durationInDays) || 0);

    if (dailyBudget < 25) {
      throw new HttpsError("invalid-argument", "Minimum daily budget for Ads Manager is 25.00 ETB.");
    }
    if (duration < 1 || duration > 90) {
      throw new HttpsError("invalid-argument", "Campaign duration must be between 1 and 90 days.");
    }

    const totalBudget = roundCurrency(dailyBudget * duration);
    const walletRef = db.collection("wallets").doc(userId);
    const campaignRef = db.collection("ad_campaigns").doc();
    const txRefDoc = db.collection("transactions").doc();

    return await db.runTransaction(async (transaction) => {
      // 1. Check live balance
      const walletSnap = await transaction.get(walletRef);
      if (!walletSnap.exists) {
        throw new HttpsError("failed-precondition", "User wallet not found.");
      }
      const wallet = getOrCreateWallet(walletSnap, userId);

      if (wallet.chapaBalance < totalBudget) {
        throw new HttpsError(
          "failed-precondition",
          `Insufficient funds for ad campaign. Budget required: ${totalBudget.toFixed(2)} ETB, Balance: ${wallet.chapaBalance.toFixed(2)} ETB.`
        );
      }

      const now = admin.firestore.Timestamp.now();
      const newBalance = roundCurrency(wallet.chapaBalance - totalBudget);

      // Debit wallet
      transaction.update(walletRef, {
        chapaBalance: newBalance,
        updatedAt: now,
      });

      // Create ad_campaigns document with status ACTIVE
      const campaignDoc: AdCampaignDocument = {
        campaignId: campaignRef.id,
        userId,
        campaignName,
        objective,
        dailyBudgetETB: dailyBudget,
        durationInDays: duration,
        totalBudgetETB: totalBudget,
        totalSpendETB: 0,
        adHeadline,
        primaryText,
        callToAction: callToAction || "LEARN_MORE",
        destinationUrl: destinationUrl || "",
        status: "ACTIVE",
        createdAt: now,
        updatedAt: now,
      };
      transaction.set(campaignRef, campaignDoc);

      // Record transaction
      const txDoc: TransactionDocument = {
        transactionId: txRefDoc.id,
        userId,
        campaignId: campaignRef.id,
        type: "AD_CAMPAIGN",
        amountETB: totalBudget,
        stars: 0,
        feeETB: totalBudget,
        status: "COMPLETED",
        notes: `Created active campaign '${campaignName}' (${objective}, ${duration} days @ ${dailyBudget} ETB/day)`,
        timestamp: now,
      };
      transaction.set(txRefDoc, txDoc);

      return {
        success: true,
        campaignId: campaignRef.id,
        transactionId: txRefDoc.id,
        totalCostETB: totalBudget,
        newBalanceETB: newBalance,
      };
    });
  }
);

// ============================================================================
// WEBHOOK: PAYMENT GATEWAY VERIFICATION (Chapa / Telebirr)
// ============================================================================
/**
 * Secure HTTP webhook handler for Chapa and Telebirr payment confirmations.
 * Automates real balance top-ups upon verified payment receipt.
 */
export const depositWebhook = onRequest(async (req, res) => {
  if (req.method !== "POST") {
    res.status(405).send("Method Not Allowed");
    return;
  }

  try {
    const payload = req.body;
    // Chapa standard webhook signature & payload inspection
    const status = payload?.status || payload?.data?.status;
    const txRef = payload?.tx_ref || payload?.data?.tx_ref;
    const rawAmount = payload?.amount || payload?.data?.amount;
    const userId = payload?.customization?.user_id || payload?.meta?.userId;

    if (!userId || !txRef || !rawAmount) {
      res.status(400).json({ error: "Missing required webhook parameters." });
      return;
    }

    if (status !== "success" && status !== "COMPLETED") {
      res.status(200).json({ status: "ignored", reason: `Transaction status is ${status}` });
      return;
    }

    const verifiedAmount = roundCurrency(Number(rawAmount));
    const walletRef = db.collection("wallets").doc(userId);
    const txQuery = await db.collection("transactions").where("referenceId", "==", txRef).limit(1).get();

    // Idempotency: Prevent duplicate deposits on repeated webhooks
    if (!txQuery.empty) {
      res.status(200).json({ status: "already_processed", txRef });
      return;
    }

    const txDocRef = db.collection("transactions").doc();
    const now = admin.firestore.Timestamp.now();

    await db.runTransaction(async (transaction) => {
      const walletSnap = await transaction.get(walletRef);
      const currentWallet = getOrCreateWallet(walletSnap, userId);
      const updatedBalance = roundCurrency(currentWallet.chapaBalance + verifiedAmount);

      transaction.set(
        walletRef,
        {
          chapaBalance: updatedBalance,
          grossRevenue: currentWallet.grossRevenue,
          platformFee: currentWallet.platformFee,
          virtualStars: currentWallet.virtualStars,
          updatedAt: now,
        },
        { merge: true }
      );

      const txDoc: TransactionDocument = {
        transactionId: txDocRef.id,
        userId,
        type: "DEPOSIT",
        amountETB: verifiedAmount,
        stars: 0,
        feeETB: 0,
        referenceId: txRef,
        status: "COMPLETED",
        notes: `Webhook verified deposit from payment gateway (${txRef})`,
        timestamp: now,
      };
      transaction.set(txDocRef, txDoc);
    });

    res.status(200).json({ success: true, txRef, depositedAmount: verifiedAmount });
  } catch (error: any) {
    console.error("Webhook processing error:", error);
    res.status(500).json({ error: error.message || "Internal server error" });
  }
});
