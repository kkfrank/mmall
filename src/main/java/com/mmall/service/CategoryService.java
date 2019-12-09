package com.mmall.service;

import com.mmall.common.ServerResponse;
import com.mmall.pojo.Category;

import java.util.List;

public interface CategoryService {

    ServerResponse<Category> addCategory(String categoryName, Integer parentId);

    ServerResponse<Category> updateCategoryName(Integer id, String name);

    ServerResponse<List<Category>> getChildrenCategory(Integer id);

    ServerResponse<List<Category>> getChildrenCategoryDeep(Integer parentId);

}
