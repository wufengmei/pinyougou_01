app.service("homeSeckillOrderService", function ($http) {

    this.search = function (searchMap) {

        return $http.post("mySeckillOrder/findMySeckillOrder.do", searchMap);
    };
});