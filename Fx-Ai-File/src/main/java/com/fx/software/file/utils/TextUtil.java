package com.fx.software.file.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.security.MessageDigest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @FileName TextUtil
 * @Description
 * @Author fx
 * @date 2026-01-29
 */
@Slf4j
public class TextUtil {

    public static final Pattern P = Pattern.compile("\n");

    public static final String NULL_STR = "null";
    /**
     * 对中文进行处理
     *
     * @param chinese
     *            可能包含中文的字符串
     * @return 处理后的字符串
     */
    public static String processChinese(String chinese) {

        try {
            byte[] byteArray = chinese.getBytes("GBK");
            chinese = new String(byteArray, "ISO8859_1");
        } catch (Exception e) {
            log.error("TextUtil.processChinese:编码转换出错",e);
        }
        return chinese;
    }

    public static String processChineseOther(String chinese) {

        try {
            byte[] byteArray = chinese.getBytes("ISO-8859-1");
            chinese = new String(byteArray, "GB2312");
        } catch (Exception e) {
            log.error("TextUtil.processChineseOther:编码转换出错",e);
        }
        return chinese;
    }

    public static String processGB2312ToUTF8(String chinese) {

        try {
            byte[] byteArray = chinese.getBytes("GB2312");
            chinese = new String(byteArray, "UTF-8");
        } catch (Exception e) {
            log.error("TextUtil.processGB2312ToUTF8:编码转换出错",e);
        }
        return chinese;
    }
    public static String processUTF8ToGB2312(String chinese) {

        try {
            byte[] byteArray = chinese.getBytes("UTF-8");
            chinese = new String(byteArray, "GB2312");
        } catch (Exception e) {
            log.error("TextUtil.processUTF8ToGB2312:编码转换出错",e);
        }
        return chinese;
    }

    public static String transNull(String input) {
        if (input == null){
            return "";
        }
        else {
            if (NULL_STR.equals(input)){
                return "";
            } else {
                return input;
            }
        }
    }

    /**
     * 转换中文为ISO表准编码
     */
    public static String processISO(String chinese) {

        try {
            byte[] byteArray = chinese.getBytes("GB2312");
            chinese = new String(byteArray, "ISO-8859-1");
        } catch (Exception e) {
            log.error("TextUtil.processISO:编码转换出错",e);
        }
        return chinese;
    }

    /**
     * 判断字符串是否不为空
     *
     * @param sstring
     *            判断的字符串
     * @return boolean :返回值为boolean
     */
    public static boolean isNotNull(final String sstring) {
        boolean bolret = true;
        if (isNull(sstring)) {
            bolret = false;
        }
        return bolret;
    }

    /**
     * 判断数字是否不为０
     *
     * @param iint
     *            判断的字符串
     * @return boolean :返回值为boolean
     */
    public static boolean isNotNull(int iint) {
        boolean bolret = true;
        if (iint <= 0) {
            bolret = false;
        }
        return bolret;
    }

    /**
     * 判断数字是否不为０
     *
     * @param iint
     *            判断的字符串
     * @return boolean :返回值为boolean
     */
    public static boolean isNotNull(Integer iint) {
        boolean bolret = true;
        if (iint == null) {
            bolret = false;
        }
        return bolret;
    }

    /**
     * 判断数字是否不为０
     *
     * @param iint
     *            判断的字符串
     * @return boolean :返回值为boolean
     */
    public static boolean isNotNull(Long iint) {
        boolean bolret = true;
        if(iint == null || iint.intValue() == 0) {
            bolret = false;
        }
        return bolret;
    }

    /**
     * 判断数字是否不为０
     *
     * @param iint
     *            判断的字符串
     * @return boolean :返回值为boolean
     */
    public static boolean isNull(Integer iint) {
        boolean ret = false;
        if (iint == null) {
            ret = true;
        }

        return ret;
    }

    /**
     * 判断对象是否为null
     *
     * @param obj 判断的字符串
     * @return boolean :返回值为boolean
     */

    public static boolean isNull(Object obj) {
        boolean ret = false;
        if (obj == null) {
            ret = true;
        }

        return ret;
    }

    /**
     * 判断字符串是否不为空
     *
     * @param sstring
     *            判断的字符串
     * @return boolean :返回值为boolean
     */
    public static boolean isNull(final String sstring) {
        boolean bolret = false;
        String nullStr = "null";
        if (sstring == null || nullStr.equals(sstring.trim())
                || "".equals(sstring.trim())) {
            bolret = true;
        }
        return bolret;
    }

    /**
     * 判断列表是否为空
     *
     * @param lst 判断的列表
     * @return boolean :返回值为boolean
     */

    public static boolean isNull(List lst) {
        boolean bolret = false;
        if (lst == null) {
            bolret = true;
        }
        if (lst != null) {
            if (lst.size() == 0){
                bolret = true;
            }
        }
        return bolret;
    }

    /**
     * 判断列表是否为空
     * @param str 字符串
     * @return 非null字符串
     */
    public static String  isStrNotNull(String str){

        if (null==str || NULL_STR.equals(str)
                || "".equals(str.trim())) {
            return "";
        }
        return str.trim();
    }

    public static boolean isNotNull(List lst) {
        boolean bolret = false;
        if (lst != null) {
            if (lst.size() > 0){
                bolret = true;
            }
        }
        return bolret;
    }

    /**
     * 判断数字是否不为０
     *
     * @param iint
     *            判断的字符串
     * @return boolean :返回值为boolean
     */
    public static boolean isNull(int iint) {
        boolean bolret = false;
        if (iint > 0) {
            bolret = true;
        }
        return bolret;
    }

    /**
     * 判断字符串是否为空,并赋上默认值
     *
     * @param sstring
     *            判断的字符串
     * @param sdefault
     *            默认值
     * @return String :返回值为String
     */
    public static String replaceNullWith(String sstring, String sdefault) {
        String sret = null;
        if (isNotNull(sstring)) {
            sret = sstring;
        } else {
            sret = sdefault;
        }

        return sret;
    }

    /**
     * 替换一段文字中的某个字符
     *
     * @param content
     *            一段文字
     * @param oldWord
     *            要替换的字符
     * @param newWord
     *            替换后的字符
     * @return 替换后的文字
     */
    public static String replace(String content, String oldWord, String newWord) {
        String tempString = content;
        int position = tempString.indexOf(oldWord);
        while (position > -1) {
            tempString = tempString.substring(0, position) + newWord
                    + tempString.substring(position + oldWord.length());
            position = tempString.indexOf(oldWord, position + newWord.length());
        }
        return tempString;
    }

    /**
     * 替换空格和回车，对<table /table>中的html内容不做处理
     *
     * @param lsContent
     *            原内容文本
     * @return 处理后的内容文本
     */
    public static String escapeHtmlTag(String lsContent) {
        if (lsContent == null || lsContent.length() == 0) {
            return lsContent;
        }
        int liLen = lsContent.length();
        int i = 0;
        String lsNewcon = "";
        String lsToken = "";

        int theTable = 0;
        for (i = 0; i < liLen; i++) {
            lsToken = "";
            char lc = lsContent.charAt(i);
            if (lc == '<') {
                String lsTemp = lsContent.substring(i, i + 6);

                if ("<TABLE".equalsIgnoreCase(lsTemp)) {
                    theTable = theTable + 1;
                }
                if ("</TABL".equalsIgnoreCase(lsTemp)) {
                    theTable = theTable - 1;
                }

            }
            if (theTable > 0) {
                lsNewcon = lsNewcon + lsContent.charAt(i);
                continue;
            }

            lsToken = replaceSpecied(lc, lsToken);

            lsNewcon = lsNewcon + lsToken;
        }
        return lsNewcon;
    }

    private static String replaceSpecied(char lc,String lsToken){
        char blank = ' ';
        char rChar = '\r';
        char tChar = '\t';
        char nChar = '\n';
        if (lc == blank) {
            lsToken = lsToken + "&nbsp;";
        } else if (lc == rChar) {
            lsToken = lsToken + "<br>";
        } else if (lc == tChar) {
            lsToken = lsToken + "&nbsp;&nbsp;";
        } else if (lc == nChar) {
            lsToken = lsToken + "&nbsp;";
        } else {
            lsToken = lsToken + lc;
        }

        return lsToken;
    }
    private static long UIDCounter = System.currentTimeMillis();

    /**
     * 产生唯一的ID
     * @return 唯一id
     */
    public static synchronized String generateUID() {
        TextUtil.UIDCounter++;
        return String.valueOf(System.currentTimeMillis())
                + String.valueOf(UIDCounter);
    }

    /**
     * 在指定的字符串前加0以达到指定的长度
     *
     * @param id
     *            待检验字符串
     * @param leng
     *            填充长度
     * @return 经过填充后的字符串
     * @deprecated use the pad(String ,int length)
     */
    public static String addZero(int id, int leng) {
        StringBuilder sid = new StringBuilder(id);
        if (sid.length() != leng) {
            int pack = leng - sid.length();
            for (int i = 0; i < pack; i++) {
                sid = new StringBuilder("0").append(sid);
            }
            return sid.toString();
        } else {
            return sid.toString();
        }
    }


    /**
     * 去处时间后面的0
     *
     * @param timestamp
     * @return
     */
    public static String formatTime(String timestamp) {
        int num = 19;
        if (isNotNull(timestamp) && timestamp.length() > num){
            return timestamp.substring(0, timestamp.length() - 2);
        }
        return timestamp;
    }

    /**
     * 压缩输入字符中的重复的数据
     *
     * @param inputstr
     * @return outstr change log: 1.created and fix the bug 2003-12 Towncarl
     */
    public static String compress(String inputstr, String delimiter) {
        StringBuffer outstr = new StringBuffer();
        String[] arrtemp = inputstr.split(delimiter);
        int length = arrtemp.length;
        for (int i = 0; i < length; i++) {
            if (arrtemp[i] != "") {
                outstr.append(arrtemp[i] + delimiter);
                for (int j = i + 1; j < length; j++) {
                    if (arrtemp[i].equals(arrtemp[j])) {
                        arrtemp[j] = "";
                    }
                }
            }
        }
        if (!inputstr.endsWith(delimiter)){
            return outstr.substring(0, outstr.length() - 1);
        } else {
            return outstr.toString();
        }
    }

    /**
     * 加密字符串
     *
     * @param pwd
     *            要加密的字符串
     * @return 加密后的32位字符串
     */
    public static String encode(String pwd) {
        byte[] buf = pwd.getBytes();
        StringBuffer hexString = new StringBuffer();
        try {
            MessageDigest algorithm = MessageDigest.getInstance("MD5");
            algorithm.reset();
            algorithm.update(buf);
            byte[] digest = algorithm.digest();
            for (int i = 0; i < digest.length; i++) {
                hexString.append(pad(Integer.toHexString(0xFF & digest[i]), 2));
            }
        } catch (Exception e) {
            log.error("TextUtil.encode:编码出错",e);
        }
        return hexString.toString();
    }

    /**
     * 带签名加密字符串
     *
     * @param userid
     *            签名
     * @param pwd
     *            要加密的字符串
     * @return 加密后的32位字符串
     */
    public static String encodeWithKey(String userid, String pwd) {
        byte[] buf = pwd.getBytes();
        byte[] key = userid.getBytes();
        StringBuffer hexString = new StringBuffer();
        try {
            MessageDigest algorithm = MessageDigest.getInstance("MD5");
            algorithm.reset();
            algorithm.update(buf);
            byte[] digest = algorithm.digest(key);
            for (int i = 0; i < digest.length; i++) {
                hexString.append(pad(Integer.toHexString(0xFF & digest[i]), 2));
            }
        } catch (Exception e) {
            log.error("TextUtil.encodeWithKey:带签名加密字符串出错",e);
        }
        return hexString.toString();
    }

    /**
     * 在字符串之前补0
     *
     * @param i
     * @param l
     * @return
     */
    public static String pad(String i, int l) {
        while (i.length() < l) {
            i = '0' + i;
        }
        return i;
    }

    /**
     * 1
     *
     * @author Towncarl 2003-12-6
     */
    /**
     * 2进制字符串到10进制的转换
     *
     * @param binstr
     *            可以是以逗号分隔"1,1,1,1,1"或"11111"
     * @return int 转换后的10进制数据
     */
    public static int binstrToInt(String binstr) {
        String temp = binstr;
        char separator = ',';
        if (temp.indexOf(separator) != -1) {
            temp = temp.replaceAll(",", "");
        }
        return Integer.valueOf(temp, 2).intValue();
    }

    /**
     * int to binstr
     */
    public static String intToBinstr(int i) {
        return pad(Integer.toBinaryString(i), 7);
    }

    /**
     * 把一个单一字符转换为以,号分割的字符串
     *
     * @param csv
     * @return
     */
    public static String stringToCSS(String csv) {
        StringBuffer sboutstr = new StringBuffer();
        char[] tempca = csv.toCharArray();
        int length = tempca.length;
        if (length > 0){
            sboutstr.append(tempca[0]);
        }
        for (int i = 1; i < length; i++){
            sboutstr.append("," + tempca[i]);
        }
        return sboutstr.toString();
    }

    /**
     * 比较二个对象值是否相等，传入的二个对象可以为null.
     *
     * @param fromObj
     *            比较对象
     * @param toObj
     *            被比较的对象
     * @return boolean 比较结果.
     */
    public static boolean compareObj(Object fromObj, Object toObj) {
        boolean result = false;
        if (fromObj == null) {
            if (toObj == null) {
                result = true;
            }
        } else {
            if (toObj != null) {
                result = fromObj.equals(toObj) ? true : false;
            }
        }

        return result;
    }

    /**
     * 取得将给定字符串中的<b>\n</b>全部转换成<b>&lt;br&gt;</b>,并且去掉所有的<b>\r</b>的字符串.
     *
     * @param in 字符串参数
     * @return String 经过处理后的字符串.
     */
    public static String formatHtml(String in) {
        if (in == null){
            return null;
        }
        char[] c = { 13, 10 };
        String strOut = "";
        String brstr = new String(c);
        String[] strs = in.split(brstr);
        for (int i = 0; i < strs.length; i++) {
            strOut += strs[i];
            if (i < strs.length - 1){
                strOut += "<br>";
            }
        }
        // 将空格的情况也做了处理，该方法的名称可能更改一下更好
        strOut = strOut.replaceAll("\\s+$", "");
        strOut = strOut.replaceAll("\\s", "&nbsp;");
        return strOut;
    }

    /**
     * 创建目录
     *
     * @param dir
     * @return
     */
    public static boolean createDir(String dir) {
        boolean isSuccess = false;
        File dirFile = new File(dir);
        try {
            if (!dirFile.exists()) {
                isSuccess = dirFile.mkdirs();
            } else {
                isSuccess = true;
            }
        } catch (Exception e) {
            log.info("create dir is failure!!!");
        }
        return isSuccess;
    }

    /**
     * 去除字符串中的空格、回车、换行符、制表符
     * @param str
     * @return
     */
    public static String replaceBlank(String str) {
        String dest = "";
        if (str!=null) {

            Matcher m = P.matcher(str);
            dest = m.replaceAll("");
        }
        return dest;
    }

    public static String toString(Object obj) {
        if (obj==null){
            return null;
        }else{
            return String.valueOf(obj);
        }
    }

    public static String transListToString(List list, String seprator) {

        StringBuilder str = new StringBuilder();
        for (int i=0;i<list.size();i++) {
            Object value = list.get(i);
            if (value != null) {
                str.append(toString(value));
                if (i<list.size()-1) {
                    str.append(seprator);
                }
            }
        }
        return str.toString();
    }

    public static Integer integerValueOf(String value) {
        boolean notNull = isNotNull(value);
        if (notNull) {
            return  Integer.valueOf(value);
        }
        return 0;
    }

    public static Date stringToDate(String date){
        if (isNotNull(date)) {

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                return sdf.parse(date);
            } catch (ParseException e) {
                log.error("转换时间格式异常", e);
            }
        }
        return null;
    }

    public static Long longValueOf(String value) {
        boolean notNull = isNotNull(value);
        if (notNull) {
            return Long.valueOf(value);
        }
        return null;
    }

    public static Double doubleValueOf(String value) {
        boolean notNull = isNotNull(value);
        if (notNull) {
            return Double.valueOf(value);
        }
        return null;
    }
}
