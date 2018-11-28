app.service("addressService",function($http){
    this.getAddressInformation = function () {
        return $http.get("address/getAddressInformation.do?r=" + Math.random());
    };
    this.setDefaultAddress = function (addressId) {
        return $http.get("address/setDefaultAddress.do?addressId=" + addressId);
    };
    this.delete = function (selectedIds) {
        return $http.get("address/delete.do?ids=" + selectedIds);
    };
    this.findOne = function (id) {
        return $http.get("address/findOne.do?id=" + id +"&r="+ Math.random());
    };
    this.updateAddressInfromation = function (thisUserInfromation) {
        return $http.post("address/updateAddressInfromation.do?r="+ Math.random(),thisUserInfromation)
    };
    this.insertAddressInformation = function (thisUserInfromation) {
        return $http.post("address/insertAddressInformation.do?r="+ Math.random(),thisUserInfromation)
    };


    this.findProvince = function () {
        return $http.get("address/findProvince.do?r=" + Math.random())
    };
    this.findCities = function (provinceId) {
        return $http.get("address/findCities.do?provinceId=" + provinceId +"&r="+ Math.random())
    };
    this.findAreas = function (cityId) {
        return $http.get("address/findAreas.do?cityId=" + cityId +"&r="+ Math.random())
    };
});