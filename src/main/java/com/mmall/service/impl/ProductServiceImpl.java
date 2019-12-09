package com.mmall.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.dao.CategoryMapper;
import com.mmall.dao.ProductMapper;
import com.mmall.pojo.Category;
import com.mmall.pojo.Product;
import com.mmall.service.CategoryService;
import com.mmall.service.ProductService;
import com.mmall.util.DateTimeUtil;
import com.mmall.util.PropertiesUtil;
import com.mmall.vo.ProductDetailVo;
import com.mmall.vo.ProductListVo;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service("productService")
@Transactional
public class ProductServiceImpl implements ProductService{

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private CategoryService categoryService;

    @Override
    public ServerResponse<ProductDetailVo> getProduct(Integer id, Integer status) {
        if(id == null){
            return ServerResponse.createByErrorCodeMsg(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Product product = productMapper.selectByPrimaryKey(id);
        if(product == null){
            return ServerResponse.createByErrorMsg("产品不存在或者已下架");
        }
        if(status != null){
            if(!status.equals(product.getStatus())){
                return ServerResponse.createByErrorMsg("产品已下架");
            }
        }
        ProductDetailVo productDetailVo = assembleProductDetailVo(product);
        return ServerResponse.createBySuccess(productDetailVo);
    }

    @Override
    public ServerResponse<PageInfo> searchProduct(Integer productId, String productName, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);

        List<Product> productList = new ArrayList<>();
        if(productId != null){
            Product product = productMapper.selectByPrimaryKey(productId);
            if(product != null){
                productList.add(product);
            }
        }else{
            if(StringUtils.isNoneBlank(productName)){
                productName = new StringBuilder().append("%").append(productName).append("%").toString();
            }
            productList = productMapper.searchProduct(productName);
        }


        List<ProductListVo> productListVoList = new ArrayList<>();
        for(Product product : productList){
            productListVoList.add(assembleProductListVo(product));
        }
        PageInfo pageInfo = new PageInfo(productList);
        pageInfo.setList(productListVoList);
        return ServerResponse.createBySuccess(pageInfo);
    }


    @Override
    public ServerResponse<PageInfo> searchProductByKeyword(String keyword, String orderBy, Integer categoryId, int pageNum, int pageSize) {
        if(StringUtils.isBlank(keyword) && categoryId == null){
            return ServerResponse.createByErrorCodeMsg(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        List<Category> categoryList = new ArrayList<>();
        if (categoryId != null){
            Category category = categoryMapper.selectByPrimaryKey(categoryId);
            if(category == null && StringUtils.isBlank((keyword))){
                PageHelper.startPage(pageNum, pageSize);
                List<ProductListVo> productListVos = new ArrayList<>();
                PageInfo pageInfo = new PageInfo(productListVos);
                return ServerResponse.createBySuccess(pageInfo);//todo
            }
            categoryList = categoryService.getChildrenCategoryDeep(categoryId).getData();
        }

        if(StringUtils.isNoneBlank(keyword)){
            keyword = new StringBuilder().append("%").append(keyword).append("%").toString();
        }
        PageHelper.startPage(pageNum, pageSize);
        //排序
        if(StringUtils.isNoneBlank(orderBy)){
            if(Const.ProductListOrderBy.PRICE_ASC.getName().equals(orderBy) || Const.ProductListOrderBy.PRICE_DESC.getName().equals(orderBy)){
                String[] orderByArr = orderBy.split("_");
                PageHelper.orderBy(orderByArr[0] + " "+orderByArr[1]);
                //PageHelper.orderBy("price desc");
            }
        }

        List<Integer> categoryIds = new ArrayList<>();
        for(Category category : categoryList){
            categoryIds.add(category.getId());
        }
        List<Product> productList = productMapper.selectByNameAndCategoryIds(StringUtils.isBlank(keyword) ? null :keyword,
                CollectionUtils.isEmpty(categoryIds) ? null : categoryIds);

        List<ProductListVo> productListVoList = new ArrayList<>();
        for(Product product : productList){
            ProductListVo listVo = assembleProductListVo(product);
            productListVoList.add(listVo);
        }
        PageInfo pageInfo = new PageInfo(productList);
        pageInfo.setList(productListVoList);
        return ServerResponse.createBySuccess(pageInfo);
    }

    @Override
    public ServerResponse<Product> saveOrUpdate(Product product) {
        if(product == null){
            return ServerResponse.createByErrorCodeMsg(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        //约定主图为子图数组的第一个
        if(StringUtils.isNoneBlank(product.getSubImages())){
            String[] subImageArr = product.getSubImages().split(",");//约定,分割
            if(subImageArr.length > 0){
                product.setMainImage(subImageArr[0]);
            }
        }

        if(product.getId() != null){// create
            int rowCount = productMapper.updateByPrimaryKeySelective(product);
            if(rowCount == 0){
                return ServerResponse.createByErrorMsg("更新失败");
            }
        }else{// update
            int rowCount = productMapper.insert(product);
            if(rowCount == 0){
                return ServerResponse.createByErrorMsg("创建失败");
            }
        }
        Product newProduct = productMapper.selectByPrimaryKey(product.getId());
        return ServerResponse.createBySuccess(newProduct);
    }

//    @Override
//    public ServerResponse<Product> update(Product product) {
//        int rowCount = productMapper.updateByPrimaryKeySelective(product);
//        if(rowCount == 0){
//            return ServerResponse.createByErrorMsg("更新失败");
//        }
//        Product insertProduct = productMapper.selectByPrimaryKey(product.getId());
//        return ServerResponse.createBySuccess(insertProduct);
//    }


    @Override
    public ServerResponse<String> updateSaleStatus(Integer productId, Integer status) {
        if(productId == null || status == null){
            return ServerResponse.createByErrorCodeMsg(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }

        Product product = new Product();
        product.setId(productId);
        product.setStatus(status);
        int rowCount = productMapper.updateByPrimaryKeySelective(product);
        if(rowCount == 0){
            return ServerResponse.createByErrorMsg("更新失败");
        }
        return ServerResponse.createBySuccessMsg("更新成功");
    }

    @Override
    public ServerResponse<Product> delete(Integer id) {
        return null;
    }


    private ProductDetailVo assembleProductDetailVo(Product product){
        if(product == null){
            return null;
        }
        ProductDetailVo productDetailVo = new ProductDetailVo();
        productDetailVo.setId(product.getId());
        productDetailVo.setCategoryId(product.getCategoryId());
        productDetailVo.setName(product.getName());
        productDetailVo.setSubtitle(product.getSubtitle());
        productDetailVo.setMainImage(product.getMainImage());
        productDetailVo.setSubImages(product.getSubImages());
        productDetailVo.setDetail(product.getDetail());
        productDetailVo.setPrice(product.getPrice());
        productDetailVo.setStock(product.getStock());
        productDetailVo.setStatus(product.getStatus());

        productDetailVo.setCreateTime(DateTimeUtil.dateToStr(product.getCreateTime()));
        productDetailVo.setUpdateTime(DateTimeUtil.dateToStr(product.getUpdateTime()));

        productDetailVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));

        Category category = categoryMapper.selectByPrimaryKey(product.getCategoryId());
        if(category == null){// no
            productDetailVo.setParentCategoryId(0);
        }else{
            productDetailVo.setParentCategoryId(category.getParentId());
        }
        return productDetailVo;
    }

    private ProductListVo assembleProductListVo(Product product){
        if(product == null){
            return null;
        }
        ProductListVo productListVo = new ProductListVo();
        productListVo.setId(product.getId());
        productListVo.setCategoryId(product.getCategoryId());
        productListVo.setName(product.getName());
        productListVo.setSubtitle(product.getSubtitle());
        productListVo.setMainImage(product.getMainImage());
        productListVo.setPrice(product.getPrice());
        productListVo.setStatus(product.getStatus());
        productListVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));

        return productListVo;
    }
}
