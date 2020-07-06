$(document).ready(function () {

    $("#btnSubmit").click(function (event) {

        //stop submit the form, we will post it manually.
        event.preventDefault();

        index();

    });

    $("#btnSearch").click(function (event) {

        //stop submit the form, we will post it manually.
        event.preventDefault();

        search();

    });

});

function index() {

    // Get form
    var form = $('#indexForm')[0];

    var data = new FormData(form);

    $("#btnSubmit").prop("disabled", true);

    $.ajax({
        type: "POST",
        enctype: 'multipart/form-data',
        url: "/index",
        data: data,
        processData: false, //prevent jQuery from automatically transforming the data into a query string
        contentType: false,
        cache: false,
        timeout: 600000,
        success: function (data) {
            $('#result').empty();
            $("#result").text(data);
            console.log("SUCCESS : ", data);
            $("#btnSubmit").prop("disabled", false);

        },
        error: function (e) {
            $('#result').empty();
            $("#result").text(e.responseText);
            console.log("ERROR : ", e);
            $("#btnSubmit").prop("disabled", false);

        }
    });

}

function search() {
    const data = []
    const elements = $('#searchForm div')

    for (let i = 0; i < elements.length; i++) {
        const element = elements[i]
        let input = $(element).children('input')[0]
        let operation = $(element).children('select')[0]
        let value = input.value.trim();
        if (value.replace(/"/g, '').trim()) {
            data.push({
                field: input.name,
                value: input.value,
                operation: operation.value
            })
        }
    }

    $("#btnSearch").prop("disabled", true);

    $.ajax({
        type: "POST",
        url: "/search",
        data: JSON.stringify(data),
        contentType: 'application/json',
        success: function (data) {

            $('#result').empty();
            for (let i = 0; i < data.length; i++) {
                const result = data[i]
                $.each(result, function (key, value) {
                    $('#result').append('<li>' + key + ': ' + value + '</li>');
                });
                $('#result').append('<hr/>');
            }
            console.log("SUCCESS : ", data);
            $("#btnSearch").prop("disabled", false);

        },
        error: function (e) {
            $('#result').empty();
            $("#result").text(e.responseText);
            console.log("ERROR : ", e);
            $("#btnSearch").prop("disabled", false);

        }
    });
}
