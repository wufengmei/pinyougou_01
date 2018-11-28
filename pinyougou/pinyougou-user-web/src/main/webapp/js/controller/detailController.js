app.controller("detailController", function ($scope,$location, userService) {

    $scope.getUsername = function () {
        userService.getUsername().success(function (response) {
            $scope.username = response.username;
        });
    };


    $scope.findOrderById = function(){
        userService.findOrderById($scope.orderId).success(function (response) {
            $scope.orderDetail = JSON.parse(response);

        })
    };



    $scope.getMoney = function () {
        $scope.money = $location.search()["money"];
    };


    $scope.getOrderItemId = function () {
        $scope.orderItemId = $location.search()["orderItemId"]

    };
    $scope.getOrderId = function () {
        $scope.orderId = $location.search()["orderId"]

    };


});