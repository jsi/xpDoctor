var dataValidator = require('/lib/dataValidator.js');

exports.get = function (req) {

    var taskId = dataValidator.execute();

    return {
        contentType: 'application/json',
        body: {
            taskId: taskId
        }
    }
};

