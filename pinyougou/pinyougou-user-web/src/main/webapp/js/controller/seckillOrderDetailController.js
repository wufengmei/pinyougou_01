app.controller("seckillOrderDetailController", function ($scope,$location, seckillOrderDetailService,userService,$interval) {

    $scope.getUsername = function () {
        userService.getUsername().success(function (response) {
            $scope.username = response.username;
        });
    };


    //搜索
    $scope.findOne = function () {
        seckillOrderDetailService.findOne($location.search()["id"]).success(function (response) {

            $scope.resultMap = response;

            //倒计时总秒数  20分钟后
            if (response.id.status=='2'){
                var allSeconds = Math.floor((new Date(response.id.consignTime+1*60*1000).getTime() - new Date().getTime()) / 1000);
                /**
                 * $interval定时器内部服务对象
                 * 参数1：要执行的方法
                 * 参数2：执行频率，单位为毫秒
                 */
                var task = $interval(function () {
                    if (allSeconds > 0) {
                        allSeconds = allSeconds - 1;
                        //转换倒计时总秒数为 **天**:**:** 的格式并在页面展示
                        $scope.timestring = convertTimeString(allSeconds);
                    } else {
                        $interval.cancel(task);
                        // 修改该订单的状态号和交易成功时间
                        updateEndTime($location.search()["id"]);
                    }
                }, 1000);
            }

        });

    };

    // 修改该订单的状态号和交易成功时间
    updateEndTime = function (id) {
        seckillOrderDetailService.updateEndTime(id).success(function (response) {
            if (response.success){
                // 刷新页面
                findOne();
            } else {
                alert(response.message);
            }

        })
    };

    convertTimeString = function (allSeconds) {
        //天数
        var days = Math.floor(allSeconds / (60 * 60 * 24));
        //时
        var hours = Math.floor((allSeconds - days * 60 * 60 * 24) / (60 * 60));
        //分
        var minutes = Math.floor((allSeconds - days * 60 * 60 * 24 - hours * 60 * 60) / 60);
        //秒
        var seconds = allSeconds - days * 60 * 60 * 24 - hours * 60 * 60 - minutes * 60;

        var str = "";
        if (days > 0) {
            str = days + "天";
        }
        return str + hours + ":" + minutes + ":" + seconds;
    };

    //是否当前页
    $scope.isCurrentPage = function (pageNo) {
        return $scope.searchMap.pageNo==pageNo;
    };

    $scope.upPage = function () {
        $scope.searchMap.pageNo = parseInt($scope.searchMap.pageNo)-1;
        $scope.search();
    };

    $scope.nextPage = function () {
        $scope.searchMap.pageNo = parseInt($scope.searchMap.pageNo)+1;
        $scope.search();
    };

    //根据页号查询
    $scope.queryByPageNo = function (pageNo) {
        if (1 < pageNo && pageNo <= $scope.resultMap.totalPages) {
            $scope.searchMap.pageNo = pageNo;

            $scope.search();
        }

    };

    //加载请求地址栏中的搜索关键字
    $scope.loadKeywords = function () {
        $scope.searchMap.keywords = $location.search()["keywords"];
        $scope.search();

    };

    //订单的状态
    $scope.orderStatus = ["未付款","未发货","已发货","交易成功","交易关闭","待评价"];

    $scope.setTime = function (time) {
      return  format(new Date(time),'yyyy-MM-dd HH:mm:ss');
    };

    // 显示年月日
    $scope.year = function (time) {
        return  format(new Date(time),'yyyy-MM-dd');
    };

    // 显示时分秒
    // 显示年月日
    $scope.day = function (time) {
        return  format(new Date(time),'HH:mm:ss');
    };

    $scope.returnS = function (time) {
        if (time != null){
            return "current";
        }else {
            return "todo";
        }
    }




    var format = function(time, format){
        var t = new Date(time);
        var tf = function(i){return (i < 10 ? '0' : '') + i};
        return format.replace(/yyyy|MM|dd|HH|mm|ss/g, function(a){
            switch(a){
                case 'yyyy':
                    return tf(t.getFullYear());
                    break;
                case 'MM':
                    return tf(t.getMonth() + 1);
                    break;
                case 'mm':
                    return tf(t.getMinutes());
                    break;
                case 'dd':
                    return tf(t.getDate());
                    break;
                case 'HH':
                    return tf(t.getHours());
                    break;
                case 'ss':
                    return tf(t.getSeconds());
                    break; } }) }

});