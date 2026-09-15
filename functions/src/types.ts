import { Timestamp } from "firebase-admin/firestore";

/**
 * Cloud Firestore Collection: wallets/{userId}
 */
export interface WalletDocument {
  chapaBalance: number;     // Live liquid ETB balance in user wallet
  grossRevenue: number;     // Total lifetime gross creator revenue in ETB
  platformFee: number;      // Total platform fees contributed in ETB
  virtualStars: number;     // Current live virtual star balance
  updatedAt: Timestamp;
}

/**
 * Cloud Firestore Collection: studio_analytics/{userId}
 */
export interface StudioAnalyticsDocument {
  dailyApproximateEarnings: number; // Approximate estimated earnings today in ETB
  last7DaysEarnings: number;        // Total aggregated earnings for the rolling 7-day period in ETB
  updatedAt?: Timestamp;
}

/**
 * Valid transaction types according to financial audit specification
 */
export type TransactionType =
  | "DEPOSIT"
  | "WITHDRAWAL"
  | "SUPPORT"
  | "STARS_PURCHASE"
  | "STARS_GIFT"
  | "POST_BOOST"
  | "AD_CAMPAIGN";

export type TransactionStatus = "COMPLETED" | "FAILED" | "PENDING";

/**
 * Cloud Firestore Collection: transactions/{transactionId}
 */
export interface TransactionDocument {
  transactionId: string;
  userId: string;
  type: TransactionType;
  amountETB: number;
  stars: number;
  feeETB: number;
  recipientId?: string;
  campaignId?: string;
  referenceId?: string;
  status: TransactionStatus;
  notes?: string;
  timestamp: Timestamp;
}

export type AdCampaignStatus = "ACTIVE" | "PAUSED" | "COMPLETED";

/**
 * Cloud Firestore Collection: ad_campaigns/{campaignId}
 */
export interface AdCampaignDocument {
  campaignId: string;
  userId: string;
  campaignName: string;
  objective: string;
  dailyBudgetETB: number;
  durationInDays: number;
  totalBudgetETB: number;
  totalSpendETB: number;
  adHeadline: string;
  primaryText: string;
  callToAction: string;
  destinationUrl?: string;
  status: AdCampaignStatus;
  createdAt: Timestamp;
  updatedAt: Timestamp;
}

// --------------------------------------------------------
// Request & Response Payloads for Callable Cloud Functions
// --------------------------------------------------------

export interface DepositETBRequest {
  amountETB: number;
  txRef: string;
  paymentMethod: "CHAPA" | "TELEBIRR" | "CBE_BIRR";
}

export interface DepositETBResponse {
  success: boolean;
  transactionId: string;
  newBalanceETB: number;
  amountDepositedETB: number;
}

export interface WithdrawETBRequest {
  withdrawAmountETB: number;
  accountNumber: string;
  accountHolderName: string;
  bankCode: string;
}

export interface WithdrawETBResponse {
  success: boolean;
  transactionId: string;
  withdrawnAmountETB: number;
  newBalanceETB: number;
}

export interface SupportCreatorRequest {
  creatorId: string;
  amountETB: number;
  message?: string;
}

export interface SupportCreatorResponse {
  success: boolean;
  transactionId: string;
  amountSentETB: number;
  senderNewBalanceETB: number;
}

export interface PurchaseStarsRequest {
  totalStars: number;
}

export interface PurchaseStarsResponse {
  success: boolean;
  transactionId: string;
  totalStarsPurchased: number;
  totalCostETB: number;
  newStarBalance: number;
  newChapaBalanceETB: number;
}

export interface SendStarGiftRequest {
  creatorId: string;
  giftStarCount: number;
  postId?: string;
}

export interface SendStarGiftResponse {
  success: boolean;
  transactionId: string;
  starsGifted: number;
  grossValueETB: number;
  platformFeeETB: number;
  creatorNetETB: number;
  senderRemainingStars: number;
}

export interface BoostPostRequest {
  postId: string;
  dailyBudgetETB: number;
  durationInDays: number;
}

export interface BoostPostResponse {
  success: boolean;
  transactionId: string;
  totalCostETB: number;
  newBalanceETB: number;
  boostExpiryDate: string;
}

export interface CreateAdCampaignRequest {
  campaignName: string;
  objective: string;
  dailyBudgetETB: number;
  durationInDays: number;
  adHeadline: string;
  primaryText: string;
  callToAction: string;
  destinationUrl?: string;
}

export interface CreateAdCampaignResponse {
  success: boolean;
  campaignId: string;
  transactionId: string;
  totalCostETB: number;
  newBalanceETB: number;
}
