package com.hngy.siae.ai.util;

import lombok.extern.slf4j.Slf4j;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;

/**
 * 图片压缩工具类
 * 用于压缩发送给 Ollama 的图片，避免请求体过大导致处理缓慢
 */
@Slf4j
public class ImageCompressor {

    // 最大宽度/高度（像素）- 降低到 512 加快处理速度
    private static final int MAX_DIMENSION = 512;
    
    // JPEG 压缩质量 (0.0 - 1.0)
    private static final float JPEG_QUALITY = 0.6f;
    
    // 最大文件大小（字节）- 200KB，小模型处理更快
    private static final int MAX_SIZE_BYTES = 200 * 1024;
    
    // 触发压缩的阈值 - 50KB 以上就压缩
    private static final int COMPRESS_THRESHOLD = 50 * 1024;

    /**
     * 压缩图片
     * @param imageBytes 原始图片字节数组
     * @return 压缩后的图片字节数组
     */
    public static byte[] compress(byte[] imageBytes) {
        if (imageBytes == null || imageBytes.length == 0) {
            return imageBytes;
        }

        int originalSize = imageBytes.length;
        log.info("Original image size: {} bytes ({} KB)", originalSize, originalSize / 1024);

        // 只有非常小的图片才跳过压缩
        if (originalSize <= COMPRESS_THRESHOLD) {
            log.info("Image is small enough, skipping compression");
            return imageBytes;
        }

        try {
            // 读取图片
            BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(imageBytes));
            if (originalImage == null) {
                log.warn("Failed to read image, returning original");
                return imageBytes;
            }

            int width = originalImage.getWidth();
            int height = originalImage.getHeight();
            log.info("Original image dimensions: {}x{}", width, height);

            // 计算缩放比例
            double scale = 1.0;
            if (width > MAX_DIMENSION || height > MAX_DIMENSION) {
                scale = Math.min((double) MAX_DIMENSION / width, (double) MAX_DIMENSION / height);
            }

            // 缩放图片
            BufferedImage resizedImage;
            if (scale < 1.0) {
                int newWidth = (int) (width * scale);
                int newHeight = (int) (height * scale);
                log.info("Resizing image to: {}x{}", newWidth, newHeight);
                resizedImage = resize(originalImage, newWidth, newHeight);
            } else {
                resizedImage = originalImage;
            }

            // 压缩为 JPEG
            byte[] compressedBytes = compressToJpeg(resizedImage, JPEG_QUALITY);
            
            // 如果还是太大，继续降低质量
            float quality = JPEG_QUALITY;
            while (compressedBytes.length > MAX_SIZE_BYTES && quality > 0.2f) {
                quality -= 0.1f;
                log.info("Image still too large ({}KB), reducing quality to {}", 
                        compressedBytes.length / 1024, quality);
                compressedBytes = compressToJpeg(resizedImage, quality);
            }
            
            // 如果质量降到最低还是太大，进一步缩小尺寸
            if (compressedBytes.length > MAX_SIZE_BYTES && resizedImage.getWidth() > 256) {
                log.info("Still too large, resizing to 256px");
                resizedImage = resize(resizedImage, 256, 
                        (int)(256.0 * resizedImage.getHeight() / resizedImage.getWidth()));
                compressedBytes = compressToJpeg(resizedImage, 0.5f);
            }

            log.info("Compressed image size: {} bytes ({} KB), compression ratio: {:.1f}%", 
                    compressedBytes.length, 
                    compressedBytes.length / 1024,
                    (1 - (double) compressedBytes.length / originalSize) * 100);

            return compressedBytes;

        } catch (IOException e) {
            log.error("Failed to compress image", e);
            return imageBytes;
        }
    }

    /**
     * 缩放图片
     */
    private static BufferedImage resize(BufferedImage original, int newWidth, int newHeight) {
        BufferedImage resized = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resized.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // 填充白色背景（处理透明图片）
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, newWidth, newHeight);
        
        g.drawImage(original, 0, 0, newWidth, newHeight, null);
        g.dispose();
        return resized;
    }

    /**
     * 压缩为 JPEG 格式
     */
    private static byte[] compressToJpeg(BufferedImage image, float quality) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpeg");
        if (!writers.hasNext()) {
            throw new IOException("No JPEG writer found");
        }
        
        ImageWriter writer = writers.next();
        try (ImageOutputStream ios = ImageIO.createImageOutputStream(baos)) {
            writer.setOutput(ios);
            
            ImageWriteParam param = writer.getDefaultWriteParam();
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(quality);
            
            writer.write(null, new IIOImage(image, null, null), param);
        } finally {
            writer.dispose();
        }
        
        return baos.toByteArray();
    }
}
