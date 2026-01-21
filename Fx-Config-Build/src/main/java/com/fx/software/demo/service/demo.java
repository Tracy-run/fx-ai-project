package com.fx.software.demo.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**z
 * @FileName demo
 * @Description
 * @Author fx
 * @date 2025-09-21
 */
public class demo {


    public static void main(String[] args) {

        List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5);
        Map<Boolean, List<Integer>> collect = numbers.stream().collect(Collectors.partitioningBy(a -> {
            return a > 5;
        }));


        // 特殊分组
        Map<Boolean, List<Integer>> map = numbers.stream().collect(Collectors.partitioningBy(item -> {
            Integer student = item;
            return student.toString() == "5";
        }));
    }


}
