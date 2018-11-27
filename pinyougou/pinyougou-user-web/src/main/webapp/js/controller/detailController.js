app.controller("detailController", function ($scope,$location, userService) {

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
    $scope.findOrderItemById = function(){
        userService.findOrderItemById($scope.orderItemId).success(function (response) {
            $scope.orderItem = JSON.parse(response);
        })
    };
    $scope.findOrderById = function(){
        userService.findOrderById($scope.orderId).success(function (response) {
           // $scope.createTime = getMyDate(response[0].createTime);
            $scope.orderlist = JSON.parse(response);
            //$scope.createTime = orderList.createTime;




        })
    };
        getMyDate = function(str){

        str = parseInt(str);
        if(str!=""||str!=null){
            var oDate = new Date(str);
            var oYear = oDate.getFullYear();
            var oMonth = oDate.getMonth()+1;
            oMonth = oMonth>=10? oMonth:'0'+oMonth;
            var oDay = oDate.getDate();
            oDay = oDay>=10? oDay:'0'+oDay;
            var theDate = oYear+"-"+oMonth+"-"+oDay;
        }else{
            theDate = "";
        }
        return theDate
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