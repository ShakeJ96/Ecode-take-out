package com.ecode.controller.user;

import com.ecode.constant.StatusConstant;
import com.ecode.entity.Dish;
import com.ecode.result.Result;
import com.ecode.service.DishService;
import com.ecode.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController("userDishController")
@RequestMapping("/user/dish")
@Slf4j
@Api(tags = "C端-菜品浏览接口")
public class DishController {
    @Autowired
    private DishService dishService;
//
//    @Autowired
//    private RedisTemplate redisTemplate;
//    /**
//     * 根据分类id查询菜品
//     *
//     * @param categoryId
//     * @return
//     */
//    @GetMapping("/list")
//    @ApiOperation("根据分类id查询菜品")
//    public Result<List<DishVO>> list(Long categoryId) {
//        Dish dish = new Dish();
//        dish.setCategoryId(categoryId);
//        dish.setStatus(StatusConstant.ENABLE);//查询起售中的菜品
//
//        List<DishVO> list = dishService.listWithFlavor(dish);
//
//        return Result.success(list);
//    }

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 根据分类id查询菜品
     *
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    public Result<List<DishVO>> list(Long categoryId) {
        // 构造redis中的key,这里将规则定义为：dish_分类id
        String key="dish_"+categoryId;

        //查询redis中是否存在菜品的数据
        List<DishVO> list = (List<DishVO>) redisTemplate.opsForValue().get(key);

        //如果存在，直接返回，无序查询数据库了
        if(list!=null && list.size()>0){
            return Result.success(list);
        }

        //////////////////////////
        Dish dish = new Dish();
        dish.setCategoryId(categoryId);
        dish.setStatus(StatusConstant.ENABLE);//查询起售中的菜品

        list = dishService.listWithFlavor(dish);

        /////////////////////////
        redisTemplate.opsForValue().set(key,list);
        return Result.success(list);

    }

}
