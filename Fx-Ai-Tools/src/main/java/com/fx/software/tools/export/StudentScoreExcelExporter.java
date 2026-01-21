package com.fx.software.tools.export;

import com.fx.software.tools.vo.Student;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @FileName StudentScoreExcelExporter
 * @Description
 * @Author fx
 * @date 2026-01-20
 */
public class StudentScoreExcelExporter {

    /**
     * 按科目导出学生成绩到Excel（每个科目一个sheet页）
     * @param students 学生列表
     * @param filePath 文件保存路径
     * @param subjects 科目列表（可选，如果为空则导出所有有成绩的科目）
     * @return 是否导出成功
     */
    public static boolean exportBySubject(List<Student> students, String filePath, List<String> subjects) throws IOException {
        if (students == null || students.isEmpty()) {
            System.err.println("学生数据为空，无法导出");
            return false;
        }

        try (XSSFWorkbook workbook = new XSSFWorkbook();
             FileOutputStream fos = new FileOutputStream(filePath)) {

            // 如果没有指定科目，则自动检测所有有成绩的科目
            List<String> allSubjects = subjects != null ? subjects : detectAllSubjects(students);

            // 1. 为每个科目创建一个sheet页
            Map<String, XSSFSheet> subjectSheets = new LinkedHashMap<>();
            for (String subject : allSubjects) {
                XSSFSheet sheet = workbook.createSheet(subject);
                subjectSheets.put(subject, sheet);
                createSubjectSheet(sheet, subject, students);
            }

            // 2. 创建汇总sheet页
            createSummarySheet(workbook, students, allSubjects);

            // 3. 创建统计数据sheet页
            createStatisticsSheet(workbook, students, allSubjects);

            // 4. 设置所有列的自动宽度
            for (XSSFSheet sheet : subjectSheets.values()) {
                autoSizeColumns(sheet);
            }

            // 保存文件
            workbook.write(fos);
            System.out.println("学生成绩Excel文件已成功导出到: " + filePath);
            System.out.println("包含 " + allSubjects.size() + " 个科目sheet页和2个统计sheet页");

            return true;

        } catch (IOException e) {
            System.err.println("导出Excel文件失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 为单个科目创建sheet页
     */
    private static void createSubjectSheet(XSSFSheet sheet, String subject, List<Student> students) {
        // 创建样式
        XSSFCellStyle headerStyle = createHeaderStyle(sheet.getWorkbook());
        XSSFCellStyle dataStyle = createDataStyle(sheet.getWorkbook());
        XSSFCellStyle highlightStyle = createHighlightStyle(sheet.getWorkbook());

        // 创建标题行
        Row headerRow = sheet.createRow(0);
        String[] headers = {"序号", "学号", "姓名", "班级", subject + "成绩", "等级", "备注"};

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // 填充数据行
        AtomicInteger rowNum = new AtomicInteger(1);
        students.forEach(student -> {
            Row row = sheet.createRow(rowNum.getAndIncrement());

            // 序号
            row.createCell(0).setCellValue(rowNum.get() - 1);

            // 学号
            row.createCell(1).setCellValue(student.getStudentId());

            // 姓名
            row.createCell(2).setCellValue(student.getStudentName());

            // 班级
            row.createCell(3).setCellValue(student.getClassName());

            // 科目成绩
            Double score = student.getScoreBySubject(subject);
            Cell scoreCell = row.createCell(4);
            if (score != null) {
                scoreCell.setCellValue(score);

                // 根据成绩设置单元格样式
                if (score >= 90) {
                    scoreCell.setCellStyle(highlightStyle);
                } else {
                    scoreCell.setCellStyle(dataStyle);
                }

                // 等级
                row.createCell(5).setCellValue(getGradeLevel(score));

                // 备注
                row.createCell(6).setCellValue(getRemark(score));
            } else {
                scoreCell.setCellValue("缺考");
                scoreCell.setCellStyle(dataStyle);
                row.createCell(5).setCellValue("无");
                row.createCell(6).setCellValue("未参加考试");
            }
        });

        // 添加统计行
        addStatisticsRow(sheet, rowNum.get(), subject, students);
    }

    /**
     * 创建汇总sheet页
     */
    private static void createSummarySheet(XSSFWorkbook workbook, List<Student> students, List<String> subjects) {
        XSSFSheet sheet = workbook.createSheet("成绩汇总");
        XSSFCellStyle headerStyle = createHeaderStyle(workbook);
        XSSFCellStyle totalStyle = createTotalStyle(workbook);

        // 创建标题行
        Row headerRow = sheet.createRow(0);
        List<String> headers = new ArrayList<>(Arrays.asList("序号", "学号", "姓名", "班级"));
        headers.addAll(subjects);
        headers.addAll(Arrays.asList("总分", "平均分", "排名"));

        for (int i = 0; i < headers.size(); i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers.get(i));
            cell.setCellStyle(headerStyle);
        }

        // 计算每个学生的总分和排名
        List<StudentWithTotal> studentTotals = new ArrayList<>();
        for (Student student : students) {
            double total = 0;
            for (String subject : subjects) {
                Double score = student.getScoreBySubject(subject);
                if (score != null) {
                    total += score;
                }
            }
            studentTotals.add(new StudentWithTotal(student, total));
        }

        // 按总分排序
        studentTotals.sort((a, b) -> Double.compare(b.totalScore, a.totalScore));

        // 填充数据
        for (int i = 0; i < studentTotals.size(); i++) {
            StudentWithTotal swt = studentTotals.get(i);
            Student student = swt.student;
            Row row = sheet.createRow(i + 1);

            row.createCell(0).setCellValue(i + 1); // 序号
            row.createCell(1).setCellValue(student.getStudentId());
            row.createCell(2).setCellValue(student.getStudentName());
            row.createCell(3).setCellValue(student.getClassName());

            // 各科成绩
            int colIndex = 4;
            for (String subject : subjects) {
                Double score = student.getScoreBySubject(subject);
                row.createCell(colIndex++).setCellValue(score != null ? score : 0);
            }

            // 总分、平均分、排名
            row.createCell(colIndex++).setCellValue(swt.totalScore);
            row.createCell(colIndex++).setCellValue(swt.totalScore / subjects.size());
            row.createCell(colIndex).setCellValue(i + 1);

            // 为总分列设置特殊样式
            row.getCell(headers.indexOf("总分")).setCellStyle(totalStyle);
        }

        // 添加统计行
        addSummaryStatistics(sheet, studentTotals, subjects);
        autoSizeColumns(sheet);
    }

    /**
     * 创建统计数据sheet页
     */
    private static void createStatisticsSheet(XSSFWorkbook workbook, List<Student> students, List<String> subjects) {
        XSSFSheet sheet = workbook.createSheet("统计数据");
        XSSFCellStyle headerStyle = createHeaderStyle(workbook);

        // 表头
        Row headerRow = sheet.createRow(0);
        String[] headers = {"科目", "参考人数", "平均分", "最高分", "最低分",
                "90分以上", "80-89分", "70-79分", "60-69分", "60分以下", "及格率"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // 计算每个科目的统计数据
        int rowNum = 1;
        for (String subject : subjects) {
            Row row = sheet.createRow(rowNum++);

            // 收集该科目的所有有效成绩
            List<Double> scores = new ArrayList<>();
            for (Student student : students) {
                Double score = student.getScoreBySubject(subject);
                if (score != null) {
                    scores.add(score);
                }
            }

            if (!scores.isEmpty()) {
                double sum = scores.stream().mapToDouble(Double::doubleValue).sum();
                double avg = sum / scores.size();
                double max = Collections.max(scores);
                double min = Collections.min(scores);

                // 统计各分数段人数
                long excellent = scores.stream().filter(s -> s >= 90).count();
                long good = scores.stream().filter(s -> s >= 80 && s < 90).count();
                long medium = scores.stream().filter(s -> s >= 70 && s < 80).count();
                long pass = scores.stream().filter(s -> s >= 60 && s < 70).count();
                long fail = scores.stream().filter(s -> s < 60).count();

                double passRate = (scores.size() - fail) * 100.0 / scores.size();

                // 填充数据
                row.createCell(0).setCellValue(subject);
                row.createCell(1).setCellValue(scores.size());
                row.createCell(2).setCellValue(avg);
                row.createCell(3).setCellValue(max);
                row.createCell(4).setCellValue(min);
                row.createCell(5).setCellValue(excellent);
                row.createCell(6).setCellValue(good);
                row.createCell(7).setCellValue(medium);
                row.createCell(8).setCellValue(pass);
                row.createCell(9).setCellValue(fail);
                row.createCell(10).setCellValue(String.format("%.2f%%", passRate));
            }
        }

        autoSizeColumns(sheet);
    }

    /**
     * 为科目sheet添加统计行
     */
    private static void addStatisticsRow(XSSFSheet sheet, int startRow, String subject, List<Student> students) {
        Row statRow = sheet.createRow(startRow);

        // 收集该科目的所有有效成绩
        List<Double> scores = students.stream()
                .map(s -> s.getScoreBySubject(subject))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (!scores.isEmpty()) {
            double sum = scores.stream().mapToDouble(Double::doubleValue).sum();
            double avg = sum / scores.size();
            double max = Collections.max(scores);
            double min = Collections.min(scores);

            statRow.createCell(0).setCellValue("统计");
            statRow.createCell(1).setCellValue("");
            statRow.createCell(2).setCellValue("");
            statRow.createCell(3).setCellValue("平均分:");
            statRow.createCell(4).setCellValue(avg);
            statRow.createCell(5).setCellValue("最高分:");
            statRow.createCell(6).setCellValue(max);

            Row statRow2 = sheet.createRow(startRow + 1);
            statRow2.createCell(3).setCellValue("最低分:");
            statRow2.createCell(4).setCellValue(min);
            statRow2.createCell(5).setCellValue("参考人数:");
            statRow2.createCell(6).setCellValue(scores.size());
        }
    }

    /**
     * 添加汇总统计信息
     */
    private static void addSummaryStatistics(XSSFSheet sheet, List<StudentWithTotal> studentTotals, List<String> subjects) {
        int lastRow = studentTotals.size() + 1;
        Row statRow = sheet.createRow(lastRow);

        double totalAvg = studentTotals.stream()
                .mapToDouble(swt -> swt.totalScore / subjects.size())
                .average()
                .orElse(0);

        statRow.createCell(0).setCellValue("班级平均分:");
        statRow.createCell(subjects.size() + 4).setCellValue(totalAvg);
    }

    /**
     * 自动检测所有有成绩的科目
     */
    private static List<String> detectAllSubjects(List<Student> students) {
        Set<String> subjects = new LinkedHashSet<>();

        // 预定义的科目列表
        String[] possibleSubjects = {"语文", "数学", "英语", "物理", "化学",
                "生物", "历史", "地理", "政治"};

        for (Student student : students) {
            for (String subject : possibleSubjects) {
                if (student.getScoreBySubject(subject) != null) {
                    subjects.add(subject);
                }
            }
        }

        return new ArrayList<>(subjects);
    }

    /**
     * 根据分数获取等级
     */
    private static String getGradeLevel(Double score) {
        if (score == null) return "无";
        if (score >= 90) return "优秀";
        if (score >= 80) return "良好";
        if (score >= 70) return "中等";
        if (score >= 60) return "及格";
        return "不及格";
    }

    /**
     * 根据分数获取备注
     */
    private static String getRemark(Double score) {
        if (score == null) return "未参加考试";
        if (score >= 95) return "表现优异";
        if (score >= 85) return "表现良好";
        if (score >= 60) return "继续努力";
        return "需要加强学习";
    }

    /**
     * 创建表头样式
     */
    private static XSSFCellStyle createHeaderStyle(XSSFWorkbook workbook) {
        XSSFCellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.SKY_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    /**
     * 创建数据单元格样式
     */
    private static XSSFCellStyle createDataStyle(XSSFWorkbook workbook) {
        XSSFCellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    /**
     * 创建高亮样式（用于高分）
     */
    private static XSSFCellStyle createHighlightStyle(XSSFWorkbook workbook) {
        XSSFCellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setColor(IndexedColors.RED.getIndex());
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    /**
     * 创建总分样式
     */
    private static XSSFCellStyle createTotalStyle(XSSFWorkbook workbook) {
        XSSFCellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderTop(BorderStyle.MEDIUM);
        style.setBorderBottom(BorderStyle.MEDIUM);
        style.setBorderLeft(BorderStyle.MEDIUM);
        style.setBorderRight(BorderStyle.MEDIUM);
        return style;
    }

    /**
     * 自动调整列宽
     */
    private static void autoSizeColumns(XSSFSheet sheet) {
        for (int i = 0; i < sheet.getRow(0).getPhysicalNumberOfCells(); i++) {
            sheet.autoSizeColumn(i);
            // 设置最小列宽
            sheet.setColumnWidth(i, Math.max(sheet.getColumnWidth(i), 3000));
        }
    }

    /**
     * 内部类：学生与总分
     */
    private static class StudentWithTotal {
        Student student;
        double totalScore;

        StudentWithTotal(Student student, double totalScore) {
            this.student = student;
            this.totalScore = totalScore;
        }
    }


}
