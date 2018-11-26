app.controller("indexController", function ($scope, userService) {
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
            // document.getElementById('select_year2').value
        })
    };
    $scope.updateUserInformation = function (userInformation) {
        userService.updateUserInformation(userInformation).success(function (response) {
            alert("信息保存成功")
            return;
        }).fail(function (response) {
            alert("信息保存失败，请检查网络")
        })
    }


});