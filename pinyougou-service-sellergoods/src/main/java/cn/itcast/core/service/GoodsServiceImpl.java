package cn.itcast.core.service;

import cn.itcast.core.dao.good.BrandDao;
import cn.itcast.core.dao.good.GoodsDao;
import cn.itcast.core.dao.good.GoodsDescDao;
import cn.itcast.core.dao.item.ItemCatDao;
import cn.itcast.core.dao.item.ItemDao;
import cn.itcast.core.dao.seller.SellerDao;
import cn.itcast.core.pojo.good.Goods;
import cn.itcast.core.pojo.good.GoodsQuery;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.item.ItemQuery;
import cn.itcast.core.pojogroup.GoodsVo;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import entity.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.data.solr.core.query.SolrDataQuery;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import java.util.*;

/**
 * 商品管理
 */
@Service
public class GoodsServiceImpl  implements  GoodsService{

    @Autowired
    private GoodsDao goodsDao;
    @Autowired
    private GoodsDescDao goodsDescDao;
    @Autowired
    private ItemDao itemDao;

    @Autowired
    private ItemCatDao itemCatDao;
    @Autowired
    private SellerDao sellerDao;
    @Autowired
    private BrandDao brandDao;


    @Override
    public PageResult search(Integer page, Integer rows, Goods goods) {
        //分页插件
        PageHelper.startPage(page, rows);
        //排序
        PageHelper.orderBy("id desc");
        //条件对象
        GoodsQuery goodsQuery = new GoodsQuery();
        GoodsQuery.Criteria criteria = goodsQuery.createCriteria();
        //判断
        if (null != goods.getAuditStatus() && !"".equals(goods.getAuditStatus())) {
            criteria.andAuditStatusEqualTo(goods.getAuditStatus());
        }
        if (null != goods.getGoodsName() && !"".equals(goods.getGoodsName().trim())) {
            criteria.andGoodsNameLike("%" + goods.getGoodsName().trim() + "%");
        }
        //判断  系统是不是商家  登陆的
        if(null != goods.getSellerId() && !"".equals(goods.getSellerId())){
            criteria.andSellerIdEqualTo(goods.getSellerId());

        }
        //只查询不删除的  已经删除了的不要了
        criteria.andIsDeleteIsNull();

        Page<Goods> p = (Page<Goods>) goodsDao.selectByExample(goodsQuery);

        return new PageResult(p.getTotal(), p.getResult());
    }



    //添加商品
    public void add(GoodsVo vo){
        //商品表
       //状态  0:未审核  1:审核通过  2  3
        vo.getGoods().setAuditStatus("0");
        //保存  返回Id
        goodsDao.insertSelective(vo.getGoods());

        //商品详情表
        vo.getGoodsDesc().setGoodsId(vo.getGoods().getId());
        goodsDescDao.insertSelective(vo.getGoodsDesc());

        //是否启用规格
        //TODO 库存表 (多张数据)
        /*List<Item> itemList = vo.getItemList();*/
        //是否启用规格
        if ("1".equals(vo.getGoods().getIsEnableSpec())) {
            // 库存表 (多张数据)
            List<Item> itemList = vo.getItemList();
            for (Item item : itemList) {
                //ID
                //标题  == 商品名称 规格1 规格2 规格3 规格4
                //  Apple 苹果 iPhone Xs Max 手机 金色 全网通 512GB
                String title = vo.getGoods().getGoodsName();
                //{"机身内存":"16G","网络":"联通3G"}
                Map<String, String> specMap = JSON.parseObject(item.getSpec(), Map.class);
                Set<Map.Entry<String, String>> entries = specMap.entrySet();
                for (Map.Entry<String, String> entry : entries) {
                    title += " " + entry.getValue();
                }
                item.setTitle(title);
                //卖点
                item.setSellPoint(vo.getGoods().getCaption());
                //图片
                List<Map> listMap = JSON.parseArray(vo.getGoodsDesc().getItemImages(), Map.class);
                if (null != listMap && listMap.size() > 0) {
                    item.setImage((String) listMap.get(0).get("url"));
                }
                //第三级分类ID
                item.setCategoryid(vo.getGoods().getCategory3Id());
                //第三级分类名称
                item.setCategory(itemCatDao.selectByPrimaryKey(vo.getGoods().getCategory3Id()).getName());
                //添加时间
                item.setCreateTime(new Date());
                //更新时间
                item.setUpdateTime(new Date());
                //商品表的ID
                item.setGoodsId(vo.getGoods().getId());
                //商家的ID
                item.setSellerId(vo.getGoods().getSellerId());
                //商家的名称
                item.setSeller(sellerDao.selectByPrimaryKey(vo.getGoods().getSellerId()).getNickName());
                //品牌名称
                item.setBrand(brandDao.selectByPrimaryKey(vo.getGoods().getBrandId()).getName());
                //保存库存表
                itemDao.insertSelective(item);
            }
        }

    }

    @Override
    public GoodsVo findOne(Long id) {
        GoodsVo vo = new GoodsVo();
        vo.setGoods(goodsDao.selectByPrimaryKey(id));
        vo.setGoodsDesc(goodsDescDao.selectByPrimaryKey(id));
        ItemQuery itemQuery = new ItemQuery();
        itemQuery.createCriteria().andGoodsIdEqualTo(id);
        vo.setItemList(itemDao.selectByExample(itemQuery));
        return vo;
    }
    @Autowired
    private SolrTemplate solrTemplate;


  /*  @Autowired
    private ItemPageService itemPageService;*/

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private Destination topicPageAndSolrDestination;

    @Override
    public void updateStatus(Long[] ids, String status) {
        Goods goods = new Goods();
        goods.setAuditStatus(status);
        for (Long id : ids) {
            goods.setId(id);
            //1:开始审核
            goodsDao.updateByPrimaryKeySelective(goods);

        }
        //只有在审核通过的情况下 才做如下事情
        if("1".equals(status)){
            //TODO 2:创建索引
            //将审核通过的商品导入到solr索引库中
           /* updateSolr(ids);*/

            //TODO 3:静态化
            //TODO 3:静态化
            //TODO 3:静态化
            for (Long id : ids) {
                //使用activemq发送消息到消息中间件
                jmsTemplate.send(topicPageAndSolrDestination, new MessageCreator() {
                    @Override
                    public Message createMessage(Session session) throws JMSException {
                        //必须将你要发送的消息返回
                        return session.createTextMessage(id + "");
                    }
                });
//                itemPageService.genItemPage(id);
            }
        }

    }

    /**
     * 商品审核通过导入solr索引库
     * @param ids : spu的id列表
     */
    public void updateSolr(Long[] ids){
        //1.根据商品spu找到sku列表
        ItemQuery query = new ItemQuery();
        query.createCriteria().andGoodsIdIn(Arrays.asList(ids));
        List<Item> itemList = itemDao.selectByExample(query);

        solrTemplate.saveBeans(itemList);
        solrTemplate.commit();

    }


    @Autowired
    private Destination queueSolrDeleteDestination;
    /**
     * 删除商品
     * @param ids : 商品的spu的id列表
     */
    @Override
    public void delete(Long[] ids) {
        Goods goods = new Goods();
        goods.setIsDelete("1");
        for (Long id : ids) {
            goods.setId(id);
            //1:删除  修改 isDelete 为 1
            goodsDao.updateByPrimaryKeySelective(goods);
            //TODO 2:删除索引

        /*    deleteSolr(id);*/
            //TODO 3:静态化 ( 待议)

            jmsTemplate.send(queueSolrDeleteDestination, new MessageCreator() {
                @Override
                public Message createMessage(Session session) throws JMSException {
                    return session.createObjectMessage(id);
                }
            });

        }
    }

    /**
     * 根据删除的商品spu,删除solr索引库
     * @param id
     */
/*    private void deleteSolr(Long id) {
        SolrDataQuery query = new SimpleQuery();
        Criteria criteria = new Criteria("item_goodsid").is(id);
        query.addCriteria(criteria);
        solrTemplate.delete(query);
        solrTemplate.commit();
    }*/


}
