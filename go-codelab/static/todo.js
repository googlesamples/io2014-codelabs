'use strict';

var app = angular.module('todo', ['ngRoute', 'restangular']);

app.config(['$routeProvider', '$locationProvider',
    function($routeProvider, $locationProvider) {
        $routeProvider.
        when('/:listId', {
            templateUrl: '/static/partials/list.html',
            controller: 'ListCtrl',
        }).
        otherwise({
            templateUrl: '/static/partials/home.html',
            controller: 'HomeCtrl',
        });
        $locationProvider.html5Mode(true);
    }
]);

app.controller('ListCtrl', ['$scope', '$routeParams', '$location', 'REST', 'Log',
    function($scope, $routeParams, $location, REST, Log) {
        var list = REST.one('api/list', $routeParams.listId);
        var tasks = list.all('task');

        list.get().then(null, function(err) {
            Log.error(err);
            $location.path("/");
        });

        $scope.list = list.get().$object;
        $scope.tasks = tasks.getList().$object;

        $scope.addTask = function() {
            tasks.post($scope.newTask).then(function(task) {
                $scope.tasks.push(task);
                $scope.newTask = {};
            }, Log.error);
        };

        $scope.toggle = function(task) {
            var copy = task.clone();
            copy.Done = !copy.Done;
            copy.patch().then(function() {
                task.Done = copy.Done;
            }, Log.error);
        };

        $scope.delete = function() {
            list.remove().then(function() {
                $location.path("/");
            }, Log.error);
        };
    }
]);

app.controller('HomeCtrl', ['$scope', 'REST', 'Log',
    function($scope, REST, Log) {
        var lists = REST.all('api/list').getList();
        lists.then(null, Log.error);

        $scope.lists = lists.$object;
        $scope.newList = REST.one('api/list');

        $scope.addList = function() {
            $scope.newList.post().then(function(list) {
                $scope.lists.push(list);
                $scope.newList = REST.one('api/list');
            }, Log.error);
        };
    }
]);

app.service('Log', ['$timeout',
    function($timeout) {
        var svc = {
            logs: [],
            error: function(msg) {
                svc.log(msg, 'error');
            },
            info: function(msg) {
                svc.log(msg, 'info');
            },
            log: function(msg, level) {
                svc.logs.push({
                    msg: msg.data || msg,
                    level: level
                });
                $timeout(function() {
                    svc.logs.shift();
                }, 3000);
            }
        };
        return svc;
    }
]);

app.controller('LogCtrl', ['$scope', 'Log',
    function($scope, Log) {
        $scope.logs = Log.logs;
    }
]);

app.service('REST', ['Restangular',
    function(Restangular) {
        return Restangular.withConfig(function(config) {
            // Our app uses 'ID' as identifier instead of 'id'.
            config.setRestangularFields({
                id: 'ID'
            });
            // Restangular sends DELETE ops with payload.
            // This fixes the issue.
            config.setRequestInterceptor(function(elem, op) {
                if (op == "remove") return "";
                return elem;
            })
        });
    }
]);

app.service('Auth', ['REST', 'Log',
    function(REST, Log) {
        return REST.one('api', 'auth').get().$object;
    }
]);

app.controller('AuthCtrl', ['$rootScope', 'Auth',
    function($rootScope, Auth) {
        $rootScope.auth = Auth;
    }
]);
