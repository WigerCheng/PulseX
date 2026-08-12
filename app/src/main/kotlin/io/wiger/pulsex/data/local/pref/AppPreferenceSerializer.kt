package io.wiger.pulsex.data.local.pref

import androidx.datastore.core.Serializer
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

object AppPreferenceSerializer : Serializer<AppPreference> {
    override val defaultValue: AppPreference
        get() = AppPreference()

    override suspend fun readFrom(input: InputStream): AppPreference = runCatching {
        Json.decodeFromString(
            deserializer = AppPreference.serializer(),
            string = input.readBytes().decodeToString()
        )
    }.getOrElse { AppPreference() }

    override suspend fun writeTo(
        t: AppPreference,
        output: OutputStream,
    ) {
        output.write(
            Json.encodeToString(AppPreference.serializer(), t)
                .encodeToByteArray()
        )
    }
}
