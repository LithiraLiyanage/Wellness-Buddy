package com.example.wellnessapp.util

import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

/**
 * Utility class for JSON serialization and deserialization.
 */
object JsonUtils {
    
    val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    
    /**
     * Serializes an object to JSON string
     */
    inline fun <reified T> encodeToString(value: T): String {
        return try {
            json.encodeToString(value)
        } catch (e: Exception) {
            ""
        }
    }
    
    /**
     * Deserializes a JSON string to an object
     */
    inline fun <reified T> decodeFromString(value: String): T? {
        return try {
            if (value.isBlank()) null else json.decodeFromString<T>(value)
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Serializes a list to JSON string
     */
    inline fun <reified T> encodeListToString(list: List<T>): String {
        return encodeToString(list)
    }
    
    /**
     * Deserializes a JSON string to a list
     */
    inline fun <reified T> decodeListFromString(value: String): List<T> {
        return try {
            if (value.isBlank()) emptyList() else json.decodeFromString<List<T>>(value)
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * Serializes a map to JSON string
     */
    inline fun <reified K, reified V> encodeMapToString(map: Map<K, V>): String {
        return encodeToString(map)
    }
    
    /**
     * Deserializes a JSON string to a map
     */
    inline fun <reified K, reified V> decodeMapFromString(value: String): Map<K, V> {
        return try {
            if (value.isBlank()) emptyMap() else json.decodeFromString<Map<K, V>>(value)
        } catch (e: Exception) {
            emptyMap()
        }
    }
}
