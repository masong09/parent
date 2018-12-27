package cn.itcast.core.service;

import cn.itcast.core.dao.ad.ContentDao;
import cn.itcast.core.pojo.ad.Content;
import cn.itcast.core.pojo.ad.ContentQuery;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import entity.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class ContentServiceImpl implements ContentService {
	
	@Autowired
	private ContentDao contentDao;
	@Autowired
	private RedisTemplate redisTemplate;

	@Override
	public List<Content> findAll() {
		List<Content> list = contentDao.selectByExample(null);
		return list;
	}

	@Override
	public PageResult findPage(Content content, Integer pageNum, Integer pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		Page<Content> page = (Page<Content>)contentDao.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Override
	public void add(Content content) {
		contentDao.insertSelective(content);
	}

	@Override
	public void edit(Content content) {
		contentDao.updateByPrimaryKeySelective(content);
	}

	@Override
	public Content findOne(Long id) {
		Content content = contentDao.selectByPrimaryKey(id);
		return content;
	}

	@Override
	public void delAll(Long[] ids) {
		if(ids != null){
			for(Long id : ids){
				contentDao.deleteByPrimaryKey(id);
			}
		}
	}

	@Override
	public List<Content> findByCategoryId(Long categoryId) {
		//1:从缓存中获取
		//redisTemplate.boundValueOps("content:" + categoryId).get();
		List<Content> contentList = (List<Content>) redisTemplate.boundHashOps("content").get(categoryId);
		if(null == contentList || contentList.size() == 0){
			//3:没有 查询数据库
			ContentQuery contentQuery = new ContentQuery();
			contentQuery.createCriteria().andCategoryIdEqualTo(categoryId);
			//排序
			contentQuery.setOrderByClause("sort_order desc");

			contentList = contentDao.selectByExample(contentQuery);
			//4:再保存缓存一份  时间一天
			redisTemplate.boundHashOps("content").put(categoryId,contentList);
			redisTemplate.boundHashOps("content").expire(30, TimeUnit.SECONDS);
		}
		//5:有  直接返回
		return contentList;



	}
}
