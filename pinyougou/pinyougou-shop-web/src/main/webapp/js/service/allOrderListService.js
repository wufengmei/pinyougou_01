//定义业务；可以将与后台交互的代码放置到业务对象中
app.service("allOrderListService", function ($http) {

    //this 表示 当前的brandService
    this.findAll = function(){
        return $http.get("../order/findAll.do");
    };

    //根据分页查询
    this.findPage = function (page, rows) {
        return $http.get("../order/findPage.do?page=" + page + "&rows=" + row);
    };

    //新增
    this.add = function (entity) {
        return $http.post("../order/add.do", entity);
    };

    //更新
    this.update = function (entity) {
        return $http.post("../order/update.do", entity);
    };

    //根据主键查询
    this.findOne = function (id) {
        return $http.get("../order/findOne.do?id=" + id);
    };


    //删除
    this.delete = function(selectedIds){
        return $http.get("../order/delete.do?ids=" + selectedIds);
    };

    //查询
    this.search = function (page, rows, searchEntity) {
        return $http.post("../order/search.do?page=" + page + "&rows=" + rows, searchEntity);
    };

    //查询品牌列表
    this.selectOptionList = function () {
        return $http.get("../order/selectOptionList.do");

    };

    this.updateStatus = function (selectedIds, status) {
        return $http.get("../order/updateOrderStatus.do?ids=" + selectedIds + "&status=" + status);
    };

});