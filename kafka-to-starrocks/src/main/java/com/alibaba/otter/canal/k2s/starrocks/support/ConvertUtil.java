package com.alibaba.otter.canal.k2s.starrocks.support;

import java.util.List;
import java.util.stream.Collectors;

public class ConvertUtil {
    public static List<String> convertBytesToString(List<byte []>  byteData) {
        return byteData.stream()
                .map(String::new)
                .collect(Collectors.toList());
    }
}
