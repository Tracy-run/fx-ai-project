package com.fx.software.file.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import net.coobird.thumbnailator.Thumbnails;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Base64;
import java.util.Locale;

/**
 * @FileName ImageUtil
 * @Description
 * @Author fx
 * @date 2026-01-29
 */
@Slf4j
public class ImageUtil {

    /**
     * 按尺寸原比例缩放图片
     *
     * @param source 输入源
     * @param output 输出源
     * @param width  256
     * @param height 256
     * @throws IOException
     */
    public static void imgThumb(String source, String output, int width, int height) throws IOException {
        Thumbnails.of(source).size(width, height).toFile(output);
    }

    /**
     * 按照比例进行缩放
     *
     * @param source 输入源
     * @param output 输出源
     * @param scale  比例
     * @throws IOException
     */
    public static void imgScale(String source, String output, double scale) throws IOException {
        Thumbnails.of(source).scale(scale).toFile(output);
    }

    /**
     * 按照比例和规格压缩图片得到base64图片字符串
     *
     * @param maxSize 单位kb
     * @param w
     * @param h
     * @return
     */
    public static String resizeImage(String filePath, int maxSize, int w, int h) {
        String base64 = null;
        BufferedImage src = null;
        BufferedImage src2 = null;
        BufferedImage src3 = null;
        try {
            String imageFormat = filePath.substring(filePath.lastIndexOf(".") + 1, filePath.length());
            src = fileToBufferedImage(filePath);
            int size = src.getData().getDataBuffer().getSize();

            if (size > maxSize * 10000) {

                src2 = Thumbnails.of(src).scale(0.6f).asBufferedImage();
                base64 = imageToBase64(src2, imageFormat);
                if (base64.length() - base64.length() / 8 * 2 > maxSize * 1000) {
                    src3 = Thumbnails.of(src2).size(w, h).asBufferedImage();
                    base64 = imageToBase64(src3, imageFormat);
                }
            } else {
                base64 = imageToBase64(src, imageFormat);
            }

        } catch (Exception e) {
            log.error("图片转换base64异常", e);
        } finally {
            if(src != null) {
                src.getGraphics().dispose();
            }
            if(src2 != null) {
                src2.getGraphics().dispose();
            }
            if(src3 != null) {
                src3.getGraphics().dispose();
            }
        }
        if (TextUtil.isNull(base64)) {
            base64 = getImageStr(filePath,false);
        }
        String imageType = getImageType(filePath.substring(filePath.lastIndexOf(".")));
        return imageType + base64;


    }

    public static String resizeImage(InputStream inputStream, int maxSize, int w, int h, String imageFormat) {
        String base64 = null;
        BufferedImage src = null;
        BufferedImage src1 = null;
        BufferedImage src2 = null;
        try {
//            ImageIO.setUseCache(false);
            src = ImageIO.read(inputStream);
            int size = src.getData().getDataBuffer().getSize();

            if (size > maxSize * 10000) {

                src1 = Thumbnails.of(src).scale(0.6f).asBufferedImage();
                base64 = imageToBase64(src1, imageFormat);
                if (base64.length() - base64.length() / 8 * 2 > maxSize * 1000) {

                    src2 = Thumbnails.of(src1).size(w, h).asBufferedImage();
                    base64 = imageToBase64(src2, imageFormat);
                }
            } else {
                base64 = imageToBase64(src, imageFormat);
            }


            if (!StringUtils.startsWith(imageFormat, ".")) {
                imageFormat = "."+imageFormat;
            }
            String imageType = getImageType(imageFormat);
            return imageType + base64;
        } catch (Exception e) {
            log.error("图片转换base64异常", e);
            if (!StringUtils.startsWith(imageFormat, ".")) {
                imageFormat = "."+imageFormat;
            }
            String imageType = getImageType(imageFormat);
            return imageType + base64;
        } finally {
            if(src != null) {
                src.getGraphics().dispose();
            }
            if(src1 != null) {
                src1.getGraphics().dispose();
            }
            if(src2 != null) {
                src2.getGraphics().dispose();
            }
        }
//        if (TextUtil.isNull(base64)) {
//            base64 = getImageStr(filePath,false);
//        }



    }

    public static String getImageStr(String imgFilePath , Boolean isImageType) {
        InputStream in = null;
        // 将图片文件转化为字节数组字符串，并对其进行Base64编码处理
        // 读取图片字节数组
        try {
            File file = new File(imgFilePath);
            String name = file.getName();
            String suffix = name.substring(name.lastIndexOf("."));
            in = new FileInputStream(imgFilePath);
            byte[] data = new byte[in.available()];
            in.read(data);
            // 对字节数组Base64编码
//            BASE64Encoder encoder = new BASE64Encoder();
//            return encoder.encode(data);//返回字符串
            Base64.Encoder encoder = Base64.getEncoder();
            String encodeToString = encoder.encodeToString(data);
            String imageType = ImageUtil.getImageType(suffix);
            if (isImageType) {
                return imageType + encodeToString;
            } else {
                return encodeToString;
            }
        } catch (IOException e) {
            log.error("图片", e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    log.error("getImageStr:", e);
                }
            }
        }
        return null;
    }

    public static String getImageType(String filetype) {
        switch (filetype.toLowerCase(Locale.ENGLISH)) {
            case ".png":
                return "data:image/png;base64,";
            case ".jpg":
                return "data:image/jpeg;base64,";
            case ".gif":
                return "data:image/gif;base64,";
            case ".svg":
                return "data:image/svg+xml;base64,";
            case ".ico":
                return "data:image/x-icon;base64,";
            case ".bmp":
                return "data:image/bmp;base64,";
            case ".jpeg":
                return "data:image/jpeg;base64,";
            default:
                return "data:image/png;base64,";
        }
    }

    /**
     * 图片文件转BufferedImage
     *
     * @param filePath
     * @return
     * @throws Exception
     */
    public static BufferedImage fileToBufferedImage(String filePath) throws Exception {
        FileInputStream is = null;
        try {
            is = new FileInputStream(filePath);
            BufferedImage img = ImageIO.read(is);
            return img;
        } catch (Exception e) {
            throw e;
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    log.error("downloadLocalFile:", e);
                }
            }
        }

    }

    /**
     * 将图片base64字符串转换为BufferedImage
     *
     * @param base64string
     * @return
     */
    public static BufferedImage base64String2BufferedImage(String base64string) {
        BufferedImage image = null;
        try {
            InputStream stream = base64StringToInputStream(base64string);
            image = ImageIO.read(stream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }

    /**
     * 将base64字符转换为输入流
     *
     * @param base64string
     * @return
     */
    private static InputStream base64StringToInputStream(String base64string) {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(base64string.getBytes());
        InputStream inputStream = byteArrayInputStream;
        return inputStream;
    }

    /**
     * 将BufferedImage转换为base64字符串
     *
     * @param bufferedImage
     * @return
     */
    public static String imageToBase64(BufferedImage bufferedImage, String imageFormat) {
        Base64.Encoder encoder = Base64.getEncoder();

        try(ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(bufferedImage, imageFormat, baos);
            return new String(encoder.encode((baos.toByteArray())));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
