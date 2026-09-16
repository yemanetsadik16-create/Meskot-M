package com.example.data

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Concrete implementation of [UserRepository] backed by Firebase Firestore.
 * Fetches, synchronizes, and persists real user documents from the Firestore "users" collection.
 */
class FirestoreUserRepository(
    private val firestoreProvider: () -> FirebaseFirestore? = { FirebaseManager.firestore }
) : UserRepository {

    companion object {
        private const val TAG = "FirestoreUserRepo"
        const val COLLECTION_USERS = FirebaseManager.COL_USERS
    }

    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _users = MutableStateFlow<List<User>>(emptyList())
    override val users: StateFlow<List<User>> = _users.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    override val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    override val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    override val error: StateFlow<String?> = _error.asStateFlow()

    private var usersListenerRegistration: ListenerRegistration? = null
    private var currentUserListenerRegistration: ListenerRegistration? = null

    init {
        startRealtimeUserSync()
    }

    /**
     * Attaches a real-time listener to Firestore's "users" collection to stream
     * actual user documents directly into [users].
     */
    private fun startRealtimeUserSync() {
        val db = firestoreProvider()
        if (db == null) {
            Log.w(TAG, "Firestore not initialized yet; will retry on explicit fetch or initialization")
            return
        }

        _isLoading.value = true
        usersListenerRegistration?.remove()
        try {
            usersListenerRegistration = db.collection(COLLECTION_USERS)
                .addSnapshotListener { snapshot, error ->
                    _isLoading.value = false
                    if (error != null) {
                        Log.e(TAG, "Error streaming users from Firestore: ${error.message}", error)
                        _error.value = error.localizedMessage
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        val parsedUsers = snapshot.documents.mapNotNull { doc ->
                            doc.data?.let { parseUserDocument(doc.id, it) }
                        }
                        _users.value = parsedUsers
                        _error.value = null
                        Log.d(TAG, "Streamed ${parsedUsers.size} actual users from Firestore collection '$COLLECTION_USERS'")

                        // Sync active user if present
                        val activeUid = _currentUser.value?.uid
                        if (activeUid != null) {
                            val matching = parsedUsers.find { it.uid == activeUid }
                            if (matching != null && matching != _currentUser.value) {
                                _currentUser.value = matching
                            }
                        }
                    }
                }
        } catch (e: Exception) {
            _isLoading.value = false
            _error.value = e.localizedMessage
            Log.e(TAG, "Failed to attach Firestore snapshot listener: ${e.message}", e)
        }
    }

    override suspend fun fetchUsers(): Result<List<User>> {
        val db = firestoreProvider() ?: return Result.failure(IllegalStateException("Firestore is not available"))
        _isLoading.value = true
        return try {
            val snapshot = db.collection(COLLECTION_USERS).get().awaitTask()
            val userList = snapshot.documents.mapNotNull { doc ->
                doc.data?.let { parseUserDocument(doc.id, it) }
            }
            _users.value = userList
            _isLoading.value = false
            _error.value = null
            Log.d(TAG, "Successfully fetched ${userList.size} actual users from Firestore")
            Result.success(userList)
        } catch (e: Exception) {
            _isLoading.value = false
            _error.value = e.localizedMessage
            Log.e(TAG, "Failed to fetch users from Firestore: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun getUserById(uid: String): Result<User?> {
        if (uid.isBlank()) return Result.success(null)

        // Check in-memory state first
        val cached = _users.value.find { it.uid == uid }
        if (cached != null) return Result.success(cached)

        val db = firestoreProvider() ?: return Result.failure(IllegalStateException("Firestore is not available"))
        return try {
            val doc = db.collection(COLLECTION_USERS).document(uid).get().awaitTask()
            if (doc != null && doc.exists()) {
                val user = doc.data?.let { parseUserDocument(doc.id, it) }
                Result.success(user)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get user $uid from Firestore: ${e.message}", e)
            Result.failure(e)
        }
    }

    override fun observeUser(uid: String): Flow<User?> = callbackFlow {
        val db = firestoreProvider()
        if (db == null || uid.isBlank()) {
            trySend(null)
            close()
            return@callbackFlow
        }

        val registration = db.collection(COLLECTION_USERS).document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error observing user $uid: ${error.message}")
                    trySend(null)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val user = snapshot.data?.let { parseUserDocument(snapshot.id, it) }
                    trySend(user)
                } else {
                    trySend(null)
                }
            }

        awaitClose {
            registration.remove()
        }
    }

    override suspend fun saveUser(user: User): Result<Unit> {
        val db = firestoreProvider() ?: return Result.failure(IllegalStateException("Firestore is not available"))
        return try {
            val data = userToMap(user)
            db.collection(COLLECTION_USERS).document(user.uid)
                .set(data, SetOptions.merge())
                .awaitTask()

            // Update in-memory state immediately
            val existing = _users.value.toMutableList()
            val index = existing.indexOfFirst { it.uid == user.uid }
            if (index >= 0) {
                existing[index] = user
            } else {
                existing.add(user)
            }
            _users.value = existing

            if (_currentUser.value?.uid == user.uid) {
                _currentUser.value = user
            }

            Log.d(TAG, "Successfully persisted user ${user.uid} (${user.displayName}) to Firestore")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save user ${user.uid} to Firestore: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun updateUserFields(uid: String, fields: Map<String, Any?>): Result<Unit> {
        val db = firestoreProvider() ?: return Result.failure(IllegalStateException("Firestore is not available"))
        return try {
            db.collection(COLLECTION_USERS).document(uid)
                .update(fields)
                .awaitTask()

            // Refresh the specific document
            val refreshed = getUserById(uid).getOrNull()
            if (refreshed != null) {
                _users.value = _users.value.map { if (it.uid == uid) refreshed else it }
                if (_currentUser.value?.uid == uid) {
                    _currentUser.value = refreshed
                }
            }

            Log.d(TAG, "Successfully updated fields for user $uid in Firestore")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update user fields for $uid in Firestore: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun searchUsers(query: String): List<User> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return _users.value

        // Search local Firestore cache first
        val matchingLocal = _users.value.filter {
            it.displayName.contains(trimmed, ignoreCase = true) ||
                it.bio.contains(trimmed, ignoreCase = true) ||
                it.profession.contains(trimmed, ignoreCase = true) ||
                it.location.contains(trimmed, ignoreCase = true)
        }

        if (matchingLocal.isNotEmpty()) {
            return matchingLocal
        }

        // Query Firestore by displayName
        val db = firestoreProvider() ?: return emptyList()
        return try {
            val snapshot = db.collection(COLLECTION_USERS)
                .whereGreaterThanOrEqualTo("displayName", trimmed)
                .whereLessThanOrEqualTo("displayName", trimmed + "\uf8ff")
                .limit(20)
                .get()
                .awaitTask()

            snapshot.documents.mapNotNull { doc ->
                doc.data?.let { parseUserDocument(doc.id, it) }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Firestore search query failed, using local match: ${e.message}")
            matchingLocal
        }
    }

    override fun setCurrentUser(user: User?) {
        _currentUser.value = user
        currentUserListenerRegistration?.remove()

        if (user != null) {
            // Check if present in users list; if not, add locally, otherwise update in-place
            if (_users.value.none { it.uid == user.uid }) {
                _users.value = _users.value + user
            } else {
                _users.value = _users.value.map { if (it.uid == user.uid) user else it }
            }

            // Listen for changes to current user document in Firestore
            val db = firestoreProvider()
            if (db != null) {
                currentUserListenerRegistration = db.collection(COLLECTION_USERS).document(user.uid)
                    .addSnapshotListener { doc, err ->
                        if (err == null && doc != null && doc.exists()) {
                            val live = doc.data?.let { parseUserDocument(doc.id, it) }
                            if (live != null) {
                                _currentUser.value = live
                                _users.value = _users.value.map { if (it.uid == live.uid) live else it }
                            }
                        }
                    }
            }

            // Sync with Firestore in background
            repositoryScope.launch {
                saveUser(user)
            }
        }
    }

    override suspend fun refreshUsers() {
        startRealtimeUserSync()
        fetchUsers()
    }

    /**
     * Parses a raw Firestore document snapshot map into a strongly-typed [User] object.
     * Extracts all 29 fields without fallback to mock profiles.
     */
    private fun sanitizeMock(str: String?, mockVal: String): String {
        val s = str?.trim() ?: ""
        return if (s.equals(mockVal.trim(), ignoreCase = true)) "" else s
    }

    fun parseUserDocument(uid: String, d: Map<String, Any?>): User {
        val rawFollowers = (d["followersCount"] as? Number)?.toInt() ?: 0
        val rawFollowing = (d["followingCount"] as? Number)?.toInt() ?: 0
        val rawWatchHours = (d["watchHours"] as? Number)?.toDouble() ?: 0.0

        // Real Meskot counts: discard legacy mock seeds (8500, 3700, 3420) and use real metrics
        val safeFollowers = if (rawFollowers == 8500) 0 else rawFollowers
        val safeFollowing = if (rawFollowing == 3700 || rawFollowing == 370) 0 else rawFollowing
        val safeWatchHours = if (rawWatchHours == 3420.0) 0.0 else rawWatchHours

        return User(
            uid = uid,
            displayName = d["displayName"] as? String ?: "User",
            email = d["email"] as? String ?: "",
            bio = sanitizeMock(d["bio"] as? String, "Engineer is a problem solver"),
            photoUrl = d["photoUrl"] as? String ?: "",
            coverPhotoUrl = d["coverPhotoUrl"] as? String ?: "",
            isAdmin = d["isAdmin"] as? Boolean ?: false,
            isSuspended = d["isSuspended"] as? Boolean ?: false,
            lastSeen = (d["lastSeen"] as? Number)?.toLong() ?: System.currentTimeMillis(),
            createdAt = (d["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
            gender = d["gender"] as? String ?: "",
            birthDate = sanitizeMock(d["birthDate"] as? String, "May 11, 1994"),
            phoneNumber = d["phoneNumber"] as? String ?: "",
            starBalance = (d["starBalance"] as? Number)?.toInt() ?: 0,
            creatorGrossEarnings = (d["creatorGrossEarnings"] as? Number)?.toDouble() ?: 0.0,
            creatorNetBalance = (d["creatorNetBalance"] as? Number)?.toDouble() ?: 0.0,
            vipMemberships = (d["vipMemberships"] as? Map<*, *>)?.mapNotNull { (k, v) ->
                if (k is String && v is String) k to v else null
            }?.toMap() ?: emptyMap(),
            followersCount = safeFollowers,
            followingCount = safeFollowing,
            profession = sanitizeMock(d["profession"] as? String, "Public figure"),
            location = sanitizeMock(d["location"] as? String, "Calgary, Alberta"),
            hometown = sanitizeMock(d["hometown"] as? String, "Calgary, Alberta"),
            workplace = sanitizeMock(d["workplace"] as? String, "Adigrat university _Engineering Sciences"),
            workRole = sanitizeMock(d["workRole"] as? String, "Civil Engineering"),
            education = sanitizeMock(d["education"] as? String, "Adigrat University"),
            educationClass = sanitizeMock(d["educationClass"] as? String, "Class of 2018"),
            watchHours = safeWatchHours,
            kycVerified = d["kycVerified"] as? Boolean ?: false,
            policyStrikes = (d["policyStrikes"] as? Number)?.toInt() ?: 0,
            payoutDestinationAccount = d["payoutDestinationAccount"] as? String ?: ""
        )
    }

    /**
     * Serializes all 29 fields of a [User] into a key-value Map suitable for Firestore persistence.
     */
    fun userToMap(u: User): Map<String, Any?> = mapOf(
        "uid" to u.uid,
        "displayName" to u.displayName,
        "email" to u.email,
        "bio" to u.bio,
        "photoUrl" to u.photoUrl,
        "coverPhotoUrl" to u.coverPhotoUrl,
        "isAdmin" to u.isAdmin,
        "isSuspended" to u.isSuspended,
        "lastSeen" to u.lastSeen,
        "createdAt" to u.createdAt,
        "gender" to u.gender,
        "birthDate" to u.birthDate,
        "phoneNumber" to u.phoneNumber,
        "starBalance" to u.starBalance,
        "creatorGrossEarnings" to u.creatorGrossEarnings,
        "creatorNetBalance" to u.creatorNetBalance,
        "vipMemberships" to u.vipMemberships,
        "followersCount" to u.followersCount,
        "followingCount" to u.followingCount,
        "profession" to u.profession,
        "location" to u.location,
        "hometown" to u.hometown,
        "workplace" to u.workplace,
        "workRole" to u.workRole,
        "education" to u.education,
        "educationClass" to u.educationClass,
        "watchHours" to u.watchHours,
        "kycVerified" to u.kycVerified,
        "policyStrikes" to u.policyStrikes,
        "payoutDestinationAccount" to u.payoutDestinationAccount
    )

    private suspend fun <T> Task<T>.awaitTask(): T =
        suspendCancellableCoroutine { cont ->
            addOnSuccessListener { result ->
                if (cont.isActive) cont.resume(result)
            }
            addOnFailureListener { exception ->
                if (cont.isActive) cont.resumeWith(Result.failure(exception))
            }
            addOnCanceledListener {
                if (cont.isActive) cont.cancel()
            }
        }
}
