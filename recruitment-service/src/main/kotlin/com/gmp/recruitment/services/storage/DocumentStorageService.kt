package com.gmp.recruitment.services.storage

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.util.UUID
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.Resource
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

interface DocumentStorageService {
    fun store(file: MultipartFile): String
    fun read(storageKey: String): Resource
}

@Service
class LocalDocumentStorageService(
    @Value("\${app.storage.base-dir}") private val baseDir: String,
) : DocumentStorageService {
    override fun store(file: MultipartFile): String {
        val directory = Path.of(baseDir)
        Files.createDirectories(directory)
        val extension = file.originalFilename?.substringAfterLast('.', "")?.lowercase()?.takeIf { it.isNotBlank() } ?: "bin"
        val storageKey = UUID.randomUUID().toString() + "." + extension
        val destination = directory.resolve(storageKey)
        file.inputStream.use { input ->
            Files.copy(input, destination, StandardCopyOption.REPLACE_EXISTING)
        }
        return storageKey
    }

    override fun read(storageKey: String): Resource {
        val resource = FileSystemResource(Path.of(baseDir).resolve(storageKey))
        require(resource.exists()) { "Stored document not found for key=$storageKey" }
        return resource
    }
}
