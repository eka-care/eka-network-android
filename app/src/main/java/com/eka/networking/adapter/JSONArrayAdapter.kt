package com.eka.networking.adapter

import com.eka.networking.logger.Logger
import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.ToJson
import okio.Buffer
import org.json.JSONArray

internal object JSONArrayAdapter {

    @FromJson
    fun fromJson(reader: JsonReader): JSONArray = runCatching {
        JSONArray(reader.readJsonValue() as List<*>)
    }.onFailure(Logger::e).getOrDefault(JSONArray())

    @ToJson
    fun toJson(writer: JsonWriter, value: JSONArray?) {
        value?.let { writer.value(Buffer().writeUtf8(value.toString())) }
    }
}