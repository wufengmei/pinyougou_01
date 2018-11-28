app.service("userService",function($http){

    this.register = function (entity, smsCode) {
        return $http.post("user/add.do?smsCode=" + smsCode, entity);
    };

    this.sendSmsCode = function (phone) {
        return $http.get("user/sendSmsCode.do?phone=" +phone+"&r=" + Math.random());
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
});