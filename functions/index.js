const { onDocumentUpdated } = require("firebase-functions/v2/firestore");
const { initializeApp } = require("firebase-admin/app");
const { getFirestore } = require("firebase-admin/firestore");
const { getMessaging } = require("firebase-admin/messaging");

initializeApp();

/**
 * Отправляет push клиенту, когда админ отменяет запись.
 */
exports.notifyAppointmentCancelled = onDocumentUpdated(
  "appointments/{appointmentId}",
  async (event) => {
    const before = event.data.before.data();
    const after = event.data.after.data();
    if (!before || !after) return;
    if (before.status === after.status || after.status !== "cancelled") return;

    const clientId = after.clientId;
    if (!clientId) return;

    const userDoc = await getFirestore().collection("users").doc(clientId).get();
    const fcmToken = userDoc.data()?.fcmToken;
    if (!fcmToken) return;

    await getMessaging().send({
      token: fcmToken,
      notification: {
        title: "Запись отменена",
        body: `Запись на ${after.date} в ${after.time} (${after.serviceName}) отменена.`,
      },
      data: {
        type: "appointment_cancelled",
        appointmentId: event.params.appointmentId,
      },
    });
  }
);
