package com.linli.supermarket.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.linli.supermarket.entity.Product;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/** 商品 Mapper，含防超卖的条件扣减。 */
public interface ProductMapper extends BaseMapper<Product> {

    /**
     * 条件扣库存：仅当库存充足且商品上架时扣减成功，按影响行数判定，防止超卖。
     *
     * @return 影响行数，1 表示成功，0 表示库存不足或商品下架
     */
    @Update("UPDATE product SET stock = stock - #{n}, sales = sales + #{n} "
            + "WHERE id = #{id} AND status = 1 AND stock >= #{n}")
    int deductStock(@Param("id") Long id, @Param("n") Integer n);

    /**
     * 取消订单时回滚库存，并同步扣减销量（避免取消后销量虚高）。
     *
     * <p>幂等性由上层 OrderServiceImpl.cancel() 的 cancelCas 抢占保证：只有抢占成功的请求
     * 才会调用本方法，重复取消不会再执行回滚。此处用 GREATEST 兜底，防止销量被减成负数。</p>
     */
    @Update("UPDATE product SET stock = stock + #{n}, "
            + "sales = GREATEST(COALESCE(sales, 0) - #{n}, 0) WHERE id = #{id}")
    int rollbackStock(@Param("id") Long id, @Param("n") Integer n);
}
