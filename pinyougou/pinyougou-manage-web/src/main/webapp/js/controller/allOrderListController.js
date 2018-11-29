//定义处理器
app.controller("allOrderListController", function ($scope, $http,$controller, allOrderListService,itemCatService) {

    //继承baseController；参数1：要继承的处理器名称，参数2：将本处理器的scope传递到父控制器
    $controller("baseController", {$scope:$scope});

    $scope.findAll = function () {

        allOrderListService.findAll().success(function (response) {
            //将返回的品牌列表设置到一个变量中
            $scope.list = response;
        }).error(function () {
            alert("加载数据失败！");
        });

    };

    //根据分页查询
    $scope.findPage = function (page, rows) {
        allOrderListService.findPage(page, rows)
            .success(function (pageResult) {
                //更新list
                $scope.list = pageResult.rows;
                //更新分页插件中的总记录数
                $scope.paginationConf.totalItems = pageResult.total;
            });
    };

    //保存
    $scope.save = function () {

        var obj;
        //如果是修改的话；则修改为update
        if($scope.entity.id != null){
            obj = allOrderListService.update($scope.entity);
        } else {
            //新增
            obj = allOrderListService.add($scope.entity);
        }

        obj.success(function (response) {
            if(response.success){
                //操作成功；则刷新列表
                $scope.reloadList();
                //$scope.entity={};
            } else {
                alert(response.message);
            }
        });
    };

    //根据主键查询
    $scope.findOne = function (id) {
        allOrderListService.findOne(id).success(function (response) {
            $scope.entity = response;
        });
    };

    //删除
    $scope.delete = function () {
        //判断是否选择了
        if($scope.selectedIds.length == 0){
            alert("请先选择要删除的记录");
            return;
        }

        //如果点击了 确定 则返回true；否则false
        if(confirm("确定要删除选中的那些记录吗？")){
            allOrderListService.delete($scope.selectedIds).success(function (response) {
                if(response.success){
                    $scope.reloadList();
                    //将已选择的id清空
                    $scope.selectedIds = [];
                } else {
                    alert(response.message);
                }
            });
        }

    };

    $scope.searchEntity = {};

    //查询
    $scope.search = function (page, rows) {
        allOrderListService.search(page, rows, $scope.searchEntity).success(function (response) {
            $scope.paginationConf.totalItems = response.total;
            $scope.list = response.rows;
        });

    };

    //商品分类集合
    $scope.itemCatList = [];
    $scope.findItemCatList = function () {
        itemCatService.findAll().success(function (response) {
            for (var i = 0; i < response.length; i++) {
                var itemCat = response[i];
                $scope.itemCatList[itemCat.id] = itemCat.name;
            }
        });
    };


   //订单的状态
    $scope.orderStatus = ["未付款","已付款","已发货","交易成功","交易关闭","待评价"];
    //读取一级商品分类列表
    $scope.selectItemCat1List = function () {
        itemCatService.findByParentId(0).success(function (response) {
            $scope.itemCat1List = response;
        });
    };

    //读取二级商品分类列表
    $scope.$watch("searchEntity.category1Id", function (newValue, oldValue) {
        if (newValue != undefined) {
            //根据1级商品分类查询2级商品分类
            itemCatService.findByParentId(newValue).success(function (response) {
                $scope.itemCat2List = response;
                $scope.itemCat3List = "";
            });
        }
    });

    //读取三级商品分类列表
    $scope.$watch("searchEntity.category2Id", function (newValue, oldValue) {
        if (newValue != undefined) {
            //根据2级商品分类查询3级商品分类
            itemCatService.findByParentId(newValue).success(function (response) {
                $scope.itemCat3List = response;
            });
        }
    });

    //城市联动
    $scope.$watch("searchAdress.province1", function (newValue, oldValue) {
        if (newValue != undefined) {

            $scope.searchAdress.district1="";
            $scope.searchAdress.city1="";
        }
    });
    $scope.$watch("searchAdress.city1", function (newValue, oldValue) {
        if (newValue != undefined) {
            $scope.searchAdress.district1="";
        }
    });

    $scope.status = {
        "未付款" : "0",
        "已付款" : "1",
        "已发货" : "2",
        "交易成功" : "3",
        "交易关闭" : "4",
        "待评价" : "5"
    };
    // 显示年月日
    $scope.year = function (time) {
        return  format(new Date(time),'yyyy-MM-dd');
    };



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
                    break;
            }
        })
    };

    //修改订单的状态
    $scope.updateStatus = function (status) {
        if($scope.selectedIds.length < 1) {
            alert("请先选择订单");
            return;
        }
        if(confirm("确定要更新选中的商品状态吗？")){
            allOrderListService.updateStatus($scope.selectedIds, status).success(function (response) {
                if(response.success) {
                    //刷新列表并清空选中的那些商品
                    $scope.reloadList();
                    $scope.selectedIds = [];
                } else {
                    alert(response.message);
                }
            });
        }
    };





});
