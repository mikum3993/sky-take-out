package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.DishFlavorsMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * @program: sky-take-out
 * @description: 菜品及口味管理
 * @author: {}
 * @create: 2024/4/13 20:13
 */
@Service
@Slf4j
public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private DishFlavorsMapper dishFlavorsMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;

    /**
     * 新增菜品和对应口味数据
     *
     * @param dishDTO
     */
    @Transactional
    public void saveWIthFlavor(DishDTO dishDTO) {

        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);

        // 向菜品表插入数据
        dishMapper.insert(dish);

        // 获得insert语句生成的主键值
        Long id = dish.getId();

        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && flavors.size() > 0) {
            flavors.forEach(dishflavors ->
                    dishflavors.setDishId(id));
            // 向口味表插入数据
            dishFlavorsMapper.inserBatch(flavors);
        }


    }

    @Override
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());
        Page<DishVO> dishVO = dishFlavorsMapper.pageQuery(dishPageQueryDTO);

        return new PageResult(dishVO.getTotal(), dishVO.getResult());
    }

    /**
     * 菜品批量删除
     *
     * @param ids
     */
    public void deleteBatch(List<Long> ids) {
        // 判断当前是否能够删除--判断是否在起售中的菜品??
        for (Long id : ids) {
            Dish dish = dishMapper.getById(id);
            if (dish.getStatus().equals(StatusConstant.ENABLE)) {
                // 当前菜品不能删除
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }
        // 判断当前菜品是否能够删除--是否被关联
        List<Long> setmealDishIds = setmealDishMapper.getSetmealDishIds(ids);
        if (setmealDishIds != null && setmealDishIds.size() > 0) {
            // 当前菜品不能删除
            throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
        }

        // 删除菜品表的数据
        // for (Long id : ids) {
        //     dishMapper.deleteById(id);
        //     // 删除菜品管理口味数据
        //     dishFlavorsMapper.deleteByDishId(id);
        // }

        // delete from dish where id in (?,?,?,?)
        dishMapper.deleteByIds(ids);

        // 删除口味
        dishFlavorsMapper.deleteByDIshIds(ids);
    }

    /**
     * 根据id查询菜品
     */
    public DishVO getByIdWithFlavor(Long id) {
        // 根据id查询菜品数据
        Dish dish = dishMapper.getById(id);
        // 根据菜品id查询口味数据
        List<DishFlavor> dishFlavors = dishFlavorsMapper.getByDishId(id);

        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish, dishVO);

        dishVO.setFlavors(dishFlavors);
        // 将查询到的数据封装到VO
        return dishVO;
    }

    /**
     * 根据id修改菜品和口味的信息
     *
     * @param dishDTO
     */
    public void updateWithFlavor(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        // 修改菜品表
        dishMapper.update(dish);

        // 删除原有的口味数据
        dishFlavorsMapper.deleteByDishId(dishDTO.getId());

        // 重新插入口味数据
        List<DishFlavor> flavors = dishDTO.getFlavors();
        dishFlavorsMapper.inserBatch(flavors);
        if (flavors != null && flavors.size() > 0) {
            flavors.forEach(dishflavors -> dishflavors.setDishId(dishDTO.getId()));
            // 向口味表插入数据
            dishFlavorsMapper.inserBatch(flavors);
        }
    }

    /**
     * 条件查询菜品和口味
     * @param dish
     * @return
     */
    public List<DishVO> listWithFlavor(Dish dish) {
        List<Dish> dishList = dishMapper.list(dish);

        List<DishVO> dishVOList = new ArrayList<>();

        for (Dish d : dishList) {
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties(d,dishVO);

            //根据菜品id查询对应的口味
            List<DishFlavor> flavors = dishFlavorsMapper.getByDishId(d.getId());

            dishVO.setFlavors(flavors);
            dishVOList.add(dishVO);
        }
        return dishVOList;
    }
}
