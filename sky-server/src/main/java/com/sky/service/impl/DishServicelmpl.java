package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.entity.Setmeal;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.DishflavorMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.DisService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j

public class DishServicelmpl implements DisService {

    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishflavorMapper dishflavorMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;
    @Autowired
    private SetmealMapper setmealMapper;
    /**
     * 新增菜品和对应口味数据
     * @param dishDTO
     */
    @Override
    @Transactional
    public void saveWithFlavor(DishDTO dishDTO) {
        //向菜品表插入一条数据
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO,dish);
        dishMapper.insert(dish);
        //向插入菜品表获得菜品id，将id作为查找依据为菜品增加口味
        long dishId = dish.getId();
        //向口味表插入n条数据
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if(flavors!=null && flavors.size() > 0)
        {
            //向口味表插入n条数据
            //为每个口味赋值lamuda表达式
            flavors.forEach(dishFlavor -> {
                dishFlavor.setDishId(dishId);
            });
            dishflavorMapper.insertBatch(flavors);

        }


    }
    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(),dishPageQueryDTO.getPageSize());
        Page<DishVO> page=dishMapper.pageQuery(dishPageQueryDTO);
        return new PageResult(page.getTotal(),page.getResult());
    }

    /**
     * 菜品的批量删除
     * @param ids
     */
    @Override
    public void deleteBatch(List<Long> ids) {
        //判断菜品是否能删除--是否存在起售中的菜品
        for (Long id : ids) {
            Dish dish=dishMapper.getById(id);
            if(dish.getStatus()== StatusConstant.ENABLE) {
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }
        // 判断当前菜品是否被套餐关联
        List<Long> setmealIds = setmealDishMapper.getSetmealIdByDishIds(ids);
        if(setmealIds!=null &&setmealIds.size()>0){
            //当前菜品被套餐关联了
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }
        //删除菜品表中的菜品数据
        for (Long id : ids) {
            dishMapper.deleteById(id);
            //删除菜品关联的口味数据
            dishflavorMapper.deleteByDishId(id);
        }
    }

    @Override
    public DishVO getByIdWithFlavor(Long id) {
        //根据菜品id查询菜品数据
        Dish dish = dishMapper.getById(id);

        //根据菜品id查询口味数据
        List<DishFlavor> dishFlavors=dishflavorMapper.getByDishId(id);
        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish,dishVO);
        dishVO.setFlavors(dishFlavors);
        return dishVO;
    }

    /**
     * 根据id修改菜品信息和对应口味信息
     * @param dishDTO
     */
    @Override
    public void updateWithFlavor(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO,dish);
        //修改菜品表基本信息
        dishMapper.update(dish);
        //删除原有的口味数据再插入
        dishflavorMapper.getByDishId(dishDTO.getId());
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if(flavors!=null && flavors.size() > 0)
        {
            //向口味表插入n条数据
            //为每个口味赋值lamuda表达式
            flavors.forEach(dishFlavor -> {
                dishFlavor.setDishId(dishDTO.getId());
            });
            dishflavorMapper.insertBatch(flavors);

        }

    }
    /**
     * 起售停售菜品
     * @param status
     * @param id
     */
    @Override
    public void StartorStop(Integer status, Long id) {
        //创建一个菜品对象将数据传入菜品
        Dish dish = Dish.builder()
                    .status(status)
                    .id(id)
                    .build();
        if(dish.getStatus()==StatusConstant.DISABLE) {
            List<Long> ids = new ArrayList<>();
            ids.add(id);
            //根据菜品id查询关联的套餐id
            List<Long> setmealIds = setmealDishMapper.getSetmealIdByDishIds(ids);
            //依次更改跟菜品相关的套餐状态
            for (Long setmealId : setmealIds) {
                Setmeal setmeal = Setmeal.builder().id(setmealId).status(status).build();
                setmealMapper.update(setmeal);
            }
        }
        //更新菜品状态
        dishMapper.update(dish);
    }

    @Override
    public List<Dish> lists(Long CategoryId) {
        Dish dish = Dish.builder()
                    .categoryId(CategoryId)
                    .status(StatusConstant.ENABLE)
                    .build();

        return dishMapper.list(dish);
    }
}
