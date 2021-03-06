app.service("mallService",function($http){

    this.register = function (entity, smsCode) {
        return $http.post("user/add.do?smsCode=" + smsCode, entity);

    };

    this.sendSmsCode = function (phone) {
        return $http.get("user/sendSmsCode.do?phone=" +phone+"&r=" + Math.random());
    };

    this.getUsername = function () {
        return $http.get("mall/getUsername.do?r=" + Math.random());

    }
    this.findOrderList = function () {
        return $http.get("order/findOrderList.do?r="+ Math.random());

    };
    this.findOutTradeNo = function (orderId) {
        return $http.get("order/findOutTradeNo.do?orderId="+orderId+"&r=" + Math.random());

    };
    this.createNative = function (outTradeNo) {
        return $http.get("pay/createNative.do?outTradeNo="+outTradeNo + "&r=" + Math.random());

    };
    this.findOrderItemById = function (orderItemId) {
        return $http.get("order/findOrderItemById.do?orderItemId="+orderItemId + "&r=" + Math.random());
    };
    this.findOrderById = function (orderId) {
        return $http.get("order/findOrderById.do?orderId="+orderId + "&r=" + Math.random());
    };
    this.findItemBySeller = function (seller) {
        return $http.get("mall/findItemBySeller.do?seller="+seller + "&r=" + Math.random());
    }
    this.addItem = function (itemId) {
        return $http.get("cart/addItemToCartList.do?itemId="+itemId+"&num=1")
    }
    this.findItemById = function (itemId) {
        return $http.get("mall/findItemById.do?itemId="+itemId)
    }
});