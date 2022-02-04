package com.ldp.emos.wx.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.ldp.emos.wx.db.dao.TbDeptDao;
import com.ldp.emos.wx.db.dao.TbUserDao;
import com.ldp.emos.wx.db.pojo.MessageEntity;
import com.ldp.emos.wx.db.pojo.TbUser;
import com.ldp.emos.wx.exception.EmosException;
import com.ldp.emos.wx.service.UserService;
import com.ldp.emos.wx.task.MessageTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 用户服务层
 */
@Service
@Slf4j
@Scope("prototype")//多例注解，令牌认证需要
public class UserServiceImpl implements UserService {

    @Value("${wx.app-id}")
    private String appId;

    @Value("${wx.app-secret}")
    private String appSecret;

    @Autowired
    private TbUserDao userDao;

    @Autowired
    private MessageTask messageTask;

    @Autowired
    private TbDeptDao deptDao;


    //TODO 获取OpenId
    private String getOpenId(String code) {
        String url = "https://api.weixin.qq.com/sns/jscode2session";
        HashMap map = new HashMap();
        map.put("appid", appId);
        map.put("secret", appSecret);
        map.put("js_code", code);
        map.put("grant_type", "authorization_code");
        String response = HttpUtil.post(url, map);//发送请求
        JSONObject json = JSONUtil.parseObj(response);//转换成json对象
        String openId = json.getStr("openid");//提取openid值
        if (openId == null || openId.length() == 0) {
            throw new RuntimeException("临时登录凭证错误");
        }
        return openId;
    }

    //添加用户方法
    @Override
    public int registerUser(String registerCode, String code, String nickname, String photo) {
        if (registerCode.equals("000000") || registerCode.equals(111111)) {//如果邀请码是000000，代表是要注册超级管理员
            boolean bool = userDao.haveRootUser();//查询超级管理员账户是否已经绑定
            if (!bool) {//判断不存在超级管理员账户的情况
                //把当前用户绑定到root账户
                String openId = getOpenId(code);//code：临时字符串
                HashMap param = new HashMap();
                param.put("openId", openId);
                param.put("nickname", nickname);
                param.put("photo", photo);
                param.put("role", "[0]");
                param.put("status", 1);
                param.put("createTime", new Date());
                param.put("root", true);
                userDao.insert(param);//执行插入语句并获取id
                int id = userDao.searchIdByOpenId(openId);//执行查询语句并获取id

                //注册成功后给出一条消息通知
                MessageEntity entity = new MessageEntity();
                entity.setSenderId(0);//系统发送的消息，所以是0
                entity.setSenderName("系统消息");
                entity.setUuid(IdUtil.simpleUUID());
                entity.setMsg("欢迎您注册成为超级管理员，请及时更新你的员工个人信息。");
                entity.setSendTime(new Date());
                messageTask.sendAsync(id + "", entity);//通过异步进行发送消息

                return id;
            } else {
                //如果root已经绑定了，就抛出异常
                throw new EmosException("账号已绑定超级管理员");
            }
        }
        //普通员工注册
        else {

        }
        return 0;
    }

    //根据用户id获取用户的权限
    @Override
    public Set<String> searchUserPermissions(int userId) {
        return userDao.searchUserPermissions(userId);
    }

    //用户登录模块
    @Override
    public Integer login(String code) {
        String openId = getOpenId(code);//通过临时字符串获取openid
        Integer id = userDao.searchIdByOpenId(openId);//通过openid查询数据，获取用户的id
        if (id == null) {
            throw new EmosException("账户不存在");
        }
        //TODO 从消息队列中接收消息，转移到消息表
//        messageTask.receiveAysnc(id + "");
        return id;
    }

    //获取用户信息
    @Override
    public TbUser searchById(int userId) {
        TbUser user = userDao.searchById(userId);
        return user;
    }

    //获取用户的入职日期
    @Override
    public String searchUserHiredate(int userId) {
        String hiredate = userDao.searchUserHiredate(userId);
        return hiredate;
    }

    //获取用户的概要信息
    @Override
    public HashMap searchUserSummary(int userId) {
        return userDao.searchUserSummary(userId);
    }

    //查询部门和员工的数据，并进行合并
    @Override
    public ArrayList<HashMap> searchUserGroupByDept(String keyword) {
        ArrayList<HashMap> list_1 = deptDao.searchDeptMembers(keyword);//通过keyword值获取部门数据
        ArrayList<HashMap> list_2 = userDao.searchUserGroupByDept(keyword);//通过keyword值获取员工数据
        for (HashMap map_1 : list_1) {
            long deptId = (Long) map_1.get("id");//获取部门id
            ArrayList members = new ArrayList();
            for (HashMap map_2 : list_2) {
                long id = (long) map_2.get("deptId");//获取员工数据里的部门id
                if (deptId == id) {//如果部门id和员工表的部门id一致，则将数据记录在数组中
                    members.add(map_2);
                }
            }
            map_1.put("members", members);//
        }
        return list_1;
    }

    //根据userId查询用户信息
    @Override
    public ArrayList<HashMap> searchMembers(List param) {
        return userDao.searchMembers(param);
    }
}
