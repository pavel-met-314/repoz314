package presentation.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import domain.model.User
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    suspend fun login(email: String, password: String): User {
        auth.signInWithEmailAndPassword(email, password).await()
        val uid = auth.currentUser?.uid ?: error("UID не найден")
        val doc = db.collection("users").document(uid).get().await()
        return doc.toObject(User::class.java) ?: error("Пользователь не найден в базе")
    }

    suspend fun register(email: String, password: String, name: String, phone: String): User {
        auth.createUserWithEmailAndPassword(email, password).await()
        val uid = auth.currentUser?.uid ?: error("UID не найден")
        val user = User(
            id = uid,
            email = email,
            name = name,
            phone = phone,
            role = "client",
            createdAt = System.currentTimeMillis()
        )
        db.collection("users").document(uid).set(user).await()
        return user
    }

    fun getCurrentUserId(): String? = auth.currentUser?.uid

    suspend fun getCurrentUser(): User? {
        val uid = auth.currentUser?.uid ?: return null
        val doc = db.collection("users").document(uid).get().await()
        return doc.toObject(User::class.java)
    }

    fun isLoggedIn(): Boolean = auth.currentUser != null

    fun logout() = auth.signOut()
}

