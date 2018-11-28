app.controller("addressController", function ($scope, addressService,userService) {

    $scope.addressId = {};
    // 用于删除选中的地址id
    $scope.selectedIds = [];
    //用于装用户选中的地址
    $scope.addressHub = {};

    $scope.getUsername = function () {
        userService.getUsername().success(function (response) {
            $scope.username = response.username;
        });
    };

    $scope.getAddressInformation = function() {
        addressService.getAddressInformation().success(function(response) {
            $scope.addressInformation = response;
        });
    };
    $scope.setDefaultAddress = function(id) {
        addressService.setDefaultAddress(id).success(function () {
            $scope.getAddressInformation();
        });
    };

    $scope.delete = function (id) {
        var mymessage=confirm("确认删除？");
        if(mymessage) {
            $scope.selectedIds.push(id);
            addressService.delete($scope.selectedIds).success(function () {
                $scope.getAddressInformation();
            });
        }
    };

    $scope.findOne = function (id) {
        $scope.thisUserInfromation = {};
        $scope.findProvince();
        addressService.findOne(id).success(function (response) {

            $scope.thisUserInfromation = response;
            $scope.addressHub.initShowProvince = $scope.thisUserInfromation.addressMap.provinceName.province;
        });
    };

    $scope.updateAddressInfromation = function (thisUserInfromation) {
        //当id不为空就是update，当id为空说明是新增insert用户
        if(thisUserInfromation.id != null) {
            addressService.updateAddressInfromation(thisUserInfromation).success(function (response) {
                $scope.results = response;
                if(response.success){
                    alert("修改成功");
                    $scope.getAddressInformation();

                }else {
                    alert("修改失败");
                }
            });
        }else{
            thisUserInfromation.default = "0";
            addressService.insertAddressInformation(thisUserInfromation).success(function (response) {
                $scope.results = response;
                if(response.success){
                    alert("新增成功");
                    $scope.getAddressInformation();
                }else {
                    alert("新增失败");
                }
            });
        }
    };



    $scope.findAddressList = function () {
        $scope.thisUserInfromation = {};
        $scope.findProvince();
    };

    $scope.findProvince = function () {
        addressService.findProvince().success(function(response) {
            $scope.provinceList = response;
        })
    };

    $scope.findCities = function (provinceId) {
        addressService.findCities(provinceId).success(function (response) {
            $scope.citiesList = response;
        });
    };
    $scope.findAreas = function (cityId) {
        addressService.findAreas(cityId).success(function(response) {
            $scope.areasList = response;
        });
    };

   /* $scope.$watch("thisUserInfromation.addressMap.provinceName",function () {
        $scope.findCities($scope.thisUserInfromation.addressMap.provinceName.provinceId);
    });

    $scope.$watch("thisUserInfromation.addressMap.cityName", function () {
        $scope.findAreas($scope.thisUserInfromation.addressMap.cityName.cityId);
    });

    $scope.$watch("thisUserInfromation.addressMap.townName", function () {

    });*/



});