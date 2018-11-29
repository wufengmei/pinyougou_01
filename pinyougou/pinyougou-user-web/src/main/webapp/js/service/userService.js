app.service("userService",function($http){

    this.register = function (entity, smsCode) {
        return $http.post("user/add.do?smsCode=" + smsCode, entity);
    };
    this.sendSmsCode = function (phone) {
        return $http.get("user/sendSmsCode.do?phone=" +phone+"&r=" + Math.random());
    };
    this.checkSmsCode = function (smsCode,phone) {
        return $http.get("user/checkSmsCode.do?smsCode=" +smsCode+ "&phone="+ phone +"&r=" + Math.random())
    };
    this.savePhone = function (phoneNum) {
        return $http.get("user/savePhone.do?phone="+phoneNum+"&r=" + Math.random())
    };
    this.getUsername = function () {
        return $http.get("user/getUsername.do?r=" + Math.random());
    };
    
    this.getUserInformation = function () {
        return $http.get("user/getUserInformation.do?r=" + Math.random());
    };
    this.updateUserInformation = function (userInformation) {
        return $http.post("user/updateUserInformation.do",userInformation);
    };
    this.checkPassword = function (password) {
        return $http.post("user/checkPassword.do",password);
    };
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
});