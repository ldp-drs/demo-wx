package com.ldp.emos.wx;

import cn.hutool.core.util.IdUtil;
import com.ldp.emos.wx.db.pojo.MessageEntity;
import com.ldp.emos.wx.db.pojo.MessageRefEntity;
import com.ldp.emos.wx.db.pojo.TbMeeting;
import com.ldp.emos.wx.service.MessageService;
import com.ldp.emos.wx.service.MettingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;

@SpringBootTest
class EmosWxApiApplicationTests {

    @Autowired
    private MessageService messageService;

    @Autowired
    private MettingService mettingService;

    @Test
    void contextLoads() {
        for (int i = 1; i <= 100; i++) {
            MessageEntity message = new MessageEntity();
            message.setUuid(IdUtil.simpleUUID());
            message.setSenderId(0);
            message.setSenderName("系统消息");
            message.setMsg("这是第" + i + "条测试消息");
            message.setSendTime(new Date());
            String id = messageService.insertMessage(message);

            MessageRefEntity ref = new MessageRefEntity();
            ref.setMessageId(id);
            ref.setReceiverId(10);
            ref.setLastFlag(true);
            ref.setReadFlag(false);
            messageService.inserRef(ref);
        }
    }

    @Test
    void createMeetingData() {
        for (int i = 1; i <= 100; i++) {
            TbMeeting meeting = new TbMeeting();
            meeting.setId((long) i);
            meeting.setUuid(IdUtil.simpleUUID());
            meeting.setTitle("测试会议" + i);
            meeting.setCreatorId(10L);
            meeting.setPlace("线上会议室");
            meeting.setStart("08:30");
            meeting.setEnd("10:30");
            meeting.setType((short) 1);
            meeting.setMembers("[10,11]");//参会人员
            meeting.setDesc("会议研讨Ludp项目上线测试");
            meeting.setInstanceId(IdUtil.simpleUUID());
            meeting.setStatus((short) 3);
            meeting.setDate("2021-12-08");
            meeting.setCreateTime(new Date());
            mettingService.insertMessting(meeting);
        }
    }
}
