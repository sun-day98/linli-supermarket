package com.linli.supermarket.service;

import com.linli.supermarket.common.PageResult;
import com.linli.supermarket.dto.ProductDTO;
import com.linli.supermarket.dto.ProductQueryDTO;
import com.linli.supermarket.entity.Product;
import com.linli.supermarket.vo.ProductVO;

/** 商品服务。 */
public interface ProductService {

    PageResult<ProductVO> page(ProductQueryDTO dto);

    /** 商品详情（含审核通过的评价）。 */
    ProductVO detail(Long id);

    /** 查询商品实体，不存在返回 null。 */
    Product getById(Long id);

    /** 条件扣库存（防超卖），返回是否扣减成功。 */
    boolean deductStock(Long id, Integer quantity);

    /** 回滚库存（取消订单时调用）。 */
    void rollbackStock(Long id, Integer quantity);

    Long create(ProductDTO dto);

    void update(Long id, ProductDTO dto);

    void delete(Long id);

    void changeStatus(Long id, Integer status);
}
