app.controller("payController", function ($scope, $location, cartService, payService) {

    $scope.getUsername = function () {
        cartService.getUsername().success(function (response) {
            $scope.username = response.username;

        });

    };


    //生成二维码
    $scope.createNative = function () {
        //获取地址栏中的交易号（支付日志id）
        $scope.outTradeNo = $location.search()["outTradeNo"];
        payService.createNative($scope.outTradeNo).success(function (response) {
            if("SUCCESS"==response.result_code){
                //总金额
                $scope.totalFee = (response.totalFee/100).toFixed(2);

                //根据微信返回的地址生成二维码图片
                var qr = new QRious({
                    element:document.getElementById("qrious"),
                    size:250,
                    level:"M",
                    value:response.code_url
                });

                //查询支付状态
                queryPayStatus($scope.outTradeNo);

            } else {
                alert("生成支付二维码失败");
            }

        });
    };

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

});