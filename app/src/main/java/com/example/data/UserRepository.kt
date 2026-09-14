package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * Repository layer contract for managing User data stored in Firebase Firestore.
 * Provides observable state flows for real-time Firestore synchronization,
 * one-shot queries, user updates, and user profile management.
 */
interface UserRepository {

    /**
     * Real-time observable list of actual user records stored in Firebase Firestore.
     * Starts empty and is populated directly from Firestore collections.
     */
    val users: StateFlow<List<User>>

    /**
     * Observable state representing the currently authenticated and synced Firestore user profile.
     */
    val currentUser: StateFlow<User?>

    /**
     * Indicates whether an active fetch, sync, or mutation operation is executing against Firestore.
     */
    val isLoading: StateFlow<Boolean>

    /**
     * Holds the latest error message if an operation against Firestore failed, or null if healthy.
     */
    val error: StateFlow<String?>

    /**
     * Performs a one-shot fetch of all users from the Firestore "users" collection.
     */
    suspend fun fetchUsers(): Result<List<User>>

    /**
     * Fetches a specific user document by UID from Firestore.
     */
    suspend fun getUserById(uid: String): Result<User?>

    /**
     * Returns a real-time reactive Flow observing a specific user document in Firestore.
     */
    fun observeUser(uid: String): Flow<User?>

    /**
     * Persists or merges a User entity into the Firestore "users" collection.
     */
    suspend fun saveUser(user: User): Result<Unit>

    /**
     * Updates specific key-value fields of a user document in Firestore.
     */
    suspend fun updateUserFields(uid: String, fields: Map<String, Any?>): Result<Unit>

    /**
     * Queries Firestore users matching the search query across display names and bios.
     */
    suspend fun searchUsers(query: String): List<User>

    /**
     * Sets the local active user session and syncs the profile from/to Firestore.
     */
    fun setCurrentUser(user: User?)

    /**
     * Explicitly refreshes the users cache from Firestore.
     */
    suspend fun refreshUsers()
}
