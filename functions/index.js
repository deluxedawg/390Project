const functions = require("firebase-functions");
const admin = require("firebase-admin");

admin.initializeApp();

/**
 * Fires whenever a new chat message is written to
 * conversations/{conversationId}/messages/{messageId}, looks up the *other*
 * participant's stored FCM token (profiles/{uid}.fcmToken, written by the
 * Android client in ProfileManager.updateFcmToken), and pushes a data-only
 * message so the client's PushService can build the notification and deep
 * link into the right ChatActivity thread.
 */
exports.sendMessageNotification = functions.firestore
    .document("conversations/{conversationId}/messages/{messageId}")
    .onCreate(async (snap, context) => {
      const message = snap.data();
      const conversationId = context.params.conversationId;

      const conversationSnap = await admin.firestore()
          .collection("conversations")
          .doc(conversationId)
          .get();
      const conversation = conversationSnap.data();
      if (!conversation || !Array.isArray(conversation.participants)) return null;

      const recipientUid = conversation.participants.find(
          (uid) => uid !== message.senderUid);
      if (!recipientUid) return null;

      const recipientProfileSnap = await admin.firestore()
          .collection("profiles")
          .doc(recipientUid)
          .get();
      const recipientProfile = recipientProfileSnap.data();
      const token = recipientProfile && recipientProfile.fcmToken;
      if (!token) return null;

      const avatars = conversation.avatars || {};

      return admin.messaging().send({
        token,
        data: {
          otherUid: message.senderUid,
          otherUsername: message.senderUsername || "",
          otherAvatarId: String(avatars[message.senderUid] || 0),
          text: message.text || "",
        },
        android: {priority: "high"},
      });
    });
