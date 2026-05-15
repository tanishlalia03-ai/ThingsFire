package com.example.thingsfire.DataModels

data class FeedX(
    val created_at: String,
    val entry_id: Int,
    val field1: String? = null,
    val field2: String? = null,
    val field3: String? = null,
    val field4: String? = null,
    val field5: String? = null,
    val field6: String? = null,
    val field7: String? = null,
    val field8: String? = null
)

fun FeedX.valueForField(fieldNumber: Int): String? {
    val value = when (fieldNumber) {
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

    return value?.takeIf { it.isNotBlank() }
}
