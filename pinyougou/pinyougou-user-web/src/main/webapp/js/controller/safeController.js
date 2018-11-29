app.controller("safeController", function ($scope, addressService, userService) {

    $scope.password = {};
    $scope.userInformation = {};

    $scope.getUsername = function () {
        userService.getUsername().success(function (response) {
            $scope.username = response.username;
        });
    };
    $scope.getUserInformation = function() {
        userService.getUserInformation().success(function(response) {
            $scope.userInformation.nickName = response.nickName;
            $scope.userInformation.sex = response.sex;
            $scope.userInformation.birthdayYear = response.birthdayYear;
            $scope.userInformation.birthdayMonth = response.birthdayMonth;
            $scope.userInformation.birthdayDay = response.birthdayDay;
            $scope.userInformation.phone = response.phone;
        })
    };
    $scope.submitPassword = function (password) {
        userService.checkPassword(password).success(function (response) {
            if(response.success){
                alert(response.message);
                location.href = "/logout/cas?service=http://user.pinyougou.com";

            }else{
                alert(response.message);
            }
        })
    };
    $scope.sendSmsCodeNormal = function (phoneNum) {
        userService.sendSmsCode(phoneNum).success(function (response) {
            alert(response.message);
        });
    };
    $scope.checkSmsCode = function(smsCode,phoneNum) {
        userService.checkSmsCode(smsCode,phoneNum).success(function(response) {
            if(response.success) {
                alert(response.message);
                location.href = "home-setting-address-phone.html";
            }else{
                alert(response.message);
                //跳转回主页
                // location.href = "home-setting-safe.html";
            }
        })
    };
    $scope.savePhone = function(phoneNum){
        userService.savePhone(phoneNum).success(function(response) {
            if(response.success) {
                location.href = "home-setting-address-complete.html";
            }else{
                alert(response.message)
            }
        })
    }

});