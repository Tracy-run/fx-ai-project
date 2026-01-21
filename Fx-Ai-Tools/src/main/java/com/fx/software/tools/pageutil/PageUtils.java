package com.fx.software.tools.pageutil;

import com.fx.software.core.exception.PageException;
import lombok.extern.slf4j.Slf4j;

/**
 * @FileName PageUtils
 * @Description
 * @Author fx
 * @date 2026-01-17
 */
@Slf4j
public class PageUtils {

    /**
     * 每页条数
     */
    private Integer pageSize;

    /**
     * 当前页
     */
    private Integer currentPage;

    /**
     * 总条数
     */
    private Integer total;

    /**
     * 总页数
     */
    private Integer pageCount;

    public PageUtils(){

    }

    public PageUtils(Integer pageSize, Integer currentPage, Integer total) {
        this.pageSize = pageSize;
        this.currentPage = currentPage;
        this.total = total;
    }

    public Integer getPageCount() throws PageException {
        try {
            if (total != null) {
                if (total % pageSize == 0) {

                    this.pageCount = total / pageSize;
                    return pageCount;
                } else {
                    this.pageCount = total / pageSize + 1;
                    return pageCount;
                }
            } else {
                this.pageCount = 0;
                return pageCount;
            }
        } catch (Exception e) {
            log.error("getPageCount报错:",e);
            if (pageSize==null || pageSize==0){
                throw new PageException("pageSize参数未传递");
            }else{
                throw new PageException("分页信息封装报错");
            }
        }
    }
}
