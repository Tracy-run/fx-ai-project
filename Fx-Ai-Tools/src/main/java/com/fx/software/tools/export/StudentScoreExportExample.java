package com.fx.software.tools.export;

import com.fx.software.tools.vo.Student;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * @FileName StudentScoreExportExample
 * @Description
 * @Author fx
 * @date 2026-01-20
 */
public class StudentScoreExportExample {

    public static void main(String[] args) throws IOException {
        // 1. 生成测试数据
        List<Student> students = generateTestData(30);

        // 2. 指定要导出的科目（如果为null，则自动检测所有科目）
        List<String> subjects = Arrays.asList("语文", "数学", "英语", "物理", "化学");

        // 3. 导出到Excel
        String filePath = "E:/data/学生成绩报表.xlsx";
        boolean success = StudentScoreExcelExporter.exportBySubject(students, filePath, subjects);

        if (success) {
            System.out.println("导出完成！");
            System.out.println("文件位置: " + filePath);
            System.out.println("包含以下内容：");
            System.out.println("1. 每个科目一个独立的sheet页");
            System.out.println("2. 成绩汇总sheet页（包含总分、平均分、排名）");
            System.out.println("3. 统计数据sheet页（各科目统计信息）");
        }
    }

    /**
     * 生成测试数据
     */
    private static List<Student> generateTestData(int count) {
        List<Student> students = new ArrayList<>();
        Random random = new Random();
        String[] classes = {"高一(1)班", "高一(2)班", "高一(3)班", "高一(4)班"};
        String[] surnames = {"张", "李", "王", "刘", "陈", "杨", "赵", "黄", "周", "吴"};
        String[] givenNames = {"伟", "芳", "娜", "秀英", "敏", "静", "丽", "强", "磊", "军",
                "洋", "勇", "艳", "杰", "娟", "涛", "明", "超", "秀兰", "霞"};

        for (int i = 1; i <= count; i++) {
            Student student = new Student();
            student.setStudentId(String.format("2024%03d", i));
            student.setStudentName(surnames[random.nextInt(surnames.length)] +
                    givenNames[random.nextInt(givenNames.length)]);
            student.setClassName(classes[random.nextInt(classes.length)]);
            student.setAge(15 + random.nextInt(3));
            student.setGender(random.nextBoolean() ? "男" : "女");

            // 生成各科成绩（60-100分之间）
            student.setChineseScore(60 + random.nextDouble() * 40);
            student.setMathScore(60 + random.nextDouble() * 40);
            student.setEnglishScore(60 + random.nextDouble() * 40);

            // 随机生成其他科目成绩
            if (random.nextBoolean()) student.setPhysicsScore(60 + random.nextDouble() * 40);
            if (random.nextBoolean()) student.setChemistryScore(60 + random.nextDouble() * 40);
            if (random.nextBoolean()) student.setBiologyScore(60 + random.nextDouble() * 40);
            if (random.nextBoolean()) student.setHistoryScore(60 + random.nextDouble() * 40);
            if (random.nextBoolean()) student.setGeographyScore(60 + random.nextDouble() * 40);
            if (random.nextBoolean()) student.setPoliticsScore(60 + random.nextDouble() * 40);

            students.add(student);
        }

        return students;
    }
}
