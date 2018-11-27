app.controller("indexController", function ($scope,$location, userService,payService) {

    $scope.getUsername = function () {
        userService.getUsername().success(function (response) {
            $scope.username = response.username;
        });
    };
    $scope.findOrderList = function () {
        userService.findOrderList().success(function (response) {
            $scope.orderList = JSON.parse(response);



        });
    };

    $scope.findOutTradeNo = function(){
        userService.findOutTradeNo($scope.orderId).success(function (response) {
            $scope.weixin = response;
            $scope.outTradeNo = response.outTradeNo


                    if("SUCCESS"==response.result_code){
                        //总金额
                        $scope.totalFee = (response.totalFee/100).toFixed(2);

                        //根据微信返回的地址生成二维码图片
                        var qr = new QRious({
                            element:document.getElementById("qrious"),
                            size:250,
                            level:"M",
                            value:response.code_url,
                        });

                        //查询支付状态
                        queryPayStatus($scope.outTradeNo);

                    } else {
                        alert("生成支付二维码失败");
                    }



        })
    }

    queryPayStatus = function (outTradeNo) {
        payService.queryPayStatus(outTradeNo).success(function (response) {
            if(response.success){
                //如果支付成功跳转到支付成功的提示页面
                location.href = "paysuccess.html#?money=" + $scope.totalFee;
            } else {
                if ("支付超时" == response.message) {
                    //支付超时重新生成二维码;
                    $scope.createNative();
                } else {
                    //如果支付失败则跳转到支付失败页面
                    location.href= "payfail.html";
                }
            }

        });

    };
    $scope.getMoney = function () {
        $scope.money = $location.search()["money"];
    };


    $scope.getOrderId = function () {
        $scope.orderId = $location.search()["orderId"]

    };


});