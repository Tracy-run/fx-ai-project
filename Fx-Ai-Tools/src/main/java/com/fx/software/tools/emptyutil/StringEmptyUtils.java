package com.fx.software.tools.emptyutil;

/**
 * @FileName StringEmptyUtils
 * @Description
 * @Author fx
 * @date 2026-01-17
 */
public class StringEmptyUtils {

    public static boolean multiVerifyEmpty(Long ... objs){
        for (Long obj : objs) {
            if(null == obj || "null".equals(obj.toString()) || "".equals(obj.toString())){
                return true;
            }
        }
        return false;
    }
    public static boolean multiVerifyEmpty(String ... objs){
        for (String obj : objs) {
            if(null == obj || "null".equals(obj) || "".equals(obj)){
                return true;
            }
        }
        return false;
    }
}
