package cn.itcast.core.service;

import org.springframework.beans.factory.annotation.Autowired;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

/**
 * @author huyy
 * @Title: PageAndSolrListener
 * @ProjectName parent
 * @Description: 自定义监听类(业务方法)
 * @date 2018/10/139:48
 */
public class PageAndSolrListener implements MessageListener {

    //注入搜索服务
    @Autowired
    private ItemsearchService itemsearchService;

    @Override
    public void onMessage(Message message) {

        //读取消息
        TextMessage textMessage = (TextMessage) message;
        try {
            String id = textMessage.getText();
            //调用业务方法
            itemsearchService.updateSolr(Long.parseLong(id));
        } catch (JMSException e) {
            e.printStackTrace();
        }

    }


}
