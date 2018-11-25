app.controller("baseController", function ($scope) {

    // 初始化分页参数
    $scope.paginationConf = {
        currentPage: 1,// 当前页号
        totalItems: 10,// 总记录数
        itemsPerPage: 10,// 页大小
        perPageOptions: [10, 20, 30, 40, 50],// 可选择的每页大小
        onChange: function () {// 当上述的参数发生变化了后触发；渲染分页组件完之后也会触发
            $scope.reloadList();
        }
    };

    $scope.reloadList = function () {
        //$scope.findPage($scope.paginationConf.currentPage, $scope.paginationConf.itemsPerPage);
        $scope.search($scope.paginationConf.currentPage, $scope.paginationConf.itemsPerPage);
    };

    //当前选中了的那些id数组
    $scope.selectedIds = [];

    //复选框点击事件
    $scope.updateSelection = function ($event, id) {

        if($event.target.checked){
            //当选中某个品牌的时候，应该将该品牌的id加入数组
            $scope.selectedIds.push(id);
        } else {
            //如果反选某个已经选中的品牌复选框的时候，应该将该品牌id从数组删除
            //先查询id在数组中的索引号
            var index = $scope.selectedIds.indexOf(id);
            //使用splic方法删除对应索引号的元素，删除个数为1
            $scope.selectedIds.splice(index, 1);
        }
    };

    //将一个json列表字符串的某个属性对应的值获取并返回（使用，隔开）
    $scope.jsonToString = function (jsonLisStr, key) {
        var str = "";
        //将字符串转换为一个集合
        var jsonArray = JSON.parse(jsonLisStr);
        for (var i = 0; i < jsonArray.length; i++) {
            var jsonObj = jsonArray[i];

            if(str.length > 0){
                str += "," + jsonObj[key];
            } else {
                str = jsonObj[key];
            }
        }
        return str;

    };

});