app.controller("orderInfoController", function ($scope, cartService, addressService) {

    $scope.getUsername = function () {
        cartService.getUsername().success(function (response) {
            $scope.username = response.username;

        });

    };

    //查询购物车列表
    $scope.findCartList = function () {
        cartService.findCartList().success(function (response) {
            $scope.cartList = response;

            //计算总价和总数
            $scope.totalValue = cartService.sumTotalValue(response);
        });

    };

    //查询收件人地址
    $scope.findAddressList = function () {
        addressService.findAddressList().success(function (response) {
            $scope.addressList = response;

            //处理默认地址
            for (var i = 0; i < response.length; i++) {
                var address = response[i];
                if (address.isDefault == "1") {
                    $scope.address = address;
                }
            }
        });

    };

    //选择当前收件人地址
    $scope.selectAddress = function (address) {
        $scope.address = address;
    };

    //是否选中的地址
    $scope.isSelectedAddress = function (address) {
        return $scope.address==address;

    };

    //默认支付方式 1微信支付，2货到付款
    $scope.order = {"paymentType":"1"};

    //选择支付类型
    $scope.selectPaymentType = function (type) {
        $scope.order.paymentType = type;
    };

    //提交订单
    $scope.submitOrder = function () {
        $scope.order.recevier = $scope.address.contact;
        $scope.order.receiverMobile = $scope.address.mobile;
        $scope.order.receiverAreaName = $scope.address.address;
        cartService.submitOrder($scope.order).success(function (response) {
            if(response.success){
                if("1"==$scope.order.paymentType) {
                    //如果是微信支付则携带支付日志id跳转到支付二维码页面
                    location.href="pay.html#?outTradeNo=" + response.message;
                } else {
                    //如果是货到付款则跳转到支付成功页面
                    location.href = "paysuccess.html";
                }
            } else {
                //提交订单失败
                alert(response.message);
            }

        });
    };
});