package cn.itcast.core.service;

import cn.itcast.core.dao.item.ItemDao;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.item.ItemQuery;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**'
 * 搜索管理
 */
@Service
public class ItemsearchServiceImpl implements  ItemsearchService {

    @Autowired
    private SolrTemplate solrTemplate;


    @Autowired
    private RedisTemplate redisTemplate;

    //定义搜索对象的结构  category:商品分类
    // $scope.searchMap={'keywords':'','category':'','brand':'','spec':{},'price':'','pageNo':1,'pageSize':20,'sort':'','sortField':''};


    //搜索
    public Map<String,Object> search(Map<String,String> searchMap){



        //处理关键字中多余的空格
        String keywords = searchMap.get("keywords").replaceAll(" ","");
        searchMap.put("keywords",keywords);

        //最终返回的map
        Map<String, Object> resultMap = new HashMap<>();

        //高亮查询
        resultMap.putAll(hlSearch(searchMap));
        //普通查询
        // Map<String, Object> resultMap = noSearch(searchMap);

        List<String> categoryList = searchCategoryList(searchMap);
        resultMap.put("categoryList",categoryList);


        if(StringUtils.isNotEmpty(searchMap.get("category"))){
            //如果用户传递了商品分类的信息,根据用户传递的分类进行查找
            resultMap.putAll(searchBrandAndSpecList(searchMap.get("category")));
        }else{
            //如果没传商品分类,根据第一个商品分类找品牌列表和规格列表
            if(categoryList != null && categoryList.size() > 0){
                resultMap.putAll(searchBrandAndSpecList(categoryList.get(0)));
            }
        }

        //普通查询
        // Map<String, Object> resultMap = noSearch(searchMap);
        return resultMap;
    }



    /**
     * 根据分类名称查找品牌列表和规格列表
     * @param category
     * @return
     */
    private Map searchBrandAndSpecList(String category) {
        Map  map = new HashMap();

        Long typeId = (Long)redisTemplate.boundHashOps("itemCat").get(category);

        List<Map> brandList = (List<Map>) redisTemplate.boundHashOps("brandList").get(typeId);


        List<Map> specList = (List<Map>) redisTemplate.boundHashOps("specList").get(typeId);
        map.put("brandList",brandList);
        map.put("specList",specList);
        return map;

    }

    //高亮查询
    public Map<String,Object> hlSearch(Map<String,String> searchMap) {
        Map<String, Object> resultMap = new HashMap<>();
        //关键词搜索
        String keywords = searchMap.get("keywords");
        Criteria criteria = new Criteria("item_keywords").is(keywords);
        //条件对象
        HighlightQuery highlightQuery = new SimpleHighlightQuery(criteria);
        HighlightOptions highlightOptions = new HighlightOptions();
        //设置需要高亮的域
        highlightOptions.addField("item_title");
        //前缀
        highlightOptions.setSimplePrefix("<em style='color:red'>");
        //后缀
        highlightOptions.setSimplePostfix("</em>");

        highlightQuery.setHighlightOptions(highlightOptions);



        //1.2 根据商品分类添加过滤条件
        if(StringUtils.isNotEmpty(searchMap.get("category"))){
            FilterQuery categoryQuery = new SimpleFilterQuery();
            Criteria categorycriteria = new Criteria("item_category").is(searchMap.get("category"));
            categoryQuery.addCriteria(categorycriteria);
            highlightQuery.addFilterQuery(categoryQuery);

        }

        //1.3 根据品牌构建过滤条件
        if(StringUtils.isNotEmpty(searchMap.get("brand"))){
            FilterQuery brandQuery = new SimpleFilterQuery();
            Criteria brandcriteria = new Criteria("item_brand").is(searchMap.get("brand"));
            brandQuery.addCriteria(brandcriteria);
            highlightQuery.addFilterQuery(brandQuery);
        }

        //1.4 规格数据构建过滤条件
        if(StringUtils.isNotEmpty(searchMap.get("spec"))){
            Map<String,String> specMap = JSON.parseObject(searchMap.get("spec"),Map.class);

            for (String key : specMap.keySet()) {
                FilterQuery specQuery = new SimpleFilterQuery();
                Criteria specCriteria = new Criteria("item_spec_" + key).is(specMap.get(key));
                specQuery.addCriteria(specCriteria);
                highlightQuery.addFilterQuery(specQuery);
            }

        }

        //1.5 根据价格过滤
        if(StringUtils.isNotEmpty(searchMap.get("price"))){
            String[] prices = searchMap.get("price").split("-");

            if(!"0".equals(prices[0])){//如果低价格不为0

                FilterQuery priceQuery = new SimpleFilterQuery();
                Criteria priceCriteria = new Criteria("item_price").greaterThanEqual(prices[0]);
                priceQuery.addCriteria(priceCriteria);
                highlightQuery.addFilterQuery(priceQuery);
            }

            if(!"*".equals(prices[1])){ // 如果最高价格不为 *
                FilterQuery priceQuery = new SimpleFilterQuery();
                Criteria priceCriteria = new Criteria("item_price").lessThanEqual(prices[1]);
                priceQuery.addCriteria(priceCriteria);
                highlightQuery.addFilterQuery(priceQuery);
            }

        }



        //分页
        //当前页
        //开始行 偏移量
        Integer pageNo = Integer.parseInt(searchMap.get("pageNo"));
        Integer pageSize = Integer.parseInt(searchMap.get("pageSize"));
        highlightQuery.setOffset((pageNo-1)*pageSize);
        //每页数
        highlightQuery.setRows(pageSize);


        //1.7 设置排序条件
        if(StringUtils.isNotEmpty(searchMap.get("sort")) && StringUtils.isNotEmpty(searchMap.get("sortField"))){

            if("ASC".equals(searchMap.get("sort"))){
                Sort sort = new Sort(Sort.Direction.ASC,"item_" + searchMap.get("sortField"));
                highlightQuery.addSort(sort);
            }
            if("DESC".equals(searchMap.get("sort"))){
                Sort sort = new Sort(Sort.Direction.DESC,"item_" + searchMap.get("sortField"));
                highlightQuery.addSort(sort);
            }
        }

        //执行查询
        HighlightPage<Item> page = solrTemplate.queryForHighlightPage(highlightQuery, Item.class);

        List<HighlightEntry<Item>> highlighted = page.getHighlighted();
        for (HighlightEntry<Item> itemHighlightEntry : highlighted) {
            Item entity = itemHighlightEntry.getEntity();
            List<HighlightEntry.Highlight> highlights = itemHighlightEntry.getHighlights();
            if(null != highlights && highlights.size()>0){
                entity.setTitle(highlights.get(0).getSnipplets().get(0));

            }
        }

        //总条数
        resultMap.put("total",page.getTotalElements());
        //总页数
        resultMap.put("totalPages", page.getTotalPages());
        //结果集
        List<Item> itemList = page.getContent();

        resultMap.put("rows",itemList);
        return resultMap;

    }
    //普通查询
    public Map<String,Object> noSearch(Map<String,String> searchMap){
        Map<String,Object> resultMap = new HashMap<>();
        //关键词搜索
        String keywords = searchMap.get("keywords");
        Criteria criteria = new Criteria("item_keywords").is(keywords);
        //条件对象
        Query query = new SimpleQuery(criteria);
        //分页
        //当前页
        //开始行 偏移量
        Integer pageNo = Integer.parseInt(searchMap.get("pageNo"));
        Integer pageSize = Integer.parseInt(searchMap.get("pageSize"));
        query.setOffset((pageNo-1)*pageSize);
        //每页数
        query.setRows(pageSize);

        ScoredPage<Item> page = solrTemplate.queryForPage(query,Item.class);

        //总条数
        resultMap.put("total",page.getTotalElements());
        //总页数
        resultMap.put("totalPages", page.getTotalPages());
        //结果集
        List<Item> itemList = page.getContent();

        resultMap.put("rows",itemList);
        return resultMap;
    }



    /**
     * 根据关键字分组查询商品分类列表
     * @param searchMap : 关键字的查询条件
     * @return : 分类的列表
     */
    private List<String> searchCategoryList(Map<String,String> searchMap){

        //返回的结果
        List<String> categoryList = new ArrayList<>();

        //关键词搜索
        String keywords = searchMap.get("keywords");
        Criteria criteria = new Criteria("item_keywords").is(keywords);
        //条件对象
        Query query = new SimpleQuery(criteria);

        //添加分组选项
        GroupOptions groupOption = new GroupOptions().addGroupByField("item_category");
        query.setGroupOptions(groupOption);


        GroupPage<Item> groupPage = solrTemplate.queryForGroupPage(query, Item.class);

        GroupResult<Item> groupResult = groupPage.getGroupResult("item_category");

        Page<GroupEntry<Item>> entries = groupResult.getGroupEntries();

        List<GroupEntry<Item>> content = entries.getContent();

        for (GroupEntry<Item> itemGroupEntry : content) {
            String groupValue = itemGroupEntry.getGroupValue();
            categoryList.add(groupValue);
        }


        return categoryList;
    }


    @Autowired
    private ItemDao itemDao;


    /**
     * 根据商品id,导入solr索引库
     *
     * @param id
     */
    @Override
    public void updateSolr(long id) {
        //1.根据商品spu找到sku列表
        ItemQuery query = new ItemQuery();
        query.createCriteria().andGoodsIdEqualTo(id);
        List<Item> itemList = itemDao.selectByExample(query);

        solrTemplate.saveBeans(itemList);
        solrTemplate.commit();
    }

    /**
     * 根据商品id,删除solr索引库
     *
     * @param id
     */
    @Override
    public void deleteSolr(long id) {
        SolrDataQuery query = new SimpleQuery();
        Criteria criteria = new Criteria("item_goodsid").is(id);
        query.addCriteria(criteria);
        solrTemplate.delete(query);
        solrTemplate.commit();
    }


}
