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
    $scope.addressId = {};
    // 用于删除选中的地址id
    $scope.selectedIds = [];
    //用于装用户选中的地址
    $scope.addressHub = {};



    $scope.getAddressInformation = function() {
        addressService.getAddressInformation().success(function(response) {
            $scope.addressInformation = response;
        });
    };
    $scope.setDefaultAddress = function(id) {
        addressService.setDefaultAddress(id).success(function () {
            $scope.getAddressInformation();
        });
    };

    $scope.delete = function (id) {
        var mymessage=confirm("确认删除？");
        if(mymessage) {
            $scope.selectedIds.push(id);
            addressService.delete($scope.selectedIds).success(function () {
                $scope.getAddressInformation();
            });
        }
    };


    $scope.updateAddressInfromation = function (thisUserInfromation) {
        //当id不为空就是update，当id为空说明是新增insert用户
        if(thisUserInfromation.id != null) {
            addressService.updateAddressInfromation(thisUserInfromation).success(function (response) {
                $scope.results = response;
                if(response.success){
                    alert("修改成功");
                    $scope.getAddressInformation();

                }else {
                    alert("修改失败");
                }
            });
        }else{
            thisUserInfromation.default = "0";
            addressService.insertAddressInformation(thisUserInfromation).success(function (response) {
                $scope.results = response;
                if(response.success){
                    alert("新增成功");
                    $scope.getAddressInformation();
                }else {
                    alert("新增失败");
                }
            });
        }
    };



    $scope.findAddressList = function () {
        $scope.thisUserInfromation = {};
        $scope.findProvince();
    };

    $scope.findProvince = function () {
        addressService.findProvince().success(function(response) {
            $scope.provinceList = response;
        })
    };

    $scope.findCities = function (provinceId) {
        addressService.findCities(provinceId).success(function (response) {
            $scope.citiesList = response;
        });
    };
    $scope.findAreas = function (cityId) {
        addressService.findAreas(cityId).success(function(response) {
            $scope.areasList = response;
        });
    };
});