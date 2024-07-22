package ch.obermuhlner.chat.controller

import ch.obermuhlner.chat.model.Tool
import ch.obermuhlner.chat.service.ToolService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/tools")
class ToolController(
    private val toolService: ToolService
) {

    @GetMapping
    fun getAllTools(): List<Tool> {
        return toolService.getAllTools()
    }
}