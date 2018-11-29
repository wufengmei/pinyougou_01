app.service("seckillOrderDetailService", function ($http) {

    this.findOne = function (id) {

        return $http.get("mySeckillOrder/findOne.do?id="+id);
    };

    this.updateEndTime =function (id) {
        return $http.get("mySeckillOrder/updateEndTime.do?id="+id);
    }
});