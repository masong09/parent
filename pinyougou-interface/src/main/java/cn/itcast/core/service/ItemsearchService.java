package cn.itcast.core.service;

import java.util.Map;

public interface ItemsearchService  {

    public Map<String,Object> search(Map<String,String> searchMap);

    /**
     * 根据商品id,导入solr索引库
     * @param id
     */
    void updateSolr(long id);

    /**
     * 根据商品id,删除solr索引库
     * @param l
     */
    void deleteSolr(long l);
}
