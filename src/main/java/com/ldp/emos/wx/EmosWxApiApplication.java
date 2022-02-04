package com.ldp.emos.wx;

import cn.hutool.core.util.StrUtil;
import com.ldp.emos.wx.config.SystemConstants;
import com.ldp.emos.wx.db.dao.SysConfigDao;
import com.ldp.emos.wx.db.pojo.SysConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

import javax.annotation.PostConstruct;
import java.io.File;
import java.lang.reflect.Field;
import java.util.List;

@SpringBootApplication
@ServletComponentScan
@Slf4j
@EnableAsync
public class EmosWxApiApplication {

    @Autowired
    private SysConfigDao sysConfigDao;

    @Autowired
    private SystemConstants constants;

    @Value("${emos.image-folder}")
    private String imageFolder;//临时图片存放地址

    public static void main(String[] args) {
        SpringApplication.run(EmosWxApiApplication.class, args);
    }

    @PostConstruct//初始化注解
    public void init() {
        List<SysConfig> list = sysConfigDao.selectAllParam();//获取签到打卡数据
        list.forEach(one -> {//通过循环
            String key = one.getParamKey();//获取打卡名
            key = StrUtil.toCamelCase(key);//通过驼峰命名法重新给key赋值
            String value = one.getParamValue();//获取打卡时间
            try {
                Field field = constants.getClass().getDeclaredField(key);//获取变量声明
                field.set(constants, value);//写入数据
            } catch (Exception e) {
                log.error("执行异常", e);
            }
        });
        new File(imageFolder).mkdirs();//判断文件夹是否存在，不存在则创建
    }
}
