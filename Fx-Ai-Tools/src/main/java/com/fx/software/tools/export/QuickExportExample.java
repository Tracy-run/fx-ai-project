package com.fx.software.tools.export;

import com.fx.software.tools.vo.Student;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @FileName QuickExportExample
 * @Description
 * @Author fx
 * @date 2026-01-20
 */
public class QuickExportExample {

    public static void main(String[] args) throws IOException {
        // 快速创建数据并导出
        List<Student> students = new ArrayList<>();

        // 添加学生数据
        students.add(new Student("001", "张三", "高一(1)班", 85.5, 92.0, 88.0));
        students.add(new Student("002", "李四", "高一(1)班", 78.0, 85.5, 90.5));
        students.add(new Student("003", "王五", "高一(2)班", 92.5, 88.0, 76.5));
        students.add(new Student("004", "赵六", "高一(2)班", 65.0, 72.5, 68.0));

        // 导出
        StudentScoreExcelExporter.exportBySubject(students,
                "E:/data/学生成绩_快速导出.xlsx",
                null); // null表示自动检测所有科目

        System.out.println("快速导出完成！");
    }
}
