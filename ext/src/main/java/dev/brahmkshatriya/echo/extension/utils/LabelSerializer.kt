package dev.brahmkshatriya.echo.extension.utils

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.JsonObject

object LabelSerializer : KSerializer<String?> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("LabelNameAsString", PrimitiveKind.STRING).let {
            it.copy(isNullable = true)
        }

    override fun deserialize(decoder: Decoder): String? {
        return when (decoder) {
            is JsonDecoder -> {
                val element = decoder.decodeJsonElement()
                when (element) {
                    is JsonPrimitive -> element.contentOrNull
                    is JsonObject -> element["name"]?.jsonPrimitive?.contentOrNull
                    else -> null
                }
            }
            else -> {
                try {
                    decoder.decodeString()
                } catch (e: Exception) {
                    null
                }
            }
        }
    }

    override fun serialize(encoder: Encoder, value: String?) {
        if (value != null) {
            encoder.encodeString(value)
        } else {
            encoder.encodeNull()
        }
    }
}