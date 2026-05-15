package com.example.thingsfire.DataModels

data class Channel(
    val created_at: String? = null,
    val description: String? = null,
    val field1: String? = null,
    val field2: String? = null,
    val field3: String? = null,
    val field4: String? = null,
    val field5: String? = null,
    val field6: String? = null,
    val field7: String? = null,
    val field8: String? = null,
    val id: Int? = null,
    val last_entry_id: Int? = null,
    val latitude: String? = null,
    val longitude: String? = null,
    val name: String? = null,
    val updated_at: String? = null
)

fun Channel.labelForField(fieldNumber: Int): String {
    val configuredLabel = when (fieldNumber) {
        1 -> field1
        2 -> field2
        3 -> field3
        4 -> field4
        5 -> field5
        6 -> field6
        7 -> field7
        8 -> field8
        else -> null
    }

    return configuredLabel?.takeIf { it.isNotBlank() } ?: "Field $fieldNumber"
}
