package cn.itcast.core.service;

import org.springframework.beans.factory.annotation.Autowired;

import javax.jms.*;

/**
 * @author huyy
 * @Title: PageAndSolrListener
 * @ProjectName parent
 * @Description: 自定义监听类(业务方法)
 * @date 2018/10/139:48
 */
public class DeleteSolrListener implements MessageListener {

    //注入搜索服务
    @Autowired
    private ItemsearchService itemsearchService;

    @Override
    public void onMessage(Message message) {

        //读取消息
        ObjectMessage objectMessage = (ObjectMessage) message;
        try {
            Long id = (Long) objectMessage.getObject();
            //调用业务方法
            itemsearchService.deleteSolr(id);
        } catch (JMSException e) {
            e.printStackTrace();
        }

    }


}
