/**
 * Vercel Serverless Function: /api/admin/verify-maintenance-key
 * 
 * Companion endpoint for KCET GenZ backend (compatible with kcet-password-reset.vercel.app).
 * Verifies the developer access security key against Firebase Firestore collection app_config/maintenance_access.
 */

const admin = require("firebase-admin");
const crypto = require("crypto");

if (!admin.apps.length) {
  try {
    if (process.env.FIREBASE_SERVICE_ACCOUNT_KEY) {
      const serviceAccount = JSON.parse(process.env.FIREBASE_SERVICE_ACCOUNT_KEY);
      admin.initializeApp({
        credential: admin.credential.cert(serviceAccount)
      });
    } else {
      admin.initializeApp();
    }
  } catch (e) {
    console.error("Firebase admin init failed:", e.message);
  }
}

const rateLimitMap = new Map();
const MAX_FAILED_ATTEMPTS = 5;
const LOCKOUT_DURATION_MS = 15 * 60 * 1000;

function sha256Hex(str) {
  return crypto.createHash("sha256").update(str, "utf8").digest("hex");
}

function safeCompare(a, b) {
  if (typeof a !== "string" || typeof b !== "string") return false;
  const bufA = Buffer.from(a.trim().toLowerCase(), "utf8");
  const bufB = Buffer.from(b.trim().toLowerCase(), "utf8");
  if (bufA.length !== bufB.length) return false;
  return crypto.timingSafeEqual(bufA, bufB);
}

module.exports = async function handler(req, res) {
  res.setHeader("Access-Control-Allow-Origin", "*");
  res.setHeader("Access-Control-Allow-Methods", "POST, OPTIONS");
  res.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
  res.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, private");

  if (req.method === "OPTIONS") {
    return res.status(204).end();
  }

  if (req.method !== "POST") {
    return res.status(405).json({
      success: false,
      reason: "METHOD_NOT_ALLOWED",
      message: "Method not allowed. Use POST."
    });
  }

  const rawIp = req.headers["x-forwarded-for"] || req.socket?.remoteAddress || "unknown";
  const clientIp = String(rawIp).split(",")[0].trim();
  const now = Date.now();

  const rateData = rateLimitMap.get(clientIp);
  if (rateData && rateData.lockUntil > now) {
    const minutesLeft = Math.max(1, Math.ceil((rateData.lockUntil - now) / (60 * 1000)));
    return res.status(429).json({
      success: false,
      reason: "RATE_LIMITED",
      message: `Too many failed attempts. Please try again in ${minutesLeft} minute(s).`
    });
  }

  try {
    const enteredKey = req.body && typeof req.body.securityKey === "string" ? req.body.securityKey.trim() : "";
    if (!enteredKey) {
      return res.status(400).json({
        success: false,
        reason: "EMPTY_KEY",
        message: "Please enter the security key."
      });
    }

    const db = admin.firestore();
    const docRef = db.collection("app_config").doc("maintenance_access");
    const docSnap = await docRef.get();

    if (!docSnap.exists) {
      return res.status(404).json({
        success: false,
        reason: "DISABLED",
        message: "Developer access is disabled."
      });
    }

    const config = docSnap.data() || {};
    if (config.enabled !== true) {
      return res.status(403).json({
        success: false,
        reason: "DISABLED",
        message: "Developer access is disabled."
      });
    }

    const storedHash = typeof config.keyHash === "string" ? config.keyHash.trim().toLowerCase() : "";
    const candidateHash = sha256Hex(enteredKey);
    const plaintextFallback = typeof config.key === "string" && config.key.trim().length > 0
      ? sha256Hex(config.key.trim())
      : "";

    const isMatch = (storedHash.length > 0 && safeCompare(candidateHash, storedHash)) ||
      (plaintextFallback.length > 0 && safeCompare(candidateHash, plaintextFallback));

    if (isMatch) {
      rateLimitMap.delete(clientIp);
      return res.status(200).json({
        success: true,
        message: "Developer access verified successfully."
      });
    } else {
      const currentFailures = (rateData?.failedCount || 0) + 1;
      if (currentFailures >= MAX_FAILED_ATTEMPTS) {
        rateLimitMap.set(clientIp, {
          failedCount: currentFailures,
          lockUntil: now + LOCKOUT_DURATION_MS
        });
      } else {
        rateLimitMap.set(clientIp, {
          failedCount: currentFailures,
          lockUntil: 0
        });
      }

      return res.status(401).json({
        success: false,
        reason: "INVALID_KEY",
        message: "Invalid security key."
      });
    }
  } catch (error) {
    console.error("Vercel verifyMaintenanceKey error:", error.message || error);
    return res.status(500).json({
      success: false,
      reason: "SERVER_ERROR",
      message: "Unable to verify key. Please try again."
    });
  }
};
