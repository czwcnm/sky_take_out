package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DisService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/dish")
@Api(tags = "菜品相关接口")
@Slf4j
public class DishController {

    @Autowired
    private DisService disService;

    /**
     * 新增菜品
     * @param dishDTO
     * @return
     */
    @PostMapping
    @ApiOperation("新增菜品")
    public Result save(@RequestBody DishDTO dishDTO)
    {

        log.info("新增菜品:{}",dishDTO);
        disService.saveWithFlavor(dishDTO);
        return Result.success();
    }
    @GetMapping("/page")
    @ApiOperation("菜品分页查询")
     public Result<PageResult>page(DishPageQueryDTO dishPageQueryDTO){
        log.info("菜品分页查询",dishPageQueryDTO);
        PageResult result = disService.pageQuery(dishPageQueryDTO);
        return Result.success(result);
     }

     @DeleteMapping
     @ApiOperation("菜品批量删除")
     public Result delete(@RequestParam List<Long> ids){
        log.info("菜品的批量删除：{}",ids);
        disService.deleteBatch(ids);
        return Result.success();
     }
     @GetMapping("/{id}")
     public Result<DishVO>getById(@PathVariable Long id){
        log.info("根据id查询菜品：{}",id);
        DishVO dishVo = disService.getByIdWithFlavor(id);
        return Result.success(dishVo);
     }
     @PutMapping
     @ApiOperation("修改菜品接口")
     public Result update(@RequestBody DishDTO dishDTO){
        log.info("修改菜品：{}",dishDTO);
        disService.updateWithFlavor(dishDTO);
        return Result.success();
     }
     @PostMapping ("status/{status}")
     @ApiOperation("起售停售菜品")
     public Result<String> StartorStop(@PathVariable Integer status,Long id){
        log.info("起售停售菜品:{}{}",status,id);
        disService.StartorStop(status,id);
        return Result.success();
     }
     @GetMapping("/list")
     @ApiOperation("根据分类id查询菜品")
    public Result<List<Dish> > QueryDishBycategoryId( Long categoryId){
        log.info("根据分类id：{}查询",categoryId);
        List<Dish> dishes=disService.lists(categoryId);
        return Result.success(dishes);
    }
}
