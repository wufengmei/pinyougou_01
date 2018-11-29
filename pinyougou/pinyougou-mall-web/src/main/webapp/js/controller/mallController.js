
app.controller("mallController", function ($scope,$location, mallService) {

    $scope.getUsername = function () {
        mallService.getUsername().success(function (response) {
            $scope.username = response.username;


        });
    };

    $scope.getSeller = function () {
        $scope.seller = $location.search().seller;


    };
    $scope.findItemBySeller = function () {
        mallService.findItemBySeller($scope.seller).success(function (response) {
            $scope.itemList = response.rows;

        })
    };
    $scope.addItem = function (itemId) {
        mallService.addItem(itemId).success(function (response) {
            if ("加入购物车成功"==response.message){
                alert("加入购物车成功")
                $location.href="success-cart.html"
            } else {
                alert("加入购物车失败")
            }
        })

    };

    $scope.getItemId = function(){
        $scope.itemId = $location.search().itemId;
    };

    $scope.findItemById = function (itemId) {

        mallService.findItemById(itemId).success(function (response) {
            $scope.item = response;
        })
    }

});