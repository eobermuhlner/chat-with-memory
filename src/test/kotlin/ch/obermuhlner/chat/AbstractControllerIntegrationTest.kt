package ch.obermuhlner.chat

import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.*
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringJUnitConfig
class AbstractControllerIntegrationTest {

    @Autowired
    protected lateinit var restTemplate: TestRestTemplate

    @LocalServerPort
    protected var port: Int = 0

    protected fun <B, T> requestPost(url: String, body: B, responseType: Class<T>, headers: HttpHeaders = HttpHeaders()): ResponseEntity<T> {
        return restTemplate.exchange("http://localhost:$port$url", HttpMethod.POST, HttpEntity(body, headers), responseType)
    }

    protected fun <B, T> requestPut(url: String, body: B, responseType: Class<T>, headers: HttpHeaders = HttpHeaders()): ResponseEntity<T> {
        return restTemplate.exchange("http://localhost:$port$url", HttpMethod.PUT, HttpEntity(body, headers), responseType)
    }

    protected fun <T> requestGet(url: String, responseType: Class<T>, headers: HttpHeaders = HttpHeaders()): ResponseEntity<T> {
        return restTemplate.exchange("http://localhost:$port$url", HttpMethod.GET, HttpEntity<Unit>(headers), responseType)
    }

    protected fun <T> requestDelete(url: String, responseType: Class<T>, headers: HttpHeaders = HttpHeaders()): ResponseEntity<T> {
        return restTemplate.exchange("http://localhost:$port$url", HttpMethod.DELETE, HttpEntity<Unit>(headers), responseType)
    }

    protected fun <T> requestPostMultipart(url: String, parts: Map<String, Any>, responseType: Class<T>, headers: HttpHeaders = HttpHeaders()): ResponseEntity<T> {
        val body: MultiValueMap<String, Any> = LinkedMultiValueMap()
        parts.forEach { (key, value) ->
            if (value is MockMultipartFile) {
                val fileResource = object : ByteArrayResource(value.bytes) {
                    override fun getFilename(): String {
                        return value.originalFilename ?: "file"
                    }
                }
                val fileHeaders = HttpHeaders()
                fileHeaders.contentType = value.contentType?.let { MediaType.parseMediaType(it) } ?: MediaType.MULTIPART_FORM_DATA
                val fileHttpEntity = HttpEntity(fileResource, fileHeaders)
                body.add(key, fileHttpEntity)
            } else {
                body.add(key, value)
            }
        }
        headers.contentType = MediaType.MULTIPART_FORM_DATA
        return restTemplate.exchange("http://localhost:$port$url", HttpMethod.POST, HttpEntity(body, headers), responseType)
    }

    protected fun <T> requestPutMultipart(url: String, parts: Map<String, Any>, responseType: Class<T>, headers: HttpHeaders = HttpHeaders()): ResponseEntity<T> {
        val body: MultiValueMap<String, Any> = LinkedMultiValueMap()
        parts.forEach { (key, value) ->
            if (value is MockMultipartFile) {
                val fileHeaders = HttpHeaders()
                fileHeaders.contentType = MediaType.MULTIPART_FORM_DATA
                val fileHttpEntity = HttpEntity(value.bytes, fileHeaders)
                body.add(key, fileHttpEntity)
            } else {
                body.add(key, value)
            }
        }
        headers.contentType = MediaType.MULTIPART_FORM_DATA
        return restTemplate.exchange("http://localhost:$port$url", HttpMethod.PUT, HttpEntity(body, headers), responseType)
    }
}
