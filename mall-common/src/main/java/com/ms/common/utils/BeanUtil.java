package com.ms.common.utils;

import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;

public abstract class BeanUtil {

    public static Object copyProperties(Object source, Object target, String... ignoreProperties) {
        if (null == source) {
            return target;
        }
        BeanUtils.copyProperties(source, target, ignoreProperties);
        return target;
    }

    public static <T> List<T> copyList(List sources, Class<T> clazz, Callback<T> callback) {
        List<T> targetList = new ArrayList<>();
        if (null != sources) {
            for (Object source: sources) {
                T t = null;
                try {
                    t = clazz.newInstance();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                copyProperties(source, t);
                if (null != callback) {
                    callback.set(source, t);
                }
                targetList.add(t);
            }
        }
        return targetList;
    }

    public static interface Callback<T>{
        void set(Object source, T target);
    }
}
