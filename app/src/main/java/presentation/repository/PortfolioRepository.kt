package presentation.repository

import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import domain.model.Portfolio
import kotlinx.coroutines.tasks.await

class PortfolioRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
) {
    suspend fun getPortfolio(): List<Portfolio> {
        val snapshot = db.collection("portfolio")
            .orderBy("uploadedAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get().await()
        return snapshot.documents.mapNotNull { it.toObject(Portfolio::class.java) }
    }

    suspend fun uploadPhoto(uri: Uri, serviceId: String?, caption: String): Portfolio {
        // Загружаем фото в Firebase Storage
        val fileName = "portfolio/${System.currentTimeMillis()}.jpg"
        val ref = storage.reference.child(fileName)
        ref.putFile(uri).await()
        val downloadUrl = ref.downloadUrl.await().toString()

        // Сохраняем запись в Firestore
        val docRef = db.collection("portfolio").document()
        val portfolio = Portfolio(
            id = docRef.id,
            imageUrl = downloadUrl,
            serviceId = serviceId,
            caption = caption,
            uploadedAt = System.currentTimeMillis()
        )
        docRef.set(portfolio).await()
        return portfolio
    }

    suspend fun deletePhoto(portfolio: Portfolio) {
        // Удаляем из Firestore
        db.collection("portfolio").document(portfolio.id).delete().await()
        // Удаляем из Storage если url содержит наш bucket
        if (portfolio.imageUrl.isNotEmpty()) {
            try {
                storage.getReferenceFromUrl(portfolio.imageUrl).delete().await()
            } catch (_: Exception) { /* игнорируем если файл уже удалён */ }
        }
    }
}

