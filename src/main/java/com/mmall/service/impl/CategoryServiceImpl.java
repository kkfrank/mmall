package com.mmall.service.impl;

import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.dao.CategoryMapper;
import com.mmall.pojo.Category;
import com.mmall.service.CategoryService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service("categoryService")
@Transactional
public class CategoryServiceImpl implements CategoryService{

    private Logger logger = LoggerFactory.getLogger(CategoryServiceImpl.class);

    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public ServerResponse<Category> addCategory(String categoryName, Integer parentId) {
        if(parentId == null || StringUtils.isBlank(categoryName)){
            return ServerResponse.createByErrorCodeMsg(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }

        Category category = new Category();
        category.setName(categoryName);
        category.setParentId(parentId);
        category.setStatus(true);

        // todo check parentId is exist
        int rowCount = categoryMapper.insertSelective(category);
        if(rowCount == 0){
            return ServerResponse.createByErrorMsg("添加分类失败");
        }
        Category insertCategory = categoryMapper.selectByPrimaryKey(category.getId());
        return ServerResponse.createBySuccess(insertCategory);
    }

    @Override
    public ServerResponse<Category> updateCategoryName(Integer id, String name) {
        if(id == null || StringUtils.isBlank(name)){
            return ServerResponse.createByErrorCodeMsg(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }

        Category category = new Category();
        category.setId(id);
        category.setName(name);

        int rowCount = categoryMapper.updateByPrimaryKeySelective(category);
        if(rowCount == 0){
            return ServerResponse.createByErrorMsg("更新失败");
        }
        return ServerResponse.createBySuccess(categoryMapper.selectByPrimaryKey(category.getId()));
    }

    @Override
    public ServerResponse<List<Category>> getChildrenCategory(Integer id) {
        List<Category> categoryList = categoryMapper.selectByParentId(id);
        if(CollectionUtils.isEmpty(categoryList)){
            logger.info("未找到当前分类的子分类");
        }
        return ServerResponse.createBySuccess(categoryList);
    }

    @Override
    public ServerResponse<List<Category>> getChildrenCategoryDeep(Integer parentId) {
        if(parentId == null){
            return ServerResponse.createByErrorCodeMsg(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }

        Set<Category> categorySet = new HashSet<>();
        getChildrenCategoryDeep(categorySet, parentId);
        List<Category> categoryList = new ArrayList<>(categorySet);

        if(CollectionUtils.isEmpty(categoryList)){
            logger.info("未找到当前分类的或其子分类");
        }
        return ServerResponse.createBySuccess(categoryList);
    }

    private Set<Category> getChildrenCategoryDeep(Set<Category> result, Integer parentId){
        if(parentId == null){
            return result;
        }

        List<Category> childList = categoryMapper.selectByParentId(parentId);
        for(Category child : childList){
            result.add(child);
            getChildrenCategoryDeep(result, child.getId());
        }
        return result;
    }
}
