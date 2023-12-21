package com.ecode.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.ecode.constant.MessageConstant;
import com.ecode.context.BaseContext;
import com.ecode.dto.*;
import com.ecode.entity.*;
import com.ecode.exception.AddressBookBusinessException;
import com.ecode.exception.OrderBusinessException;
import com.ecode.exception.ShoppingCartBusinessException;
import com.ecode.mapper.*;
import com.ecode.result.PageResult;
import com.ecode.service.OrderService;
import com.ecode.utils.WeChatPayUtil;
import com.ecode.vo.OrderPaymentVO;
import com.ecode.vo.OrderStatisticsVO;
import com.ecode.vo.OrderSubmitVO;
import com.ecode.vo.OrderVO;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.xiaoymin.knife4j.core.util.CollectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.weaver.ast.Or;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private AddressBookMapper addressBookMapper;

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;


    // 微信支付
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WeChatPayUtil weChatPayUtil;



    /**
     * 实现用户下单
     * @param ordersSubmitDTO
     * @return
     */
    @Transactional
    @Override
    public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {

        //处理各类异常情况(比如说：地址薄为空、购物车为空)
        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if(addressBook==null){
            //抛出异常情况
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }

        //查询当前用户的购物车数据
        Long userId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(userId);
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.list(shoppingCart);

        if(shoppingCartList==null || shoppingCartList.size()==0){
            //抛出购物车异常
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

        //构造订单数据
        Orders order = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO,order);
        order.setPhone(addressBook.getPhone());
        order.setAddress(addressBook.getDetail());
        order.setConsignee(addressBook.getConsignee());
        order.setNumber(String.valueOf(System.currentTimeMillis()));
        order.setUserId(userId);
        order.setStatus(Orders.PENDING_PAYMENT);
        order.setPayStatus(Orders.UN_PAID);
        order.setOrderTime(LocalDateTime.now());

        //2. 向订单表插入1条数据
        orderMapper.insert(order);

        //订单明细数据
        List<OrderDetail> orderDetailList = new ArrayList<>();
        for(ShoppingCart cart: shoppingCartList){
            OrderDetail orderDetail = new OrderDetail(); //订单明细
            BeanUtils.copyProperties(cart,orderDetail);

            orderDetail.setOrderId(order.getId()); //设置当前订单明细关联的id，前提是mapper映射文件中可以返回主键id
            orderDetailList.add(orderDetail);
        }

        //3. 向订单明细表插入n条数据
        orderDetailMapper.insertBatch(orderDetailList);

        //4.清空当前用户的购物车数据
        shoppingCartMapper.deleteByUserId(userId);

        //5. 封装VO返回结果
        OrderSubmitVO orderSubmitVO=OrderSubmitVO.builder()
                .id(order.getId())
                .orderNumber(order.getNumber())
                .orderAmount(order.getAmount())
                .orderTime(order.getOrderTime())
                .build();

        return orderSubmitVO;
    }


    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */

    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.getById(userId);

        //调用微信支付接口，生成预支付交易单
//        JSONObject jsonObject = weChatPayUtil.pay(
//                ordersPaymentDTO.getOrderNumber(), //商户订单号
//                new BigDecimal(0.01), //支付金额，单位 元
//                "外卖订单", //商品描述
//                user.getOpenid() //微信用户的openid
//        );

        //生成空的jsonObject
        JSONObject jsonObject = new JSONObject();
        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
            throw new OrderBusinessException("该订单已支付");
        }

        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));

        return vo;
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */

    public void paySuccess(String outTradeNo) {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();

        // 根据订单号查询当前用户的订单
        Orders ordersDB = orderMapper.getByNumberAndUserId(outTradeNo, userId);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .packAmount(4).tablewareNumber(4).build();

        orderMapper.update(orders);
    }

    /**
     * 历史订单查询
     * @param pageNum
     * @param pageSize
     * @param status
     * @return
     */
    @Override
    public PageResult pageQuery4User(int pageNum, int pageSize, Integer status) {
        //设置分页
        PageHelper.startPage(pageNum,pageSize);

        OrdersPageQueryDTO ordersPageQueryDTO = new OrdersPageQueryDTO();
        ordersPageQueryDTO.setUserId(BaseContext.getCurrentId());
        ordersPageQueryDTO.setStatus(status);

        //分页条件查询
        Page<Orders> page=orderMapper.pageQuery(ordersPageQueryDTO);
        List<OrderVO>list=new ArrayList<>();

        //查询出订单明细。并封装入OrderVO进行响应
        if(page!=null && page.getTotal()>0){
            for(Orders orders:page){
                Long orderId = orders.getId(); //订单id

                //查询订单明细
                List<OrderDetail> orderDetails=orderDetailMapper.getByOrderId(orderId);

                OrderVO orderVo = new OrderVO();
                BeanUtils.copyProperties(orders,orderVo);
                orderVo.setOrderDetailList(orderDetails);

                list.add(orderVo);
            }
        }
        return new PageResult(page.getTotal(),list);
    }

    /**
     * 查询订单详情
     * @param id
     * @return
     */
    @Override
    public OrderVO details(Long id) {
        //根据id查询订单
        Orders o=orderMapper.getById(id);
        //查询该订单对应的菜品/套餐明细
        List<OrderDetail> orderDetailList=orderDetailMapper.getByOrderId(o.getId());

        //将该订单及其详情信息进行封装
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(o,orderVO);
        orderVO.setOrderDetailList(orderDetailList);

        return orderVO;

    }

    /**
     * 取消订单
     * @param id
     * @return
     */
    @Override
    public void userCancelById(Long id) {
       // 首先根据id查询订单
        Orders ordersDB = orderMapper.getById(id);

        //校验订单是否存在
        if(ordersDB==null){
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        //订单的状态 1待付款 2待接单 3 已结单 4派送中 5已完成 6已取消
        if(ordersDB.getStatus()>2){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        Orders orders = new Orders();
        orders.setId(ordersDB.getId());

        //订单处于待接单状态下取消，此时需要进行退款
        if(ordersDB.getId().equals(Orders.TO_BE_CONFIRMED)){
//            //调用微信支付退款接口
//            weChatPayUtil.refund(
//                    ordersDB.getNumber(), //商户订单号
//                    ordersDB.getNumber(), //商户退款单号
//                    new BigDecimal(0.01),//退款金额，单位 元
//                    new BigDecimal(0.01));//原订单金额

            //支付状态修改为 退款
            orders.setPayStatus(Orders.REFUND);
        }
        // 更新订单状态、取消原因、取消时间
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelReason("用户取消");
        orders.setCancelTime(LocalDateTime.now());
        orderMapper.update(orders);
    }

    /**
     * 再来一单
     * @param id
     * @return
     */
    @Override
    public void repetition(Long id) {
       // 查询当前用户的id
        Long currentId = BaseContext.getCurrentId();

        //根据订单的id查询当前订单的详情
        List<OrderDetail> byOrderId = orderDetailMapper.getByOrderId(id);

        //将订单详情的对象转化为购物车的对象
        List<ShoppingCart>shoppingCartList=byOrderId.stream().map(x ->{
            ShoppingCart cart = new ShoppingCart();

            //将原订单详情内的菜品放入复制到购物车对象当中
            BeanUtils.copyProperties(x,cart,"id");
            cart.setUserId(currentId);
            cart.setCreateTime(LocalDateTime.now());

            return cart;
        }).collect(Collectors.toList());

        //将购物车中的对象批量添加到数据库当中
        shoppingCartMapper.insertBatch(shoppingCartList);
    }


    /**
     * 订单搜索
     * @param ordersPageQueryDTO
     * @return
     */
    @Override
    public PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());

        Page<Orders> page = orderMapper.pageQuery(ordersPageQueryDTO);

        //部分订单状态，需要额外返回订单菜品信息，将Orders转化为OrderVO
        List<OrderVO> orderVOList=getOrderVOList(page);
        return new PageResult(page.getTotal(),orderVOList);

    }

    /**
     * 各个状态的订单数量统计
     * @return
     */
    @Override
    public OrderStatisticsVO statistics() {
        // 根据状态分为，查询出待接单、待派送、派送中的订单数量】
        Integer toBeConfirmed=orderMapper.countStatus(Orders.TO_BE_CONFIRMED);
        Integer Confirmed=orderMapper.countStatus(Orders.CONFIRMED);
        Integer deliveryInProgress=orderMapper.countStatus(Orders.DELIVERY_IN_PROGRESS);

        //将查询出的数据封装到orderStatisticsVO中响应
        OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();
        orderStatisticsVO.setToBeConfirmed(toBeConfirmed);
        orderStatisticsVO.setConfirmed(Confirmed);

        orderStatisticsVO.setDeliveryInProgress(deliveryInProgress);

        return orderStatisticsVO;
    }

    /**
     * 接单
     * @param ordersConfirmDTO
     */
    @Override
    public void confirm(OrdersConfirmDTO ordersConfirmDTO) {
        Orders orders= Orders.builder()
                .id(ordersConfirmDTO.getId())
                .status(Orders.CONFIRMED)
                .packAmount(2).tablewareNumber(2).build();

        orderMapper.update(orders);
    }

    /**
     * 拒单
     * @param ordersRejectionDTO
     */
    @Override
    public void rejection(OrdersRejectionDTO ordersRejectionDTO) {
        //首先需要查询订单
        Orders ordersDB=orderMapper.getById(ordersRejectionDTO.getId());

        //订单只有存在状态为2（待接单）的时候可以拒单
        if(ordersDB==null || !ordersDB.getStatus().equals(Orders.TO_BE_CONFIRMED)){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        //支付状态
        Integer payStatus = ordersDB.getPayStatus();
        if (payStatus == Orders.PAID) {
            //用户已支付，需要退款
//            String refund = weChatPayUtil.refund(
//                    ordersDB.getNumber(),
//                    ordersDB.getNumber(),
//                    new BigDecimal(0.01),
//                    new BigDecimal(0.01));
//            log.info("申请退款：{}", refund);
        }

        //拒单需要退款，根据订单id更新订单状态、拒单的原因、取消时间
        Orders orders = new Orders();
        orders.setId(orders.getId());
        orders.setStatus(Orders.CANCELLED);
        orders.setRejectionReason(ordersRejectionDTO.getRejectionReason());
        orders.setCancelTime(LocalDateTime.now());

        orderMapper.update(orders);
    }

    /**
     * 取消订单
     * @param ordersCancelDTO
     */
    @Override
    public void cancel(OrdersCancelDTO ordersCancelDTO) {
        //首先还是根据id查询订单
        Orders ordersDB=orderMapper.getById(ordersCancelDTO.getId());

        //支付状态
        Integer payStatus = ordersDB.getPayStatus();
        if (payStatus == 1) {
//            //用户已支付，需要退款
//            String refund = weChatPayUtil.refund(
//                    ordersDB.getNumber(),
//                    ordersDB.getNumber(),
//                    new BigDecimal(0.01),
//                    new BigDecimal(0.01));
//            log.info("申请退款：{}", refund);
        }

        // 管理端取消订单需要退款，根据订单id更新订单状态、取消原因、取消时间
        Orders orders = new Orders();
        orders.setId(ordersCancelDTO.getId());
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelReason(ordersCancelDTO.getCancelReason());
        orders.setCancelTime(LocalDateTime.now());
        orderMapper.update(orders);
    }

    /**
     * 派送订单
     * @param id
     */
    @Override
    public void delivery(Long id) {
        //首先获取订单
        Orders id1 = orderMapper.getById(id);

        //检查订单是否存在，状态为3
        if(id1==null || !id1.getStatus().equals(Orders.CONFIRMED)){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orders=new Orders();
        orders.setId(id1.getId());
        //更新订单的状态，转为派送中
        orders.setStatus(Orders.DELIVERY_IN_PROGRESS);

        orderMapper.update(orders);

    }

    /**
     * 完成订单
     * @param id
     */
    @Override
    public void complete(Long id) {
        //首先获取订单
        Orders order111 = orderMapper.getById(id);

        //检查订单是否存在
        if(order111==null || !order111.getStatus().equals(Orders.DELIVERY_IN_PROGRESS)){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orders = new Orders();
        orders.setId(order111.getId());
        orders.setStatus(Orders.COMPLETED);
        orders.setDeliveryTime(LocalDateTime.now());

        orderMapper.update(orders);
    }

    /**
     * 返回订单菜品信息
     * @param page
     * @return
     */
    private List<OrderVO> getOrderVOList(Page<Orders> page) {
        // 需要返回订单菜品信息，自定义OrderVO响应结果
        List<OrderVO> orderVOList = new ArrayList<>();

        List<Orders> ordersList = page.getResult();
        if (!CollectionUtils.isEmpty(ordersList)) {
            for (Orders orders : ordersList) {
                // 将共同字段复制到OrderVO
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(orders, orderVO);
                String orderDishes = getOrderDishesStr(orders);

                // 将订单菜品信息封装到orderVO中，并添加到orderVOList
                orderVO.setOrderDishes(orderDishes);
                orderVOList.add(orderVO);
            }
        }
        return orderVOList;
    }

    /**
     * 根据订单id获取菜品信息字符串
     *
     * @param orders
     * @return
     */
    private String getOrderDishesStr(Orders orders) {
        // 查询订单菜品详情信息（订单中的菜品和数量）
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(orders.getId());

        // 将每一条订单菜品信息拼接为字符串（格式：宫保鸡丁*3；）
        List<String> orderDishList = orderDetailList.stream().map(x -> {
            String orderDish = x.getName() + "*" + x.getNumber() + ";";
            return orderDish;
        }).collect(Collectors.toList());

        // 将该订单对应的所有菜品信息拼接在一起
        return String.join("", orderDishList);
    }

}
