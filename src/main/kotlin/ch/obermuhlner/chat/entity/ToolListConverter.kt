package ch.obermuhlner.chat.entity

import ch.obermuhlner.chat.model.Tool
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter
class ToolListConverter : AttributeConverter<List<Tool?>?, String?> {
    override fun convertToDatabaseColumn(tools: List<Tool?>?): String? {
        return tools
            ?.joinToString(SEPARATOR) {
                it?.name ?: ""
            }
    }

    override fun convertToEntityAttribute(dbData: String?): List<Tool?> {
        return dbData
            ?.split(SEPARATOR)
            ?.mapNotNull {
                try {
                    Tool.valueOf(it.trim())
                } catch (ex: Exception) {
                    null
                }
            }
            ?: emptyList()
    }

    companion object {
        private const val SEPARATOR = "|"
    }
}
