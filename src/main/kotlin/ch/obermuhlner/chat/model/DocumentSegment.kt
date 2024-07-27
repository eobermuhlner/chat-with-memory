package ch.obermuhlner.chat.model

data class DocumentSegment(
    var documentId: Long,
    var index: Int,
    var text: String
)