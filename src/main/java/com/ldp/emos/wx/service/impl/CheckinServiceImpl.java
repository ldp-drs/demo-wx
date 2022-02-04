package com.ldp.emos.wx.service.impl;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateRange;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.ldp.emos.wx.config.SystemConstants;
import com.ldp.emos.wx.db.dao.*;
import com.ldp.emos.wx.db.pojo.TbCheckin;
import com.ldp.emos.wx.db.pojo.TbFaceModel;
import com.ldp.emos.wx.exception.EmosException;
import com.ldp.emos.wx.service.CheckinService;
import com.ldp.emos.wx.task.EmailTask;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

@Service
@Scope("prototype")//多例对象注解
@Slf4j
public class CheckinServiceImpl implements CheckinService {

    @Autowired
    private SystemConstants systemConstants;//打卡签到类

    @Autowired
    private TbHolidaysDao holidaysDao;//是否是节假日

    @Autowired
    private TbWorkdayDao workdayDao;//是否是工作日

    @Autowired
    private TbCheckinDao checkinDao;//是否已签到类

    @Autowired
    private TbFaceModelDao faceModelDao;//人脸模型

    @Autowired
    private TbCityDao cityDao;//城市

    @Autowired
    private TbUserDao userDao;

    @Value("${emos.face.createFaceModelUrl}")
    private String createFaceModelUrl;

    @Value("${emos.face.checkinUrl}")
    private String checkinUrl;

    @Value("${emos.email.hr}")
    private String hrEmail;

    @Value("${emos.code}")
    private String code;

    @Autowired
    private EmailTask emailTask;

    //检测当天是否可以签到
    @Override
    public String validCanCheckIn(int userId, String date) {
        //查询当天是否是节假日
        boolean bool_1 = holidaysDao.searchTodayIsHolidays() != null ? true : false;
        //查询当天是否是工作日
        boolean bool_2 = workdayDao.searchTodayIsWorkday() != null ? true : false;
        String type = "工作日";//默认为工作日
        if (DateUtil.date().isWeekend()) {//判断当天是否是周末
            type = "节假日";
        }
        if (bool_1) {
            type = "节假日";
        } else if (bool_2) {
            type = "工作日";
        }
        if (type.equals("节假日")) {
            return "节假日不需要考勤";
        } else {
            DateTime now = DateUtil.date();//获取当前时间对象
            String start = DateUtil.today() + " " + systemConstants.attendanceStartTime;//获取当天加上开始签到时间
            String end = DateUtil.today() + " " + systemConstants.attendanceEndTime;//获取当天加结束签到时间
            DateTime attendanceStart = DateUtil.parse(start);//时间转换
            DateTime attendanceEnd = DateUtil.parse(end);//转换时间
            if (now.isBefore(attendanceStart)) {//判断是否在考勤之前
                return "未到上班考勤开始时间";
            } else if (now.isAfter(attendanceEnd)) {
                return "考勤已结束";
            } else {
                HashMap map = new HashMap();
                map.put("userId", userId);
                map.put("date", date);
                map.put("start", start);
                map.put("end", end);
                boolean bool = checkinDao.haveCheckin(map) != null ? true : false;//查询当天是否已签到
                return bool ? "今日已考勤，不用重复考勤" : "可以考勤";
            }
        }
    }

    //签到方法
    @Override
    public void checkin(HashMap param) {
        //判断签到
        Date d1 = DateUtil.date();//获取当前时间
        Date d2 = DateUtil.parse(DateUtil.today() + " " + systemConstants.attendanceTime);//获取上班签到时间对象
        Date d3 = DateUtil.parse(DateUtil.today() + " " + systemConstants.attendanceEndTime);//获取签到结束时间
        int status = 1;//状态 默认为正常
        if (d1.compareTo(d2) <= 0) {//判断早于上班时间，则正常签到
            status = 1;//正常签到
        } else if (d1.compareTo(d2) > 0 && d1.compareTo(d3) < 0) {//判断时间超过正常上班时间，但没有超过结束考勤时间，则为迟到
            status = 2;//迟到
        }
        //TODO 查询签到人的人脸模型数据
        int userId = (Integer) param.get("userId");//获取用户id
        String faceModel = faceModelDao.searchFaceModel(userId);//查询签到人的人脸模型数据
        //判断人脸模型是否为空
        if (faceModel == null) {
            throw new EmosException("不存在人脸模型");
        } else {
            String path = (String) param.get("path");//获取照片地址
            //请求人脸模型判断
            HttpRequest request = HttpUtil.createPost(checkinUrl);
            //上传图片（图片名，图片地址，python名，人脸模型数据）
            request.form("phtot", FileUtil.file(path), "targetModel", faceModel);
            request.form("code", code);
            HttpResponse response = request.execute();//获取返回响应的内容
            if (response.getStatus() != 200) {//判断返回响应状态码
                log.error("人脸识别异常服务");
                throw new EmosException("人脸识别服务异常");
            }
            String body = response.body();//获取返回响应的body主体
            if ("无法识别出人脸".equals(body) || "照片中存在多张人脸".equals(body)) {
                throw new EmosException(body);
            } else if ("False".equals(body)) {
                throw new EmosException("签到无效，非本人签到");
            } else if ("True".equals(body) || "icode不存在".equals(body)) {//人脸识别正确
                //TODO 查询疫情风险登记
                int risk = 1;//风险等级：1.低 2.中 3.高
                String city = (String) param.get("city");//查询城市简称
                String district = (String) param.get("district");//查询区域
                String address = (String) param.get("address");//获取地址
                String country = (String) param.get("country");//获取国家
                String province = (String) param.get("province");//获取省份
                //如果城市和地区不为空
                if (!StrUtil.isBlank(city) && !StrUtil.isBlank(district)) {
                    String code = cityDao.searchCode(city);//获取城市编码
                    //TODO 查询地区风险
                    try {
                        String url = "http://m." + code + ".bendibao.com/news/yqdengji/?qu=" + district;//查询并获取该地区的风险
                        Document document = Jsoup.connect(url).get();//获取查询到的html数据
                        Elements elements = document.getElementsByClass("list-content");//获取html风险中的控件
                        if (elements.size() > 0) {//判断获取的控件中是否有值
                            Element element = elements.get(0);//获取控件内的第一个内容
                            String result = element.select("p:last-child").text();//在内容中获取最后一个p标签的值
                            if ("高风险".equals(result)) {
                                risk = 3;
                                //TODO 发送告警邮件
                                HashMap<String, String> map = userDao.searchNameAndDept(userId);//根据用户id获取用户信息
                                String name = map.get("name");//获取名字
                                String deptName = map.get("dept_name");//获取地区名
                                deptName = deptName != null ? deptName : "";
                                SimpleMailMessage message = new SimpleMailMessage();
                                message.setTo(hrEmail);//设置右键地址
                                message.setSubject("员工" + name + "身处高风险疫情地区警告");//设置标题
                                //设置信息
                                message.setText(deptName + "员工" + name + ", " + DateUtil.format(new Date(), "yyyy年MM月dd日") + "处于" + address + ",属于新冠疫情高风险地区，请及时与该员工联系，核实情况！");
                                emailTask.sendAsync(message);//发送右键
                            } else if ("中风险".equals(result)) {
                                risk = 2;
                            }
                        }
                    } catch (Exception e) {
                        log.error("执行异常", e);
                        throw new EmosException("获取风险等级失败");
                    }
                }
                //TODO 保存签到记录
                try {
                    TbCheckin entity = new TbCheckin();
                    //将数据写入实体类中
                    entity.setUserId(userId);
                    entity.setAddress(address);
                    entity.setCountry(country);
                    entity.setProvince(province);
                    entity.setCity(city);
                    entity.setDistrict(district);
                    entity.setStatus((byte) status);
                    entity.setRisk(risk);
                    entity.setDate(DateUtil.today());
                    entity.setCreateTime(d1);
                    checkinDao.insert(entity);//将数据写入数据库中
                } catch (Exception e) {
                    log.error(e.getMessage());
                    throw new EmosException("今日已签到");
                }
            }
        }
    }

    /**
     * 创建人脸模型方法
     *
     * @param userId 用户id
     * @param path   人脸模型路径
     */
    @Override
    public void createFaceModel(int userId, String path) {
        HttpRequest request = HttpUtil.createPost(createFaceModelUrl);//发起请求
        request.form("photo", FileUtil.file(path));//将key和照片放入请求中
        request.form("code", code);
        HttpResponse response = request.execute();//获取返回响应的内容
        String body = response.body();//获取响应体
        if ("无法识别出人脸".equals(body) || "照片中存在多张人脸".equals(body)) {
            throw new EmosException(body);
        } else {
            TbFaceModel entity = new TbFaceModel();
            entity.setUserId(userId);//往实体类中写入用户id
            entity.setFaceModel(body);//往实体类中写入响应体
            faceModelDao.insert(entity);//执行插入语句写入数据库中
        }

    }

    //查询员工当天的签到情况、员工考勤日期总数
    @Override
    public HashMap searchTodayCheckin(int userId) {
        return checkinDao.searchTodayCheckin(userId);
    }

    //统计用户总的签到天数
    @Override
    public long searchCheckinDays(int userId) {
        return checkinDao.searchCheckinDays(userId);
    }

    //本周的考勤情况
    @Override
    public ArrayList<HashMap> searchWeekCheckin(HashMap param) {
        ArrayList<HashMap> checkinList = checkinDao.searchWeekCheckin(param);//获取本周的考勤情况
        ArrayList<String> holidaysList = holidaysDao.searchHolidaysInRange(param);//查询本周特殊节假日
        ArrayList<String> workdayList = workdayDao.searchWorkdayInRange(param);//查询本周特殊工作日
        DateTime startDate = DateUtil.parseDate(param.get("startDate").toString());//获取本周的起始日期
        DateTime endDate = DateUtil.parseDate(param.get("endDate").toString());//获取本周的起始日期
        DateRange range = DateUtil.range(startDate, endDate, DateField.DAY_OF_MONTH);//获取本周的日期对象
        ArrayList<HashMap> list = new ArrayList<>();
        range.forEach(one -> {
            String date = one.toString("yyyy-MM-dd");//获取当天日期
            String type = "工作日";
            if (one.isWeekend()) {//判断是否是周末
                type = "休息日";
            }
            if (holidaysList != null && holidaysList.contains(date)) {//判断是否是节假日
                type = "节假日";
            } else if (workdayList != null && workdayList.contains(date)) {//判断是否是工作日
                type = "工作日";
            }
            String status = "";
            if (type.equals("工作日") && DateUtil.compare(one, DateUtil.date()) <= 0) {//判断当天工作日是否发生，就是当天工作日是否已过
                status = "缺勤";
                boolean flag = false;//判断当天是否已考勤：未考勤
                for (HashMap<String, String> map : checkinList) {//将checkinList里的结果保存到HashMap里，然后循环
                    if (map.containsValue(date)) {//判断是否是当天的
                        status = map.get("status");//获取当天的状态
                        flag = true;
                        break;
                    }
                }
                //获取当天考勤结束的时间
                DateTime endTime = DateUtil.parse(DateUtil.today() + " " + systemConstants.attendanceEndTime);
                String today = DateUtil.today();//获取当天日期的字符串
                //判断是否等于当天考勤时间，并且早于考勤结束时间，并且是为考勤的
                if (date.equals(today) && DateUtil.date().isBefore(endTime) && flag == false) {
                    status = "";
                }
            }
            HashMap map = new HashMap();
            map.put("date", date);//当天日期
            map.put("status", status);//考勤状态
            map.put("type", type);//当天属于什么日子
            map.put("day", one.dayOfWeekEnum().toChinese("周"));//写入当天是星期几，并转换成周几
            list.add(map);
        });
        return list;
    }

    //查询当月考勤方法 uesrid，起始日期，结束日期
    @Override
    public ArrayList<HashMap> searchMonthCheckin(HashMap param) {
        return this.searchWeekCheckin(param);//获取当月考勤的内容
    }
}
