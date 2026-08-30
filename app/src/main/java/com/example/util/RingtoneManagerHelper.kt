package com.example.util

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

data class ImportedRingtone(
    val id: String,
    val name: String,
    val uriString: String,
    val dateAdded: Long = System.currentTimeMillis()
)

object RingtoneManagerHelper {

    private const val PREFS_NAME = "imported_ringtones_prefs"
    private const val KEY_RINGTONES_JSON = "key_imported_ringtones_list"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Retrieves the persistent list of all user-imported ringtones.
     */
    fun getImportedRingtones(context: Context): List<ImportedRingtone> {
        val prefs = getPrefs(context)
        val jsonStr = prefs.getString(KEY_RINGTONES_JSON, null) ?: return emptyList()
        val list = mutableListOf<ImportedRingtone>()
        try {
            val jsonArray = JSONArray(jsonStr)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val uriStr = obj.optString("uriString", "")
                val uri = Uri.parse(uriStr)
                // If it's a file URI, verify the file still exists
                if (uri.scheme == "file") {
                    val file = File(uri.path ?: "")
                    if (!file.exists()) continue
                }
                list.add(
                    ImportedRingtone(
                        id = obj.optString("id", System.currentTimeMillis().toString()),
                        name = obj.optString("name", "Custom Ringtone"),
                        uriString = uriStr,
                        dateAdded = obj.optLong("dateAdded", System.currentTimeMillis())
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    /**
     * Adds an imported ringtone to persistent storage.
     */
    fun addImportedRingtone(context: Context, name: String, uriString: String): ImportedRingtone {
        val current = getImportedRingtones(context).toMutableList()
        // Check if already exists by uri
        val existing = current.find { it.uriString == uriString }
        if (existing != null) {
            return existing
        }

        val newRingtone = ImportedRingtone(
            id = "ringtone_${System.currentTimeMillis()}",
            name = name,
            uriString = uriString,
            dateAdded = System.currentTimeMillis()
        )
        current.add(0, newRingtone)
        saveList(context, current)
        return newRingtone
    }

    /**
     * Removes an imported ringtone from persistent storage and deletes local cached file.
     */
    fun deleteImportedRingtone(context: Context, ringtoneId: String) {
        val current = getImportedRingtones(context).toMutableList()
        val item = current.find { it.id == ringtoneId }
        if (item != null) {
            current.remove(item)
            saveList(context, current)
            try {
                val uri = Uri.parse(item.uriString)
                if (uri.scheme == "file") {
                    File(uri.path ?: "").delete()
                }
            } catch (_: Exception) {}
        }
    }

    private fun saveList(context: Context, list: List<ImportedRingtone>) {
        val jsonArray = JSONArray()
        for (item in list) {
            val obj = JSONObject().apply {
                put("id", item.id)
                put("name", item.name)
                put("uriString", item.uriString)
                put("dateAdded", item.dateAdded)
            }
            jsonArray.put(obj)
        }
        getPrefs(context).edit().putString(KEY_RINGTONES_JSON, jsonArray.toString()).apply()
    }
}
