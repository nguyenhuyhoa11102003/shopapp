package com.example.demo.utils;

import org.apache.catalina.util.StringUtil;
import org.apache.commons.io.FilenameUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;


import java.nio.file.Paths;
import java.nio.file.Files;
import java.io.IOException;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.UUID;

public class FileUtils {
	private static String UPLOADS_FOLDER = "uploads";


	public static boolean isImageFile(MultipartFile file) {
		return true;
	}

	public static String storeFile(MultipartFile file) throws IOException {

		if (!isImageFile(file) || file.getOriginalFilename() == null) {
			throw new IOException("Invalid image format");
		}

		String fileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
		String extension = FilenameUtils.getExtension(fileName);

		String uniqueFilename = UUID.randomUUID().toString() + "_" + System.nanoTime() + "." + extension;

		// Đường dẫn đến thư mục mà bạn muốn lưu file
		java.nio.file.Path uploadDir = Paths.get(UPLOADS_FOLDER);

		if (!java.nio.file.Files.exists(uploadDir)) {
			java.nio.file.Files.createDirectories(uploadDir);
		}

		// Đường dẫn đầy đủ đến file
		java.nio.file.Path destination = Paths.get(uploadDir.toString(), uniqueFilename);
		Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
		return uniqueFilename;
	}

	public static void deleteFile(String filename) throws IOException {
        // Đường dẫn đến thư mục chứa file
        java.nio.file.Path uploadDir = Paths.get(UPLOADS_FOLDER);
        // Đường dẫn đầy đủ đến file cần xóa
        java.nio.file.Path filePath = uploadDir.resolve(filename);

        // Kiểm tra xem file tồn tại hay không
        if (Files.exists(filePath)) {
            // Xóa file
            Files.delete(filePath);
        } else {
            //throw new FileNotFoundException("File not found: " + filename);
        }
    }
}
