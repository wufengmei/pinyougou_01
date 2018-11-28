app.controller("safeController", function ($scope, addressService,userService) {

    $scope.password = {};

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

});