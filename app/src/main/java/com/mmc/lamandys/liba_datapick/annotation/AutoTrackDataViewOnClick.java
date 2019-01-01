package com.mmc.lamandys.liba_datapick.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Army
 * @version V_1.0.0
 * @date 2019/1/1
 * @description
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface AutoTrackDataViewOnClick {
}
