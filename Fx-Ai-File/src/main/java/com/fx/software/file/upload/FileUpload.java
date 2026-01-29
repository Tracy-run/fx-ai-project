package com.fx.software.file.upload;

import com.fx.software.core.web.ResponseWrapper;
import com.fx.software.file.excep.FileOpException;
import com.fx.software.file.service.MinioService;
import com.fx.software.file.vo.BpmAttachment;
import com.fx.software.file.vo.FileInfo;
import com.fx.software.file.vo.ResultCode;
import io.minio.MinioClient;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.List;

/**
 * @FileName FileUpload
 * @Description
 * @Author fx
 * @date 2026-01-17
 */
@RestController
@Slf4j
public class FileUpload {

    public static String BUCKET_NAME = "filebucket";

    @Autowired
    MinioService minioService;

    @Autowired
    MinioClient minioClient;

    /**
     * 上传文件到minio
     * @param request
     * @param fileInfo
     * @return
     */
    @PostMapping("/api/file/uploadToServer")
    public ResponseWrapper<List<BpmAttachment>> uploadToServer(MultipartHttpServletRequest request,
                                                               @Valid
                                                               @ApiParam(required = true, value = "表类型对象") FileInfo fileInfo) {

        try {
            log.info(String.valueOf(System.currentTimeMillis()));
            List<BpmAttachment> attachmentList = this.minioService.uploadToServer(BUCKET_NAME, request.getMultiFileMap().get("file"), fileInfo);
            log.info(String.valueOf(System.currentTimeMillis()));
            return new ResponseWrapper.Builder<List<BpmAttachment>>().code(ResultCode.SUCCESS.value()).data(attachmentList).build();
        } catch (FileOpException e){
            log.error("uploadToServer:", e);
            return new ResponseWrapper.Builder<List<BpmAttachment>>().code(ResultCode.FAILURE.value()).message(e.getMessage()).build();
        } catch (Exception e) {
            log.error("uploadToServer:", e);
            return new ResponseWrapper.Builder<List<BpmAttachment>>().code(ResultCode.FAILURE.value()).message(e.getMessage()).build();
        }
    }


    @GetMapping(value = "/file/downloadById")
    public void downloadById(
            @RequestParam(value = "id", required = true) String id, @RequestParam(required = false, defaultValue = "false") Boolean thumbnail, HttpServletResponse response) throws IOException {
        try {
            minioService.downloadById(BUCKET_NAME, id,thumbnail, response);
        } catch (Exception e) {
            log.error("downloadById:", e);
        }
    }


    @DeleteMapping("/api/file/deleteFile")
    public ResponseWrapper<String> uploadToServer(@RequestParam("id") String id) {
        try {
            this.minioService.deleteFileById(BUCKET_NAME, id);
            return new ResponseWrapper.Builder<String>().code(ResultCode.SUCCESS.value()).build();
        } catch (Exception e) {
            log.error("deleteFile:", e);
            return new ResponseWrapper.Builder<String>().code(ResultCode.FAILURE.value()).data(e.getMessage()).build();
        }
    }

    @PostMapping(value = "/file/download4Zip")
    public void download4Zip(
            @RequestBody List<FileInfo> fileInfos,  HttpServletResponse response) throws IOException {
        try {
            minioService.download4Zip(BUCKET_NAME, fileInfos, response);
        } catch (Exception e) {
            log.error("download4Zip:", e);
        }
    }


}
