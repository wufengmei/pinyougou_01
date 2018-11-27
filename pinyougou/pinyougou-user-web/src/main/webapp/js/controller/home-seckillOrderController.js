app.controller("seckillOrderController", function ($scope,$location, homeSeckillOrderService,userService) {

    $scope.getUsername = function () {
        userService.getUsername().success(function (response) {
            $scope.username = response.username;
        });
    };


    //搜索
    $scope.search = function () {

        homeSeckillOrderService.search().success(function (response) {
            $scope.resultMap = response;

            //构建分页导航条
            buildPageInfo();

        });

    };


    buildPageInfo = function () {
        $scope.pageNoList = [];
        //起始页号
        var startPage = 1;
        //结束页号
        var endPage = $scope.resultMap.totalPages;

        //要在页面中总共显示的页号个数，5
        var showPages = 5;

        //如果总页数大于要显示的页号的话：
        if($scope.resultMap.totalPages > showPages){

            //在当前页号左右两边的间隔
            var interval = Math.floor(showPages/2);

            startPage = parseInt($scope.searchMap.pageNo) - interval;
            endPage = parseInt($scope.searchMap.pageNo) + interval;

            if(startPage > 0){
                if (endPage > $scope.resultMap.totalPages) {
                    //结束页号不能大于总页数
                    startPage = startPage - (endPage-$scope.resultMap.totalPages);
                    endPage = $scope.resultMap.totalPages;
                }
            } else {
                //起始页号不能小于1
                startPage = 1;
                endPage = showPages;
            }
        }

        //前面3个点
        $scope.frontDot = false;
        if (startPage > 1) {
            $scope.frontDot = true;
        }
        //后面3个点
        $scope.backDot = false;
        if (endPage < $scope.resultMap.totalPages) {
            $scope.backDot = true;
        }

        for (var i = startPage; i <= endPage; i++) {
            $scope.pageNoList.push(i);
        }

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
    $scope.orderStatus = ["未付款","已付款","未发货","已发货","交易成功","交易关闭","待评价"];
});