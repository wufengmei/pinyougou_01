app.controller("mallController", function ($scope,$location, mallService) {

    $scope.getUsername = function () {
        mallService.getUsername().success(function (response) {
            $scope.username = response.username;
            $scope.seller = "LG";
        });
    };

    $scope.getSeller = function () {
        $scope.seller = $location.search()["seller"];

    };
    $scope.findItemBySeller = function () {
        mallService.findItemBySeller($scope.seller).success(function (response) {
            $scope.itemList = response.rows;
        })
    };


});