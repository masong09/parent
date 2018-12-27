package cn.itcast.core.service;

import cn.itcast.core.dao.good.GoodsDao;
import cn.itcast.core.dao.good.GoodsDescDao;
import cn.itcast.core.dao.item.ItemCatDao;
import cn.itcast.core.dao.item.ItemDao;
import cn.itcast.core.pojo.good.Goods;
import cn.itcast.core.pojo.good.GoodsDesc;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.item.ItemQuery;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfig;

import javax.servlet.ServletContext;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author huyy
 * @Title: ItemPageServiceImpl
 * @ProjectName parent
 * @Description: 生成静态页面的服务实现
 * @date 2018/10/129:16
 */
@Service
public class ItemPageServiceImpl implements ItemPageService,ServletContextAware {

    @Autowired
    private FreeMarkerConfig freeMarkerConfigurer;

    @Autowired
    private ItemDao itemDao;

    @Autowired
    private ItemCatDao itemCatDao;

    @Autowired
    private GoodsDao goodsDao;

    @Autowired
    private GoodsDescDao goodsDescDao;

    //记录servletContext对象
    private ServletContext servletContext;

    /**
     * 根据spu的id生成静态页面
     *
     * @param goodsId
     */
    @Override
    public void genItemPage(Long goodsId) {

        try {
            //1. 获取configuration对象
            Configuration configuration = freeMarkerConfigurer.getConfiguration();

            //2. 获取详情页面的模板
            Template template = configuration.getTemplate("item.ftl");

            //3. 准备需要的数据: tb_item  tb_goodsDesc  tb_itemCat tb_goods
            Map dataModel = new HashMap();
            Goods goods = goodsDao.selectByPrimaryKey(goodsId);
            dataModel.put("goods",goods);

            GoodsDesc goodsDesc = goodsDescDao.selectByPrimaryKey(goodsId);
            //商品的描述数据
            dataModel.put("goodsDesc",goodsDesc);

            ItemQuery query = new ItemQuery();
            ItemQuery.Criteria criteria = query.createCriteria();
            criteria.andGoodsIdEqualTo(goodsId);// 商品id
            criteria.andStatusEqualTo("1");//状态
            criteria.andNumGreaterThan(0);//库存大于0
            query.setOrderByClause("is_default desc");//默认的拍到第一个
            List<Item> itemList = itemDao.selectByExample(query);
            dataModel.put("itemList",itemList);

            //商品分类的数据
            String itemCat1 = itemCatDao.selectByPrimaryKey(goods.getCategory1Id()).getName();
            String itemCat2 = itemCatDao.selectByPrimaryKey(goods.getCategory2Id()).getName();
            String itemCat3 = itemCatDao.selectByPrimaryKey(goods.getCategory3Id()).getName();

            dataModel.put("itemCat1",itemCat1);
            dataModel.put("itemCat2",itemCat2);
            dataModel.put("itemCat3",itemCat3);

            //4. 创建输出流  :  webapp/goodsId.html
            String realPath = servletContext.getRealPath("/");
            String path  = realPath + "/" + goodsId + ".html";
            System.out.println("文件路径: " + path);
            Writer out = new FileWriter(new File(path));
//            Writer out = new OutputStreamWriter(new File())

            //5.template.process方法,进行html页面的生成
            template.process(dataModel,out);

            //6.关闭流
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Set the {@link ServletContext} that this object runs in.
     * <p>Invoked after population of normal bean properties but before an init
     * callback like InitializingBean's {@code afterPropertiesSet} or a
     * custom init-method. Invoked after ApplicationContextAware's
     * {@code setApplicationContext}.
     *
     * @param servletContext ServletContext object to be used by this object
     * @see InitializingBean#afterPropertiesSet
     * @see ApplicationContextAware#setApplicationContext
     */
    @Override
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }
}
