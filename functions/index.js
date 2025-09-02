const functions = require("firebase-functions");
const admin = require("firebase-admin");

admin.initializeApp();

exports.sendCallNotification = functions.https.onCall(async (data, context) => {
  const recipientId = data.recipientId;
  const channelName = data.channelName;
  const senderId = context.auth.uid;

  const senderDoc = await admin.firestore().collection("users").doc(senderId).get();
  if (!senderDoc.exists) {
    throw new functions.https.HttpsError("not-found", "Профиль звонящего не найден.");
  }
  const senderName = senderDoc.data().name || "Новый пользователь";

  const recipientDoc = await admin.firestore().collection("users").doc(recipientId).get();
  if (!recipientDoc.exists) {
    throw new functions.https.HttpsError("not-found", "Профиль получателя не найден.");
  }
  const fcmToken = recipientDoc.data().fcmToken;

  if (!fcmToken) {
    console.log("У пользователя нет FCM токена. Невозможно отправить уведомление.");
    return { success: false, reason: "Нет токена." };
  }

  const payload = {
    token: fcmToken,
    data: {
      type: "incoming_call",
      sender_id: senderId,
      sender_name: senderName,
      channel_name: channelName,
    },
    android: {
      priority: "high",
    },
  };

  try {
    console.log(`Отправка уведомления на токен: ${fcmToken}`);
    await admin.messaging().send(payload);
    console.log("Уведомление успешно отправлено.");
    return { success: true };
  } catch (error) {
    console.error("Ошибка при отправке уведомления:", error);
    throw new functions.https.HttpsError("internal", "Не удалось отправить уведомление.");
  }
});