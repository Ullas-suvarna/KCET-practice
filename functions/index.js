/**
 * KCET GenZ - Backend Firebase Cloud Functions
 *
 * Provides secure verification for maintenance-mode developer access.
 * Reads configuration from Firestore collection: app_config, document: maintenance_access
 * Never exposes the key or its hash to client applications.
 */

const functions = require("firebase-functions");
const admin = require("firebase-admin");
const crypto = require("crypto");

if (!admin.apps.length) {
  admin.initializeApp();
}

const db = admin.firestore();

// In-memory rate limiting map: ip -> { failedCount, lockUntil }
const rateLimitMap = new Map();
const MAX_FAILED_ATTEMPTS = 5;
const LOCKOUT_DURATION_MS = 15 * 60 * 1000; // 15-minute lock after 5 repeated failed attempts

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

/**
 * HTTPS Cloud Function: verifyMaintenanceAccessKey
 *
 * Verifies submitted security key against Firestore backend configuration.
 * Endpoint: POST https://<region>-<project-id>.cloudfunctions.net/verifyMaintenanceAccessKey
 * Body: { "securityKey": "17082006" }
 */
exports.verifyMaintenanceAccessKey = functions.https.onRequest(async (req, res) => {
  // CORS configuration
  res.set("Access-Control-Allow-Origin", "*");
  res.set("Access-Control-Allow-Methods", "POST, OPTIONS");
  res.set("Access-Control-Allow-Headers", "Content-Type, Authorization");
  res.set("Cache-Control", "no-store, no-cache, must-revalidate, private");

  if (req.method === "OPTIONS") {
    res.status(204).send("");
    return;
  }

  if (req.method !== "POST") {
    res.status(405).json({
      success: false,
      reason: "METHOD_NOT_ALLOWED",
      message: "Method not allowed. Use POST."
    });
    return;
  }

  const rawIp = req.headers["x-forwarded-for"] || req.connection?.remoteAddress || req.socket?.remoteAddress || "unknown";
  const clientIp = String(rawIp).split(",")[0].trim();
  const now = Date.now();

  // Rate Limiting Check
  const rateData = rateLimitMap.get(clientIp);
  if (rateData && rateData.lockUntil > now) {
    const minutesLeft = Math.max(1, Math.ceil((rateData.lockUntil - now) / (60 * 1000)));
    res.status(429).json({
      success: false,
      reason: "RATE_LIMITED",
      message: `Too many failed attempts. Please try again in ${minutesLeft} minute(s).`
    });
    return;
  }

  try {
    let enteredKey = "";
    if (typeof req.body === "object" && req.body !== null) {
      enteredKey = typeof req.body.securityKey === "string" ? req.body.securityKey.trim() : "";
    } else if (typeof req.body === "string" && req.body.length > 0) {
      try {
        const parsed = JSON.parse(req.body);
        enteredKey = typeof parsed.securityKey === "string" ? parsed.securityKey.trim() : "";
      } catch (_) {
        enteredKey = req.body.trim();
      }
    }

    if (!enteredKey) {
      res.status(400).json({
        success: false,
        reason: "EMPTY_KEY",
        message: "Please enter the security key."
      });
      return;
    }

    // Read Firestore configuration: app_config/maintenance_access
    const docRef = db.collection("app_config").doc("maintenance_access");
    const docSnap = await docRef.get();

    if (!docSnap.exists) {
      res.status(404).json({
        success: false,
        reason: "DISABLED",
        message: "Developer access is disabled."
      });
      return;
    }

    const config = docSnap.data() || {};
    const isEnabled = config.enabled === true;

    if (!isEnabled) {
      res.status(403).json({
        success: false,
        reason: "DISABLED",
        message: "Developer access is disabled."
      });
      return;
    }

    // Validate using timing-safe SHA-256 hash
    const storedHash = typeof config.keyHash === "string" ? config.keyHash.trim().toLowerCase() : "";
    const candidateHash = sha256Hex(enteredKey);

    // Support fallback in case admin stored plaintext "key" field directly in Firestore
    const plaintextFallback = typeof config.key === "string" && config.key.trim().length > 0
      ? sha256Hex(config.key.trim())
      : "";

    const isMatch = (storedHash.length > 0 && safeCompare(candidateHash, storedHash)) ||
      (plaintextFallback.length > 0 && safeCompare(candidateHash, plaintextFallback));

    if (isMatch) {
      // Clear rate limiting counter upon successful authentication
      rateLimitMap.delete(clientIp);
      res.status(200).json({
        success: true,
        message: "Developer access verified successfully."
      });
    } else {
      // Record failure for IP rate limiting
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

      res.status(401).json({
        success: false,
        reason: "INVALID_KEY",
        message: "Invalid security key."
      });
    }
  } catch (error) {
    // Note: Do NOT log the entered key or confidential headers
    console.error("verifyMaintenanceAccessKey server error:", error.message || error);
    res.status(500).json({
      success: false,
      reason: "SERVER_ERROR",
      message: "Unable to verify key. Please try again."
    });
  }
});
