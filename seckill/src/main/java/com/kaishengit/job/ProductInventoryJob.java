package com.kaishengit.job;

import com.kaishengit.entity.Product;
import com.kaishengit.mapper.ProductMapper;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class ProductInventoryJob implements Job {

    private Logger logger = LoggerFactory.getLogger(ProductInventoryJob.class);

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        JobDataMap jobDataMap = jobExecutionContext.getJobDetail().getJobDataMap();
        String productId = jobDataMap.getString("productId");

        try {
            ApplicationContext ctx = (ApplicationContext) jobExecutionContext.getScheduler().getContext().get("springApplicationContext");
            //Mapper
            ProductMapper productMapper = (ProductMapper) ctx.getBean("productMapper");
            //JedisPool
            JedisPool jedisPool = (JedisPool) ctx.getBean("jedisPool");

            Jedis jedis = jedisPool.getResource();
            Long size = jedis.llen("product:"+productId+":inventory");

            Product product = productMapper.selectByPrimaryKey(Integer.valueOf(productId));
            product.setProductInventory(size.intValue());
            productMapper.updateByPrimaryKey(product);


            logger.info("商品{}修改库存成功{}",productId,size);

        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }
}
