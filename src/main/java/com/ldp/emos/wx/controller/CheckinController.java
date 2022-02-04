package com.ldp.emos.wx.controller;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import com.ldp.emos.wx.common.util.R;
import com.ldp.emos.wx.config.SystemConstants;
import com.ldp.emos.wx.config.shiro.JwtUtil;
import com.ldp.emos.wx.controller.form.CheckinForm;
import com.ldp.emos.wx.controller.form.SearchMonthCheckinForm;
import com.ldp.emos.wx.exception.EmosException;
import com.ldp.emos.wx.service.CheckinService;
import com.ldp.emos.wx.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

/**
 * 打卡签到
 */
@RequestMapping("/checkin")
@RestController
@Api("签到模块web接口")
@Slf4j
public class CheckinController {

    @Autowired
    private JwtUtil jwtUtil;//令牌工具类

    @Autowired
    private CheckinService checkinService;//签到Service业务层

    @Autowired
    private UserService userService;//用户Service业务层

    @Autowired
    private SystemConstants constants;//打卡签到封装类

    @Value("${emos.image-folder}")
    private String imageFolder;//图片临时存放地址

    @GetMapping("/validCanCheckIn")
    @ApiOperation("查看用户今天是否可以签到")
    public R validCanCheckIn(@RequestHeader("token") String token) {//从header请求头里获取token
        int userId = jwtUtil.getUserId(token);//通过token获取用户id
        String result = checkinService.validCanCheckIn(userId, DateUtil.today());//检测当天是否可以签到
        return R.ok(result);
    }

    /**
     * 签到方法
     *
     * @param form  前端提交来的数据
     * @param file  图片文件
     * @param token token
     * @return
     */
    @PostMapping("/checkin")
    @ApiOperation("签到")
    public R checkin(@Valid CheckinForm form, @RequestParam("photo") MultipartFile file, @RequestHeader("token") String token) {
        if (file == null) {
            return R.error("没有上传图片");
        }
        int userId = jwtUtil.getUserId(token);//解析token获取userid
        String fileName = file.getOriginalFilename().toLowerCase();//获取图片名
        if (!fileName.endsWith(".jpg")) {
            return R.error("必须提交JPG格式的图片");
        } else {
            String path = imageFolder + "/" + fileName;//获取图片文件
            try {
                file.transferTo(Paths.get(path));//将图片保存到上面的路径下
                HashMap param = new HashMap();
                param.put("userId", userId);//用户id
                param.put("path", path);//图片路径
                param.put("city", form.getCity());//城市
                param.put("district", form.getDistrict());//地区
                param.put("address", form.getAddress());//地址
                param.put("country", form.getCountry());//国家
                param.put("province", form.getProvince());//省份
                checkinService.checkin(param);//将数据发送到service层执行
                return R.ok("签到成功");
            } catch (IOException e) {
                log.error(e.getMessage(), e);
                throw new EmosException("图片保存错误");
            } finally {
                FileUtil.del(path);//删除前面保存到硬盘上的图片
            }
        }
    }

    /**
     * 创建人脸模型
     *
     * @param file  请求中的图片
     * @param token 请求中的token
     * @return
     */
    @PostMapping("/createFaceModel")
    @ApiOperation("创建人脸模型")
    public R createFaceModel(@RequestParam("photo") MultipartFile file, @RequestHeader("token") String token) {
        if (file == null) {
            return R.error("没有上传图片");
        }
        int userId = jwtUtil.getUserId(token);//解析token获取userid
        String fileName = file.getOriginalFilename().toLowerCase();//获取图片名
        if (!fileName.endsWith(".jpg")) {
            return R.error("必须提交JPG格式的图片");
        } else {
            String path = imageFolder + "/" + fileName;//获取图片文件
            try {
                file.transferTo(Paths.get(path));//将图片保存到上面的路径下
                checkinService.createFaceModel(userId, path);//将数据发送到service层执行
                return R.ok("人脸建模成功");
            } catch (IOException e) {
                log.error(e.getMessage(), e);
                throw new EmosException("图片保存错误");
            } finally {
                FileUtil.del(path);//删除前面保存到硬盘上的图片
            }
        }
    }

    @GetMapping("/searchTodayCheckin")
    @ApiOperation("查询用户当日签到数据")
    public HashMap searchTodayCheckin(@RequestHeader("token") String token) {
        int userId = jwtUtil.getUserId(token);//获取userid
        HashMap map = checkinService.searchTodayCheckin(userId);//查询员工当天的签到情况、员工考勤日期总数
        map.put("attendanceTime", constants.attendanceTime);//获取当天考勤开始时间
        map.put("closingTime", constants.closingEndTime);//获取当天考勤结束时间
        long days = checkinService.searchCheckinDays(userId);//统计用户总的签到天数
        map.put("checkinDays", days);
        //TODO 判断日期是否在用户入职之前
        DateTime hiredate = DateUtil.parse(userService.searchUserHiredate(userId));//获取用户的入职日期
        DateTime startDate = DateUtil.beginOfWeek(DateUtil.date());//获取本周的开始日期
        if (startDate.isBefore(hiredate)) {//判断本周的开始日期是否在入职日期之前
            startDate = hiredate;//入职之前的日期不统计，统计入职之后的日期
        }
        DateTime endDate = DateUtil.endOfWeek(DateUtil.date());//获取本周的结束日期
        HashMap param = new HashMap();
        param.put("startDate", startDate.toString());
        param.put("endDate", endDate.toString());
        param.put("userId", userId);
        ArrayList<HashMap> list = checkinService.searchWeekCheckin(param);//获取本周的考勤情况
        map.put("weekCheckin", list);//考勤情况
        return R.ok().put("result", map);
    }

    //查询用户当月考勤数据
    @PostMapping("/searchMonthCheckin")
    @ApiOperation("查询用户某月签到数据")
    public R searchMonthCheckin(@Valid @RequestBody SearchMonthCheckinForm form, @RequestHeader("token") String token) {
        int userId = jwtUtil.getUserId(token);
        DateTime hiredate = DateUtil.parse(userService.searchUserHiredate(userId));//查询入职日期
        String month = form.getMonth() < 10 ? "0" + form.getMonth() : form.getMonth().toString();//把月份处理成双数字
        DateTime startDate = DateUtil.parse(form.getYear() + "-" + month + "-01");//某年某月的起始日期
        if (startDate.isBefore(DateUtil.beginOfMonth(hiredate))) {//如果查询的月份早于员工入职日期的月份就抛出异常
            throw new EmosException("只能查询入职之后日期的数据");
        }
        if (startDate.isBefore(hiredate)) {//如果查询月份与入职月份恰好是同月，本月考勤查询开始日期设置成入职日期
            startDate = hiredate;//将当月的起始日期设置为入职日期
        }
        DateTime endDate = DateUtil.endOfMonth(startDate);//获取本月的结束日期
        HashMap param = new HashMap();
        param.put("userId", userId);
        param.put("startDate", startDate.toString());
        param.put("endDate", endDate.toString());
        ArrayList<HashMap> list = checkinService.searchMonthCheckin(param);//查询当月考勤数据
        int sum_1 = 0, sum_2 = 0, sum_3 = 0;
        for (HashMap<String, String> one : list) {
            String type = one.get("type");//获取当日的日子状态（节假日工作日）
            String status = one.get("status");//获取当日的考勤状态（正常迟到缺勤）
            if ("工作日".equals(type)) {
                if ("正常".equals(status)) {
                    sum_1++;
                } else if ("迟到".equals(status)) {
                    sum_2++;
                } else if ("缺勤".equals(status)) {
                    sum_3++;
                }
            }
        }
        return R.ok().put("list", list).put("sum_1", sum_1).put("sum_2", sum_2).put("sum_3", sum_3);
    }
}
