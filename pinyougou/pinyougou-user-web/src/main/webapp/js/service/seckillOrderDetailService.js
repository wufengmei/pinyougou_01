app.service("seckillOrderDetailService", function ($http) {

    this.findOne = function (id) {

        return $http.get("mySeckillOrder/findOne.do?id="+id);
    };
});