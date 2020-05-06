package com.shenyong.flutter.checker;

import java.io.File;

/**
 * @author ShenYong
 * @date 2020/5/6
 */
public interface ICheck {

    /**
     * 校验方法
     * @param path 待检查路径
     * @return 校验结果，true 为 通过
     */
    boolean check(String path);
}
