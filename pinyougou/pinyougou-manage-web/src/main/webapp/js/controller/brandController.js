//定义处理器
app.controller("brandController", function ($scope, $http,$controller, brandService) {

    //继承baseController；参数1：要继承的处理器名称，参数2：将本处理器的scope传递到父控制器
    $controller("baseController", {$scope:$scope});

    $scope.findAll = function () {

        brandService.findAll().success(function (response) {
            //将返回的品牌列表设置到一个变量中
            $scope.list = response;
        }).error(function () {
            alert("加载数据失败！");
        });

    };

    //根据分页查询
    $scope.findPage = function (page, rows) {
        brandService.findPage(page, rows)
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
            obj = brandService.update($scope.entity);
        } else {
            //新增
            obj = brandService.add($scope.entity);
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
        brandService.findOne(id).success(function (response) {
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
            brandService.delete($scope.selectedIds).success(function (response) {
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
        brandService.search(page, rows, $scope.searchEntity).success(function (response) {
            $scope.paginationConf.totalItems = response.total;
            $scope.list = response.rows;
        });

    };

});
