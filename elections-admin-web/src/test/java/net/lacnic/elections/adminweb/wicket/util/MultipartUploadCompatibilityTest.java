package net.lacnic.elections.adminweb.wicket.util;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import javax.imageio.ImageIO;

import org.apache.commons.fileupload2.core.DiskFileItem;
import org.apache.commons.fileupload2.core.DiskFileItemFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.apache.wicket.markup.html.form.upload.FileUpload;

import net.lacnic.elections.adminweb.validators.CandidatePictureUploadValidator;

class MultipartUploadCompatibilityTest {

    @TempDir
    Path uploadDirectory;

    @Test
    void multipartUploadCanCreateAndReadDiskBackedItem() throws Exception {
        DiskFileItemFactory factory = DiskFileItemFactory.builder()
                .setPath(uploadDirectory).setBufferSize(1).get();
        DiskFileItem item = factory.fileItemBuilder().setFieldName("organizations")
                .setFileName("organizations.xlsx").get();
        byte[] content = "multipart upload regression".getBytes(StandardCharsets.UTF_8);
        try {
            try (OutputStream output = item.getOutputStream()) {
                output.write(content);
            }
            assertArrayEquals(content, item.get());
        } finally {
            item.delete();
        }
    }

    @Test
    void wicketUploadRejectsTextDisguisedAsJpeg() throws Exception {
        DiskFileItem item = upload("photo.jpg", "not an image".getBytes(StandardCharsets.UTF_8));
        try {
            var result = CandidatePictureUploadValidator.validateAndBuildForCandidate(new FileUpload(item));
            assertFalse(result.isValid());
            assertEquals(CandidatePictureUploadValidator.FailureReason.INVALID_FORMAT, result.getFailureReason());
        } finally {
            item.delete();
        }
    }

    @Test
    void wicketUploadProcessesPngIntoCandidateJpeg() throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB), "png", bytes);
        DiskFileItem item = upload("photo.PNG", bytes.toByteArray());
        try {
            var result = CandidatePictureUploadValidator.validateAndBuildForCandidate(new FileUpload(item));
            assertTrue(result.isValid());
            assertEquals("jpg", result.getPictureExtension());
            BufferedImage processed = ImageIO.read(new ByteArrayInputStream(result.getPictureInfo()));
            assertTrue(processed.getWidth() <= 400);
            assertTrue(processed.getHeight() <= 400);
        } finally {
            item.delete();
        }
    }

    private DiskFileItem upload(String name, byte[] bytes) throws Exception {
        DiskFileItem item = DiskFileItemFactory.builder().setPath(uploadDirectory).setBufferSize(1).get()
                .fileItemBuilder().setFieldName("photo").setFileName(name).get();
        try (OutputStream output = item.getOutputStream()) {
            output.write(bytes);
        }
        return item;
    }
}
