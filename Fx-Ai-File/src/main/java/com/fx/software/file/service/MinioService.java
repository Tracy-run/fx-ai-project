package com.fx.software.file.service;

import com.fx.software.file.config.FileMimeConfiguration;
import com.fx.software.file.excep.FileOpException;
import com.fx.software.file.utils.DateUtil;
import com.fx.software.file.utils.FileUtils;
import com.fx.software.file.utils.ImageUtil;
import com.fx.software.file.utils.TextUtil;
import com.fx.software.file.vo.BpmAttachment;
import com.fx.software.file.vo.FileInfo;
import io.minio.*;
import io.minio.errors.*;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.lang3.StringUtils;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @FileName MinioService
 * @Description
 * @Author fx
 * @date 2026-01-29
 */
@Service
@Slf4j
public class MinioService {

    private static final int BUFFER_SIZE = 2 * 1024;

    private static Pattern FilePattern = Pattern.compile("[\\\\/:*?\"<>|]");

    @Value("${file.mime.check:false}")
    String fileMimeCheck;

    @Autowired
    private MinioClient minioClient;

    @Autowired
    FileMimeConfiguration fileMimeConfiguration;

    /**
     * 文件上传
     *
     * @param bucketName
     * @param multipartFileList
     */
    @Transactional(rollbackFor = {Exception.class})
    public List<BpmAttachment> uploadToServer(String bucketName, List<MultipartFile> multipartFileList, FileInfo fileInfo) throws IOException, NoSuchAlgorithmException, InsufficientDataException, InternalException, InvalidResponseException, InvalidKeyException, XmlParserException, ErrorResponseException, ServerException, FileOpException {

        //Optional<LoginUserinfo> optionalLoginUserinfo = authClientFeign.getUserinfo();

        List<BpmAttachment> attachmentList = new ArrayList<>();
        makeBucket(bucketName);
//        InputStream inputStream = null;
//        BufferedInputStream bufferedInputStream = null;
        for (MultipartFile multipartFile : multipartFileList) {

            String suffix = FileUtils.getSuffix(multipartFile.getOriginalFilename());
            Tika tika = new Tika();

            String id = FileUtils.getId();
            log.info(multipartFile.getContentType() + "开始上传====" + System.currentTimeMillis());

            try {
                log.info("tika==" + tika.detect(multipartFile.getInputStream()));
                if ("true".equals(fileMimeCheck)) {
                    Map<String, String> fileMimeMap = fileMimeConfiguration.getFileMimeMap();
                    if (!fileMimeMap.containsKey(suffix)) {
                        throw new FileOpException("文件后缀不合法");
                    } else {
                        String mimeType = fileMimeMap.get(suffix);
                        if (TextUtil.isNotNull(mimeType)) {

                            String contentType = tika.detect(multipartFile.getInputStream());
                            log.info("contentType======" + contentType);
                            if (contentType != null && !mimeType.contains(contentType)) {
                                throw new FileOpException("文件后缀与文件类型不一致");
                            }
                        }
                    }
                }

                minioClient.putObject(PutObjectArgs.builder().bucket(bucketName).object(multipartFile.getOriginalFilename() + "-" + id).stream(multipartFile.getInputStream(), multipartFile.getSize(), 50 * 1024 * 1024).build());

                log.info("上传结束====" + System.currentTimeMillis());
                BpmAttachment bpmAttachment = new BpmAttachment();
                bpmAttachment.setId(id);
                bpmAttachment.setFileName(multipartFile.getOriginalFilename());
                bpmAttachment.setFileType(suffix);
                bpmAttachment.setFileSize(multipartFile.getSize() + "");
                bpmAttachment.setFilePath(multipartFile.getOriginalFilename() + "-" + id);
                bpmAttachment.setCreateTime(new Date());
//                if (optionalLoginUserinfo.isPresent()) {
//                    LoginUserinfo loginUserinfo = optionalLoginUserinfo.get();
//                    bpmAttachment.setCreatorId(loginUserinfo.getUseraccount());
//                    bpmAttachment.setCreatorName(loginUserinfo.getName());
//                }

                bpmAttachment.setCreatorIp(fileInfo.getCreatorIp());
                bpmAttachment.setFlowId(fileInfo.getFlowId());
                bpmAttachment.setFlowNo(fileInfo.getFlowNo());
                bpmAttachment.setFlowTitle(fileInfo.getFlowTitle());
                bpmAttachment.setFlowType(fileInfo.getFlowType());
                bpmAttachment.setFlowTypeid(fileInfo.getFlowTypeid());
                bpmAttachment.setProcessinstid(fileInfo.getProcessinstid());
                bpmAttachment.setProcessdefname(fileInfo.getProcessdefname());
                bpmAttachment.setProcesschname(fileInfo.getProcesschname());
                bpmAttachment.setVersion(fileInfo.getVersion());
                //workitemid
                bpmAttachment.setTaskid(fileInfo.getTaskid());
                //环节英文名
                bpmAttachment.setTaskdefname(fileInfo.getTaskdefname());
                //环节中文名
                bpmAttachment.setTaskchname(fileInfo.getTaskchname());
                bpmAttachment.setProvinceName(fileInfo.getProvinceName());
                bpmAttachment.setTaskscence(fileInfo.getTaskscence());


                bpmAttachment.setDownloadCount(0);
                bpmAttachment.setSaveType("minio");
                bpmAttachment.setFileDesc(fileInfo.getFileDesc());

                bpmAttachment.setCreateTime(new Date());

                bpmAttachment.setQueryFilter(fileInfo.getQueryFilter());
                //bpmAttachmentMapper.insert(bpmAttachment);

                log.info("保存数据====" + System.currentTimeMillis());
                if ("jpg".equalsIgnoreCase(suffix) || "png".equalsIgnoreCase(suffix) || "jpeg".equalsIgnoreCase(suffix) || "svg".equalsIgnoreCase(suffix) || "bmp".equalsIgnoreCase(suffix) || "gif".equalsIgnoreCase(suffix)) {
                    final String base64Str = ImageUtil.resizeImage(multipartFile.getInputStream(), 50, 256, 256, suffix);
                    bpmAttachment.setImageBase64(base64Str);
                }
                attachmentList.add(bpmAttachment);
            } catch (IOException e) {
                throw e;
            }


        }
        return attachmentList;
    }


    /**
     * 创建存储桶
     *
     * @param bucketName 存储桶名称
     */
    public boolean makeBucket(String bucketName) throws IOException, InvalidKeyException, InvalidResponseException, InsufficientDataException, NoSuchAlgorithmException, InternalException, XmlParserException, ErrorResponseException, ServerException {
        boolean flag = bucketExists(bucketName);
        if (!flag) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            return true;
        } else {
            return false;
        }
    }

    public boolean bucketExists(String bucketName) throws IOException, InvalidKeyException, InvalidResponseException, InsufficientDataException, NoSuchAlgorithmException, InternalException, XmlParserException, ErrorResponseException, ServerException {
        boolean flag = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        return flag;
    }




    /**
     * 删除文件
     *
     * @param bucketName
     * @param id
     * @throws IOException
     * @throws InvalidKeyException
     * @throws InvalidResponseException
     * @throws InsufficientDataException
     * @throws NoSuchAlgorithmException
     * @throws ServerException
     * @throws InternalException
     * @throws XmlParserException
     * @throws ErrorResponseException
     */
    public void deleteFileById(String bucketName, String id) throws IOException, InvalidKeyException, InvalidResponseException, InsufficientDataException, NoSuchAlgorithmException, ServerException, InternalException, XmlParserException, ErrorResponseException {

        BpmAttachment bpmAttachment = null; // bpmAttachmentMapper.selectById(id);
        if (bpmAttachment != null) {
            String filePath = bpmAttachment.getFilePath();

            RemoveObjectArgs removeObjectArgs = RemoveObjectArgs.builder().bucket(bucketName).object(filePath).build();
            this.minioClient.removeObject(removeObjectArgs);
            //this.bpmAttachmentMapper.deleteById(id);
        }
    }





    public void download4Zip(String bucketName, List<FileInfo> fileInfos, HttpServletResponse response) throws IOException, ErrorResponseException {
        if (!CollectionUtils.isEmpty(fileInfos)) {
            ZipOutputStream zos = new ZipOutputStream(response.getOutputStream());
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode("文件打包-" + DateUtil.getCurDate() + ".zip", "utf-8"));
            fileInfos.stream().forEach(fileInfo -> {
                BpmAttachment bpmAttachment = null;//bpmAttachmentMapper.selectById(fileInfo.getId());
                InputStream fileObjectInputStream = null;
                try {
                    fileObjectInputStream = this.minioClient.getObject(GetObjectArgs.builder().bucket(bucketName).object(bpmAttachment.getFilePath()).build());
                    String fileName = getFileName(fileInfo, bpmAttachment);
                    zos.putNextEntry(new ZipEntry(fileName));
                    byte[] buf = new byte[BUFFER_SIZE];
                    int len;
                    while ((len = fileObjectInputStream.read(buf)) != -1) {
                        zos.write(buf, 0, len);
                    }

                    zos.closeEntry();
                } catch (Exception e) {
                    log.error("加载图片异常", e);
                } finally {
                    if (fileObjectInputStream != null) {
                        try {
                            fileObjectInputStream.close();
                        } catch (IOException e) {
                            log.error("download4Zip", e);
                        }
                    }
                }
            });
            zos.flush();
            zos.close();
        }
    }

    private String getFileName(FileInfo fileInfo, BpmAttachment bpmAttachment) throws UnsupportedEncodingException {
        String fileName="";
        if (StringUtils.isNotEmpty(fileInfo.getFileName())) {
            fileName = fileInfo.getFileName();
        } else {
            fileName = bpmAttachment.getFileName();
        }
        if (!fileName.endsWith("." + bpmAttachment.getFileType())) {
            fileName += "." + bpmAttachment.getFileType();
        }

        return FilePattern.matcher(fileName).replaceAll("");
    }



    public void downloadById(String bucketName, String id, Boolean thumbnail, HttpServletResponse response) throws IOException, InvalidKeyException, InvalidResponseException, InsufficientDataException, NoSuchAlgorithmException, ServerException, InternalException, XmlParserException, ErrorResponseException {
        BpmAttachment bpmAttachment = null; // bpmAttachmentMapper.selectById(id);
        response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(bpmAttachment.getFileName(), "utf-8"));
        String objectName = bpmAttachment.getFilePath();
        InputStream fileObjectInputStream = this.minioClient.getObject(GetObjectArgs.builder().bucket(bucketName).object(objectName).build());
        OutputStream outputStream = null;
        try {
            outputStream = response.getOutputStream();
            if (thumbnail) {
                Thumbnails.of(fileObjectInputStream).scale(0.25).toOutputStream(outputStream);
            } else {
                byte[] buff = new byte[1024];

                int i = fileObjectInputStream.read(buff);
                while (i != -1) {
                    outputStream.write(buff, 0, i);
                    outputStream.flush();
                    i = fileObjectInputStream.read(buff);
                }
            }
        } catch (IOException e) {
            throw e;
        } finally {
            if (fileObjectInputStream != null ) {
                fileObjectInputStream.close();
            }
            if (outputStream != null) {
                outputStream.close();
            }
        }

    }




}
