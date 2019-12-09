package com.mmall.service;

import com.github.pagehelper.PageInfo;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.Product;
import com.mmall.vo.ProductDetailVo;

public interface ProductService {

    ServerResponse<ProductDetailVo> getProduct(Integer id, Integer status);

    ServerResponse<PageInfo> searchProduct(Integer productId, String productName, int pageNum, int pageSize);

    ServerResponse<PageInfo> searchProductByKeyword(String keyword,  String orderBy, Integer categoryId, int pageNum, int pageSize);

//    ServerResponse<PageInfo> searchProductByName(String productName, int pageNum, int pageSize);

    ServerResponse<Product> saveOrUpdate(Product product);

    ServerResponse<String> updateSaleStatus(Integer productId, Integer status);

//    ServerResponse<Product> update(Product product);

    ServerResponse<Product> delete(Integer id);
}
